package com.lexipal.core.entities;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column()
    private Integer readPages = 0;

    @Column(nullable = false)
    private Integer totalPages;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status = BookStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    public Book()
    {

    }

    public Book(Long id, String title, String author, Integer readPages, Integer totalPages,
                LocalDateTime createdAt, BookStatus status, User user) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.readPages = readPages;
        this.totalPages = totalPages;
        this.createdAt = createdAt;
        this.status = status;
        this.user = user;
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        if(this.readPages == null)
        {
            this.readPages = 0;
            this.status = BookStatus.ACTIVE;
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getReadPages() {
        return readPages;
    }

    public void setReadPages(Integer readPages) {
        this.readPages = readPages;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public enum BookStatus
    {
        ACTIVE,
        COMPLETE,
        ARCHIVED,
        DELETED
    }
}
