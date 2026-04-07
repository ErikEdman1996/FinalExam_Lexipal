package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.StreakMetaDataCreateRequestDTO;
import com.lexipal.core.dtos.responses.StreakMetaDataReadResponseDTO;
import com.lexipal.core.entities.StreakMetaData;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.StreakMetaDataRepository;
import com.lexipal.core.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class StreakMetaDataService implements StreakMetaDataServiceInterface
{
    private final StreakMetaDataRepository streakMetaDataRepository;
    private final UserRepository userRepository;

    private static final Logger userLogger = LogManager.getLogger("UserLogger");
    private static final Logger streakLogger = LogManager.getLogger("StreakMetaDataLogger");

    @Autowired
    public StreakMetaDataService(final StreakMetaDataRepository streakMetaDataRepository,
                                 final UserRepository userRepository)
    {
        this.streakMetaDataRepository = streakMetaDataRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public StreakMetaDataReadResponseDTO initializeStreakMetaData(StreakMetaDataCreateRequestDTO dto, String keycloakSub)
    {
        User user = getOrCreateUser(keycloakSub);

        StreakMetaData streakMetaData = streakMetaDataRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createStreakMetaData(user, dto));

        return toStreakMetaDataReadResponse(streakMetaData);
    }

    @Override
    @Transactional
    public StreakMetaDataReadResponseDTO getStreakMetaData(String keycloakSub)
    {
        User user = getOrCreateUser(keycloakSub);
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
            return;
        }

        if (activityDate.isBefore(lastActivityDate) || activityDate.isEqual(lastActivityDate))
        {
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
    }

    private User getOrCreateUser(String keycloakSub)
    {
        return userRepository.findByKeycloakSub(keycloakSub)
                .orElseGet(() -> {
                    userLogger.info("User not found, creating new user...");
                    User newUser = new User();
                    newUser.setKeycloakSub(keycloakSub);
                    User savedUser = userRepository.save(newUser);
                    userLogger.info("Created new user with id: {}", savedUser.getId());
                    return savedUser;
                });
    }

    private StreakMetaData ensureStreakMetaData(User user)
    {
        return streakMetaDataRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createStreakMetaData(user, null));
    }

    private StreakMetaData createStreakMetaData(User user, StreakMetaDataCreateRequestDTO dto)
    {
        streakLogger.info("Creating streak metadata for user with id: {}", user.getId());

        StreakMetaData streakMetaData = new StreakMetaData();
        streakMetaData.setUser(user);
        streakMetaData.setCurrentStreak(dto != null && dto.getCurrentStreak() != null ? dto.getCurrentStreak() : 0);
        streakMetaData.setLongestStreak(dto != null && dto.getLongestStreak() != null ? dto.getLongestStreak() : 0);
        streakMetaData.setLastActivityDate(dto != null ? dto.getLastActivityDate() : null);

        StreakMetaData savedStreak = streakMetaDataRepository.save(streakMetaData);
        user.setStreakMetaData(savedStreak);
        userRepository.save(user);

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
