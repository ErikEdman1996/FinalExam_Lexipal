package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.CircleChatSendRequestDTO;
import com.lexipal.core.services.BookCircleChatServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class BookCircleChatWebSocketController
{
    private final BookCircleChatServiceInterface bookCircleChatService;

    @Autowired
    public BookCircleChatWebSocketController(final BookCircleChatServiceInterface bookCircleChatService)
    {
        this.bookCircleChatService = bookCircleChatService;
    }

    @MessageMapping("/book-circles/{circleId}/messages")
    public void sendMessage(@DestinationVariable Long circleId,
                            @Payload CircleChatSendRequestDTO request,
                            Principal principal)
    {
        bookCircleChatService.sendMessage(circleId, request, requirePrincipalName(principal));
    }

    @MessageMapping("/book-circles/{circleId}/presence.join")
    public void joinPresence(@DestinationVariable Long circleId,
                             @Header("simpSessionId") String sessionId,
                             Principal principal)
    {
        bookCircleChatService.joinPresence(circleId, requirePrincipalName(principal), sessionId);
    }

    @MessageMapping("/book-circles/{circleId}/presence.leave")
    public void leavePresence(@DestinationVariable Long circleId,
                              @Header("simpSessionId") String sessionId,
                              Principal principal)
    {
        bookCircleChatService.leavePresence(circleId, requirePrincipalName(principal), sessionId);
    }

    private String requirePrincipalName(Principal principal)
    {
        if (principal == null || principal.getName() == null || principal.getName().isBlank())
        {
            throw new IllegalArgumentException("Authenticated user subject is required.");
        }

        return principal.getName();
    }
}
