package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;
import java.util.List;

public class ProfileReadResponseDTO
{
    private String status;
    private LocalDateTime createdAt;
    private ProfileStatsResponseDTO stats;
    private StreakMetaDataReadResponseDTO streak;
    private List<DailyActivityReadResponseDTO> recentDailyActivities;

    public ProfileReadResponseDTO()
    {

    }

    public ProfileReadResponseDTO(String status, LocalDateTime createdAt, ProfileStatsResponseDTO stats,
                                  StreakMetaDataReadResponseDTO streak,
                                  List<DailyActivityReadResponseDTO> recentDailyActivities)
    {
        this.status = status;
        this.createdAt = createdAt;
        this.stats = stats;
        this.streak = streak;
        this.recentDailyActivities = recentDailyActivities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ProfileStatsResponseDTO getStats() {
        return stats;
    }

    public void setStats(ProfileStatsResponseDTO stats) {
        this.stats = stats;
    }

    public StreakMetaDataReadResponseDTO getStreak() {
        return streak;
    }

    public void setStreak(StreakMetaDataReadResponseDTO streak) {
        this.streak = streak;
    }

    public List<DailyActivityReadResponseDTO> getRecentDailyActivities() {
        return recentDailyActivities;
    }

    public void setRecentDailyActivities(List<DailyActivityReadResponseDTO> recentDailyActivities) {
        this.recentDailyActivities = recentDailyActivities;
    }
}
