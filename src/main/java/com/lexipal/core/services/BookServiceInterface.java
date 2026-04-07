package com.lexipal.core.services;
import com.lexipal.core.dtos.responses.BookCreateResponseDTO;
import com.lexipal.core.dtos.responses.BookReadResponseDTO;
import com.lexipal.core.dtos.responses.BookUpdateResponseDTO;
import com.lexipal.core.dtos.requests.BookUpdateRequestDTO;
import com.lexipal.core.dtos.requests.BookCreateRequestDTO;
import com.lexipal.core.entities.Book;
import java.util.List;

public interface BookServiceInterface
{
    BookCreateResponseDTO addBook(BookCreateRequestDTO request, String keycloakSub);
    BookUpdateResponseDTO updateBook(Long bookId, BookUpdateRequestDTO dto, String keycloakSub);
    void deleteBook(Long bookId, String keycloakSub);
    BookReadResponseDTO getBookById(Long bookId, String keycloakSub);
    List<BookReadResponseDTO> getAllBooksByUserSub(String keycloakSub);
    void syncBookProgress(Long bookId);

    double calculateBookProgress(Book book);
}
