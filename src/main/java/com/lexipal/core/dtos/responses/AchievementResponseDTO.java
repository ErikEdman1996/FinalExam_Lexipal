package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;

public class AchievementResponseDTO
{
    private String code;
    private String category;
    private String title;
    private String description;
    private Integer threshold;
    private Integer progress;
    private boolean unlocked;
    private LocalDateTime unlockedAt;
    private Integer sortOrder;

    public AchievementResponseDTO()
    {

    }

    public AchievementResponseDTO(String code, String category, String title, String description,
                                  Integer threshold, Integer progress, boolean unlocked,
                                  LocalDateTime unlockedAt, Integer sortOrder)
    {
        this.code = code;
        this.category = category;
        this.title = title;
        this.description = description;
        this.threshold = threshold;
        this.progress = progress;
        this.unlocked = unlocked;
        this.unlockedAt = unlockedAt;
        this.sortOrder = sortOrder;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getThreshold() { return threshold; }
    public void setThreshold(Integer threshold) { this.threshold = threshold; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
