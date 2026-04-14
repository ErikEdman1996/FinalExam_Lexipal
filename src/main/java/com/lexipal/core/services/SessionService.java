package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.SessionCreateRequestDTO;
import com.lexipal.core.dtos.requests.SessionUpdateRequestDTO;
import com.lexipal.core.dtos.responses.SessionCreateResponseDTO;
import com.lexipal.core.dtos.responses.SessionReadResponseDTO;
import com.lexipal.core.dtos.responses.SessionUpdateResponseDTO;
import com.lexipal.core.dtos.responses.XpUpdateResponseDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.SessionRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService implements SessionServiceInterface
{
    private static final Logger logger = LogManager.getLogger(SessionService.class);

    final SessionRepository sessionRepository;
    final BookRepository bookRepository;
    private final BookServiceInterface bookService;
    private final DailyActivityServiceInterface dailyActivityService;
    private final UserServiceInterface userService;
    private final XpService xpService;

    @Autowired
    public SessionService(final SessionRepository sessionRepository,
                          final BookRepository bookRepository,
                          final BookServiceInterface bookService,
                          final DailyActivityServiceInterface dailyActivityService,
                          final UserServiceInterface userService,
                          final XpService xpService)
    {
        this.sessionRepository = sessionRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.dailyActivityService = dailyActivityService;
        this.userService = userService;
        this.xpService = xpService;
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

        validateBookCanAcceptSessionChanges(book);

        Session session = new Session();
        session.setUser(user);
        session.setBook(book);
        session.setStartPage(dto.getStartPage());
        session.setEndPage(dto.getEndPage());
        session.setDurationSeconds(dto.getDurationSeconds());
        session.setReflection(dto.getReflection() == null ? null : dto.getReflection().trim());

        Session savedSession = sessionRepository.save(session);

        bookService.syncBookProgress(book.getId());
        logger.info("Created session {} for book {} and user {}", savedSession.getId(), book.getId(), user.getId());

        return toSessionCreateResponse(savedSession, null);
    }

    @Override
    @Transactional
    public SessionUpdateResponseDTO updateSession(Long sessionId, SessionUpdateRequestDTO dto, String keycloakSub)
    {
        Session existingSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        boolean wasCompleted = existingSession.getStatus() == Session.SessionStatus.COMPLETED;

        if (!existingSession.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to update session {} they do not own", existingSession.getUser().getId(), sessionId);
            throw new UnauthorizedActionException("This session does not belong to you.");
        }

        validateBookCanAcceptSessionChanges(existingSession.getBook());

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
            existingSession.setEndPage(dto.getEndPage());
        }

        if (dto.getReflection() != null) {
            existingSession.setReflection(dto.getReflection().trim());
        }

        if (dto.getStatus() != null && dto.getStatus() == Session.SessionStatus.COMPLETED)
        {
            existingSession.setStatus(dto.getStatus());
        }

        Integer startPage = existingSession.getStartPage();
        Integer endPage = existingSession.getEndPage();

        if (startPage != null && endPage != null)
        {
            existingSession.setPagesRead(Math.max(0, endPage - startPage));
        }
        else
        {
            existingSession.setPagesRead(0);
        }

        // Optional guard: auto-complete if all completion fields exist
        if (existingSession.getEndPage() != null
                && existingSession.getDurationSeconds() != null
                && existingSession.getDurationSeconds() > 0
                && existingSession.getReflection() != null
                && !existingSession.getReflection().isBlank())
        {
            existingSession.setStatus(Session.SessionStatus.COMPLETED);
        }

        Session updatedSession = sessionRepository.save(existingSession);

        bookService.syncBookProgress(updatedSession.getBook().getId());

        XpUpdateResponseDTO xpUpdate = null;
        if (!wasCompleted && updatedSession.getStatus() == Session.SessionStatus.COMPLETED)
        {
            dailyActivityService.recordSessionCompletedActivity(updatedSession);
            xpUpdate = xpService.awardXp(updatedSession.getUser(), 25);
        }

        logger.info("Updated session {} with status {}", updatedSession.getId(), updatedSession.getStatus());

        return toSessionUpdateResponse(updatedSession, xpUpdate);
    }

    private void validateBookCanAcceptSessionChanges(Book book)
    {
        if (book.getStatus() == Book.BookStatus.DELETED || book.getStatus() == Book.BookStatus.ARCHIVED)
        {
            throw new IllegalArgumentException("Sessions cannot be modified for archived or deleted books.");
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
    public List<SessionReadResponseDTO> getSessionsByBook(Long bookId, String keycloakSub)
    {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new ResourceNotFoundException("Book", "id", bookId));

        if(!book.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to list sessions for book {} they do not own", book.getUser().getId(), bookId);
            throw new UnauthorizedActionException("This book does not belong to you.");
        }

        return sessionRepository.findByBookIdOrderBySessionDateDesc(bookId).stream()
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
                session.getCapturedWordCount(),
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
                session.getCapturedWordCount()
        );
    }

    private SessionUpdateResponseDTO toSessionUpdateResponse(Session session, XpUpdateResponseDTO xpUpdate)
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
                session.getCapturedWordCount(),
                xpUpdate
        );
    }
}
