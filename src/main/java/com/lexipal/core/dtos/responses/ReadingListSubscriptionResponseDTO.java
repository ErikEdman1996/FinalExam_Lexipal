package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;

public class ReadingListSubscriptionResponseDTO
{
    private Long listId;
    private String listTitle;
    private String ownerNickname;
    private LocalDateTime subscribedAt;
    private LocalDateTime completedAt;
    private Integer slotsTotal;
    private Integer slotsCompleted;

    public ReadingListSubscriptionResponseDTO()
    {

    }

    public ReadingListSubscriptionResponseDTO(Long listId, String listTitle, String ownerNickname,
                                              LocalDateTime subscribedAt, LocalDateTime completedAt,
                                              Integer slotsTotal, Integer slotsCompleted)
    {
        this.listId = listId;
        this.listTitle = listTitle;
        this.ownerNickname = ownerNickname;
        this.subscribedAt = subscribedAt;
        this.completedAt = completedAt;
        this.slotsTotal = slotsTotal;
        this.slotsCompleted = slotsCompleted;
    }

    public Long getListId() { return listId; }
    public void setListId(Long listId) { this.listId = listId; }

    public String getListTitle() { return listTitle; }
    public void setListTitle(String listTitle) { this.listTitle = listTitle; }

    public String getOwnerNickname() { return ownerNickname; }
    public void setOwnerNickname(String ownerNickname) { this.ownerNickname = ownerNickname; }

    public LocalDateTime getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(LocalDateTime subscribedAt) { this.subscribedAt = subscribedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getSlotsTotal() { return slotsTotal; }
    public void setSlotsTotal(Integer slotsTotal) { this.slotsTotal = slotsTotal; }

    public Integer getSlotsCompleted() { return slotsCompleted; }
    public void setSlotsCompleted(Integer slotsCompleted) { this.slotsCompleted = slotsCompleted; }
}
