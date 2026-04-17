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
public class BookService implements BookServiceInterface
{
    private static final Logger logger = LogManager.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final SessionRepository sessionRepository;
    private final UserServiceInterface userService;

    @Autowired
    public BookService(final BookRepository bookRepository,
                       final SessionRepository sessionRepository,
                       final UserServiceInterface userService)
    {
        this.bookRepository = bookRepository;
        this.sessionRepository = sessionRepository;
        this.userService = userService;
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

        if(dto.getTitle() != null) existingBook.setTitle(dto.getTitle().trim());
        if(dto.getAuthor() != null) existingBook.setAuthor(dto.getAuthor().trim());
        if(dto.getStatus() != null) existingBook.setStatus(dto.getStatus());
        if(dto.getReadPages() != null) existingBook.setReadPages(dto.getReadPages());
        if(dto.getTotalPages() != null) existingBook.setTotalPages(dto.getTotalPages());

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

        if (furthestPage == null) {
            book.setReadPages(0);
        } else {
            book.setReadPages(furthestPage);
        }

        if (book.getStatus() == Book.BookStatus.DELETED || book.getStatus() == Book.BookStatus.ARCHIVED) {
            bookRepository.save(book);
            logger.info("Synced read progress for book {} without changing status because it is {}", bookId, book.getStatus());
            return;
        }

        // Auto-complete logic
        if (book.getReadPages() >= book.getTotalPages()) {
            book.setStatus(Book.BookStatus.COMPLETE);
        } else {
            book.setStatus(Book.BookStatus.ACTIVE);
        }

        bookRepository.save(book);
        logger.info("Synced book {} progress to {} pages with status {}", bookId, book.getReadPages(), book.getStatus());
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
                book.getCreatedAt()
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
                book.getCreatedAt()
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
                book.getCreatedAt()
        );
    }

    private int getCompletedSessionCount(Book book)
    {
        return Math.toIntExact(sessionRepository.countByBookIdAndStatus(
                book.getId(),
                com.lexipal.core.entities.Session.SessionStatus.COMPLETED
        ));
    }
}
