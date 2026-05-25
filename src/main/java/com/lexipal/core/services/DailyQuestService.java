package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.DailyQuestResponseDTO;
import com.lexipal.core.dtos.responses.XpUpdateResponseDTO;
import com.lexipal.core.entities.CurrencyTransactionReason;
import com.lexipal.core.entities.DailyActivity;
import com.lexipal.core.entities.DailyQuest;
import com.lexipal.core.entities.DailyQuestStatus;
import com.lexipal.core.entities.DailyQuestType;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.DailyActivityRepository;
import com.lexipal.core.repositories.DailyQuestRepository;
import com.lexipal.core.repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional
public class DailyQuestService implements DailyQuestServiceInterface
{
    static final int XP_REWARD = 10;
    static final int COIN_REWARD = 10;

    private static final int DEFAULT_MINUTE_TARGET = 5;
    private static final int MIN_MINUTE_TARGET = 5;
    private static final int MAX_MINUTE_TARGET = 45;
    private static final int MAX_MINUTE_INCREASE = 5;
    private static final int MIN_PAGE_TARGET = 5;
    private static final int PAGE_CATCH_UP_TARGET = 10;
    private static final int MAX_PAGE_TARGET = 30;
    private static final int MAX_PAGE_INCREASE = 5;

    private final DailyQuestRepository dailyQuestRepository;
    private final SessionRepository sessionRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final UserServiceInterface userService;
    private final XpService xpService;
    private final CurrencyService currencyService;
    private final ZoneId questZone;

    public DailyQuestService(final DailyQuestRepository dailyQuestRepository,
                             final SessionRepository sessionRepository,
                             final DailyActivityRepository dailyActivityRepository,
                             final UserServiceInterface userService,
                             final XpService xpService,
                             final CurrencyService currencyService,
                             @Value("${lexipal.streak.timezone:Europe/Stockholm}") String timezone)
    {
        this.dailyQuestRepository = dailyQuestRepository;
        this.sessionRepository = sessionRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.userService = userService;
        this.xpService = xpService;
        this.currencyService = currencyService;
        this.questZone = ZoneId.of(timezone);
    }

    @Override
    public DailyQuestResponseDTO getTodayQuest(String keycloakSub)
    {
        return getTodayQuestForUser(userService.getOrCreateUser(keycloakSub));
    }

    @Override
    public DailyQuestResponseDTO getTodayQuestForUser(User user)
    {
        LocalDate questDate = LocalDate.now(questZone);
        DailyQuest quest = getOrCreateQuest(user, questDate);
        return toDailyQuestResponse(refreshProgressAndMaybeComplete(quest, null, true).quest());
    }

    @Override
    public DailyQuestEvaluationResult evaluateSessionCompletion(Session session)
    {
        if (session == null || session.getUser() == null || session.getCompletedAt() == null)
        {
            return null;
        }

        LocalDate questDate = session.getCompletedAt().toLocalDate();
        DailyQuest quest = getOrCreateQuest(session.getUser(), questDate);
        CompletionResult result = refreshProgressAndMaybeComplete(quest, session, true);

        if (result.completedNow())
        {
            return new DailyQuestEvaluationResult(toDailyQuestResponse(result.quest()), result.xpUpdate());
        }

        return null;
    }

    DailyQuest createQuest(User user, LocalDate questDate)
    {
        LocalDateTime questDayStart = questDate.atStartOfDay();
        Session baselineSession = sessionRepository.findLatestCompletedBefore(
                user.getId(), questDayStart).orElse(null);
        DailyQuest previousQuest = dailyQuestRepository
                .findFirstByUser_IdAndQuestDateLessThanOrderByQuestDateDesc(user.getId(), questDate)
                .orElse(null);

        DailyQuestType type = chooseQuestType(baselineSession, previousQuest);
        int baselineValue = baselineValue(baselineSession, type);
        int targetValue = calculateTarget(type, baselineValue);

        DailyQuest quest = new DailyQuest();
        quest.setUser(user);
        quest.setQuestDate(questDate);
        quest.setType(type);
        quest.setTargetValue(targetValue);
        quest.setProgressValue(0);
        quest.setStatus(DailyQuestStatus.ACTIVE);
        quest.setXpReward(XP_REWARD);
        quest.setCoinReward(COIN_REWARD);
        quest.setBaselineValue(baselineValue);

        return dailyQuestRepository.save(quest);
    }

