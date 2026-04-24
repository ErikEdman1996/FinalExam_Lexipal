package com.lexipal.core.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_portrait_unlocks",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_portrait_unlocks_user_portrait",
                columnNames = {"user_id", "portrait_id"}))
public class UserPortraitUnlock
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portrait_id", nullable = false)
    private PortraitDefinition portrait;

    @Column(nullable = false)
    private LocalDateTime unlockedAt;

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

    public PortraitDefinition getPortrait() {
        return portrait;
    }

    public void setPortrait(PortraitDefinition portrait) {
        this.portrait = portrait;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }
}
