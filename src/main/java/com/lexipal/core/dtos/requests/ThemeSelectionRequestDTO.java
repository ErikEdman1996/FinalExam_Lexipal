package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public class ThemeSelectionRequestDTO
{
    @NotBlank(message = "themeCode is required")
    private String themeCode;

    public ThemeSelectionRequestDTO()
    {

    }

    public ThemeSelectionRequestDTO(String themeCode)
    {
        this.themeCode = themeCode;
    }

    public String getThemeCode() {
        return themeCode;
    }

    public void setThemeCode(String themeCode) {
        this.themeCode = themeCode;
    }
}
