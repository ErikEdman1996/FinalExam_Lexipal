package com.lexipal.core.dtos.responses;

public class XpUpdateResponseDTO
{
    private Integer xpAwarded;
    private Integer previousTotalXp;
    private Integer newTotalXp;
    private Integer level;
    private Integer xpIntoLevel;
    private Integer xpPerLevel;
    private boolean leveledUp;

    public XpUpdateResponseDTO()
    {

    }

    public XpUpdateResponseDTO(Integer xpAwarded, Integer previousTotalXp, Integer newTotalXp, Integer level,
                               Integer xpIntoLevel, Integer xpPerLevel, boolean leveledUp)
    {
        this.xpAwarded = xpAwarded;
        this.previousTotalXp = previousTotalXp;
        this.newTotalXp = newTotalXp;
        this.level = level;
        this.xpIntoLevel = xpIntoLevel;
        this.xpPerLevel = xpPerLevel;
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

    public boolean isLeveledUp()
    {
        return leveledUp;
    }

    public void setLeveledUp(boolean leveledUp)
    {
        this.leveledUp = leveledUp;
    }
}