    DailyQuestType chooseQuestType(Session baselineSession, DailyQuest previousQuest)
    {
        boolean hasMinutes = baselineSession != null
                && baselineSession.getDurationSeconds() != null
                && baselineSession.getDurationSeconds() > 0;
        boolean hasPages = baselineSession != null
                && baselineSession.getPagesRead() != null
                && baselineSession.getPagesRead() > 0;

        if (hasMinutes && hasPages)
        {
            return previousQuest != null && previousQuest.getType() == DailyQuestType.READ_MINUTES
                    ? DailyQuestType.READ_PAGES
                    : DailyQuestType.READ_MINUTES;
        }

        if (hasPages)
        {
            return DailyQuestType.READ_PAGES;
        }

        return DailyQuestType.READ_MINUTES;
    }

    int calculateTarget(DailyQuestType type, int baselineValue)
    {
        if (type == DailyQuestType.READ_PAGES)
        {
            if (baselineValue < PAGE_CATCH_UP_TARGET)
            {
                return PAGE_CATCH_UP_TARGET;
            }

            int increase = Math.max(1, Math.min(MAX_PAGE_INCREASE, (int) Math.ceil(baselineValue * 0.1)));
            return clamp(baselineValue + increase, MIN_PAGE_TARGET, MAX_PAGE_TARGET);
        }

        if (baselineValue <= 0)
        {
            return DEFAULT_MINUTE_TARGET;
        }

        int increase = Math.max(1, Math.min(MAX_MINUTE_INCREASE, (int) Math.ceil(baselineValue * 0.1)));
        return clamp(baselineValue + increase, MIN_MINUTE_TARGET, MAX_MINUTE_TARGET);
    }

    private DailyQuest getOrCreateQuest(User user, LocalDate questDate)
    {
        return dailyQuestRepository.findByUser_IdAndQuestDate(user.getId(), questDate)
                .orElseGet(() -> {
                    try
                    {
                        return createQuest(user, questDate);
                    }
                    catch (DataIntegrityViolationException ignored)
                    {
                        return dailyQuestRepository.findByUser_IdAndQuestDate(user.getId(), questDate)
                                .orElseThrow();
                    }
                });
    }

    private CompletionResult refreshProgressAndMaybeComplete(DailyQuest quest, Session sourceSession, boolean award)
    {
        DailyQuest lockedQuest = quest.getId() == null
                ? quest
                : dailyQuestRepository.findByIdForUpdate(quest.getId()).orElse(quest);
        LocalDateTime start = lockedQuest.getQuestDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        int progress = calculateProgress(lockedQuest, start, end);

        lockedQuest.setProgressValue(progress);

        boolean completedNow = false;
        XpUpdateResponseDTO xpUpdate = null;

        if (lockedQuest.getStatus() == DailyQuestStatus.ACTIVE
                && progress >= lockedQuest.getTargetValue())
        {
            completedNow = true;
            lockedQuest.setStatus(DailyQuestStatus.COMPLETED);
            lockedQuest.setCompletedAt(LocalDateTime.now());
            lockedQuest.setSourceSession(resolveCompletionSourceSession(lockedQuest, sourceSession, start, end));
            dailyQuestRepository.save(lockedQuest);

            if (award)
            {
                xpUpdate = awardQuestCompletion(lockedQuest);
            }
        }
        else
        {
            dailyQuestRepository.save(lockedQuest);
        }

        return new CompletionResult(lockedQuest, completedNow, xpUpdate);
    }

