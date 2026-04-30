package com.lexipal.core.dtos.responses;

import com.fasterxml.jackson.databind.JsonNode;

public class CrosswordPuzzleSubmitResponseDTO
{
    private CrosswordPuzzleReadResponseDTO puzzle;
    private ChallengeAttemptReadResponseDTO attempt;
    private String result;
    private Integer correctCount;
    private Integer totalEntries;
    private JsonNode answerResults;

    public CrosswordPuzzleSubmitResponseDTO()
    {

    }

    public CrosswordPuzzleSubmitResponseDTO(CrosswordPuzzleReadResponseDTO puzzle,
                                            ChallengeAttemptReadResponseDTO attempt,
                                            String result,
                                            Integer correctCount,
                                            Integer totalEntries,
                                            JsonNode answerResults)
    {
        this.puzzle = puzzle;
        this.attempt = attempt;
        this.result = result;
        this.correctCount = correctCount;
        this.totalEntries = totalEntries;
        this.answerResults = answerResults;
    }

    public CrosswordPuzzleReadResponseDTO getPuzzle()
    {
        return puzzle;
    }

    public void setPuzzle(CrosswordPuzzleReadResponseDTO puzzle)
    {
        this.puzzle = puzzle;
    }

    public ChallengeAttemptReadResponseDTO getAttempt()
    {
        return attempt;
    }

    public void setAttempt(ChallengeAttemptReadResponseDTO attempt)
    {
        this.attempt = attempt;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public Integer getCorrectCount()
    {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount)
    {
        this.correctCount = correctCount;
    }

    public Integer getTotalEntries()
    {
        return totalEntries;
    }

    public void setTotalEntries(Integer totalEntries)
    {
        this.totalEntries = totalEntries;
    }

    public JsonNode getAnswerResults()
    {
        return answerResults;
    }

    public void setAnswerResults(JsonNode answerResults)
    {
        this.answerResults = answerResults;
    }
}
