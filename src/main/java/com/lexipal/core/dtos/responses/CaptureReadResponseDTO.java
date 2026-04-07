package com.lexipal.core.dtos.responses;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class CaptureReadResponseDTO
{
    private Long id;
    private Long wordId;
    private Long sessionId;
    private String lemma;
    private String specificDefinition;
    private Integer xpValue;
    private LocalDateTime capturedAt;
    private Double rarityScore;
    private String rarityTier;
    private String source;
    private JsonNode definitions;
    private JsonNode phonetics;

    public CaptureReadResponseDTO()
    {

    }

    public CaptureReadResponseDTO(Long id, Long wordId, Long sessionId, String lemma, String specificDefinition,
                                  Integer xpValue, LocalDateTime capturedAt, Double rarityScore, String source,
                                  JsonNode definitions, JsonNode phonetics)
    {
        this.id = id;
        this.wordId = wordId;
        this.sessionId = sessionId;
        this.lemma = lemma;
        this.specificDefinition = specificDefinition;
        this.xpValue = xpValue;
        this.capturedAt = capturedAt;
        this.rarityScore = rarityScore;
        this.rarityTier = determineTier(rarityScore);
        this.source = source;
        this.definitions = definitions;
        this.phonetics = phonetics;
    }

    private String determineTier(Double score)
    {
        if (score == null) return "COMMON";
        if (score >= 0.9) return "MYTHIC";
        if (score >= 0.7) return "RARE";
        return "COMMON";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getSpecificDefinition() {
        return specificDefinition;
    }

    public void setSpecificDefinition(String specificDefinition) {
        this.specificDefinition = specificDefinition;
    }

    public Integer getXpValue() {
        return xpValue;
    }

    public void setXpValue(Integer xpValue) {
        this.xpValue = xpValue;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    public Double getRarityScore() {
        return rarityScore;
    }

    public void setRarityScore(Double rarityScore) {
        this.rarityScore = rarityScore;
        this.rarityTier = determineTier(rarityScore);
    }

    public String getRarityTier() {
        return rarityTier;
    }

    public void setRarityTier(String rarityTier) {
        this.rarityTier = rarityTier;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public JsonNode getDefinitions() {
        return definitions;
    }

    public void setDefinitions(JsonNode definitions) {
        this.definitions = definitions;
    }

    public JsonNode getPhonetics() {
        return phonetics;
    }

    public void setPhonetics(JsonNode phonetics) {
        this.phonetics = phonetics;
    }
}
