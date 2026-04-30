# Frontend Implementation Brief - User-Created Stories

> Hand this to a frontend agent. It is self-contained: product intent, user flows, API contracts, DTO shapes, validation rules, mature-content behavior, Markdown handling, view-count semantics, UI expectations, and acceptance scenarios.

---

## 1. What You Are Building

Build the frontend for **Lexipal Stories**, a feature where authenticated users can write, draft, publish, browse, read, like, and subscribe to user-created multi-chapter stories.

The feature should feel like a polished in-app creative writing and reading area, not a marketing page. The first screen should be the actual stories experience: browsing, searching, filtering, and reading. Authors should have a clear workspace for managing drafts and chapters.

Users can:

- Browse published stories.
- Search stories by title, summary, genre, tag, and visible author nickname.
- Filter by genre and tag.
- Sort by newly published, most viewed, most liked, latest chapter, or title.
- Open a story detail page and read published chapters.
- Like or unlike a story.
- Subscribe or unsubscribe to a story.
- View subscribed stories sorted by latest published chapter.
- Create their own story drafts.
- Add chapters to stories.
- Publish chapters.
- Publish stories when they meet backend rules.
- Choose whether each story is attributed to their nickname or shown anonymously.
- Mark a story as 18+.
- Enable adult-content access in their profile settings.

### Core Product Rules

1. **Stories are made of chapters.**
   A story is metadata plus an ordered list of chapters. Chapter bodies are fetched separately when opening a chapter.

2. **Stories and chapters both have statuses.**
   Stories use `DRAFT | PUBLISHED | ARCHIVED`.
   Chapters use `DRAFT | PUBLISHED | ARCHIVED`.

3. **Readers only see published content.**
   Non-owners can only access `PUBLISHED` stories and only see `PUBLISHED` chapters.

4. **Owners see their own drafts.**
   Authors can see all of their story statuses and chapter statuses through `/stories/mine` and story detail.

5. **Publishing a story requires content.**
   The backend rejects publishing a story unless it has at least one genre and at least one published chapter.

6. **Published stories cannot be left empty.**
   If an edit would leave a published story with no published chapters, the backend rejects it.

7. **Anonymous means anonymous to readers.**
   If a story uses `ANONYMOUS` attribution, readers see `authorDisplayName: "Anonymous"` and `ownerUserId: null` or omitted. The frontend must not try to recover or display the real owner.

8. **Anonymous owner nickname does not match search.**
   Search results do not match the owner's nickname for anonymous stories. Do not imply that anonymous stories can be found by author name.

9. **Adult stories are protected.**
   Adult stories are hidden from browse/search unless both:
   - request has `includeAdult=true`
   - caller has enabled adult content in profile settings

   Direct access to an adult story/chapter returns `403` with message `MATURE_CONTENT_REQUIRED` unless the caller is the owner or has adult content enabled.

10. **Markdown is stored, not rendered by the backend.**
    The backend returns `bodyMarkdown` as plain Markdown text. The frontend may render Markdown, but any rendered HTML must be sanitized by the rendering layer. Do not use unsafe raw HTML rendering.

11. **Opening a chapter can count as a view.**
    `GET /stories/{id}/chapters/{chapterId}` records a view only when a non-owner reader opens a published chapter of a published story. Author opens, draft chapter opens, archived chapter opens, and blocked adult-content opens do not count.

---

## 2. Auth And Base URL

All story endpoints require the existing Keycloak JWT:

```http
Authorization: Bearer <token>
```

Base API prefix:

```text
/lexipal/api/v1
```

Story endpoint prefix:

```text
/lexipal/api/v1/stories
```

Dev host is normally:

```text
http://<host>:8585/lexipal/api/v1
```

Unauthenticated requests return:

```http
401 Unauthorized
```

Reuse the existing frontend auth/token mechanism.

---

## 3. Required Frontend Screens

### 3.1 Stories Browse

Route suggestion:

```text
/stories
```

Purpose:

- Main discovery surface.
- Shows published stories the current user is allowed to see.
- Supports search, filters, sort, pagination/infinite load, likes, subscriptions, and adult-content gating.

Required controls:

- Search input for `q`.
- Genre filter input or combobox for `genre`.
- Tag filter input or combobox for `tag`.
- Sort segmented control or select:
  - `NEWLY_PUBLISHED`
  - `MOST_VIEWED`
  - `MOST_LIKED`
  - `LATEST_CHAPTER`
  - `TITLE_ASC`
- Adult-content toggle:
  - Disabled or hidden if the user's profile has `adultContentEnabled=false`.
  - When enabled in UI, send `includeAdult=true`.
  - Explain in settings, not as verbose text on every card.
- Page/load-more behavior.

Card/list item should show:

- Title.
- Summary preview.
- Author display name.
- Genres.
- Tags.
- 18+ badge if `isAdult=true`.
- Like count.
- Total views.
- Unique views if space allows.
- Chapter count.
- Published date.
- Latest chapter date.
- Current user flags:
  - `hasLiked`
  - `isSubscribed`
  - `callerIsOwner`

