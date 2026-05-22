package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.StorePurchaseResponseDTO;
import com.lexipal.core.dtos.responses.StoreResponseDTO;
import com.lexipal.core.services.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lexipal/api/v1/store")
public class StoreController
{
    private final StoreService storeService;

    public StoreController(final StoreService storeService)
    {
        this.storeService = storeService;
    }

    @GetMapping("/me")
    public ResponseEntity<StoreResponseDTO> getMyStore(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(storeService.getMyStore(jwt.getSubject()));
    }

    @PostMapping("/items/{itemCode}/purchase")
    public ResponseEntity<StorePurchaseResponseDTO> purchaseItem(@PathVariable String itemCode,
                                                                 @AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(storeService.purchaseItem(jwt.getSubject(), itemCode));
    }
}
