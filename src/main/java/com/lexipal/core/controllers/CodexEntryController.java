package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.CodexEntryReadResponseDTO;
import com.lexipal.core.services.CodexEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/codexEntries")
public class CodexEntryController
{
    private final CodexEntryService codexEntryService;

    @Autowired
    public CodexEntryController(final CodexEntryService codexEntryService)
    {
        this.codexEntryService = codexEntryService;
    }

    @GetMapping
    public ResponseEntity<List<CodexEntryReadResponseDTO>> getAllCodexEntries(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        List<CodexEntryReadResponseDTO> words = codexEntryService.getAllCodexEntriesBySub(keycloakSub);

        return ResponseEntity.ok(words);
    }
}
