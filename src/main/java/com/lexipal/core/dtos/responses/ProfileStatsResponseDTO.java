package com.lexipal.core.dtos.responses;

public class ProfileStatsResponseDTO
{
    private Integer totalPagesRead;
    private Integer totalBooksRead;
    private Integer totalSessionsCompleted;
    private Integer totalCapturedWords;
    private Integer totalXpEarned;

    public ProfileStatsResponseDTO()
    {

    }

    public ProfileStatsResponseDTO(Integer totalPagesRead, Integer totalBooksRead, Integer totalSessionsCompleted,
                                   Integer totalCapturedWords, Integer totalXpEarned)
    {
        this.totalPagesRead = totalPagesRead;
        this.totalBooksRead = totalBooksRead;
        this.totalSessionsCompleted = totalSessionsCompleted;
        this.totalCapturedWords = totalCapturedWords;
        this.totalXpEarned = totalXpEarned;
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
}
