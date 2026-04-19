package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class NoteUpdateRequestDTO
{
    private String quotedText;

    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNumber;

    @Size(max = 100, message = "Chapter label must be 100 characters or fewer")
    private String chapterLabel;

    public NoteUpdateRequestDTO()
    {

    }

    public NoteUpdateRequestDTO(String quotedText, Integer pageNumber, String chapterLabel)
    {
        this.quotedText = quotedText;
        this.pageNumber = pageNumber;
        this.chapterLabel = chapterLabel;
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
}
