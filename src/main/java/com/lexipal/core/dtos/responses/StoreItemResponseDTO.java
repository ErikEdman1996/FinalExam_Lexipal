package com.lexipal.core.dtos.responses;

public record StoreItemResponseDTO(
        String code,
        String name,
        String description,
        String itemType,
        Integer priceCoins,
        boolean owned,
        boolean selected,
        Integer ownedQuantity,
        String themeCode
)
{
}
