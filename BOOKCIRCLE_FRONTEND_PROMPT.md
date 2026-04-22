# Frontend Implementation Brief — BookCircle Feature

> Hand this to a frontend agent. It is self-contained: API specs, DTO shapes, auth model, visibility rules, error formats, and required UI screens.

---

## 1. What you are building

A "BookCircle" is a shared reading room. A user creates a circle, picks a book, sets a member cap (2–50), and chooses one of two visibility modes. The backend returns a 6-letter invite code (e.g. `KQXMVT`) the creator shares with friends. When someone joins:

- The book is **automatically added to their personal library** as a fresh `Book` entity (their own copy, page 0).
- They become a member and can track their own reading.
- Progress is per-member: each member's `Book.readPages` is independent.

**Visibility modes** (set at creation):
- `ALL_MEMBERS` — every member sees everyone's progress.
- `OWNER_ONLY` — only the owner sees per-member progress; other members see the roster (names, roles, join dates, member count) but **progress fields are `null` for everyone except themselves**.

**Drill-down view**: clicking a member opens a detail panel showing every reading `Session` and every `Note` for that member's circle book. Allowed for: the circle owner (always), the target member viewing themselves (always), any peer when `visibility == ALL_MEMBERS`.

Owner cannot leave a circle — they must **close** it instead. Closing sets `status = CLOSED` and blocks new joins; existing data is preserved.
Owner can also **remove** non-owner members. Removal deletes circle membership, preserves the removed user's personal book/history, and permanently blocks that user from rejoining the same circle with the same invite code.

---

## 2. Auth model

All endpoints require a Keycloak-issued JWT in the `Authorization: Bearer <token>` header. The frontend already handles Keycloak login for the rest of the app — reuse the existing token-fetch mechanism.

Base URL prefix: `http://<host>:8585/lexipal/api/v1` (dev port `8585`).

---

## 2a. Nickname onboarding (prerequisite)

Users are identified in circles by a unique **nickname** they pick after signing in. The onboarding flow lives on the existing profile endpoint; block all circle UI behind it.

- `GET /profile/me` returns `nickname` (string or `null`) and `needsOnboarding` (boolean). `needsOnboarding == true` iff the caller has no nickname yet.
- On first signin, show a blocking modal / screen that calls `PATCH /profile/nickname` with `{"nickname":"chosen_name"}` and only releases the app once the response has `needsOnboarding: false`.
- Nickname rules: `^[a-zA-Z0-9_]{3,20}$` — 3-20 chars, letters/digits/underscore only. Uniqueness is **case-insensitive** on the backend (so `Erik` and `erik` collide) but the original casing is preserved for display.
- Users can rename anytime by calling `PATCH /profile/nickname` again.

Errors from `PATCH /profile/nickname`:
- `400` with `errors.nickname` — validation failed. Show the message inline on the input.
- `409 Conflict` with `{ "message": "Nickname 'X' is already taken", "status": 409 }` — show inline error under the input.

---

## 3. Endpoints

All paths below are relative to `/lexipal/api/v1/book-circles`.

### 3.1 Create circle — `POST /`

Either reuse a book the user already owns (pass `existingBookId`) **or** create a fresh book by passing `bookTitle` + `bookAuthor` + `totalPages`. Never both.

Request body:
```json
{
  "existingBookId": 42,
  "bookTitle": null,
  "bookAuthor": null,
  "totalPages": null,
  "memberCap": 5,
  "visibility": "ALL_MEMBERS"
}
```
or
```json
{
  "existingBookId": null,
  "bookTitle": "Dune",
  "bookAuthor": "Frank Herbert",
  "totalPages": 688,
  "memberCap": 5,
  "visibility": "OWNER_ONLY"
}
```

Validation:
- `memberCap`: required, 2–50 inclusive.
- `visibility`: required, one of `"ALL_MEMBERS"` | `"OWNER_ONLY"`.
- `bookTitle` / `bookAuthor`: max 100 chars when present.
- `totalPages`: ≥ 1 when present.
- Cross-field: must provide either `existingBookId` OR all three of `bookTitle`/`bookAuthor`/`totalPages`. Backend returns `400` if both or neither.

