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

    private Long codexEntryId;
    private Long wordId;

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

    public Long getCodexEntryId()
    {
        return codexEntryId;
    }

    public void setCodexEntryId(Long codexEntryId)
    {
        this.codexEntryId = codexEntryId;
    }

    public Long getWordId()
    {
        return wordId;
    }

    public void setWordId(Long wordId)
    {
        this.wordId = wordId;
    }
}
