package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.WordLookupCreateRequestDTO;
import com.lexipal.core.dtos.responses.WordLookupCreateResponseDTO;
import com.lexipal.core.dtos.responses.WordLookupReadResponseDTO;
import com.lexipal.core.services.WordLookupServiceInterface;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/word-lookups")
public class WordLookupController
{
    private final WordLookupServiceInterface wordLookupService;

    public WordLookupController(final WordLookupServiceInterface wordLookupService)
    {
        this.wordLookupService = wordLookupService;
    }

    @GetMapping
    public ResponseEntity<List<WordLookupReadResponseDTO>> getWordLookups(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(wordLookupService.getLookupsForCurrentUser(jwt.getSubject()));
    }

    @PostMapping("/{sessionId}")
    public ResponseEntity<WordLookupCreateResponseDTO> lookupWord(@PathVariable Long sessionId,
                                                                  @Valid @RequestBody WordLookupCreateRequestDTO requestDTO,
                                                                  @AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(wordLookupService.lookupWord(sessionId, requestDTO, jwt.getSubject()));
    }
}
