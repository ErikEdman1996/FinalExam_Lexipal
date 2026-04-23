package com.lexipal.core.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class SetNextMeetingRequestDTO
{
    @NotNull(message = "Meeting date is required")
    @Future(message = "Meeting date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime meetingDate;

    @Size(max = 100, message = "Chapter label must be 100 characters or fewer")
    private String chapterLabel;

    @Min(value = 1, message = "Page start must be at least 1")
    private Integer pageStart;

    @Min(value = 1, message = "Page end must be at least 1")
    private Integer pageEnd;

    public SetNextMeetingRequestDTO()
    {

    }

    public SetNextMeetingRequestDTO(LocalDateTime meetingDate, String chapterLabel,
                                    Integer pageStart, Integer pageEnd)
    {
        this.meetingDate = meetingDate;
        this.chapterLabel = chapterLabel;
        this.pageStart = pageStart;
        this.pageEnd = pageEnd;
    }

    public LocalDateTime getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(LocalDateTime meetingDate) {
        this.meetingDate = meetingDate;
    }

    public String getChapterLabel() {
        return chapterLabel;
    }

    public void setChapterLabel(String chapterLabel) {
        this.chapterLabel = chapterLabel;
    }

    public Integer getPageStart() {
        return pageStart;
    }

    public void setPageStart(Integer pageStart) {
        this.pageStart = pageStart;
    }

    public Integer getPageEnd() {
        return pageEnd;
    }

    public void setPageEnd(Integer pageEnd) {
        this.pageEnd = pageEnd;
    }
}