Card actions:

- Open story.
- Like/unlike, unless `callerIsOwner=true`.
- Subscribe/unsubscribe, unless `callerIsOwner=true`.
- If `callerIsOwner=true`, show manage/edit entry instead of reader actions.

Empty states:

- No results: "No stories found."
- Adult filter unavailable: prompt user to enable adult content in profile settings.
- Adult filter enabled but no adult stories: normal empty state.

Important:

- Browse returns an array only. It does **not** return pagination metadata.
- Use `page` and `size`. If the returned array length is less than `size`, assume no more results.

### 3.2 Story Detail

Route suggestion:

```text
/stories/:storyId
```

Purpose:

- Shows story metadata and chapter list.
- Readers see published chapters only.
- Owners see all chapters and management actions.

Show:

- Title.
- Summary.
- Author display name.
- Anonymous indicator only if helpful, not the hidden owner identity.
- Genres and tags.
- 18+ badge.
- Like count, total views, unique views.
- Published date and latest chapter date.
- Chapter list ordered by `position`.
- Like/unlike button for non-owner published stories.
- Subscribe/unsubscribe button for non-owner published stories.
- Edit/manage buttons for owner.

Chapter list items:

- Position.
- Chapter title.
- Status badge for owners.
- Published date if present.
- Open/read action for readable chapters.

Reader behavior:

- If a chapter is returned in the chapter list for a non-owner, it should be readable.
- Opening a chapter calls `GET /stories/{storyId}/chapters/{chapterId}` and may increment backend view counts.

Owner behavior:

- Owner detail includes draft and archived chapters.
- Owner can edit story metadata.
- Owner can create chapters.
- Owner can edit chapter status.

Mature-content behavior:

- If story detail returns `403` with message `MATURE_CONTENT_REQUIRED`, show a mature-content blocked state.
- Provide an action to go to profile settings and enable adult content.
- Do not count the blocked request as a view.

### 3.3 Chapter Reader

Route suggestion:

```text
/stories/:storyId/chapters/:chapterId
```

Purpose:

- Reads a single chapter body.

Fetch:

```http
GET /lexipal/api/v1/stories/{storyId}/chapters/{chapterId}
```

Show:

- Story context link back to story detail.
- Chapter position and title.
- Markdown-rendered body.
- Previous/next chapter navigation if the story detail chapter list is available in client state.
- Owner status badge if owner is viewing their own draft/archived chapter.

Markdown rules:

- Source field is `bodyMarkdown`.
- Render Markdown only through a safe renderer.
- Any generated HTML must be sanitized.
- Do not enable raw HTML unless it is sanitized.
- Preserve paragraph spacing and readable line length.

View-count rule:

- The act of fetching this endpoint can increment views for reader opens.
- Do not make extra duplicate calls on mount if avoidable. React Strict Mode/dev duplicate fetches can cause extra views in local development. Cache/dedupe requests per route load where possible.

### 3.4 My Stories

Route suggestion:

```text
/stories/mine
```

Purpose:

- Author dashboard for all stories owned by the caller.
- Includes draft, published, and archived stories.

Fetch:

```http
GET /lexipal/api/v1/stories/mine
```

Show:

- Story title.
- Status badge.
- Attribution badge: nickname or anonymous.
- 18+ badge.
- Genre/tag summary.
- Chapter count.
- Like count.
- View counts.
- Published date.
- Latest chapter date.
- Edit/manage action.

Suggested grouping:

- Drafts.
- Published.
- Archived.

Required actions:

- Create new story.
- Manage existing story.
- Open published story as reader preview.

### 3.5 Story Editor

Route suggestions:

```text
/stories/new
/stories/:storyId/edit
```

Purpose:

- Create and edit story metadata.

Fields:

- Title, required, max 180 chars.
- Summary, optional, max 2000 chars.
- Genres, optional while drafting, but at least one is required before publishing.
- Tags, optional.
- Attribution:
  - `NICKNAME`
  - `ANONYMOUS`
- Adult story toggle, maps to `isAdult`.
- Status for existing stories:
  - `DRAFT`
  - `PUBLISHED`
  - `ARCHIVED`

Create behavior:

- `POST /stories` always creates a draft story in backend.
- The create request can send genres, tags, adult flag, and attribution.
- Do not expect story creation to publish immediately.

Update behavior:

- `PATCH /stories/{id}` applies only provided fields.
- To publish, send `{ "status": "PUBLISHED" }`.
- If publishing fails, surface backend validation:
  - "Published stories must have at least one genre"
  - "Published stories must have at least one published chapter"

Important UI flow:

- Encourage authors to create story metadata first.
- Then create at least one published chapter.
- Then publish the story.

Recommended publish CTA behavior:

- If no genres in current state, disable publish and show inline validation.
- If no published chapters in current state, disable publish and show inline validation.
- Still handle backend `400` because another tab or stale data can change state.

### 3.6 Chapter Editor

Route suggestions:

```text
/stories/:storyId/chapters/new
/stories/:storyId/chapters/:chapterId/edit
```

