package com.lexipal.core.dtos.responses;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailyQuestResponseDTO
{
    private Long id;
    private LocalDate questDate;
    private String type;
    private String title;
    private String description;
    private Integer targetValue;
    private Integer progressValue;
    private String unit;
    private String status;
    private Integer xpReward;
    private Integer coinReward;
    private LocalDateTime completedAt;

    public DailyQuestResponseDTO()
    {

    }

    public DailyQuestResponseDTO(Long id, LocalDate questDate, String type, String title, String description,
                                 Integer targetValue, Integer progressValue, String unit, String status,
                                 Integer xpReward, Integer coinReward, LocalDateTime completedAt)
    {
        this.id = id;
        this.questDate = questDate;
        this.type = type;
        this.title = title;
        this.description = description;
        this.targetValue = targetValue;
        this.progressValue = progressValue;
        this.unit = unit;
        this.status = status;
        this.xpReward = xpReward;
        this.coinReward = coinReward;
        this.completedAt = completedAt;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public LocalDate getQuestDate()
    {
        return questDate;
    }

    public void setQuestDate(LocalDate questDate)
    {
        this.questDate = questDate;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Integer getTargetValue()
    {
        return targetValue;
    }

    public void setTargetValue(Integer targetValue)
    {
        this.targetValue = targetValue;
    }

    public Integer getProgressValue()
    {
        return progressValue;
    }

    public void setProgressValue(Integer progressValue)
    {
        this.progressValue = progressValue;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getXpReward()
    {
        return xpReward;
    }

    public void setXpReward(Integer xpReward)
    {
        this.xpReward = xpReward;
    }

    public Integer getCoinReward()
    {
        return coinReward;
    }

    public void setCoinReward(Integer coinReward)
    {
        this.coinReward = coinReward;
    }

    public LocalDateTime getCompletedAt()
    {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt)
    {
        this.completedAt = completedAt;
    }
}
