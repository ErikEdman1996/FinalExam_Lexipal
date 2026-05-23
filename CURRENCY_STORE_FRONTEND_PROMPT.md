# Frontend Implementation Prompt - Currency Store and Themes

> Hand this prompt to a frontend agent. It describes the wallet, streak-freeze store item, paid theme items, theme selection APIs, palette contract, refresh rules, and UI acceptance criteria supported by the backend.

## Goal

Extend Lexipal's currency UI into a practical store and theme picker.

Build:

1. A compact wallet summary with current coins and owned streak-freeze tokens.
2. A store surface that can buy one streak freeze or unlock one of eight paid themes.
3. A theme picker that exposes the two free defaults plus paid theme ownership and selection state.
4. CSS variable application for every theme code listed below.

This is an in-product reading tool. Keep the wallet and store useful and restrained rather than making a marketing or game-shop page.

## Product Rules

- Coins are spendable currency.
- XP remains lifetime progression for levels and portrait unlocks.
- Never subtract XP for store purchases.
- Coin earnings are backend-owned; the frontend has no grant-coins endpoint.
- Failed or zero-reward outcomes do not award coins.
- Streak freezes are automatic inventory items. Do not build manual activation or date selection.
- Themes are durable unlocks. Purchase and selection are separate actions.
- Theme hex maps stay frontend-owned. Backend theme APIs return metadata, ownership, selection, and prices only.

Coin rewards currently happen after these backend events:

| Event | Coins |
| --- | ---: |
| Reading session completed | `25` |
| Challenge passed | `10` |
| Crossword passed | `10` |
| Reading-list book completed | `50` |
| Reading list completed | `200` |

## Theme Catalog

Theme codes are the contract between backend metadata and frontend palette maps.

### Free themes

| Code | Name | Tone | Price |
| --- | --- | --- | ---: |
| `default_light` | Reading Room | Light | `0` |
| `default_dark` | Midnight Reader | Dark | `0` |

The two free themes are always selectable and do not need unlock rows.

### Paid themes

Every paid theme costs `100` coins.

| Code | Name | Tone |
| --- | --- | --- |
| `vellum_dawn` | Vellum Dawn | Light |
| `sage_margin` | Sage Margin | Light |
| `harbor_folio` | Harbor Folio | Light |
| `rose_bookmark` | Rose Bookmark | Light |
| `midnight_archive` | Midnight Archive | Dark |
| `mothlight_nocturne` | Mothlight Nocturne | Dark |
| `forest_canopy` | Forest Canopy | Dark |
| `plum_afterword` | Plum Afterword | Dark |

Purchasing a paid theme:

- spends `100` coins once
- unlocks the theme
- does not automatically select it
- returns `409 Conflict` if the user already owns it

## Frontend Palette Contract

Keep the current `theme.css` token contract complete for all ten theme codes.

Current tokens:

```ts
type LexipalThemeTokens = {
  background: string;
  surface: string;
  surfaceMuted: string;
  surfaceAlt: string;
  sidebar: string;
  sidebarBorder: string;
  sidebarHover: string;
  border: string;
  borderStrong: string;
  text: string;
  textMuted: string;
  textSubtle: string;
  textOnBrand: string;
  sidebarText: string;
  sidebarMuted: string;
  brand: string;
  brandHover: string;
  brandSoft: string;
  brandStrong: string;
  accent: string;
  accentSoft: string;
  accentStrong: string;
  gold: string;
  danger: string;
  dangerSoft: string;
  gradientFrom: string;
  gradientVia: string;
  gradientTo: string;
  pageGlow1: string;
  pageGlow2: string;
  pageGlow3: string;
};
```

Use the current default palettes exactly:

