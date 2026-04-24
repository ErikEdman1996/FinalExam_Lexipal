package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.PortraitResponseDTO;
import com.lexipal.core.entities.PortraitDefinition;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.UserPortraitUnlock;
import com.lexipal.core.repositories.PortraitDefinitionRepository;
import com.lexipal.core.repositories.UserPortraitUnlockRepository;
import com.lexipal.core.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class PortraitService
{
    public static final String STARTER_PORTRAIT_CODE = "larvae_starter";

    private final PortraitDefinitionRepository portraitDefinitionRepository;
    private final UserPortraitUnlockRepository userPortraitUnlockRepository;
    private final UserRepository userRepository;
    private final UserServiceInterface userService;
    private final XpService xpService;

    @Autowired
    public PortraitService(final PortraitDefinitionRepository portraitDefinitionRepository,
                           final UserPortraitUnlockRepository userPortraitUnlockRepository,
                           final UserRepository userRepository,
                           final UserServiceInterface userService,
                           final XpService xpService)
    {
        this.portraitDefinitionRepository = portraitDefinitionRepository;
        this.userPortraitUnlockRepository = userPortraitUnlockRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.xpService = xpService;
    }

    public List<PortraitResponseDTO> getPortraitsForUser(String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);
        unlockEligiblePortraits(user);
        return buildPortraitResponses(user);
    }

    public PortraitResponseDTO selectPortrait(String keycloakSub, String portraitCode)
    {
        User user = userService.getOrCreateUser(keycloakSub);
        unlockEligiblePortraits(user);

        String cleanCode = portraitCode == null ? null : portraitCode.trim();
        PortraitDefinition portrait = portraitDefinitionRepository.findByCode(cleanCode)
                .filter(PortraitDefinition::getActive)
                .orElseThrow(() -> new IllegalArgumentException("Portrait not found or inactive."));

        if (!userPortraitUnlockRepository.existsByUser_IdAndPortrait_Id(user.getId(), portrait.getId()))
        {
            throw new IllegalArgumentException("Portrait is locked.");
        }

        user.setSelectedPortrait(portrait);
        userRepository.save(user);

        return toResponse(portrait, true, true, findUnlockedAt(user.getId(), portrait.getId()));
    }

    public PortraitResponseDTO getSelectedPortraitForProfile(User user)
    {
        unlockEligiblePortraits(user);

        PortraitDefinition selected = user.getSelectedPortrait();
        if (selected == null)
        {
            return null;
        }

        return toResponse(selected, true, true, findUnlockedAt(user.getId(), selected.getId()));
    }

    public void unlockEligiblePortraits(User user)
    {
        if (user == null || user.getId() == null)
        {
            return;
        }

        int level = xpService.calculateLevel(xpService.getCurrentTotalXp(user));
        Map<Long, UserPortraitUnlock> unlocksByPortraitId = userPortraitUnlockRepository.findByUser_Id(user.getId())
                .stream()
                .collect(Collectors.toMap(upu -> upu.getPortrait().getId(), Function.identity()));

        for (PortraitDefinition portrait : portraitDefinitionRepository.findByActiveTrueOrderBySortOrderAsc())
        {
            if (portrait.getRequiredLevel() > level || unlocksByPortraitId.containsKey(portrait.getId()))
            {
                continue;
            }

            UserPortraitUnlock unlock = new UserPortraitUnlock();
            unlock.setUser(user);
            unlock.setPortrait(portrait);
            unlock.setUnlockedAt(LocalDateTime.now());
            userPortraitUnlockRepository.save(unlock);
            unlocksByPortraitId.put(portrait.getId(), unlock);
        }

        if (user.getSelectedPortrait() == null)
        {
            portraitDefinitionRepository.findByCode(STARTER_PORTRAIT_CODE)
                    .filter(PortraitDefinition::getActive)
                    .filter(starter -> unlocksByPortraitId.containsKey(starter.getId()))
                    .or(() -> unlocksByPortraitId.values().stream()
                            .map(UserPortraitUnlock::getPortrait)
                            .min(Comparator.comparing(PortraitDefinition::getSortOrder)))
                    .ifPresent(selected -> {
                        user.setSelectedPortrait(selected);
                        userRepository.save(user);
                    });
        }
    }

    private List<PortraitResponseDTO> buildPortraitResponses(User user)
    {
        Map<Long, UserPortraitUnlock> unlocksByPortraitId = userPortraitUnlockRepository.findByUser_Id(user.getId())
                .stream()
                .collect(Collectors.toMap(upu -> upu.getPortrait().getId(), Function.identity()));
        Long selectedId = user.getSelectedPortrait() == null ? null : user.getSelectedPortrait().getId();

        return portraitDefinitionRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(portrait -> {
                    UserPortraitUnlock unlock = unlocksByPortraitId.get(portrait.getId());
                    return toResponse(
                            portrait,
                            unlock != null,
                            selectedId != null && selectedId.equals(portrait.getId()),
                            unlock == null ? null : unlock.getUnlockedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    private LocalDateTime findUnlockedAt(Long userId, Long portraitId)
    {
        return userPortraitUnlockRepository.findByUser_Id(userId).stream()
                .filter(unlock -> unlock.getPortrait().getId().equals(portraitId))
                .map(UserPortraitUnlock::getUnlockedAt)
                .findFirst()
                .orElse(null);
    }

    private PortraitResponseDTO toResponse(PortraitDefinition portrait, boolean unlocked, boolean selected,
                                           LocalDateTime unlockedAt)
    {
        return new PortraitResponseDTO(
                portrait.getCode(),
                portrait.getName(),
                portrait.getDescription(),
                portrait.getAssetKey(),
                portrait.getRequiredLevel(),
                portrait.getSortOrder(),
                unlocked,
                selected,
                unlockedAt
        );
    }
}