Fields:

- Position, required integer, min 1.
- Title, required, max 180 chars.
- Body Markdown, required, max 100000 chars.
- Status:
  - `DRAFT`
  - `PUBLISHED`
  - `ARCHIVED`

Create behavior:

- `POST /stories/{storyId}/chapters`
- Default status should be `DRAFT` unless user explicitly chooses "publish chapter now".
- If `status=PUBLISHED`, backend sets `publishedAt`.

Update behavior:

- `PATCH /stories/{storyId}/chapters/{chapterId}`
- All fields optional.
- Blank title is rejected.
- Blank body is rejected.
- Duplicate position is rejected.
- If the story is currently published, changing the only published chapter to draft/archive is rejected.

Editor UX:

- Use a split or tabbed Markdown editor:
  - Write tab.
  - Preview tab.
- Show word/character count.
- Autosave is not supported by backend as a special endpoint; if implemented, it should use normal PATCH carefully.
- Avoid firing PATCH on every keystroke unless throttled/debounced and the UX clearly communicates save state.
- Provide explicit Save Draft and Publish Chapter actions.

### 3.7 Subscriptions Feed

Route suggestion:

```text
/stories/subscriptions
```

Purpose:

- Lets readers see stories they follow, sorted by latest published chapter.

Fetch:

```http
GET /lexipal/api/v1/stories/subscriptions
```

Show:

- Story title.
- Author display name.
- 18+ badge.
- Subscribed date.
- Latest chapter published date.
- Open story action.

Behavior:

- Feed only returns published stories visible to the caller.
- Adult stories appear only if the caller has adult content enabled.
- There are no unread notifications in v1.
- There are no realtime updates in v1. Refetch on page open, pull-to-refresh, or after subscribing/unsubscribing.

### 3.8 Profile Adult Content Setting

Existing profile route can include this setting, or add it to settings.

Fetch profile:

```http
GET /lexipal/api/v1/profile/me
```

Update adult preference:

```http
PATCH /lexipal/api/v1/profile/adult-content
Content-Type: application/json

{
  "adultContentEnabled": true
}
```

Response is the updated profile, including:

```json
{
  "adultContentEnabled": true
}
```

UX:

- Default is false.
- Make this an intentional setting, not an accidental filter click.
- When disabled, do not send `includeAdult=true` from browse.
- If user hits a direct adult story link and receives `MATURE_CONTENT_REQUIRED`, show a blocked state with a route to this setting.

---

## 4. API Endpoints

All paths below are relative to:

```text
/lexipal/api/v1/stories
```

### 4.1 Create Story

```http
POST /
```

Request body:

```json
{
  "title": "The Clockwork Library",
  "summary": "A reader finds a library that rewrites itself every midnight.",
  "genres": ["Fantasy", "Mystery"],
  "tags": ["Magic", "Libraries"],
  "isAdult": false,
  "attribution": "NICKNAME"
}
```

Validation:

- `title`: required, non-blank, max 180 chars.
- `summary`: optional, max 2000 chars.
- `genres`: optional on create. Max 10 values. Each max 50 chars. No duplicates after normalization.
- `tags`: optional. Max 20 values. Each max 50 chars. No duplicates after normalization.
- `isAdult`: optional boolean. Defaults false.
- `attribution`: optional. Defaults `NICKNAME`.
- Accepted attribution values: `NICKNAME | ANONYMOUS`.

Response:

```http
201 Created
```

Body: `StoryDetailResponseDTO`.

Important:

- Backend creates stories as `DRAFT`.
- Creating a story does not create chapters.

### 4.2 Browse Stories

```http
GET /
```

Query params:

| Param | Type | Required | Behavior |
| --- | --- | --- | --- |
| `q` | string | no | Search title, summary, genres, tags, and visible author nickname. |
| `genre` | string | no | Exact normalized genre filter. |
| `tag` | string | no | Exact normalized tag filter. |
| `includeAdult` | boolean | no | Include adult stories only if caller has adult content enabled. |
| `page` | integer | no | 0-based page index. Default 0. |
| `size` | integer | no | Page size. Default 20, max 100. |
| `sort` | string | no | Sort mode. Default `NEWLY_PUBLISHED`. |

Sort values:

```ts
type StorySortOption =
  | "NEWLY_PUBLISHED"
  | "MOST_VIEWED"
  | "MOST_LIKED"
  | "LATEST_CHAPTER"
  | "TITLE_ASC";
```

Response:

```http
200 OK
```

Body:

```ts
StorySummaryResponseDTO[]
```

Notes:

- Only `PUBLISHED` stories are returned.
- Adult stories are excluded unless allowed.
- Anonymous stories do not match owner nickname search.
- Response is page content only, not a page object.

### 4.3 My Stories

```http
GET /mine
```

Response:

```ts
StorySummaryResponseDTO[]
```

Behavior:

- Returns caller-owned stories across all statuses.
- Ordered by creation date descending.
- Owner can see anonymous story owner fields because they are the owner.

### 4.4 My Story Subscriptions

```http
GET /subscriptions
```

