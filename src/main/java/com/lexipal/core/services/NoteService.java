package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.NoteCreateRequestDTO;
import com.lexipal.core.dtos.requests.NoteUpdateRequestDTO;
import com.lexipal.core.dtos.responses.NoteReadResponseDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.Note;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.NoteRepository;
import com.lexipal.core.repositories.SessionRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService implements NoteServiceInterface
{
    private static final Logger logger = LogManager.getLogger(NoteService.class);

    private final NoteRepository noteRepository;
    private final BookRepository bookRepository;
    private final SessionRepository sessionRepository;
    private final UserServiceInterface userService;

    @Autowired
    public NoteService(final NoteRepository noteRepository,
                       final BookRepository bookRepository,
                       final SessionRepository sessionRepository,
                       final UserServiceInterface userService)
    {
        this.noteRepository = noteRepository;
        this.bookRepository = bookRepository;
        this.sessionRepository = sessionRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public NoteReadResponseDTO createNote(NoteCreateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> {
                    logger.warn("Book {} not found while creating note for user {}", dto.getBookId(), user.getId());
                    return new ResourceNotFoundException("Book", "id", dto.getBookId());
                });

        if (book.getUser() == null || !book.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to create a note on book {} they do not own", user.getId(), book.getId());
            throw new UnauthorizedActionException("This book does not belong to you.");
        }

        if (book.getTotalPages() != null && dto.getPageNumber() > book.getTotalPages())
        {
            throw new IllegalArgumentException("Page number " + dto.getPageNumber() +
                    " exceeds the book's total pages (" + book.getTotalPages() + ").");
        }

        Long sourceSessionId = null;

        if (dto.getSessionId() != null)
        {
            Session session = sessionRepository.findById(dto.getSessionId())
                    .orElseThrow(() -> {
                        logger.warn("Session {} not found while creating note for user {}", dto.getSessionId(), user.getId());
                        return new ResourceNotFoundException("Session", "id", dto.getSessionId());
                    });

            if (!session.getUser().getKeycloakSub().equals(keycloakSub))
            {
                logger.warn("User {} attempted to attach note to session {} they do not own", user.getId(), session.getId());
                throw new UnauthorizedActionException("This session does not belong to you.");
            }

            if (!session.getBook().getId().equals(book.getId()))
            {
                throw new IllegalArgumentException("Session " + session.getId() +
                        " does not belong to book " + book.getId() + ".");
            }

            sourceSessionId = session.getId();
        }

        Note note = new Note();
        note.setUser(user);
        note.setBook(book);
        note.setSourceSessionId(sourceSessionId);
        note.setQuotedText(dto.getQuotedText());
        note.setPageNumber(dto.getPageNumber());
        note.setChapterLabel(dto.getChapterLabel());

        Note saved = noteRepository.save(note);
        logger.info("Created note {} on book {} page {} for user {}", saved.getId(), book.getId(), saved.getPageNumber(), user.getId());

        return toReadResponse(saved);
    }

    @Override
    public List<NoteReadResponseDTO> getAllNotesForUser(String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        return noteRepository.findAllBySub(user.getKeycloakSub()).stream()
                .map(this::toReadResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteReadResponseDTO> getAllNotesForBook(Long bookId, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (book.getUser() == null || !book.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to list notes on book {} they do not own", user.getId(), bookId);
            throw new UnauthorizedActionException("This book does not belong to you.");
        }

        return noteRepository.findAllByBook_IdAndUser_IdOrderByPageNumberAscCreatedAtAsc(bookId, user.getId()).stream()
                .map(this::toReadResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteReadResponseDTO updateNote(Long noteId, NoteUpdateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        if (!note.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to update note {} they do not own", user.getId(), noteId);
            throw new UnauthorizedActionException("This note does not belong to you.");
        }

        if (dto.getQuotedText() != null)
        {
            if (dto.getQuotedText().isBlank())
            {
                throw new IllegalArgumentException("Quoted text cannot be blank.");
            }
            note.setQuotedText(dto.getQuotedText());
        }

        if (dto.getPageNumber() != null)
        {
            Integer totalPages = note.getBook().getTotalPages();
            if (totalPages != null && dto.getPageNumber() > totalPages)
            {
                throw new IllegalArgumentException("Page number " + dto.getPageNumber() +
                        " exceeds the book's total pages (" + totalPages + ").");
            }
            note.setPageNumber(dto.getPageNumber());
        }

        if (dto.getChapterLabel() != null)
        {
            note.setChapterLabel(dto.getChapterLabel().isBlank() ? null : dto.getChapterLabel());
        }

        Note saved = noteRepository.save(note);
        logger.info("Updated note {} for user {}", saved.getId(), user.getId());

        return toReadResponse(saved);
    }

    @Override
    @Transactional
    public void deleteNote(Long noteId, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        if (!note.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to delete note {} they do not own", user.getId(), noteId);
            throw new UnauthorizedActionException("This note does not belong to you.");
        }

        noteRepository.delete(note);
        logger.info("Deleted note {} for user {}", noteId, user.getId());
    }

    private NoteReadResponseDTO toReadResponse(Note note)
    {
        return new NoteReadResponseDTO(
                note.getId(),
                note.getBook().getId(),
                note.getSourceSessionId(),
                note.getPageNumber(),
                note.getChapterLabel(),
                note.getQuotedText(),
                note.getCreatedAt()
        );
    }
}
