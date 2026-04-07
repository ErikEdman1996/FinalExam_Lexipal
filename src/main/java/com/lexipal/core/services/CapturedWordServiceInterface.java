package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.CaptureCreateRequestDTO;
import com.lexipal.core.dtos.responses.CaptureCreateResponseDTO;
import com.lexipal.core.dtos.responses.CaptureReadResponseDTO;

import java.util.List;

public interface CapturedWordServiceInterface
{
    CaptureCreateResponseDTO performSearchAndCapture(Long sessionId, CaptureCreateRequestDTO dto, String keycloakSub);
    List<CaptureReadResponseDTO> getAllWordsBySub(String keycloakSub);
}
