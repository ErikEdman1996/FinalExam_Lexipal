package com.lexipal.core.controllers;

import com.lexipal.core.entities.Book;
import com.lexipal.core.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
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

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book, @AuthenticationPrincipal final Jwt jwt)
    {
        String keycloakSub =  jwt.getSubject();
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(book, keycloakSub));
    }
}
