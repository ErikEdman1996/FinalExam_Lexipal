# BookCircle — Backend Implementation Plan

## Context

Lexipal currently models reading as a solo activity — each `User` owns their own `Book` records (`@ManyToOne User` on `Book`), progress tracked via `Session.endPage` and synced into `Book.readPages`. There is no social layer.

**Goal:** Add a "BookCircle" feature — a shared reading room where a creator picks a book + member cap, shares a 6-letter invite code, and joiners automatically get the book added to their library. Progress visibility is either shared across all members or creator-only.

**Why it fits the stack:** Since each user already owns a per-user `Book` copy, a BookCircle does not merge progress into one row — each member keeps their own `Book` + `Session` rows. The circle is a *coordination* entity that links these per-user `Book` rows together so progress can be aggregated and displayed under a shared visibility rule.

---

## Package / File Layout

All new files under `com.lexipal.core` matching existing conventions.

```
entities/
  BookCircle.java              (new)
  CircleMember.java            (new — join entity)
enums/ (or inside entities/)
  CircleStatus.java            (new — ACTIVE, CLOSED, DELETED)
  CircleVisibility.java        (new — ALL_MEMBERS, OWNER_ONLY)
  CircleMemberRole.java        (new — OWNER, MEMBER)
repositories/
  BookCircleRepository.java    (new)
  CircleMemberRepository.java  (new)
services/
  BookCircleServiceInterface.java (new)
  BookCircleService.java       (new)
  InviteCodeGenerator.java     (new — utility)
controllers/
  BookCircleController.java    (new)
dtos/requests/
  BookCircleCreateRequestDTO.java
  BookCircleJoinRequestDTO.java
dtos/responses/
  BookCircleCreateResponseDTO.java
  BookCircleReadResponseDTO.java
  CircleMemberProgressDTO.java
  CircleMemberDetailResponseDTO.java
  CircleSessionDTO.java
  CircleNoteDTO.java
exceptions/
  (reuse existing — see "Error handling")
```

No migration files needed. `ddl-auto=update` (confirmed in `application.properties`) handles schema.

---

## Entities

### 1. `BookCircle`

Mirrors style of `Book.java`: `Long id` IDENTITY, `@PrePersist` for `createdAt`, status enum.

Fields:
- `Long id` — PK
- `String inviteCode` — **unique, 6 uppercase letters**, indexed (`@Column(unique=true, length=6)`)
- `String bookTitle`, `String bookAuthor`, `Integer totalPages` — snapshot of the chosen book (so circle survives if creator deletes their `Book`)
- `Integer memberCap` — max members incl. owner (validate range, e.g. 2–50)
- `CircleVisibility visibility` — `ALL_MEMBERS` | `OWNER_ONLY`
- `CircleStatus status` — `ACTIVE` | `CLOSED` | `DELETED`
- `@ManyToOne(fetch=LAZY) User owner` — creator (needed for authorization even if also in `CircleMember` table)
- `@OneToMany(mappedBy="circle", cascade=ALL, orphanRemoval=true) List<CircleMember> members`
- `LocalDateTime createdAt` — `@PrePersist`

### 2. `CircleMember` (join entity)

Rationale: join entity over `@ManyToMany` lets us track `joinedAt`, `role`, and link to the per-user `Book` row that was auto-added on join.

Fields:
- `Long id` — PK
- `@ManyToOne(fetch=LAZY) BookCircle circle`
- `@ManyToOne(fetch=LAZY) User user`
- `@ManyToOne(fetch=LAZY) Book book` — the user's own `Book` row (auto-created at join for members; for owner this is either an existing chosen Book or a freshly created one — see `createCircle` flow)
- `CircleMemberRole role` — `OWNER` | `MEMBER`
- `LocalDateTime joinedAt`
- Unique constraint on `(circle_id, user_id)` to prevent double-join

---

## Repositories

### `BookCircleRepository extends JpaRepository<BookCircle, Long>`
```java
Optional<BookCircle> findByInviteCodeAndStatus(String code, CircleStatus status);
boolean existsByInviteCode(String code);  // used by code generator for collision check

@Query("SELECT bc FROM BookCircle bc JOIN bc.members m " +
       "WHERE m.user.keycloakSub = :sub AND bc.status = 'ACTIVE'")
List<BookCircle> findActiveCirclesForUser(@Param("sub") String sub);
```