Response `201 Created`:
```json
{
  "id": 17,
  "inviteCode": "KQXMVT",
  "bookTitle": "Dune",
  "bookAuthor": "Frank Herbert",
  "totalPages": 688,
  "memberCap": 5,
  "visibility": "OWNER_ONLY",
  "status": "ACTIVE",
  "createdAt": "2026-04-21T22:13:04.512"
}
```

The owner is automatically a member with role `OWNER`. If `existingBookId` was passed, the owner's existing `Book` row is linked. Otherwise a new `Book` is created in their library.

### 3.2 Join circle — `POST /join`

Request body:
```json
{ "inviteCode": "KQXMVT" }
```

- `inviteCode` is exactly 6 chars. Backend uppercases the input, so `kqxmvt` works too.
- Letters `I` and `O` are excluded from generated codes (visual ambiguity), but accept any A–Z input — the backend will return 404 if not found.

On success: a new `Book` is created in the joiner's library (title/author/pages snapshotted from the circle), and they are added as `MEMBER`.

Response `200 OK`: full `BookCircleReadResponseDTO` (see §3.3).

Errors:
- `404` — invite code not found, or circle is not `ACTIVE`.
- `400` — circle full (`countByCircle_Id >= memberCap`) or already a member.

### 3.3 Get circle — `GET /{id}`

Caller must be a member. Returns visibility-filtered detail.

Response `200 OK`:
```json
{
  "id": 17,
  "inviteCode": "KQXMVT",
  "bookTitle": "Dune",
  "bookAuthor": "Frank Herbert",
  "totalPages": 688,
  "memberCap": 5,
  "currentMemberCount": 3,
  "visibility": "OWNER_ONLY",
  "status": "ACTIVE",
  "ownerNickname": "erik_e",
  "callerIsOwner": true,
  "createdAt": "2026-04-21T22:13:04.512",
  "members": [
    {
      "userId": 7,
      "userNickname": "erik_e",
      "role": "OWNER",
      "readPages": 412,
      "totalPages": 688,
      "progressPercentage": 59.88,
      "joinedAt": "2026-04-21T22:13:04.512"
    },
    {
      "userId": 9,
      "userNickname": "lina42",
      "role": "MEMBER",
      "readPages": 120,
      "totalPages": 688,
      "progressPercentage": 17.44,
      "joinedAt": "2026-04-21T22:30:18.901"
    }
  ]
}
```

**Visibility-driven nullability** in `members[]`:
- `visibility == "ALL_MEMBERS"` OR `callerIsOwner == true`: every member's `readPages`, `totalPages`, `progressPercentage` are populated.
- `visibility == "OWNER_ONLY"` AND caller is a non-owner member: `readPages`, `totalPages`, `progressPercentage` are `null` for all rows **except** the row where `userId === <currentUser>.id`. The roster (names, roles, joinedAt, member count) is always visible.

Errors: `404` if circle does not exist; `403` if caller is not a member.

### 3.4 List my circles — `GET /mine`

Returns active circles where the caller is a member (owner or otherwise). Response `200 OK`: `BookCircleReadResponseDTO[]` (same shape as §3.3, one entry per circle).

### 3.5 Member drill-down — `GET /{id}/members/{userId}/details`

`{userId}` is the **internal `User.id`** of the target member (the same value returned in `members[].userId` from §3.3), NOT a Keycloak sub.

