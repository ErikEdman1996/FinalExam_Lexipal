package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.admin.AdminAnalyticsRange;
import com.lexipal.core.dtos.responses.admin.AdminDashboardDailyPointDTO;
import com.lexipal.core.dtos.responses.admin.AdminDashboardLifetimeDTO;
import com.lexipal.core.dtos.responses.admin.AdminDashboardRangeDTO;
import com.lexipal.core.dtos.responses.admin.AdminDashboardResponseDTO;
import com.lexipal.core.dtos.responses.admin.AdminDashboardSummaryDTO;
import com.lexipal.core.dtos.responses.admin.AdminDashboardTopBookDTO;
import com.lexipal.core.dtos.responses.admin.AdminDashboardTopUserDTO;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.DailyActivityRepository;
import com.lexipal.core.repositories.ReadingListRepository;
import com.lexipal.core.repositories.ReadingListSubscriptionRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import com.lexipal.core.repositories.projections.AdminDailyBookProjection;
import com.lexipal.core.repositories.projections.AdminDailySessionProjection;
import com.lexipal.core.repositories.projections.AdminDailyUserProjection;
import com.lexipal.core.repositories.projections.AdminDailyXpProjection;
import com.lexipal.core.repositories.projections.AdminTopBookProjection;
import com.lexipal.core.repositories.projections.AdminTopUserProjection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminAnalyticsService
{
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final BookRepository bookRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final ReadingListRepository readingListRepository;
    private final ReadingListSubscriptionRepository subscriptionRepository;
    private final ZoneId analyticsZone;

    public AdminAnalyticsService(final UserRepository userRepository,
                                 final SessionRepository sessionRepository,
                                 final BookRepository bookRepository,
                                 final DailyActivityRepository dailyActivityRepository,
                                 final ReadingListRepository readingListRepository,
                                 final ReadingListSubscriptionRepository subscriptionRepository,
                                 @Value("${lexipal.analytics.timezone:Europe/Stockholm}") String timezone)
    {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bookRepository = bookRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.readingListRepository = readingListRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.analyticsZone = ZoneId.of(timezone);
    }

    public AdminDashboardResponseDTO getDashboard(AdminAnalyticsRange range)
    {
        AdminAnalyticsRange safeRange = range == null ? AdminAnalyticsRange.THIS_MONTH : range;
        LocalDate endDate = LocalDate.now(analyticsZone);
        LocalDate startDate = resolveStartDate(safeRange, endDate);
        LocalDate queryStartDate = startDate == null ? resolveEarliestDate(endDate) : startDate;
        LocalDateTime queryStart = queryStartDate.atStartOfDay();
        LocalDateTime queryEnd = endDate.plusDays(1).atStartOfDay();

        AdminDashboardLifetimeDTO lifetime = buildLifetime();
        AdminDashboardSummaryDTO summary = buildSummary(queryStart, queryEnd, queryStartDate, endDate, lifetime, safeRange);
        List<AdminDashboardDailyPointDTO> dailySeries = buildDailySeries(queryStartDate, endDate, queryStart, queryEnd);

        return new AdminDashboardResponseDTO(
                new AdminDashboardRangeDTO(safeRange, startDate, endDate, analyticsZone.getId()),
                summary,
                lifetime,
                dailySeries,
                buildTopUsers(),
                buildTopBooks()
        );
    }

    private AdminDashboardLifetimeDTO buildLifetime()
    {
        return new AdminDashboardLifetimeDTO(
                userRepository.count(),
                bookRepository.countAllNonDeleted(),
                bookRepository.countAllCompleted(),
                sessionRepository.sumAllCompletedPagesRead(),
                sessionRepository.countAllCompleted(),
                dailyActivityRepository.sumAllPositiveXpEarned()
        );
    }

    private AdminDashboardSummaryDTO buildSummary(LocalDateTime queryStart, LocalDateTime queryEnd,
                                                  LocalDate queryStartDate, LocalDate endDate,
                                                  AdminDashboardLifetimeDTO lifetime,
                                                  AdminAnalyticsRange range)
    {
        long newUsers = range == AdminAnalyticsRange.ALL_TIME
                ? lifetime.totalUsers()
                : userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(queryStart, queryEnd);

        return new AdminDashboardSummaryDTO(
                lifetime.totalUsers(),
                newUsers,
                sessionRepository.countActiveUsersBetween(queryStart, queryEnd),
                sessionRepository.sumPagesReadCompletedBetween(queryStart, queryEnd),
                bookRepository.countCompletedBetween(queryStart, queryEnd),
                sessionRepository.countCompletedBetween(queryStart, queryEnd),
                sessionRepository.sumDurationSecondsCompletedBetween(queryStart, queryEnd) / 60,
                dailyActivityRepository.sumPositiveXpEarnedBetween(queryStartDate, endDate),
                readingListRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(queryStart, queryEnd),
                subscriptionRepository.countBySubscribedAtGreaterThanEqualAndSubscribedAtLessThan(queryStart, queryEnd),
                subscriptionRepository.countByCompletedAtGreaterThanEqualAndCompletedAtLessThan(queryStart, queryEnd)
        );
    }

    private List<AdminDashboardDailyPointDTO> buildDailySeries(LocalDate queryStartDate, LocalDate endDate,
                                                               LocalDateTime queryStart, LocalDateTime queryEnd)
    {
        Map<LocalDate, DailyAccumulator> days = new LinkedHashMap<>();
        for (LocalDate date = queryStartDate; !date.isAfter(endDate); date = date.plusDays(1))
        {
            days.put(date, new DailyAccumulator());
        }

        for (AdminDailySessionProjection row : sessionRepository.findDailySessionStats(queryStart, queryEnd))
        {
            DailyAccumulator acc = days.computeIfAbsent(toLocalDate(row.getDate()), ignored -> new DailyAccumulator());
            acc.pagesRead = row.getPagesRead();
            acc.completedSessions = row.getCompletedSessions();
            acc.activeUsers = row.getActiveUsers();
        }

        for (AdminDailyBookProjection row : bookRepository.findDailyCompletedBooks(queryStart, queryEnd))
        {
            days.computeIfAbsent(toLocalDate(row.getDate()), ignored -> new DailyAccumulator())
                    .completedBooks = row.getCompletedBooks();
        }

        for (AdminDailyUserProjection row : userRepository.findDailyNewUsers(queryStart, queryEnd))
        {
            days.computeIfAbsent(toLocalDate(row.getDate()), ignored -> new DailyAccumulator())
                    .newUsers = row.getNewUsers();
        }

        for (AdminDailyXpProjection row : dailyActivityRepository.findDailyXpAwarded(queryStartDate, endDate))
        {
            days.computeIfAbsent(toLocalDate(row.getDate()), ignored -> new DailyAccumulator())
                    .xpAwarded = row.getXpAwarded();
        }

        return days.entrySet().stream()
                .map(entry -> new AdminDashboardDailyPointDTO(
                        entry.getKey(),
                        entry.getValue().pagesRead,
                        entry.getValue().completedSessions,
                        entry.getValue().completedBooks,
                        entry.getValue().newUsers,
                        entry.getValue().activeUsers,
                        entry.getValue().xpAwarded
                ))
                .toList();
    }

    private List<AdminDashboardTopUserDTO> buildTopUsers()
    {
        return userRepository.findTopUsers().stream()
                .map(this::toTopUser)
                .collect(Collectors.toList());
    }

    private List<AdminDashboardTopBookDTO> buildTopBooks()
    {
        return bookRepository.findTopBooks().stream()
                .map(this::toTopBook)
                .collect(Collectors.toList());
    }

    private AdminDashboardTopUserDTO toTopUser(AdminTopUserProjection row)
    {
        return new AdminDashboardTopUserDTO(
                row.getUserId(),
                row.getNickname(),
                row.getTotalPagesRead(),
                row.getCompletedBooks(),
                row.getCompletedSessions(),
                row.getTotalXpEarned()
        );
    }

    private AdminDashboardTopBookDTO toTopBook(AdminTopBookProjection row)
    {
        return new AdminDashboardTopBookDTO(
                row.getTitle(),
                row.getAuthor(),
                row.getCompletedCount(),
                row.getPagesRead()
        );
    }

    private LocalDate resolveStartDate(AdminAnalyticsRange range, LocalDate endDate)
    {
        return switch (range)
        {
            case TODAY -> endDate;
            case LAST_7_DAYS -> endDate.minusDays(6);
            case THIS_MONTH -> endDate.withDayOfMonth(1);
            case LAST_30_DAYS -> endDate.minusDays(29);
            case ALL_TIME -> null;
        };
    }

    private LocalDate resolveEarliestDate(LocalDate fallback)
    {
        return Stream.of(
                        toLocalDate(userRepository.findEarliestCreatedAt()),
                        toLocalDate(sessionRepository.findEarliestCompletedAt()),
                        toLocalDate(bookRepository.findEarliestCompletedAt()),
                        toLocalDate(readingListRepository.findEarliestCreatedAt()),
                        toLocalDate(subscriptionRepository.findEarliestSubscribedAt()),
                        toLocalDate(subscriptionRepository.findEarliestCompletedAt())
                )
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(fallback);
    }

    private LocalDate toLocalDate(LocalDateTime value)
    {
        return value == null ? null : value.toLocalDate();
    }

    private LocalDate toLocalDate(Object value)
    {
        if (value instanceof LocalDate localDate)
        {
            return localDate;
        }
        if (value instanceof Date date)
        {
            return date.toLocalDate();
        }
        if (value instanceof java.util.Date date)
        {
            return date.toInstant().atZone(analyticsZone).toLocalDate();
        }
        return LocalDate.parse(String.valueOf(value));
    }

    private static class DailyAccumulator
    {
        private long pagesRead;
        private long completedSessions;
        private long completedBooks;
        private long newUsers;
        private long activeUsers;
        private long xpAwarded;
    }
}
