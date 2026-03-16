package com.lexipal.core.dtos;
import com.lexipal.core.entities.Book;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class BookUpdateDTO
{
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    private String author;

    @Min(value = 0, message = "Read pages cannot be negative")
    private Integer readPages;

    @Min(value = 1, message = "Total pages must be at least 1")
    private Integer totalPages;

    private Book.BookStatus status;

    public BookUpdateDTO()
    {

    }

    public BookUpdateDTO(String title, String author, Integer readPages, Integer totalPages, Book.BookStatus status)
    {
        this.title = title;
        this.author = author;
        this.readPages = readPages;
        this.totalPages = totalPages;
        this.status = status;
    }

    public @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters") String getTitle() {
        return title;
    }

    public void setTitle(@Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters") String title) {
        this.title = title;
    }

    public @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters") String getAuthor() {
        return author;
    }

    public void setAuthor(@Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters") String author) {
        this.author = author;
    }

    public @Min(value = 0, message = "Read pages cannot be negative") Integer getReadPages() {
        return readPages;
    }

    public void setReadPages(@Min(value = 0, message = "Read pages cannot be negative") Integer readPages) {
        this.readPages = readPages;
    }

    public @Min(value = 1, message = "Total pages must be at least 1") Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(@Min(value = 1, message = "Total pages must be at least 1") Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Book.BookStatus getStatus() {
        return status;
    }

    public void setStatus(Book.BookStatus status) {
        this.status = status;
    }
}
