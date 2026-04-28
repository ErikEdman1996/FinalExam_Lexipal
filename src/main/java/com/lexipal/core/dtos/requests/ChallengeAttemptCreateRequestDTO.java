package com.lexipal.core.dtos.requests;

import com.lexipal.core.entities.ChallengeResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChallengeAttemptCreateRequestDTO
{
    @Size(max = 1000, message = "User answer must be 1000 characters or fewer")
    private String userAnswer;

    @NotNull(message = "Result is required")
    private ChallengeResult result;

    @NotNull(message = "wordLookupId is required")
    private Long wordLookupId;

    public ChallengeAttemptCreateRequestDTO()
    {

    }

    public String getUserAnswer()
    {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer)
    {
        this.userAnswer = userAnswer;
    }

    public ChallengeResult getResult()
    {
        return result;
    }

    public void setResult(ChallengeResult result)
    {
        this.result = result;
    }

    public Long getWordLookupId()
    {
        return wordLookupId;
    }

    public void setWordLookupId(Long wordLookupId)
    {
        this.wordLookupId = wordLookupId;
    }
}
