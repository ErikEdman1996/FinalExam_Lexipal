package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookCoverCandidatesRequestDTO
{
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be 100 characters or fewer")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author must be 100 characters or fewer")
    private String author;

    public BookCoverCandidatesRequestDTO()
    {

    }

    public BookCoverCandidatesRequestDTO(String title, String author)
    {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
