package com.lexipal.core.dtos.responses;

import java.time.LocalDate;

public class DailyActivityReadResponseDTO
{
    private Long id;
    private Long userId;
    private Long sessionId;
    private LocalDate activityDate;
    private String activityType;
    private Integer xpEarned;

    public DailyActivityReadResponseDTO()
    {

    }

    public DailyActivityReadResponseDTO(Long id, Long userId, Long sessionId, LocalDate activityDate,
                                        String activityType, Integer xpEarned)
    {
        this.id = id;
        this.userId = userId;
        this.sessionId = sessionId;
        this.activityDate = activityDate;
        this.activityType = activityType;
        this.xpEarned = xpEarned;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }
}
