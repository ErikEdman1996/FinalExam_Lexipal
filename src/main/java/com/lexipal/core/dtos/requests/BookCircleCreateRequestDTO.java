package com.lexipal.core.dtos.requests;

import com.lexipal.core.entities.CircleVisibility;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BookCircleCreateRequestDTO
{
    private Long existingBookId;

    @Size(max = 100, message = "Title must be 100 characters or fewer")
    private String bookTitle;

    @Size(max = 100, message = "Author must be 100 characters or fewer")
    private String bookAuthor;

    @Min(value = 1, message = "Total pages must be at least 1")
    private Integer totalPages;

    @NotNull(message = "Member cap is required")
    @Min(value = 2, message = "Member cap must be at least 2")
    @Max(value = 50, message = "Member cap must be 50 or fewer")
    private Integer memberCap;

    @NotNull(message = "Visibility is required")
    private CircleVisibility visibility;

    public BookCircleCreateRequestDTO()
    {

    }

    public BookCircleCreateRequestDTO(Long existingBookId, String bookTitle, String bookAuthor,
                                      Integer totalPages, Integer memberCap, CircleVisibility visibility)
    {
        this.existingBookId = existingBookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.totalPages = totalPages;
        this.memberCap = memberCap;
        this.visibility = visibility;
    }

    public Long getExistingBookId() {
        return existingBookId;
    }

    public void setExistingBookId(Long existingBookId) {
        this.existingBookId = existingBookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getMemberCap() {
        return memberCap;
    }

    public void setMemberCap(Integer memberCap) {
        this.memberCap = memberCap;
    }

    public CircleVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(CircleVisibility visibility) {
        this.visibility = visibility;
    }
}
