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
import com.lexipal.core.repositories.UserRepository;
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
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    private static final Logger bookLogger = LogManager.getLogger("BookLogger");
    private static final Logger userLogger = LogManager.getLogger("UserLogger");

    @Autowired
    public BookService(final BookRepository bookRepository,
                       final UserRepository userRepository,
                       final SessionRepository sessionRepository)
    {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public BookCreateResponseDTO addBook(BookCreateRequestDTO request, String keycloakSub)
    {
        userLogger.info("Retrieving user...");
        User user = userRepository.findByKeycloakSub(keycloakSub)
                .orElseGet(() -> {
                    userLogger.info("User not found, creating new user...");
                    User newUser = new User();
                    newUser.setKeycloakSub(keycloakSub);
                    User savedUser = userRepository.save(newUser);
                    userLogger.info("Created new user with id: {}", savedUser.getId());
                    return savedUser;
                });

        bookLogger.info("Creating a new book for user: {}", user.getId());
        Book newBook = new Book();

        newBook.setTitle(request.getTitle().trim());
        newBook.setAuthor(request.getAuthor().trim());
        newBook.setTotalPages(request.getTotalPages());
        newBook.setUser(user);

        user.getBooks().add(newBook);

        Book savedBook = bookRepository.save(newBook);
        bookLogger.info("Created new book with id: {} for user: {}", savedBook.getId(), user.getId());

        return toBookCreateResponse(savedBook);
    }

    @Override
    @Transactional
    public BookUpdateResponseDTO updateBook(Long bookId, BookUpdateRequestDTO dto, String keycloakSub)
    {
        bookLogger.info("Retrieving book with id: {}", bookId);
        Book existingBook = bookRepository.findById(bookId).orElseThrow(()->
        {
            bookLogger.error("Book not found with id: {}", bookId);
            return new ResourceNotFoundException("Book", "id", bookId);
        });


        if(!existingBook.getUser().getKeycloakSub().equals(keycloakSub))
        {
            bookLogger.error("Current user does not own book with id: {}", bookId);
            throw new UnauthorizedActionException("You do not own this book");
        }

        bookLogger.info("Updating book with id: {}", bookId);
        if(dto.getTitle() != null) existingBook.setTitle(dto.getTitle().trim());
        if(dto.getAuthor() != null) existingBook.setAuthor(dto.getAuthor().trim());
        if(dto.getStatus() != null) existingBook.setStatus(dto.getStatus());
        if(dto.getReadPages() != null) existingBook.setReadPages(dto.getReadPages());
        if(dto.getTotalPages() != null) existingBook.setTotalPages(dto.getTotalPages());

        bookLogger.info("Book updated with id: {}", bookId);
        return toBookUpdateResponse(bookRepository.save(existingBook));
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId, String keycloakSub)
    {
        bookLogger.info("Retrieving book with id: {}", bookId);
        Book existingBook = bookRepository.findById(bookId).orElseThrow(()->
        {
            bookLogger.error("Book not found with id: {}", bookId);
            return new ResourceNotFoundException("Book", "id", bookId);
        });

        if(!existingBook.getUser().getKeycloakSub().equals(keycloakSub))
        {
            bookLogger.error("Current user does not own book with id: {}", bookId);
            throw new UnauthorizedActionException("You do not own this book");
        }

        bookLogger.info("Changing status to DELETED for book with id: {}", bookId);
        existingBook.setStatus(Book.BookStatus.DELETED);

        bookLogger.info("Book with id: {} marked as DELETED", bookId);
        bookRepository.save(existingBook);
    }

    @Override
    public BookReadResponseDTO getBookById(Long bookId, String keycloakSub)
    {
        bookLogger.info("Retrieving book with id: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .filter(foundBook -> foundBook.getUser().getKeycloakSub().equals(keycloakSub))
                .filter(foundBook -> foundBook.getStatus() != Book.BookStatus.DELETED)
                .orElseThrow(()->
                {
                    bookLogger.error("Book not found with id: {}", bookId);
                    return new ResourceNotFoundException("Book", "id", bookId);
                });

        return toBookReadResponse(book);
    }

    @Override
    public List<BookReadResponseDTO> getAllBooksByUserSub(String keycloakSub)
    {
        userLogger.info("Retrieving user...");
        User user = userRepository.findByKeycloakSub(keycloakSub)
                .orElseGet(() -> {
                    userLogger.info("User not found, creating new user...");
                    User newUser = new User();
                    newUser.setKeycloakSub(keycloakSub);
                    User savedUser = userRepository.save(newUser);
                    userLogger.info("Created new user with id: {}", savedUser.getId());
                    return savedUser;
                });

        bookLogger.info("Retrieving all active books for user with id: {}", user.getId());

        List<Book> activeBooks = bookRepository.findAllActiveBooks(keycloakSub);

        return activeBooks.stream()
                .map(this::toBookReadResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void syncBookProgress(Long bookId)
    {
        bookLogger.info("Retrieving book with id: {}", bookId);
        Book book = bookRepository.findById(bookId).orElseThrow(()->
        {
            bookLogger.error("Book not found with id: {}", bookId);
            return new ResourceNotFoundException("Book", "id", bookId);
        });

        // Find the highest endPage across all sessions
        Integer furthestPage = sessionRepository.findMaxEndPageByBookId(bookId);

        if (furthestPage == null) {
            book.setReadPages(0);
        } else {
            book.setReadPages(furthestPage);
        }

        if (book.getStatus() == Book.BookStatus.DELETED || book.getStatus() == Book.BookStatus.ARCHIVED) {
            bookRepository.save(book);
            return;
        }

        // Auto-complete logic
        if (book.getReadPages() >= book.getTotalPages()) {
            book.setStatus(Book.BookStatus.COMPLETE);
        } else {
            book.setStatus(Book.BookStatus.ACTIVE);
        }

        bookRepository.save(book);
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
                book.getSessions().size(),
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
                book.getSessions().size(),
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
                book.getSessions().size(),
                book.getCreatedAt()
        );
    }
}
