package com.lexipal.core.services;

import com.lexipal.core.dtos.BookUpdateDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookService implements BookServiceInterface
{
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookService(final BookRepository bookRepository, final UserRepository userRepository)
    {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
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
    public void deleteBook(Long bookId, String keycloakSub)
    {

    }

    @Override
    public Optional<Book> getBookById(Long bookId, String keycloakSub)
    {
        return Optional.empty();
    }

    @Override
    public List<Book> getAllBooksByUserId(String keycloakSub)
    {
        return List.of();
    }
}
