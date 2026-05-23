package com.lexipal.core.controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lexipal.core.IntegrationTestBase;
import com.lexipal.core.dtos.requests.DictionaryAPIRequestDTO;
import com.lexipal.core.dtos.requests.WordLookupCreateRequestDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ExternalServiceException;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import com.lexipal.core.services.DictionaryClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WordLookupControllerIntegrationTest extends IntegrationTestBase
{
    private static final String BASE_URL = "/lexipal/api/v1/word-lookups";
    private static final String USER_SUB = "keycloak-sub-user-1";
    private static final String OTHER_USER_SUB = "keycloak-sub-user-2";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @MockitoBean
    private DictionaryClientService dictionaryClientService;

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

    @Test
    @DisplayName("POST /word-lookups/{sessionId} creates a lookup")
    void lookupWord_newWord_createsLookup() throws Exception
    {
        WordLookupCreateRequestDTO request = new WordLookupCreateRequestDTO("ephemeral");

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$.lookupSucceeded").value(true))
                .andExpect(jsonPath("$.isNewLookup").value(true))
                .andExpect(jsonPath("$.wordLookup").isNotEmpty())
                .andExpect(jsonPath("$.wordLookup.lemma").value("ephemeral"))
                .andExpect(jsonPath("$.wordLookup.lookupCount").value(1));
    }

    @Test
    @DisplayName("POST /word-lookups/{sessionId} updates an existing lookup")
    void lookupWord_existingWord_updatesLookupCount() throws Exception
    {
        WordLookupCreateRequestDTO request = new WordLookupCreateRequestDTO("ephemeral");

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNewLookup").value(true));

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$.isNewLookup").value(false))
                .andExpect(jsonPath("$.wordLookup.lookupCount").value(2));
    }

    @Test
    @DisplayName("POST /word-lookups/{sessionId} rejects invalid request body")
    void lookupWord_invalidPayload_returnsBadRequest() throws Exception
    {
        WordLookupCreateRequestDTO request = new WordLookupCreateRequestDTO(" ");

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.lemma").exists());
    }

    @Test
    @DisplayName("POST /word-lookups/{sessionId} returns error for another user's session")
    void lookupWord_otherUsersSession_throwsUnauthorized() throws Exception
    {
        WordLookupCreateRequestDTO request = new WordLookupCreateRequestDTO("ephemeral");

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
    @DisplayName("POST /word-lookups/{sessionId} returns 404 for non-existent session")
    void lookupWord_nonExistentSession_returns404() throws Exception
    {
        WordLookupCreateRequestDTO request = new WordLookupCreateRequestDTO("ephemeral");

        mockMvc.perform(post(BASE_URL + "/99999")
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /word-lookups/{sessionId} returns 404 for non-existent user")
    void lookupWord_nonExistentUser_returns404() throws Exception
    {
        WordLookupCreateRequestDTO request = new WordLookupCreateRequestDTO("ephemeral");

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub("non-existent-sub"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /word-lookups/{sessionId} returns 404 with body when word is missing from dictionary")
    void lookupWord_missingDictionaryWord_returnsNotFoundWithBody() throws Exception
    {
        when(dictionaryClientService.fetchWordFromApi(eq("missingword")))
                .thenThrow(new ResourceNotFoundException("Word", "lemma", "missingword"));

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WordLookupCreateRequestDTO("missingword"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Word with lemma")));
    }

    @Test
    @DisplayName("POST /word-lookups/{sessionId} returns 502 with body when dictionary service fails")
    void lookupWord_dictionaryFailure_returnsBadGatewayWithBody() throws Exception
    {
        when(dictionaryClientService.fetchWordFromApi(eq("outage")))
                .thenThrow(new ExternalServiceException("Dictionary service is unavailable for word: outage"));

        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WordLookupCreateRequestDTO("outage"))))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Dictionary service is unavailable for word: outage"));
    }

    @Test
    @DisplayName("GET /word-lookups returns lookup history")
    void getWordLookups_returnsLookupHistory() throws Exception
    {
        mockMvc.perform(post(BASE_URL + "/" + testSession.getId())
                        .with(jwtWithSub(USER_SUB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WordLookupCreateRequestDTO("ephemeral"))))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL)
                        .with(jwtWithSub(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].wordId").isNumber())
                .andExpect(jsonPath("$[0].lemma").value("ephemeral"))
                .andExpect(jsonPath("$[0].lookupCount").value(1));
    }

    private void stubDictionaryApi()
    {
        when(dictionaryClientService.fetchWordFromApi(eq("ephemeral"))).thenReturn(
                createFakeDictionaryResponse("ephemeral", "adjective", "lasting for a very short time", "/ephemeral/")
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

        ObjectNode entry = objectMapper.createObjectNode();
        entry.put("partOfSpeech", partOfSpeech);

        ArrayNode pronunciations = objectMapper.createArrayNode();
        ObjectNode pronunciation = objectMapper.createObjectNode();
        pronunciation.put("type", "ipa");
        pronunciation.put("text", phoneticText);
        pronunciation.set("tags", objectMapper.createArrayNode());
        pronunciations.add(pronunciation);
        entry.set("pronunciations", pronunciations);

        entry.set("forms", objectMapper.createArrayNode());

        ArrayNode senses = objectMapper.createArrayNode();
        ObjectNode sense = objectMapper.createObjectNode();
        sense.put("definition", definitionText);
        sense.set("tags", objectMapper.createArrayNode());
        sense.set("examples", objectMapper.createArrayNode());
        sense.set("quotes", objectMapper.createArrayNode());
        sense.set("synonyms", objectMapper.createArrayNode());
        sense.set("antonyms", objectMapper.createArrayNode());
        sense.set("subsenses", objectMapper.createArrayNode());
        senses.add(sense);
        entry.set("senses", senses);
        entry.set("synonyms", objectMapper.createArrayNode());
        entry.set("antonyms", objectMapper.createArrayNode());

        ArrayNode entries = objectMapper.createArrayNode();
        entries.add(entry);
        fakeResponse.setEntries(entries);

        return fakeResponse;
    }
}
