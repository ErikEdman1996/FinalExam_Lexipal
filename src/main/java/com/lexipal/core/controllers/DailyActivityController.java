package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.DailyActivityReadResponseDTO;
import com.lexipal.core.services.DailyActivityServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/dailyActivities")
public class DailyActivityController
{
    private final DailyActivityServiceInterface dailyActivityService;

    @Autowired
    public DailyActivityController(final DailyActivityServiceInterface dailyActivityService)
    {
        this.dailyActivityService = dailyActivityService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<DailyActivityReadResponseDTO>> getAllActivities(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(dailyActivityService.getDailyActivitiesByUser(keycloakSub));
    }
}
