package com.lexipal.core.services;

import com.lexipal.core.dtos.SessionRequestDTO;
import com.lexipal.core.dtos.SessionUpdateDTO;
import com.lexipal.core.entities.Session;

import java.util.List;

public interface SessionServiceInterface
{
    Session addSession(SessionRequestDTO dto, String keycloakSub);
    Session updateSession(Long sessionId, SessionUpdateDTO dto, String keycloakSub);
    Session getSession(Long sessionId, String keycloakSub);
    List<Session> getSessionsByBook(Long bookId, String keycloakSub);
}
