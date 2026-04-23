# Frontend Implementation Brief - BookCircle Chat

> Hand this to a frontend agent. It is self-contained for the new chat feature and matches the backend that is now implemented. It assumes the base BookCircle screens already exist per `BOOKCIRCLE_FRONTEND_PROMPT.md`.

---

## 1. What you are building

Add a real-time group chat to each `ACTIVE` BookCircle so members can hold the meeting inside the app.

Rules:
- Chat is available to any active member of the circle.
- `CircleVisibility` does **not** affect chat. It still only controls reading-progress visibility.
- Chat is text-only in v1.
- Messages are persisted and can be paged through as history.
- Senders can delete their own messages.
- Circle owners can delete any message.
- Deleted messages remain in history as tombstones.
- Presence is live and deduped per user, even if they have multiple tabs open.
- When a member is removed or leaves, chat access ends immediately.
- When a circle is closed, chat becomes inaccessible to everyone and the UI should shut down.

Out of scope:
- attachments
- message editing
- typing indicators
- read receipts
- reactions
- threads
- activity feeds

---

## 2. Auth And Transport Model

### 2.1 REST auth

All REST chat endpoints use the normal API auth model:

`Authorization: Bearer <token>`

Base REST prefix:

`http://<host>:8585/lexipal/api/v1`

### 2.2 WebSocket auth

The backend uses **native STOMP over WebSocket**, not SockJS.

WebSocket endpoint:

`ws://<host>:8585/lexipal/ws`

Important:
- Do **not** use SockJS. The backend only registers a plain websocket STOMP endpoint.
- Browser handshake auth is **not** where the JWT goes.
- The JWT must be sent in the STOMP `CONNECT` frame headers:

```txt
Authorization: Bearer <token>
```

- The backend uses the JWT `sub` as the websocket principal name.
- If the `CONNECT` frame is missing the auth header, connection setup fails.

### 2.3 Recommended client library

Use `@stomp/stompjs` with the browser's native websocket transport.

Recommended setup:

```ts
import { Client } from "@stomp/stompjs";

const client = new Client({
  brokerURL: "ws://localhost:8585/lexipal/ws",
  connectHeaders: {
    Authorization: `Bearer ${token}`,
  },
  reconnectDelay: 5000,
});
```

For production, switch `ws://` to `wss://` when the site is served over HTTPS.

---

## 3. Recommended Page Lifecycle

When the user opens `/circles/:id` and the chat panel should be active:

1. Ensure you already have the normal circle detail payload from `GET /book-circles/{id}`.
2. Ensure you have the current profile from `GET /profile/me`.
3. Open the STOMP connection with the JWT in `connectHeaders`.
4. Subscribe to:
   - `/user/queue/book-circles/{circleId}/events`
   - `/user/queue/errors`
5. Fetch recent history with `GET /book-circles/{circleId}/chat/messages`.
6. Derive the caller's internal `userId` from the circle members list.
7. Send `SEND /app/book-circles/{circleId}/presence.join`.
8. Render the chat UI.

When leaving the page:

1. Send `SEND /app/book-circles/{circleId}/presence.leave`
2. Unsubscribe
3. Disconnect the STOMP client

If the tab closes or disconnects without `presence.leave`, the backend still cleans presence up automatically.

---

## 4. How To Identify The Current User In Chat

Chat messages use the backend's internal `User.id`:

```json
{
  "senderUserId": 9
}
```

`GET /profile/me` does **not** return the internal user id.

To know whether a message is "mine", derive the current user's internal id from the BookCircle detail payload:

1. Call `GET /profile/me` and get `nickname`
2. Call `GET /book-circles/{id}` and inspect `members[]`
3. Find the member whose `userNickname === profile.nickname`
4. Use that `members[].userId` as the current internal user id for chat comparisons

This is safe because nicknames are unique.

Example:

```ts
const currentCircleUserId =
  circle.members.find((m) => m.userNickname === profile.nickname)?.userId ?? null;
```

Use that for:
- deciding whether a message belongs to the current user
- deciding whether the delete action should be shown
- rendering "You" labels if desired

---

## 5. REST API

All paths below are relative to:

`/lexipal/api/v1/book-circles`

### 5.1 Get chat history - `GET /{circleId}/chat/messages`

Query params:
- `beforeMessageId` optional cursor
- `limit` optional, default `50`, min `1`, max `100`

