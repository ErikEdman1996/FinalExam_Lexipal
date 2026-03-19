package com.lexipal.core.dtos;

public class SessionUpdateDTO
{
    private Integer endPage;
    private Long durationSeconds;
    private String reflection;

    public SessionUpdateDTO()
    {

    }

    public SessionUpdateDTO(Integer endPage, Long durationSeconds, String reflection)
    {
        this.endPage = endPage;
        this.durationSeconds = durationSeconds;
        this.reflection = reflection;
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
}
