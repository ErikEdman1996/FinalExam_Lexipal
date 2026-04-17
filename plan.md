# Codex Puzzle-Board Implementation Plan

## Goal

Turn the Codex into a curated collectible system instead of an open-ended word log.

- Total collectible pool: `300` words
- Structure: `12` pages with `25` words each
- Player fantasy: a word-hunting puzzle board / egg hunt
- Lookup behavior: dictionary lookup still works for any valid word
- Capture behavior: only words that belong to the curated Codex board can be captured

## Product Direction

The Codex should feel finite, intentional, and satisfying to complete.

- Players should always know what page they are progressing on
- Each page should feel like a mini-collection to finish
- Locked cards should create curiosity without showing an overwhelming wall of thousands of entries
- The board can be expanded later with new volumes instead of endlessly inflating one collection

## Collection Design

Create a curated set of `300` collectible words with a deliberate rarity spread.

Suggested rarity split:

- `150` common
- `90` uncommon
- `45` rare
- `15` mythic

Suggested page design:

- Early pages lean more common and uncommon
- Mid pages introduce more rare words
- Final pages contain the highest concentration of rare and mythic words
- Every page should still contain a mix so each page feels exciting

Example progression:

- Pages `1-3`: mostly common, some uncommon
- Pages `4-8`: balanced common/uncommon with a few rare
- Pages `9-11`: more uncommon and rare, occasional mythic
- Page `12`: showcase page with the hardest mix

## Data Model Changes

Introduce a curated collection definition separate from user captures.

### 1. Add a collection-definition entity

Create a new entity such as `CodexCollectionWord` or `CodexBoardWord`.

Suggested fields:

- `id`
- `lemma`
- `pageNumber`
- `slotNumber`
- `rarityTier`
- `rarityScore`
- `isActive`
- `volume` or `collectionKey` for future expansions

Purpose:

- Defines which words belong to the collectible board
- Defines where they appear in the UI
- Allows future expansions without changing the capture model

### 2. Keep `CodexEntry` as the user capture record

`CodexEntry` should remain the record that a user has captured a collectible word.

It should continue to store:

- captured word
- source session
- xp gained
- created timestamp

Optional improvement:

- Link `CodexEntry` to the collection-definition row as well as `Word`
- This makes page/slot lookup simpler and avoids relying only on lemma matching

## Source of Truth for the 300 Words

Do not use the full `word-frequency.csv` directly as the visible board.

Instead:

- Keep `word-frequency.csv` for rarity scoring and future content selection
- Create a dedicated curated board file for the collectible set

Suggested new resource:

- `src/main/resources/codex-board-volume-1.csv`

Suggested columns:

- `page_number`
- `slot_number`
- `lemma`
- `rarity_tier`
- `rarity_score`

Why this is better:

- The collectible board becomes intentional and stable
- The frontend layout is deterministic
- Expansions are easy to add with new files like `codex-board-volume-2.csv`

## Backend Flow Changes

### 1. Lookup remains universal

When a user looks up a word:

- Continue fetching dictionary data for any valid lemma
- Continue returning definitions and phonetics as today

### 2. Capture becomes conditional

After lookup succeeds:

- Normalize the lemma
- Check whether the lemma exists in the curated Codex board
- If not in the board:
  - return dictionary data
  - do not create `CodexEntry`
  - do not increment captured count
  - do not award capture XP

### 3. Existing captured words still return cleanly

If the lemma is in the board and the user already captured it:

- return the existing capture
- do not duplicate the entry
- do not award XP again

### 4. New collectible capture flow

If the lemma is in the board and not yet captured:

- create `CodexEntry`
- increment session captured count
- award XP based on rarity
- return updated page/progress information

## API Changes

The current Codex API returns only captured entries. That is not enough for locked-card pages.

Add a dedicated board response that returns the full puzzle-board structure.

### 1. Keep current capture endpoint, but expand response

Current behavior:

- search word
- maybe capture word

Recommended response additions:

- `lookupSucceeded`
- `isCollectible`
- `isNewCapture`
- `alreadyCaptured`
- `pageNumber` if collectible
- `slotNumber` if collectible
- `pageProgress`
- `overallProgress`

This allows the frontend to show:

