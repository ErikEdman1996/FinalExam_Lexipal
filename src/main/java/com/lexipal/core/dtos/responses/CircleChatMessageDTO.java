package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;

public class CircleChatMessageDTO
{
    private Long id;
    private Long circleId;
    private Long senderUserId;
    private String senderNickname;
    private String content;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public CircleChatMessageDTO()
    {

    }

    public CircleChatMessageDTO(Long id, Long circleId, Long senderUserId, String senderNickname,
                                String content, boolean deleted, LocalDateTime createdAt,
                                LocalDateTime deletedAt)
    {
        this.id = id;
        this.circleId = circleId;
        this.senderUserId = senderUserId;
        this.senderNickname = senderNickname;
        this.content = content;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getCircleId()
    {
        return circleId;
    }

    public void setCircleId(Long circleId)
    {
        this.circleId = circleId;
    }

    public Long getSenderUserId()
    {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId)
    {
        this.senderUserId = senderUserId;
    }

    public String getSenderNickname()
    {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname)
    {
        this.senderNickname = senderNickname;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public boolean isDeleted()
    {
        return deleted;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeletedAt()
    {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt)
    {
        this.deletedAt = deletedAt;
    }
}
