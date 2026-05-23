package com.lexipal.core.configs;

import com.lexipal.core.entities.StoreItemDefinition;
import com.lexipal.core.entities.StoreItemType;
import com.lexipal.core.entities.ThemeDefinition;
import com.lexipal.core.entities.ThemeTone;
import com.lexipal.core.repositories.StoreItemDefinitionRepository;
import com.lexipal.core.repositories.ThemeDefinitionRepository;
import com.lexipal.core.services.CurrencyService;
import com.lexipal.core.services.StoreService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class StoreThemeDataInitializer
{
    @Bean
    @Order(1)
    CommandLineRunner seedStoreAndThemeDefinitions(StoreItemDefinitionRepository storeItemRepository,
                                                   ThemeDefinitionRepository themeRepository)
    {
        return args -> {
            seedStoreItem(storeItemRepository, StoreService.STREAK_FREEZE_ITEM_CODE, "Streak Freeze",
                    "Protect one missed reading day.", StoreItemType.STREAK_FREEZE,
                    CurrencyService.STREAK_FREEZE_PRICE, 10);

            seedFreeTheme(themeRepository, "default_light", "Reading Room",
                    "The free light Lexipal reading palette.", ThemeTone.LIGHT, 10);
            seedFreeTheme(themeRepository, "default_dark", "Midnight Reader",
                    "The free dark Lexipal reading palette.", ThemeTone.DARK, 20);

            seedPaidTheme(storeItemRepository, themeRepository, "vellum_dawn", "Vellum Dawn",
                    "Warm paper tones for an early reading session.", ThemeTone.LIGHT, 100);
            seedPaidTheme(storeItemRepository, themeRepository, "sage_margin", "Sage Margin",
                    "Soft green margins and quiet study tones.", ThemeTone.LIGHT, 110);
            seedPaidTheme(storeItemRepository, themeRepository, "harbor_folio", "Harbor Folio",
                    "Cool blue pages with a harbor-side calm.", ThemeTone.LIGHT, 120);
            seedPaidTheme(storeItemRepository, themeRepository, "rose_bookmark", "Rose Bookmark",
                    "A rosy bookmark palette with a gentle contrast.", ThemeTone.LIGHT, 130);
            seedPaidTheme(storeItemRepository, themeRepository, "midnight_archive", "Midnight Archive",
                    "Deep archive blues for after-hours reading.", ThemeTone.DARK, 140);
            seedPaidTheme(storeItemRepository, themeRepository, "mothlight_nocturne", "Mothlight Nocturne",
                    "Amber lamplight against a dark nocturne.", ThemeTone.DARK, 150);
            seedPaidTheme(storeItemRepository, themeRepository, "forest_canopy", "Forest Canopy",
                    "Dark forest greens for quiet evening reading.", ThemeTone.DARK, 160);
            seedPaidTheme(storeItemRepository, themeRepository, "plum_afterword", "Plum Afterword",
                    "Plum shadows for the page after the ending.", ThemeTone.DARK, 170);
        };
    }

    private void seedFreeTheme(ThemeDefinitionRepository repository, String code, String name,
                               String description, ThemeTone tone, int sortOrder)
    {
        if (repository.findByCode(code).isPresent())
        {
            return;
        }

        ThemeDefinition theme = new ThemeDefinition();
        theme.setCode(code);
        theme.setName(name);
        theme.setDescription(description);
        theme.setTone(tone);
        theme.setSortOrder(sortOrder);
        theme.setActive(true);
        repository.save(theme);
    }

    private void seedPaidTheme(StoreItemDefinitionRepository storeItemRepository,
                               ThemeDefinitionRepository themeRepository, String code, String name,
                               String description, ThemeTone tone, int sortOrder)
    {
        StoreItemDefinition item = seedStoreItem(storeItemRepository, code, name, description,
                StoreItemType.THEME, 100, sortOrder);
        if (themeRepository.findByCode(code).isPresent())
        {
            return;
        }

        ThemeDefinition theme = new ThemeDefinition();
        theme.setCode(code);
        theme.setName(name);
        theme.setDescription(description);
        theme.setTone(tone);
        theme.setSortOrder(sortOrder);
        theme.setActive(true);
        theme.setStoreItem(item);
        themeRepository.save(theme);
    }

    private StoreItemDefinition seedStoreItem(StoreItemDefinitionRepository repository, String code, String name,
                                              String description, StoreItemType itemType, int priceCoins,
                                              int sortOrder)
    {
        return repository.findByCode(code).orElseGet(() -> {
            StoreItemDefinition item = new StoreItemDefinition();
            item.setCode(code);
            item.setName(name);
            item.setDescription(description);
            item.setItemType(itemType);
            item.setPriceCoins(priceCoins);
            item.setSortOrder(sortOrder);
            item.setActive(true);
            return repository.save(item);
        });
    }
}
