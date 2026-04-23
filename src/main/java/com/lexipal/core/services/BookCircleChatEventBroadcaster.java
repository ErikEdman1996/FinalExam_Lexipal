package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.CircleChatEventDTO;
import com.lexipal.core.repositories.CircleMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class BookCircleChatEventBroadcaster
{
    private final SimpMessagingTemplate messagingTemplate;
    private final CircleMemberRepository circleMemberRepository;

    @Autowired
    public BookCircleChatEventBroadcaster(final SimpMessagingTemplate messagingTemplate,
                                          final CircleMemberRepository circleMemberRepository)
    {
        this.messagingTemplate = messagingTemplate;
        this.circleMemberRepository = circleMemberRepository;
    }

    @Transactional(readOnly = true)
    public void broadcastToCircleMembers(Long circleId, CircleChatEventDTO event)
    {
        List<String> userSubs = circleMemberRepository.findByCircle_Id(circleId).stream()
                .map(member -> member.getUser().getKeycloakSub())
                .distinct()
                .toList();

        broadcastToUserSubs(circleId, userSubs, event);
    }

    public void broadcastToUserSubs(Long circleId, Collection<String> userSubs, CircleChatEventDTO event)
    {
        for (String userSub : new LinkedHashSet<>(userSubs))
        {
            sendToUser(userSub, circleId, event);
        }
    }

    public void sendToUser(String userSub, Long circleId, CircleChatEventDTO event)
    {
        messagingTemplate.convertAndSendToUser(
                userSub,
                "/queue/book-circles/" + circleId + "/events",
                event
        );
    }
}
