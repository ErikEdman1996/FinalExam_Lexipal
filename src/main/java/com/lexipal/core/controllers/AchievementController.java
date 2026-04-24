package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.AchievementResponseDTO;
import com.lexipal.core.services.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/achievements")
public class AchievementController
{
    private final AchievementService achievementService;

    @Autowired
    public AchievementController(final AchievementService achievementService)
    {
        this.achievementService = achievementService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<AchievementResponseDTO>> getMyAchievements(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(achievementService.getAchievementsForUser(jwt.getSubject()));
    }
}
