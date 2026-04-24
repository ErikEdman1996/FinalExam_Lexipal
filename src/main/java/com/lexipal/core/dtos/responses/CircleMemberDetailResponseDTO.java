package com.lexipal.core.dtos.responses;

import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.CircleMemberRole;

import java.time.LocalDateTime;
import java.util.List;

public class CircleMemberDetailResponseDTO
{
    private Long circleId;
    private Long userId;
    private String userNickname;
    private CircleMemberRole role;
    private LocalDateTime joinedAt;
    private Integer readPages;
    private Integer totalPages;
    private Double progressPercentage;
    private PortraitResponseDTO selectedPortrait;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private Book.BookStatus bookStatus;
    private List<CircleSessionDTO> sessions;
    private List<CircleNoteDTO> notes;

    public CircleMemberDetailResponseDTO()
    {

    }

    public CircleMemberDetailResponseDTO(Long circleId, Long userId, String userNickname,
                                         CircleMemberRole role, LocalDateTime joinedAt,
                                         Integer readPages, Integer totalPages, Double progressPercentage,
                                         Long bookId, String bookTitle, String bookAuthor,
                                         Book.BookStatus bookStatus,
                                         List<CircleSessionDTO> sessions, List<CircleNoteDTO> notes)
    {
        this(circleId, userId, userNickname, role, joinedAt, readPages, totalPages, progressPercentage,
                null, bookId, bookTitle, bookAuthor, bookStatus, sessions, notes);
    }

    public CircleMemberDetailResponseDTO(Long circleId, Long userId, String userNickname,
                                         CircleMemberRole role, LocalDateTime joinedAt,
                                         Integer readPages, Integer totalPages, Double progressPercentage,
                                         PortraitResponseDTO selectedPortrait,
                                         Long bookId, String bookTitle, String bookAuthor,
                                         Book.BookStatus bookStatus,
                                         List<CircleSessionDTO> sessions, List<CircleNoteDTO> notes)
    {
        this.circleId = circleId;
        this.userId = userId;
        this.userNickname = userNickname;
        this.role = role;
        this.joinedAt = joinedAt;
        this.readPages = readPages;
        this.totalPages = totalPages;
        this.progressPercentage = progressPercentage;
        this.selectedPortrait = selectedPortrait;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookStatus = bookStatus;
        this.sessions = sessions;
        this.notes = notes;
    }

    public Long getCircleId() { return circleId; }
    public void setCircleId(Long circleId) { this.circleId = circleId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }

    public CircleMemberRole getRole() { return role; }
    public void setRole(CircleMemberRole role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public Integer getReadPages() { return readPages; }
    public void setReadPages(Integer readPages) { this.readPages = readPages; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }

    public PortraitResponseDTO getSelectedPortrait() { return selectedPortrait; }
    public void setSelectedPortrait(PortraitResponseDTO selectedPortrait) { this.selectedPortrait = selectedPortrait; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }

    public Book.BookStatus getBookStatus() { return bookStatus; }
    public void setBookStatus(Book.BookStatus bookStatus) { this.bookStatus = bookStatus; }

    public List<CircleSessionDTO> getSessions() { return sessions; }
    public void setSessions(List<CircleSessionDTO> sessions) { this.sessions = sessions; }

    public List<CircleNoteDTO> getNotes() { return notes; }
    public void setNotes(List<CircleNoteDTO> notes) { this.notes = notes; }
}
