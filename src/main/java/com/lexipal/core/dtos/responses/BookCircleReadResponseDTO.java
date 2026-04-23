package com.lexipal.core.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lexipal.core.entities.CircleStatus;
import com.lexipal.core.entities.CircleVisibility;

import java.time.LocalDateTime;
import java.util.List;

public class BookCircleReadResponseDTO
{
    private Long id;
    private String inviteCode;
    private String bookTitle;
    private String bookAuthor;
    private Integer totalPages;
    private Integer memberCap;
    private Integer currentMemberCount;
    private CircleVisibility visibility;
    private CircleStatus status;
    private String ownerNickname;
    private boolean callerIsOwner;
    private LocalDateTime createdAt;
    private List<CircleMemberProgressDTO> members;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextMeetingDate;
    private String meetingChapterLabel;
    private Integer meetingPageStart;
    private Integer meetingPageEnd;

    public BookCircleReadResponseDTO()
    {

    }

    public BookCircleReadResponseDTO(Long id, String inviteCode, String bookTitle, String bookAuthor,
                                     Integer totalPages, Integer memberCap, Integer currentMemberCount,
                                     CircleVisibility visibility, CircleStatus status,
                                     String ownerNickname, boolean callerIsOwner,
                                     LocalDateTime createdAt, List<CircleMemberProgressDTO> members)
    {
        this.id = id;
        this.inviteCode = inviteCode;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.totalPages = totalPages;
        this.memberCap = memberCap;
        this.currentMemberCount = currentMemberCount;
        this.visibility = visibility;
        this.status = status;
        this.ownerNickname = ownerNickname;
        this.callerIsOwner = callerIsOwner;
        this.createdAt = createdAt;
        this.members = members;
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

    public Integer getCurrentMemberCount() { return currentMemberCount; }
    public void setCurrentMemberCount(Integer currentMemberCount) { this.currentMemberCount = currentMemberCount; }

    public CircleVisibility getVisibility() { return visibility; }
    public void setVisibility(CircleVisibility visibility) { this.visibility = visibility; }

    public CircleStatus getStatus() { return status; }
    public void setStatus(CircleStatus status) { this.status = status; }

    public String getOwnerNickname() { return ownerNickname; }
    public void setOwnerNickname(String ownerNickname) { this.ownerNickname = ownerNickname; }

    public boolean isCallerIsOwner() { return callerIsOwner; }
    public void setCallerIsOwner(boolean callerIsOwner) { this.callerIsOwner = callerIsOwner; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<CircleMemberProgressDTO> getMembers() { return members; }
    public void setMembers(List<CircleMemberProgressDTO> members) { this.members = members; }

    public LocalDateTime getNextMeetingDate() { return nextMeetingDate; }
    public void setNextMeetingDate(LocalDateTime nextMeetingDate) { this.nextMeetingDate = nextMeetingDate; }

    public String getMeetingChapterLabel() { return meetingChapterLabel; }
    public void setMeetingChapterLabel(String meetingChapterLabel) { this.meetingChapterLabel = meetingChapterLabel; }

    public Integer getMeetingPageStart() { return meetingPageStart; }
    public void setMeetingPageStart(Integer meetingPageStart) { this.meetingPageStart = meetingPageStart; }

    public Integer getMeetingPageEnd() { return meetingPageEnd; }
    public void setMeetingPageEnd(Integer meetingPageEnd) { this.meetingPageEnd = meetingPageEnd; }
}
