package com.lexipal.core.dtos.responses;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class WordLookupReadResponseDTO
{
    private Long id;
    private Long wordId;
    private String lemma;
    private String source;
    private JsonNode definitions;
    private JsonNode phonetics;
    private LocalDateTime firstLookedUpAt;
    private LocalDateTime lastLookedUpAt;
    private Integer lookupCount;

    public WordLookupReadResponseDTO()
    {

    }

    public WordLookupReadResponseDTO(Long id, Long wordId, String lemma, String source,
                                     JsonNode definitions, JsonNode phonetics,
                                     LocalDateTime firstLookedUpAt, LocalDateTime lastLookedUpAt,
                                     Integer lookupCount)
    {
        this.id = id;
        this.wordId = wordId;
        this.lemma = lemma;
        this.source = source;
        this.definitions = definitions;
        this.phonetics = phonetics;
        this.firstLookedUpAt = firstLookedUpAt;
        this.lastLookedUpAt = lastLookedUpAt;
        this.lookupCount = lookupCount;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public JsonNode getDefinitions()
    {
        return definitions;
    }

    public void setDefinitions(JsonNode definitions)
    {
        this.definitions = definitions;
    }

    public JsonNode getPhonetics()
    {
        return phonetics;
    }

    public void setPhonetics(JsonNode phonetics)
    {
        this.phonetics = phonetics;
    }

    public LocalDateTime getFirstLookedUpAt()
    {
        return firstLookedUpAt;
    }

    public void setFirstLookedUpAt(LocalDateTime firstLookedUpAt)
    {
        this.firstLookedUpAt = firstLookedUpAt;
    }

    public LocalDateTime getLastLookedUpAt()
    {
        return lastLookedUpAt;
    }

    public void setLastLookedUpAt(LocalDateTime lastLookedUpAt)
    {
        this.lastLookedUpAt = lastLookedUpAt;
    }

    public Integer getLookupCount()
    {
        return lookupCount;
    }

    public void setLookupCount(Integer lookupCount)
    {
        this.lookupCount = lookupCount;
    }
}
