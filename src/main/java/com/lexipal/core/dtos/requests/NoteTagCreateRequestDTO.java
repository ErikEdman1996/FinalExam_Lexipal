package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NoteTagCreateRequestDTO
{
    @NotBlank(message = "Tag name is required")
    @Size(max = 50, message = "Tag name must be 50 characters or fewer")
    private String name;

    private String colorHex;

    public NoteTagCreateRequestDTO()
    {

    }

    public NoteTagCreateRequestDTO(String name, String colorHex)
    {
        this.name = name;
        this.colorHex = colorHex;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
}
