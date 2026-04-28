package com.lexipal.core.services;
import com.lexipal.core.dtos.responses.BookCreateResponseDTO;
import com.lexipal.core.dtos.responses.BookReadResponseDTO;
import com.lexipal.core.dtos.responses.BookUpdateResponseDTO;
import com.lexipal.core.dtos.requests.BookUpdateRequestDTO;
import com.lexipal.core.dtos.requests.BookCreateRequestDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.models.BookCoverMatch;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.SessionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService implements BookServiceInterface
{
    private static final Logger logger = LogManager.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final SessionRepository sessionRepository;
    private final UserServiceInterface userService;
    private final BookLifecycleValidator bookLifecycleValidator;
    private final ReadingListServiceInterface readingListService;
    private final BookCoverLookupService bookCoverLookupService;

    @Autowired
    public BookService(final BookRepository bookRepository,
                       final SessionRepository sessionRepository,
                       final UserServiceInterface userService,
                       final BookLifecycleValidator bookLifecycleValidator,
                       @Lazy final ReadingListServiceInterface readingListService,
                       final BookCoverLookupService bookCoverLookupService)
    {
        this.bookRepository = bookRepository;
        this.sessionRepository = sessionRepository;
        this.userService = userService;
        this.bookLifecycleValidator = bookLifecycleValidator;
        this.readingListService = readingListService;
        this.bookCoverLookupService = bookCoverLookupService;
    }

    @Override
    @Transactional
    public BookCreateResponseDTO addBook(BookCreateRequestDTO request, String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);

        Book newBook = new Book();

        newBook.setTitle(request.getTitle().trim());
        newBook.setAuthor(request.getAuthor().trim());
        newBook.setTotalPages(request.getTotalPages());
        newBook.setUser(user);
        applyBestCoverMatch(newBook);

        user.getBooks().add(newBook);

        Book savedBook = bookRepository.save(newBook);
        logger.info("Created book {} for user {}", savedBook.getId(), user.getId());

        return toBookCreateResponse(savedBook);
    }

    @Override
    @Transactional
    public BookUpdateResponseDTO updateBook(Long bookId, BookUpdateRequestDTO dto, String keycloakSub)
    {
        Book existingBook = bookRepository.findById(bookId).orElseThrow(()->
        {
            logger.warn("Book {} was not found during update", bookId);
            return new ResourceNotFoundException("Book", "id", bookId);
        });


        if(!existingBook.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to update book {} they do not own", existingBook.getUser().getId(), bookId);
            throw new UnauthorizedActionException("You do not own this book");
        }

        BookLifecycleValidator.BookUpdateResolution resolution =
                bookLifecycleValidator.resolveUpdate(existingBook, dto);
        boolean titleOrAuthorChanged = titleOrAuthorChanged(existingBook, resolution);

        Integer furthestCompletedPage = sessionRepository.findMaxEndPageByBookIdAndStatus(
                bookId,
                com.lexipal.core.entities.Session.SessionStatus.COMPLETED
        );
        if (furthestCompletedPage != null && furthestCompletedPage > resolution.totalPages())
        {
            throw new IllegalArgumentException(
                    "totalPages cannot be less than completed session progress (" + furthestCompletedPage + ").");
        }

        existingBook.setTitle(resolution.title());
        existingBook.setAuthor(resolution.author());
        existingBook.setReadPages(resolution.readPages());
        existingBook.setTotalPages(resolution.totalPages());
        applyStatusWithCompletionTimestamp(existingBook, resolution.status(), null);
        if (titleOrAuthorChanged && hasAutoResolvedCover(existingBook))
        {
            clearCoverMetadata(existingBook);
            applyBestCoverMatch(existingBook);
        }

        Book updatedBook = bookRepository.save(existingBook);
        logger.info("Updated book {} for user {}", bookId, updatedBook.getUser().getId());
        return toBookUpdateResponse(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId, String keycloakSub)
    {
        Book existingBook = bookRepository.findById(bookId).orElseThrow(()->
        {
            logger.warn("Book {} was not found during delete", bookId);
            return new ResourceNotFoundException("Book", "id", bookId);
        });

        if(!existingBook.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to delete book {} they do not own", existingBook.getUser().getId(), bookId);
            throw new UnauthorizedActionException("You do not own this book");
        }

        existingBook.setStatus(Book.BookStatus.DELETED);
        existingBook.setCompletedAt(null);
        bookRepository.save(existingBook);
        logger.info("Marked book {} as deleted", bookId);
    }

    @Override
    public BookReadResponseDTO getBookById(Long bookId, String keycloakSub)
    {
        Book book = bookRepository.findById(bookId)
                .filter(foundBook -> foundBook.getUser().getKeycloakSub().equals(keycloakSub))
                .filter(foundBook -> foundBook.getStatus() != Book.BookStatus.DELETED)
                .orElseThrow(()->
                {
                    logger.warn("Book {} was not found or was inaccessible", bookId);
                    return new ResourceNotFoundException("Book", "id", bookId);
                });

        return toBookReadResponse(book);
    }

    @Override
    public List<BookReadResponseDTO> getAllBooksByUserSub(String keycloakSub)
    {
        userService.getOrCreateUser(keycloakSub);

        List<Book> activeBooks = bookRepository.findAllActiveBooks(keycloakSub);

        return activeBooks.stream()
                .map(this::toBookReadResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void syncBookProgress(Long bookId)
    {
        Book book = bookRepository.findById(bookId).orElseThrow(()->
        {
            logger.warn("Book {} was not found while syncing progress", bookId);
            return new ResourceNotFoundException("Book", "id", bookId);
        });

        Integer furthestPage = sessionRepository.findMaxEndPageByBookIdAndStatus(
                bookId,
                com.lexipal.core.entities.Session.SessionStatus.COMPLETED
        );

        int syncedReadPages = furthestPage == null ? 0 : furthestPage;
        bookLifecycleValidator.assertReadPagesWithinTotalPages(syncedReadPages, book.getTotalPages());
        book.setReadPages(syncedReadPages);

        if (book.getStatus() == Book.BookStatus.DELETED || book.getStatus() == Book.BookStatus.ARCHIVED) {
            bookRepository.save(book);
            logger.info("Synced read progress for book {} without changing status because it is {}", bookId, book.getStatus());
            return;
        }

        Book.BookStatus previousStatus = book.getStatus();

        // Auto-complete logic
        if (book.getReadPages() >= book.getTotalPages()) {
            LocalDateTime completedAt = sessionRepository.findLatestCompletedAtByBookId(bookId);
            applyStatusWithCompletionTimestamp(book, Book.BookStatus.COMPLETE, completedAt);
        } else {
            applyStatusWithCompletionTimestamp(book, Book.BookStatus.ACTIVE, null);
        }

        Book savedBook = bookRepository.save(book);
        logger.info("Synced book {} progress to {} pages with status {}", bookId, book.getReadPages(), book.getStatus());

        if (previousStatus != Book.BookStatus.COMPLETE && savedBook.getStatus() == Book.BookStatus.COMPLETE)
        {
            readingListService.onBookCompleted(savedBook);
        }
    }

    @Override
    public double calculateBookProgress(Book book)
    {
        if (book.getTotalPages() == 0) return 0;
        return (double) book.getReadPages() / book.getTotalPages() * 100;
    }

    private BookCreateResponseDTO toBookCreateResponse(Book book)
    {
        return new BookCreateResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getReadPages(),
                book.getTotalPages(),
                calculateBookProgress(book),
                book.getStatus().name(),
                getCompletedSessionCount(book),
                book.getCreatedAt(),
                book.getCoverImageUrl(),
                book.getCoverSource(),
                book.getExternalBookId()
        );
    }

    private BookReadResponseDTO toBookReadResponse(Book book)
    {
        return new BookReadResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getReadPages(),
                book.getTotalPages(),
                calculateBookProgress(book),
                book.getStatus().name(),
                getCompletedSessionCount(book),
                book.getCreatedAt(),
                book.getCoverImageUrl(),
                book.getCoverSource(),
                book.getExternalBookId()
        );
    }

    private BookUpdateResponseDTO toBookUpdateResponse(Book book)
    {
        return new BookUpdateResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getReadPages(),
                book.getTotalPages(),
                calculateBookProgress(book),
                book.getStatus().name(),
                getCompletedSessionCount(book),
                book.getCreatedAt(),
                book.getCoverImageUrl(),
                book.getCoverSource(),
                book.getExternalBookId()
        );
    }

    private int getCompletedSessionCount(Book book)
    {
        return Math.toIntExact(sessionRepository.countByBookIdAndStatus(
                book.getId(),
                com.lexipal.core.entities.Session.SessionStatus.COMPLETED
        ));
    }

    private void applyStatusWithCompletionTimestamp(Book book, Book.BookStatus nextStatus, LocalDateTime completedAt)
    {
        Book.BookStatus previousStatus = book.getStatus();
        book.setStatus(nextStatus);

        if (nextStatus == Book.BookStatus.COMPLETE)
        {
            if (book.getCompletedAt() == null || previousStatus != Book.BookStatus.COMPLETE)
            {
                book.setCompletedAt(completedAt == null ? LocalDateTime.now() : completedAt);
            }
            return;
        }

        if (previousStatus == Book.BookStatus.COMPLETE || book.getCompletedAt() != null)
        {
            book.setCompletedAt(null);
        }
    }

    private void applyBestCoverMatch(Book book)
    {
        bookCoverLookupService.findBestCover(book.getTitle(), book.getAuthor())
                .ifPresent(match -> applyCoverMatch(book, match));
    }

    private void applyCoverMatch(Book book, BookCoverMatch match)
    {
        book.setCoverImageUrl(match.coverImageUrl());
        book.setCoverSource(match.coverSource());
        book.setExternalBookId(match.externalBookId());
    }

    private boolean titleOrAuthorChanged(Book existingBook, BookLifecycleValidator.BookUpdateResolution resolution)
    {
        return !existingBook.getTitle().equals(resolution.title())
                || !existingBook.getAuthor().equals(resolution.author());
    }

    private boolean hasAutoResolvedCover(Book book)
    {
        return book.getCoverSource() == null
                || BookCoverLookupService.SOURCE_OPEN_LIBRARY.equals(book.getCoverSource())
                || BookCoverLookupService.SOURCE_GOOGLE_BOOKS.equals(book.getCoverSource());
    }

    private void clearCoverMetadata(Book book)
    {
        book.setCoverImageUrl(null);
        book.setCoverSource(null);
        book.setExternalBookId(null);
    }
}
