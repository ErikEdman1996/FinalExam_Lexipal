package com.lexipal.core.entities;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keycloakSub;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column
    private Integer totalPagesRead;

    @Column
    private Integer totalBooksRead;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-books")
    private List<Book> books = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-sessions")
    private List<Session> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-capturedWords")
    private List<CapturedWord> capturedWords = new ArrayList<>();

    public enum UserStatus
    {
        ACTIVE,
        INACTIVE,
        DELETED
    }

    public User()
    {

    }

    public User(String keycloakSub, LocalDateTime createdAt, UserStatus status,
                Integer totalPagesRead, Integer totalBooksRead, List<Book> books, List<Session> sessions,
                List<CapturedWord> capturedWords) {
        this.keycloakSub = keycloakSub;
        this.createdAt = createdAt;
        this.status = status;
        this.totalPagesRead = totalPagesRead;
        this.totalBooksRead = totalBooksRead;
        this.books = books;
        this.sessions = sessions;
        this.capturedWords = capturedWords;
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        this.status = UserStatus.ACTIVE;
        this.totalPagesRead = 0;
        this.totalBooksRead = 0;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getKeycloakSub() {
        return keycloakSub;
    }

    public void setKeycloakSub(String keycloakSub) {
        this.keycloakSub = keycloakSub;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public UserStatus getStatus()
    {
        return status;
    }

    public void setStatus(UserStatus status)
    {
        this.status = status;
    }

    public Integer getTotalPagesRead()
    {
        return totalPagesRead;
    }

    public void setTotalPagesRead(Integer totalPagesRead)
    {
        this.totalPagesRead = totalPagesRead;
    }

    public Integer getTotalBooksRead()
    {
        return totalBooksRead;
    }

    public void setTotalBooksRead(Integer totalBooksRead)
    {
        this.totalBooksRead = totalBooksRead;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }

    public List<CapturedWord> getCapturedWords() {
        return capturedWords;
    }

    public void setCapturedWords(List<CapturedWord> capturedWords) {
        this.capturedWords = capturedWords;
    }
}
