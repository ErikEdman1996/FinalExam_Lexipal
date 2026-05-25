package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.DailyQuestResponseDTO;
import com.lexipal.core.services.DailyQuestServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lexipal/api/v1/daily-quests")
public class DailyQuestController
{
    private final DailyQuestServiceInterface dailyQuestService;

    public DailyQuestController(final DailyQuestServiceInterface dailyQuestService)
    {
        this.dailyQuestService = dailyQuestService;
    }

    @GetMapping("/me/today")
    public ResponseEntity<DailyQuestResponseDTO> getTodayQuest(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(dailyQuestService.getTodayQuest(jwt.getSubject()));
    }
}
