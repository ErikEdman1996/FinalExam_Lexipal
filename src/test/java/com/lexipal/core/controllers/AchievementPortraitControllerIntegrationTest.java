package com.lexipal.core.controllers;

import com.lexipal.core.IntegrationTestBase;
import com.lexipal.core.dtos.requests.PortraitSelectionRequestDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.BookCircle;
import com.lexipal.core.entities.CircleMember;
import com.lexipal.core.entities.CircleMemberRole;
import com.lexipal.core.entities.CircleStatus;
import com.lexipal.core.entities.CircleVisibility;
import com.lexipal.core.entities.CodexEntry;
import com.lexipal.core.entities.ReadingList;
import com.lexipal.core.entities.ReadingListSubscription;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.Word;
import com.lexipal.core.repositories.BookCircleRepository;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.CircleMemberRepository;
import com.lexipal.core.repositories.CodexEntryRepository;
import com.lexipal.core.repositories.PortraitDefinitionRepository;
import com.lexipal.core.repositories.ReadingListRepository;
import com.lexipal.core.repositories.ReadingListSubscriptionRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserAchievementRepository;
import com.lexipal.core.repositories.UserRepository;
import com.lexipal.core.repositories.WordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AchievementPortraitControllerIntegrationTest extends IntegrationTestBase
{
    private static final String ACHIEVEMENTS_URL = "/lexipal/api/v1/achievements/me";
    private static final String PORTRAITS_URL = "/lexipal/api/v1/portraits/me";

    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private WordRepository wordRepository;
    @Autowired private CodexEntryRepository codexEntryRepository;
    @Autowired private ReadingListRepository readingListRepository;
    @Autowired private ReadingListSubscriptionRepository subscriptionRepository;
    @Autowired private BookCircleRepository bookCircleRepository;
    @Autowired private CircleMemberRepository circleMemberRepository;
    @Autowired private PortraitDefinitionRepository portraitDefinitionRepository;
    @Autowired private UserAchievementRepository userAchievementRepository;

    @Test
    @DisplayName("GET /achievements/me unlocks retroactive milestones and stays idempotent")
    void achievements_unlockRetroactivelyAndDoNotDuplicate() throws Exception
    {
        User user = createUser("achievement-retro-sub", 0);
        Book book = createBook(user, "Retro Book", 100, Book.BookStatus.COMPLETE);
        createCompletedSession(user, book, 1, 61);
        createCodexEntries(user, 10);
        createCompletedCircleBook(user, book);

        mockMvc.perform(get(ACHIEVEMENTS_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'PAGES_50')].unlocked").value(hasItem(true)))
                .andExpect(jsonPath("$[?(@.code == 'PAGES_100')].unlocked").value(hasItem(false)))
                .andExpect(jsonPath("$[?(@.code == 'PAGES_100')].progress").value(hasItem(60)))
                .andExpect(jsonPath("$[?(@.code == 'BOOKS_1')].unlocked").value(hasItem(true)))
                .andExpect(jsonPath("$[?(@.code == 'CODEX_10')].unlocked").value(hasItem(true)))
                .andExpect(jsonPath("$[?(@.code == 'CIRCLE_BOOKS_1')].unlocked").value(hasItem(true)));

        int unlockCount = userAchievementRepository.findByUser_Id(user.getId()).size();

        mockMvc.perform(get(ACHIEVEMENTS_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk());

        assertEquals(unlockCount, userAchievementRepository.findByUser_Id(user.getId()).size());
    }

    @Test
    @DisplayName("Reading list star and subscriber achievements unlock for the list owner")
    void readingListAchievements_unlockForOwner() throws Exception
    {
        User owner = createUser("list-achievement-owner-sub", 0);
        User actor = createUser("list-achievement-actor-sub", 0);
        ReadingList list = createReadingList(owner, "Public Roadmap");

        mockMvc.perform(post("/lexipal/api/v1/reading-lists/" + list.getId() + "/star")
                        .with(jwtWithSub(actor.getKeycloakSub())))
                .andExpect(status().isNoContent());

        ReadingListSubscription subscription = new ReadingListSubscription();
        subscription.setList(list);
        subscription.setUser(actor);
        subscriptionRepository.save(subscription);

        mockMvc.perform(get(ACHIEVEMENTS_URL).with(jwtWithSub(owner.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'LIST_STARS_1')].unlocked").value(hasItem(true)))
                .andExpect(jsonPath("$[?(@.code == 'LIST_SUBSCRIBERS_1')].unlocked").value(hasItem(true)));

        mockMvc.perform(get(ACHIEVEMENTS_URL).with(jwtWithSub(actor.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'LIST_STARS_1')].unlocked").value(hasItem(false)))
                .andExpect(jsonPath("$[?(@.code == 'LIST_SUBSCRIBERS_1')].unlocked").value(hasItem(false)));
    }

    @Test
    @DisplayName("Portraits unlock from level and selected portrait can be changed")
    void portraits_unlockAndCanBeSelected() throws Exception
    {
        User user = createUser("portrait-level-sub", 200);

        mockMvc.perform(get("/lexipal/api/v1/profile/me").with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedPortrait.code").value("larvae_starter"));

        mockMvc.perform(get(PORTRAITS_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'larvae_starter')].unlocked").value(hasItem(true)))
                .andExpect(jsonPath("$[?(@.code == 'curious_larvae')].unlocked").value(hasItem(true)))
                .andExpect(jsonPath("$[?(@.code == 'lexipal_moth')].unlocked").value(hasItem(false)));

        mockMvc.perform(patch(PORTRAITS_URL + "/selected")
                        .with(jwtWithSub(user.getKeycloakSub()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PortraitSelectionRequestDTO("curious_larvae"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("curious_larvae"))
                .andExpect(jsonPath("$.selected").value(true));
    }

    @Test
    @DisplayName("Locked portraits cannot be selected")
    void portraits_rejectLockedSelection() throws Exception
    {
        User user = createUser("portrait-locked-sub", 0);

        mockMvc.perform(patch(PORTRAITS_URL + "/selected")
                        .with(jwtWithSub(user.getKeycloakSub()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PortraitSelectionRequestDTO("lexipal_moth"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Portrait is locked."));
    }

    @Test
    @DisplayName("Circle member responses include selected portrait")
    void circleMemberResponses_includeSelectedPortrait() throws Exception
    {
        User owner = createUser("circle-portrait-owner-sub", 0);
        User member = createUser("circle-portrait-member-sub", 200);
        member.setSelectedPortrait(portraitDefinitionRepository.findByCode("curious_larvae").orElseThrow());
        member = userRepository.save(member);

        Book ownerBook = createBook(owner, "Shared Portrait Book", 120, Book.BookStatus.ACTIVE);
        Book memberBook = createBook(member, "Shared Portrait Book", 120, Book.BookStatus.ACTIVE);

        BookCircle circle = createCircle(owner, ownerBook, CircleVisibility.ALL_MEMBERS);
        createCircleMember(circle, owner, ownerBook, CircleMemberRole.OWNER);
        createCircleMember(circle, member, memberBook, CircleMemberRole.MEMBER);

        mockMvc.perform(get("/lexipal/api/v1/book-circles/" + circle.getId())
                        .with(jwtWithSub(owner.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[?(@.userId == " + member.getId()
                        + ")].selectedPortrait.code").value(hasItem("curious_larvae")));

        mockMvc.perform(get("/lexipal/api/v1/book-circles/" + circle.getId()
                        + "/members/" + member.getId() + "/details")
                        .with(jwtWithSub(owner.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedPortrait.code").value("curious_larvae"));
    }

    private User createUser(String sub, int totalXp)
    {
        User user = new User();
        user.setKeycloakSub(sub);
        user = userRepository.save(user);
        user.setTotalXp(totalXp);
        return userRepository.save(user);
    }

    private Book createBook(User user, String title, int totalPages, Book.BookStatus status)
    {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Author");
        book.setTotalPages(totalPages);
        book.setReadPages(status == Book.BookStatus.COMPLETE ? totalPages : 0);
        book.setStatus(status);
        book.setCompletedAt(status == Book.BookStatus.COMPLETE ? LocalDateTime.now() : null);
        book.setUser(user);
        return bookRepository.save(book);
    }

    private void createCompletedSession(User user, Book book, int startPage, int endPage)
    {
        Session session = new Session();
        session.setUser(user);
        session.setBook(book);
        session.setStartPage(startPage);
        session.setEndPage(endPage);
        session.setPagesRead(endPage - startPage);
        session.setDurationSeconds(600L);
        session.setReflection("Thoughtful session.");
        session.setStatus(Session.SessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    private void createCodexEntries(User user, int count)
    {
        for (int i = 0; i < count; i++)
        {
            Word word = new Word();
            word.setLemma("achievement-word-" + user.getKeycloakSub() + "-" + i);
            word = wordRepository.save(word);

            CodexEntry entry = new CodexEntry();
            entry.setUser(user);
            entry.setWord(word);
            entry.setXpValue(10);
            codexEntryRepository.save(entry);
        }
    }

    private ReadingList createReadingList(User owner, String title)
    {
        ReadingList list = new ReadingList();
        list.setTitle(title);
        list.setOwner(owner);
        return readingListRepository.save(list);
    }

    private void createCompletedCircleBook(User user, Book completedBook)
    {
        BookCircle circle = createCircle(user, completedBook, CircleVisibility.ALL_MEMBERS);

        createCircleMember(circle, user, completedBook, CircleMemberRole.OWNER);
    }

    private BookCircle createCircle(User owner, Book book, CircleVisibility visibility)
    {
        BookCircle circle = new BookCircle();
        circle.setInviteCode("A" + Math.abs((owner.getKeycloakSub() + book.getTitle()).hashCode() % 100000));
        circle.setBookTitle(book.getTitle());
        circle.setBookAuthor(book.getAuthor());
        circle.setTotalPages(book.getTotalPages());
        circle.setMemberCap(5);
        circle.setVisibility(visibility);
        circle.setStatus(CircleStatus.ACTIVE);
        circle.setOwner(owner);
        return bookCircleRepository.save(circle);
    }

    private CircleMember createCircleMember(BookCircle circle, User user, Book book, CircleMemberRole role)
    {
        CircleMember member = new CircleMember();
        member.setCircle(circle);
        member.setUser(user);
        member.setBook(book);
        member.setRole(role);
        return circleMemberRepository.save(member);
    }
}
