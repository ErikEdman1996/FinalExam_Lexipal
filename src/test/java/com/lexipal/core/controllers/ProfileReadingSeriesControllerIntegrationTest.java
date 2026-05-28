package com.lexipal.core.controllers;

import com.lexipal.core.IntegrationTestBase;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileReadingSeriesControllerIntegrationTest extends IntegrationTestBase
{
    private static final String READING_SERIES_URL = "/lexipal/api/v1/profile/me/reading-series";
    private static final ZoneId ANALYTICS_ZONE = ZoneId.of("Europe/Stockholm");

    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private SessionRepository sessionRepository;

    @Test
    @DisplayName("GET /profile/me/reading-series returns this week daily bucket totals for current user")
    void getReadingSeries_thisWeekReturnsDailyBucketTotals() throws Exception
    {
        LocalDate today = LocalDate.now(ANALYTICS_ZONE);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        User user = createUser("reading-series-week-sub");
        Book book = createBook(user);
        User otherUser = createUser("reading-series-week-other-sub");
        Book otherBook = createBook(otherUser);

        createCompletedSession(user, book, today.atTime(9, 0), 10);
        createCompletedSession(user, book, today.atTime(18, 0), 8);
        createCompletedSession(user, book, monday.minusDays(1).atTime(10, 0), 99);
        createInProgressSession(user, book);
        createCompletedSession(otherUser, otherBook, today.atTime(12, 0), 100);

        if (monday.isBefore(today))
        {
            createCompletedSession(user, book, monday.atTime(8, 0), 7);
        }

        ResultActions result = mockMvc.perform(get(READING_SERIES_URL + "?range=THIS_WEEK")
                        .with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range").value("THIS_WEEK"))
                .andExpect(jsonPath("$.startDate").value(monday.toString()))
                .andExpect(jsonPath("$.endDate").value(today.toString()))
                .andExpect(jsonPath("$.timezone").value(ANALYTICS_ZONE.getId()))
                .andExpect(jsonPath("$.bucketType").value("DAY"))
                .andExpect(jsonPath("$.points", hasSize((int) ChronoUnit.DAYS.between(monday, today) + 1)))
                .andExpect(jsonPath("$.points[?(@.date == '" + today + "')].pagesRead").value(hasItem(18)));

        if (monday.isBefore(today))
        {
            result.andExpect(jsonPath("$.points[?(@.date == '" + monday + "')].pagesRead").value(hasItem(7)));
        }

        LocalDate zeroDate = monday.plusDays(1);
        if (zeroDate.isBefore(today))
        {
            result.andExpect(jsonPath("$.points[?(@.date == '" + zeroDate + "')].pagesRead").value(hasItem(0)));
        }
    }

    @Test
    @DisplayName("GET /profile/me/reading-series returns this month daily buckets through today")
    void getReadingSeries_thisMonthExcludesSessionsBeforeMonth() throws Exception
    {
        LocalDate today = LocalDate.now(ANALYTICS_ZONE);
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        User user = createUser("reading-series-month-sub");
        Book book = createBook(user);

        createCompletedSession(user, book, firstDayOfMonth.minusDays(1).atTime(12, 0), 50);
        createCompletedSession(user, book, today.atTime(13, 0), 6);

        mockMvc.perform(get(READING_SERIES_URL + "?range=THIS_MONTH")
                        .with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range").value("THIS_MONTH"))
                .andExpect(jsonPath("$.startDate").value(firstDayOfMonth.toString()))
                .andExpect(jsonPath("$.endDate").value(today.toString()))
                .andExpect(jsonPath("$.bucketType").value("DAY"))
                .andExpect(jsonPath("$.points", hasSize(today.getDayOfMonth())))
                .andExpect(jsonPath("$.points[0].date").value(firstDayOfMonth.toString()))
                .andExpect(jsonPath("$.points[?(@.date == '" + today + "')].pagesRead").value(hasItem(6)));
    }

    @Test
    @DisplayName("GET /profile/me/reading-series returns this year monthly buckets")
    void getReadingSeries_thisYearReturnsMonthlyBuckets() throws Exception
    {
        LocalDate today = LocalDate.now(ANALYTICS_ZONE);
        LocalDate firstDayOfYear = today.withDayOfYear(1);
        LocalDate currentMonth = today.withDayOfMonth(1);
        User user = createUser("reading-series-year-sub");
        Book book = createBook(user);

        createCompletedSession(user, book, firstDayOfYear.minusDays(1).atTime(12, 0), 100);
        createCompletedSession(user, book, today.atTime(14, 0), 13);

        mockMvc.perform(get(READING_SERIES_URL + "?range=THIS_YEAR")
                        .with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range").value("THIS_YEAR"))
                .andExpect(jsonPath("$.startDate").value(firstDayOfYear.toString()))
                .andExpect(jsonPath("$.endDate").value(today.toString()))
                .andExpect(jsonPath("$.bucketType").value("MONTH"))
                .andExpect(jsonPath("$.points", hasSize(today.getMonthValue())))
                .andExpect(jsonPath("$.points[0].date").value(firstDayOfYear.toString()))
                .andExpect(jsonPath("$.points[?(@.date == '" + currentMonth + "')].pagesRead").value(hasItem(13)));
    }

    @Test
    @DisplayName("GET /profile/me/reading-series all time uses current user's earliest completed session")
    void getReadingSeries_allTimeUsesCurrentUserEarliestCompletedSession() throws Exception
    {
        LocalDate today = LocalDate.now(ANALYTICS_ZONE);
        LocalDate earliestDate = today.minusMonths(2).withDayOfMonth(15);
        LocalDate earliestMonth = earliestDate.withDayOfMonth(1);
        LocalDate zeroMonth = earliestMonth.plusMonths(1);
        LocalDate currentMonth = today.withDayOfMonth(1);
        User user = createUser("reading-series-all-time-sub");
        Book book = createBook(user);
        User otherUser = createUser("reading-series-all-time-other-sub");
        Book otherBook = createBook(otherUser);

        createCompletedSession(otherUser, otherBook, earliestDate.minusYears(2).atTime(12, 0), 500);
        createCompletedSession(user, book, earliestDate.atTime(10, 0), 11);
        createCompletedSession(user, book, today.atTime(15, 0), 7);
        createInProgressSession(user, book);

        mockMvc.perform(get(READING_SERIES_URL + "?range=ALL_TIME")
                        .with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range").value("ALL_TIME"))
                .andExpect(jsonPath("$.startDate").value(earliestDate.toString()))
                .andExpect(jsonPath("$.endDate").value(today.toString()))
                .andExpect(jsonPath("$.bucketType").value("MONTH"))
                .andExpect(jsonPath("$.points", hasSize(3)))
                .andExpect(jsonPath("$.points[?(@.date == '" + earliestMonth + "')].pagesRead").value(hasItem(11)))
                .andExpect(jsonPath("$.points[?(@.date == '" + zeroMonth + "')].pagesRead").value(hasItem(0)))
                .andExpect(jsonPath("$.points[?(@.date == '" + currentMonth + "')].pagesRead").value(hasItem(7)));
    }

    @Test
    @DisplayName("GET /profile/me/reading-series all time returns current month zero for users without completed sessions")
    void getReadingSeries_allTimeWithoutCompletedSessionsReturnsCurrentMonthZero() throws Exception
    {
        LocalDate today = LocalDate.now(ANALYTICS_ZONE);
        LocalDate currentMonth = today.withDayOfMonth(1);
        User user = createUser("reading-series-empty-sub");

        mockMvc.perform(get(READING_SERIES_URL + "?range=ALL_TIME")
                        .with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range").value("ALL_TIME"))
                .andExpect(jsonPath("$.startDate").value(today.toString()))
                .andExpect(jsonPath("$.endDate").value(today.toString()))
                .andExpect(jsonPath("$.bucketType").value("MONTH"))
                .andExpect(jsonPath("$.points", hasSize(1)))
                .andExpect(jsonPath("$.points[0].date").value(currentMonth.toString()))
                .andExpect(jsonPath("$.points[0].pagesRead").value(0));
    }

    @Test
    @DisplayName("GET /profile/me/reading-series rejects invalid or missing ranges")
    void getReadingSeries_rejectsInvalidOrMissingRanges() throws Exception
    {
        String sub = "reading-series-invalid-range-sub";

        mockMvc.perform(get(READING_SERIES_URL + "?range=LAST_7_DAYS").with(jwtWithSub(sub)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(READING_SERIES_URL).with(jwtWithSub(sub)))
                .andExpect(status().isBadRequest());
    }

    private User createUser(String keycloakSub)
    {
        User user = new User();
        user.setKeycloakSub(keycloakSub);
        return userRepository.save(user);
    }

    private Book createBook(User user)
    {
        Book book = new Book();
        book.setTitle("Reading Series Book");
        book.setAuthor("Author");
        book.setTotalPages(500);
        book.setUser(user);
        return bookRepository.save(book);
    }

    private void createCompletedSession(User user, Book book, LocalDateTime completedAt, int pagesRead)
    {
        Session session = new Session();
        session.setUser(user);
        session.setBook(book);
        session.setStartPage(0);
        session.setEndPage(pagesRead);
        session.setPagesRead(pagesRead);
        session.setDurationSeconds(600L);
        session.setReflection("A completed reading session.");
        session.setStatus(Session.SessionStatus.COMPLETED);
        session.setSessionDate(completedAt.minusMinutes(30));
        session.setCompletedAt(completedAt);
        sessionRepository.save(session);
    }

    private void createInProgressSession(User user, Book book)
    {
        Session session = new Session();
        session.setUser(user);
        session.setBook(book);
        session.setStartPage(0);
        session.setPagesRead(25);
        session.setStatus(Session.SessionStatus.IN_PROGRESS);
        session.setSessionDate(LocalDate.now(ANALYTICS_ZONE).atTime(9, 0));
        sessionRepository.save(session);
    }
}
