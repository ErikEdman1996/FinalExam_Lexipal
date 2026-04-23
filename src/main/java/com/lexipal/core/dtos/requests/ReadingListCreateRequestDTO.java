package com.lexipal.core.dtos.requests;

import com.lexipal.core.entities.ReadingListStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ReadingListCreateRequestDTO
{
    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must be 120 characters or fewer")
    private String title;

    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;

    private ReadingListStatus status;

    @NotEmpty(message = "A reading list must contain at least one book")
    @Valid
    private List<ReadingListBookSlotRequestDTO> books;

    public ReadingListCreateRequestDTO()
    {

    }

    public ReadingListCreateRequestDTO(String title, String description, ReadingListStatus status,
                                       List<ReadingListBookSlotRequestDTO> books)
    {
        this.title = title;
        this.description = description;
        this.status = status;
        this.books = books;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReadingListStatus getStatus() { return status; }
    public void setStatus(ReadingListStatus status) { this.status = status; }

    public List<ReadingListBookSlotRequestDTO> getBooks() { return books; }
    public void setBooks(List<ReadingListBookSlotRequestDTO> books) { this.books = books; }
}
