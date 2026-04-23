package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.CircleChatSendRequestDTO;
import com.lexipal.core.dtos.responses.CircleChatEventDTO;
import com.lexipal.core.dtos.responses.CircleChatHistoryResponseDTO;
import com.lexipal.core.dtos.responses.CircleChatMessageDTO;
import com.lexipal.core.entities.BookCircle;
import com.lexipal.core.entities.CircleChatEventType;
import com.lexipal.core.entities.CircleChatMessage;
import com.lexipal.core.entities.CircleMember;
import com.lexipal.core.entities.CircleStatus;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.BookCircleRepository;
import com.lexipal.core.repositories.CircleChatMessageRepository;
import com.lexipal.core.repositories.CircleMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class BookCircleChatService implements BookCircleChatServiceInterface
{
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final CircleChatMessageRepository circleChatMessageRepository;
    private final BookCircleRepository bookCircleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final UserServiceInterface userService;
    private final BookCircleChatEventBroadcaster eventBroadcaster;
    private final BookCircleChatPresenceService presenceService;
    private final TransactionAfterCommitExecutor afterCommitExecutor;

    @Autowired
    public BookCircleChatService(final CircleChatMessageRepository circleChatMessageRepository,
                                 final BookCircleRepository bookCircleRepository,
                                 final CircleMemberRepository circleMemberRepository,
                                 final UserServiceInterface userService,
                                 final BookCircleChatEventBroadcaster eventBroadcaster,
                                 final BookCircleChatPresenceService presenceService,
                                 final TransactionAfterCommitExecutor afterCommitExecutor)
    {
        this.circleChatMessageRepository = circleChatMessageRepository;
        this.bookCircleRepository = bookCircleRepository;
        this.circleMemberRepository = circleMemberRepository;
        this.userService = userService;
        this.eventBroadcaster = eventBroadcaster;
        this.presenceService = presenceService;
        this.afterCommitExecutor = afterCommitExecutor;
    }

    @Override
    @Transactional(readOnly = true)
    public CircleChatHistoryResponseDTO getMessages(Long circleId, Long beforeMessageId, Integer limit, String keycloakSub)
    {
        requireActiveMember(circleId, keycloakSub);

        int resolvedLimit = resolveLimit(limit);
        Pageable pageable = PageRequest.of(0, resolvedLimit + 1, Sort.by(Sort.Direction.DESC, "id"));

        List<CircleChatMessage> fetchedMessages = beforeMessageId == null
                ? circleChatMessageRepository.findByCircle_Id(circleId, pageable)
                : circleChatMessageRepository.findByCircle_IdAndIdLessThan(circleId, beforeMessageId, pageable);

        boolean hasMore = fetchedMessages.size() > resolvedLimit;
        if (hasMore)
        {
            fetchedMessages = new ArrayList<>(fetchedMessages.subList(0, resolvedLimit));
        }

        List<CircleChatMessageDTO> messages = fetchedMessages.stream()
                .map(this::toMessageDTO)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        Collections.reverse(messages);

        Long nextBeforeMessageId = hasMore && !messages.isEmpty() ? messages.get(0).getId() : null;

        return new CircleChatHistoryResponseDTO(messages, hasMore, nextBeforeMessageId);
    }

    @Override
    public void sendMessage(Long circleId, CircleChatSendRequestDTO request, String keycloakSub)
    {
        ChatAccess access = requireActiveMember(circleId, keycloakSub);

        CircleChatMessage message = new CircleChatMessage();
        message.setCircle(access.circle());
        message.setSender(access.user());
        message.setContent(normalizeContent(request == null ? null : request.getContent()));

        CircleChatMessage savedMessage = circleChatMessageRepository.save(message);
        CircleChatMessageDTO dto = toMessageDTO(savedMessage);

        afterCommitExecutor.run(() -> eventBroadcaster.broadcastToCircleMembers(
                circleId,
                new CircleChatEventDTO(circleId, CircleChatEventType.MESSAGE_CREATED, dto, null, null)
        ));
    }

    @Override
    public void deleteMessage(Long circleId, Long messageId, String keycloakSub)
    {
        ChatAccess access = requireActiveMember(circleId, keycloakSub);

        CircleChatMessage message = circleChatMessageRepository.findByIdAndCircle_Id(messageId, circleId)
                .orElseThrow(() -> new ResourceNotFoundException("CircleChatMessage", "id", messageId));

        boolean callerIsSender = message.getSender().getId().equals(access.user().getId());
        if (!callerIsSender && !access.callerIsOwner())
        {
            throw new UnauthorizedActionException("Only the sender or circle owner can delete this message");
        }

        if (message.getDeletedAt() != null)
        {
            throw new IllegalArgumentException("This message has already been deleted.");
        }

        message.setDeletedAt(LocalDateTime.now());
        message.setDeletedBy(access.user());
        circleChatMessageRepository.save(message);

        CircleChatMessageDTO dto = toMessageDTO(message);

        afterCommitExecutor.run(() -> eventBroadcaster.broadcastToCircleMembers(
                circleId,
                new CircleChatEventDTO(circleId, CircleChatEventType.MESSAGE_DELETED, dto, null, null)
        ));
    }

    @Override
    public void joinPresence(Long circleId, String keycloakSub, String sessionId)
    {
        ChatAccess access = requireActiveMember(circleId, keycloakSub);
        presenceService.join(circleId, access.user(), sessionId);
    }

    @Override
    public void leavePresence(Long circleId, String keycloakSub, String sessionId)
    {
        ChatAccess access = requireActiveMember(circleId, keycloakSub);
        presenceService.leave(circleId, access.user(), sessionId);
    }

    @Override
    public void handleChatAccessRevoked(Long circleId, Long userId, String userSub, String reason)
    {
        String resolvedReason = reason == null || reason.isBlank()
                ? "You are no longer a member of this circle."
                : reason;

        afterCommitExecutor.run(() -> {
            presenceService.revokeUserAccess(circleId, userId);
            eventBroadcaster.sendToUser(
                    userSub,
                    circleId,
                    new CircleChatEventDTO(circleId, CircleChatEventType.CHAT_ACCESS_REVOKED, null, null, resolvedReason)
            );
        });
    }

    @Override
    public void handleCircleClosed(Long circleId, String reason)
    {
        String resolvedReason = reason == null || reason.isBlank()
                ? "This circle has been closed and chat is no longer available."
                : reason;

        afterCommitExecutor.run(() -> {
            presenceService.clearCircle(circleId);
            eventBroadcaster.broadcastToCircleMembers(
                    circleId,
                    new CircleChatEventDTO(circleId, CircleChatEventType.CHAT_CLOSED, null, null, resolvedReason)
            );
        });
    }

    private ChatAccess requireActiveMember(Long circleId, String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);

        BookCircle circle = bookCircleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("BookCircle", "id", circleId));

        if (circle.getStatus() != CircleStatus.ACTIVE)
        {
            throw new UnauthorizedActionException("Chat is unavailable because this circle is closed.");
        }

        CircleMember membership = circleMemberRepository.findByCircle_IdAndUser_Id(circleId, user.getId())
                .orElseThrow(() -> new UnauthorizedActionException("You are not a member of this circle"));

        boolean callerIsOwner = circle.getOwner().getId().equals(user.getId());
        return new ChatAccess(circle, user, membership, callerIsOwner);
    }

    private int resolveLimit(Integer limit)
    {
        if (limit == null)
        {
            return DEFAULT_LIMIT;
        }

        if (limit < 1 || limit > MAX_LIMIT)
        {
            throw new IllegalArgumentException("limit must be between 1 and 100.");
        }

        return limit;
    }

    private String normalizeContent(String content)
    {
        if (content == null)
        {
            throw new IllegalArgumentException("Message content is required.");
        }

        String normalized = content.trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException("Message content is required.");
        }

        if (normalized.length() > 2000)
        {
            throw new IllegalArgumentException("Message content must be 2000 characters or fewer.");
        }

        return normalized;
    }

    private CircleChatMessageDTO toMessageDTO(CircleChatMessage message)
    {
        boolean deleted = message.getDeletedAt() != null;

        return new CircleChatMessageDTO(
                message.getId(),
                message.getCircle().getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                deleted ? null : message.getContent(),
                deleted,
                message.getCreatedAt(),
                message.getDeletedAt()
        );
    }

    private record ChatAccess(BookCircle circle, User user, CircleMember membership, boolean callerIsOwner)
    {
    }
}
