package com.lexipal.core.dtos.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DictionaryAPIRequestDTO
{

    private String word;
    private JsonNode entries;
    private JsonNode source;

    public DictionaryAPIRequestDTO()
    {

    }

    // Getters and Setters
    public String getWord()
    {
        return word;
    }

    public void setWord(String word)
    {
        this.word = word;
    }

    public JsonNode getEntries()
    {
        return entries;
    }

    public void setEntries(JsonNode entries)
    {
        this.entries = entries;
    }

    public JsonNode getSource()
    {
        return source;
    }

    public void setSource(JsonNode source)
    {
        this.source = source;
    }
}
