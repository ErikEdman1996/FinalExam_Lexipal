package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.WalletResponseDTO;
import com.lexipal.core.services.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lexipal/api/v1/streak-freezes")
public class StreakFreezeController
{
    private final StoreService storeService;

    public StreakFreezeController(final StoreService storeService)
    {
        this.storeService = storeService;
    }

    @PostMapping("/me/purchase")
    public ResponseEntity<WalletResponseDTO> purchaseMyStreakFreeze(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(storeService.purchaseMyStreakFreeze(jwt.getSubject()));
    }
}
