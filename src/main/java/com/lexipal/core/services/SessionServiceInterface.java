package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.SessionCreateRequestDTO;
import com.lexipal.core.dtos.requests.SessionUpdateRequestDTO;
import com.lexipal.core.dtos.responses.SessionCreateResponseDTO;
import com.lexipal.core.dtos.responses.SessionReadResponseDTO;
import com.lexipal.core.dtos.responses.SessionUpdateResponseDTO;

import java.util.List;

public interface SessionServiceInterface
{
    SessionCreateResponseDTO addSession(SessionCreateRequestDTO dto, String keycloakSub);
    SessionUpdateResponseDTO updateSession(Long sessionId, SessionUpdateRequestDTO dto, String keycloakSub);
    SessionReadResponseDTO getSession(Long sessionId, String keycloakSub);
    List<SessionReadResponseDTO> getSessionsByBook(Long bookId, String keycloakSub);
}
