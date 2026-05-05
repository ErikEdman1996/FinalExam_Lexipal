package com.lexipal.core.dtos.responses;

public class XpUpdateResponseDTO
{
    private Integer xpAwarded;
    private Integer previousTotalXp;
    private Integer newTotalXp;
    private Integer level;
    private Integer xpIntoLevel;
    private Integer xpPerLevel;
    private Integer maxLevel;
    private Integer currentLevelMinXp;
    private Integer nextLevelMinXp;
    private Integer xpToNextLevel;
    private boolean leveledUp;

    public XpUpdateResponseDTO()
    {

    }

    public XpUpdateResponseDTO(Integer xpAwarded, Integer previousTotalXp, Integer newTotalXp, Integer level,
                               Integer xpIntoLevel, Integer xpPerLevel, boolean leveledUp)
    {
        this(xpAwarded, previousTotalXp, newTotalXp, level, xpIntoLevel, xpPerLevel, leveledUp,
                null, null, null, null);
    }

    public XpUpdateResponseDTO(Integer xpAwarded, Integer previousTotalXp, Integer newTotalXp, Integer level,
                               Integer xpIntoLevel, Integer xpPerLevel, boolean leveledUp, Integer maxLevel,
                               Integer currentLevelMinXp, Integer nextLevelMinXp, Integer xpToNextLevel)
    {
        this.xpAwarded = xpAwarded;
        this.previousTotalXp = previousTotalXp;
        this.newTotalXp = newTotalXp;
        this.level = level;
        this.xpIntoLevel = xpIntoLevel;
        this.xpPerLevel = xpPerLevel;
        this.maxLevel = maxLevel;
        this.currentLevelMinXp = currentLevelMinXp;
        this.nextLevelMinXp = nextLevelMinXp;
        this.xpToNextLevel = xpToNextLevel;
        this.leveledUp = leveledUp;
    }

    public Integer getXpAwarded()
    {
        return xpAwarded;
    }

    public void setXpAwarded(Integer xpAwarded)
    {
        this.xpAwarded = xpAwarded;
    }

    public Integer getPreviousTotalXp()
    {
        return previousTotalXp;
    }

    public void setPreviousTotalXp(Integer previousTotalXp)
    {
        this.previousTotalXp = previousTotalXp;
    }

    public Integer getNewTotalXp()
    {
        return newTotalXp;
    }

    public void setNewTotalXp(Integer newTotalXp)
    {
        this.newTotalXp = newTotalXp;
    }

    public Integer getLevel()
    {
        return level;
    }

    public void setLevel(Integer level)
    {
        this.level = level;
    }

    public Integer getXpIntoLevel()
    {
        return xpIntoLevel;
    }

    public void setXpIntoLevel(Integer xpIntoLevel)
    {
        this.xpIntoLevel = xpIntoLevel;
    }

    public Integer getXpPerLevel()
    {
        return xpPerLevel;
    }

    public void setXpPerLevel(Integer xpPerLevel)
    {
        this.xpPerLevel = xpPerLevel;
    }

    public Integer getMaxLevel()
    {
        return maxLevel;
    }

    public void setMaxLevel(Integer maxLevel)
    {
        this.maxLevel = maxLevel;
    }

    public Integer getCurrentLevelMinXp()
    {
        return currentLevelMinXp;
    }

    public void setCurrentLevelMinXp(Integer currentLevelMinXp)
    {
        this.currentLevelMinXp = currentLevelMinXp;
    }

    public Integer getNextLevelMinXp()
    {
        return nextLevelMinXp;
    }

    public void setNextLevelMinXp(Integer nextLevelMinXp)
    {
        this.nextLevelMinXp = nextLevelMinXp;
    }

    public Integer getXpToNextLevel()
    {
        return xpToNextLevel;
    }

    public void setXpToNextLevel(Integer xpToNextLevel)
    {
        this.xpToNextLevel = xpToNextLevel;
    }

    public boolean isLeveledUp()
    {
        return leveledUp;
    }

    public void setLeveledUp(boolean leveledUp)
    {
        this.leveledUp = leveledUp;
    }
}
