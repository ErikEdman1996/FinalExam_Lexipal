package com.lexipal.core.controllers;

import com.lexipal.core.IntegrationTestBase;
import com.lexipal.core.dtos.requests.BookCreateRequestDTO;
import com.lexipal.core.dtos.requests.BookUpdateRequestDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookControllerIntegrationTest extends IntegrationTestBase
{
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

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

    private Book createBook(String title, String author, int totalPages, User owner)
    {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setTotalPages(totalPages);
        book.setUser(owner);
        return bookRepository.save(book);
    }

    private Session createDraftSession(Book book, int startPage, Integer endPage, Long durationSeconds)
    {
        Session session = new Session();
        session.setUser(book.getUser());
        session.setBook(book);
        session.setStartPage(startPage);
        session.setEndPage(endPage);
        session.setDurationSeconds(durationSeconds);
        return sessionRepository.save(session);
    }

    private Session createCompletedSession(Book book, int startPage, int endPage, long durationSeconds)
    {
        Session session = createDraftSession(book, startPage, endPage, durationSeconds);
        session.setReflection("Finished session");
        session.setStatus(Session.SessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        session.setPagesRead(Math.max(0, endPage - startPage));
        return sessionRepository.save(session);
    }

    @Test
    @DisplayName("POST /books creates a book and auto-creates the user")
    void addBook_createsBookAndUser() throws Exception
    {
        String newSub = "brand-new-sub";
        BookCreateRequestDTO request = new BookCreateRequestDTO("Clean Code", "Robert Martin", 431);

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub(newSub))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert Martin"))
                .andExpect(jsonPath("$.totalPages").value(431))
                .andExpect(jsonPath("$.readPages").value(0))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.sessionCount").value(0));

        assert userRepository.findByKeycloakSub(newSub).isPresent();
    }

    @Test
    @DisplayName("POST /books reuses an existing user for the same sub")
    void addBook_existingUser_reusesUser() throws Exception
    {
        BookCreateRequestDTO req1 = new BookCreateRequestDTO("Book One", "Author A", 100);
        BookCreateRequestDTO req2 = new BookCreateRequestDTO("Book Two", "Author B", 200);

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

        long userCount = userRepository.findAll().stream()
                .filter(u -> u.getKeycloakSub().equals(USER_SUB))
                .count();
        assert userCount == 1;
    }

    @Test
    @DisplayName("POST /books rejects invalid request body")
    void addBook_invalidPayload_returnsBadRequest() throws Exception
    {
        BookCreateRequestDTO request = new BookCreateRequestDTO(" ", null, 0);

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

    @Test
    @DisplayName("GET /books/{id} returns own book")
    void getBook_returnsOwnBook() throws Exception
    {
        Book book = createBook("1984", "George Orwell", 328, testUser);

        mockMvc.perform(get(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("1984"))
                .andExpect(jsonPath("$.author").value("George Orwell"))
                .andExpect(jsonPath("$.sessionCount").value(0));
    }

    @Test
    @DisplayName("GET /books/{id} counts completed sessions only")
    void getBook_countsCompletedSessionsOnly() throws Exception
    {
        Book book = createBook("1984", "George Orwell", 328, testUser);
        createDraftSession(book, 1, 20, 600L);
        createCompletedSession(book, 21, 40, 900L);

        mockMvc.perform(get(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionCount").value(1));
    }

    @Test
    @DisplayName("GET /books/{id} returns 404 for another user's book")
    void getBook_otherUsersBook_returns404() throws Exception
    {
        Book book = createBook("Secret Book", "Author", 100, testUser);

        mockMvc.perform(get(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(OTHER_USER_SUB)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /books/{id} returns 404 for deleted book")
    void getBook_deletedBook_returns404() throws Exception
    {
        Book book = createBook("Old Book", "Author", 100, testUser);
        book.setStatus(Book.BookStatus.DELETED);
        bookRepository.save(book);

        mockMvc.perform(get(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /books/all returns only active books for the user")
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

    @Test
    @DisplayName("GET /books/all returns completed-only session counts")
    void getAllBooks_usesCompletedSessionCounts() throws Exception
    {
        Book book = createBook("Active Book", "Author", 200, testUser);
        createDraftSession(book, 1, 20, 600L);
        createCompletedSession(book, 21, 40, 900L);

        mockMvc.perform(get(BASE_URL + "/all")
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sessionCount").value(1));
    }

    @Test
    @DisplayName("PUT /books/{id} updates book fields")
    void updateBook_updatesFields() throws Exception
    {
        Book book = createBook("Old Title", "Old Author", 100, testUser);
        createDraftSession(book, 1, 20, 600L);
        createCompletedSession(book, 21, 40, 900L);
        BookUpdateRequestDTO dto = new BookUpdateRequestDTO("New Title", "New Author", null, 300, null);

        mockMvc.perform(put(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.author").value("New Author"))
                .andExpect(jsonPath("$.totalPages").value(300))
                .andExpect(jsonPath("$.sessionCount").value(1));
    }

    @Test
    @DisplayName("PUT /books/{id} returns error when updating another user's book")
    void updateBook_otherUser_throwsUnauthorized() throws Exception
    {
        Book book = createBook("Someone's Book", "Author", 100, testUser);
        BookUpdateRequestDTO dto = new BookUpdateRequestDTO("Hacked", null, null, null, null);

        mockMvc.perform(put(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(OTHER_USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /books/{id} rejects invalid update payload")
    void updateBook_invalidPayload_returnsBadRequest() throws Exception
    {
        Book book = createBook("Old Title", "Old Author", 100, testUser);
        BookUpdateRequestDTO dto = new BookUpdateRequestDTO("", " ", -1, 0, null);

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

    @Test
    @DisplayName("PUT /books/{id} rejects readPages greater than totalPages")
    void updateBook_readPagesGreaterThanTotalPages_returnsBadRequest() throws Exception
    {
        Book book = createBook("Progress Book", "Author", 100, testUser);
        BookUpdateRequestDTO dto = new BookUpdateRequestDTO(null, null, 120, 100, null);

        mockMvc.perform(put(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("readPages cannot be greater than totalPages."));
    }

    @Test
    @DisplayName("PUT /books/{id} rejects invalid COMPLETE status combinations")
    void updateBook_invalidCompleteStatus_returnsBadRequest() throws Exception
    {
        Book book = createBook("Progress Book", "Author", 100, testUser);
        BookUpdateRequestDTO dto = new BookUpdateRequestDTO(null, null, 50, 100, Book.BookStatus.COMPLETE);

        mockMvc.perform(put(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A COMPLETE book must have readPages equal to totalPages."));
    }

    @Test
    @DisplayName("PUT /books/{id} rejects invalid ACTIVE status combinations")
    void updateBook_invalidActiveStatus_returnsBadRequest() throws Exception
    {
        Book book = createBook("Progress Book", "Author", 100, testUser);
        BookUpdateRequestDTO dto = new BookUpdateRequestDTO(null, null, 100, 100, Book.BookStatus.ACTIVE);

        mockMvc.perform(put(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("An ACTIVE book must have readPages less than totalPages."));
    }

    @Test
    @DisplayName("PUT /books/{id} rejects deleted books")
    void updateBook_deletedBook_returnsBadRequest() throws Exception
    {
        Book book = createBook("Deleted Book", "Author", 100, testUser);
        book.setStatus(Book.BookStatus.DELETED);
        bookRepository.save(book);

        BookUpdateRequestDTO dto = new BookUpdateRequestDTO("Updated", null, null, null, null);

        mockMvc.perform(put(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Deleted books cannot be updated."));
    }

    @Test
    @DisplayName("PUT /books/{id} rejects totalPages lower than completed session progress")
    void updateBook_totalPagesBelowCompletedProgress_returnsBadRequest() throws Exception
    {
        Book book = createBook("Progress Book", "Author", 100, testUser);
        createCompletedSession(book, 10, 40, 900L);

        BookUpdateRequestDTO dto = new BookUpdateRequestDTO(null, null, null, 30, null);

        mockMvc.perform(put(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("totalPages cannot be less than completed session progress (40)."));
    }

    @Test
    @DisplayName("DELETE /books/{id} soft-deletes by setting status to DELETED")
    void deleteBook_setsStatusDeleted() throws Exception
    {
        Book book = createBook("To Delete", "Author", 100, testUser);

        mockMvc.perform(delete(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isNoContent());

        Book deletedBook = bookRepository.findById(book.getId()).orElseThrow();
        assert deletedBook.getStatus() == Book.BookStatus.DELETED;
    }

    @Test
    @DisplayName("DELETE /books/{id} returns error when deleting another user's book")
    void deleteBook_otherUser_throwsUnauthorized() throws Exception
    {
        Book book = createBook("Not Yours", "Author", 100, testUser);

        mockMvc.perform(delete(BASE_URL + "/" + book.getId())
                        .with(jwtWithSub(OTHER_USER_SUB)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /books/all returns 401 without JWT")
    void unauthenticatedRequest_returns401() throws Exception
    {
        mockMvc.perform(get(BASE_URL + "/all"))
                .andExpect(status().isUnauthorized());
    }
}
