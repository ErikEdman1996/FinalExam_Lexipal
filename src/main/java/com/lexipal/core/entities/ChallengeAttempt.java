package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_attempts")
public class ChallengeAttempt
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String userAnswer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeResult result;

    @Column(nullable = false)
    private Integer xpAwarded;

    @Column(nullable = false)
    private LocalDateTime attemptedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-challengeAttempts")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    @JsonBackReference("challenge-attempts")
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_lookup_id", nullable = false)
    private WordLookup wordLookup;

    public ChallengeAttempt()
    {

    }

    @PrePersist
    protected void onCreate()
    {
        this.attemptedAt = LocalDateTime.now();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getUserAnswer()
    {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer)
    {
        this.userAnswer = userAnswer;
    }

    public ChallengeResult getResult()
    {
        return result;
    }

    public void setResult(ChallengeResult result)
    {
        this.result = result;
    }

    public Integer getXpAwarded()
    {
        return xpAwarded;
    }

    public void setXpAwarded(Integer xpAwarded)
    {
        this.xpAwarded = xpAwarded;
    }

    public LocalDateTime getAttemptedAt()
    {
        return attemptedAt;
    }

    public void setAttemptedAt(LocalDateTime attemptedAt)
    {
        this.attemptedAt = attemptedAt;
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

    public WordLookup getWordLookup()
    {
        return wordLookup;
    }

    public void setWordLookup(WordLookup wordLookup)
    {
        this.wordLookup = wordLookup;
    }
}
