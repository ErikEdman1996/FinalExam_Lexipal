package com.lexipal.core.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class NoteReadResponseDTO
{
    private Long id;
    private Long bookId;
    private Long sourceSessionId;
    private Integer pageNumber;
    private String chapterLabel;
    private String quotedText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public NoteReadResponseDTO()
    {

    }

    public NoteReadResponseDTO(Long id, Long bookId, Long sourceSessionId, Integer pageNumber,
                               String chapterLabel, String quotedText, LocalDateTime createdAt)
    {
        this.id = id;
        this.bookId = bookId;
        this.sourceSessionId = sourceSessionId;
        this.pageNumber = pageNumber;
        this.chapterLabel = chapterLabel;
        this.quotedText = quotedText;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getSourceSessionId() {
        return sourceSessionId;
    }

    public void setSourceSessionId(Long sourceSessionId) {
        this.sourceSessionId = sourceSessionId;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getChapterLabel() {
        return chapterLabel;
    }

    public void setChapterLabel(String chapterLabel) {
        this.chapterLabel = chapterLabel;
    }

    public String getQuotedText() {
        return quotedText;
    }

    public void setQuotedText(String quotedText) {
        this.quotedText = quotedText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
