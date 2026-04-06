package com.lexipal.core.services;
import com.lexipal.core.dtos.BookSummaryDTO;
import com.lexipal.core.dtos.BookUpdateDTO;
import com.lexipal.core.dtos.CreateBookRequest;
import com.lexipal.core.entities.Book;
import java.util.List;
import java.util.Optional;

public interface BookServiceInterface
{
    Book addBook(CreateBookRequest request, String keycloakSub);
    Book updateBook(Long bookId, BookUpdateDTO dto, String keycloakSub);
    void deleteBook(Long bookId, String keycloakSub);
    Book getBookById(Long bookId, String keycloakSub);
    List<BookSummaryDTO> getAllBooksByUserSub(String keycloakSub);
    void syncBookProgress(Long bookId);

    double calculateBookProgress(Book book);
}