Response:

```ts
StorySubscriptionResponseDTO[]
```

Behavior:

- Returns subscribed published stories visible to the caller.
- Sorted by latest published chapter first.
- Adult subscribed stories are hidden if adult content is disabled.

### 4.5 Get Story Detail

```http
GET /{id}
```

Response:

```ts
StoryDetailResponseDTO
```

Reader behavior:

- Non-owner can only get `PUBLISHED` stories.
- Non-owner receives only published chapters.
- Adult story direct access returns `403 MATURE_CONTENT_REQUIRED` if adult content is disabled.

Owner behavior:

- Owner can get their own draft, published, and archived stories.
- Owner receives all chapters.
- Owner can access their own adult stories even if adult content preference is disabled.

### 4.6 Update Story

```http
PATCH /{id}
```

Request body:

All fields optional.

```json
{
  "title": "The Clockwork Library: Revised",
  "summary": "A sharper summary.",
  "status": "PUBLISHED",
  "attribution": "ANONYMOUS",
  "isAdult": false,
  "genres": ["Fantasy", "Mystery"],
  "tags": ["Magic", "Libraries", "Slow Burn"]
}
```

Accepted statuses:

```ts
type StoryStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";
```

Rules:

- Owner only.
- `title` if provided must be non-blank and max 180 chars.
- `summary` if provided max 2000 chars.
- Sending `genres` replaces all genres.
- Sending `tags` replaces all tags.
- Publishing requires at least one genre.
- Publishing requires at least one published chapter.

Response:

```ts
StoryDetailResponseDTO
```

### 4.7 Create Chapter

```http
POST /{id}/chapters
```

Request body:

```json
{
  "position": 1,
  "title": "Chapter One: The Locked Shelf",
  "bodyMarkdown": "The shelf was not there yesterday.\n\nThat was the first problem.",
  "status": "DRAFT"
}
```

Validation:

- `position`: required integer, min 1.
- `title`: required, non-blank, max 180 chars.
- `bodyMarkdown`: required, non-blank, max 100000 chars.
- `status`: optional, defaults `DRAFT`.
- Accepted chapter statuses: `DRAFT | PUBLISHED | ARCHIVED`.
- Duplicate chapter position in the same story is rejected.

Response:

```http
201 Created
```

Body:

```ts
StoryChapterReadResponseDTO
```

### 4.8 Get Chapter

```http
GET /{id}/chapters/{chapterId}
```

Response:

```ts
StoryChapterReadResponseDTO
```

Behavior:

- Non-owner can read only published chapters of published stories.
- Owner can read own draft/archived chapters.
- Non-owner opens of published chapters increment story view counts.
- Direct adult chapter access can return `403 MATURE_CONTENT_REQUIRED`.

### 4.9 Update Chapter

```http
PATCH /{id}/chapters/{chapterId}
```

Request body:

All fields optional.

```json
{
  "position": 2,
  "title": "Chapter Two: The Catalogue",
  "bodyMarkdown": "Updated Markdown body.",
  "status": "PUBLISHED"
}
```

Rules:

- Owner only.
- Blank title is rejected.
- Blank body is rejected.
- Duplicate position is rejected.
- First publish sets `publishedAt`.
- Publishing a chapter updates the story's `latestChapterPublishedAt`.
- If the story is published and this change would leave it with zero published chapters, backend returns `400`.

Response:

```ts
StoryChapterReadResponseDTO
```

### 4.10 Like And Unlike

```http
POST /{id}/like
DELETE /{id}/like
```

Behavior:

- Reader only. Authors cannot like their own story.
- Published stories only.
- Adult story requires adult content enabled.
- Like is idempotent.
- Unlike is idempotent.

Responses:

```http
204 No Content
```

Frontend:

- Optimistic update is okay, but rollback on failure.
- Increment/decrement `likeCount`.
- Update `hasLiked`.

### 4.11 Subscribe And Unsubscribe

```http
POST /{id}/subscribe
DELETE /{id}/subscribe
```

Behavior:

- Reader only. Authors cannot subscribe to their own story.
- Published stories only.
- Adult story requires adult content enabled.
- Subscribe is idempotent.
- Unsubscribe is idempotent.

Responses:

```http
204 No Content
```

Frontend:

- Optimistic update is okay, but rollback on failure.
- Update `isSubscribed`.
- Refetch `/subscriptions` after changes if the feed is visible.

---

## 5. DTO Shapes

Use these TypeScript interfaces in the frontend.

