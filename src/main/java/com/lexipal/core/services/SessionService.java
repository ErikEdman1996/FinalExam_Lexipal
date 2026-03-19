package com.lexipal.core.services;

import com.lexipal.core.dtos.SessionRequestDTO;
import com.lexipal.core.dtos.SessionUpdateDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService implements SessionServiceInterface
{

    final SessionRepository sessionRepository;
    final UserRepository userRepository;
    final BookRepository bookRepository;
    private final BookService bookService;

    @Autowired
    public SessionService(final SessionRepository sessionRepository,
                          final UserRepository userRepository,
                          final BookRepository bookRepository,
                          final BookService bookService)
    {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    @Override
    @Transactional
    public Session addSession(SessionRequestDTO dto, String keycloakSub)
    {
        User user = userRepository.findByKeycloakSub(keycloakSub).orElseThrow(()->new RuntimeException("User not found"));

        Long bookId = dto.getBookId();

        Book book = bookRepository.findById(bookId).orElseThrow(()->new ResourceNotFoundException("Book", "id", bookId));

        if(!book.getUser().getId().equals(user.getId()))
        {
            throw new UnauthorizedActionException("This book does belong to you.");
        }

        Session session = new Session();
        session.setUser(user);
        session.setBook(book);
        session.setStartPage(dto.getStartPage());
        session.setEndPage(dto.getEndPage());
        session.setDurationSeconds(dto.getDurationSeconds());
        session.setReflection(dto.getReflection());

        Session savedSession = sessionRepository.save(session);

        bookService.syncBookProgress(book.getId());

        return savedSession;
    }

    @Override
    @Transactional
    public Session updateSession(Long sessionId, SessionUpdateDTO dto, String keycloakSub)
    {
        Session existingSession = sessionRepository.findById(sessionId).orElseThrow(()->new ResourceNotFoundException("Session", "id", sessionId));

        if(!existingSession.getUser().getKeycloakSub().equals(keycloakSub))
        {
            throw new UnauthorizedActionException("This session does not belong to you.");
        }

        if(dto.getEndPage() != null) existingSession.setEndPage(dto.getEndPage());
        if(dto.getDurationSeconds() != null) existingSession.setDurationSeconds(dto.getDurationSeconds());
        if(dto.getReflection() != null) existingSession.setReflection(dto.getReflection());

        existingSession.setPagesRead(Math.max(0, dto.getEndPage() - existingSession.getStartPage()));

        Session updatedSession = sessionRepository.save(existingSession);

        bookService.syncBookProgress(updatedSession.getBook().getId());

        return updatedSession;
    }

    @Override
    public Session getSession(Long sessionId, String keycloakSub)
    {
        return sessionRepository.findById(sessionId)
                .filter(session -> session.getUser().getKeycloakSub().equals(keycloakSub))
                .orElseThrow(()->new ResourceNotFoundException("Session", "id", sessionId));
    }

    @Override
    public List<Session> getSessionsByBook(Long bookId, String keycloakSub)
    {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new ResourceNotFoundException("Book", "id", bookId));

        if(!book.getUser().getKeycloakSub().equals(keycloakSub))
        {
            throw new UnauthorizedActionException("This book does not belong to you.");
        }

        return sessionRepository.findByBookIdOrderBySessionDateDesc(bookId);
    }
}
