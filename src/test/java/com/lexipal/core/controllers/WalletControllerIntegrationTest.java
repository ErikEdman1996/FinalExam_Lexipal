package com.lexipal.core.controllers;

import com.lexipal.core.IntegrationTestBase;
import com.lexipal.core.dtos.requests.SessionCompleteRequestDTO;
import com.lexipal.core.dtos.requests.ThemeSelectionRequestDTO;
import com.lexipal.core.dtos.requests.WordLookupCreateRequestDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.CurrencyTransaction;
import com.lexipal.core.entities.CurrencyTransactionReason;
import com.lexipal.core.entities.CurrencyTransactionType;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.Word;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.CurrencyTransactionRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import com.lexipal.core.repositories.UserThemeUnlockRepository;
import com.lexipal.core.repositories.WordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WalletControllerIntegrationTest extends IntegrationTestBase
{
    private static final String WALLET_URL = "/lexipal/api/v1/wallet/me";
    private static final String PURCHASE_FREEZE_URL = "/lexipal/api/v1/streak-freezes/me/purchase";
    private static final String STORE_URL = "/lexipal/api/v1/store/me";
    private static final String STORE_ITEMS_URL = "/lexipal/api/v1/store/items/";
    private static final String THEMES_URL = "/lexipal/api/v1/themes/me";

    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private WordRepository wordRepository;
    @Autowired private CurrencyTransactionRepository currencyTransactionRepository;
    @Autowired private UserThemeUnlockRepository userThemeUnlockRepository;

    @Test
    @DisplayName("Store catalog returns freeze and paid theme items")
    void storeCatalog_returnsFreezeAndThemeItems() throws Exception
    {
        User user = createUser("store-catalog-sub");

        mockMvc.perform(get(STORE_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallet.coinBalance").value(0))
                .andExpect(jsonPath("$.items", hasSize(9)))
                .andExpect(jsonPath("$.items[?(@.code == 'streak_freeze')].priceCoins").value(hasItem(100)))
                .andExpect(jsonPath("$.items[?(@.code == 'streak_freeze')].itemType")
                        .value(hasItem("STREAK_FREEZE")))
                .andExpect(jsonPath("$.items[?(@.code == 'vellum_dawn')].priceCoins").value(hasItem(100)))
                .andExpect(jsonPath("$.items[?(@.code == 'sage_margin')].priceCoins").value(hasItem(100)))
                .andExpect(jsonPath("$.items[?(@.code == 'harbor_folio')].priceCoins").value(hasItem(100)))
                .andExpect(jsonPath("$.items[?(@.code == 'rose_bookmark')].priceCoins").value(hasItem(100)))
                .andExpect(jsonPath("$.items[?(@.code == 'midnight_archive')].priceCoins").value(hasItem(100)))
                .andExpect(jsonPath("$.items[?(@.code == 'mothlight_nocturne')].priceCoins").value(hasItem(100)))
                .andExpect(jsonPath("$.items[?(@.code == 'forest_canopy')].priceCoins").value(hasItem(100)))
                .andExpect(jsonPath("$.items[?(@.code == 'plum_afterword')].priceCoins").value(hasItem(100)));
    }

    @Test
    @DisplayName("Theme purchase unlocks once and selected paid theme appears in profile")
    void themePurchase_unlocksAndSelectionPersists() throws Exception
    {
        User user = createUser("store-theme-sub");
        user.setCoinBalance(100);
        userRepository.save(user);

        mockMvc.perform(post(STORE_ITEMS_URL + "vellum_dawn/purchase")
                        .with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallet.coinBalance").value(0))
                .andExpect(jsonPath("$.item.code").value("vellum_dawn"))
                .andExpect(jsonPath("$.item.themeCode").value("vellum_dawn"))
                .andExpect(jsonPath("$.item.owned").value(true))
                .andExpect(jsonPath("$.item.selected").value(false));

        mockMvc.perform(get(THEMES_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'vellum_dawn')].unlocked").value(hasItem(true)))
                .andExpect(jsonPath("$[?(@.code == 'vellum_dawn')].selected").value(hasItem(false)));

        mockMvc.perform(patch(THEMES_URL + "/selected")
                        .with(jwtWithSub(user.getKeycloakSub()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ThemeSelectionRequestDTO("vellum_dawn"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("vellum_dawn"))
                .andExpect(jsonPath("$.selected").value(true));

        mockMvc.perform(get("/lexipal/api/v1/profile/me").with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedTheme.code").value("vellum_dawn"))
                .andExpect(jsonPath("$.selectedTheme.unlocked").value(true));

        assertEquals(1, userThemeUnlockRepository.findByUser_Id(user.getId()).size());
        List<CurrencyTransaction> transactions =
                currencyTransactionRepository.findByUser_IdOrderByCreatedAtAscIdAsc(user.getId());
        assertEquals(1, transactions.size());
        assertEquals(CurrencyTransactionReason.THEME_PURCHASE, transactions.get(0).getReason());
        assertEquals(100, transactions.get(0).getAmount());
    }

    @Test
    @DisplayName("Theme purchase rejects insufficient funds without unlocking")
    void themePurchase_rejectsInsufficientFunds() throws Exception
    {
        User user = createUser("store-theme-insufficient-sub");
        user.setCoinBalance(99);
        userRepository.save(user);

        mockMvc.perform(post(STORE_ITEMS_URL + "forest_canopy/purchase")
                        .with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You do not have enough coins."));

        mockMvc.perform(get(WALLET_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinBalance").value(99));
        assertEquals(0, userThemeUnlockRepository.findByUser_Id(user.getId()).size());
        assertEquals(0, currencyTransactionRepository.findByUser_IdOrderByCreatedAtAscIdAsc(user.getId()).size());
    }

    @Test
    @DisplayName("Theme purchase rejects duplicate ownership without spending twice")
    void themePurchase_rejectsDuplicateOwnership() throws Exception
    {
        User user = createUser("store-theme-duplicate-sub");
        user.setCoinBalance(200);
        userRepository.save(user);

        mockMvc.perform(post(STORE_ITEMS_URL + "plum_afterword/purchase")
                        .with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk());

        mockMvc.perform(post(STORE_ITEMS_URL + "plum_afterword/purchase")
                        .with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Theme is already unlocked."));

        mockMvc.perform(get(WALLET_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinBalance").value(100));
        assertEquals(1, userThemeUnlockRepository.findByUser_Id(user.getId()).size());
        assertEquals(1, currencyTransactionRepository.findByUser_IdOrderByCreatedAtAscIdAsc(user.getId()).size());
    }

    @Test
    @DisplayName("Free default themes can be selected")
    void freeTheme_canBeSelected() throws Exception
    {
        User user = createUser("store-free-theme-sub");

        mockMvc.perform(patch(THEMES_URL + "/selected")
                        .with(jwtWithSub(user.getKeycloakSub()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ThemeSelectionRequestDTO("default_dark"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("default_dark"))
                .andExpect(jsonPath("$.unlocked").value(true))
                .andExpect(jsonPath("$.priceCoins").value(0));

        mockMvc.perform(get("/lexipal/api/v1/profile/me").with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedTheme.code").value("default_dark"));
    }

    @Test
    @DisplayName("Locked paid themes cannot be selected")
    void lockedPaidTheme_cannotBeSelected() throws Exception
    {
        User user = createUser("store-locked-theme-sub");

        mockMvc.perform(patch(THEMES_URL + "/selected")
                        .with(jwtWithSub(user.getKeycloakSub()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ThemeSelectionRequestDTO("midnight_archive"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Theme is locked."));
    }

    @Test
    @DisplayName("Wallet starts empty and session completion earns coins")
    void sessionCompletion_awardsWalletCoins() throws Exception
    {
        User user = createUser("wallet-session-sub");
        Book book = createBook(user, "Wallet Session Book");
        Session session = createInProgressSession(user, book);

        mockMvc.perform(get(WALLET_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinBalance").value(0))
                .andExpect(jsonPath("$.availableStreakFreezes").value(0));

        mockMvc.perform(post("/lexipal/api/v1/sessions/" + session.getId() + "/complete")
                        .with(jwtWithSub(user.getKeycloakSub()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SessionCompleteRequestDTO(12, 600L, "The wallet earns from reading."))))
                .andExpect(status().isOk());

        mockMvc.perform(get(WALLET_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinBalance").value(25));

        List<CurrencyTransaction> transactions =
                currencyTransactionRepository.findByUser_IdOrderByCreatedAtAscIdAsc(user.getId());
        assertEquals(1, transactions.size());
        assertEquals(CurrencyTransactionType.EARN, transactions.get(0).getType());
        assertEquals(CurrencyTransactionReason.SESSION_COMPLETED, transactions.get(0).getReason());
        assertEquals(25, transactions.get(0).getAmount());
        assertEquals(25, transactions.get(0).getBalanceAfter());
    }

    @Test
    @DisplayName("Streak freeze purchase spends coins and returns wallet state")
    void purchaseStreakFreeze_spendsCoinsAndAddsToken() throws Exception
    {
        User user = createUser("wallet-freeze-sub");
        user.setCoinBalance(100);
        userRepository.save(user);

        mockMvc.perform(post(PURCHASE_FREEZE_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinBalance").value(0))
                .andExpect(jsonPath("$.availableStreakFreezes").value(1));

        mockMvc.perform(get("/lexipal/api/v1/profile/me").with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallet.coinBalance").value(0))
                .andExpect(jsonPath("$.wallet.availableStreakFreezes").value(1));

        List<CurrencyTransaction> transactions =
                currencyTransactionRepository.findByUser_IdOrderByCreatedAtAscIdAsc(user.getId());
        assertEquals(1, transactions.size());
        assertEquals(CurrencyTransactionType.SPEND, transactions.get(0).getType());
        assertEquals(CurrencyTransactionReason.STREAK_FREEZE_PURCHASE, transactions.get(0).getReason());
        assertEquals(100, transactions.get(0).getAmount());
        assertEquals(0, transactions.get(0).getBalanceAfter());
    }

    @Test
    @DisplayName("Streak freeze purchase rejects insufficient funds")
    void purchaseStreakFreeze_rejectsInsufficientBalance() throws Exception
    {
        User user = createUser("wallet-insufficient-sub");
        user.setCoinBalance(99);
        userRepository.save(user);

        mockMvc.perform(post(PURCHASE_FREEZE_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You do not have enough coins."));

        mockMvc.perform(get(WALLET_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinBalance").value(99))
                .andExpect(jsonPath("$.availableStreakFreezes").value(0));
        assertEquals(0, currencyTransactionRepository.findByUser_IdOrderByCreatedAtAscIdAsc(user.getId()).size());
    }

    @Test
    @DisplayName("Word lookups do not award XP or coins")
    void wordLookup_doesNotAwardXpOrCoins() throws Exception
    {
        User user = createUser("wallet-lookup-sub");
        Book book = createBook(user, "Wallet Lookup Book");
        Session session = createInProgressSession(user, book);
        Word word = new Word();
        word.setLemma("explain");
        wordRepository.save(word);

        mockMvc.perform(post("/lexipal/api/v1/word-lookups/" + session.getId())
                        .with(jwtWithSub(user.getKeycloakSub()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WordLookupCreateRequestDTO("explain"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupSucceeded").value(true))
                .andExpect(jsonPath("$.isNewLookup").value(true))
                .andExpect(jsonPath("$.*", hasSize(3)));

        mockMvc.perform(post("/lexipal/api/v1/word-lookups/" + session.getId())
                        .with(jwtWithSub(user.getKeycloakSub()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WordLookupCreateRequestDTO("explain"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupSucceeded").value(true))
                .andExpect(jsonPath("$.isNewLookup").value(false));

        mockMvc.perform(get(WALLET_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinBalance").value(0));
        assertEquals(0, currencyTransactionRepository.findByUser_IdOrderByCreatedAtAscIdAsc(user.getId()).size());
    }

    private User createUser(String sub)
    {
        User user = new User();
        user.setKeycloakSub(sub);
        return userRepository.save(user);
    }

    private Book createBook(User user, String title)
    {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Wallet Author");
        book.setReadPages(0);
        book.setTotalPages(200);
        book.setStatus(Book.BookStatus.ACTIVE);
        book.setUser(user);
        return bookRepository.save(book);
    }

    private Session createInProgressSession(User user, Book book)
    {
        Session session = new Session();
        session.setUser(user);
        session.setBook(book);
        session.setStartPage(1);
        session.setPagesRead(0);
        session.setStatus(Session.SessionStatus.IN_PROGRESS);
        return sessionRepository.save(session);
    }
}
