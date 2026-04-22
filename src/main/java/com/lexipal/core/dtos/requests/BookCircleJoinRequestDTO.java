package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookCircleJoinRequestDTO
{
    @NotBlank(message = "Invite code is required")
    @Size(min = 6, max = 6, message = "Invite code must be exactly 6 characters")
    private String inviteCode;

    public BookCircleJoinRequestDTO()
    {

    }

    public BookCircleJoinRequestDTO(String inviteCode)
    {
        this.inviteCode = inviteCode;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}
