package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.ThemeResponseDTO;
import com.lexipal.core.entities.ThemeDefinition;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.UserThemeUnlock;
import com.lexipal.core.repositories.ThemeDefinitionRepository;
import com.lexipal.core.repositories.UserRepository;
import com.lexipal.core.repositories.UserThemeUnlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ThemeService
{
    private final ThemeDefinitionRepository themeDefinitionRepository;
    private final UserThemeUnlockRepository userThemeUnlockRepository;
    private final UserRepository userRepository;
    private final UserServiceInterface userService;

    public ThemeService(final ThemeDefinitionRepository themeDefinitionRepository,
                        final UserThemeUnlockRepository userThemeUnlockRepository,
                        final UserRepository userRepository,
                        final UserServiceInterface userService)
    {
        this.themeDefinitionRepository = themeDefinitionRepository;
        this.userThemeUnlockRepository = userThemeUnlockRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<ThemeResponseDTO> getThemesForUser(String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);
        Map<Long, UserThemeUnlock> unlocksByThemeId = getUnlocksByThemeId(user);
        Long selectedId = user.getSelectedTheme() == null ? null : user.getSelectedTheme().getId();

        return themeDefinitionRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(theme -> toResponse(
                        theme,
                        isUnlocked(theme, unlocksByThemeId),
                        selectedId != null && selectedId.equals(theme.getId()),
                        unlockedAt(theme, unlocksByThemeId)
                ))
                .toList();
    }

    public ThemeResponseDTO selectTheme(String keycloakSub, String themeCode)
    {
        User user = userService.getOrCreateUser(keycloakSub);
        String cleanCode = themeCode == null ? null : themeCode.trim();
        ThemeDefinition theme = themeDefinitionRepository.findByCode(cleanCode)
                .filter(ThemeDefinition::getActive)
                .orElseThrow(() -> new IllegalArgumentException("Theme not found or inactive."));

        UserThemeUnlock unlock = userThemeUnlockRepository.findByUser_IdAndTheme_Id(user.getId(), theme.getId())
                .orElse(null);
        if (theme.getStoreItem() != null && unlock == null)
        {
            throw new IllegalArgumentException("Theme is locked.");
        }

        user.setSelectedTheme(theme);
        userRepository.save(user);

        return toResponse(theme, true, true, unlock == null ? null : unlock.getUnlockedAt());
    }

    public ThemeResponseDTO getSelectedThemeForProfile(User user)
    {
        if (user == null || user.getSelectedTheme() == null || !user.getSelectedTheme().getActive())
        {
            return null;
        }

        ThemeDefinition selected = user.getSelectedTheme();
        UserThemeUnlock unlock = userThemeUnlockRepository.findByUser_IdAndTheme_Id(user.getId(), selected.getId())
                .orElse(null);
        return toResponse(selected, selected.getStoreItem() == null || unlock != null, true,
                unlock == null ? null : unlock.getUnlockedAt());
    }

    private Map<Long, UserThemeUnlock> getUnlocksByThemeId(User user)
    {
        return userThemeUnlockRepository.findByUser_Id(user.getId()).stream()
                .collect(Collectors.toMap(unlock -> unlock.getTheme().getId(), Function.identity()));
    }

    private boolean isUnlocked(ThemeDefinition theme, Map<Long, UserThemeUnlock> unlocksByThemeId)
    {
        return theme.getStoreItem() == null || unlocksByThemeId.containsKey(theme.getId());
    }

    private LocalDateTime unlockedAt(ThemeDefinition theme, Map<Long, UserThemeUnlock> unlocksByThemeId)
    {
        UserThemeUnlock unlock = unlocksByThemeId.get(theme.getId());
        return unlock == null ? null : unlock.getUnlockedAt();
    }

    private ThemeResponseDTO toResponse(ThemeDefinition theme, boolean unlocked, boolean selected,
                                        LocalDateTime unlockedAt)
    {
        return new ThemeResponseDTO(
                theme.getCode(),
                theme.getName(),
                theme.getDescription(),
                theme.getTone().name(),
                theme.getSortOrder(),
                unlocked,
                selected,
                theme.getStoreItem() == null ? 0 : theme.getStoreItem().getPriceCoins(),
                unlockedAt
        );
    }
}