### `CircleMemberRepository extends JpaRepository<CircleMember, Long>`
```java
Optional<CircleMember> findByCircleIdAndUserId(Long circleId, Long userId);
long countByCircleId(Long circleId);
List<CircleMember> findByCircleId(Long circleId);
```

---

## Services

### `InviteCodeGenerator` (utility, `@Component`)

- Generates 6-char `[A-Z]` code using `SecureRandom`.
- Exclude visually ambiguous chars (`I`, `O`) — alphabet size 24, giving 24⁶ ≈ 191M combos (plenty).
- `generateUnique(BookCircleRepository repo)` loops until `!existsByInviteCode(code)`, max 10 attempts, throws `ExternalServiceException` if exhausted.

### `BookCircleService` (interface + impl, matches `BookService` pattern)

Constructor-inject: `BookCircleRepository`, `CircleMemberRepository`, `BookRepository`, `SessionRepository`, `NoteRepository`, `UserService`, `BookService`, `InviteCodeGenerator`.

Methods (all `@Transactional` unless read-only):

```java
BookCircleCreateResponseDTO createCircle(BookCircleCreateRequestDTO req, String keycloakSub);
BookCircleReadResponseDTO joinCircle(String inviteCode, String keycloakSub);
BookCircleReadResponseDTO getCircle(Long circleId, String keycloakSub);
List<BookCircleReadResponseDTO> getMyCircles(String keycloakSub);
CircleMemberDetailResponseDTO getMemberDetails(Long circleId, Long targetUserId, String keycloakSub);
void leaveCircle(Long circleId, String keycloakSub);
void closeCircle(Long circleId, String keycloakSub);  // owner only
```

#### `createCircle` flow
1. `User owner = userService.getOrCreateUser(keycloakSub)`.
2. Validate `memberCap` (2–50). Cross-field check on book source (see DTO note).
3. Resolve owner's `Book`:
   - **If `existingBookId` provided:** load via `bookRepository.findById`; if not found → `ResourceNotFoundException`; if `book.user.id != owner.id` → `UnauthorizedActionException`; snapshot `bookTitle`/`bookAuthor`/`totalPages` from this Book onto the circle.
   - **Else:** call `bookService.addBook(new BookCreateRequestDTO(title, author, totalPages), keycloakSub)` to create a fresh Book; snapshot the same fields onto the circle.
4. Generate unique invite code.
5. Build `BookCircle`, persist.
6. Persist `CircleMember { circle, user=owner, book=resolvedBook, role=OWNER }`.
7. Return DTO with invite code.

#### `joinCircle` flow
1. `User user = userService.getOrCreateUser(keycloakSub)`.
2. Load circle by `findByInviteCodeAndStatus(code, ACTIVE)` → `ResourceNotFoundException` if absent.
3. Reject if `countByCircleId(circle.id) >= circle.memberCap` → `IllegalStateException` (409 via handler) or new `CircleFullException`.
4. Reject if `findByCircleIdAndUserId(...)` already exists → `IllegalStateException` "already a member".
5. **Auto-add book to library**: call `bookService.addBook(new BookCreateRequestDTO(circle.bookTitle, circle.bookAuthor, circle.totalPages), keycloakSub)` — reuses existing cascade/owner wiring.
6. Persist `CircleMember { circle, user, book, role=MEMBER }`.
7. Return circle DTO with members/progress according to visibility.

#### `getMemberDetails` flow (drill-down view)

Returns full reading history for one member of a circle: every `Session` and every `Note` tied to that member's circle-linked `Book`.

1. Load circle by id → `ResourceNotFoundException` if absent.
2. Load `CircleMember` for `(circleId, targetUserId)` → `ResourceNotFoundException` if absent.
3. **Authorization** — caller must satisfy ONE of:
   - Caller is the circle owner (always allowed regardless of visibility).
   - Caller is the target user themselves (members can always drill into their own data).
   - Caller is any member AND `circle.visibility == ALL_MEMBERS` (peer drill-down only when progress is shared).
   - Else → `UnauthorizedActionException`.
4. Resolve `Book book = member.book`.
5. Fetch sessions: `sessionRepository.findByBook_IdOrderBySessionDateDesc(book.id)` (add derived method if absent).
6. Fetch notes: `noteRepository.findByBook_IdOrderByCreatedAtDesc(book.id)` (add derived method if absent — verify in `NoteRepository`).
7. Map to `CircleMemberDetailResponseDTO` with member metadata + session list + note list.

