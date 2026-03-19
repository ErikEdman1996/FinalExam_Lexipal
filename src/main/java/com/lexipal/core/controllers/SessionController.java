package com.lexipal.core.controllers;

import com.lexipal.core.dtos.SessionRequestDTO;
import com.lexipal.core.dtos.SessionUpdateDTO;
import com.lexipal.core.entities.Session;
import com.lexipal.core.services.SessionService;
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
    private final SessionService sessionService;

    @Autowired
    public SessionController(final SessionService sessionService)
    {
        this.sessionService = sessionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> getSession(@PathVariable final Long id, @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        Session session = sessionService.getSession(id, keycloakSub);

        return ResponseEntity.ok(session);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Session>> getSessions(@PathVariable final Long bookId, @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        List<Session> sessions = sessionService.getSessionsByBook(bookId, keycloakSub);

        return ResponseEntity.ok(sessions);
    }


    @PostMapping
    public ResponseEntity<Session> addSession(@RequestBody final SessionRequestDTO dto,
                                              @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.addSession(dto, keycloakSub));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id,
                                                 @RequestBody SessionUpdateDTO dto,
                                                 @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        Session updatesSession = sessionService.updateSession(id, dto, keycloakSub);

        return ResponseEntity.ok(updatesSession);
    }
}
