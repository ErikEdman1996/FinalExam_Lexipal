# Frontend Implementation Prompt: Reading List Genres And Expanded Search

You are updating the Lexipal frontend reading-list feature.

The backend now supports list-level genres and broader search through the existing reading-list browse endpoint. Implement the frontend changes needed to let users add/edit genres on reading lists and search lists by list title, genre, contained book title, or contained author.

## Backend Contract

Base URL remains:

```text
/lexipal/api/v1/reading-lists
```

All reading-list endpoints still require the existing authenticated JWT flow.

## Updated DTOs

### ReadingListCreateRequestDTO

`POST /lexipal/api/v1/reading-lists`

```ts
type ReadingListCreateRequestDTO = {
  title: string;
  description?: string | null;
  status?: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  genres?: string[];
  books: ReadingListBookSlotRequestDTO[];
};
```

`genres` is optional. Existing create calls without `genres` still work.

Genre validation:

- Maximum 10 genres.
- Each genre must be non-blank after trimming.
- Each genre must be 50 characters or fewer.
- Duplicate genres are rejected after backend normalization, so `" Epic Fantasy "` and `"epic   fantasy"` count as the same genre.

Example:

```json
{
  "title": "Fantasy Foundations",
  "description": "A curated route through modern fantasy.",
  "status": "PUBLISHED",
  "genres": ["Fantasy", "Epic Fantasy"],
  "books": [
    {
      "title": "A Game of Thrones",
      "author": "George R. R. Martin",
      "note": "Political fantasy with a huge cast.",
      "position": 1
    }
  ]
}
```

### ReadingListUpdateRequestDTO

`PATCH /lexipal/api/v1/reading-lists/{id}`

```ts
type ReadingListUpdateRequestDTO = {
  title?: string;
  description?: string | null;
  status?: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  genres?: string[] | null;
};
```

Important PATCH behavior:

- If `genres` is omitted, the backend leaves the existing genres unchanged.
- If `genres` is provided, it replaces the full genre set.
- Send `genres: []` to remove all genres.
- Do not send `genres` unless the user actually edited genres.

The existing limitation still applies: PATCH updates list metadata only. It does not edit the book slot lineup.

### ReadingListSummaryResponseDTO

Browse and mine responses now include `genres`.

```ts
type ReadingListSummaryResponseDTO = {
  id: number;
  title: string;
  description?: string | null;
  ownerNickname?: string | null;
  ownerUserId: number;
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  genres: string[];
  starCount: number;
  bookCount: number;
  callerIsOwner: boolean;
  hasStarred: boolean;
  isSubscribed: boolean;
  createdAt: string;
  updatedAt: string;
};
```

### ReadingListDetailResponseDTO

Detail/create/update responses now include `genres`.

```ts
type ReadingListDetailResponseDTO = {
  id: number;
  title: string;
  description?: string | null;
  ownerNickname?: string | null;
  ownerUserId: number;
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  genres: string[];
  starCount: number;
  callerIsOwner: boolean;
  hasStarred: boolean;
  isSubscribed: boolean;
  createdAt: string;
  updatedAt: string;
  slots: ReadingListSlotResponseDTO[];
};
```

The backend returns genres sorted by normalized genre value for stable rendering.

## Expanded Search

The existing browse endpoint is unchanged:

```text
GET /lexipal/api/v1/reading-lists?q={query}&minStars={minStars}&page={page}&size={size}&sort={sort}
```

The frontend should keep using one search input bound to `q`.

`q` now searches across:

- Reading-list title.
- Reading-list genres.
- Contained catalog book titles.
- Contained catalog book authors.

Only `PUBLISHED` lists are returned. `sort` is optional and accepts `POPULAR | RECENT | OLDEST | TITLE_ASC`; missing or unknown values default to `POPULAR`. Pagination behavior is unchanged: response body is only the page array, so treat `response.length === size` as "there may be a next page."

Search examples:

- Searching `fantasy` should show lists tagged with `Fantasy` or `Epic Fantasy`.
- Searching `Game of Thrones` should show every published list containing `A Game of Thrones`.
- Searching `George R. R. Martin` should show lists containing books by that author.
- Searching with different casing or extra whitespace should still work because backend search is normalized.

## Required Frontend Changes

### Browse / Explore Page

- Keep the current single search input.
- Update placeholder/copy to imply broad search, for example: `Search lists, genres, books, or authors`.
- Continue debouncing `q` and sending it to `GET /reading-lists`.
- Add a compact sort select/menu and send `sort` with Explore requests.
- Render genre chips on each reading-list card when `genres.length > 0`.
- Keep existing star, subscribe, minStars, pagination, and card click behavior.

### List Detail Page

- Render genre chips near the list title/metadata.
- If `genres` is empty, do not show an empty genre section.
- Keep existing slots, star, subscribe, owner actions, and progress behavior unchanged.

### Create Reading List UI

- Add a genre editor to the create form.
- Recommended UI:
  - Tag/chip input.
  - Add genre by pressing Enter or clicking an add button.
  - Show removable chips for selected genres.
  - Enforce max 10 genres client-side.
  - Trim each genre before adding.
  - Warn/prevent blank genres.
  - Warn/prevent duplicate normalized genres using:

```ts
function normalizeGenre(value: string): string {
  return value.trim().toLowerCase().replace(/\s+/g, " ");
}
```

- Include `genres` in the create payload only if the array is non-empty.

### Edit Reading List UI

- Add the same genre editor to the metadata edit form.
- Initialize it from `ReadingListDetailResponseDTO.genres`.
- If the user edits genres, send the full replacement array in PATCH.
- If the user does not touch genres, omit `genres` from PATCH.
- If the user removes every genre, send `genres: []`.
- Continue to keep book slots read-only in edit mode.

## Error Handling

The backend returns existing project error shapes. Display validation messages in the same style as current reading-list errors.

Common new errors:

- Too many genres.
- Blank genre.
- Genre longer than 50 characters.
- Duplicate normalized genre.

Example backend error shape:

```json
{
  "message": "Duplicate genre: epic fantasy",
  "status": 400
}
```

## Acceptance Criteria

- Users can create a reading list with 0-10 genres.
- Users can edit the genre set for an existing reading list.
- Browse cards and detail pages display genres as chips.
- Searching one input finds lists by title, genre, contained book title, and contained author.
- Searching `Game of Thrones` returns published lists containing `A Game of Thrones`.
- Search still respects min-stars filtering and pagination.
- Existing reading-list create/edit flows without genres continue to work.
- No frontend normalization changes book title or author display; genre normalization is only for client-side duplicate prevention.