Response `200 OK`:
```json
{
  "circleId": 17,
  "userId": 9,
  "userNickname": "lina42",
  "role": "MEMBER",
  "joinedAt": "2026-04-21T22:30:18.901",
  "readPages": 120,
  "totalPages": 688,
  "progressPercentage": 17.44,
  "bookId": 53,
  "bookTitle": "Dune",
  "bookAuthor": "Frank Herbert",
  "bookStatus": "ACTIVE",
  "sessions": [
    {
      "sessionId": 201,
      "sessionDate": "2026-04-20T19:00:00",
      "completedAt": "2026-04-20T19:42:13",
      "durationSeconds": 2533,
      "startPage": 80,
      "endPage": 120,
      "pagesRead": 40,
      "status": "COMPLETED",
      "reflection": "The Fremen scene...",
      "capturedWordCount": 4
    }
  ],
  "notes": [
    {
      "noteId": 88,
      "quotedText": "Fear is the mind-killer.",
      "pageNumber": 23,
      "chapterLabel": "Chapter 3",
      "createdAt": "2026-04-19T20:14:55",
      "sourceSessionId": 199
    }
  ]
}
```

Sessions are sorted by `sessionDate DESC`. Notes are sorted by `createdAt DESC`.

`status` enum values for sessions: `"IN_PROGRESS"` | `"COMPLETED"`.
`bookStatus` enum values: `"ACTIVE"` | `"COMPLETE"` | `"ARCHIVED"` | `"DELETED"`.

Authorization rules (enforced by backend, return `403` if violated):
- Owner can drill into any member.
- A member can always drill into themselves.
- A member can drill into another member **only** when `visibility == "ALL_MEMBERS"`.

Errors: `404` if circle or membership not found; `403` if not allowed.

### 3.6 Leave circle — `DELETE /{id}/members/me`

Caller is removed from the circle. Their personal `Book` row stays in their library (progress preserved).

Response `204 No Content`.

Errors:
- `400` if caller is the **owner** — they must close the circle, not leave it. Message: `"Owner cannot leave the circle. Close the circle instead."`
- `404` if caller is not a member of that circle.

### 3.7 Remove member — `DELETE /{id}/members/{userId}` (owner only)

`{userId}` is the internal `User.id` of the target member from `members[].userId`.

Owner removes a non-owner member from the circle. This does **two** things atomically:
- deletes the target's active circle membership
- permanently blocks that user from rejoining the same circle

The removed user's personal `Book` row stays in their library, along with all `Session` and `Note` history tied to that book.

Response `204 No Content`.

Errors:
- `403` if caller is not the owner. Message: `"Only the circle owner can remove members"`
- `404` if the target user is not currently a member of that circle
- `400` if the target is the owner. Message: `"Owner cannot be removed from the circle."`

If a removed user later tries `POST /book-circles/join` with the same invite code, backend returns `403` with message: `"You have been removed from this circle and cannot rejoin."`

### 3.8 Close circle — `DELETE /{id}` (owner only)

Sets `status = CLOSED`. Existing members stay in the data, but the circle is hidden from `GET /mine` and the invite code stops working for new joins.

Response `204 No Content`.

Errors: `403` if caller is not the owner; `404` if circle not found.

---

## 4. Error response shape

All errors returned by the backend's `GlobalExceptionHandler`:

Standard error:
```json
{ "message": "BookCircle with inviteCode 'XYZABC' not found'", "status": 404 }
```

Validation error (from `@Valid` failures, e.g. missing `memberCap`):
```json
{
  "message": "Validation failed",
  "errors": {
    "memberCap": "Member cap is required",
    "visibility": "Visibility is required"
  }
}
```

Status codes you will see:
- `200` / `201` / `204` — success
- `400` — bad input, full circle, already member, owner trying to leave, owner targeted for removal, invalid book-source combo
- `403` — not a member / not the owner / blocked from rejoining after owner removal
- `404` — circle, member, or book not found
- `502` — could not generate a unique invite code after 10 attempts (rare; show a generic "Try again" toast)

---

## 5. Date format

LocalDateTime fields on circle DTOs (no explicit `@JsonFormat`) are serialized by Jackson's `java.time` module as ISO-8601 strings without a timezone, e.g. `"2026-04-21T22:13:04.512"`. Parse with `new Date(...)` or your library of choice; treat the value as **server local time** (Stockholm / Europe). Do not assume UTC.

---

## 6. UI screens to build

Build these screens. Use the existing app's design system, Keycloak token wiring, and book-card styling for consistency.

