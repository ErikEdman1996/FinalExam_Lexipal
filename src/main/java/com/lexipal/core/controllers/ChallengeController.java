package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.ChallengeReadResponseDTO;
import com.lexipal.core.services.ChallengeServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/challenges")
public class ChallengeController
{
    private final ChallengeServiceInterface challengeService;

    @Autowired
    public ChallengeController(final ChallengeServiceInterface challengeService)
    {
        this.challengeService = challengeService;
    }

    @GetMapping
    public ResponseEntity<List<ChallengeReadResponseDTO>> getAllChallenges()
    {
        return ResponseEntity.ok(challengeService.getAllChallenges());
    }

    @GetMapping("/{challengeId}")
    public ResponseEntity<ChallengeReadResponseDTO> getChallengeById(@PathVariable Long challengeId)
    {
        return ResponseEntity.ok(challengeService.getChallengeById(challengeId));
    }
}
