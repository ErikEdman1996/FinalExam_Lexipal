package com.lexipal.core.dtos.responses;

import com.lexipal.core.entities.CircleChatEventType;

import java.util.List;

public class CircleChatEventDTO
{
    private Long circleId;
    private CircleChatEventType type;
    private CircleChatMessageDTO message;
    private List<CircleChatPresenceMemberDTO> onlineMembers;
    private String reason;

    public CircleChatEventDTO()
    {

    }

    public CircleChatEventDTO(Long circleId, CircleChatEventType type, CircleChatMessageDTO message,
                              List<CircleChatPresenceMemberDTO> onlineMembers, String reason)
    {
        this.circleId = circleId;
        this.type = type;
        this.message = message;
        this.onlineMembers = onlineMembers;
        this.reason = reason;
    }

    public Long getCircleId()
    {
        return circleId;
    }

    public void setCircleId(Long circleId)
    {
        this.circleId = circleId;
    }

    public CircleChatEventType getType()
    {
        return type;
    }

    public void setType(CircleChatEventType type)
    {
        this.type = type;
    }

    public CircleChatMessageDTO getMessage()
    {
        return message;
    }

    public void setMessage(CircleChatMessageDTO message)
    {
        this.message = message;
    }

    public List<CircleChatPresenceMemberDTO> getOnlineMembers()
    {
        return onlineMembers;
    }

    public void setOnlineMembers(List<CircleChatPresenceMemberDTO> onlineMembers)
    {
        this.onlineMembers = onlineMembers;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
