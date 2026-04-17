package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SessionCreateRequestDTO
{
    @NotNull(message = "Book id is required")
    private Long bookId;

    @Min(value = 0, message = "Duration must be zero or greater")
    private Long durationSeconds;

    @NotNull(message = "Start page is required")
    @Min(value = 0, message = "Start page must be zero or greater")
    private Integer startPage;

    @Min(value = 0, message = "End page must be zero or greater")
    private Integer endPage;

    @Size(max = 2000, message = "Reflection must be 2000 characters or fewer")
    private String reflection;

    public SessionCreateRequestDTO()
    {

    }

    public SessionCreateRequestDTO(Long bookId, Long durationSeconds, Integer startPage, Integer endPage, String reflection)
    {
        this.bookId = bookId;
        this.durationSeconds = durationSeconds;
        this.startPage = startPage;
        this.endPage = endPage;
        this.reflection = reflection;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }

    public Integer getEndPage() {
        return endPage;
    }

    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
    }

    public String getReflection() {
        return reflection;
    }

    public void setReflection(String reflection) {
        this.reflection = reflection;
    }
}
