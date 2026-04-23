package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_activities")
public class DailyActivity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate activityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private Integer xpEarned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-dailyActivities")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @JsonBackReference("session-dailyActivities")
    private Session session;

    @Column(name = "external_ref_id")
    private Long externalRefId;

    public enum ActivityType
    {
        SESSION_COMPLETED,
        CHALLENGE_COMPLETED,
        READING_LIST_BOOK_COMPLETED,
        READING_LIST_COMPLETED
    }

    public DailyActivity()
    {

    }

    public DailyActivity(Long id, LocalDate activityDate, ActivityType activityType, Integer xpEarned, User user,
                         Session session)
    {
        this.id = id;
        this.activityDate = activityDate;
        this.activityType = activityType;
        this.xpEarned = xpEarned;
        this.user = user;
        this.session = session;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
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

    public Long getExternalRefId() {
        return externalRefId;
    }

    public void setExternalRefId(Long externalRefId) {
        this.externalRefId = externalRefId;
    }
}
