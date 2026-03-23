package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "captured_words")
public class CapturedWord
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

    private LocalDateTime capturedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonBackReference
    private Session session;

    public CapturedWord()
    {

    }

    @PrePersist
    protected void onCreate()
    {
        this.capturedAt = LocalDateTime.now();
    }

    public CapturedWord(Long id, Word word, String specificDefinition, Integer xpValue,
                        LocalDateTime capturedAt, User user, Session session)
    {
        this.id = id;
        this.word = word;
        this.specificDefinition = specificDefinition;
        this.xpValue = xpValue;
        this.capturedAt = capturedAt;
        this.user = user;
        this.session = session;
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

    public String getspecificDefinition() {
        return specificDefinition;
    }

    public void setSpecificDefintion(String specificDefinition) {
        this.specificDefinition = specificDefinition;
    }

    public Integer getXpValue() {
        return xpValue;
    }

    public void setXpValue(Integer xpValue) {
        this.xpValue = xpValue;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
