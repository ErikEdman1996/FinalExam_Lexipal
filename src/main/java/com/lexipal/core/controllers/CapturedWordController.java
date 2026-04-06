package com.lexipal.core.controllers;


import com.lexipal.core.dtos.CaptureRequestDTO;
import com.lexipal.core.dtos.CaptureResponseDTO;
import com.lexipal.core.services.CapturedWordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/lexipal/api/v1/capturedWords")
public class CapturedWordController
{
    private final CapturedWordService capturedWordService;

    @Autowired
    public CapturedWordController(final CapturedWordService capturedWordService)
    {
        this.capturedWordService = capturedWordService;
    }

    @PostMapping("/{sessionId}")
    public ResponseEntity<CaptureResponseDTO> searchAndCapture(@PathVariable Long sessionId,
                                                               @Valid @RequestBody CaptureRequestDTO requestDTO,
                                                               @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        CaptureResponseDTO response = capturedWordService.performSearchAndCapture(sessionId, requestDTO, keycloakSub);

        return ResponseEntity.ok(response);
    }
}
