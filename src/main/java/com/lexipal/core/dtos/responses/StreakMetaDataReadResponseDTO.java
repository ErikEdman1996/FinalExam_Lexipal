package com.lexipal.core.dtos.responses;

import java.time.LocalDate;

public class StreakMetaDataReadResponseDTO
{
    private Long id;
    private Long userId;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActivityDate;

    public StreakMetaDataReadResponseDTO()
    {

    }

    public StreakMetaDataReadResponseDTO(Long id, Long userId, Integer currentStreak, Integer longestStreak,
                                         LocalDate lastActivityDate)
    {
        this.id = id;
        this.userId = userId;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.lastActivityDate = lastActivityDate;
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
}
