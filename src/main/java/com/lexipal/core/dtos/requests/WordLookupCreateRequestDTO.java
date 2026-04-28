package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WordLookupCreateRequestDTO
{
    @NotBlank(message = "Lemma is required")
    @Size(max = 100, message = "Lemma must be 100 characters or fewer")
    private String lemma;

    public WordLookupCreateRequestDTO()
    {

    }

    public WordLookupCreateRequestDTO(String lemma)
    {
        this.lemma = lemma;
    }

    public String getLemma()
    {
        return lemma;
    }

    public void setLemma(String lemma)
    {
        this.lemma = lemma;
    }
}
