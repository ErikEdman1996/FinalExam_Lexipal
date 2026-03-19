package com.lexipal.core.services;
import com.lexipal.core.dtos.BookUpdateDTO;
import com.lexipal.core.entities.Book;
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
import java.util.Optional;

@Service
public class BookService implements BookServiceInterface
{
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

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
    public Book addBook(Book book, String keycloakSub)
    {
        User user = userRepository.findByKeycloakSub(keycloakSub)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setKeycloakSub(keycloakSub);
                    return userRepository.save(newUser);
                });

        book.setUser(user);

        user.getBooks().add(book);

        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public Book updateBook(Long bookId, BookUpdateDTO dto, String keycloakSub)
    {
        Book existingBook = bookRepository.findById(bookId).orElseThrow(()->new ResourceNotFoundException("Book", "id", bookId));

        if(!existingBook.getUser().getKeycloakSub().equals(keycloakSub))
        {
            throw new UnauthorizedActionException("You do not own this book");
        }

        if(dto.getTitle() != null) existingBook.setTitle(dto.getTitle());
        if(dto.getAuthor() != null) existingBook.setAuthor(dto.getAuthor());
        if(dto.getStatus() != null) existingBook.setStatus(dto.getStatus());
        if(dto.getReadPages() != null) existingBook.setReadPages(dto.getReadPages());
        if(dto.getTotalPages() != null) existingBook.setTotalPages(dto.getTotalPages());

        return bookRepository.save(existingBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId, String keycloakSub)
    {
        Book existingBook = bookRepository.findById(bookId).orElseThrow(()->new ResourceNotFoundException("Book", "id", bookId));

        if(!existingBook.getUser().getKeycloakSub().equals(keycloakSub))
        {
            throw new UnauthorizedActionException("You do not own this book");
        }

        existingBook.setStatus(Book.BookStatus.DELETED);

        bookRepository.save(existingBook);
    }

    @Override
    public Book getBookById(Long bookId, String keycloakSub)
    {
        return bookRepository.findById(bookId)
                .filter(book -> book.getUser().getKeycloakSub().equals(keycloakSub))
                .filter(book -> book.getStatus() != Book.BookStatus.DELETED)
                .orElseThrow(()->new ResourceNotFoundException("Book", "id", bookId));
    }

    @Override
    public List<Book> getAllBooksByUserId(String keycloakSub)
    {
        return bookRepository.findAllActiveBooks(keycloakSub);
    }

    @Override
    public void syncBookProgress(Long bookId)
    {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        // Find the highest endPage across all sessions
        // You can add this method to your SessionRepository
        Integer furthestPage = sessionRepository.findMaxEndPageByBookId(bookId);

        if (furthestPage == null) {
            book.setReadPages(0);
        } else {
            book.setReadPages(furthestPage);
        }

        // Auto-complete logic
        if (book.getReadPages() >= book.getTotalPages()) {
            book.setStatus(Book.BookStatus.COMPLETE);
        } else {
            book.setStatus(Book.BookStatus.ACTIVE);
        }

        bookRepository.save(book);
    }
}