```ts
export type StoryStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";

export type StoryChapterStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";

export type StoryAttribution = "NICKNAME" | "ANONYMOUS";

export type StorySortOption =
  | "NEWLY_PUBLISHED"
  | "MOST_VIEWED"
  | "MOST_LIKED"
  | "LATEST_CHAPTER"
  | "TITLE_ASC";

export interface StoryCreateRequestDTO {
  title: string;
  summary?: string | null;
  genres?: string[] | null;
  tags?: string[] | null;
  isAdult?: boolean | null;
  attribution?: StoryAttribution | null;
}

export interface StoryUpdateRequestDTO {
  title?: string | null;
  summary?: string | null;
  status?: StoryStatus | null;
  attribution?: StoryAttribution | null;
  isAdult?: boolean | null;
  genres?: string[] | null;
  tags?: string[] | null;
}

export interface StoryChapterCreateRequestDTO {
  position: number;
  title: string;
  bodyMarkdown: string;
  status?: StoryChapterStatus | null;
}

export interface StoryChapterUpdateRequestDTO {
  position?: number | null;
  title?: string | null;
  bodyMarkdown?: string | null;
  status?: StoryChapterStatus | null;
}

export interface StorySummaryResponseDTO {
  id: number;
  title: string;
  summary: string | null;
  authorDisplayName: string | null;
  ownerUserId?: number | null;
  status: StoryStatus;
  attribution: StoryAttribution;
  isAdult: boolean;
  genres: string[];
  tags: string[];
  likeCount: number;
  totalViewCount: number;
  uniqueViewCount: number;
  chapterCount: number;
  callerIsOwner: boolean;
  hasLiked: boolean;
  isSubscribed: boolean;
  publishedAt: string | null;
  latestChapterPublishedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface StoryChapterSummaryResponseDTO {
  id: number;
  position: number;
  title: string;
  status: StoryChapterStatus;
  publishedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface StoryDetailResponseDTO extends StorySummaryResponseDTO {
  chapters: StoryChapterSummaryResponseDTO[];
}

export interface StoryChapterReadResponseDTO {
  id: number;
  storyId: number;
  position: number;
  title: string;
  bodyMarkdown: string;
  status: StoryChapterStatus;
  publishedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface StorySubscriptionResponseDTO {
  storyId: number;
  title: string;
  authorDisplayName: string | null;
  isAdult: boolean;
  subscribedAt: string;
  latestChapterPublishedAt: string | null;
}

export interface AdultContentPreferenceRequestDTO {
  adultContentEnabled: boolean;
}
```

Date/time note:

- Treat date fields as backend `LocalDateTime` strings.
- Format them for display client-side.
- Do not use locale-specific string parsing unless your date library supports the backend format safely.

---

## 6. Example Responses

### 6.1 Story Summary

```json
{
  "id": 42,
  "title": "The Clockwork Library",
  "summary": "A reader finds a library that rewrites itself every midnight.",
  "authorDisplayName": "erik",
  "ownerUserId": 7,
  "status": "PUBLISHED",
  "attribution": "NICKNAME",
  "isAdult": false,
  "genres": ["Fantasy", "Mystery"],
  "tags": ["Libraries", "Magic"],
  "likeCount": 12,
  "totalViewCount": 240,
  "uniqueViewCount": 98,
  "chapterCount": 4,
  "callerIsOwner": false,
  "hasLiked": true,
  "isSubscribed": false,
  "publishedAt": "2026-04-29T19:30:00",
  "latestChapterPublishedAt": "2026-04-29T21:15:00",
  "createdAt": "2026-04-28T15:00:00",
  "updatedAt": "2026-04-29T21:15:00"
}
```

### 6.2 Anonymous Story Summary For Reader

```json
{
  "id": 43,
  "title": "Masked Tale",
  "summary": "A mystery told in letters.",
  "authorDisplayName": "Anonymous",
  "ownerUserId": null,
  "status": "PUBLISHED",
  "attribution": "ANONYMOUS",
  "isAdult": false,
  "genres": ["Mystery"],
  "tags": ["Letters"],
  "likeCount": 3,
  "totalViewCount": 15,
  "uniqueViewCount": 9,
  "chapterCount": 1,
  "callerIsOwner": false,
  "hasLiked": false,
  "isSubscribed": true,
  "publishedAt": "2026-04-29T19:30:00",
  "latestChapterPublishedAt": "2026-04-29T19:30:00",
  "createdAt": "2026-04-29T18:00:00",
  "updatedAt": "2026-04-29T19:30:00"
}
```

### 6.3 Story Detail

```json
{
  "id": 42,
  "title": "The Clockwork Library",
  "summary": "A reader finds a library that rewrites itself every midnight.",
  "authorDisplayName": "erik",
  "ownerUserId": 7,
  "status": "PUBLISHED",
  "attribution": "NICKNAME",
  "isAdult": false,
  "genres": ["Fantasy", "Mystery"],
  "tags": ["Libraries", "Magic"],
  "likeCount": 12,
  "totalViewCount": 240,
  "uniqueViewCount": 98,
  "chapterCount": 2,
  "callerIsOwner": false,
  "hasLiked": true,
  "isSubscribed": false,
  "publishedAt": "2026-04-29T19:30:00",
  "latestChapterPublishedAt": "2026-04-29T21:15:00",
  "createdAt": "2026-04-28T15:00:00",
  "updatedAt": "2026-04-29T21:15:00",
  "chapters": [
    {
      "id": 100,
      "position": 1,
      "title": "The Locked Shelf",
      "status": "PUBLISHED",
      "publishedAt": "2026-04-29T19:30:00",
      "createdAt": "2026-04-29T19:00:00",
      "updatedAt": "2026-04-29T19:30:00"
    },
    {
      "id": 101,
      "position": 2,
      "title": "The Catalogue",
      "status": "PUBLISHED",
      "publishedAt": "2026-04-29T21:15:00",
      "createdAt": "2026-04-29T20:50:00",
      "updatedAt": "2026-04-29T21:15:00"
    }
  ]
}
```

