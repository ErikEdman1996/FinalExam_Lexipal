package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CaptureCreateRequestDTO
{
    @NotBlank(message = "Lemma is required")
    @Size(max = 100, message = "Lemma must be 100 characters or fewer")
    private String lemma;

    public CaptureCreateRequestDTO()
    {

    }

    public CaptureCreateRequestDTO(String lemma)
    {
        this.lemma = lemma;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }
}
