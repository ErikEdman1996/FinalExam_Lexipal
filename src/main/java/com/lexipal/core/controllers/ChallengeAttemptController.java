package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.ChallengeAttemptCreateRequestDTO;
import com.lexipal.core.dtos.responses.ChallengeAttemptReadResponseDTO;
import com.lexipal.core.services.ChallengeAttemptServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1")
public class ChallengeAttemptController
{
    private final ChallengeAttemptServiceInterface challengeAttemptService;

    @Autowired
    public ChallengeAttemptController(final ChallengeAttemptServiceInterface challengeAttemptService)
    {
        this.challengeAttemptService = challengeAttemptService;
    }

    @GetMapping("/challengeAttempts")
    public ResponseEntity<List<ChallengeAttemptReadResponseDTO>> getAllAttempts(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        return ResponseEntity.ok(challengeAttemptService.getAttemptsForCurrentUser(keycloakSub));
    }

    @GetMapping("/challenges/{challengeId}/attempts")
    public ResponseEntity<List<ChallengeAttemptReadResponseDTO>> getAttemptsByChallenge(@PathVariable Long challengeId,
                                                                                         @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        return ResponseEntity.ok(challengeAttemptService.getAttemptsForCurrentUserByChallenge(challengeId, keycloakSub));
    }

    @PostMapping("/challenges/{challengeId}/attempts")
    public ResponseEntity<ChallengeAttemptReadResponseDTO> createAttempt(@PathVariable Long challengeId,
                                                                         @Valid @RequestBody ChallengeAttemptCreateRequestDTO requestDTO,
                                                                         @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        ChallengeAttemptReadResponseDTO response = challengeAttemptService.createAttempt(challengeId, requestDTO, keycloakSub);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
