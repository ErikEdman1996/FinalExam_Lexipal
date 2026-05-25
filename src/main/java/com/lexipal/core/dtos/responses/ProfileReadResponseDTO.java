package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;
import java.util.List;

public class ProfileReadResponseDTO
{
    private String status;
    private String nickname;
    private boolean needsOnboarding;
    private LocalDateTime createdAt;
    private ProfileStatsResponseDTO stats;
    private StreakMetaDataReadResponseDTO streak;
    private List<DailyActivityReadResponseDTO> recentDailyActivities;
    private PortraitResponseDTO selectedPortrait;
    private ThemeResponseDTO selectedTheme;
    private WalletResponseDTO wallet;
    private DailyQuestResponseDTO dailyQuest;

    public ProfileReadResponseDTO()
    {

    }

    public ProfileReadResponseDTO(String status, String nickname, boolean needsOnboarding,
                                  LocalDateTime createdAt, ProfileStatsResponseDTO stats,
                                  StreakMetaDataReadResponseDTO streak,
                                  List<DailyActivityReadResponseDTO> recentDailyActivities)
    {
        this(status, nickname, needsOnboarding, createdAt, stats, streak, recentDailyActivities,
                null, null, null, null);
    }

    public ProfileReadResponseDTO(String status, String nickname, boolean needsOnboarding,
                                  LocalDateTime createdAt, ProfileStatsResponseDTO stats,
                                  StreakMetaDataReadResponseDTO streak,
                                  List<DailyActivityReadResponseDTO> recentDailyActivities,
                                  PortraitResponseDTO selectedPortrait)
    {
        this(status, nickname, needsOnboarding, createdAt, stats, streak, recentDailyActivities,
                selectedPortrait, null, null, null);
    }

    public ProfileReadResponseDTO(String status, String nickname, boolean needsOnboarding,
                                  LocalDateTime createdAt, ProfileStatsResponseDTO stats,
                                  StreakMetaDataReadResponseDTO streak,
                                  List<DailyActivityReadResponseDTO> recentDailyActivities,
                                  PortraitResponseDTO selectedPortrait,
                                  WalletResponseDTO wallet)
    {
        this(status, nickname, needsOnboarding, createdAt, stats, streak, recentDailyActivities,
                selectedPortrait, null, wallet, null);
    }

    public ProfileReadResponseDTO(String status, String nickname, boolean needsOnboarding,
                                  LocalDateTime createdAt, ProfileStatsResponseDTO stats,
                                  StreakMetaDataReadResponseDTO streak,
                                  List<DailyActivityReadResponseDTO> recentDailyActivities,
                                  PortraitResponseDTO selectedPortrait,
                                  ThemeResponseDTO selectedTheme,
                                  WalletResponseDTO wallet)
    {
        this(status, nickname, needsOnboarding, createdAt, stats, streak, recentDailyActivities,
                selectedPortrait, selectedTheme, wallet, null);
    }

    public ProfileReadResponseDTO(String status, String nickname, boolean needsOnboarding,
                                  LocalDateTime createdAt, ProfileStatsResponseDTO stats,
                                  StreakMetaDataReadResponseDTO streak,
                                  List<DailyActivityReadResponseDTO> recentDailyActivities,
                                  PortraitResponseDTO selectedPortrait,
                                  ThemeResponseDTO selectedTheme,
                                  WalletResponseDTO wallet,
                                  DailyQuestResponseDTO dailyQuest)
    {
        this.status = status;
        this.nickname = nickname;
        this.needsOnboarding = needsOnboarding;
        this.createdAt = createdAt;
        this.stats = stats;
        this.streak = streak;
        this.recentDailyActivities = recentDailyActivities;
        this.selectedPortrait = selectedPortrait;
        this.selectedTheme = selectedTheme;
        this.wallet = wallet;
        this.dailyQuest = dailyQuest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isNeedsOnboarding() {
        return needsOnboarding;
    }

    public void setNeedsOnboarding(boolean needsOnboarding) {
        this.needsOnboarding = needsOnboarding;
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

    public PortraitResponseDTO getSelectedPortrait() {
        return selectedPortrait;
    }

    public void setSelectedPortrait(PortraitResponseDTO selectedPortrait) {
        this.selectedPortrait = selectedPortrait;
    }

    public ThemeResponseDTO getSelectedTheme() {
        return selectedTheme;
    }

    public void setSelectedTheme(ThemeResponseDTO selectedTheme) {
        this.selectedTheme = selectedTheme;
    }

    public WalletResponseDTO getWallet() {
        return wallet;
    }

    public void setWallet(WalletResponseDTO wallet) {
        this.wallet = wallet;
    }

    public DailyQuestResponseDTO getDailyQuest() {
        return dailyQuest;
    }

    public void setDailyQuest(DailyQuestResponseDTO dailyQuest) {
        this.dailyQuest = dailyQuest;
    }
}