| Token | `default_light` | `default_dark` |
| --- | --- | --- |
| Background | `#F1EDE6` | `#0F0D0B` |
| Surface | `#F8F5EE` | `#1D1814` |
| Surface muted | `#ECE7DC` | `#16120F` |
| Surface alt | `#ECE7DC` | `#120F0D` |
| Sidebar | `#E5DFD2` | `#0B0908` |
| Sidebar border | `#2A26221A` | `#E8E2D714` |
| Sidebar hover | `#2A26220F` | `#E8E2D712` |
| Border | `#2A26221A` | `#E8E2D714` |
| Border strong | `#2A26222E` | `#E8E2D729` |
| Text | `#2A2622` | `#EAE4D6` |
| Text muted | `#6B6259` | `#9A9082` |
| Text subtle | `#9C9388` | `#65604F` |
| Text on brand | `#FFFFFF` | `#18110D` |
| Sidebar text | `#2A2622` | `#EAE4D6` |
| Sidebar muted | `#6B6259` | `#9A9082` |
| Brand | `#B85A3E` | `#D87A5A` |
| Brand hover | `#A64B32` | `#E08A6C` |
| Brand soft | `#ECD3C8` | `#D87A5A2E` |
| Brand strong | `#AE5137` | `#D87A5A` |
| Accent | `#6B6259` | `#9A9082` |
| Accent soft | `#6B625924` | `#9A908229` |
| Accent strong | `#6B6259` | `#9A9082` |
| Gold | `#A88433` | `#D4A55A` |
| Danger | `#B85A3E` | `#D87A5A` |
| Danger soft | `#ECD3C8` | `#D87A5A2E` |
| Gradient from | `#F8F5EE` | `#0F0D0B` |
| Gradient via | `#F1EDE6` | `#100E0C` |
| Gradient to | `#ECE7DC` | `#0F0D0B` |
| Page glow 1 | `#B85A3E29` | `#D87A5A1A` |
| Page glow 2 | `#A884331F` | `#D4A55A12` |
| Page glow 3 | `#6B62591A` | `#9A90820F` |

Use these seeds for the eight paid themes:

| Code | Background / Surface / Muted / Alt / Sidebar | Brand / Hover / Soft / Accent / Gold |
| --- | --- | --- |
| `vellum_dawn` | `#F3E7CF / #FFF7E7 / #E8D6B5 / #E8D6B5 / #E1CCAA` | `#B9673D / #A6532E / #EDD2BF / #567A63 / #A27A27` |
| `sage_margin` | `#EAF0E4 / #F6FAF0 / #DDE7D2 / #DDE7D2 / #D1DEC6` | `#7A6040 / #664E31 / #E1D4BC / #52745B / #A48A3E` |
| `harbor_folio` | `#E6EEF0 / #F4FAFB / #D7E4E8 / #D7E4E8 / #C9DADF` | `#327B86 / #286873 / #C7E3E6 / #5D657D / #B08A35` |
| `rose_bookmark` | `#F3E7E9 / #FCF4F4 / #E7D7DC / #E7D7DC / #DDCAD0` | `#B24E6A / #993F59 / #EBCBD5 / #6B647F / #A98142` |
| `midnight_archive` | `#0C1220 / #151D31 / #10182A / #0E1424 / #080E19` | `#6D95D8 / #82A6E3 / #6D95D82E / #A1B7D6 / #D7A65D` |
| `mothlight_nocturne` | `#120F0A / #211A11 / #19130D / #150F0A / #0D0906` | `#D99245 / #E2A45C / #D992452E / #A89A7C / #E0B65D` |
| `forest_canopy` | `#09130F / #10231B / #0D1A14 / #0A1610 / #07100C` | `#55A676 / #69B88A / #55A6762E / #8FB3A0 / #C6A458` |
| `plum_afterword` | `#150D1C / #24162D / #1B1122 / #160C1D / #100913` | `#AE72D6 / #BF86E3 / #AE72D62E / #A58BB8 / #D6A85E` |

For paid theme token maps:

- Build complete token objects, not partial overrides.
- Use the corresponding free Light or Dark theme relationships for borders, sidebar text, text contrast, danger, gradients, and glow alpha behavior.
- Keep CSS variable application keyed by theme code.
- When profile has no server-selected theme, preserve the existing frontend default-theme fallback.

## Auth And Paths

Reuse the existing authenticated API client and Keycloak token handling.

All endpoints require:

```http
Authorization: Bearer <token>
```

API prefix:

```text
/lexipal/api/v1
```