### 6.1 Circles list (`/circles`)
- Calls `GET /book-circles/mine`.
- Renders one card per circle: book title + author, member count vs cap (`3 / 5`), visibility badge, owner badge if `callerIsOwner`, invite code (with copy-to-clipboard button) only if `callerIsOwner`.
- Empty state: "You're not in any book circles yet" with two CTAs — **Create circle** and **Join with code**.

### 6.2 Create circle modal/page
- Tab or toggle for **Use existing book** vs **Add new book**.
  - "Use existing": list the user's existing books (call the existing `GET /books/all` endpoint) and pick one — submit `existingBookId`.
  - "Add new": three text/number fields for title, author, totalPages — submit those.
- Member cap: number input or stepper (default 5, range 2–50).
- Visibility: radio group with two options:
  - "Everyone sees progress" → `ALL_MEMBERS`
  - "Only I see progress" → `OWNER_ONLY`
- On `201`, show the returned invite code in a success step with a big copy button and a "Share" CTA. Then route to the new circle's detail page.

### 6.3 Join circle modal
- Single 6-char input (uppercased on the fly). Show validation error if not 6 chars.
- On submit `POST /book-circles/join` with `{ inviteCode }`.
- On success, route to the circle detail page and toast "Book added to your library."
- On `404`: "No active circle found for that code."
- On `400`: show the backend message verbatim ("This circle is full" / "You are already a member of this circle").
- On `403`: show the backend message verbatim. Most important case: removed member trying to rejoin sees "You have been removed from this circle and cannot rejoin."

### 6.4 Circle detail page (`/circles/:id`)
- Calls `GET /book-circles/{id}`.
- Header: book title/author, invite code (copy button if owner), member count, visibility chip, status chip.
- Owner-only actions: "Close circle" button (calls `DELETE /book-circles/{id}`, confirm first).
- Non-owner actions: "Leave circle" button (calls `DELETE /book-circles/{id}/members/me`, confirm first).
- Member list: one row per member showing nickname (or "You" when `userId === currentUser.id`; fall back to "Anonymous" on the rare chance `userNickname` is `null`), role badge (OWNER/MEMBER), join date, and a progress bar.
  - When `progressPercentage` is `null` (OWNER_ONLY visibility for peers), show a lock icon and the text "Hidden by owner" instead of the bar — but still show role and join date.
  - For owners, show a "Remove member" action on non-owner rows only. It should call `DELETE /book-circles/{id}/members/{userId}` and require confirmation first.
  - Never show "Remove member" on the owner row.
- Clicking a row opens the drill-down panel (§6.5). Disable the click when drill-down is not allowed (peer in OWNER_ONLY): show a tooltip "Progress hidden by circle owner."

  Drill-down allowed-when logic on the client (mirror the backend):
  ```
  canDrillInto(targetUserId) =
      callerIsOwner
      || targetUserId === currentUser.id
      || visibility === "ALL_MEMBERS"
  ```

### 6.5 Member drill-down panel
- Calls `GET /book-circles/{id}/members/{userId}/details`.
- Top: large progress bar with `readPages / totalPages` and percentage.
- Tabs: **Sessions** and **Notes**.
  - **Sessions tab**: list cards sorted by `sessionDate` desc. Each card shows date, duration (format `durationSeconds` as `Xh Ym`), pages `startPage → endPage` (or "in progress" when `endPage` is null), `status` chip (IN_PROGRESS/COMPLETED), and the `reflection` text (collapsible if long).
  - **Notes tab**: list cards sorted by `createdAt` desc. Each card shows the `quotedText` as a blockquote, page number, optional `chapterLabel`, and `createdAt` date.
- Empty states: "No reading sessions yet" / "No notes yet."

---

## 7. State management notes

- After a successful `POST /book-circles` or `POST /book-circles/join`, **invalidate** the user's books cache — the joiner's library has a new book, and the owner's library may have one too.
- After `DELETE /book-circles/{id}` (close), `DELETE /book-circles/{id}/members/me` (leave), or `DELETE /book-circles/{id}/members/{userId}` (owner remove), refetch `GET /book-circles/mine`.
- After owner removal, also refetch the currently open circle detail so the roster updates immediately.
- The drill-down panel should refetch every time it opens — sessions/notes are fast-changing.

