package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class NoteCreateRequestDTO
{
    @NotNull(message = "Book id is required")
    private Long bookId;

    private Long sessionId;

    @NotBlank(message = "Quoted text is required")
    private String quotedText;

    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNumber;

    @Size(max = 100, message = "Chapter label must be 100 characters or fewer")
    private String chapterLabel;

    private List<Long> tagIds;

    public NoteCreateRequestDTO()
    {

    }

    public NoteCreateRequestDTO(Long bookId, Long sessionId, String quotedText,
                                Integer pageNumber, String chapterLabel)
    {
        this(bookId, sessionId, quotedText, pageNumber, chapterLabel, null);
    }

    public NoteCreateRequestDTO(Long bookId, Long sessionId, String quotedText,
                                Integer pageNumber, String chapterLabel, List<Long> tagIds)
    {
        this.bookId = bookId;
        this.sessionId = sessionId;
        this.quotedText = quotedText;
        this.pageNumber = pageNumber;
        this.chapterLabel = chapterLabel;
        this.tagIds = tagIds;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getQuotedText() {
        return quotedText;
    }

    public void setQuotedText(String quotedText) {
        this.quotedText = quotedText;
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

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
}
