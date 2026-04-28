package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.CodexEntryReadResponseDTO;

import java.util.List;

public interface CodexEntryServiceInterface
{
    List<CodexEntryReadResponseDTO> getAllCodexEntriesBySub(String keycloakSub);
}
