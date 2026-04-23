package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reading_list_stars", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reading_list_stars", columnNames = {"list_id", "user_id"})
})
public class ReadingListStar
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private ReadingList list;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "starred_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime starredAt;

    public ReadingListStar()
    {

    }

    @PrePersist
    protected void onCreate()
    {
        if (this.starredAt == null)
        {
            this.starredAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReadingList getList() {
        return list;
    }

    public void setList(ReadingList list) {
        this.list = list;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getStarredAt() {
        return starredAt;
    }

    public void setStarredAt(LocalDateTime starredAt) {
        this.starredAt = starredAt;
    }
}
