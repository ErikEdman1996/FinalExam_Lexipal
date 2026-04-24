package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.PortraitSelectionRequestDTO;
import com.lexipal.core.dtos.responses.PortraitResponseDTO;
import com.lexipal.core.services.PortraitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/portraits")
public class PortraitController
{
    private final PortraitService portraitService;

    @Autowired
    public PortraitController(final PortraitService portraitService)
    {
        this.portraitService = portraitService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<PortraitResponseDTO>> getMyPortraits(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(portraitService.getPortraitsForUser(jwt.getSubject()));
    }

    @PatchMapping("/me/selected")
    public ResponseEntity<PortraitResponseDTO> selectPortrait(
            @Valid @RequestBody PortraitSelectionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(portraitService.selectPortrait(jwt.getSubject(), request.getPortraitCode()));
    }
}
