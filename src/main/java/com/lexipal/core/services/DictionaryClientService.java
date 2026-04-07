package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.DictionaryAPIRequestDTO;
import com.lexipal.core.exceptions.ExternalServiceException;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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
        try {
            // The API returns an array: [ { "word": "...", ... } ]
            // We use ParameterizedTypeReference to handle the List wrapper safely
            List<DictionaryAPIRequestDTO> response = restClient.get()
                    .uri("/{word}", word)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, res) -> {
                        throw new ResourceNotFoundException("Word", "lemma", word);
                    })
                    .body(new ParameterizedTypeReference<List<DictionaryAPIRequestDTO>>() {});

            if (response != null && !response.isEmpty()) {
                return response.get(0);
            }

            return null;
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Dictionary service is unavailable for word: " + word, ex);
        }
    }
}