    private XpUpdateResponseDTO awardQuestCompletion(DailyQuest quest)
    {
        boolean alreadyAwarded = dailyActivityRepository.existsByUser_IdAndActivityTypeAndExternalRefId(
                quest.getUser().getId(),
                DailyActivity.ActivityType.DAILY_QUEST_COMPLETED,
                quest.getId());

        if (alreadyAwarded)
        {
            return xpService.buildZeroAwardUpdate(quest.getUser());
        }

        DailyActivity activity = new DailyActivity();
        activity.setUser(quest.getUser());
        activity.setSession(quest.getSourceSession());
        activity.setActivityDate(quest.getQuestDate());
        activity.setActivityType(DailyActivity.ActivityType.DAILY_QUEST_COMPLETED);
        activity.setXpEarned(quest.getXpReward());
        activity.setExternalRefId(quest.getId());
        dailyActivityRepository.save(activity);

        XpUpdateResponseDTO xpUpdate = xpService.awardXp(quest.getUser(), quest.getXpReward());
        currencyService.awardCoins(quest.getUser(),
                CurrencyTransactionReason.DAILY_QUEST_COMPLETED,
                "DAILY_QUEST",
                quest.getId());

        return xpUpdate;
    }

    private int calculateProgress(DailyQuest quest, LocalDateTime start, LocalDateTime end)
    {
        if (quest.getType() == DailyQuestType.READ_PAGES)
        {
            return sessionRepository.sumCompletedPagesByUserIdBetween(quest.getUser().getId(), start, end);
        }

        long seconds = sessionRepository.sumCompletedDurationSecondsByUserIdBetween(
                quest.getUser().getId(), start, end);
        return Math.toIntExact(seconds / 60);
    }

    private Session resolveCompletionSourceSession(DailyQuest quest, Session sourceSession,
                                                   LocalDateTime start, LocalDateTime end)
    {
        if (sourceSession != null)
        {
            return sourceSession;
        }

        return sessionRepository.findLatestCompletedBetween(
                quest.getUser().getId(), start, end).orElse(null);
    }

    private int baselineValue(Session baselineSession, DailyQuestType type)
    {
        if (baselineSession == null)
        {
            return 0;
        }

        if (type == DailyQuestType.READ_PAGES)
        {
            return baselineSession.getPagesRead() == null ? 0 : Math.max(baselineSession.getPagesRead(), 0);
        }

        Long durationSeconds = baselineSession.getDurationSeconds();
        if (durationSeconds == null || durationSeconds <= 0)
        {
            return 0;
        }

        return Math.toIntExact(durationSeconds / 60);
    }

    private DailyQuestResponseDTO toDailyQuestResponse(DailyQuest quest)
    {
        String unit = quest.getType() == DailyQuestType.READ_PAGES ? "pages" : "minutes";
        String title = quest.getType() == DailyQuestType.READ_PAGES
                ? "Read " + quest.getTargetValue() + " pages"
                : "Read " + quest.getTargetValue() + " minutes";
        String description = "Complete today's reading quest to earn "
                + quest.getXpReward() + " XP and " + quest.getCoinReward() + " coins.";

        return new DailyQuestResponseDTO(
                quest.getId(),
                quest.getQuestDate(),
                quest.getType().name(),
                title,
                description,
                quest.getTargetValue(),
                quest.getProgressValue(),
                unit,
                quest.getStatus().name(),
                quest.getXpReward(),
                quest.getCoinReward(),
                quest.getCompletedAt()
        );
    }

    private int clamp(int value, int min, int max)
    {
        return Math.max(min, Math.min(value, max));
    }

    private record CompletionResult(DailyQuest quest, boolean completedNow, XpUpdateResponseDTO xpUpdate)
    {
    }
}