---

## 8. Things the backend deliberately does NOT do (do not assume otherwise)

- **No invite-code regeneration.** If the code leaks, the owner must close the circle and create a new one.
- **No ownership transfer.** Owner is fixed for the circle's lifetime.
- **No notifications, chat, or activity feed** inside circles.
- Joiners get a **fresh `Book` row** every time they join — even if they already own a personal copy of the same book under a different name. There is no dedup. UI should not try to merge them.

---

## 9. Acceptance walkthrough (manual smoke test)

1. User A logs in, opens **Create circle**, picks "Add new book", fills `Dune / Frank Herbert / 688`, sets cap=3, visibility=ALL_MEMBERS, submits. Sees invite code `XXXXXX`. Routes to circle page. Verifies `Dune` now appears in their library.
2. Copies code, sends to User B.
3. User B logs in, opens **Join with code**, pastes `XXXXXX`, submits. Sees the circle and `Dune` in their library at page 0.
4. User B records a reading session via the existing sessions UI, hitting page 100.
5. User A reopens the circle page. User B's row shows a 14% progress bar.
6. User A clicks User B's row → drill-down opens, showing the session card and any notes.
7. User C joins (cap=3 → A, B, C). User D tries to join → toast "This circle is full."
8. User C tries to join again → toast "You are already a member of this circle."
9. Switch the test to a fresh circle with `visibility=OWNER_ONLY`. Repeat step 5 — User B (a non-owner) sees Member C in the roster but with a lock icon and no progress numbers. User B can still see their own progress.
10. User B clicks "Leave circle" → confirms → routed back to `/circles`. `Dune` is **still** in B's library (verify via `GET /books/all`).
11. In a fresh circle, owner clicks "Remove member" on User B → confirm. User B disappears from the roster, still has `Dune` in their library, and a fresh attempt by B to join with the same code → "You have been removed from this circle and cannot rejoin."
12. Owner clicks "Close circle" → confirm. Circle disappears from `GET /mine`. A fresh attempt by anyone else to join with that code → "No active circle found for that code."

---

## 10. Quick reference table

| Action | Method | Path | Body | Success | Notable errors |
|---|---|---|---|---|---|
| Get profile | GET | `/profile/me` | — | 200 + `ProfileReadResponseDTO` (incl. `nickname`, `needsOnboarding`) | — |
| Set nickname | PATCH | `/profile/nickname` | `{ nickname }` | 200 + `ProfileReadResponseDTO` | 400 (validation), 409 (taken) |
| Create | POST | `/book-circles` | `BookCircleCreateRequestDTO` | 201 + `BookCircleCreateResponseDTO` | 400 (validation, book-source rule) |
| Join | POST | `/book-circles/join` | `{ inviteCode }` | 200 + `BookCircleReadResponseDTO` | 404 (bad code), 400 (full / dup), 403 (blocked after owner removal) |
| Get one | GET | `/book-circles/{id}` | — | 200 + `BookCircleReadResponseDTO` | 403 (not member), 404 |
| List mine | GET | `/book-circles/mine` | — | 200 + `BookCircleReadResponseDTO[]` | — |
| Drill-down | GET | `/book-circles/{id}/members/{userId}/details` | — | 200 + `CircleMemberDetailResponseDTO` | 403 (visibility), 404 |
| Remove member | DELETE | `/book-circles/{id}/members/{userId}` | — | 204 | 403 (not owner), 404 (not member), 400 (owner target) |
| Leave | DELETE | `/book-circles/{id}/members/me` | — | 204 | 400 (owner), 404 (not member) |
| Close | DELETE | `/book-circles/{id}` | — | 204 | 403 (not owner), 404 |

---

That's the full spec. Build the screens, wire the calls, respect the visibility rules, and show clear messages on the documented errors. If anything in the backend response shape surprises you, treat the backend as source of truth and ask before guessing.
