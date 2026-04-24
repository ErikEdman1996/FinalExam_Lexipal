package com.lexipal.core.dtos.responses;

import com.lexipal.core.entities.CircleMemberRole;

import java.time.LocalDateTime;

public class CircleMemberProgressDTO
{
    private Long userId;
    private String userNickname;
    private CircleMemberRole role;
    private Integer readPages;
    private Integer totalPages;
    private Double progressPercentage;
    private LocalDateTime joinedAt;
    private PortraitResponseDTO selectedPortrait;

    public CircleMemberProgressDTO()
    {

    }

    public CircleMemberProgressDTO(Long userId, String userNickname, CircleMemberRole role,
                                   Integer readPages, Integer totalPages, Double progressPercentage,
                                   LocalDateTime joinedAt)
    {
        this(userId, userNickname, role, readPages, totalPages, progressPercentage, joinedAt, null);
    }

    public CircleMemberProgressDTO(Long userId, String userNickname, CircleMemberRole role,
                                   Integer readPages, Integer totalPages, Double progressPercentage,
                                   LocalDateTime joinedAt, PortraitResponseDTO selectedPortrait)
    {
        this.userId = userId;
        this.userNickname = userNickname;
        this.role = role;
        this.readPages = readPages;
        this.totalPages = totalPages;
        this.progressPercentage = progressPercentage;
        this.joinedAt = joinedAt;
        this.selectedPortrait = selectedPortrait;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }

    public CircleMemberRole getRole() { return role; }
    public void setRole(CircleMemberRole role) { this.role = role; }

    public Integer getReadPages() { return readPages; }
    public void setReadPages(Integer readPages) { this.readPages = readPages; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public PortraitResponseDTO getSelectedPortrait() { return selectedPortrait; }
    public void setSelectedPortrait(PortraitResponseDTO selectedPortrait) { this.selectedPortrait = selectedPortrait; }
}
