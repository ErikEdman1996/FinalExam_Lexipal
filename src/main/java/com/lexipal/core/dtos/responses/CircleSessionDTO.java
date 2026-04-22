package com.lexipal.core.dtos.responses;

import com.lexipal.core.entities.Session;

import java.time.LocalDateTime;

public class CircleSessionDTO
{
    private Long sessionId;
    private LocalDateTime sessionDate;
    private LocalDateTime completedAt;
    private Long durationSeconds;
    private Integer startPage;
    private Integer endPage;
    private Integer pagesRead;
    private Session.SessionStatus status;
    private String reflection;
    private Integer capturedWordCount;

    public CircleSessionDTO()
    {

    }

    public CircleSessionDTO(Long sessionId, LocalDateTime sessionDate, LocalDateTime completedAt,
                            Long durationSeconds, Integer startPage, Integer endPage, Integer pagesRead,
                            Session.SessionStatus status, String reflection, Integer capturedWordCount)
    {
        this.sessionId = sessionId;
        this.sessionDate = sessionDate;
        this.completedAt = completedAt;
        this.durationSeconds = durationSeconds;
        this.startPage = startPage;
        this.endPage = endPage;
        this.pagesRead = pagesRead;
        this.status = status;
        this.reflection = reflection;
        this.capturedWordCount = capturedWordCount;
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDateTime sessionDate) { this.sessionDate = sessionDate; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public Integer getStartPage() { return startPage; }
    public void setStartPage(Integer startPage) { this.startPage = startPage; }

    public Integer getEndPage() { return endPage; }
    public void setEndPage(Integer endPage) { this.endPage = endPage; }

    public Integer getPagesRead() { return pagesRead; }
    public void setPagesRead(Integer pagesRead) { this.pagesRead = pagesRead; }

    public Session.SessionStatus getStatus() { return status; }
    public void setStatus(Session.SessionStatus status) { this.status = status; }

    public String getReflection() { return reflection; }
    public void setReflection(String reflection) { this.reflection = reflection; }

    public Integer getCapturedWordCount() { return capturedWordCount; }
    public void setCapturedWordCount(Integer capturedWordCount) { this.capturedWordCount = capturedWordCount; }
}
