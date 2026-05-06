package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CircleNoteDTO
{
    private Long noteId;
    private String quotedText;
    private Integer pageNumber;
    private String chapterLabel;
    private LocalDateTime createdAt;
    private Long sourceSessionId;
    private List<NoteTagResponseDTO> tags = new ArrayList<>();

    public CircleNoteDTO()
    {

    }

    public CircleNoteDTO(Long noteId, String quotedText, Integer pageNumber, String chapterLabel,
                         LocalDateTime createdAt, Long sourceSessionId)
    {
        this(noteId, quotedText, pageNumber, chapterLabel, createdAt, sourceSessionId, new ArrayList<>());
    }

    public CircleNoteDTO(Long noteId, String quotedText, Integer pageNumber, String chapterLabel,
                         LocalDateTime createdAt, Long sourceSessionId, List<NoteTagResponseDTO> tags)
    {
        this.noteId = noteId;
        this.quotedText = quotedText;
        this.pageNumber = pageNumber;
        this.chapterLabel = chapterLabel;
        this.createdAt = createdAt;
        this.sourceSessionId = sourceSessionId;
        this.tags = tags == null ? new ArrayList<>() : tags;
    }

    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }

    public String getQuotedText() { return quotedText; }
    public void setQuotedText(String quotedText) { this.quotedText = quotedText; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public String getChapterLabel() { return chapterLabel; }
    public void setChapterLabel(String chapterLabel) { this.chapterLabel = chapterLabel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getSourceSessionId() { return sourceSessionId; }
    public void setSourceSessionId(Long sourceSessionId) { this.sourceSessionId = sourceSessionId; }

    public List<NoteTagResponseDTO> getTags() { return tags; }
    public void setTags(List<NoteTagResponseDTO> tags) { this.tags = tags == null ? new ArrayList<>() : tags; }
}
