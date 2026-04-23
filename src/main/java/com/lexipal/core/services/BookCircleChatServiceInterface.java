package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.CircleChatSendRequestDTO;
import com.lexipal.core.dtos.responses.CircleChatHistoryResponseDTO;

public interface BookCircleChatServiceInterface
{
    CircleChatHistoryResponseDTO getMessages(Long circleId, Long beforeMessageId, Integer limit, String keycloakSub);

    void sendMessage(Long circleId, CircleChatSendRequestDTO request, String keycloakSub);

    void deleteMessage(Long circleId, Long messageId, String keycloakSub);

    void joinPresence(Long circleId, String keycloakSub, String sessionId);

    void leavePresence(Long circleId, String keycloakSub, String sessionId);

    void handleChatAccessRevoked(Long circleId, Long userId, String userSub, String reason);

    void handleCircleClosed(Long circleId, String reason);
}
