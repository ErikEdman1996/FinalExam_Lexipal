package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.CodexEntryCreateRequestDTO;
import com.lexipal.core.dtos.responses.CodexEntryCreateResponseDTO;
import com.lexipal.core.dtos.responses.CodexEntryReadResponseDTO;

import java.util.List;

public interface CodexEntryServiceInterface
{
    CodexEntryCreateResponseDTO searchAndAddToCodex(Long sessionId, CodexEntryCreateRequestDTO dto, String keycloakSub);

    List<CodexEntryReadResponseDTO> getAllCodexEntriesBySub(String keycloakSub);
}
