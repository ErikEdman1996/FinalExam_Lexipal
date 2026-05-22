package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.StoreItemResponseDTO;
import com.lexipal.core.dtos.responses.StorePurchaseResponseDTO;
import com.lexipal.core.dtos.responses.StoreResponseDTO;
import com.lexipal.core.dtos.responses.WalletResponseDTO;
import com.lexipal.core.entities.CurrencyTransactionReason;
import com.lexipal.core.entities.StoreItemDefinition;
import com.lexipal.core.entities.StoreItemType;
import com.lexipal.core.entities.ThemeDefinition;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.UserThemeUnlock;
import com.lexipal.core.exceptions.ResourceConflictException;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.repositories.StoreItemDefinitionRepository;
import com.lexipal.core.repositories.ThemeDefinitionRepository;
import com.lexipal.core.repositories.UserRepository;
import com.lexipal.core.repositories.UserThemeUnlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StoreService
{
    public static final String STREAK_FREEZE_ITEM_CODE = "streak_freeze";

    private final StoreItemDefinitionRepository storeItemDefinitionRepository;
    private final ThemeDefinitionRepository themeDefinitionRepository;
    private final UserThemeUnlockRepository userThemeUnlockRepository;
    private final UserRepository userRepository;
    private final UserServiceInterface userService;
    private final CurrencyService currencyService;
    private final StreakMetaDataServiceInterface streakMetaDataService;

    public StoreService(final StoreItemDefinitionRepository storeItemDefinitionRepository,
                        final ThemeDefinitionRepository themeDefinitionRepository,
                        final UserThemeUnlockRepository userThemeUnlockRepository,
                        final UserRepository userRepository,
                        final UserServiceInterface userService,
                        final CurrencyService currencyService,
                        final StreakMetaDataServiceInterface streakMetaDataService)
    {
        this.storeItemDefinitionRepository = storeItemDefinitionRepository;
        this.themeDefinitionRepository = themeDefinitionRepository;
        this.userThemeUnlockRepository = userThemeUnlockRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.currencyService = currencyService;
        this.streakMetaDataService = streakMetaDataService;
    }

    public StoreResponseDTO getMyStore(String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);
        return new StoreResponseDTO(currencyService.getWallet(user), getCatalogItems(user));
    }

    public StorePurchaseResponseDTO purchaseItem(String keycloakSub, String itemCode)
    {
        User user = userService.getOrCreateUser(keycloakSub);
        StoreItemDefinition item = findActiveItem(itemCode);
        User lockedUser = lockUser(user);
        StoreItemResponseDTO itemResponse = switch (item.getItemType())
        {
            case STREAK_FREEZE -> purchaseStreakFreeze(lockedUser, item);
            case THEME -> purchaseTheme(lockedUser, item);
        };

        return new StorePurchaseResponseDTO(currencyService.getWallet(lockedUser), itemResponse);
    }

    public WalletResponseDTO purchaseMyStreakFreeze(String keycloakSub)
    {
        return purchaseItem(keycloakSub, STREAK_FREEZE_ITEM_CODE).wallet();
    }

    private StoreItemResponseDTO purchaseStreakFreeze(User user, StoreItemDefinition item)
    {
        currencyService.spendCoins(user, item.getPriceCoins(), CurrencyTransactionReason.STREAK_FREEZE_PURCHASE,
                "STORE_ITEM", item.getId());
        streakMetaDataService.addAvailableStreakFreeze(user);
        return toStoreItemResponse(user, item, null);
    }

    private StoreItemResponseDTO purchaseTheme(User user, StoreItemDefinition item)
    {
        ThemeDefinition theme = themeDefinitionRepository.findByStoreItem_Id(item.getId())
                .filter(ThemeDefinition::getActive)
                .orElseThrow(() -> new IllegalArgumentException("Theme store item is not available."));
        if (userThemeUnlockRepository.existsByUser_IdAndTheme_Id(user.getId(), theme.getId()))
        {
            throw new ResourceConflictException("Theme is already unlocked.");
        }

        currencyService.spendCoins(user, item.getPriceCoins(), CurrencyTransactionReason.THEME_PURCHASE,
                "STORE_ITEM", item.getId());

        UserThemeUnlock unlock = new UserThemeUnlock();
        unlock.setUser(user);
        unlock.setTheme(theme);
        userThemeUnlockRepository.save(unlock);
        return toStoreItemResponse(user, item, theme);
    }

    private List<StoreItemResponseDTO> getCatalogItems(User user)
    {
        Map<Long, ThemeDefinition> themesByStoreItemId = new HashMap<>();
        for (ThemeDefinition theme : themeDefinitionRepository.findByActiveTrueOrderBySortOrderAsc())
        {
            if (theme.getStoreItem() != null)
            {
                themesByStoreItemId.put(theme.getStoreItem().getId(), theme);
            }
        }

        return storeItemDefinitionRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(item -> toStoreItemResponse(user, item, themesByStoreItemId.get(item.getId())))
                .toList();
    }

    private StoreItemResponseDTO toStoreItemResponse(User user, StoreItemDefinition item, ThemeDefinition theme)
    {
        Integer ownedQuantity = null;
        boolean owned = false;
        boolean selected = false;
        if (item.getItemType() == StoreItemType.STREAK_FREEZE)
        {
            ownedQuantity = streakMetaDataService.getAvailableStreakFreezes(user);
            owned = ownedQuantity > 0;
        }
        else if (theme != null)
        {
            owned = userThemeUnlockRepository.existsByUser_IdAndTheme_Id(user.getId(), theme.getId());
            selected = user.getSelectedTheme() != null && user.getSelectedTheme().getId().equals(theme.getId());
        }

        return new StoreItemResponseDTO(
                item.getCode(),
                item.getName(),
                item.getDescription(),
                item.getItemType().name(),
                item.getPriceCoins(),
                owned,
                selected,
                ownedQuantity,
                theme == null ? null : theme.getCode()
        );
    }

    private StoreItemDefinition findActiveItem(String itemCode)
    {
        String cleanCode = itemCode == null ? null : itemCode.trim();
        return storeItemDefinitionRepository.findByCode(cleanCode)
                .filter(StoreItemDefinition::getActive)
                .orElseThrow(() -> new IllegalArgumentException("Store item not found or inactive."));
    }

    private User lockUser(User user)
    {
        return userRepository.findByIdForUpdate(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));
    }
}
