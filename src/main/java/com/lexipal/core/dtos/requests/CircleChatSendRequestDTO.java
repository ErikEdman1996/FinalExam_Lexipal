package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CircleChatSendRequestDTO
{
    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message content must be 2000 characters or fewer")
    private String content;

    public CircleChatSendRequestDTO()
    {

    }

    public CircleChatSendRequestDTO(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
