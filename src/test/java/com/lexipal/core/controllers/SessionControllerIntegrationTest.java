package com.lexipal.core.controllers;

import com.lexipal.core.IntegrationTestBase;
import com.lexipal.core.dtos.requests.SessionCreateRequestDTO;
import com.lexipal.core.dtos.requests.SessionUpdateRequestDTO;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SessionControllerIntegrationTest extends IntegrationTestBase
{
    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String BASE_URL = "/lexipal/api/v1/sessions";
    private static final String USER_SUB = "keycloak-sub-user-1";
    private static final String OTHER_USER_SUB = "keycloak-sub-user-2";

    private User testUser;
    private User otherUser;
    private Book testBook;

    @BeforeEach
    void setUp()
    {
        testUser = new User();
        testUser.setKeycloakSub(USER_SUB);
        testUser = userRepository.save(testUser);

        otherUser = new User();
        otherUser.setKeycloakSub(OTHER_USER_SUB);
        otherUser = userRepository.save(otherUser);

        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setAuthor("Author");
        testBook.setTotalPages(300);
        testBook.setUser(testUser);
        testBook = bookRepository.save(testBook);
    }

    // ─── Helper ───────────────────────────────────────────────────

    private Session createSession(User user, Book book, int startPage, int endPage, long duration)
    {
        Session session = new Session();
        session.setUser(user);
        session.setBook(book);
        session.setStartPage(startPage);
        session.setEndPage(endPage);
        session.setDurationSeconds(duration);
        return sessionRepository.save(session);
    }

    // ─── POST /sessions ───────────────────────────────────────────

    @Test
    @DisplayName("POST /sessions — creates session and syncs book progress")
    void addSession_createsSession() throws Exception
    {
        SessionCreateRequestDTO dto = new SessionCreateRequestDTO(testBook.getId(), 1800L, 1, 50, "Great reading!");

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startPage").value(1))
                .andExpect(jsonPath("$.endPage").value(50))
                .andExpect(jsonPath("$.durationSeconds").value(1800))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Verify book progress was synced
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assert updatedBook.getReadPages() == 50;
    }

    @Test
    @DisplayName("POST /sessions — returns error when book belongs to another user")
    void addSession_otherUsersBook_throwsUnauthorized() throws Exception
    {
        SessionCreateRequestDTO dto = new SessionCreateRequestDTO(testBook.getId(), 1800L, 1, 50, null);

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub(OTHER_USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /sessions - rejects creating a session for a deleted book")
    void addSession_deletedBook_returnsBadRequest() throws Exception
    {
        testBook.setStatus(Book.BookStatus.DELETED);
        bookRepository.save(testBook);

        SessionCreateRequestDTO dto = new SessionCreateRequestDTO(testBook.getId(), 1800L, 1, 50, "Should fail");

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /sessions - rejects creating a session for an archived book")
    void addSession_archivedBook_returnsBadRequest() throws Exception
    {
        testBook.setStatus(Book.BookStatus.ARCHIVED);
        bookRepository.save(testBook);

        SessionCreateRequestDTO dto = new SessionCreateRequestDTO(testBook.getId(), 1800L, 1, 50, "Should fail");

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /sessions - rejects invalid request body")
    void addSession_invalidPayload_returnsBadRequest() throws Exception
    {
        SessionCreateRequestDTO dto = new SessionCreateRequestDTO(null, -1L, null, -5, null);

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.bookId").exists())
                .andExpect(jsonPath("$.errors.durationSeconds").exists())
                .andExpect(jsonPath("$.errors.startPage").exists())
                .andExpect(jsonPath("$.errors.endPage").exists());
    }

    @Test
    @DisplayName("POST /sessions returns 404 with error body when authenticated user is missing")
    void addSession_missingUser_returnsNotFoundWithBody() throws Exception
    {
        SessionCreateRequestDTO dto = new SessionCreateRequestDTO(testBook.getId(), 1800L, 1, 50, "Great reading!");

        mockMvc.perform(post(BASE_URL)
                        .with(jwtWithSub("missing-user-sub"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("User with sub")));
    }

    // ─── GET /sessions/{id} ───────────────────────────────────────

    @Test
    @DisplayName("GET /sessions/{id} — returns own session")
    void getSession_returnsOwnSession() throws Exception
    {
        Session session = createSession(testUser, testBook, 1, 30, 900L);

        mockMvc.perform(get(BASE_URL + "/" + session.getId())
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startPage").value(1))
                .andExpect(jsonPath("$.endPage").value(30));
    }

    @Test
    @DisplayName("GET /sessions/{id} — returns 404 for another user's session")
    void getSession_otherUser_returns404() throws Exception
    {
        Session session = createSession(testUser, testBook, 1, 30, 900L);

        mockMvc.perform(get(BASE_URL + "/" + session.getId())
                        .with(jwtWithSub(OTHER_USER_SUB)))
                .andExpect(status().isNotFound());
    }

    // ─── GET /sessions/book/{bookId} ──────────────────────────────

    @Test
    @DisplayName("GET /sessions/book/{bookId} — returns sessions ordered by date desc")
    void getSessionsByBook_returnsSessions() throws Exception
    {
        createSession(testUser, testBook, 1, 30, 900L);
        createSession(testUser, testBook, 31, 60, 1200L);

        mockMvc.perform(get(BASE_URL + "/book/" + testBook.getId())
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /sessions/book/{bookId} — returns error when book belongs to another user")
    void getSessionsByBook_otherUser_throwsUnauthorized() throws Exception
    {
        mockMvc.perform(get(BASE_URL + "/book/" + testBook.getId())
                        .with(jwtWithSub(OTHER_USER_SUB)))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /sessions/{id} ──────────────────────────────────────

    @Test
    @DisplayName("PUT /sessions/{id} — updates fields and syncs book progress")
    void updateSession_updatesFieldsAndSyncsBook() throws Exception
    {
        Session session = createSession(testUser, testBook, 1, 30, 900L);

        SessionUpdateRequestDTO dto = new SessionUpdateRequestDTO(80, 2400L, "Updated reflection", null);

        mockMvc.perform(put(BASE_URL + "/" + session.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endPage").value(80))
                .andExpect(jsonPath("$.durationSeconds").value(2400))
                .andExpect(jsonPath("$.reflection").value("Updated reflection"))
                .andExpect(jsonPath("$.pagesRead").value(79));

        // Verify book progress was synced
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assert updatedBook.getReadPages() == 80;
    }

    @Test
    @DisplayName("PUT /sessions/{id} — auto-completes when all fields are set")
    void updateSession_autoCompletes() throws Exception
    {
        Session session = createSession(testUser, testBook, 1, 30, 0L);

        SessionUpdateRequestDTO dto = new SessionUpdateRequestDTO(100, 3600L, "Finished the book!", null);

        mockMvc.perform(put(BASE_URL + "/" + session.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("PUT /sessions/{id} — rejects endPage less than startPage")
    void updateSession_endPageLessThanStart_throwsError() throws Exception
    {
        Session session = createSession(testUser, testBook, 50, 60, 900L);

        // endPage (10) < startPage (50) should throw
        SessionUpdateRequestDTO dto = new SessionUpdateRequestDTO(10, null, null, null);

        mockMvc.perform(put(BASE_URL + "/" + session.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /sessions/{id} - rejects updates when the parent book is deleted")
    void updateSession_deletedBook_returnsBadRequestAndKeepsDeletedStatus() throws Exception
    {
        Session session = createSession(testUser, testBook, 1, 30, 900L);
        testBook.setStatus(Book.BookStatus.DELETED);
        bookRepository.save(testBook);

        SessionUpdateRequestDTO dto = new SessionUpdateRequestDTO(80, 2400L, "Should fail", null);

        mockMvc.perform(put(BASE_URL + "/" + session.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Book unchangedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assert unchangedBook.getStatus() == Book.BookStatus.DELETED;
    }

    @Test
    @DisplayName("PUT /sessions/{id} - rejects updates when the parent book is archived")
    void updateSession_archivedBook_returnsBadRequestAndKeepsArchivedStatus() throws Exception
    {
        Session session = createSession(testUser, testBook, 1, 30, 900L);
        testBook.setStatus(Book.BookStatus.ARCHIVED);
        bookRepository.save(testBook);

        SessionUpdateRequestDTO dto = new SessionUpdateRequestDTO(80, 2400L, "Should fail", null);

        mockMvc.perform(put(BASE_URL + "/" + session.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Book unchangedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assert unchangedBook.getStatus() == Book.BookStatus.ARCHIVED;
    }

    @Test
    @DisplayName("PUT /sessions/{id} - rejects invalid update payload")
    void updateSession_invalidPayload_returnsBadRequest() throws Exception
    {
        Session session = createSession(testUser, testBook, 1, 30, 900L);
        SessionUpdateRequestDTO dto = new SessionUpdateRequestDTO(-10, -1L, null, null);

        mockMvc.perform(put(BASE_URL + "/" + session.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.endPage").exists())
                .andExpect(jsonPath("$.errors.durationSeconds").exists());
    }
}