- “Found a word, but it is not part of this volume”
- “You captured slot 8 on page 3”
- “Page 3 is now 14/25 complete”

### 2. Add a board/catalog endpoint

Suggested endpoint:

- `GET /lexipal/api/v1/codex-board`

Suggested response shape:

- `volume`
- `totalWords`
- `capturedWords`
- `completionPercentage`
- `pages`

Each page should include:

- `pageNumber`
- `capturedCount`
- `totalCount`
- `completionPercentage`
- `slots`

Each slot should include:

- `slotNumber`
- `isCaptured`
- `lemma` only if captured, or hidden placeholder if locked
- `rarityTier`
- `capturedAt` if captured
- optional preview metadata if you want partial hints later

## Frontend Experience

Design the Codex as a visible puzzle board instead of a raw list.

### Main Codex screen

Show:

- overall completion percentage
- total captured vs `300`
- list of `12` pages
- page progress bars

### Page screen

Show:

- `25` fixed slots in a clean grid
- captured words as revealed cards
- locked words as silhouette / sealed-card placeholders
- rarity styling after capture, or subtle hidden styling before capture

### Capture feedback

When a collectible word is found:

- animate the slot reveal
- show rarity color/tier
- show XP gain
- update page completion immediately

When a non-collectible word is looked up:

- still show dictionary info
- show a soft message like:
  - `This word is not part of the current Codex board.`

## XP and Reward Design

Keep rarity-based XP because it reinforces the collectible fantasy.

Suggested rewards:

- Common: low XP
- Uncommon: medium XP
- Rare: high XP
- Mythic: very high XP

Optional progression rewards:

- bonus XP for completing a page
- title/badge for finishing a volume
- profile badge for 100% completion

## Migration / Compatibility Considerations

There are existing `CodexEntry` rows that may contain words outside the future curated board.

Decide one of these approaches before implementation:

### Option A. Soft legacy support

- Keep old captures in the database
- Only show curated-board captures in the new Codex board UI
- Optionally expose legacy captures in a separate “Archive” or “Discovered Words” view

### Option B. Hard reset for the Codex feature

- Clear or migrate old captures before launch
- Start all users on the curated Volume 1 board

### Recommended

Use `Option A` during development unless there is a strong reason to reset data.
It is safer and avoids destructive migration decisions too early.

## Implementation Phases

### Phase 1. Collection definition

- Define the 300-word curated list
- Decide page distribution and rarity spread
- Add a new resource file for the board
- Add service logic to load and cache the board

### Phase 2. Backend collectible logic

- Add collection-definition entity/model
- Add repository/service for collectible board lookup
- Update codex capture flow to require board membership
- Expand capture response to include collectible status and progress

### Phase 3. Board API

- Add endpoint to return pages, slots, and progress
- Compute captured state by joining board words with user `CodexEntry` rows
- Return locked-slot friendly payloads

### Phase 4. Frontend Codex board

- Build overview page with 12-page progress display
- Build 25-slot page grid
- Add locked/unlocked card states
- Add completion percentage and capture feedback

### Phase 5. Rewards and polish

- Add page-complete reward
- Add volume-complete reward
- Add animations / visual polish

## Open Decisions

These should be finalized before implementation begins:

- Should the collectible board reveal the rarity of locked words before capture?
- Should locked slots show hints, or be completely hidden?
- Should pages unlock gradually, or should all 12 pages be visible from the start?
- Should repeated lookup of a collectible but already-captured word give any secondary feedback?
- Should non-board words be stored anywhere as lookup history, or only returned transiently?

## Recommended Decisions

To keep the first version simple and fun:

- All 12 pages visible from the start
- Locked slots visible, but lemma hidden
- No hint text on locked cards in v1
- Rarity revealed on capture
- No XP for non-collectible lookups
- No duplicate XP for already-captured words
- Legacy non-board captures kept out of the puzzle-board UI

## Final Recommendation

Build the Codex as `Volume 1` of a collectible puzzle board.

- `300` curated words
- `12` pages
- `25` slots per page
- mixed rarity on every page
- dictionary lookup always available
- capture only for board words

This gives Lexipal a Codex that feels like a real game system:

- finite
- collectible
- visually satisfying
- expandable later with new volumes
