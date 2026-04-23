package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.StreakMetaDataCreateRequestDTO;
import com.lexipal.core.dtos.responses.StreakMetaDataReadResponseDTO;
import com.lexipal.core.entities.StreakMetaData;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.StreakMetaDataRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class StreakMetaDataService implements StreakMetaDataServiceInterface
{
    private static final Logger logger = LogManager.getLogger(StreakMetaDataService.class);

    private final StreakMetaDataRepository streakMetaDataRepository;
    private final UserServiceInterface userService;

    @Autowired
    public StreakMetaDataService(final StreakMetaDataRepository streakMetaDataRepository,
                                 final UserServiceInterface userService)
    {
        this.streakMetaDataRepository = streakMetaDataRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public StreakMetaDataReadResponseDTO initializeStreakMetaData(StreakMetaDataCreateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);

        StreakMetaData streakMetaData = streakMetaDataRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createStreakMetaData(user, dto));

        return toStreakMetaDataReadResponse(streakMetaData);
    }

    @Override
    @Transactional
    public StreakMetaDataReadResponseDTO getStreakMetaData(String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);
        StreakMetaData streakMetaData = ensureStreakMetaData(user);

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
        LocalDate lastActivityDate = streakMetaData.getLastActivityDate();

        if (lastActivityDate == null)
        {
            streakMetaData.setCurrentStreak(1);
            streakMetaData.setLongestStreak(Math.max(1, streakMetaData.getLongestStreak()));
            streakMetaData.setLastActivityDate(activityDate);
            streakMetaDataRepository.save(streakMetaData);
            logger.info("Initialized streak tracking for user {} on {}", user.getId(), activityDate);
            return;
        }

        if (activityDate.isBefore(lastActivityDate) || activityDate.isEqual(lastActivityDate))
        {
            logger.info("Ignored streak update for user {} because activity date {} is not after {}", user.getId(), activityDate, lastActivityDate);
            return;
        }

        if (activityDate.isEqual(lastActivityDate.plusDays(1)))
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
                streakMetaData.getCurrentStreak(),
                streakMetaData.getLongestStreak(),
                streakMetaData.getLastActivityDate()
        );
    }
}