Behavior:
- Caller must be a current member of an `ACTIVE` circle
- Returns the page in **chronological order** within that page: oldest first, newest last
- `nextBeforeMessageId` is the id you pass back on the next request to load older messages

Example request:

`GET /17/chat/messages?limit=50`

Example response:

```json
{
  "messages": [
    {
      "id": 121,
      "circleId": 17,
      "senderUserId": 9,
      "senderNickname": "lina42",
      "content": "I think the last chapter is the turning point.",
      "deleted": false,
      "createdAt": "2026-04-23T19:02:14.221",
      "deletedAt": null
    },
    {
      "id": 122,
      "circleId": 17,
      "senderUserId": 7,
      "senderNickname": "erik_e",
      "content": "Agreed. The pacing changes there.",
      "deleted": false,
      "createdAt": "2026-04-23T19:02:41.610",
      "deletedAt": null
    }
  ],
  "hasMore": true,
  "nextBeforeMessageId": 121
}
```

Pagination rule:
- If `hasMore === true`, call the same endpoint again with `beforeMessageId=nextBeforeMessageId`
- Prepend the returned `messages` array to the local list as-is

Tombstone example:

```json
{
  "id": 99,
  "circleId": 17,
  "senderUserId": 9,
  "senderNickname": "lina42",
  "content": null,
  "deleted": true,
  "createdAt": "2026-04-23T18:30:11.004",
  "deletedAt": "2026-04-23T18:35:19.991"
}
```

Errors:
- `403` with `"You are not a member of this circle"`
- `403` with `"Chat is unavailable because this circle is closed."`
- `400` with `"limit must be between 1 and 100."`
- `404` if the circle id does not exist

### 5.2 Delete message - `DELETE /{circleId}/chat/messages/{messageId}`

Rules:
- sender can delete own message
- circle owner can delete any message
- other members cannot delete the message

Success:
- `204 No Content`

Errors:
- `403` with `"Only the sender or circle owner can delete this message"`
- `403` with `"You are not a member of this circle"`
- `403` with `"Chat is unavailable because this circle is closed."`
- `400` with `"This message has already been deleted."`
- `404` if the message id does not exist inside that circle

Do not remove a deleted message from the UI. Replace it with a deleted/tombstone state.

---

## 6. WebSocket Destinations

### 6.1 Subscribe destinations

Subscribe after connect:

- Chat events:
  - `/user/queue/book-circles/{circleId}/events`
- Chat errors:
  - `/user/queue/errors`

### 6.2 Send destinations

Send over STOMP:

- Send a message:
  - `/app/book-circles/{circleId}/messages`
- Join presence:
  - `/app/book-circles/{circleId}/presence.join`
- Leave presence:
  - `/app/book-circles/{circleId}/presence.leave`

### 6.3 Send payloads

#### Message send payload

```json
{
  "content": "Hello everyone"
}
```

Validation:
- trimmed server-side
- cannot be blank/whitespace-only
- max 2000 chars

#### Presence join/leave payload

No body is required. Send an empty frame/body.

Example:

```ts
client.publish({
  destination: `/app/book-circles/${circleId}/presence.join`,
  body: "",
});
```

---

## 7. WebSocket Event DTOs

All chat events delivered on `/user/queue/book-circles/{circleId}/events` use this shape:

```json
{
  "circleId": 17,
  "type": "MESSAGE_CREATED",
  "message": null,
  "onlineMembers": null,
  "reason": null
}
```

Fields:
- `circleId: number`
- `type: "MESSAGE_CREATED" | "MESSAGE_DELETED" | "PRESENCE_UPDATED" | "CHAT_CLOSED" | "CHAT_ACCESS_REVOKED"`
- `message: CircleChatMessageDTO | null`
- `onlineMembers: CircleChatPresenceMemberDTO[] | null`
- `reason: string | null`

### 7.1 `MESSAGE_CREATED`

Example:

```json
{
  "circleId": 17,
  "type": "MESSAGE_CREATED",
  "message": {
    "id": 123,
    "circleId": 17,
    "senderUserId": 9,
    "senderNickname": "lina42",
    "content": "Hello everyone",
    "deleted": false,
    "createdAt": "2026-04-23T19:05:03.102",
    "deletedAt": null
  },
  "onlineMembers": null,
  "reason": null
}
```

Frontend handling:
- append the message if it is not already present
- if the id already exists locally, replace the existing row
- the sender also receives this event, so the sender should use the server echo as source of truth

Recommendation:
- avoid optimistic message insertion in v1
- wait for `MESSAGE_CREATED` and render from the server payload

