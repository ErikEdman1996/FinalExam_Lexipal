package com.lexipal.core.dtos.responses;

import com.lexipal.core.models.LibraryItemType;
import com.lexipal.core.models.LibraryProgressUnit;

import java.time.LocalDateTime;

public class LibraryItemResponseDTO
{
    private LibraryItemType itemType;
    private Long itemId;
    private String title;
    private String creatorName;
    private Integer completedUnits;
    private Integer totalUnits;
    private double progressPercentage;
    private LibraryProgressUnit progressUnit;
    private String status;
    private String coverImageUrl;
    private LocalDateTime addedAt;

    public LibraryItemResponseDTO()
    {

    }

    public LibraryItemResponseDTO(LibraryItemType itemType, Long itemId, String title, String creatorName,
                                  Integer completedUnits, Integer totalUnits, double progressPercentage,
                                  LibraryProgressUnit progressUnit, String status, String coverImageUrl,
                                  LocalDateTime addedAt)
    {
        this.itemType = itemType;
        this.itemId = itemId;
        this.title = title;
        this.creatorName = creatorName;
        this.completedUnits = completedUnits;
        this.totalUnits = totalUnits;
        this.progressPercentage = progressPercentage;
        this.progressUnit = progressUnit;
        this.status = status;
        this.coverImageUrl = coverImageUrl;
        this.addedAt = addedAt;
    }

    public LibraryItemType getItemType() { return itemType; }
    public void setItemType(LibraryItemType itemType) { this.itemType = itemType; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Integer getCompletedUnits() { return completedUnits; }
    public void setCompletedUnits(Integer completedUnits) { this.completedUnits = completedUnits; }

    public Integer getTotalUnits() { return totalUnits; }
    public void setTotalUnits(Integer totalUnits) { this.totalUnits = totalUnits; }

    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

    public LibraryProgressUnit getProgressUnit() { return progressUnit; }
    public void setProgressUnit(LibraryProgressUnit progressUnit) { this.progressUnit = progressUnit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
