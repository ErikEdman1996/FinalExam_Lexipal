package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public class StreakMetaDataCreateRequestDTO
{
    @Min(value = 0, message = "Current streak must be zero or greater")
    private Integer currentStreak;

    @Min(value = 0, message = "Longest streak must be zero or greater")
    private Integer longestStreak;

    private LocalDate lastActivityDate;

    public StreakMetaDataCreateRequestDTO()
    {

    }

    public StreakMetaDataCreateRequestDTO(Integer currentStreak, Integer longestStreak, LocalDate lastActivityDate)
    {
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.lastActivityDate = lastActivityDate;
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
