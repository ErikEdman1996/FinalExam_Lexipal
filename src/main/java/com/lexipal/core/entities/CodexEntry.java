package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "codex_entries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_codex_entry_user_word", columnNames = {"user_id", "word_id"})
        }
)
public class CodexEntry
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(length = 255)
    private String specificDefinition;

    private Integer xpValue;

    private LocalDateTime createdAt;

    @Column(name = "source_session_id")
    private Long sourceSessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-codexEntries")
    private User user;

    public CodexEntry()
    {

    }

    public CodexEntry(Long id, Word word, String specificDefinition, Integer xpValue,
                      LocalDateTime createdAt, Long sourceSessionId, User user)
    {
        this.id = id;
        this.word = word;
        this.specificDefinition = specificDefinition;
        this.xpValue = xpValue;
        this.createdAt = createdAt;
        this.sourceSessionId = sourceSessionId;
        this.user = user;
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public String getSpecificDefinition() {
        return specificDefinition;
    }

    public void setSpecificDefinition(String specificDefinition) {
        this.specificDefinition = specificDefinition;
    }

    public Integer getXpValue() {
        return xpValue;
    }

    public void setXpValue(Integer xpValue) {
        this.xpValue = xpValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getSourceSessionId() {
        return sourceSessionId;
    }

    public void setSourceSessionId(Long sourceSessionId) {
        this.sourceSessionId = sourceSessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
