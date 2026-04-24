package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public class PortraitSelectionRequestDTO
{
    @NotBlank(message = "portraitCode is required")
    private String portraitCode;

    public PortraitSelectionRequestDTO()
    {

    }

    public PortraitSelectionRequestDTO(String portraitCode)
    {
        this.portraitCode = portraitCode;
    }

    public String getPortraitCode() {
        return portraitCode;
    }

    public void setPortraitCode(String portraitCode) {
        this.portraitCode = portraitCode;
    }
}
