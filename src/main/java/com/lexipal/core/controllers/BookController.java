package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.BookCreateResponseDTO;
import com.lexipal.core.dtos.responses.BookReadResponseDTO;
import com.lexipal.core.dtos.responses.BookUpdateResponseDTO;
import com.lexipal.core.dtos.requests.BookUpdateRequestDTO;
import com.lexipal.core.dtos.requests.BookCreateRequestDTO;
import com.lexipal.core.services.BookServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/books")
public class BookController
{
    private final BookServiceInterface bookService;

    @Autowired
    public BookController(final BookServiceInterface bookService)
    {
        this.bookService = bookService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookReadResponseDTO> getBook(@PathVariable Long id,
                                                       @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        BookReadResponseDTO book = bookService.getBookById(id, keycloakSub);

        return ResponseEntity.ok(book);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BookReadResponseDTO>> getAllBooks(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        List<BookReadResponseDTO> activeBooks = bookService.getAllBooksByUserSub(keycloakSub);

        return ResponseEntity.ok(activeBooks);
    }

    @PostMapping
    public ResponseEntity<BookCreateResponseDTO> addBook(@Valid @RequestBody BookCreateRequestDTO request,
                                                         @AuthenticationPrincipal final Jwt jwt)
    {
        String keycloakSub =  jwt.getSubject();
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(request, keycloakSub));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookUpdateResponseDTO> updateBook(@PathVariable Long id,
                                                            @Valid @RequestBody BookUpdateRequestDTO dto,
                                                            @AuthenticationPrincipal final Jwt jwt)
    {
        String keycloakSub =  jwt.getSubject();

        BookUpdateResponseDTO updatedBook = bookService.updateBook(id, dto, keycloakSub);

        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id,
                                        @AuthenticationPrincipal final Jwt jwt)
    {
        String keycloakSub =  jwt.getSubject();

        bookService.deleteBook(id, keycloakSub);

        return ResponseEntity.noContent().build();
    }
}
