package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.StreakMetaDataCreateRequestDTO;
import com.lexipal.core.dtos.responses.StreakMetaDataReadResponseDTO;
import com.lexipal.core.services.StreakMetaDataServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lexipal/api/v1/streakMetadata")
public class StreakMetaDataController
{
    private final StreakMetaDataServiceInterface streakMetaDataService;

    @Autowired
    public StreakMetaDataController(final StreakMetaDataServiceInterface streakMetaDataService)
    {
        this.streakMetaDataService = streakMetaDataService;
    }

    @GetMapping("/me")
    public ResponseEntity<StreakMetaDataReadResponseDTO> getMyStreak(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(streakMetaDataService.getStreakMetaData(keycloakSub));
    }

    @PostMapping("/initialize")
    public ResponseEntity<StreakMetaDataReadResponseDTO> initializeStreak(@Valid @RequestBody StreakMetaDataCreateRequestDTO dto,
                                                                          @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(streakMetaDataService.initializeStreakMetaData(dto, keycloakSub));
    }
}