Repository additions (if not already present):
```java
// SessionRepository
List<Session> findByBook_IdOrderBySessionDateDesc(Long bookId);

// NoteRepository
List<Note> findByBook_IdOrderByCreatedAtDesc(Long bookId);
```

#### `leaveCircle` flow
1. Load `CircleMember` for `(circleId, callerUserId)` → `ResourceNotFoundException` if absent.
2. If caller is `OWNER` → reject with `IllegalArgumentException` "owner must close circle, not leave" (or transfer ownership — out of scope).
3. Delete the `CircleMember` row only. **Per-user `Book` is left untouched** — member keeps their progress and the book stays in their library. No cascade to `Book`.

#### Progress aggregation (for DTO build)
- For each `CircleMember`, read `member.book.readPages`, `member.book.totalPages`, compute percent.
- Visibility filter:
  - `ALL_MEMBERS` → return every member's full progress row.
  - `OWNER_ONLY`:
    - If caller is owner → return every member's full progress row.
    - Else → return roster (userId, displayName, role, joinedAt) for **all** members, but null out `readPages` / `totalPages` / `percent` for everyone except the caller's own row. Member count visible.

---

## Controller

`BookCircleController` at `/lexipal/api/v1/book-circles` — matches plural-noun convention used by `BookController`.

```java
POST   /                              createCircle        (body: BookCircleCreateRequestDTO)
POST   /join                          joinCircle          (body: { inviteCode })
GET    /{id}                          getCircle           (summary view, visibility-filtered)
GET    /mine                          getMyCircles
GET    /{id}/members/{userId}/details getMemberDetails    (drill-down: sessions + notes)
DELETE /{id}/members/me               leaveCircle
DELETE /{id}                          closeCircle         (owner only)
```

Every endpoint: `@AuthenticationPrincipal Jwt jwt` → `jwt.getSubject()` passed to service. Request DTOs annotated `@Valid`. No `@PreAuthorize` — authorization is done in service via ownership/membership check (matches current codebase).

---

## DTOs

### `BookCircleCreateRequestDTO`
```
Long existingBookId           // OPTIONAL — if set, owner reuses an existing Book they own
@Size(max=100) String bookTitle    // required only when existingBookId == null
@Size(max=100) String bookAuthor   // required only when existingBookId == null
@Min(1) Integer totalPages         // required only when existingBookId == null
@NotNull @Min(2) @Max(50) Integer memberCap
@NotNull CircleVisibility visibility
```
Cross-field validation in service: exactly one of (`existingBookId`) or (`bookTitle` + `bookAuthor` + `totalPages`) must be present. If `existingBookId`, load the `Book`, verify `book.user.id == owner.id` else `UnauthorizedActionException`, then snapshot title/author/totalPages onto the circle.

### `BookCircleCreateResponseDTO`
```
Long id, String inviteCode, String bookTitle, String bookAuthor,
Integer totalPages, Integer memberCap, CircleVisibility visibility,
CircleStatus status, LocalDateTime createdAt
```

### `BookCircleReadResponseDTO`
```
Long id, String inviteCode, String bookTitle, String bookAuthor,
Integer totalPages, Integer memberCap, Integer currentMemberCount,
CircleVisibility visibility, CircleStatus status,
String ownerDisplayName, boolean callerIsOwner,
List<CircleMemberProgressDTO> members   // filtered by visibility
```

### `CircleMemberProgressDTO`
```
Long userId, String displayName, CircleMemberRole role,
Integer readPages, Integer totalPages, Double percent,
LocalDateTime joinedAt
```

### `CircleMemberDetailResponseDTO` (drill-down payload)
```
Long circleId,
Long userId, String displayName, CircleMemberRole role, LocalDateTime joinedAt,
Integer readPages, Integer totalPages, Double percent,
Long bookId, String bookTitle, String bookAuthor, BookStatus bookStatus,
List<CircleSessionDTO> sessions,
List<CircleNoteDTO> notes
```

### `CircleSessionDTO`
```
Long sessionId, LocalDateTime sessionDate, LocalDateTime completedAt,
Long durationSeconds, Integer startPage, Integer endPage, Integer pagesRead,
SessionStatus status, String reflection
```

