package com.lexipal.core.dtos.responses;

public class ProfileStatsResponseDTO
{
    private Long totalMinutesRead;
    private Integer totalPagesRead;
    private Integer totalBooksRead;
    private Integer totalSessionsCompleted;
    private Integer totalCapturedWords;
    private Integer totalXpEarned;
    private Integer level;
    private Integer xpIntoLevel;
    private Integer xpPerLevel;
    private Integer maxLevel;
    private Integer currentLevelMinXp;
    private Integer nextLevelMinXp;
    private Integer xpToNextLevel;

    public ProfileStatsResponseDTO()
    {

    }

    public ProfileStatsResponseDTO(Long totalMinutesRead, Integer totalPagesRead, Integer totalBooksRead, Integer totalSessionsCompleted,
                                   Integer totalCapturedWords, Integer totalXpEarned)
    {
        this(totalMinutesRead, totalPagesRead, totalBooksRead, totalSessionsCompleted, totalCapturedWords, totalXpEarned,
                null, null, null, null, null, null, null);
    }

    public ProfileStatsResponseDTO(Long totalMinutesRead, Integer totalPagesRead, Integer totalBooksRead, Integer totalSessionsCompleted,
                                   Integer totalCapturedWords, Integer totalXpEarned, Integer level,
                                   Integer xpIntoLevel, Integer xpPerLevel, Integer maxLevel,
                                   Integer currentLevelMinXp, Integer nextLevelMinXp, Integer xpToNextLevel)
    {
        this.totalMinutesRead = totalMinutesRead;
        this.totalPagesRead = totalPagesRead;
        this.totalBooksRead = totalBooksRead;
        this.totalSessionsCompleted = totalSessionsCompleted;
        this.totalCapturedWords = totalCapturedWords;
        this.totalXpEarned = totalXpEarned;
        this.level = level;
        this.xpIntoLevel = xpIntoLevel;
        this.xpPerLevel = xpPerLevel;
        this.maxLevel = maxLevel;
        this.currentLevelMinXp = currentLevelMinXp;
        this.nextLevelMinXp = nextLevelMinXp;
        this.xpToNextLevel = xpToNextLevel;
    }

    public Long getTotalMinutesRead() {
        return totalMinutesRead;
    }

    public void setTotalMinutesRead(Long totalMinutesRead) {
        this.totalMinutesRead = totalMinutesRead;
    }

    public Integer getTotalPagesRead() {
        return totalPagesRead;
    }

    public void setTotalPagesRead(Integer totalPagesRead) {
        this.totalPagesRead = totalPagesRead;
    }

    public Integer getTotalBooksRead() {
        return totalBooksRead;
    }

    public void setTotalBooksRead(Integer totalBooksRead) {
        this.totalBooksRead = totalBooksRead;
    }

    public Integer getTotalSessionsCompleted() {
        return totalSessionsCompleted;
    }

    public void setTotalSessionsCompleted(Integer totalSessionsCompleted) {
        this.totalSessionsCompleted = totalSessionsCompleted;
    }

    public Integer getTotalCapturedWords() {
        return totalCapturedWords;
    }

    public void setTotalCapturedWords(Integer totalCapturedWords) {
        this.totalCapturedWords = totalCapturedWords;
    }

    public Integer getTotalXpEarned() {
        return totalXpEarned;
    }

    public void setTotalXpEarned(Integer totalXpEarned) {
        this.totalXpEarned = totalXpEarned;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getXpIntoLevel() {
        return xpIntoLevel;
    }

    public void setXpIntoLevel(Integer xpIntoLevel) {
        this.xpIntoLevel = xpIntoLevel;
    }

    public Integer getXpPerLevel() {
        return xpPerLevel;
    }

    public void setXpPerLevel(Integer xpPerLevel) {
        this.xpPerLevel = xpPerLevel;
    }

    public Integer getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(Integer maxLevel) {
        this.maxLevel = maxLevel;
    }

    public Integer getCurrentLevelMinXp() {
        return currentLevelMinXp;
    }

    public void setCurrentLevelMinXp(Integer currentLevelMinXp) {
        this.currentLevelMinXp = currentLevelMinXp;
    }

    public Integer getNextLevelMinXp() {
        return nextLevelMinXp;
    }

    public void setNextLevelMinXp(Integer nextLevelMinXp) {
        this.nextLevelMinXp = nextLevelMinXp;
    }

    public Integer getXpToNextLevel() {
        return xpToNextLevel;
    }

    public void setXpToNextLevel(Integer xpToNextLevel) {
        this.xpToNextLevel = xpToNextLevel;
    }
}
