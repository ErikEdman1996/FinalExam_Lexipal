package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class NicknameUpdateRequestDTO
{
    @NotBlank(message = "Nickname is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$",
            message = "Nickname must be 3-20 characters, letters/digits/underscore only")
    private String nickname;

    public NicknameUpdateRequestDTO()
    {

    }

    public NicknameUpdateRequestDTO(String nickname)
    {
        this.nickname = nickname;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }
}
