package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;

public class PortraitResponseDTO
{
    private String code;
    private String name;
    private String description;
    private String assetKey;
    private Integer requiredLevel;
    private Integer sortOrder;
    private boolean unlocked;
    private boolean selected;
    private LocalDateTime unlockedAt;

    public PortraitResponseDTO()
    {

    }

    public PortraitResponseDTO(String code, String name, String description, String assetKey,
                               Integer requiredLevel, Integer sortOrder, boolean unlocked,
                               boolean selected, LocalDateTime unlockedAt)
    {
        this.code = code;
        this.name = name;
        this.description = description;
        this.assetKey = assetKey;
        this.requiredLevel = requiredLevel;
        this.sortOrder = sortOrder;
        this.unlocked = unlocked;
        this.selected = selected;
        this.unlockedAt = unlockedAt;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAssetKey() { return assetKey; }
    public void setAssetKey(String assetKey) { this.assetKey = assetKey; }
    public Integer getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(Integer requiredLevel) { this.requiredLevel = requiredLevel; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
}
