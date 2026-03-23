package com.lexipal.core.dtos;

public class CaptureRequestDTO
{
    private String lemma;
    private Long userId;
    private Long sessionId;

    public CaptureRequestDTO()
    {

    }

    public CaptureRequestDTO(String lemma, Long userId, Long sessionId)
    {
        this.lemma = lemma;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
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
}
