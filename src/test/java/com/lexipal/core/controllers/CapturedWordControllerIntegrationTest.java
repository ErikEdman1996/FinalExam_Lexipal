package com.lexipal.core.controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lexipal.core.IntegrationTestBase;
import com.lexipal.core.dtos.requests.CaptureCreateRequestDTO;
import com.lexipal.core.dtos.requests.DictionaryAPIRequestDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ExternalServiceException;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.CapturedWordRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import com.lexipal.core.services.DictionaryClientService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CapturedWordControllerIntegrationTest extends IntegrationTestBase
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CapturedWordRepository capturedWordRepository;

    @MockitoBean
    private DictionaryClientService dictionaryClientService;

    private static final String BASE_URL = "/lexipal/api/v1/capturedWords";
    private static final String USER_SUB = "keycloak-sub-user-1";
    private static final String OTHER_USER_SUB = "keycloak-sub-user-2";

    private User testUser;
    private Session testSession;

    @BeforeEach
    void setUp()
    {
        testUser = new User();
        testUser.setKeycloakSub(USER_SUB);
        testUser = userRepository.save(testUser);

        Book testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setAuthor("Author");
        testBook.setTotalPages(300);
        testBook.setUser(testUser);
        testBook = bookRepository.save(testBook);

        testSession = new Session();
        testSession.setUser(testUser);
        testSession.setBook(testBook);
        testSession.setStartPage(1);
        testSession.setEndPage(30);
        testSession.setDurationSeconds(900L);
        testSession = sessionRepository.save(testSession);

        stubDictionaryApi();
    }

    private void stubDictionaryApi()
    {
        when(dictionaryClientService.fetchWordFromApi(eq("ephemeral"))).thenReturn(
                createFakeDictionaryResponse("ephemeral", "adjective", "lasting for a very short time", "/ephemeral/")
        );

        when(dictionaryClientService.fetchWordFromApi(eq("serendipity"))).thenReturn(
                createFakeDictionaryResponse("serendipity", "noun", "the occurrence of events by chance in a happy way", "/serendipity/")
        );

        when(dictionaryClientService.fetchWordFromApi(anyString())).thenAnswer(invocation ->
                createFakeDictionaryResponse(
                        invocation.getArgument(0, String.class),
                        "noun",
                        "generated test definition",
                        "/test/"
                )
        );
    }

    private DictionaryAPIRequestDTO createFakeDictionaryResponse(
            String word,
            String partOfSpeech,
            String definitionText,
            String phoneticText)
    {
        DictionaryAPIRequestDTO fakeResponse = new DictionaryAPIRequestDTO();
        fakeResponse.setWord(word);

        ObjectNode meaning = objectMapper.createObjectNode();
        meaning.put("partOfSpeech", partOfSpeech);

        ArrayNode definitions = objectMapper.createArrayNode();
        ObjectNode definition = objectMapper.createObjectNode();
        definition.put("definition", definitionText);
        definitions.add(definition);
        meaning.set("definitions", definitions);

        ArrayNode meaningsArray = objectMapper.createArrayNode();
        meaningsArray.add(meaning);
        fakeResponse.setMeanings(meaningsArray);

        ArrayNode phonetics = objectMapper.createArrayNode();
        ObjectNode phonetic = objectMapper.createObjectNode();
        phonetic.put("text", phoneticText);
        phonetics.add(phonetic);
        fakeResponse.setPhonetics(phonetics);

        return fakeResponse;
    }

    @Test
    @DisplayName("POST /capturedWords/{sessionId} creates a new captured word")
    void searchAndCapture_newWord_capturesAndReturnsNew() throws Exception
    {
        CaptureCreateRequestDTO request = new CaptureCreateRequestDTO("ephemeral");

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newCapture").value(true))
                .andExpect(jsonPath("$.capturedWord").isNotEmpty());
    }

    @Test
    @DisplayName("POST /capturedWords/{sessionId} rejects invalid request body")
    void searchAndCapture_invalidPayload_returnsBadRequest() throws Exception
    {
        CaptureCreateRequestDTO request = new CaptureCreateRequestDTO(" ");

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.lemma").exists());
    }

    @Test
    @DisplayName("POST /capturedWords/{sessionId} returns existing capture for duplicate word")
    void searchAndCapture_existingWord_returnsCached() throws Exception
    {
        CaptureCreateRequestDTO request = new CaptureCreateRequestDTO("ephemeral");

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newCapture").value(true));

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newCapture").value(false))
                .andExpect(jsonPath("$.capturedWord.lemma").value("ephemeral"))
                .andExpect(jsonPath("$.capturedWord.wordId").isNumber());
    }

    @Test
    @DisplayName("POST /capturedWords/{sessionId} lets the same user capture multiple different words")
    void searchAndCapture_multipleDifferentWordsForSameUser_succeeds() throws Exception
    {
        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CaptureCreateRequestDTO("ephemeral"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newCapture").value(true));

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CaptureCreateRequestDTO("serendipity"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newCapture").value(true));

        Assertions.assertEquals(2, capturedWordRepository.count());
    }

    @Test
    @DisplayName("POST /capturedWords/{sessionId} returns error for another user's session")
    void searchAndCapture_otherUsersSession_throwsUnauthorized() throws Exception
    {
        CaptureCreateRequestDTO request = new CaptureCreateRequestDTO("ephemeral");

        User otherUser = new User();
        otherUser.setKeycloakSub(OTHER_USER_SUB);
        userRepository.save(otherUser);

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(OTHER_USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /capturedWords/{sessionId} returns 404 for non-existent session")
    void searchAndCapture_nonExistentSession_returns404() throws Exception
    {
        CaptureCreateRequestDTO request = new CaptureCreateRequestDTO("ephemeral");

        mockMvc.perform(post(BASE_URL + "/99999")
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /capturedWords/{sessionId} returns 404 for non-existent user")
    void searchAndCapture_nonExistentUser_returns404() throws Exception
    {
        CaptureCreateRequestDTO request = new CaptureCreateRequestDTO("ephemeral");

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub("non-existent-sub"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /capturedWords/{sessionId} returns 404 with body when word is missing from dictionary")
    void searchAndCapture_missingDictionaryWord_returnsNotFoundWithBody() throws Exception
    {
        when(dictionaryClientService.fetchWordFromApi(eq("missingword")))
                .thenThrow(new ResourceNotFoundException("Word", "lemma", "missingword"));

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CaptureCreateRequestDTO("missingword"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Word with lemma")));
    }

    @Test
    @DisplayName("POST /capturedWords/{sessionId} returns 502 with body when dictionary service fails")
    void searchAndCapture_dictionaryFailure_returnsBadGatewayWithBody() throws Exception
    {
        when(dictionaryClientService.fetchWordFromApi(eq("outage")))
                .thenThrow(new ExternalServiceException("Dictionary service is unavailable for word: outage"));

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CaptureCreateRequestDTO("outage"))))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Dictionary service is unavailable for word: outage"));
    }
}
