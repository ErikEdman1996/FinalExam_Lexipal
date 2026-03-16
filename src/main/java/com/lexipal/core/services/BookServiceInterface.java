package com.lexipal.core.services;

import com.lexipal.core.dtos.BookUpdateDTO;
import com.lexipal.core.entities.Book;

import java.util.List;
import java.util.Optional;

public interface BookServiceInterface
{
    Book addBook(Book book, String keycloakSub);
    Book updateBook(Long bookId, BookUpdateDTO dto, String keycloakSub);
    void deleteBook(Long bookId, String keycloakSub);
    Optional<Book> getBookById(Long bookId, String keycloakSub);
    List<Book> getAllBooksByUserId(String keycloakSub);
}