## Backend APIs

### Wallet

```http
GET /wallet/me
```

```json
{
  "coinBalance": 125,
  "availableStreakFreezes": 2
}
```

### Generic store catalog

```http
GET /store/me
```

```json
{
  "wallet": {
    "coinBalance": 125,
    "availableStreakFreezes": 2
  },
  "items": [
    {
      "code": "streak_freeze",
      "name": "Streak Freeze",
      "description": "Protect one missed reading day.",
      "itemType": "STREAK_FREEZE",
      "priceCoins": 100,
      "owned": true,
      "selected": false,
      "ownedQuantity": 2,
      "themeCode": null
    },
    {
      "code": "vellum_dawn",
      "name": "Vellum Dawn",
      "description": "Warm paper tones for an early reading session.",
      "itemType": "THEME",
      "priceCoins": 100,
      "owned": false,
      "selected": false,
      "ownedQuantity": null,
      "themeCode": "vellum_dawn"
    }
  ]
}
```

The store catalog contains the streak freeze plus the eight paid theme store items. Free default themes come from the themes endpoint, not from store items.

### Generic purchase

```http
POST /store/items/{itemCode}/purchase
```

Request body:

- None.

Response:

```json
{
  "wallet": {
    "coinBalance": 25,
    "availableStreakFreezes": 3
  },
  "item": {
    "code": "streak_freeze",
    "name": "Streak Freeze",
    "description": "Protect one missed reading day.",
    "itemType": "STREAK_FREEZE",
    "priceCoins": 100,
    "owned": true,
    "selected": false,
    "ownedQuantity": 3,
    "themeCode": null
  }
}
```

Use this endpoint for store purchases.

The old compatibility endpoint still buys one freeze and returns only wallet state:

```http
POST /streak-freezes/me/purchase
```

### Themes

```http
GET /themes/me
```

Response item shape:

```json
{
  "code": "vellum_dawn",
  "name": "Vellum Dawn",
  "description": "Warm paper tones for an early reading session.",
  "tone": "LIGHT",
  "sortOrder": 100,
  "unlocked": true,
  "selected": false,
  "priceCoins": 100,
  "unlockedAt": "2026-05-22T14:30:00.000"
}
```

The endpoint returns all active free and paid theme definitions. Locked paid themes still appear with `unlocked: false`.

Select one theme:

```http
PATCH /themes/me/selected
Content-Type: application/json

{
  "themeCode": "vellum_dawn"
}
```

The response is the selected theme item.

### Profile

```http
GET /profile/me
```

The profile response now includes both wallet and selected theme metadata:

```ts
interface ProfileReadResponseDTO {
  status: string;
  nickname: string | null;
  needsOnboarding: boolean;
  createdAt: string;
  stats: ProfileStatsResponseDTO;
  streak: StreakMetaDataReadResponseDTO;
  recentDailyActivities: DailyActivityReadResponseDTO[];
  selectedPortrait: PortraitResponseDTO | null;
  selectedTheme: ThemeResponseDTO | null;
  wallet: WalletResponseDTO;
}
```

`selectedTheme` is nullable. A null value means the backend has no persisted user choice yet.

### Streak state

```http
GET /streakMetadata/me
```

```ts
interface StreakMetaDataReadResponseDTO {
  id: number;
  userId: number;
  currentStreak: number;
  longestStreak: number;
  lastActivityDate: string | null;
  availableStreakFreezes: number;
  freezeProtectedThroughDate: string | null;
}
```

The backend can consume freezes while reconciling streak state during profile/streak reads. Always render the returned values instead of calculating freeze use in the browser.

## Suggested TypeScript Types

