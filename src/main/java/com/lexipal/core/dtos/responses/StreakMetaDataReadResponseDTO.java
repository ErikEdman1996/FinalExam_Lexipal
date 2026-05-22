package com.lexipal.core.dtos.responses;

import java.time.LocalDate;

public class StreakMetaDataReadResponseDTO
{
    private Long id;
    private Long userId;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActivityDate;
    private Integer availableStreakFreezes;
    private LocalDate freezeProtectedThroughDate;

    public StreakMetaDataReadResponseDTO()
    {

    }

    public StreakMetaDataReadResponseDTO(Long id, Long userId, Integer currentStreak, Integer longestStreak,
                                         LocalDate lastActivityDate)
    {
        this(id, userId, currentStreak, longestStreak, lastActivityDate, 0, null);
    }

    public StreakMetaDataReadResponseDTO(Long id, Long userId, Integer currentStreak, Integer longestStreak,
                                         LocalDate lastActivityDate, Integer availableStreakFreezes,
                                         LocalDate freezeProtectedThroughDate)
    {
        this.id = id;
        this.userId = userId;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.lastActivityDate = lastActivityDate;
        this.availableStreakFreezes = availableStreakFreezes;
        this.freezeProtectedThroughDate = freezeProtectedThroughDate;
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

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(LocalDate lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public Integer getAvailableStreakFreezes() {
        return availableStreakFreezes;
    }

    public void setAvailableStreakFreezes(Integer availableStreakFreezes) {
        this.availableStreakFreezes = availableStreakFreezes;
    }

    public LocalDate getFreezeProtectedThroughDate() {
        return freezeProtectedThroughDate;
    }

    public void setFreezeProtectedThroughDate(LocalDate freezeProtectedThroughDate) {
        this.freezeProtectedThroughDate = freezeProtectedThroughDate;
    }
}
