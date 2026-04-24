package com.lexipal.core.dtos.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lexipal.core.entities.ReadingListStatus;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ReadingListUpdateRequestDTO
{
    @Size(max = 120, message = "Title must be 120 characters or fewer")
    private String title;

    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;

    private ReadingListStatus status;

    private List<String> genres;

    @JsonIgnore
    private boolean genresProvided;

    public ReadingListUpdateRequestDTO()
    {

    }

    public ReadingListUpdateRequestDTO(String title, String description, ReadingListStatus status)
    {
        this(title, description, status, null, false);
    }

    public ReadingListUpdateRequestDTO(String title, String description, ReadingListStatus status,
                                       List<String> genres)
    {
        this(title, description, status, genres, true);
    }

    private ReadingListUpdateRequestDTO(String title, String description, ReadingListStatus status,
                                        List<String> genres, boolean genresProvided)
    {
        this.title = title;
        this.description = description;
        this.status = status;
        this.genres = genres;
        this.genresProvided = genresProvided;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReadingListStatus getStatus() { return status; }
    public void setStatus(ReadingListStatus status) { this.status = status; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres)
    {
        this.genres = genres;
        this.genresProvided = true;
    }

    public boolean isGenresProvided() { return genresProvided; }
}
