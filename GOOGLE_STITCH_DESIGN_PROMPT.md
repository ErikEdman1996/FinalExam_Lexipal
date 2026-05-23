# Google Stitch Design Prompt - Lexipal Website

Design a polished responsive web app UI for **Lexipal**, a reading and vocabulary platform where users track books, read community stories, save notes, join book circles, and build reading lists.

The design should feel calm, literary, focused, and modern. This is an authenticated app experience, not a marketing landing page. The first screen should be the actual library/dashboard experience.

## Product Context

Lexipal helps readers:

- Track books in their personal library.
- Follow user-created multi-chapter stories.
- See both books and subscribed stories in one unified library.
- Continue reading from current progress.
- Browse story discovery, reading lists, and book circles.
- Track reading sessions, notes, vocabulary, XP, and achievements.

## Core Design Goal

Create a clean app shell and main library experience that makes books and stories feel like part of the same reading ecosystem while still visually distinguishing them.

The main page should answer:

- What am I currently reading?
- Which stories am I subscribed to?
- What should I continue next?
- What reading progress have I made?

## Required Screens

### 1. Unified Library

Main route: `/library`

Design a library page showing a mixed list/grid of:

- `BOOK` items.
- `STORY` items.

Each library item should show:

- Title.
- Author or story creator.
- Type badge: Book or Story.
- Progress:
  - Books use pages, for example `120 / 340 pages`.
  - Stories use chapters, for example `3 / 8 chapters`.
- Progress bar.
- Status badge.
- Cover image area for books.
- Elegant placeholder/typographic cover treatment for stories.
- Adult `18+` badge only when relevant.
- Last added or latest activity date.

Book cards should feel grounded and cover-led. Story cards can feel more editorial, using title typography, chapter count, and subtle metadata.

Primary actions:

- Continue.
- Open details.

Filters:

- All.
- Books.
- Stories.
- In progress.
- Completed.

Sort options:

- Recently added.
- Progress.
- Title.

### 2. Dashboard Sidebar / Navigation

Include a persistent app navigation for desktop and compact bottom/tab navigation for mobile.

Navigation items:

- Library.
- Stories.
- Reading Lists.
- Book Circles.
- Notes.
- Profile.

Use clear icons with labels. Keep the shell practical and easy to scan.

### 3. Story Discovery Preview

Design a stories browse area or section that shows published stories with:

- Title.
- Summary preview.
- Author display name.
- Genre and tag chips.
- Like count.
- View count.
- Chapter count.
- Subscribe action.
- `18+` badge when needed.

Do not expose anonymous story owners. Anonymous stories should display exactly as `Anonymous`.

### 4. Book Detail / Reading Progress

Design a book detail view with:

- Cover.
- Title and author.
- Page progress.
- Reading sessions.
- Notes.
- Start or continue session action.

This should remain book-specific. Do not make story chapters look like book pages.

### 5. Story Detail / Reader

Design a story detail view with:

- Title.
- Author display name.
- Summary.
- Genres and tags.
- Subscribe/unsubscribe action.
- Like action.
- Ordered chapter list.
- Chapter progress for subscribed users.

Design a comfortable chapter reader:

- Readable max line width.
- Strong typography.
- Back to story link.
- Previous/next chapter controls.
- Minimal distractions.

## Visual Style

- Calm, premium, reading-focused, and useful.
- Avoid a generic SaaS landing-page look.
- Avoid oversized hero sections.
- Avoid decorative gradient blobs or abstract background ornaments.
- Use warm neutral surfaces with a few tasteful accent colors, but do not make the UI monochrome.
- Cards should have subtle borders, soft shadows if needed, and border radius no larger than 8px.
- The layout should feel dense enough for real daily use but still breathable.
- Use accessible contrast.
- Use real app content, not lorem ipsum.

## Components To Include

- App shell with sidebar/topbar.
- Search input.
- Segmented filter controls.
- Sort menu.
- Book card.
- Story card.
- Progress bar.
- Type badges.
- Empty state.
- Loading skeleton.
- Detail page header.
- Chapter list.
- Reader layout.

## Example Library Data

Use examples like:

- Book: `The Hobbit`, creator `J.R.R. Tolkien`, progress `146 / 310 pages`.
- Book: `Dune`, creator `Frank Herbert`, progress `688 / 688 pages`, status `Complete`.
- Story: `The Clockwork Library`, creator `erik`, progress `2 / 5 chapters`.
- Story: `Masked Tale`, creator `Anonymous`, progress `1 / 3 chapters`.
- Story: `Midnight Door`, creator `Anonymous`, progress `0 / 4 chapters`, `18+`.

## Responsive Requirements

Desktop:

- Sidebar navigation.
- Library grid or split list with detail preview.
- Filters and search in a compact toolbar.

Mobile:

- Bottom navigation or compact top navigation.
- Single-column library list.
- Filters collapse into a sheet or horizontal segmented control.
- Cards must remain readable and tappable.

## Output Expectations

Create a high-fidelity app design for the Lexipal authenticated experience. Prioritize the unified library page, then include supporting story and reader screens. The design should be ready for a frontend developer to translate into React components.
