package com.lexipal.core.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_quests",
        uniqueConstraints = @UniqueConstraint(name = "uk_daily_quests_user_date", columnNames = {"user_id", "quest_date"}))
public class DailyQuest
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate questDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DailyQuestType type;

    @Column(nullable = false)
    private Integer targetValue;

    @Column(nullable = false)
    private Integer progressValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DailyQuestStatus status;

    @Column(nullable = false)
    private Integer xpReward;

    @Column(nullable = false)
    private Integer coinReward;

    @Column
    private Integer baselineValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_session_id")
    private Session sourceSession;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate()
    {
        if (createdAt == null)
        {
            createdAt = LocalDateTime.now();
        }

        if (progressValue == null)
        {
            progressValue = 0;
        }

        if (status == null)
        {
            status = DailyQuestStatus.ACTIVE;
        }
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public LocalDate getQuestDate()
    {
        return questDate;
    }

    public void setQuestDate(LocalDate questDate)
    {
        this.questDate = questDate;
    }

    public DailyQuestType getType()
    {
        return type;
    }

    public void setType(DailyQuestType type)
    {
        this.type = type;
    }

    public Integer getTargetValue()
    {
        return targetValue;
    }

    public void setTargetValue(Integer targetValue)
    {
        this.targetValue = targetValue;
    }

    public Integer getProgressValue()
    {
        return progressValue;
    }

    public void setProgressValue(Integer progressValue)
    {
        this.progressValue = progressValue;
    }

    public DailyQuestStatus getStatus()
    {
        return status;
    }

    public void setStatus(DailyQuestStatus status)
    {
        this.status = status;
    }

    public Integer getXpReward()
    {
        return xpReward;
    }

    public void setXpReward(Integer xpReward)
    {
        this.xpReward = xpReward;
    }

    public Integer getCoinReward()
    {
        return coinReward;
    }

    public void setCoinReward(Integer coinReward)
    {
        this.coinReward = coinReward;
    }

    public Integer getBaselineValue()
    {
        return baselineValue;
    }

    public void setBaselineValue(Integer baselineValue)
    {
        this.baselineValue = baselineValue;
    }

    public Session getSourceSession()
    {
        return sourceSession;
    }

    public void setSourceSession(Session sourceSession)
    {
        this.sourceSession = sourceSession;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt()
    {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt)
    {
        this.completedAt = completedAt;
    }
}
