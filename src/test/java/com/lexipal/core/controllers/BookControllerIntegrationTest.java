package com.lexipal.core.controllers;

import com.lexipal.core.IntegrationTestBase;
import com.lexipal.core.dtos.BookUpdateDTO;
import com.lexipal.core.dtos.CreateBookRequest;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookControllerIntegrationTest extends IntegrationTestBase
{
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String BASE_URL = "/lexipal/api/v1/books";
    private static final String USER_SUB = "keycloak-sub-user-1";
    private static final String OTHER_USER_SUB = "keycloak-sub-user-2";

    private User testUser;

    @BeforeEach
    void setUp()
    {
        testUser = new User();
        testUser.setKeycloakSub(USER_SUB);
        testUser = userRepository.save(testUser);
    }

    // ─── Helper ───────────────────────────────────────────────────

    private Book createBook(String title, String author, int totalPages, User owner)
    {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setTotalPages(totalPages);
        book.setUser(owner);
        return bookRepository.save(book);
    }

    // ─── POST /books ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /books — creates book and auto-creates user when user doesn't exist")
    void addBook_createsBookAndUser() throws Exception
    {
        String newSub = "brand-new-sub";
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", 431);

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub(newSub))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert Martin"))
                .andExpect(jsonPath("$.totalPages").value(431))
                .andExpect(jsonPath("$.readPages").value(0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify user was auto-created
        assert userRepository.findByKeycloakSub(newSub).isPresent();
    }

    @Test
    @DisplayName("POST /books — reuses existing user for the same sub")
    void addBook_existingUser_reusesUser() throws Exception
    {
        CreateBookRequest req1 = new CreateBookRequest("Book One", "Author A", 100);
        CreateBookRequest req2 = new CreateBookRequest("Book Two", "Author B", 200);

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // Only one user should exist for this sub
        long userCount = userRepository.findAll().stream()
                .filter(u -> u.getKeycloakSub().equals(USER_SUB))
                .count();
        assert userCount == 1;
    }

    @Test
    @DisplayName("POST /books - rejects invalid request body")
    void addBook_invalidPayload_returnsBadRequest() throws Exception
    {
        CreateBookRequest request = new CreateBookRequest(" ", null, 0);

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.author").exists())
                .andExpect(jsonPath("$.errors.totalPages").exists());
    }

    // ─── GET /books/{id} ──────────────────────────────────────────

    @Test
    @DisplayName("GET /books/{id} — returns own book")
    void getBook_returnsOwnBook() throws Exception
    {
        Book book = createBook("1984", "George Orwell", 328, testUser);

        mockMvc.perform(get(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("1984"))
                .andExpect(jsonPath("$.author").value("George Orwell"));
    }

    @Test
    @DisplayName("GET /books/{id} — returns 404 for another user's book")
    void getBook_otherUsersBook_returns404() throws Exception
    {
        Book book = createBook("Secret Book", "Author", 100, testUser);

        mockMvc.perform(get(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(OTHER_USER_SUB)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /books/{id} — returns 404 for deleted book")
    void getBook_deletedBook_returns404() throws Exception
    {
        Book book = createBook("Old Book", "Author", 100, testUser);
        book.setStatus(Book.BookStatus.DELETED);
        bookRepository.save(book);

        mockMvc.perform(get(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isNotFound());
    }

    // ─── GET /books/all ───────────────────────────────────────────

    @Test
    @DisplayName("GET /books/all — returns only active books for the user")
    void getAllBooks_returnsOnlyActiveBooks() throws Exception
    {
        createBook("Active Book", "Author", 200, testUser);
        Book deleted = createBook("Deleted Book", "Author", 150, testUser);
        deleted.setStatus(Book.BookStatus.DELETED);
        bookRepository.save(deleted);

        mockMvc.perform(get(BASE_URL + "/all")
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Active Book"));
    }

    // ─── PUT /books/{id} ──────────────────────────────────────────

    @Test
    @DisplayName("PUT /books/{id} — updates book fields")
    void updateBook_updatesFields() throws Exception
    {
        Book book = createBook("Old Title", "Old Author", 100, testUser);
        BookUpdateDTO dto = new BookUpdateDTO("New Title", "New Author", null, 300, null);

        mockMvc.perform(put(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.author").value("New Author"))
                .andExpect(jsonPath("$.totalPages").value(300));
    }

    @Test
    @DisplayName("PUT /books/{id} — returns error when updating another user's book")
    void updateBook_otherUser_throwsUnauthorized() throws Exception
    {
        Book book = createBook("Someone's Book", "Author", 100, testUser);
        BookUpdateDTO dto = new BookUpdateDTO("Hacked", null, null, null, null);

        mockMvc.perform(put(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(OTHER_USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /books/{id} - rejects invalid update payload")
    void updateBook_invalidPayload_returnsBadRequest() throws Exception
    {
        Book book = createBook("Old Title", "Old Author", 100, testUser);
        BookUpdateDTO dto = new BookUpdateDTO("", " ", -1, 0, null);

        mockMvc.perform(put(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.author").exists())
                .andExpect(jsonPath("$.errors.readPages").exists())
                .andExpect(jsonPath("$.errors.totalPages").exists());
    }

    // ─── DELETE /books/{id} ───────────────────────────────────────

    @Test
    @DisplayName("DELETE /books/{id} — soft-deletes by setting status to DELETED")
    void deleteBook_setsStatusDeleted() throws Exception
    {
        Book book = createBook("To Delete", "Author", 100, testUser);

        mockMvc.perform(delete(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isNoContent());

        // Verify the book is soft-deleted
        Book deletedBook = bookRepository.findById(book.getId()).orElseThrow();
        assert deletedBook.getStatus() == Book.BookStatus.DELETED;
    }

    @Test
    @DisplayName("DELETE /books/{id} — returns error when deleting another user's book")
    void deleteBook_otherUser_throwsUnauthorized() throws Exception
    {
        Book book = createBook("Not Yours", "Author", 100, testUser);

        mockMvc.perform(delete(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(OTHER_USER_SUB)))
                .andExpect(status().isForbidden());
    }

    // ─── Security ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /books/all — returns 401 without JWT")
    void unauthenticatedRequest_returns401() throws Exception
    {
        mockMvc.perform(get(BASE_URL + "/all"))
                .andExpect(status().isUnauthorized());
    }
}
