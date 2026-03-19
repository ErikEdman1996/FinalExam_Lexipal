package com.lexipal.core.dtos;

public class SessionRequestDTO
{
    private Long bookId;
    private Long durationSeconds;
    private Integer startPage;
    private Integer endPage;
    private String reflection;

    public SessionRequestDTO()
    {

    }

    public SessionRequestDTO(Long bookId, Long durationSeconds, Integer startPage, Integer endPage, String reflection)
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
