package com.lexipal.core.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_achievements_user_definition",
                columnNames = {"user_id", "achievement_id"}))
public class UserAchievement
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private AchievementDefinition achievement;

    @Column(nullable = false)
    private LocalDateTime unlockedAt;

    @Column(nullable = false)
    private Integer progressValueAtUnlock;

    @PrePersist
    protected void onCreate()
    {
        if (this.unlockedAt == null)
        {
            this.unlockedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AchievementDefinition getAchievement() {
        return achievement;
    }

    public void setAchievement(AchievementDefinition achievement) {
        this.achievement = achievement;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    public Integer getProgressValueAtUnlock() {
        return progressValueAtUnlock;
    }

    public void setProgressValueAtUnlock(Integer progressValueAtUnlock) {
        this.progressValueAtUnlock = progressValueAtUnlock;
    }
}
