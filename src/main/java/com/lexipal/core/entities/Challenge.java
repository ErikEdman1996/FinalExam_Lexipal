package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenges")
public class Challenge
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ChallengeType type;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeDifficulty difficulty;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    @JsonManagedReference("challenge-attempts")
    private List<ChallengeAttempt> attempts = new ArrayList<>();

    public Challenge()
    {

    }

    public Challenge(Long id, ChallengeType type, String description, ChallengeDifficulty difficulty)
    {
        this.id = id;
        this.type = type;
        this.description = description;
        this.difficulty = difficulty;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public ChallengeType getType()
    {
        return type;
    }

    public void setType(ChallengeType type)
    {
        this.type = type;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public ChallengeDifficulty getDifficulty()
    {
        return difficulty;
    }

    public void setDifficulty(ChallengeDifficulty difficulty)
    {
        this.difficulty = difficulty;
    }

    public List<ChallengeAttempt> getAttempts()
    {
        return attempts;
    }

    public void setAttempts(List<ChallengeAttempt> attempts)
    {
        this.attempts = attempts;
    }
}