### 7.2 `MESSAGE_DELETED`

Example:

```json
{
  "circleId": 17,
  "type": "MESSAGE_DELETED",
  "message": {
    "id": 123,
    "circleId": 17,
    "senderUserId": 9,
    "senderNickname": "lina42",
    "content": null,
    "deleted": true,
    "createdAt": "2026-04-23T19:05:03.102",
    "deletedAt": "2026-04-23T19:07:19.440"
  },
  "onlineMembers": null,
  "reason": null
}
```

Frontend handling:
- find the existing message by `id`
- replace it with the tombstone payload
- do not remove it from the list

### 7.3 `PRESENCE_UPDATED`

Example:

```json
{
  "circleId": 17,
  "type": "PRESENCE_UPDATED",
  "message": null,
  "onlineMembers": [
    {
      "userId": 7,
      "userNickname": "erik_e"
    },
    {
      "userId": 9,
      "userNickname": "lina42"
    }
  ],
  "reason": null
}
```

Important:
- This is a **full replacement snapshot**, not a patch
- Replace the local online-member list with `onlineMembers`
- Multiple tabs from the same user are already deduped by the backend
- Presence updates are broadcast to all active members, including the caller

`PRESENCE_UPDATED` can happen when:
- a member sends `presence.join`
- a member sends `presence.leave`
- a tab disconnects
- a member is removed

### 7.4 `CHAT_CLOSED`

Example:

```json
{
  "circleId": 17,
  "type": "CHAT_CLOSED",
  "message": null,
  "onlineMembers": null,
  "reason": "This circle has been closed and chat is no longer available."
}
```

Frontend handling:
- show a non-dismissible banner or blocking state
- disable the composer immediately
- stop presence
- unsubscribe and disconnect the chat socket for that page
- do not attempt to refetch history again
- ideally route the user back to the circles list or refresh the parent circle screen so it reflects `status = CLOSED`

### 7.5 `CHAT_ACCESS_REVOKED`

Example:

```json
{
  "circleId": 17,
  "type": "CHAT_ACCESS_REVOKED",
  "message": null,
  "onlineMembers": null,
  "reason": "You are no longer a member of this circle."
}
```

Frontend handling:
- show the reason as a toast/banner
- disable the composer immediately
- unsubscribe and disconnect the chat socket
- clear the local message list for that circle view or route away

This can happen when:
- the owner removes the member
- the user leaves the circle in another tab/session

Possible reasons:
- `"You are no longer a member of this circle."`
- `"You left this circle and chat is no longer available."`

---

## 8. WebSocket Error Channel

Subscribe to:

`/user/queue/errors`

The backend sends structured errors there for websocket message handling failures.

Shape:

```json
{
  "message": "You are not a member of this circle",
  "status": 403
}
```

This is used for cases like:
- outsider sends `presence.join`
- outsider sends a message
- member sends after the circle is closed
- invalid message body such as blank content

Frontend behavior:
- show the error as a toast or inline composer error
- keep the socket connected unless the page is no longer valid

Likely messages:
- `400` `"Message content is required."`
- `400` `"Message content must be 2000 characters or fewer."`
- `403` `"You are not a member of this circle"`
- `403` `"Chat is unavailable because this circle is closed."`
- `404` if a referenced circle does not exist

---

## 9. UI Requirements

Add chat to the existing BookCircle detail page (`/circles/:id`).

### 9.1 Main chat panel

Include:
- a header with title like `Circle Chat`
- a live online count or avatar row driven by `PRESENCE_UPDATED`
- a scrollable message list
- a composer at the bottom
- a "Load older messages" affordance when `hasMore === true`

### 9.2 Message rendering

For normal messages show:
- sender nickname
- timestamp
- text content
- special styling if it is the current user's message

For deleted messages show:
- sender nickname
- original timestamp
- deleted/tombstone text such as `Message deleted`
- disabled delete button
- muted styling

Do not hide deleted messages from the timeline.

### 9.3 Delete action visibility

Show delete on a message only if:

```txt
message.senderUserId === currentCircleUserId
OR
circle.callerIsOwner === true
```

When the user deletes a message:
- call the REST `DELETE` endpoint
- do not remove the message locally
- wait for the `MESSAGE_DELETED` event or refetch if needed

### 9.4 Composer behavior

Requirements:
- trim whitespace before send on the client too
- block blank sends
- max length 2000 characters
- disable while the page is in a revoked/closed state

