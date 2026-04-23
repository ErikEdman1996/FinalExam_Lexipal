# Frontend Implementation Brief — Reading List Feature

> Hand this to a frontend agent. It is self-contained: API specs, DTO shapes, auth model, flow rules, XP mechanics, error formats, and required UI screens.

---

## 1. What you are building

A **reading list** is a public, user-curated roadmap of books. Any user can:

- **Author** a list — pick a title, description, and an ordered list of books (title + author only, no page counts).
- **Browse** published lists, filter by minimum star count, sort by popularity.
- **Star** a list (one star per user, toggleable — think GitHub stars). Star count drives discovery.
- **Subscribe** to a list — this marks them as "working through it" and activates per-slot progress tracking.
- **Earn bonus XP** when they finish a book that belongs to a list they subscribed to (+50), and a big bonus when they finish the whole list (+200).

### Key model rules (READ CAREFULLY)

1. **Books in a list are metadata only.** A slot holds `title + author + optional note`. There is **no `totalPages` field** because page counts vary by edition — the subscriber decides that when they add the book to their personal library.
2. **Matching is by normalized title + author.** Casing and surrounding whitespace are ignored. `"Dune" by "Frank Herbert"` matches `"dune" by " frank herbert "`.
3. **Subscribing does NOT auto-create personal Book entities.** The subscriber must manually add each book to their library via the existing `POST /books` endpoint (they already know how — it's the same flow as today). The UI should make this easy: show an "Add to my library" button per slot.
4. **Completion is derived.** A slot is "complete" iff the subscribing user has a personal Book with status `COMPLETE` whose normalized title+author matches the slot's catalog book. The backend computes this; the frontend just renders `completedByMe`.
5. **XP is automatic.** When the user's personal Book flips to `COMPLETE` (which already happens via the existing session-completion flow), the backend fires the reading-list XP hook. The frontend does not need to call anything extra — just re-fetch subscription progress / XP after a session completes.

---

## 2. Auth model

All endpoints require a Keycloak-issued JWT in the `Authorization: Bearer <token>` header. Reuse the existing token-fetch mechanism.

Base URL prefix: `http://<host>:8585/lexipal/api/v1` (dev port `8585`).

Unauthenticated requests → `401 Unauthorized`.

---

## 3. Endpoints

All paths below are relative to `/lexipal/api/v1/reading-lists`.

### 3.1 Create list — `POST /`

Request body (`ReadingListCreateRequestDTO`):
```json
{
  "title": "Sci-Fi Foundations",
  "description": "A curated journey through the genre's backbone.",
  "status": "PUBLISHED",
  "books": [
    { "title": "Dune",  "author": "Frank Herbert",  "note": "Start here — sets the tone.", "position": 1 },
    { "title": "1984",  "author": "George Orwell",  "note": null,                           "position": 2 },
    { "title": "Neuromancer", "author": "William Gibson", "note": "Short but dense.",     "position": 3 }
  ]
}
```

Validation rules:
- `title`: required, non-blank, ≤ 120 chars.
- `description`: optional, ≤ 2000 chars.
- `status`: optional. Defaults to `PUBLISHED`. Accepted values: `DRAFT | PUBLISHED | ARCHIVED`.
- `books`: required, non-empty array of slots.
- Each slot:
  - `title`: required, non-blank, ≤ 200 chars.
  - `author`: required, non-blank, ≤ 200 chars.
  - `note`: optional, ≤ 500 chars.
  - `position`: required integer.
- No two slots may share the same `position`.
- No two slots may share the same normalized `(title, author)` — duplicates within a list are rejected.

Response: `201 Created` with `ReadingListDetailResponseDTO` (see §4.3).

Errors:
- `400` — validation failure or duplicate title/position across slots.

### 3.2 Browse public lists — `GET /`

Query params (all optional):
- `q` — free-text substring match against list title (case-insensitive).
- `minStars` — integer, lists with `starCount < minStars` are excluded. Defaults to `0`.
- `page` — 0-based page index. Defaults to `0`.
- `size` — page size. Defaults to `20`, max `100`.

Only lists with `status = PUBLISHED` are returned. Results are sorted by `starCount` descending, then by `createdAt` descending.

Response: `200 OK` with `ReadingListSummaryResponseDTO[]` (see §4.1). The array is the page content — pagination metadata is NOT returned in the body (frontend paginates by incrementing `page` and checking whether the returned array is shorter than `size`).

### 3.3 My authored lists — `GET /mine`

Lists that the caller owns, ordered by `createdAt` descending. Includes `DRAFT` and `ARCHIVED` too (so the author can manage them).

Response: `200 OK` with `ReadingListSummaryResponseDTO[]`.

### 3.4 My subscriptions — `GET /subscriptions`

Lists the caller has subscribed to, with progress.

Response: `200 OK` with `ReadingListSubscriptionResponseDTO[]` (see §4.4), ordered by `subscribedAt` descending.

### 3.5 Get list detail — `GET /{id}`

Returns the full list with ordered slots and per-slot completion flags.

Response: `200 OK` with `ReadingListDetailResponseDTO` (see §4.3).

Errors:
- `404` — list not found.

### 3.6 Update list metadata — `PATCH /{id}` (owner only)

Request body (`ReadingListUpdateRequestDTO`): all fields optional; only provided fields are applied.
```json
{
  "title": "New title",
  "description": "Updated description",
  "status": "ARCHIVED"
}
```

Response: `200 OK` with `ReadingListDetailResponseDTO`.

Errors:
- `400` — title provided but blank.
- `403` — caller is not the owner.
- `404` — list not found.

> **Note:** Editing the `books` array of an existing list is **NOT supported** in this version. Slots are fixed at creation time. If the user wants to change the book lineup, they must delete and recreate.

### 3.7 Delete list — `DELETE /{id}` (owner only)

Cascades: removes all slots, subscriptions, and stars.

Response: `204 No Content`.

Errors:
- `403` — caller is not the owner.
- `404` — list not found.

### 3.8 Subscribe — `POST /{id}/subscribe`

Subscribes the caller to the list. No-op if already subscribed. Only works for `PUBLISHED` lists.

Response: `204 No Content`.

Errors:
- `400` — list is not `PUBLISHED` (e.g., `DRAFT` or `ARCHIVED`).
- `404` — list not found.

### 3.9 Unsubscribe — `DELETE /{id}/subscribe`

No-op if not subscribed. Already-awarded XP is **not** clawed back.

Response: `204 No Content`.

### 3.10 Star — `POST /{id}/star`

Upvotes the list. No-op if already starred by this user. Increments `starCount` atomically.

Response: `204 No Content`.

### 3.11 Unstar — `DELETE /{id}/star`

No-op if not starred.

Response: `204 No Content`.

---

## 4. DTO shapes

### 4.1 `ReadingListSummaryResponseDTO` (browse + mine)

```json
{
  "id": 12,
  "title": "Sci-Fi Foundations",
  "description": "A curated journey through the genre's backbone.",
  "ownerNickname": "erik",
  "ownerUserId": 7,
  "status": "PUBLISHED",
  "starCount": 42,
  "bookCount": 3,
  "callerIsOwner": false,
  "hasStarred": true,
  "isSubscribed": false,
  "createdAt": "2026-04-23 08:51:28",
  "updatedAt": "2026-04-23 08:51:28"
}
```

- `ownerNickname` may be `null` if the owner hasn't set one. Fall back to `ownerUserId` or a generic "User #N".

### 4.2 `ReadingListSlotResponseDTO`

```json
{
  "catalogBookId": 55,
  "position": 1,
  "title": "Dune",
  "author": "Frank Herbert",
  "note": "Start here — sets the tone.",
  "inMyLibrary": true,
  "completedByMe": false
}
```

- `inMyLibrary` and `completedByMe` are **`null` when the caller is not subscribed** to the list. When subscribed, they are always boolean.
- Use `inMyLibrary: false` to render an "Add to my library" call-to-action that POSTs to `/lexipal/api/v1/books` (the existing flow) with this slot's title + author + a user-provided totalPages.

### 4.3 `ReadingListDetailResponseDTO` (create + getList + update)

Everything from `ReadingListSummaryResponseDTO` except `bookCount`, plus an ordered `slots` array:

```json
{
  "id": 12,
  "title": "Sci-Fi Foundations",
  "description": "A curated journey through the genre's backbone.",
  "ownerNickname": "erik",
  "ownerUserId": 7,
  "status": "PUBLISHED",
  "starCount": 42,
  "callerIsOwner": false,
  "hasStarred": true,
  "isSubscribed": true,
  "createdAt": "2026-04-23 08:51:28",
  "updatedAt": "2026-04-23 08:51:28",
  "slots": [
    { "catalogBookId": 55, "position": 1, "title": "Dune",        "author": "Frank Herbert", "note": "…", "inMyLibrary": true,  "completedByMe": true  },
    { "catalogBookId": 56, "position": 2, "title": "1984",        "author": "George Orwell", "note": null, "inMyLibrary": true,  "completedByMe": false },
    { "catalogBookId": 57, "position": 3, "title": "Neuromancer", "author": "William Gibson","note": "…", "inMyLibrary": false, "completedByMe": false }
  ]
}
```

`slots` is already sorted by `position` ascending.

### 4.4 `ReadingListSubscriptionResponseDTO` (my subscriptions)

```json
{
  "listId": 12,
  "listTitle": "Sci-Fi Foundations",
  "ownerNickname": "erik",
  "subscribedAt": "2026-04-15 14:02:10",
  "completedAt": null,
  "slotsTotal": 3,
  "slotsCompleted": 1
}
```

- `completedAt` is non-null once the user has finished every book in the list (at which point they also receive the +200 XP list-completion bonus).

### 4.5 Request DTOs

**`ReadingListBookSlotRequestDTO`**:
```ts
{ title: string; author: string; note?: string; position: number }
```

**`ReadingListCreateRequestDTO`**:
```ts
{
  title: string;
  description?: string;
  status?: "DRAFT" | "PUBLISHED" | "ARCHIVED";  // defaults to PUBLISHED
  books: ReadingListBookSlotRequestDTO[];        // non-empty
}
```

**`ReadingListUpdateRequestDTO`** (all fields optional):
```ts
{
  title?: string;
  description?: string;
  status?: "DRAFT" | "PUBLISHED" | "ARCHIVED";
}
```

---

## 5. XP mechanics (informational — no new calls to make)

The frontend does **not** directly trigger reading-list XP. It happens server-side whenever a personal `Book` transitions to `COMPLETE`:

1. **+25 XP** for completing the session (existing behavior, unchanged).
2. **+50 XP** bonus once per catalog book, if the user is actively subscribed (i.e. `completedAt == null`) to at least one list containing that book. Awarded only once per (user, catalogBook) pair — subscribing to multiple lists containing the same book does NOT multiply the bonus.
3. **+200 XP** bonus per subscription, fired when the last remaining slot in a subscribed list becomes `completedByMe`. Simultaneously sets that subscription's `completedAt`.

Both bonuses are idempotent — re-running the completion sync will not re-award them.

**What the UI should do** after a session completes (same re-fetch cadence as today):
- Refetch `GET /profile/me` (or wherever you display totalXp) to show the new XP.
- If the user is on a list detail page or their subscriptions screen, refetch that too so the slot checkmarks and `slotsCompleted` update.
- Optionally, show a celebratory toast when `completedAt` becomes non-null on a subscription.

---

## 6. Error shapes

GlobalExceptionHandler returns the project's standard shape:

- **Validation failure (`400`)**:
  ```json
  { "message": "Validation failed", "errors": { "title": "Title is required", "books": "A reading list must contain at least one book" } }
  ```
- **Other 400** (e.g. duplicate slot position, subscribing to a non-published list):
  ```json
  { "message": "Duplicate position: 2", "status": 400 }
  ```
- **403 Forbidden** (non-owner tries to PATCH/DELETE):
  ```json
  { "message": "Only the list owner can perform this action", "status": 403 }
  ```
- **404 Not Found**:
  ```json
  { "message": "ReadingList with id 999 was not found", "status": 404 }
  ```

---

## 7. Required UI screens

1. **Browse / Explore page** (`GET /`)
   - Search input (bound to `q`, debounced).
   - Min-stars filter (slider or numeric input, bound to `minStars`).
   - Paginated grid/list of `ReadingListSummaryResponseDTO` cards: title, description snippet, ownerNickname, starCount, bookCount, star/subscribe buttons, "subscribed" badge if `isSubscribed`.
   - Clicking a card → detail page.

2. **List detail page** (`GET /{id}`)
   - Header: title, description, owner, starCount, star toggle, subscribe toggle, `callerIsOwner` → show edit/delete.
   - Ordered slot list rendered as a roadmap/timeline. For each slot:
     - Title + author + note.
     - If `inMyLibrary == false` (subscriber only): "Add to my library" button → prompt for totalPages, POST to `/books`.
     - If `inMyLibrary == true` and `completedByMe == false`: "In progress" badge, link to the personal book page.
     - If `completedByMe == true`: green checkmark.
   - When not subscribed, show a subscribe CTA and hide slot-status badges (they'll be null).

3. **Authoring page / modal** (`POST /`, `PATCH /{id}`)
   - Title, description, status.
   - Dynamic slot editor: add/remove rows, each with title + author + note. Auto-assign `position` starting at 1, allow drag-to-reorder. On submit, send `position` as the current index + 1.
   - Client-side guardrails: dedupe warning if two slots have the same normalized title+author.
   - On edit (PATCH), slots are NOT editable — show a note to the user. Only title/description/status.

4. **My lists** (`GET /mine`)
   - Lists the user owns. Show drafts with a badge. Edit / delete / change-status actions.

5. **My subscriptions** (`GET /subscriptions`)
   - Card per subscription with `slotsCompleted / slotsTotal` progress bar and `completedAt` trophy badge when finished. Click → list detail.

6. **Onboarding nudge** — if `GET /profile/me` indicates `needsOnboarding == true`, block the reading-list UI behind the nickname flow (same rule as BookCircle).

---

## 8. Known gotchas

- **The `slots` array of an existing list is immutable** via PATCH. If you want to let authors reorder or swap books, that's a future iteration.
- **Normalization** happens backend-side; the frontend should just send the raw strings. Don't pre-lowercase or trim aggressively — it's harmless but confusing for display.
- **Star count is cached on the list row**; the backend updates it atomically on star/unstar. It reflects the truth immediately, no need to refetch separately.
- **Pagination** returns just the array, not total count. To know if there's a next page, check whether `response.length === size`.
- **Bonus XP is retroactive-ish**: if the user finishes book X *before* subscribing to a list containing X, then subscribes, the +50 bonus for that book does NOT fire retroactively. It only fires on the `ACTIVE → COMPLETE` transition while subscribed. Document this in the UI if relevant, or live with it — the PM call.
- **Unsubscribing does not revoke XP** already awarded for that subscription. This is intentional.
