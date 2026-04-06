package com.lexipal.core.dtos;
import com.lexipal.core.entities.Book;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BookUpdateDTO
{
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    @Pattern(regexp = ".*\\S.*", message = "Title must not be blank")
    private String title;

    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    @Pattern(regexp = ".*\\S.*", message = "Author must not be blank")
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

    public Integer getReadPages() {
        return readPages;
    }

    public void setReadPages(Integer readPages) {
        this.readPages = readPages;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Book.BookStatus getStatus() {
        return status;
    }

    public void setStatus(Book.BookStatus status) {
        this.status = status;
    }
}