Recommendation:
- use Enter to send
- use Shift+Enter for a newline if you want multiline input

### 9.5 Visibility note

`OWNER_ONLY` circles still allow full chat participation for all members.

Do not hide chat based on `circle.visibility`.

### 9.6 Closed/revoked UI

If chat closes or access is revoked:
- stop live updates
- disable the composer
- show the server reason text
- route away or fall back to a non-chat state

If the initial history fetch returns `403` with:
- `"Chat is unavailable because this circle is closed."`
- `"You are not a member of this circle"`

then do not show the normal active chat UI.

---

## 10. State Management Notes

### 10.1 Message state

Store messages keyed by `id`.

When merging REST history and websocket events:
- dedupe by `message.id`
- replace existing messages when the same id arrives again

### 10.2 History loading

Initial page load:
- fetch the most recent page with no `beforeMessageId`

Load older:
- use `nextBeforeMessageId`
- prepend the returned page to the existing list

### 10.3 Presence state

Treat the server as source of truth.

When a `PRESENCE_UPDATED` event arrives:
- replace the whole online list
- do not try to increment/decrement manually

### 10.4 Nickname changes

The backend uses the sender's **current** nickname when building message DTOs.

That means if a user changes nickname later:
- newly fetched history may show the new nickname on older messages
- incoming events also use the current nickname

Do not treat the nickname as a frozen historical snapshot.

---

## 11. Acceptance Walkthrough

Use this as the frontend smoke test.

1. User A opens a circle detail page for an `ACTIVE` circle.
2. The chat panel loads recent history via REST.
3. The client opens STOMP, subscribes to events/errors, and sends `presence.join`.
4. User A sees themselves appear in the online list.
5. User B opens the same circle in another browser/tab and also joins presence.
6. Both users see a `PRESENCE_UPDATED` result showing User A and User B online.
7. User B sends `Hello circle`.
8. Both users receive `MESSAGE_CREATED` and the message appears in the timeline.
9. User B opens the same circle in a second tab.
10. The online list should still show User B only once.
11. User B deletes their own message.
12. Both users receive `MESSAGE_DELETED`; the message remains visible as a tombstone.
13. User A sends another message.
14. User A, as owner, deletes User B's older message successfully.
15. A non-owner member tries deleting someone else's message and gets a `403`.
16. Owner removes User B from the circle.
17. User B receives `CHAT_ACCESS_REVOKED` and stops receiving future circle events.
18. User A still receives future events normally.
19. Owner closes the circle.
20. Active members receive `CHAT_CLOSED`.
21. After closure, `GET /book-circles/{id}/chat/messages` returns `403` and new websocket sends return a `/user/queue/errors` event with the closed-circle message.

---

## 12. Quick Reference Table

| Purpose | Method / Type | Path / Destination | Body | Success | Notable errors |
|---|---|---|---|---|---|
| Get recent chat history | `GET` | `/book-circles/{circleId}/chat/messages` | query: `beforeMessageId?`, `limit?` | `200` + `CircleChatHistoryResponseDTO` | `403` not member/closed, `400` bad limit |
| Delete message | `DELETE` | `/book-circles/{circleId}/chat/messages/{messageId}` | none | `204` | `403` not allowed, `400` already deleted, `404` missing message |
| Open socket | STOMP `CONNECT` | `/lexipal/ws` | header `Authorization: Bearer <token>` | connected session | connect fails if auth missing/invalid |
| Subscribe to chat events | STOMP `SUBSCRIBE` | `/user/queue/book-circles/{circleId}/events` | none | receives `CircleChatEventDTO` | no fallback topic exists |
| Subscribe to chat errors | STOMP `SUBSCRIBE` | `/user/queue/errors` | none | receives `{message,status}` | use for send/join/leave failures |
| Send message | STOMP `SEND` | `/app/book-circles/{circleId}/messages` | `{ "content": "..." }` | `MESSAGE_CREATED` event | `/user/queue/errors` on failure |
| Join presence | STOMP `SEND` | `/app/book-circles/{circleId}/presence.join` | empty body | `PRESENCE_UPDATED` event | `/user/queue/errors` on failure |
| Leave presence | STOMP `SEND` | `/app/book-circles/{circleId}/presence.leave` | empty body | `PRESENCE_UPDATED` event | usually no visible error |

---

That is the full frontend contract for BookCircle chat. Build it against the backend as source of truth, use the server event payloads rather than local guesswork, and keep the chat UI tightly coupled to circle membership and circle status.
