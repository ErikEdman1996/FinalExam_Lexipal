package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "word_lookups",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_word_lookup_user_word", columnNames = {"user_id", "word_id"})
        }
)
public class WordLookup
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-wordLookups")
    private User user;

    @Column(nullable = false)
    private LocalDateTime firstLookedUpAt;

    @Column(nullable = false)
    private LocalDateTime lastLookedUpAt;

    @Column(nullable = false)
    private Integer lookupCount;

    @Column(name = "first_source_session_id")
    private Long firstSourceSessionId;

    @Column(name = "last_source_session_id")
    private Long lastSourceSessionId;

    public WordLookup()
    {

    }

    @PrePersist
    protected void onCreate()
    {
        LocalDateTime now = LocalDateTime.now();

        if (this.firstLookedUpAt == null)
        {
            this.firstLookedUpAt = now;
        }

        if (this.lastLookedUpAt == null)
        {
            this.lastLookedUpAt = this.firstLookedUpAt;
        }

        if (this.lookupCount == null || this.lookupCount < 1)
        {
            this.lookupCount = 1;
        }
    }

    public void recordLookup(Long sourceSessionId)
    {
        LocalDateTime now = LocalDateTime.now();

        if (this.firstLookedUpAt == null)
        {
            this.firstLookedUpAt = now;
        }

        this.lastLookedUpAt = now;
        this.lookupCount = this.lookupCount == null ? 1 : this.lookupCount + 1;

        if (this.firstSourceSessionId == null)
        {
            this.firstSourceSessionId = sourceSessionId;
        }

        this.lastSourceSessionId = sourceSessionId;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Word getWord()
    {
        return word;
    }

    public void setWord(Word word)
    {
        this.word = word;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public LocalDateTime getFirstLookedUpAt()
    {
        return firstLookedUpAt;
    }

    public void setFirstLookedUpAt(LocalDateTime firstLookedUpAt)
    {
        this.firstLookedUpAt = firstLookedUpAt;
    }

    public LocalDateTime getLastLookedUpAt()
    {
        return lastLookedUpAt;
    }

    public void setLastLookedUpAt(LocalDateTime lastLookedUpAt)
    {
        this.lastLookedUpAt = lastLookedUpAt;
    }

    public Integer getLookupCount()
    {
        return lookupCount;
    }

    public void setLookupCount(Integer lookupCount)
    {
        this.lookupCount = lookupCount;
    }

    public Long getFirstSourceSessionId()
    {
        return firstSourceSessionId;
    }

    public void setFirstSourceSessionId(Long firstSourceSessionId)
    {
        this.firstSourceSessionId = firstSourceSessionId;
    }

    public Long getLastSourceSessionId()
    {
        return lastSourceSessionId;
    }

    public void setLastSourceSessionId(Long lastSourceSessionId)
    {
        this.lastSourceSessionId = lastSourceSessionId;
    }
}
