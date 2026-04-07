package com.lexipal.core.controllers;


import com.lexipal.core.dtos.requests.CaptureCreateRequestDTO;
import com.lexipal.core.dtos.responses.CaptureCreateResponseDTO;
import com.lexipal.core.dtos.responses.CaptureReadResponseDTO;
import com.lexipal.core.services.CapturedWordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/all")
    public ResponseEntity<List<CaptureReadResponseDTO>> getAllCapturedWords(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        List<CaptureReadResponseDTO> words = capturedWordService.getAllWordsBySub(keycloakSub);

        return ResponseEntity.ok(words);
    }

    @PostMapping("/{sessionId}")
    public ResponseEntity<CaptureCreateResponseDTO> searchAndCapture(@PathVariable Long sessionId,
                                                                     @Valid @RequestBody CaptureCreateRequestDTO requestDTO,
                                                                     @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        CaptureCreateResponseDTO response = capturedWordService.performSearchAndCapture(sessionId, requestDTO, keycloakSub);

        return ResponseEntity.ok(response);
    }
}
