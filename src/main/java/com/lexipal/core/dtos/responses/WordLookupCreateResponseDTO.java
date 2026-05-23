package com.lexipal.core.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WordLookupCreateResponseDTO
{
    private WordLookupReadResponseDTO wordLookup;
    private boolean lookupSucceeded;
    private boolean newLookup;

    public WordLookupCreateResponseDTO()
    {

    }

    public WordLookupCreateResponseDTO(WordLookupReadResponseDTO wordLookup,
                                       boolean lookupSucceeded,
                                       boolean newLookup)
    {
        this.wordLookup = wordLookup;
        this.lookupSucceeded = lookupSucceeded;
        this.newLookup = newLookup;
    }

    public WordLookupReadResponseDTO getWordLookup()
    {
        return wordLookup;
    }

    public void setWordLookup(WordLookupReadResponseDTO wordLookup)
    {
        this.wordLookup = wordLookup;
    }

    public boolean isLookupSucceeded()
    {
        return lookupSucceeded;
    }

    public void setLookupSucceeded(boolean lookupSucceeded)
    {
        this.lookupSucceeded = lookupSucceeded;
    }

    @JsonProperty("isNewLookup")
    public boolean isNewLookup()
    {
        return newLookup;
    }

    public void setNewLookup(boolean newLookup)
    {
        this.newLookup = newLookup;
    }
}
