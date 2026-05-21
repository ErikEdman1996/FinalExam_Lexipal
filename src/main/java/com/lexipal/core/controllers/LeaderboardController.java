package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.StreakLeaderboardResponseDTO;
import com.lexipal.core.services.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lexipal/api/v1/leaderboards")
public class LeaderboardController
{
    private final LeaderboardService leaderboardService;

    public LeaderboardController(final LeaderboardService leaderboardService)
    {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/streaks")
    public ResponseEntity<StreakLeaderboardResponseDTO> getStreakLeaderboard(
            @RequestParam(required = false) Integer limit)
    {
        return ResponseEntity.ok(leaderboardService.getStreakLeaderboard(limit));
    }
}
