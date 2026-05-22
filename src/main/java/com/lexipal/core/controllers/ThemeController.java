package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.ThemeSelectionRequestDTO;
import com.lexipal.core.dtos.responses.ThemeResponseDTO;
import com.lexipal.core.services.ThemeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/themes")
public class ThemeController
{
    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService)
    {
        this.themeService = themeService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<ThemeResponseDTO>> getMyThemes(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(themeService.getThemesForUser(jwt.getSubject()));
    }

    @PatchMapping("/me/selected")
    public ResponseEntity<ThemeResponseDTO> selectTheme(
            @Valid @RequestBody ThemeSelectionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(themeService.selectTheme(jwt.getSubject(), request.getThemeCode()));
    }
}
