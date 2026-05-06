package com.lexipal.core.dtos.responses;

public class NoteTagUsageResponseDTO
{
    private Long id;
    private String name;
    private String colorHex;
    private Long usageCount;

    public NoteTagUsageResponseDTO()
    {

    }

    public NoteTagUsageResponseDTO(Long id, String name, String colorHex, Long usageCount)
    {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
        this.usageCount = usageCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
}
