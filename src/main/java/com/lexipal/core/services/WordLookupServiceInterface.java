package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.WordLookupCreateRequestDTO;
import com.lexipal.core.dtos.responses.WordLookupCreateResponseDTO;
import com.lexipal.core.dtos.responses.WordLookupReadResponseDTO;

import java.util.List;

public interface WordLookupServiceInterface
{
    WordLookupCreateResponseDTO lookupWord(Long sessionId, WordLookupCreateRequestDTO dto, String keycloakSub);

    List<WordLookupReadResponseDTO> getLookupsForCurrentUser(String keycloakSub);
}
