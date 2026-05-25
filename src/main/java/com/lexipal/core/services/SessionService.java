package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.SessionCompleteRequestDTO;
import com.lexipal.core.dtos.requests.SessionCreateRequestDTO;
import com.lexipal.core.dtos.requests.SessionUpdateRequestDTO;
import com.lexipal.core.dtos.responses.AchievementResponseDTO;
import com.lexipal.core.dtos.responses.SessionCreateResponseDTO;
import com.lexipal.core.dtos.responses.SessionReadResponseDTO;
import com.lexipal.core.dtos.responses.SessionUpdateResponseDTO;
import com.lexipal.core.dtos.responses.XpUpdateResponseDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.CurrencyTransactionReason;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.SessionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionService implements SessionServiceInterface
{
    private static final Logger logger = LogManager.getLogger(SessionService.class);
    private static final int SESSION_COMPLETION_XP = 25;

    final SessionRepository sessionRepository;
    final BookRepository bookRepository;
    private final BookServiceInterface bookService;
    private final DailyActivityServiceInterface dailyActivityService;
    private final UserServiceInterface userService;
    private final XpService xpService;
    private final CurrencyService currencyService;
    private final BookLifecycleValidator bookLifecycleValidator;
    private final AchievementService achievementService;
    private final DailyQuestServiceInterface dailyQuestService;

    @Autowired
    public SessionService(final SessionRepository sessionRepository,
                          final BookRepository bookRepository,
                          final BookServiceInterface bookService,
                          final DailyActivityServiceInterface dailyActivityService,
                          final UserServiceInterface userService,
                          final XpService xpService,
                          final CurrencyService currencyService,
                          final BookLifecycleValidator bookLifecycleValidator,
                          final AchievementService achievementService,
                          final DailyQuestServiceInterface dailyQuestService)
    {
        this.sessionRepository = sessionRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.dailyActivityService = dailyActivityService;
        this.userService = userService;
        this.xpService = xpService;
        this.currencyService = currencyService;
        this.bookLifecycleValidator = bookLifecycleValidator;
        this.achievementService = achievementService;
        this.dailyQuestService = dailyQuestService;
    }

    @Override
    @Transactional
    public SessionCreateResponseDTO addSession(SessionCreateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Long bookId = dto.getBookId();

        Book book = bookRepository.findById(bookId).orElseThrow(()->new ResourceNotFoundException("Book", "id", bookId));

        if(!book.getUser().getId().equals(user.getId()))
        {
            logger.warn("User {} attempted to create a session for book {} they do not own", user.getId(), bookId);
            throw new UnauthorizedActionException("This book does belong to you.");
        }

        bookLifecycleValidator.assertAllowsSessionMutations(book);
        validateSessionPages(book, dto.getStartPage(), dto.getEndPage());

        sessionRepository.findFirstByBookIdAndUser_IdAndStatusOrderBySessionDateDesc(
                        bookId,
                        user.getId(),
                        Session.SessionStatus.IN_PROGRESS
                )
                .ifPresent(session -> {
                    throw new IllegalArgumentException("You already have an active session for this book.");
                });

        Session session = new Session();
        session.setUser(user);
        session.setBook(book);
        session.setStartPage(dto.getStartPage());
        session.setEndPage(dto.getEndPage());
        session.setDurationSeconds(dto.getDurationSeconds());
        session.setReflection(dto.getReflection() == null ? null : dto.getReflection().trim());

        Session savedSession = sessionRepository.save(session);

        logger.info("Created session {} for book {} and user {}", savedSession.getId(), book.getId(), user.getId());

        return toSessionCreateResponse(savedSession, null);
    }

    @Override
    @Transactional
    public SessionUpdateResponseDTO updateSession(Long sessionId, SessionUpdateRequestDTO dto, String keycloakSub)
    {
        Session existingSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        if (!existingSession.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to update session {} they do not own", existingSession.getUser().getId(), sessionId);
            throw new UnauthorizedActionException("This session does not belong to you.");
        }

        bookLifecycleValidator.assertAllowsSessionMutations(existingSession.getBook());

        if (existingSession.getStatus() == Session.SessionStatus.COMPLETED)
        {
            throw new IllegalArgumentException("Completed sessions cannot be edited.");
        }

        if (dto.getStatus() != null)
        {
            throw new IllegalArgumentException("Use the complete endpoint to finish a session.");
        }

        if (dto.getDurationSeconds() != null)
        {
            if (dto.getDurationSeconds() < 0)
            {
                throw new IllegalArgumentException("durationSeconds cannot be negative.");
            }
            existingSession.setDurationSeconds(dto.getDurationSeconds());
        }

        if (dto.getEndPage() != null)
        {
            if (existingSession.getStartPage() != null && dto.getEndPage() < existingSession.getStartPage())
            {
                throw new IllegalArgumentException("endPage cannot be less than startPage.");
            }
            validatePageWithinBook(existingSession.getBook(), "endPage", dto.getEndPage());
            existingSession.setEndPage(dto.getEndPage());
        }

        if (dto.getReflection() != null) {
            existingSession.setReflection(dto.getReflection().trim());
        }

        updatePagesRead(existingSession);

        Session updatedSession = sessionRepository.save(existingSession);

        logger.info("Updated session {} with status {}", updatedSession.getId(), updatedSession.getStatus());

        return toSessionUpdateResponse(updatedSession, null);
    }

    @Override
    @Transactional
    public SessionUpdateResponseDTO completeSession(Long sessionId, SessionCompleteRequestDTO dto, String keycloakSub)
    {
        Session existingSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        if (!existingSession.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to complete session {} they do not own", existingSession.getUser().getId(), sessionId);
            throw new UnauthorizedActionException("This session does not belong to you.");
        }

        bookLifecycleValidator.assertAllowsSessionMutations(existingSession.getBook());

        if (existingSession.getStatus() == Session.SessionStatus.COMPLETED)
        {
            throw new IllegalArgumentException("Session is already completed.");
        }

        applyCompletionFields(existingSession, dto);
        validateSessionPages(existingSession.getBook(), existingSession.getStartPage(), existingSession.getEndPage());
        validateCompletionReady(existingSession);

        existingSession.setStatus(Session.SessionStatus.COMPLETED);
        existingSession.setCompletedAt(LocalDateTime.now());
        updatePagesRead(existingSession);

        Session updatedSession = sessionRepository.save(existingSession);

        bookService.syncBookProgress(updatedSession.getBook().getId());

        dailyActivityService.recordSessionCompletedActivity(updatedSession);
        XpUpdateResponseDTO xpUpdate = xpService.awardXp(updatedSession.getUser(), SESSION_COMPLETION_XP);
        currencyService.awardCoins(updatedSession.getUser(),
                CurrencyTransactionReason.SESSION_COMPLETED,
                "SESSION", updatedSession.getId());
        DailyQuestServiceInterface.DailyQuestEvaluationResult questResult =
                dailyQuestService.evaluateSessionCompletion(updatedSession);
        if (questResult != null && questResult.xpUpdate() != null)
        {
            xpUpdate = combineXpUpdates(xpUpdate, questResult.xpUpdate());
        }
        List<AchievementResponseDTO> newlyUnlockedAchievements =
                achievementService.evaluateAchievements(updatedSession.getUser());

        logger.info("Completed session {} for user {}", updatedSession.getId(), updatedSession.getUser().getId());

        return toSessionUpdateResponse(updatedSession, xpUpdate,
                questResult != null ? questResult.dailyQuest() : null,
                newlyUnlockedAchievements);
    }

    private void validateSessionPages(Book book, Integer startPage, Integer endPage)
    {
        validatePageWithinBook(book, "startPage", startPage);
        validatePageWithinBook(book, "endPage", endPage);

        if (startPage != null && endPage != null && endPage < startPage)
        {
            throw new IllegalArgumentException("endPage cannot be less than startPage.");
        }
    }

    private void validatePageWithinBook(Book book, String fieldName, Integer page)
    {
        if (page != null && book.getTotalPages() != null && page > book.getTotalPages())
        {
            throw new IllegalArgumentException(
                    fieldName + " cannot exceed the book's total pages (" + book.getTotalPages() + ").");
        }
    }

    @Override
    public SessionReadResponseDTO getSession(Long sessionId, String keycloakSub)
    {
        Session session = sessionRepository.findById(sessionId)
                .filter(foundSession -> foundSession.getUser().getKeycloakSub().equals(keycloakSub))
                .orElseThrow(()->new ResourceNotFoundException("Session", "id", sessionId));

        return toSessionReadResponse(session);
    }

    @Override
    public SessionReadResponseDTO getActiveSessionByBook(Long bookId, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Book book = bookRepository.findById(bookId).orElseThrow(()->new ResourceNotFoundException("Book", "id", bookId));

        if(!book.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to fetch the active session for book {} they do not own", user.getId(), bookId);
            throw new UnauthorizedActionException("This book does not belong to you.");
        }

        Session session = sessionRepository.findFirstByBookIdAndUser_IdAndStatusOrderBySessionDateDesc(
                        bookId,
                        user.getId(),
                        Session.SessionStatus.IN_PROGRESS
                )
                .orElse(null);

        return session == null ? null : toSessionReadResponse(session);
    }

    @Override
    public List<SessionReadResponseDTO> getSessionsByBook(Long bookId, String keycloakSub)
    {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new ResourceNotFoundException("Book", "id", bookId));

        if(!book.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to list sessions for book {} they do not own", book.getUser().getId(), bookId);
            throw new UnauthorizedActionException("This book does not belong to you.");
        }

        return sessionRepository.findByBookIdAndStatusOrderByCompletedAtDescSessionDateDesc(
                        bookId,
                        Session.SessionStatus.COMPLETED
                ).stream()
                .map(this::toSessionReadResponse)
                .collect(Collectors.toList());
    }

    private SessionCreateResponseDTO toSessionCreateResponse(Session session, XpUpdateResponseDTO xpUpdate)
    {
        return new SessionCreateResponseDTO(
                session.getId(),
                session.getBook().getId(),
                session.getDurationSeconds(),
                session.getStartPage(),
                session.getEndPage(),
                session.getPagesRead(),
                session.getReflection(),
                session.getStatus().name(),
                session.getSessionDate(),
                session.getCompletedAt(),
                xpUpdate
        );
    }

    private SessionReadResponseDTO toSessionReadResponse(Session session)
    {
        return new SessionReadResponseDTO(
                session.getId(),
                session.getBook().getId(),
                session.getDurationSeconds(),
                session.getStartPage(),
                session.getEndPage(),
                session.getPagesRead(),
                session.getReflection(),
                session.getStatus().name(),
                session.getSessionDate(),
                session.getCompletedAt()
        );
    }

    private SessionUpdateResponseDTO toSessionUpdateResponse(Session session, XpUpdateResponseDTO xpUpdate)
    {
        return toSessionUpdateResponse(session, xpUpdate, List.of());
    }

    private SessionUpdateResponseDTO toSessionUpdateResponse(Session session, XpUpdateResponseDTO xpUpdate,
                                                             List<AchievementResponseDTO> newlyUnlockedAchievements)
    {
        return toSessionUpdateResponse(session, xpUpdate, null, newlyUnlockedAchievements);
    }

    private SessionUpdateResponseDTO toSessionUpdateResponse(Session session, XpUpdateResponseDTO xpUpdate,
                                                             com.lexipal.core.dtos.responses.DailyQuestResponseDTO dailyQuest,
                                                             List<AchievementResponseDTO> newlyUnlockedAchievements)
    {
        return new SessionUpdateResponseDTO(
                session.getId(),
                session.getBook().getId(),
                session.getDurationSeconds(),
                session.getStartPage(),
                session.getEndPage(),
                session.getPagesRead(),
                session.getReflection(),
                session.getStatus().name(),
                session.getSessionDate(),
                session.getCompletedAt(),
                xpUpdate,
                dailyQuest,
                newlyUnlockedAchievements
        );
    }

    private XpUpdateResponseDTO combineXpUpdates(XpUpdateResponseDTO first, XpUpdateResponseDTO second)
    {
        if (first == null)
        {
            return second;
        }
        if (second == null)
        {
            return first;
        }

        return new XpUpdateResponseDTO(
                safeInt(first.getXpAwarded()) + safeInt(second.getXpAwarded()),
                first.getPreviousTotalXp(),
                second.getNewTotalXp(),
                second.getLevel(),
                second.getXpIntoLevel(),
                second.getXpPerLevel(),
                first.isLeveledUp() || second.isLeveledUp(),
                second.getMaxLevel(),
                second.getCurrentLevelMinXp(),
                second.getNextLevelMinXp(),
                second.getXpToNextLevel()
        );
    }

    private int safeInt(Integer value)
    {
        return value == null ? 0 : value;
    }

    private void applyCompletionFields(Session session, SessionCompleteRequestDTO dto)
    {
        if (dto.getDurationSeconds() != null)
        {
            session.setDurationSeconds(dto.getDurationSeconds());
        }

        if (dto.getEndPage() != null)
        {
            if (session.getStartPage() != null && dto.getEndPage() < session.getStartPage())
            {
                throw new IllegalArgumentException("endPage cannot be less than startPage.");
            }

            session.setEndPage(dto.getEndPage());
        }

        if (dto.getReflection() != null)
        {
            session.setReflection(dto.getReflection().trim());
        }
    }

    private void updatePagesRead(Session session)
    {
        Integer startPage = session.getStartPage();
        Integer endPage = session.getEndPage();

        if (startPage != null && endPage != null)
        {
            session.setPagesRead(Math.max(0, endPage - startPage));
        }
        else
        {
            session.setPagesRead(0);
        }
    }

    private void validateCompletionReady(Session session)
    {
        if (session.getEndPage() == null)
        {
            throw new IllegalArgumentException("endPage is required to complete a session.");
        }

        if (session.getDurationSeconds() == null || session.getDurationSeconds() <= 0)
        {
            throw new IllegalArgumentException("durationSeconds must be greater than zero to complete a session.");
        }

        if (session.getReflection() == null || session.getReflection().isBlank())
        {
            throw new IllegalArgumentException("reflection is required to complete a session.");
        }

        if (session.getEndPage() < session.getStartPage())
        {
            throw new IllegalArgumentException("endPage cannot be less than startPage.");
        }
    }
}