### 6.4 Chapter Read

```json
{
  "id": 100,
  "storyId": 42,
  "position": 1,
  "title": "The Locked Shelf",
  "bodyMarkdown": "The shelf was not there yesterday.\n\nThat was the first problem.",
  "status": "PUBLISHED",
  "publishedAt": "2026-04-29T19:30:00",
  "createdAt": "2026-04-29T19:00:00",
  "updatedAt": "2026-04-29T19:30:00"
}
```

### 6.5 Subscription Feed Item

```json
{
  "storyId": 42,
  "title": "The Clockwork Library",
  "authorDisplayName": "erik",
  "isAdult": false,
  "subscribedAt": "2026-04-29T21:00:00",
  "latestChapterPublishedAt": "2026-04-29T21:15:00"
}
```

---

## 7. Error Handling

Global error responses generally look like:

```json
{
  "message": "Validation failed",
  "errors": {
    "title": "Title is required"
  }
}
```

or:

```json
{
  "message": "MATURE_CONTENT_REQUIRED",
  "status": 403
}
```

Common statuses:

| Status | Meaning | Frontend behavior |
| --- | --- | --- |
| `400` | Validation or illegal state | Show inline form errors when field-specific; otherwise show toast/banner. |
| `401` | Missing/expired auth | Send to login or refresh token. |
| `403` | Not owner, mature content blocked, or forbidden action | Show forbidden state or mature-content state. |
| `404` | Story/chapter not found or hidden draft | Show not found state. |
| `409` | Conflict, if future backend uses it | Show conflict message and refetch. |

Specific messages to handle:

- `MATURE_CONTENT_REQUIRED`
  - Show mature-content blocked state.
  - Offer route to profile setting.
- `Published stories must have at least one genre`
  - Highlight genre field.
- `Published stories must have at least one published chapter`
  - Highlight chapters section.
- `Duplicate chapter position: N`
  - Highlight position field.
- `Authors cannot perform this action on their own story`
  - Hide reader actions for owners; still handle this if stale UI.

---

## 8. API Client Helpers

Suggested functions:

```ts
const STORY_BASE = "/lexipal/api/v1/stories";
const PROFILE_BASE = "/lexipal/api/v1/profile";

async function browseStories(params: {
  q?: string;
  genre?: string;
  tag?: string;
  includeAdult?: boolean;
  page?: number;
  size?: number;
  sort?: StorySortOption;
}): Promise<StorySummaryResponseDTO[]> {}

async function getStory(id: number): Promise<StoryDetailResponseDTO> {}

async function createStory(payload: StoryCreateRequestDTO): Promise<StoryDetailResponseDTO> {}

async function updateStory(id: number, payload: StoryUpdateRequestDTO): Promise<StoryDetailResponseDTO> {}

async function getMyStories(): Promise<StorySummaryResponseDTO[]> {}

async function getSubscriptions(): Promise<StorySubscriptionResponseDTO[]> {}

async function createChapter(
  storyId: number,
  payload: StoryChapterCreateRequestDTO
): Promise<StoryChapterReadResponseDTO> {}

async function getChapter(
  storyId: number,
  chapterId: number
): Promise<StoryChapterReadResponseDTO> {}

async function updateChapter(
  storyId: number,
  chapterId: number,
  payload: StoryChapterUpdateRequestDTO
): Promise<StoryChapterReadResponseDTO> {}

async function likeStory(id: number): Promise<void> {}

async function unlikeStory(id: number): Promise<void> {}

async function subscribeStory(id: number): Promise<void> {}

async function unsubscribeStory(id: number): Promise<void> {}

async function setAdultContentEnabled(enabled: boolean): Promise<ProfileReadResponseDTO> {}
```

Query builder notes:

- Do not send blank `q`, `genre`, or `tag`; omit them.
- Do not send `includeAdult=true` unless profile says adult content is enabled.
- Default `page=0`, `size=20`.
- If sort is unset, omit it and let backend default to `NEWLY_PUBLISHED`.

---

## 9. State And Cache Rules

Recommended query keys:

```ts
["stories", "browse", { q, genre, tag, includeAdult, page, size, sort }]
["stories", "detail", storyId]
["stories", "chapter", storyId, chapterId]
["stories", "mine"]
["stories", "subscriptions"]
["profile", "me"]
```

After mutating story metadata:

- Invalidate:
  - `["stories", "detail", storyId]`
  - `["stories", "mine"]`
  - browse queries

After creating/updating chapter:

- Invalidate:
  - story detail
  - my stories
  - subscriptions if chapter was published
  - browse queries if story is published

After like/unlike:

