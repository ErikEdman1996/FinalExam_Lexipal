package com.lexipal.core.dtos.responses;

public class NoteTagResponseDTO
{
    private Long id;
    private String name;
    private String colorHex;

    public NoteTagResponseDTO()
    {

    }

    public NoteTagResponseDTO(Long id, String name, String colorHex)
    {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
}
