package com.lexipal.core.dtos.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;

public class NoteTagUpdateRequestDTO
{
    @Size(max = 50, message = "Tag name must be 50 characters or fewer")
    private String name;

    private String colorHex;

    @JsonIgnore
    private boolean colorHexProvided;

    public NoteTagUpdateRequestDTO()
    {

    }

    public NoteTagUpdateRequestDTO(String name, String colorHex)
    {
        this.name = name;
        this.colorHex = colorHex;
        this.colorHexProvided = true;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex)
    {
        this.colorHex = colorHex;
        this.colorHexProvided = true;
    }

    public boolean isColorHexProvided() { return colorHexProvided; }
}
