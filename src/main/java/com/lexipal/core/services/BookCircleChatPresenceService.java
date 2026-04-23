package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.CircleChatEventDTO;
import com.lexipal.core.dtos.responses.CircleChatPresenceMemberDTO;
import com.lexipal.core.entities.CircleChatEventType;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookCircleChatPresenceService
{
    private final Map<Long, Map<Long, Integer>> circlePresenceCounts = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> sessionCircleJoins = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Long>> sessionSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserIds = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final BookCircleChatEventBroadcaster eventBroadcaster;

    @Autowired
    public BookCircleChatPresenceService(final UserRepository userRepository,
                                         final BookCircleChatEventBroadcaster eventBroadcaster)
    {
        this.userRepository = userRepository;
        this.eventBroadcaster = eventBroadcaster;
    }

    public synchronized void registerSubscription(String sessionId, String subscriptionId, Long circleId)
    {
        sessionSubscriptions.computeIfAbsent(sessionId, ignored -> new HashMap<>())
                .put(subscriptionId, circleId);
    }

    public void unregisterSubscription(String sessionId, String subscriptionId)
    {
        Long circleId;

        synchronized (this)
        {
            Map<String, Long> subscriptions = sessionSubscriptions.get(sessionId);
            if (subscriptions == null)
            {
                return;
            }

            circleId = subscriptions.remove(subscriptionId);
            cleanupSessionState(sessionId);
        }

        if (circleId != null)
        {
            leaveBySession(circleId, sessionId);
        }
    }

    public void join(Long circleId, User user, String sessionId)
    {
        List<CircleChatPresenceMemberDTO> snapshot;

        synchronized (this)
        {
            sessionUserIds.put(sessionId, user.getId());

            Set<Long> joinedCircles = sessionCircleJoins.computeIfAbsent(sessionId, ignored -> new HashSet<>());
            if (!joinedCircles.add(circleId))
            {
                return;
            }

            circlePresenceCounts
                    .computeIfAbsent(circleId, ignored -> new HashMap<>())
                    .merge(user.getId(), 1, Integer::sum);

            snapshot = buildSnapshot(circleId);
        }

        broadcastPresenceUpdate(circleId, snapshot);
    }

    public void leave(Long circleId, User user, String sessionId)
    {
        leaveBySession(circleId, sessionId);
    }

    public void handleDisconnect(String sessionId)
    {
        Map<Long, List<CircleChatPresenceMemberDTO>> updates = new HashMap<>();

        synchronized (this)
        {
            Set<Long> joinedCircles = sessionCircleJoins.remove(sessionId);
            sessionSubscriptions.remove(sessionId);
            Long userId = sessionUserIds.remove(sessionId);

            if (joinedCircles == null || userId == null)
            {
                return;
            }

            for (Long circleId : joinedCircles)
            {
                if (decrementUserPresence(circleId, userId))
                {
                    updates.put(circleId, buildSnapshot(circleId));
                }
            }
        }

        updates.forEach(this::broadcastPresenceUpdate);
    }

    public void revokeUserAccess(Long circleId, Long userId)
    {
        List<CircleChatPresenceMemberDTO> snapshot = null;
        boolean changed;

        synchronized (this)
        {
            changed = forceRemoveUserFromCircle(circleId, userId);
            if (changed)
            {
                snapshot = buildSnapshot(circleId);
            }
        }

        if (changed)
        {
            broadcastPresenceUpdate(circleId, snapshot);
        }
    }

    public synchronized void clearCircle(Long circleId)
    {
        circlePresenceCounts.remove(circleId);

        for (Map.Entry<String, Set<Long>> entry : sessionCircleJoins.entrySet())
        {
            entry.getValue().remove(circleId);
        }

        for (Map.Entry<String, Map<String, Long>> entry : sessionSubscriptions.entrySet())
        {
            entry.getValue().entrySet().removeIf(subscription -> subscription.getValue().equals(circleId));
        }

        List<String> sessionIds = new ArrayList<>(sessionUserIds.keySet());
        for (String sessionId : sessionIds)
        {
            cleanupSessionState(sessionId);
        }
    }

    private void leaveBySession(Long circleId, String sessionId)
    {
        List<CircleChatPresenceMemberDTO> snapshot = null;
        boolean changed = false;

        synchronized (this)
        {
            Set<Long> joinedCircles = sessionCircleJoins.get(sessionId);
            Long userId = sessionUserIds.get(sessionId);

            if (joinedCircles == null || userId == null || !joinedCircles.remove(circleId))
            {
                return;
            }

            changed = decrementUserPresence(circleId, userId);
            cleanupSessionState(sessionId);

            if (changed)
            {
                snapshot = buildSnapshot(circleId);
            }
        }

        if (changed)
        {
            broadcastPresenceUpdate(circleId, snapshot);
        }
    }

    private boolean decrementUserPresence(Long circleId, Long userId)
    {
        Map<Long, Integer> counts = circlePresenceCounts.get(circleId);
        if (counts == null)
        {
            return false;
        }

        Integer currentCount = counts.get(userId);
        if (currentCount == null)
        {
            return false;
        }

        if (currentCount <= 1)
        {
            counts.remove(userId);
        }
        else
        {
            counts.put(userId, currentCount - 1);
        }

        if (counts.isEmpty())
        {
            circlePresenceCounts.remove(circleId);
        }

        return true;
    }

    private boolean forceRemoveUserFromCircle(Long circleId, Long userId)
    {
        boolean changed = false;

        Map<Long, Integer> counts = circlePresenceCounts.get(circleId);
        if (counts != null && counts.remove(userId) != null)
        {
            changed = true;
            if (counts.isEmpty())
            {
                circlePresenceCounts.remove(circleId);
            }
        }

        for (Map.Entry<String, Long> entry : sessionUserIds.entrySet())
        {
            if (!entry.getValue().equals(userId))
            {
                continue;
            }

            Set<Long> joinedCircles = sessionCircleJoins.get(entry.getKey());
            if (joinedCircles != null)
            {
                joinedCircles.remove(circleId);
            }
            cleanupSessionState(entry.getKey());
        }

        return changed;
    }

    private List<CircleChatPresenceMemberDTO> buildSnapshot(Long circleId)
    {
        Map<Long, Integer> counts = circlePresenceCounts.get(circleId);
        if (counts == null || counts.isEmpty())
        {
            return List.of();
        }

        Map<Long, User> usersById = new HashMap<>();
        for (User user : userRepository.findAllById(counts.keySet()))
        {
            usersById.put(user.getId(), user);
        }

        return counts.keySet().stream()
                .sorted()
                .map(userId -> {
                    User user = usersById.get(userId);
                    String nickname = user == null ? null : user.getNickname();
                    return new CircleChatPresenceMemberDTO(userId, nickname);
                })
                .sorted(Comparator.comparing(
                                CircleChatPresenceMemberDTO::getUserNickname,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(CircleChatPresenceMemberDTO::getUserId))
                .toList();
    }

    private void broadcastPresenceUpdate(Long circleId, List<CircleChatPresenceMemberDTO> snapshot)
    {
        eventBroadcaster.broadcastToCircleMembers(
                circleId,
                new CircleChatEventDTO(circleId, CircleChatEventType.PRESENCE_UPDATED, null, snapshot, null)
        );
    }

    private void cleanupSessionState(String sessionId)
    {
        Set<Long> joinedCircles = sessionCircleJoins.get(sessionId);
        if (joinedCircles != null && joinedCircles.isEmpty())
        {
            sessionCircleJoins.remove(sessionId);
        }

        Map<String, Long> subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null && subscriptions.isEmpty())
        {
            sessionSubscriptions.remove(sessionId);
        }

        if (!sessionCircleJoins.containsKey(sessionId) && !sessionSubscriptions.containsKey(sessionId))
        {
            sessionUserIds.remove(sessionId);
        }
    }
}
