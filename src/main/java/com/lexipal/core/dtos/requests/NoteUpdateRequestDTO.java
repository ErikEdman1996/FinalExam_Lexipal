package com.lexipal.core.dtos.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public class NoteUpdateRequestDTO
{
    private String quotedText;

    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNumber;

    @Size(max = 100, message = "Chapter label must be 100 characters or fewer")
    private String chapterLabel;

    private List<Long> tagIds;

    @JsonIgnore
    private boolean tagIdsProvided;

    public NoteUpdateRequestDTO()
    {

    }

    public NoteUpdateRequestDTO(String quotedText, Integer pageNumber, String chapterLabel)
    {
        this(quotedText, pageNumber, chapterLabel, null, false);
    }

    public NoteUpdateRequestDTO(String quotedText, Integer pageNumber, String chapterLabel, List<Long> tagIds)
    {
        this(quotedText, pageNumber, chapterLabel, tagIds, true);
    }

    private NoteUpdateRequestDTO(String quotedText, Integer pageNumber, String chapterLabel,
                                 List<Long> tagIds, boolean tagIdsProvided)
    {
        this.quotedText = quotedText;
        this.pageNumber = pageNumber;
        this.chapterLabel = chapterLabel;
        this.tagIds = tagIds;
        this.tagIdsProvided = tagIdsProvided;
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
        this.tagIdsProvided = true;
    }

    public boolean isTagIdsProvided() {
        return tagIdsProvided;
    }
}
