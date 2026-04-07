package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.SessionCreateRequestDTO;
import com.lexipal.core.dtos.requests.SessionUpdateRequestDTO;
import com.lexipal.core.dtos.responses.SessionCreateResponseDTO;
import com.lexipal.core.dtos.responses.SessionReadResponseDTO;
import com.lexipal.core.dtos.responses.SessionUpdateResponseDTO;
import com.lexipal.core.services.SessionServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/sessions")
public class SessionController
{
    private final SessionServiceInterface sessionService;

    @Autowired
    public SessionController(final SessionServiceInterface sessionService)
    {
        this.sessionService = sessionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionReadResponseDTO> getSession(@PathVariable final Long id, @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        SessionReadResponseDTO session = sessionService.getSession(id, keycloakSub);

        return ResponseEntity.ok(session);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<SessionReadResponseDTO>> getSessions(@PathVariable final Long bookId, @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        List<SessionReadResponseDTO> sessions = sessionService.getSessionsByBook(bookId, keycloakSub);

        return ResponseEntity.ok(sessions);
    }


    @PostMapping
    public ResponseEntity<SessionCreateResponseDTO> addSession(@Valid @RequestBody final SessionCreateRequestDTO dto,
                                                               @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.addSession(dto, keycloakSub));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionUpdateResponseDTO> updateSession(@PathVariable Long id,
                                                                  @Valid @RequestBody SessionUpdateRequestDTO dto,
                                                                  @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        SessionUpdateResponseDTO updatesSession = sessionService.updateSession(id, dto, keycloakSub);

        return ResponseEntity.ok(updatesSession);
    }
}
