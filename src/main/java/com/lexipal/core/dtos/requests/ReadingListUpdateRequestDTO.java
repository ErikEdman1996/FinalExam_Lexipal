package com.lexipal.core.dtos.requests;

import com.lexipal.core.entities.ReadingListStatus;
import jakarta.validation.constraints.Size;

public class ReadingListUpdateRequestDTO
{
    @Size(max = 120, message = "Title must be 120 characters or fewer")
    private String title;

    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;

    private ReadingListStatus status;

    public ReadingListUpdateRequestDTO()
    {

    }

    public ReadingListUpdateRequestDTO(String title, String description, ReadingListStatus status)
    {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReadingListStatus getStatus() { return status; }
    public void setStatus(ReadingListStatus status) { this.status = status; }
}
