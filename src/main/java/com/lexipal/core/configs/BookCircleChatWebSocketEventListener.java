package com.lexipal.core.configs;

import com.lexipal.core.services.BookCircleChatPresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BookCircleChatWebSocketEventListener
{
    private static final Pattern CHAT_DESTINATION_PATTERN =
            Pattern.compile("^/user/queue/book-circles/(\\d+)/events$");

    private final BookCircleChatPresenceService presenceService;

    @Autowired
    public BookCircleChatWebSocketEventListener(final BookCircleChatPresenceService presenceService)
    {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();
        String destination = accessor.getDestination();

        if (sessionId == null || subscriptionId == null || destination == null)
        {
            return;
        }

        Matcher matcher = CHAT_DESTINATION_PATTERN.matcher(destination);
        if (!matcher.matches())
        {
            return;
        }

        presenceService.registerSubscription(sessionId, subscriptionId, Long.parseLong(matcher.group(1)));
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getSessionId() == null || accessor.getSubscriptionId() == null)
        {
            return;
        }

        presenceService.unregisterSubscription(accessor.getSessionId(), accessor.getSubscriptionId());
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getSessionId() == null)
        {
            return;
        }

        presenceService.handleDisconnect(accessor.getSessionId());
    }
}
