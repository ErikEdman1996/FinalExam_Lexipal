package com.lexipal.core.dtos.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DictionaryAPIRequestDTO
{

    private String word;
    private String phonetic;

    private JsonNode phonetics;
    private JsonNode meanings;
    private String origin;

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

    public String getPhonetic()
    {
        return phonetic;
    }

    public void setPhonetic(String phonetic)
    {
        this.phonetic = phonetic;
    }

    public JsonNode getPhonetics()
    {
        return phonetics;
    }

    public void setPhonetics(JsonNode phonetics)
    {
        this.phonetics = phonetics;
    }

    public JsonNode getMeanings()
    {
        return meanings;
    }

    public void setMeanings(JsonNode meanings)
    {
        this.meanings = meanings;
    }

    public String getOrigin()
    {
        return origin;
    }

    public void setOrigin(String origin)
    {
        this.origin = origin;
    }
}