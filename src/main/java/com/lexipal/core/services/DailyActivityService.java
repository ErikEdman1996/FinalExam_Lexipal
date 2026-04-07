package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.DailyActivityCreateRequestDTO;
import com.lexipal.core.dtos.responses.DailyActivityReadResponseDTO;
import com.lexipal.core.entities.DailyActivity;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.DailyActivityRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DailyActivityService implements DailyActivityServiceInterface
{
    private static final int SESSION_COMPLETION_XP = 25;

    private final DailyActivityRepository dailyActivityRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final StreakMetaDataServiceInterface streakMetaDataService;

    private static final Logger dailyActivityLogger = LogManager.getLogger("DailyActivityLogger");
    private static final Logger userLogger = LogManager.getLogger("UserLogger");

    @Autowired
    public DailyActivityService(final DailyActivityRepository dailyActivityRepository,
                                final UserRepository userRepository,
                                final SessionRepository sessionRepository,
                                final StreakMetaDataServiceInterface streakMetaDataService)
    {
        this.dailyActivityRepository = dailyActivityRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.streakMetaDataService = streakMetaDataService;
    }

    @Override
    @Transactional
    public DailyActivityReadResponseDTO createDailyActivity(DailyActivityCreateRequestDTO dto, String keycloakSub)
    {
        User user = userRepository.findByKeycloakSub(keycloakSub)
                .orElseGet(() -> {
                    userLogger.info("User not found, creating new user...");
                    User newUser = new User();
                    newUser.setKeycloakSub(keycloakSub);
                    User savedUser = userRepository.save(newUser);
                    userLogger.info("Created new user with id: {}", savedUser.getId());
                    return savedUser;
                });

        Session session = null;
        if (dto.getSessionId() != null)
        {
            session = sessionRepository.findById(dto.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session", "id", dto.getSessionId()));

            if (!session.getUser().getId().equals(user.getId()))
            {
                throw new UnauthorizedActionException("This session does not belong to you.");
            }
        }

        if (dto.getActivityType() == DailyActivity.ActivityType.SESSION_COMPLETED && session == null)
        {
            throw new IllegalArgumentException("Session-completed activity requires a session.");
        }

        if (session != null)
        {
            DailyActivity existingActivity = dailyActivityRepository
                    .findBySession_IdAndActivityType(session.getId(), dto.getActivityType())
                    .orElse(null);

            if (existingActivity != null)
            {
                return toDailyActivityReadResponse(existingActivity);
            }
        }

        DailyActivity activity = new DailyActivity();
        activity.setUser(user);
        activity.setSession(session);
        activity.setActivityDate(dto.getActivityDate());
        activity.setActivityType(dto.getActivityType());
        activity.setXpEarned(dto.getXpEarned());

        DailyActivity savedActivity = dailyActivityRepository.save(activity);
        user.getDailyActivities().add(savedActivity);

        if (session != null)
        {
            session.getDailyActivities().add(savedActivity);
        }

        streakMetaDataService.recordActivity(user, savedActivity.getActivityDate());
        dailyActivityLogger.info("Created daily activity with id: {} for user with id: {}", savedActivity.getId(), user.getId());

        return toDailyActivityReadResponse(savedActivity);
    }

    @Override
    @Transactional
    public DailyActivityReadResponseDTO recordSessionCompletedActivity(Session session)
    {
        if (session == null)
        {
            throw new IllegalArgumentException("Session is required.");
        }

        DailyActivityCreateRequestDTO requestDTO = new DailyActivityCreateRequestDTO(
                session.getSessionDate().toLocalDate(),
                DailyActivity.ActivityType.SESSION_COMPLETED,
                SESSION_COMPLETION_XP,
                session.getId()
        );

        return createDailyActivity(requestDTO, session.getUser().getKeycloakSub());
    }

    @Override
    public List<DailyActivityReadResponseDTO> getDailyActivitiesByUser(String keycloakSub)
    {
        userRepository.findByKeycloakSub(keycloakSub)
                .orElseGet(() -> {
                    userLogger.info("User not found, creating new user...");
                    User newUser = new User();
                    newUser.setKeycloakSub(keycloakSub);
                    User savedUser = userRepository.save(newUser);
                    userLogger.info("Created new user with id: {}", savedUser.getId());
                    return savedUser;
                });

        return dailyActivityRepository.findByUser_KeycloakSubOrderByActivityDateDesc(keycloakSub).stream()
                .map(this::toDailyActivityReadResponse)
                .collect(Collectors.toList());
    }

    private DailyActivityReadResponseDTO toDailyActivityReadResponse(DailyActivity activity)
    {
        return new DailyActivityReadResponseDTO(
                activity.getId(),
                activity.getUser().getId(),
                activity.getSession() != null ? activity.getSession().getId() : null,
                activity.getActivityDate(),
                activity.getActivityType().name(),
                activity.getXpEarned()
        );
    }
}
