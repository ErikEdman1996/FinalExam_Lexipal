package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class CrosswordPuzzleSubmitRequestDTO
{
    @NotNull(message = "Answers are required")
    private Map<String, String> answers;

    public CrosswordPuzzleSubmitRequestDTO()
    {

    }

    public Map<String, String> getAnswers()
    {
        return answers;
    }

    public void setAnswers(Map<String, String> answers)
    {
        this.answers = answers;
    }
}
