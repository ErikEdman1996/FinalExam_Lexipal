package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.StreakMetaDataCreateRequestDTO;
import com.lexipal.core.dtos.responses.StreakMetaDataReadResponseDTO;
import com.lexipal.core.entities.StreakMetaData;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.StreakMetaDataRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Transactional
public class StreakMetaDataService implements StreakMetaDataServiceInterface
{
    private static final Logger logger = LogManager.getLogger(StreakMetaDataService.class);

    private final StreakMetaDataRepository streakMetaDataRepository;
    private final UserServiceInterface userService;
    private final ZoneId streakZone;

    @Autowired
    public StreakMetaDataService(final StreakMetaDataRepository streakMetaDataRepository,
                                 final UserServiceInterface userService,
                                 @Value("${lexipal.streak.timezone:Europe/Stockholm}") String timezone)
    {
        this.streakMetaDataRepository = streakMetaDataRepository;
        this.userService = userService;
        this.streakZone = ZoneId.of(timezone);
    }

    @Override
    @Transactional
    public StreakMetaDataReadResponseDTO initializeStreakMetaData(StreakMetaDataCreateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);

        StreakMetaData streakMetaData = streakMetaDataRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createStreakMetaData(user, dto));

        reconcileMissedDays(streakMetaData, LocalDate.now(streakZone));
        return toStreakMetaDataReadResponse(streakMetaData);
    }

    @Override
    @Transactional
    public StreakMetaDataReadResponseDTO getStreakMetaData(String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);
        StreakMetaData streakMetaData = ensureStreakMetaData(user);

        reconcileMissedDays(streakMetaData, LocalDate.now(streakZone));
        return toStreakMetaDataReadResponse(streakMetaData);
    }

    @Override
    @Transactional
    public void recordActivity(User user, LocalDate activityDate)
    {
        if (activityDate == null)
        {
            throw new IllegalArgumentException("Activity date is required for streak tracking.");
        }

        StreakMetaData streakMetaData = ensureStreakMetaData(user);
        reconcileMissedDays(streakMetaData, activityDate);
        LocalDate continuityDate = resolveContinuityDate(streakMetaData);

        if (continuityDate == null)
        {
            streakMetaData.setCurrentStreak(1);
            streakMetaData.setLongestStreak(Math.max(1, streakMetaData.getLongestStreak()));
            streakMetaData.setLastActivityDate(activityDate);
            streakMetaDataRepository.save(streakMetaData);
            logger.info("Initialized streak tracking for user {} on {}", user.getId(), activityDate);
            return;
        }

        if (!activityDate.isAfter(continuityDate))
        {
            logger.info("Ignored streak update for user {} because activity date {} is not after continuity date {}",
                    user.getId(), activityDate, continuityDate);
            return;
        }

        if (activityDate.isEqual(continuityDate.plusDays(1)) && streakMetaData.getCurrentStreak() > 0)
        {
            streakMetaData.setCurrentStreak(streakMetaData.getCurrentStreak() + 1);
        }
        else
        {
            streakMetaData.setCurrentStreak(1);
        }

        streakMetaData.setLongestStreak(Math.max(streakMetaData.getLongestStreak(), streakMetaData.getCurrentStreak()));
        streakMetaData.setLastActivityDate(activityDate);
        streakMetaDataRepository.save(streakMetaData);
        logger.info("Recorded streak activity for user {} on {}; current streak {}, longest streak {}", user.getId(), activityDate, streakMetaData.getCurrentStreak(), streakMetaData.getLongestStreak());
    }

    @Override
    public int getAvailableStreakFreezes(User user)
    {
        return availableStreakFreezes(ensureStreakMetaData(user));
    }

    @Override
    public void addAvailableStreakFreeze(User user)
    {
        StreakMetaData streakMetaData = ensureStreakMetaData(user);
        streakMetaData.setAvailableStreakFreezes(availableStreakFreezes(streakMetaData) + 1);
        streakMetaDataRepository.save(streakMetaData);
    }

    private StreakMetaData ensureStreakMetaData(User user)
    {
        return streakMetaDataRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createStreakMetaData(user, null));
    }

    private StreakMetaData createStreakMetaData(User user, StreakMetaDataCreateRequestDTO dto)
    {
        StreakMetaData streakMetaData = new StreakMetaData();
        streakMetaData.setUser(user);
        streakMetaData.setCurrentStreak(dto != null && dto.getCurrentStreak() != null ? dto.getCurrentStreak() : 0);
        streakMetaData.setLongestStreak(dto != null && dto.getLongestStreak() != null ? dto.getLongestStreak() : 0);
        streakMetaData.setLastActivityDate(dto != null ? dto.getLastActivityDate() : null);
        streakMetaData.setAvailableStreakFreezes(0);

        StreakMetaData savedStreak = streakMetaDataRepository.save(streakMetaData);
        user.setStreakMetaData(savedStreak);
        logger.info("Created streak metadata {} for user {}", savedStreak.getId(), user.getId());

        return savedStreak;
    }

    private StreakMetaDataReadResponseDTO toStreakMetaDataReadResponse(StreakMetaData streakMetaData)
    {
        return new StreakMetaDataReadResponseDTO(
                streakMetaData.getId(),
                streakMetaData.getUser().getId(),
                resolveEffectiveCurrentStreak(
                        streakMetaData.getCurrentStreak(),
                        resolveContinuityDate(streakMetaData),
                        LocalDate.now(streakZone)
                ),
                streakMetaData.getLongestStreak(),
                streakMetaData.getLastActivityDate(),
                availableStreakFreezes(streakMetaData),
                streakMetaData.getFreezeProtectedThroughDate()
        );
    }

    int resolveEffectiveCurrentStreak(Integer currentStreak, LocalDate lastActivityDate, LocalDate asOfDate)
    {
        if (currentStreak == null || currentStreak <= 0 || lastActivityDate == null || asOfDate == null)
        {
            return 0;
        }

        if (lastActivityDate.isEqual(asOfDate) || lastActivityDate.isEqual(asOfDate.minusDays(1)))
        {
            return currentStreak;
        }

        return 0;
    }

    void reconcileMissedDays(StreakMetaData streakMetaData, LocalDate asOfDate)
    {
        if (streakMetaData == null || asOfDate == null
                || streakMetaData.getCurrentStreak() == null || streakMetaData.getCurrentStreak() <= 0)
        {
            return;
        }

        LocalDate continuityDate = resolveContinuityDate(streakMetaData);
        if (continuityDate == null)
        {
            return;
        }

        LocalDate lastMissedDate = asOfDate.minusDays(1);
        if (!continuityDate.isBefore(lastMissedDate))
        {
            return;
        }

        int availableFreezes = availableStreakFreezes(streakMetaData);
        boolean changed = false;

        while (continuityDate.isBefore(lastMissedDate) && availableFreezes > 0)
        {
            continuityDate = continuityDate.plusDays(1);
            streakMetaData.setFreezeProtectedThroughDate(continuityDate);
            availableFreezes--;
            changed = true;
        }

        if (changed)
        {
            streakMetaData.setAvailableStreakFreezes(availableFreezes);
        }

        if (continuityDate.isBefore(lastMissedDate))
        {
            streakMetaData.setCurrentStreak(0);
            changed = true;
        }

        if (changed)
        {
            streakMetaDataRepository.save(streakMetaData);
        }
    }

    private LocalDate resolveContinuityDate(StreakMetaData streakMetaData)
    {
        LocalDate lastActivityDate = streakMetaData.getLastActivityDate();
        LocalDate freezeProtectedThroughDate = streakMetaData.getFreezeProtectedThroughDate();

        if (lastActivityDate == null)
        {
            return freezeProtectedThroughDate;
        }

        if (freezeProtectedThroughDate == null || lastActivityDate.isAfter(freezeProtectedThroughDate))
        {
            return lastActivityDate;
        }

        return freezeProtectedThroughDate;
    }

    private int availableStreakFreezes(StreakMetaData streakMetaData)
    {
        return streakMetaData.getAvailableStreakFreezes() == null
                ? 0
                : Math.max(streakMetaData.getAvailableStreakFreezes(), 0);
    }
}
