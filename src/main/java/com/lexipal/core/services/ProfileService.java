package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.DailyActivityReadResponseDTO;
import com.lexipal.core.dtos.responses.ProfileReadResponseDTO;
import com.lexipal.core.dtos.responses.ProfileStatsResponseDTO;
import com.lexipal.core.dtos.responses.StreakMetaDataReadResponseDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.DailyActivity;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ProfileService implements ProfileServiceInterface
{
    private static final int RECENT_ACTIVITY_LIMIT = 10;

    private final UserRepository userRepository;
    private final StreakMetaDataServiceInterface streakMetaDataService;

    private static final Logger userLogger = LogManager.getLogger("UserLogger");

    @Autowired
    public ProfileService(final UserRepository userRepository,
                          final StreakMetaDataServiceInterface streakMetaDataService)
    {
        this.userRepository = userRepository;
        this.streakMetaDataService = streakMetaDataService;
    }

    @Override
    @Transactional
    public ProfileReadResponseDTO getMyProfile(String keycloakSub)
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

        ProfileStatsResponseDTO stats = buildStats(user);
        StreakMetaDataReadResponseDTO streak = streakMetaDataService.getStreakMetaData(keycloakSub);
        List<DailyActivityReadResponseDTO> recentDailyActivities = user.getDailyActivities().stream()
                .sorted(Comparator.comparing(DailyActivity::getActivityDate).reversed()
                        .thenComparing(DailyActivity::getId, Comparator.reverseOrder()))
                .limit(RECENT_ACTIVITY_LIMIT)
                .map(this::toDailyActivityReadResponse)
                .toList();

        return new ProfileReadResponseDTO(
                user.getStatus().name(),
                user.getCreatedAt(),
                stats,
                streak,
                recentDailyActivities
        );
    }

    private ProfileStatsResponseDTO buildStats(User user)
    {
        int totalPagesRead = user.getSessions().stream()
                .filter(session -> session.getStatus() == Session.SessionStatus.COMPLETED)
                .map(Session::getPagesRead)
                .filter(pagesRead -> pagesRead != null && pagesRead > 0)
                .mapToInt(Integer::intValue)
                .sum();

        int totalBooksRead = (int) user.getBooks().stream()
                .filter(book -> book.getStatus() == Book.BookStatus.COMPLETE)
                .count();

        int totalSessionsCompleted = (int) user.getSessions().stream()
                .filter(session -> session.getStatus() == Session.SessionStatus.COMPLETED)
                .count();

        int totalCapturedWords = user.getCapturedWords().size();

        int totalXpEarned = user.getDailyActivities().stream()
                .map(DailyActivity::getXpEarned)
                .filter(xp -> xp != null && xp > 0)
                .mapToInt(Integer::intValue)
                .sum();

        return new ProfileStatsResponseDTO(
                totalPagesRead,
                totalBooksRead,
                totalSessionsCompleted,
                totalCapturedWords,
                totalXpEarned
        );
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
