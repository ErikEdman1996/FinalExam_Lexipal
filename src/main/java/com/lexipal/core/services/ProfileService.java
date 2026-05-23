package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.DailyActivityReadResponseDTO;
import com.lexipal.core.dtos.responses.ProfileReadResponseDTO;
import com.lexipal.core.dtos.responses.ProfileStatsResponseDTO;
import com.lexipal.core.dtos.responses.StreakMetaDataReadResponseDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.DailyActivity;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ProfileService implements ProfileServiceInterface
{
    private static final int RECENT_ACTIVITY_LIMIT = 10;

    private final UserServiceInterface userService;
    private final StreakMetaDataServiceInterface streakMetaDataService;
    private final XpService xpService;
    private final SessionRepository sessionRepository;
    private final PortraitService portraitService;
    private final ThemeService themeService;
    private final CurrencyService currencyService;

    @Autowired
    public ProfileService(final UserServiceInterface userService,
                          final StreakMetaDataServiceInterface streakMetaDataService,
                          final XpService xpService,
                          final SessionRepository sessionRepository,
                          final PortraitService portraitService,
                          final ThemeService themeService,
                          final CurrencyService currencyService)
    {
        this.userService = userService;
        this.streakMetaDataService = streakMetaDataService;
        this.xpService = xpService;
        this.sessionRepository = sessionRepository;
        this.portraitService = portraitService;
        this.themeService = themeService;
        this.currencyService = currencyService;
    }

    @Override
    @Transactional
    public ProfileReadResponseDTO getMyProfile(String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);
        return buildProfileResponse(user, keycloakSub);
    }

    @Override
    @Transactional
    public ProfileReadResponseDTO setNickname(String keycloakSub, String nickname)
    {
        User user = userService.setNickname(keycloakSub, nickname);
        return buildProfileResponse(user, keycloakSub);
    }

    private ProfileReadResponseDTO buildProfileResponse(User user, String keycloakSub)
    {
        List<Session> completedSessions = getCompletedSessions(user);
        ProfileStatsResponseDTO stats = buildStats(user, completedSessions);
        StreakMetaDataReadResponseDTO streak = streakMetaDataService.getStreakMetaData(keycloakSub);
        List<DailyActivityReadResponseDTO> recentDailyActivities = user.getDailyActivities().stream()
                .sorted(Comparator.comparing(DailyActivity::getActivityDate).reversed()
                        .thenComparing(DailyActivity::getId, Comparator.reverseOrder()))
                .limit(RECENT_ACTIVITY_LIMIT)
                .map(this::toDailyActivityReadResponse)
                .toList();

        return new ProfileReadResponseDTO(
                user.getStatus().name(),
                user.getNickname(),
                user.getNickname() == null,
                user.getCreatedAt(),
                stats,
                streak,
                recentDailyActivities,
                portraitService.getSelectedPortraitForProfile(user),
                themeService.getSelectedThemeForProfile(user),
                currencyService.getWallet(user)
        );
    }

    private List<Session> getCompletedSessions(User user)
    {
        return sessionRepository.findByUser_IdAndStatus(
                user.getId(),
                Session.SessionStatus.COMPLETED
        );
    }

    private ProfileStatsResponseDTO buildStats(User user, List<Session> completedSessions)
    {
        long totalMinutesRead = completedSessions.stream()
                .map(Session::getDurationSeconds)
                .filter(durationSeconds -> durationSeconds != null && durationSeconds > 0)
                .mapToLong(Long::longValue)
                .sum() / 60;

        int totalPagesRead = completedSessions.stream()
                .map(Session::getPagesRead)
                .filter(pagesRead -> pagesRead != null && pagesRead > 0)
                .mapToInt(Integer::intValue)
                .sum();

        int totalBooksRead = (int) user.getBooks().stream()
                .filter(book -> book.getStatus() == Book.BookStatus.COMPLETE)
                .count();

        int totalSessionsCompleted = (int) completedSessions.stream()
                .count();

        int totalXpEarned = xpService.getCurrentTotalXp(user);

        return new ProfileStatsResponseDTO(
                totalMinutesRead,
                totalPagesRead,
                totalBooksRead,
                totalSessionsCompleted,
                totalXpEarned,
                xpService.calculateLevel(totalXpEarned),
                xpService.calculateXpIntoLevel(totalXpEarned),
                xpService.calculateXpPerLevel(totalXpEarned),
                xpService.getMaxLevel(),
                xpService.calculateCurrentLevelMinXp(totalXpEarned),
                xpService.calculateNextLevelMinXp(totalXpEarned),
                xpService.calculateXpToNextLevel(totalXpEarned)
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
