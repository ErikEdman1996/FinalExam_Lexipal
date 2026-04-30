# Frontend Implementation Brief - Unified Library Items

## What changed

The library screen should fetch from the new unified library endpoint instead of using only `/books/all`.

Use:

```http
GET /lexipal/api/v1/library/items
Authorization: Bearer <token>
```

This endpoint returns both:

- Personal books the user has added.
- Stories the user has subscribed to.

Do not create book rows for stories. A story is in the library when the user is subscribed to it.

## Response shape

```ts
type LibraryItemType = "BOOK" | "STORY";
type LibraryProgressUnit = "PAGE" | "CHAPTER";

interface LibraryItemResponseDTO {
  itemType: LibraryItemType;
  itemId: number;
  title: string;
  creatorName: string | null;
  completedUnits: number;
  totalUnits: number;
  progressPercentage: number;
  progressUnit: LibraryProgressUnit;
  status: string;
  isAdult: boolean;
  coverImageUrl: string | null;
  addedAt: string;
}
```

The array is already sorted by `addedAt` descending.

## How to render items

For `BOOK` items:

- `itemId` is the existing book id.
- `creatorName` is the book author.
- `completedUnits` is pages read.
- `totalUnits` is total pages.
- `progressUnit` is `PAGE`.
- `coverImageUrl` may contain the resolved cover URL.
- Open the existing book detail/reading route using `itemId`.

For `STORY` items:

- `itemId` is the story id.
- `creatorName` is the story author display name.
- Anonymous stories return `creatorName: "Anonymous"`.
- `completedUnits` is distinct published chapters the user has opened.
- `totalUnits` is published chapter count.
- `progressUnit` is `CHAPTER`.
- `coverImageUrl` is currently `null`.
- Open the story detail route using `itemId`.

## Story behavior

- A story appears in the library after `POST /stories/{id}/subscribe`.
- A story disappears after `DELETE /stories/{id}/subscribe`.
- Adult story subscriptions are hidden while the user has adult content disabled.
- When adult content is enabled, subscribed adult stories can appear with `isAdult: true`.
- Opening story chapters can update `completedUnits` and `progressPercentage`, so refetch the library after chapter reads if the library view is active or cached.

## Important compatibility note

Keep `/books/all` for old book-specific flows if needed, but the main library screen should use `/library/items`.

Book creation, book sessions, story subscriptions, and story reading APIs are unchanged.

## Acceptance criteria

- Library shows existing books as `BOOK` items.
- Subscribed stories show as `STORY` items.
- Unsubscribed stories no longer show.
- Book progress displays as pages.
- Story progress displays as chapters.
- Anonymous story author names are not revealed.
- Adult story subscriptions follow the profile adult-content setting.
- Clicking a book opens the existing book flow.
- Clicking a story opens the story detail/reader flow.
