package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.ProfileReadResponseDTO;
import com.lexipal.core.services.ProfileServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lexipal/api/v1/profile")
public class ProfileController
{
    private final ProfileServiceInterface profileService;

    @Autowired
    public ProfileController(final ProfileServiceInterface profileService)
    {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileReadResponseDTO> getMyProfile(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(profileService.getMyProfile(keycloakSub));
    }
}