### `CircleNoteDTO`
```
Long noteId, String content, Integer page, LocalDateTime createdAt, LocalDateTime updatedAt
```
(Field names mirror `Note` entity — confirm exact fields when implementing.)

(Manual mapping in service via private `toXxxResponse(...)` helpers — matches `BookService` L41 `toBookCreateResponse`.)

---

## Error handling

Reuse existing exceptions — no new ones needed unless we want `CircleFullException`:
- Circle not found / wrong code → `ResourceNotFoundException` (404)
- Non-owner tries to close / non-member tries to read → `UnauthorizedActionException` (403)
- Circle full / already member / closed → `IllegalArgumentException` (400) — existing `GlobalExceptionHandler` already handles these
- Invite code gen exhaustion → `ExternalServiceException` (502)

---

## Critical existing files to reference (no modification needed)

- `src/main/java/com/lexipal/core/entities/Book.java` — entity pattern
- `src/main/java/com/lexipal/core/entities/User.java` — `@OneToMany books`, cascade behavior
- `src/main/java/com/lexipal/core/entities/Session.java` — drill-down session source
- `src/main/java/com/lexipal/core/entities/Note.java` — drill-down note source
- `src/main/java/com/lexipal/core/services/BookService.java` — **reused** via `addBook(...)` for auto-library-add on join
- `src/main/java/com/lexipal/core/services/UserService.java` — `getOrCreateUser(keycloakSub)`
- `src/main/java/com/lexipal/core/controllers/BookController.java` — controller style
- `src/main/java/com/lexipal/core/exceptions/GlobalExceptionHandler.java` — existing error shape

---

## Verification plan

1. **Start app** — confirm `ddl-auto=update` creates `book_circle` + `circle_member` tables with unique index on `invite_code` and `(circle_id, user_id)`.
2. **Integration (Postman / curl with Keycloak JWT):**
   - User A: `POST /book-circles` → receives invite code `XYZABC`. Verify Book added to A's library (`GET /books`).
   - User B: `POST /book-circles/join` with `XYZABC` → 200, book appears in B's library.
   - User C: join → 200.
   - User D (cap=3): join → 400 "full".
   - User B joins again → 400 "already member".
   - A: `GET /book-circles/{id}` with `ALL_MEMBERS` visibility → sees B, C progress.
   - B: same call on an `OWNER_ONLY` circle → sees full roster (names/roles) but progress fields nulled for everyone except B's own row.
   - **Drill-down (owner):** A `GET /book-circles/{id}/members/{B-userId}/details` → 200, returns B's sessions + notes for the circle book.
   - **Drill-down (self):** B `GET /.../members/{B-userId}/details` → 200.
   - **Drill-down (peer, ALL_MEMBERS):** B viewing C → 200.
   - **Drill-down (peer, OWNER_ONLY):** B viewing C → 403.
   - User B: `DELETE /book-circles/{id}/members/me` on a circle → 200, B's `CircleMember` gone, but B's `Book` still in their library (`GET /books`).
   - Owner attempting `/members/me` → 400 "owner must close circle, not leave".
   - A: `DELETE /book-circles/{id}` → status=CLOSED, join attempts now 404.
   - Random 6-letter code → 404.
3. **Progress sync check:** B creates a `Session` on their circle-linked book → `Book.readPages` updates → circle read of progress reflects it (no extra wiring needed since we reuse per-user Book).
4. **Code collision:** mock `SecureRandom` to return a duplicate; confirm regenerate loop kicks in.

---

## Resolved decisions

- **Owner book source:** Allow both — optional `existingBookId` in create DTO. Service branches on presence.
- **On member leave:** Keep per-user `Book` untouched; only `CircleMember` row is deleted.
- **`OWNER_ONLY` visibility:** Roster (names, roles, joinedAt) visible to all members; per-member `readPages`/`percent` visible only to owner and to the caller themselves.
- **Member detail drill-down:** Dedicated endpoint returns full session + note history for one member's circle book. Allowed for: owner (always), the target user themselves (always), any peer member when `visibility == ALL_MEMBERS`.

## Out of scope (future work)

- Owner regenerating invite code if leaked (`POST /{id}/regenerate-code`).
- Ownership transfer when owner wants to leave.
- Deduping when a user joins a circle for a book they already own a personal copy of.
- Notifications / chat / discussion threads inside the circle.
