package com.lexipal.core.services;

import com.lexipal.core.dtos.CaptureRequestDTO;
import com.lexipal.core.dtos.CaptureResponseDTO;

public interface CapturedWordServiceInterface
{
    CaptureResponseDTO performSearchAndCapture(Long sessionId, CaptureRequestDTO dto, String keycloakSub);
}
