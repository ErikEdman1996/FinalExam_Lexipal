package com.lexipal.core.dtos.responses;

import java.util.List;

public class CircleChatHistoryResponseDTO
{
    private List<CircleChatMessageDTO> messages;
    private boolean hasMore;
    private Long nextBeforeMessageId;

    public CircleChatHistoryResponseDTO()
    {

    }

    public CircleChatHistoryResponseDTO(List<CircleChatMessageDTO> messages, boolean hasMore, Long nextBeforeMessageId)
    {
        this.messages = messages;
        this.hasMore = hasMore;
        this.nextBeforeMessageId = nextBeforeMessageId;
    }

    public List<CircleChatMessageDTO> getMessages()
    {
        return messages;
    }

    public void setMessages(List<CircleChatMessageDTO> messages)
    {
        this.messages = messages;
    }

    public boolean isHasMore()
    {
        return hasMore;
    }

    public void setHasMore(boolean hasMore)
    {
        this.hasMore = hasMore;
    }

    public Long getNextBeforeMessageId()
    {
        return nextBeforeMessageId;
    }

    public void setNextBeforeMessageId(Long nextBeforeMessageId)
    {
        this.nextBeforeMessageId = nextBeforeMessageId;
    }
}
