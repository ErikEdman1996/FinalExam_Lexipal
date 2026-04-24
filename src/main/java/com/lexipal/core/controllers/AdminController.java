package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.admin.AdminAnalyticsRange;
import com.lexipal.core.dtos.responses.admin.AdminDashboardResponseDTO;
import com.lexipal.core.services.AdminAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lexipal/api/v1/admin")
public class AdminController
{
    private final AdminAnalyticsService adminAnalyticsService;

    public AdminController(final AdminAnalyticsService adminAnalyticsService)
    {
        this.adminAnalyticsService = adminAnalyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponseDTO> getDashboard(
            @RequestParam(required = false, defaultValue = "THIS_MONTH") AdminAnalyticsRange range)
    {
        return ResponseEntity.ok(adminAnalyticsService.getDashboard(range));
    }
}
