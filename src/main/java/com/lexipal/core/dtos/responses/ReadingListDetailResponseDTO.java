package com.lexipal.core.dtos.responses;

import com.lexipal.core.entities.ReadingListStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ReadingListDetailResponseDTO
{
    private Long id;
    private String title;
    private String description;
    private String ownerNickname;
    private Long ownerUserId;
    private ReadingListStatus status;
    private List<String> genres;
    private Integer starCount;
    private boolean callerIsOwner;
    private boolean hasStarred;
    private boolean isSubscribed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReadingListSlotResponseDTO> slots;

    public ReadingListDetailResponseDTO()
    {

    }

    public ReadingListDetailResponseDTO(Long id, String title, String description, String ownerNickname,
                                        Long ownerUserId, ReadingListStatus status, Integer starCount,
                                        boolean callerIsOwner, boolean hasStarred, boolean isSubscribed,
                                        LocalDateTime createdAt, LocalDateTime updatedAt,
                                        List<ReadingListSlotResponseDTO> slots)
    {
        this(id, title, description, ownerNickname, ownerUserId, status, null, starCount, callerIsOwner,
                hasStarred, isSubscribed, createdAt, updatedAt, slots);
    }

    public ReadingListDetailResponseDTO(Long id, String title, String description, String ownerNickname,
                                        Long ownerUserId, ReadingListStatus status, List<String> genres,
                                        Integer starCount, boolean callerIsOwner, boolean hasStarred,
                                        boolean isSubscribed, LocalDateTime createdAt, LocalDateTime updatedAt,
                                        List<ReadingListSlotResponseDTO> slots)
    {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerNickname = ownerNickname;
        this.ownerUserId = ownerUserId;
        this.status = status;
        this.genres = genres;
        this.starCount = starCount;
        this.callerIsOwner = callerIsOwner;
        this.hasStarred = hasStarred;
        this.isSubscribed = isSubscribed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.slots = slots;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerNickname() { return ownerNickname; }
    public void setOwnerNickname(String ownerNickname) { this.ownerNickname = ownerNickname; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public ReadingListStatus getStatus() { return status; }
    public void setStatus(ReadingListStatus status) { this.status = status; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public Integer getStarCount() { return starCount; }
    public void setStarCount(Integer starCount) { this.starCount = starCount; }

    public boolean isCallerIsOwner() { return callerIsOwner; }
    public void setCallerIsOwner(boolean callerIsOwner) { this.callerIsOwner = callerIsOwner; }

    public boolean isHasStarred() { return hasStarred; }
    public void setHasStarred(boolean hasStarred) { this.hasStarred = hasStarred; }

    public boolean isIsSubscribed() { return isSubscribed; }
    public void setIsSubscribed(boolean isSubscribed) { this.isSubscribed = isSubscribed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ReadingListSlotResponseDTO> getSlots() { return slots; }
    public void setSlots(List<ReadingListSlotResponseDTO> slots) { this.slots = slots; }
}
