package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.WalletResponseDTO;
import com.lexipal.core.services.CurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lexipal/api/v1/wallet")
public class WalletController
{
    private final CurrencyService currencyService;

    public WalletController(final CurrencyService currencyService)
    {
        this.currencyService = currencyService;
    }

    @GetMapping("/me")
    public ResponseEntity<WalletResponseDTO> getMyWallet(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(currencyService.getMyWallet(jwt.getSubject()));
    }
}