- Optimistically update current story/detail/card.
- Invalidate browse and detail afterward.

After subscribe/unsubscribe:

- Optimistically update `isSubscribed`.
- Invalidate subscriptions and story detail/browse.

After adult preference change:

- Invalidate profile.
- Invalidate browse.
- Invalidate subscriptions.
- If currently on mature-content blocked page, retry access.

View count caching:

- Reading a chapter can change `totalViewCount` and `uniqueViewCount`.
- After a reader opens a chapter, invalidate story detail and browse summaries.
- Avoid duplicate chapter fetches on a single navigation, especially in development.

---

## 10. Validation Rules For Forms

Client-side validation should mirror backend to reduce failed requests.

### Story Form

Title:

- Required.
- Trimmed.
- Max 180 chars.

Summary:

- Optional.
- Max 2000 chars.

Genres:

- Optional while drafting.
- Required to publish.
- Max 10.
- Each trimmed.
- Each max 50 chars.
- No duplicates after case-insensitive, whitespace-normalized comparison.

Tags:

- Optional.
- Max 20.
- Each trimmed.
- Each max 50 chars.
- No duplicates after case-insensitive, whitespace-normalized comparison.

Attribution:

- Required in UI.
- Default `NICKNAME`.

Adult:

- Boolean.
- Default false.

Publishing:

- Require at least one genre.
- Require at least one chapter with `status=PUBLISHED`.
- Backend remains source of truth.

### Chapter Form

Position:

- Required.
- Integer.
- Min 1.
- Should be unique within story.

Title:

- Required.
- Trimmed.
- Max 180 chars.

Body Markdown:

- Required.
- Trimmed.
- Max 100000 chars.

Status:

- Default `DRAFT`.
- If changing only published chapter away from published for a published story, warn user that backend will reject it.

---

## 11. UI Design Guidance

Build this as a reading and writing tool inside Lexipal.

General feel:

- Calm, focused, literary, and practical.
- Do not make a landing page.
- First screen should be browse or stories workspace.
- Use the existing app shell/navigation if present.
- Keep controls discoverable and ergonomic.

Browse:

- Prefer a list or compact card grid optimized for scanning.
- Cards should not be overly decorative.
- Keep story metadata visible: tags, genres, counts, status.
- Use badges for `DRAFT`, `PUBLISHED`, `ARCHIVED`, `18+`, `Anonymous`.
- Use icon buttons with tooltips for like and subscribe when possible.

Reader:

- Prioritize typography and reading comfort.
- Use comfortable max line width.
- Provide back-to-story and next/previous chapter navigation.
- Keep actions out of the way while reading.

Writer:

- Make draft/publish state obvious.
- Make validation visible before publishing.
- Provide Markdown preview.
- Show save/publish actions clearly.
- Do not hide destructive status changes behind ambiguous icons.

Adult content:

- Use a clear `18+` badge.
- For blocked content, show a mature-content access screen.
- Do not display adult story details when blocked.

Anonymous attribution:

- Show `Anonymous` exactly as returned.
- Do not display hidden owner IDs.
- Do not create UI that suggests anonymous stories are searchable by author nickname.

---

## 12. Suggested Component Breakdown

Data/API:

- `storiesApi.ts`
- `storyTypes.ts`
- `useStoriesBrowse`
- `useStoryDetail`
- `useStoryChapter`
- `useMyStories`
- `useStorySubscriptions`
- `useStoryMutations`

Browse:

- `StoriesPage`
- `StorySearchBar`
- `StoryFilterBar`
- `StorySortControl`
- `StoryCard`
- `StoryActionButtons`
- `AdultContentFilterControl`

Detail/reader:

- `StoryDetailPage`
- `StoryHeader`
- `StoryMetaBadges`
- `ChapterList`
- `ChapterReaderPage`
- `MarkdownRenderer`
- `MatureContentBlocked`

Authoring:

- `MyStoriesPage`
- `StoryEditorPage`
- `StoryForm`
- `ChapterEditorPage`
- `ChapterForm`
- `MarkdownEditor`
- `PublishChecklist`

Profile:

- `AdultContentSetting`

---

## 13. Detailed User Flows

### Reader Finds And Follows A Story

1. User opens `/stories`.
2. Frontend fetches `GET /stories?page=0&size=20&sort=NEWLY_PUBLISHED`.
3. User searches for "library".
4. Frontend refetches with `q=library`.
5. User opens a story.
6. Frontend fetches `GET /stories/{id}`.
7. User clicks Like.
8. Frontend calls `POST /stories/{id}/like`.
9. UI updates `hasLiked=true`, increments `likeCount`.
10. User clicks Subscribe.
11. Frontend calls `POST /stories/{id}/subscribe`.
12. UI updates `isSubscribed=true`.
13. User can now see it in `/stories/subscriptions`.

### Reader Opens A Chapter

1. User opens story detail.
2. User clicks published chapter.
3. Frontend calls `GET /stories/{storyId}/chapters/{chapterId}`.
4. Backend records view if the user is a non-owner reader.
5. Frontend renders `bodyMarkdown` safely.
6. Frontend invalidates story detail/browse counts after read.

