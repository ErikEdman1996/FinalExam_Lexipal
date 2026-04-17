package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class SessionCompleteRequestDTO
{
    @Min(value = 0, message = "End page must be zero or greater")
    private Integer endPage;

    @Min(value = 0, message = "Duration must be zero or greater")
    private Long durationSeconds;

    @Size(max = 2000, message = "Reflection must be 2000 characters or fewer")
    private String reflection;

    public SessionCompleteRequestDTO()
    {

    }

    public SessionCompleteRequestDTO(Integer endPage, Long durationSeconds, String reflection)
    {
        this.endPage = endPage;
        this.durationSeconds = durationSeconds;
        this.reflection = reflection;
    }

    public Integer getEndPage()
    {
        return endPage;
    }

    public void setEndPage(Integer endPage)
    {
        this.endPage = endPage;
    }

    public Long getDurationSeconds()
    {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds)
    {
        this.durationSeconds = durationSeconds;
    }

    public String getReflection()
    {
        return reflection;
    }

    public void setReflection(String reflection)
    {
        this.reflection = reflection;
    }
}
