package com.lexipal.core.services;

import com.lexipal.core.dtos.DictionaryAPIRequestDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class DictionaryClientService
{
    final private RestClient restClient;

    public DictionaryClientService(final RestClient.Builder clientBuilder)
    {
        restClient = clientBuilder
                .baseUrl("https://api.dictionaryapi.dev/api/v2/entries/en/")
                .build();
    }

    public DictionaryAPIRequestDTO fetchWordFromApi(String word)
    {
        // The API returns an array: [ { "word": "...", ... } ]
        // We use ParameterizedTypeReference to handle the List wrapper safely
        List<DictionaryAPIRequestDTO> response = restClient.get()
                .uri("/{word}", word)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, res) -> {
                    // You might want to throw a custom exception here for your Exam Project
                    throw new RuntimeException("Word not found in dictionary: " + word);
                })
                .body(new ParameterizedTypeReference<List<DictionaryAPIRequestDTO>>() {});

        // Return the first entry if it exists
        if (response != null && !response.isEmpty()) {
            return response.get(0);
        }

        return null;
    }
}