### Author Creates And Publishes A Story

1. Author opens `/stories/new`.
2. Author enters title, summary, genres/tags, attribution, adult toggle.
3. Frontend calls `POST /stories`.
4. Backend returns draft `StoryDetailResponseDTO`.
5. Frontend routes to story management page.
6. Author creates chapter as draft or published.
7. Frontend calls `POST /stories/{id}/chapters`.
8. Author publishes chapter if needed via `PATCH /stories/{id}/chapters/{chapterId}`.
9. Author clicks Publish Story.
10. Frontend checks:
    - at least one genre
    - at least one published chapter
11. Frontend calls `PATCH /stories/{id}` with `{ "status": "PUBLISHED" }`.
12. Backend returns updated story with `publishedAt`.

### Adult Story Direct Link

1. User opens `/stories/123`.
2. Backend returns `403` with `message=MATURE_CONTENT_REQUIRED`.
3. Frontend shows mature-content blocked screen.
4. User goes to profile settings.
5. Frontend calls `PATCH /profile/adult-content` with `{ "adultContentEnabled": true }`.
6. User retries story link.
7. Backend returns story detail.

---

## 14. Edge Cases To Handle

- Story was visible in browse but returns 404 on detail because it was archived.
- Story detail loaded for owner has draft chapters; same story loaded for reader has fewer chapters.
- `ownerUserId` is null/omitted for anonymous stories.
- `authorDisplayName` can be null for nickname stories if user has no nickname.
- Like/subscribe buttons should not show for owner.
- Like/subscribe can fail if story becomes archived between render and click.
- Adult story can disappear from browse after user disables adult preference.
- Subscription feed can hide adult subscriptions when adult preference is disabled.
- Chapter order can have gaps. Use `position`, do not assume array index plus one.
- Duplicate chapter position returns 400.
- Publishing story can fail due to missing genre or published chapter.
- Publishing/archiving chapter can fail if it would empty a published story.
- Chapter fetch can increment views; do not prefetch every chapter body from a list page.
- There is no delete story endpoint in v1.
- There is no delete chapter endpoint in v1.
- There is no unread notification state in v1.
- There is no realtime event stream for new chapters in v1.

---

## 15. Acceptance Criteria

The frontend implementation is done when:

- `/stories` browses published non-adult stories by default.
- Search, genre filter, tag filter, and all sort modes work.
- Adult stories are hidden unless profile adult content is enabled and `includeAdult=true`.
- Direct adult story access shows mature-content blocked state when disabled.
- Story detail renders metadata, actions, and ordered chapters.
- Chapter reader fetches and safely renders `bodyMarkdown`.
- Like/unlike works idempotently and updates counts/state.
- Subscribe/unsubscribe works idempotently and updates state/feed.
- `/stories/subscriptions` shows followed stories sorted by latest chapter.
- `/stories/mine` shows owned stories across draft/published/archived statuses.
- Story creation works and creates a draft.
- Story editing works, including tags, genres, attribution, adult flag, and status.
- Chapter creation works.
- Chapter editing works.
- Publishing validation is shown client-side and backend errors are handled.
- Anonymous stories show `Anonymous` to readers and do not reveal owner identity.
- Owners can manage anonymous stories and see their own metadata.
- View counts refresh after reading chapters.
- All forms enforce max lengths and required fields.
- All API calls include the bearer token.
- 401, 403, 404, and 400 states have clear UI handling.

---

## 16. Quick Manual Test Script

Use this as a QA checklist.

1. Log in as author A.
2. Create story "Frontend QA Story" with genre "Fantasy" and tag "QA".
3. Confirm it appears in `/stories/mine` as `DRAFT`.
4. Confirm it does not appear in `/stories` for reader B.
5. Add one draft chapter.
6. Try to publish story. It should fail because no chapter is published.
7. Publish the chapter.
8. Publish the story.
9. Log in as reader B.
10. Confirm story appears in browse.
11. Open story detail.
12. Like it.
13. Subscribe to it.
14. Open chapter twice.
15. Confirm story view counts update to at least total 2 and unique 1 after refetch.
16. Log in as author A and confirm author opens do not increase views.
17. Change attribution to `ANONYMOUS`.
18. Log in as reader B and confirm author is "Anonymous".
19. Search for author nickname and confirm anonymous story is not found by nickname.
20. Mark story 18+.
21. Log in as reader B with adult preference disabled and confirm browse hides it.
22. Open direct link and confirm mature-content blocked state.
23. Enable adult content in profile.
24. Confirm adult browse and direct link work.

---

## 17. Implementation Priorities

Recommended order:

1. Add types and API client methods.
2. Add profile adult-content setting support.
3. Build browse page with filters/sort.
4. Build story detail page.
5. Build chapter reader with safe Markdown rendering.
6. Add like/subscribe mutations.
7. Build subscriptions feed.
8. Build my stories dashboard.
9. Build story editor.
10. Build chapter editor.
11. Polish empty/error/loading states.
12. Run manual test script.

