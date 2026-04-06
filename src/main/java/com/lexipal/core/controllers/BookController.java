package com.lexipal.core.controllers;

import com.lexipal.core.dtos.BookSummaryDTO;
import com.lexipal.core.dtos.BookUpdateDTO;
import com.lexipal.core.dtos.CreateBookRequest;
import com.lexipal.core.entities.Book;
import com.lexipal.core.services.BookService;
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
    private final BookService bookService;

    @Autowired
    public BookController(final BookService bookService)
    {
        this.bookService = bookService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id,
                                        @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        Book book = bookService.getBookById(id, keycloakSub);

        return ResponseEntity.ok(book);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BookSummaryDTO>> getAllBooks(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();

        List<BookSummaryDTO> activeBooks = bookService.getAllBooksByUserSub(keycloakSub);

        return ResponseEntity.ok(activeBooks);
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@Valid @RequestBody CreateBookRequest request,
                                        @AuthenticationPrincipal final Jwt jwt)
    {
        String keycloakSub =  jwt.getSubject();
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(request, keycloakSub));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id,
                                           @Valid @RequestBody BookUpdateDTO dto,
                                           @AuthenticationPrincipal final Jwt jwt)
    {
        String keycloakSub =  jwt.getSubject();

        Book updatedBook = bookService.updateBook(id, dto, keycloakSub);

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
