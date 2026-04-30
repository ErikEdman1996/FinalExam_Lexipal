package com.lexipal.core.dtos.responses;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class CrosswordPuzzleReadResponseDTO
{
    private Long id;
    private Long challengeId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private JsonNode puzzle;

    public CrosswordPuzzleReadResponseDTO()
    {

    }

    public CrosswordPuzzleReadResponseDTO(Long id, Long challengeId, String status, LocalDateTime createdAt,
                                          LocalDateTime submittedAt, JsonNode puzzle)
    {
        this.id = id;
        this.challengeId = challengeId;
        this.status = status;
        this.createdAt = createdAt;
        this.submittedAt = submittedAt;
        this.puzzle = puzzle;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getChallengeId()
    {
        return challengeId;
    }

    public void setChallengeId(Long challengeId)
    {
        this.challengeId = challengeId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSubmittedAt()
    {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt)
    {
        this.submittedAt = submittedAt;
    }

    public JsonNode getPuzzle()
    {
        return puzzle;
    }

    public void setPuzzle(JsonNode puzzle)
    {
        this.puzzle = puzzle;
    }
}
