package com.lexipal.core.dtos.responses;

public record StorePurchaseResponseDTO(
        WalletResponseDTO wallet,
        StoreItemResponseDTO item
)
{
}
