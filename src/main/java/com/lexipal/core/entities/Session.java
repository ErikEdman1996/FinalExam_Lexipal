package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sessions")
public class Session
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long durationSeconds;

    @Column(nullable = false)
    private Integer startPage;

    @Column(nullable = false)
    private Integer endPage;

    @Column(nullable = false)
    private Integer pagesRead;

    @Column(columnDefinition = "TEXT")
    private String reflection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sessionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-sessions")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonBackReference(value = "book-sessions")
    private Book book;

    @Column(nullable = false)
    private Integer capturedWordCount;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @JsonManagedReference("session-dailyActivities")
    private List<DailyActivity> dailyActivities = new ArrayList<>();

    public Session()
    {

    }

    public Session(Long durationSeconds, Integer startPage, Integer endPage, Integer pagesRead,
                   String reflection, SessionStatus status, LocalDateTime sessionDate, User user, Book book,
                   Integer capturedWordCount)
    {
        this.durationSeconds = durationSeconds;
        this.startPage = startPage;
        this.endPage = endPage;
        this.pagesRead = pagesRead;
        this.reflection = reflection;
        this.status = status;
        this.sessionDate = sessionDate;
        this.user = user;
        this.book = book;
        this.capturedWordCount = capturedWordCount;
    }

    @PrePersist
    protected void onCreate()
    {
        this.sessionDate = LocalDateTime.now();
        this.status = SessionStatus.IN_PROGRESS;

        if (this.startPage != null && this.endPage != null)
        {
            this.pagesRead = Math.max(0, this.endPage - this.startPage);
        }

        if (this.capturedWordCount == null)
        {
            this.capturedWordCount = 0;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getCapturedWordCount() {
        return capturedWordCount;
    }

    public void setCapturedWordCount(Integer capturedWordCount) {
        this.capturedWordCount = capturedWordCount;
    }

    public List<DailyActivity> getDailyActivities() {
        return dailyActivities;
    }

    public void setDailyActivities(List<DailyActivity> dailyActivities) {
        this.dailyActivities = dailyActivities;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public enum SessionStatus
    {
        IN_PROGRESS,
        COMPLETED
    }
}
