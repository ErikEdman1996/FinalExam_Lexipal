package com.lexipal.core.dtos.requests;

import com.lexipal.core.entities.DailyActivity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class DailyActivityCreateRequestDTO
{
    @NotNull(message = "Activity date is required")
    private LocalDate activityDate;

    @NotNull(message = "Activity type is required")
    private DailyActivity.ActivityType activityType;

    @NotNull(message = "XP earned is required")
    @Min(value = 0, message = "XP earned must be zero or greater")
    private Integer xpEarned;

    private Long sessionId;

    public DailyActivityCreateRequestDTO()
    {

    }

    public DailyActivityCreateRequestDTO(LocalDate activityDate, DailyActivity.ActivityType activityType,
                                         Integer xpEarned, Long sessionId)
    {
        this.activityDate = activityDate;
        this.activityType = activityType;
        this.xpEarned = xpEarned;
        this.sessionId = sessionId;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public DailyActivity.ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(DailyActivity.ActivityType activityType) {
        this.activityType = activityType;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
