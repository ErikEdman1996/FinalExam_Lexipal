package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.NoteCreateRequestDTO;
import com.lexipal.core.dtos.requests.NoteUpdateRequestDTO;
import com.lexipal.core.dtos.responses.NoteReadResponseDTO;
import com.lexipal.core.dtos.responses.NoteTagResponseDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.Note;
import com.lexipal.core.entities.NoteTag;
import com.lexipal.core.entities.NoteTagAssignment;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.models.NoteTagMatch;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.NoteRepository;
import com.lexipal.core.repositories.NoteTagRepository;
import com.lexipal.core.repositories.SessionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoteService implements NoteServiceInterface
{
    private static final Logger logger = LogManager.getLogger(NoteService.class);

    private final NoteRepository noteRepository;
    private final NoteTagRepository noteTagRepository;
    private final BookRepository bookRepository;
    private final SessionRepository sessionRepository;
    private final UserServiceInterface userService;
    private final BookLifecycleValidator bookLifecycleValidator;

    @Autowired
    public NoteService(final NoteRepository noteRepository,
                       final NoteTagRepository noteTagRepository,
                       final BookRepository bookRepository,
                       final SessionRepository sessionRepository,
                       final UserServiceInterface userService,
                       final BookLifecycleValidator bookLifecycleValidator)
    {
        this.noteRepository = noteRepository;
        this.noteTagRepository = noteTagRepository;
        this.bookRepository = bookRepository;
        this.sessionRepository = sessionRepository;
        this.userService = userService;
        this.bookLifecycleValidator = bookLifecycleValidator;
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

        bookLifecycleValidator.assertAllowsNoteMutations(book);

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
        replaceTags(note, resolveOwnedTags(dto.getTagIds(), user));

        Note saved = noteRepository.save(note);
        logger.info("Created note {} on book {} page {} for user {}", saved.getId(), book.getId(), saved.getPageNumber(), user.getId());

        return toReadResponse(saved);
    }

    @Override
    public List<NoteReadResponseDTO> getAllNotesForUser(String keycloakSub, List<Long> tagIds, NoteTagMatch tagMatch)
    {
        User user = userService.getRequiredUser(keycloakSub);

        List<Long> normalizedTagIds = normalizeAndValidateTagIds(tagIds, user);
        List<Note> notes;

        if (normalizedTagIds.isEmpty())
        {
            notes = noteRepository.findAllBySub(user.getKeycloakSub());
        }
        else if (effectiveTagMatch(tagMatch) == NoteTagMatch.ALL)
        {
            notes = noteRepository.findAllBySubAndAllTags(
                    user.getKeycloakSub(), normalizedTagIds, normalizedTagIds.size());
        }
        else
        {
            notes = noteRepository.findAllBySubAndAnyTag(user.getKeycloakSub(), normalizedTagIds);
        }

        return notes.stream()
                .map(this::toReadResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteReadResponseDTO> getAllNotesForBook(Long bookId, String keycloakSub, List<Long> tagIds,
                                                        NoteTagMatch tagMatch)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (book.getUser() == null || !book.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to list notes on book {} they do not own", user.getId(), bookId);
            throw new UnauthorizedActionException("This book does not belong to you.");
        }

        List<Long> normalizedTagIds = normalizeAndValidateTagIds(tagIds, user);
        List<Note> notes;

        if (normalizedTagIds.isEmpty())
        {
            notes = noteRepository.findAllByBook_IdAndUser_IdOrderByPageNumberAscCreatedAtAsc(bookId, user.getId());
        }
        else if (effectiveTagMatch(tagMatch) == NoteTagMatch.ALL)
        {
            notes = noteRepository.findAllByBookAndUserAndAllTags(
                    bookId, user.getId(), normalizedTagIds, normalizedTagIds.size());
        }
        else
        {
            notes = noteRepository.findAllByBookAndUserAndAnyTag(bookId, user.getId(), normalizedTagIds);
        }

        return notes.stream()
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

        bookLifecycleValidator.assertAllowsNoteMutations(note.getBook());

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

        if (dto.isTagIdsProvided())
        {
            replaceTags(note, resolveOwnedTags(dto.getTagIds(), user));
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

        bookLifecycleValidator.assertAllowsNoteMutations(note.getBook());

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
                note.getCreatedAt(),
                toTagResponses(note)
        );
    }

    private List<NoteTagResponseDTO> toTagResponses(Note note)
    {
        if (note.getTagAssignments() == null || note.getTagAssignments().isEmpty())
        {
            return new ArrayList<>();
        }

        return note.getTagAssignments().stream()
                .map(NoteTagAssignment::getTag)
                .sorted(Comparator.comparing(NoteTag::getNameNormalized))
                .map(tag -> new NoteTagResponseDTO(tag.getId(), tag.getName(), tag.getColorHex()))
                .collect(Collectors.toList());
    }

    private void replaceTags(Note note, List<NoteTag> tags)
    {
        note.getTagAssignments().clear();

        for (NoteTag tag : tags)
        {
            NoteTagAssignment assignment = new NoteTagAssignment(note, tag);
            note.getTagAssignments().add(assignment);
        }
    }

    private List<NoteTag> resolveOwnedTags(List<Long> tagIds, User user)
    {
        List<Long> normalizedTagIds = normalizeAndValidateTagIds(tagIds, user);
        if (normalizedTagIds.isEmpty())
        {
            return new ArrayList<>();
        }

        List<NoteTag> tags = noteTagRepository.findAllByIdInAndUser_Id(normalizedTagIds, user.getId());
        return normalizedTagIds.stream()
                .map(tagId -> tags.stream()
                        .filter(tag -> tag.getId().equals(tagId))
                        .findFirst()
                        .orElseThrow(() -> new UnauthorizedActionException("One or more tags do not belong to you.")))
                .collect(Collectors.toList());
    }

    private List<Long> normalizeAndValidateTagIds(List<Long> tagIds, User user)
    {
        if (tagIds == null || tagIds.isEmpty())
        {
            return new ArrayList<>();
        }

        Set<Long> uniqueIds = new LinkedHashSet<>();
        for (Long tagId : tagIds)
        {
            if (tagId == null)
            {
                throw new IllegalArgumentException("Tag id cannot be null.");
            }
            uniqueIds.add(tagId);
        }

        List<Long> normalizedTagIds = new ArrayList<>(uniqueIds);
        List<NoteTag> ownedTags = noteTagRepository.findAllByIdInAndUser_Id(normalizedTagIds, user.getId());
        if (ownedTags.size() != normalizedTagIds.size())
        {
            throw new UnauthorizedActionException("One or more tags do not belong to you.");
        }

        return normalizedTagIds;
    }

    private NoteTagMatch effectiveTagMatch(NoteTagMatch tagMatch)
    {
        return tagMatch == null ? NoteTagMatch.ANY : tagMatch;
    }
}
