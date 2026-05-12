package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.AchievementResponseDTO;
import com.lexipal.core.entities.AchievementDefinition;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.UserAchievement;
import com.lexipal.core.repositories.AchievementDefinitionRepository;
import com.lexipal.core.repositories.BookCircleRepository;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.CircleMemberRepository;
import com.lexipal.core.repositories.CodexEntryRepository;
import com.lexipal.core.repositories.ReadingListRepository;
import com.lexipal.core.repositories.ReadingListSubscriptionRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AchievementService
{
    public static final String METRIC_PAGES_READ = "pages_read";
    public static final String METRIC_BOOKS_COMPLETED = "books_completed";
    public static final String METRIC_CODEX_WORDS = "codex_words";
    public static final String METRIC_READING_LIST_STARS = "reading_list_stars";
    public static final String METRIC_READING_LIST_SUBSCRIBERS = "reading_list_subscribers";
    public static final String METRIC_BOOK_CIRCLES_STARTED = "book_circles_started";
    public static final String METRIC_BOOK_CIRCLE_BOOKS_COMPLETED = "book_circle_books_completed";

    private final AchievementDefinitionRepository achievementDefinitionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserServiceInterface userService;
    private final SessionRepository sessionRepository;
    private final BookRepository bookRepository;
    private final CodexEntryRepository codexEntryRepository;
    private final ReadingListRepository readingListRepository;
    private final ReadingListSubscriptionRepository readingListSubscriptionRepository;
    private final BookCircleRepository bookCircleRepository;
    private final CircleMemberRepository circleMemberRepository;

    @Autowired
    public AchievementService(final AchievementDefinitionRepository achievementDefinitionRepository,
                              final UserAchievementRepository userAchievementRepository,
                              final UserServiceInterface userService,
                              final SessionRepository sessionRepository,
                              final BookRepository bookRepository,
                              final CodexEntryRepository codexEntryRepository,
                              final ReadingListRepository readingListRepository,
                              final ReadingListSubscriptionRepository readingListSubscriptionRepository,
                              final BookCircleRepository bookCircleRepository,
                              final CircleMemberRepository circleMemberRepository)
    {
        this.achievementDefinitionRepository = achievementDefinitionRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userService = userService;
        this.sessionRepository = sessionRepository;
        this.bookRepository = bookRepository;
        this.codexEntryRepository = codexEntryRepository;
        this.readingListRepository = readingListRepository;
        this.readingListSubscriptionRepository = readingListSubscriptionRepository;
        this.bookCircleRepository = bookCircleRepository;
        this.circleMemberRepository = circleMemberRepository;
    }

    public List<AchievementResponseDTO> getAchievementsForUser(String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);
        evaluateAchievements(user);
        return buildAchievementResponses(user);
    }

    public List<AchievementResponseDTO> evaluateAchievements(User user)
    {
        if (user == null || user.getId() == null)
        {
            return List.of();
        }

        Map<String, Integer> progressByMetric = buildProgressByMetric(user.getId());
        Map<Long, UserAchievement> unlockedByAchievementId = userAchievementRepository.findByUser_Id(user.getId())
                .stream()
                .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), Function.identity()));
        List<AchievementResponseDTO> newlyUnlockedAchievements = new ArrayList<>();

        for (AchievementDefinition definition : achievementDefinitionRepository.findByActiveTrueOrderBySortOrderAsc())
        {
            if (unlockedByAchievementId.containsKey(definition.getId()))
            {
                continue;
            }

            int progress = progressByMetric.getOrDefault(definition.getMetricKey(), 0);
            if (progress < definition.getThreshold())
            {
                continue;
            }

            UserAchievement unlock = new UserAchievement();
            unlock.setUser(user);
            unlock.setAchievement(definition);
            unlock.setUnlockedAt(LocalDateTime.now());
            unlock.setProgressValueAtUnlock(progress);
            UserAchievement savedUnlock = userAchievementRepository.save(unlock);
            unlockedByAchievementId.put(definition.getId(), savedUnlock);
            newlyUnlockedAchievements.add(toAchievementResponse(definition, progress, savedUnlock));
        }

        return newlyUnlockedAchievements;
    }

    private List<AchievementResponseDTO> buildAchievementResponses(User user)
    {
        Map<String, Integer> progressByMetric = buildProgressByMetric(user.getId());
        Map<Long, UserAchievement> unlockedByAchievementId = userAchievementRepository.findByUser_Id(user.getId())
                .stream()
                .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), Function.identity()));

        return achievementDefinitionRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(definition -> {
                    UserAchievement unlock = unlockedByAchievementId.get(definition.getId());
                    return toAchievementResponse(
                            definition,
                            progressByMetric.getOrDefault(definition.getMetricKey(), 0),
                            unlock);
                })
                .collect(Collectors.toList());
    }

    private AchievementResponseDTO toAchievementResponse(AchievementDefinition definition, int progress,
                                                         UserAchievement unlock)
    {
        return new AchievementResponseDTO(
                definition.getCode(),
                definition.getCategory(),
                definition.getTitle(),
                definition.getDescription(),
                definition.getThreshold(),
                progress,
                unlock != null,
                unlock == null ? null : unlock.getUnlockedAt(),
                definition.getSortOrder()
        );
    }

    private Map<String, Integer> buildProgressByMetric(Long userId)
    {
        Map<String, Integer> progress = new HashMap<>();
        progress.put(METRIC_PAGES_READ, sessionRepository.sumCompletedPagesByUserId(userId));
        progress.put(METRIC_BOOKS_COMPLETED,
                Math.toIntExact(bookRepository.countByUser_IdAndStatus(userId, Book.BookStatus.COMPLETE)));
        progress.put(METRIC_CODEX_WORDS, Math.toIntExact(codexEntryRepository.countByUser_Id(userId)));
        progress.put(METRIC_READING_LIST_STARS, readingListRepository.findMaxStarCountByOwnerId(userId));
        progress.put(METRIC_READING_LIST_SUBSCRIBERS,
                readingListSubscriptionRepository.findMaxSubscriberCountByOwnerId(userId));
        progress.put(METRIC_BOOK_CIRCLES_STARTED, Math.toIntExact(bookCircleRepository.countByOwner_Id(userId)));
        progress.put(METRIC_BOOK_CIRCLE_BOOKS_COMPLETED,
                Math.toIntExact(circleMemberRepository.countByUser_IdAndBook_Status(userId, Book.BookStatus.COMPLETE)));
        return progress;
    }
}
