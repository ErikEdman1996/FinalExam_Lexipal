package com.lexipal.core.dtos.requests;

import com.lexipal.core.entities.Session;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class SessionUpdateRequestDTO
{
    @Min(value = 0, message = "End page must be zero or greater")
    private Integer endPage;

    @Min(value = 0, message = "Duration must be zero or greater")
    private Long durationSeconds;

    @Size(max = 2000, message = "Reflection must be 2000 characters or fewer")
    private String reflection;
    private Session.SessionStatus status;

    public SessionUpdateRequestDTO()
    {

    }

    public SessionUpdateRequestDTO(Integer endPage, Long durationSeconds, String reflection, Session.SessionStatus status)
    {
        this.endPage = endPage;
        this.durationSeconds = durationSeconds;
        this.reflection = reflection;
        this.status = status;
    }

    public Integer getEndPage() {
        return endPage;
    }

    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getReflection() {
        return reflection;
    }

    public void setReflection(String reflection) {
        this.reflection = reflection;
    }

    public Session.SessionStatus getStatus() {
        return status;
    }

    public void setStatus(Session.SessionStatus status) {
        this.status = status;
    }
}
