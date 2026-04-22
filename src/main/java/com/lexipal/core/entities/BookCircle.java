package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "book_circles", indexes = {
        @Index(name = "idx_book_circles_invite_code", columnList = "inviteCode", unique = true)
})
public class BookCircle
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String inviteCode;

    @Column(nullable = false, length = 100)
    private String bookTitle;

    @Column(nullable = false, length = 100)
    private String bookAuthor;

    @Column(nullable = false)
    private Integer totalPages;

    @Column(nullable = false)
    private Integer memberCap;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CircleVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CircleStatus status = CircleStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "circle", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("circle-members")
    private List<CircleMember> members = new ArrayList<>();

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public BookCircle()
    {

    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        if (this.status == null)
        {
            this.status = CircleStatus.ACTIVE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getMemberCap() {
        return memberCap;
    }

    public void setMemberCap(Integer memberCap) {
        this.memberCap = memberCap;
    }

    public CircleVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(CircleVisibility visibility) {
        this.visibility = visibility;
    }

    public CircleStatus getStatus() {
        return status;
    }

    public void setStatus(CircleStatus status) {
        this.status = status;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<CircleMember> getMembers() {
        return members;
    }

    public void setMembers(List<CircleMember> members) {
        this.members = members;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
