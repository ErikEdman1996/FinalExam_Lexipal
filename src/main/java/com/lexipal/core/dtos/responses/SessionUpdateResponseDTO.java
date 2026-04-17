package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;

public class SessionUpdateResponseDTO
{
    private Long id;
    private Long bookId;
    private Long durationSeconds;
    private Integer startPage;
    private Integer endPage;
    private Integer pagesRead;
    private String reflection;
    private String status;
    private LocalDateTime sessionDate;
    private LocalDateTime completedAt;
    private int capturedWordCount;
    private XpUpdateResponseDTO xpUpdate;

    public SessionUpdateResponseDTO()
    {

    }

    public SessionUpdateResponseDTO(Long id, Long bookId, Long durationSeconds, Integer startPage, Integer endPage,
                                    Integer pagesRead, String reflection, String status, LocalDateTime sessionDate,
                                    LocalDateTime completedAt, int capturedWordCount, XpUpdateResponseDTO xpUpdate)
    {
        this.id = id;
        this.bookId = bookId;
        this.durationSeconds = durationSeconds;
        this.startPage = startPage;
        this.endPage = endPage;
        this.pagesRead = pagesRead;
        this.reflection = reflection;
        this.status = status;
        this.sessionDate = sessionDate;
        this.completedAt = completedAt;
        this.capturedWordCount = capturedWordCount;
        this.xpUpdate = xpUpdate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }

    public Integer getEndPage() {
        return endPage;
    }

    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
    }

    public Integer getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(Integer pagesRead) {
        this.pagesRead = pagesRead;
    }

    public String getReflection() {
        return reflection;
    }

    public void setReflection(String reflection) {
        this.reflection = reflection;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalDateTime getCompletedAt()
    {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt)
    {
        this.completedAt = completedAt;
    }

    public int getCapturedWordCount() {
        return capturedWordCount;
    }

    public void setCapturedWordCount(int capturedWordCount) {
        this.capturedWordCount = capturedWordCount;
    }

    public XpUpdateResponseDTO getXpUpdate()
    {
        return xpUpdate;
    }

    public void setXpUpdate(XpUpdateResponseDTO xpUpdate)
    {
        this.xpUpdate = xpUpdate;
    }
}
