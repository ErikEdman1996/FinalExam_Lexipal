package com.lexipal.core.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateBookRequest
{
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be 100 characters or fewer")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author must be 100 characters or fewer")
    private String author;

    @NotNull(message = "Total pages is required")
    @Min(value = 1, message = "Total pages must be at least 1")
    private Integer totalPages;

    public CreateBookRequest()
    {

    }

    public CreateBookRequest(String title, String author, Integer totalPages)
    {
        this.title = title;
        this.author = author;
        this.totalPages = totalPages;
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

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
