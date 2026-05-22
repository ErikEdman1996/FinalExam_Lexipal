package com.lexipal.core.dtos.responses;

import java.util.List;

public record StoreResponseDTO(
        WalletResponseDTO wallet,
        List<StoreItemResponseDTO> items
)
{
}
