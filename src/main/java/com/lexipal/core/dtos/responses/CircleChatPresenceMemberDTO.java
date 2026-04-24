package com.lexipal.core.dtos.responses;

public class CircleChatPresenceMemberDTO
{
    private Long userId;
    private String userNickname;
    private PortraitResponseDTO selectedPortrait;

    public CircleChatPresenceMemberDTO()
    {

    }

    public CircleChatPresenceMemberDTO(Long userId, String userNickname)
    {
        this(userId, userNickname, null);
    }

    public CircleChatPresenceMemberDTO(Long userId, String userNickname, PortraitResponseDTO selectedPortrait)
    {
        this.userId = userId;
        this.userNickname = userNickname;
        this.selectedPortrait = selectedPortrait;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getUserNickname()
    {
        return userNickname;
    }

    public void setUserNickname(String userNickname)
    {
        this.userNickname = userNickname;
    }

    public PortraitResponseDTO getSelectedPortrait()
    {
        return selectedPortrait;
    }

    public void setSelectedPortrait(PortraitResponseDTO selectedPortrait)
    {
        this.selectedPortrait = selectedPortrait;
    }
}
