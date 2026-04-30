package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.CrosswordPuzzleSubmitRequestDTO;
import com.lexipal.core.dtos.responses.CrosswordPuzzleReadResponseDTO;
import com.lexipal.core.dtos.responses.CrosswordPuzzleSubmitResponseDTO;
import com.lexipal.core.services.CrosswordPuzzleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lexipal/api/v1/challenges/crossword/puzzles")
public class CrosswordPuzzleController
{
    private final CrosswordPuzzleService crosswordPuzzleService;

    public CrosswordPuzzleController(final CrosswordPuzzleService crosswordPuzzleService)
    {
        this.crosswordPuzzleService = crosswordPuzzleService;
    }

    @PostMapping
    public ResponseEntity<CrosswordPuzzleReadResponseDTO> createPuzzle(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(crosswordPuzzleService.createPuzzle(jwt.getSubject()));
    }

    @GetMapping("/{puzzleId}")
    public ResponseEntity<CrosswordPuzzleReadResponseDTO> getPuzzle(@PathVariable Long puzzleId,
                                                                    @AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(crosswordPuzzleService.getPuzzle(puzzleId, jwt.getSubject()));
    }

    @PostMapping("/{puzzleId}/submit")
    public ResponseEntity<CrosswordPuzzleSubmitResponseDTO> submitPuzzle(@PathVariable Long puzzleId,
                                                                         @Valid @RequestBody CrosswordPuzzleSubmitRequestDTO requestDTO,
                                                                         @AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(crosswordPuzzleService.submitPuzzle(puzzleId, requestDTO, jwt.getSubject()));
    }
}
