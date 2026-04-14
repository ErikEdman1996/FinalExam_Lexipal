package com.lexipal.core.dtos.responses;

public class ChallengeReadResponseDTO
{
    private Long id;
    private String type;
    private String description;
    private String difficulty;

    public ChallengeReadResponseDTO()
    {

    }

    public ChallengeReadResponseDTO(Long id, String type, String description, String difficulty)
    {
        this.id = id;
        this.type = type;
        this.description = description;
        this.difficulty = difficulty;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDifficulty()
    {
        return difficulty;
    }

    public void setDifficulty(String difficulty)
    {
        this.difficulty = difficulty;
    }
}
