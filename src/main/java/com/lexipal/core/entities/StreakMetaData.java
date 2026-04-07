package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "streak_metadata",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_streak_metadata_user", columnNames = {"user_id"})
        }
)
public class StreakMetaData
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer currentStreak;

    @Column(nullable = false)
    private Integer longestStreak;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastActivityDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference("user-streakMetaData")
    private User user;

    public StreakMetaData()
    {

    }

    @PrePersist
    protected void onCreate()
    {
        if (this.currentStreak == null) this.currentStreak = 0;
        if (this.longestStreak == null) this.longestStreak = 0;
    }

    public StreakMetaData(Long id, Integer currentStreak, Integer longestStreak, LocalDate lastActivityDate, User user)
    {
        this.id = id;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.lastActivityDate = lastActivityDate;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(LocalDate lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
