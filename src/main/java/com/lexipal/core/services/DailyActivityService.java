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
    private static final Logger logger = LogManager.getLogger(DailyActivityService.class);

    private final DailyActivityRepository dailyActivityRepository;
    private final SessionRepository sessionRepository;
    private final StreakMetaDataServiceInterface streakMetaDataService;
    private final UserServiceInterface userService;

    @Autowired
    public DailyActivityService(final DailyActivityRepository dailyActivityRepository,
                                final SessionRepository sessionRepository,
                                final StreakMetaDataServiceInterface streakMetaDataService,
                                final UserServiceInterface userService)
    {
        this.dailyActivityRepository = dailyActivityRepository;
        this.sessionRepository = sessionRepository;
        this.streakMetaDataService = streakMetaDataService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public DailyActivityReadResponseDTO createDailyActivity(DailyActivityCreateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);

        Session session = null;
        if (dto.getSessionId() != null)
        {
            session = sessionRepository.findById(dto.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session", "id", dto.getSessionId()));

            if (!session.getUser().getId().equals(user.getId()))
            {
                logger.warn("User {} attempted to create daily activity for session {} they do not own", user.getId(), dto.getSessionId());
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
                logger.info("Reused existing daily activity {} for session {} and activity type {}", existingActivity.getId(), session.getId(), dto.getActivityType());
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
        logger.info("Created daily activity {} of type {} for user {}", savedActivity.getId(), savedActivity.getActivityType(), user.getId());

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
        userService.getOrCreateUser(keycloakSub);

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
