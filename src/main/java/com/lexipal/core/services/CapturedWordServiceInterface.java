package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.CaptureCreateRequestDTO;
import com.lexipal.core.dtos.responses.CaptureCreateResponseDTO;

public interface CapturedWordServiceInterface
{
    CaptureCreateResponseDTO performSearchAndCapture(Long sessionId, CaptureCreateRequestDTO dto, String keycloakSub);
}
