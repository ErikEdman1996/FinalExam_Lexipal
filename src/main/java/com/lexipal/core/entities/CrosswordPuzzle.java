package com.lexipal.core.entities;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "crossword_puzzles")
public class CrosswordPuzzle
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CrosswordPuzzleStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime submittedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "public_puzzle", columnDefinition = "jsonb", nullable = false)
    private JsonNode publicPuzzle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "solution", columnDefinition = "jsonb", nullable = false)
    private JsonNode solution;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "word_sources", columnDefinition = "jsonb", nullable = false)
    private JsonNode wordSources;

    public CrosswordPuzzle()
    {

    }

    @PrePersist
    protected void onCreate()
    {
        if (this.createdAt == null)
        {
            this.createdAt = LocalDateTime.now();
        }

        if (this.status == null)
        {
            this.status = CrosswordPuzzleStatus.ACTIVE;
        }
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public Challenge getChallenge()
    {
        return challenge;
    }

    public void setChallenge(Challenge challenge)
    {
        this.challenge = challenge;
    }

    public CrosswordPuzzleStatus getStatus()
    {
        return status;
    }

    public void setStatus(CrosswordPuzzleStatus status)
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

    public JsonNode getPublicPuzzle()
    {
        return publicPuzzle;
    }

    public void setPublicPuzzle(JsonNode publicPuzzle)
    {
        this.publicPuzzle = publicPuzzle;
    }

    public JsonNode getSolution()
    {
        return solution;
    }

    public void setSolution(JsonNode solution)
    {
        this.solution = solution;
    }

    public JsonNode getWordSources()
    {
        return wordSources;
    }

    public void setWordSources(JsonNode wordSources)
    {
        this.wordSources = wordSources;
    }
}
