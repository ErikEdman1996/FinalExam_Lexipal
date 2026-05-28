package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.DailyActivityReadResponseDTO;
import com.lexipal.core.dtos.responses.DailyQuestResponseDTO;
import com.lexipal.core.dtos.responses.ProfileReadResponseDTO;
import com.lexipal.core.dtos.responses.ProfileReadingSeriesPointDTO;
import com.lexipal.core.dtos.responses.ProfileReadingSeriesRange;
import com.lexipal.core.dtos.responses.ProfileReadingSeriesResponseDTO;
import com.lexipal.core.dtos.responses.ProfileStatsResponseDTO;
import com.lexipal.core.dtos.responses.ReadingSeriesBucketType;
import com.lexipal.core.dtos.responses.StreakMetaDataReadResponseDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.DailyActivity;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.projections.CompletedSessionPageProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final DailyQuestServiceInterface dailyQuestService;
    private final ZoneId analyticsZone;

    @Autowired
    public ProfileService(final UserServiceInterface userService,
                          final StreakMetaDataServiceInterface streakMetaDataService,
                          final XpService xpService,
                          final SessionRepository sessionRepository,
                          final PortraitService portraitService,
                          final ThemeService themeService,
                          final CurrencyService currencyService,
                          final DailyQuestServiceInterface dailyQuestService,
                          @Value("${lexipal.analytics.timezone:Europe/Stockholm}") String timezone)
    {
        this.userService = userService;
        this.streakMetaDataService = streakMetaDataService;
        this.xpService = xpService;
        this.sessionRepository = sessionRepository;
        this.portraitService = portraitService;
        this.themeService = themeService;
        this.currencyService = currencyService;
        this.dailyQuestService = dailyQuestService;
        this.analyticsZone = ZoneId.of(timezone);
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

    @Override
    @Transactional
    public ProfileReadingSeriesResponseDTO getMyReadingSeries(String keycloakSub, ProfileReadingSeriesRange range)
    {
        if (range == null)
        {
            throw new IllegalArgumentException("range is required.");
        }

        User user = userService.getOrCreateUser(keycloakSub);
        LocalDate endDate = LocalDate.now(analyticsZone);
        LocalDate startDate = resolveReadingSeriesStartDate(user.getId(), range, endDate);
        ReadingSeriesBucketType bucketType = resolveBucketType(range);
        Map<LocalDate, Long> buckets = seedBuckets(startDate, endDate, bucketType);
        LocalDateTime queryStart = startDate.atStartOfDay();
        LocalDateTime queryEnd = endDate.plusDays(1).atStartOfDay();

        for (CompletedSessionPageProjection row : sessionRepository.findCompletedPageRowsByUserIdBetween(
                user.getId(), queryStart, queryEnd))
        {
            LocalDate bucketDate = toBucketDate(row.getCompletedAt(), bucketType);
            buckets.computeIfPresent(bucketDate, (ignored, currentPages) -> currentPages + safePagesRead(row));
        }

        List<ProfileReadingSeriesPointDTO> points = buckets.entrySet().stream()
                .map(entry -> new ProfileReadingSeriesPointDTO(entry.getKey(), entry.getValue()))
                .toList();

        return new ProfileReadingSeriesResponseDTO(
                range,
                startDate,
                endDate,
                analyticsZone.getId(),
                bucketType,
                points
        );
    }

    private ProfileReadResponseDTO buildProfileResponse(User user, String keycloakSub)
    {
        DailyQuestResponseDTO dailyQuest = dailyQuestService.getTodayQuestForUser(user);
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
                currencyService.getWallet(user),
                dailyQuest
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

    private LocalDate resolveReadingSeriesStartDate(Long userId, ProfileReadingSeriesRange range, LocalDate endDate)
    {
        return switch (range)
        {
            case THIS_WEEK -> endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case THIS_MONTH -> endDate.withDayOfMonth(1);
            case THIS_YEAR -> endDate.withDayOfYear(1);
            case ALL_TIME -> {
                LocalDateTime earliestCompletedAt = sessionRepository.findEarliestCompletedAtByUserId(userId);
                yield earliestCompletedAt == null ? endDate : earliestCompletedAt.toLocalDate();
            }
        };
    }

    private ReadingSeriesBucketType resolveBucketType(ProfileReadingSeriesRange range)
    {
        return switch (range)
        {
            case THIS_WEEK, THIS_MONTH -> ReadingSeriesBucketType.DAY;
            case THIS_YEAR, ALL_TIME -> ReadingSeriesBucketType.MONTH;
        };
    }

    private Map<LocalDate, Long> seedBuckets(LocalDate startDate, LocalDate endDate, ReadingSeriesBucketType bucketType)
    {
        Map<LocalDate, Long> buckets = new LinkedHashMap<>();

        if (bucketType == ReadingSeriesBucketType.DAY)
        {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1))
            {
                buckets.put(date, 0L);
            }
            return buckets;
        }

        LocalDate startMonth = startDate.withDayOfMonth(1);
        LocalDate endMonth = endDate.withDayOfMonth(1);
        for (LocalDate date = startMonth; !date.isAfter(endMonth); date = date.plusMonths(1))
        {
            buckets.put(date, 0L);
        }

        return buckets;
    }

    private LocalDate toBucketDate(LocalDateTime completedAt, ReadingSeriesBucketType bucketType)
    {
        LocalDate completedDate = completedAt.toLocalDate();
        return bucketType == ReadingSeriesBucketType.MONTH
                ? completedDate.withDayOfMonth(1)
                : completedDate;
    }

    private long safePagesRead(CompletedSessionPageProjection row)
    {
        Integer pagesRead = row.getPagesRead();
        return pagesRead == null ? 0L : pagesRead;
    }
}
