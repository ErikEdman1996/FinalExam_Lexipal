package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;

public class ChallengeAttemptReadResponseDTO
{
    private Long id;
    private String userAnswer;
    private String result;
    private Integer xpAwarded;
    private LocalDateTime attemptedAt;
    private Long challengeId;
    private String challengeType;
    private Long wordLookupId;
    private Long wordId;
    private String lemma;
    private String definitionPreview;
    private XpUpdateResponseDTO xpUpdate;

    public ChallengeAttemptReadResponseDTO()
    {

    }

    public ChallengeAttemptReadResponseDTO(Long id, String userAnswer, String result, Integer xpAwarded,
                                           LocalDateTime attemptedAt, Long challengeId, String challengeType,
                                           Long wordLookupId, Long wordId, String lemma, String definitionPreview,
                                           XpUpdateResponseDTO xpUpdate)
    {
        this.id = id;
        this.userAnswer = userAnswer;
        this.result = result;
        this.xpAwarded = xpAwarded;
        this.attemptedAt = attemptedAt;
        this.challengeId = challengeId;
        this.challengeType = challengeType;
        this.wordLookupId = wordLookupId;
        this.wordId = wordId;
        this.lemma = lemma;
        this.definitionPreview = definitionPreview;
        this.xpUpdate = xpUpdate;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getUserAnswer()
    {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer)
    {
        this.userAnswer = userAnswer;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public Integer getXpAwarded()
    {
        return xpAwarded;
    }

    public void setXpAwarded(Integer xpAwarded)
    {
        this.xpAwarded = xpAwarded;
    }

    public LocalDateTime getAttemptedAt()
    {
        return attemptedAt;
    }

    public void setAttemptedAt(LocalDateTime attemptedAt)
    {
        this.attemptedAt = attemptedAt;
    }

    public Long getChallengeId()
    {
        return challengeId;
    }

    public void setChallengeId(Long challengeId)
    {
        this.challengeId = challengeId;
    }

    public String getChallengeType()
    {
        return challengeType;
    }

    public void setChallengeType(String challengeType)
    {
        this.challengeType = challengeType;
    }

    public Long getWordLookupId()
    {
        return wordLookupId;
    }

    public void setWordLookupId(Long wordLookupId)
    {
        this.wordLookupId = wordLookupId;
    }

    public Long getWordId()
    {
        return wordId;
    }

    public void setWordId(Long wordId)
    {
        this.wordId = wordId;
    }

    public String getLemma()
    {
        return lemma;
    }

    public void setLemma(String lemma)
    {
        this.lemma = lemma;
    }

    public String getDefinitionPreview()
    {
        return definitionPreview;
    }

    public void setDefinitionPreview(String definitionPreview)
    {
        this.definitionPreview = definitionPreview;
    }

    public XpUpdateResponseDTO getXpUpdate()
    {
        return xpUpdate;
    }

    public void setXpUpdate(XpUpdateResponseDTO xpUpdate)
    {
        this.xpUpdate = xpUpdate;
    }
}
