package com.lexipal.core.dtos.responses;

import com.lexipal.core.entities.CircleStatus;
import com.lexipal.core.entities.CircleVisibility;

import java.time.LocalDateTime;
import java.util.List;

public class BookCircleCreateResponseDTO
{
    private Long id;
    private String inviteCode;
    private String bookTitle;
    private String bookAuthor;
    private Integer totalPages;
    private Integer memberCap;
    private CircleVisibility visibility;
    private CircleStatus status;
    private LocalDateTime createdAt;
    private List<AchievementResponseDTO> newlyUnlockedAchievements = List.of();

    public BookCircleCreateResponseDTO()
    {

    }

    public BookCircleCreateResponseDTO(Long id, String inviteCode, String bookTitle, String bookAuthor,
                                       Integer totalPages, Integer memberCap, CircleVisibility visibility,
                                       CircleStatus status, LocalDateTime createdAt)
    {
        this(id, inviteCode, bookTitle, bookAuthor, totalPages, memberCap, visibility, status, createdAt, List.of());
    }

    public BookCircleCreateResponseDTO(Long id, String inviteCode, String bookTitle, String bookAuthor,
                                       Integer totalPages, Integer memberCap, CircleVisibility visibility,
                                       CircleStatus status, LocalDateTime createdAt,
                                       List<AchievementResponseDTO> newlyUnlockedAchievements)
    {
        this.id = id;
        this.inviteCode = inviteCode;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.totalPages = totalPages;
        this.memberCap = memberCap;
        this.visibility = visibility;
        this.status = status;
        this.createdAt = createdAt;
        setNewlyUnlockedAchievements(newlyUnlockedAchievements);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Integer getMemberCap() { return memberCap; }
    public void setMemberCap(Integer memberCap) { this.memberCap = memberCap; }

    public CircleVisibility getVisibility() { return visibility; }
    public void setVisibility(CircleVisibility visibility) { this.visibility = visibility; }

    public CircleStatus getStatus() { return status; }
    public void setStatus(CircleStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<AchievementResponseDTO> getNewlyUnlockedAchievements() { return newlyUnlockedAchievements; }
    public void setNewlyUnlockedAchievements(List<AchievementResponseDTO> newlyUnlockedAchievements)
    {
        this.newlyUnlockedAchievements = newlyUnlockedAchievements == null ? List.of() : newlyUnlockedAchievements;
    }
}