```ts
export interface WalletResponseDTO {
  coinBalance: number;
  availableStreakFreezes: number;
}

export interface StoreItemResponseDTO {
  code: string;
  name: string;
  description: string;
  itemType: "STREAK_FREEZE" | "THEME";
  priceCoins: number;
  owned: boolean;
  selected: boolean;
  ownedQuantity: number | null;
  themeCode: string | null;
}

export interface StoreResponseDTO {
  wallet: WalletResponseDTO;
  items: StoreItemResponseDTO[];
}

export interface StorePurchaseResponseDTO {
  wallet: WalletResponseDTO;
  item: StoreItemResponseDTO;
}

export interface ThemeResponseDTO {
  code: string;
  name: string;
  description: string;
  tone: "LIGHT" | "DARK";
  sortOrder: number;
  unlocked: boolean;
  selected: boolean;
  priceCoins: number;
  unlockedAt: string | null;
}
```

API client methods should be equivalent to:

```ts
getMyWallet(): Promise<WalletResponseDTO>
getMyStore(): Promise<StoreResponseDTO>
purchaseStoreItem(itemCode: string): Promise<StorePurchaseResponseDTO>
getMyThemes(): Promise<ThemeResponseDTO[]>
selectMyTheme(themeCode: string): Promise<ThemeResponseDTO>
```

## Required UI Work

### Wallet

- Show current coin balance where the store flow and profile/progress surfaces need it.
- Show owned streak-freeze count near streak/profile information.
- Use `profile.wallet` when profile is already loaded.
- Use `GET /wallet/me` or `GET /store/me` when a lighter or store-scoped refresh is better.

### Store

- Show current wallet balance.
- Render the streak-freeze item with inventory quantity and `100` coin price.
- Render the eight paid theme items with locked, owned, selected, loading, success, and error states.
- Use item metadata from `GET /store/me`; use `themeCode` to connect paid theme items to frontend palette previews or picker state.
- Disable purchase controls when confirmed wallet state is below price, while still handling backend conflict errors.
- Replace wallet state with the successful purchase response.
- Do not allow owned paid themes to look buyable again.

### Theme picker and application

- Render all themes from `GET /themes/me`.
- Let free defaults be selected immediately.
- Let unlocked paid themes be selected.
- Keep locked paid themes visible as store candidates but reject selection UI actions until they are unlocked.
- Apply CSS variables from the selected frontend token map after successful selection.
- On app/profile load, apply `profile.selectedTheme.code` when non-null and supported locally.
- For null or unknown server codes, fall back to the existing frontend default behavior without breaking the page.

### Streak state

- A freeze is not manually armed.
- One owned freeze protects one missed reading day on the backend.
- `currentStreak` returned by the backend is already the effective current streak.
- `lastActivityDate` and `freezeProtectedThroughDate` mean different things; do not replace one with the other.
- Profile or streak reads can consume freezes and change returned inventory count.

## Refresh Rules

Invalidate or update relevant wallet/store/profile/theme queries after:

- successful store purchase
- successful theme selection
- coin-eligible reward flows already present in the app

## Error Handling

Insufficient coins:

```json
{
  "message": "You do not have enough coins.",
  "status": 409
}
```

Duplicate paid theme purchase:

```json
{
  "message": "Theme is already unlocked.",
  "status": 409
}
```

Locked theme selection:

```json
{
  "message": "Theme is locked.",
  "status": 400
}
```

Use the app's normal authenticated request, pending state, toast, and query invalidation patterns.

## Acceptance Criteria

1. Wallet typing and UI show `coinBalance` and `availableStreakFreezes`.
2. Store typing and UI use `GET /store/me` and `POST /store/items/{itemCode}/purchase`.
3. Store catalog renders one freeze item and the eight paid theme items from backend state.
4. A freeze purchase spends `100` coins and increases owned freeze quantity by one.
5. A paid theme purchase spends `100` coins once and changes the theme to owned without auto-selecting it.
6. Duplicate and insufficient-funds purchases preserve the last confirmed wallet state.
7. Theme typing and UI use `GET /themes/me` and `PATCH /themes/me/selected`.
8. Free defaults remain selectable.
9. Locked paid themes cannot be selected until purchase succeeds.
10. Profile typing includes nullable `selectedTheme` and wallet state.
11. All ten theme codes resolve to complete frontend token maps.
12. The existing default Light and Dark hex maps stay exact.
13. Null selected-theme state preserves the existing default theme fallback.
14. Frontend does not treat XP as spendable coins or zero-reward flows as coin earnings.
