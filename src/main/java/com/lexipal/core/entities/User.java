package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "users")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keycloakSub;

    @Column(length = 20)
    private String nickname;

    @Column(name = "nickname_normalized", unique = true, length = 20)
    private String nicknameNormalized;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column
    private Integer totalPagesRead;

    @Column
    private Integer totalBooksRead;

    @Column
    private Integer totalXp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_portrait_id")
    private PortraitDefinition selectedPortrait;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-books")
    private List<Book> books = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-sessions")
    private List<Session> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-codexEntries")
    private List<CodexEntry> codexEntries = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-wordLookups")
    private List<WordLookup> wordLookups = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-dailyActivities")
    private List<DailyActivity> dailyActivities = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-challengeAttempts")
    private List<ChallengeAttempt> challengeAttempts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-notes")
    private List<Note> notes = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-streakMetaData")
    private StreakMetaData streakMetaData;

    public enum UserStatus
    {
        ACTIVE,
        INACTIVE,
        DELETED
    }

    public User()
    {

    }

    public User(String keycloakSub, LocalDateTime createdAt, UserStatus status,
                Integer totalPagesRead, Integer totalBooksRead, List<Book> books, List<Session> sessions,
                List<CodexEntry> codexEntries)
    {
        this.keycloakSub = keycloakSub;
        this.createdAt = createdAt;
        this.status = status;
        this.totalPagesRead = totalPagesRead;
        this.totalBooksRead = totalBooksRead;
        this.books = books;
        this.sessions = sessions;
        this.codexEntries = codexEntries;
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        this.status = UserStatus.ACTIVE;
        this.totalPagesRead = 0;
        this.totalBooksRead = 0;
        this.totalXp = 0;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getKeycloakSub() {
        return keycloakSub;
    }

    public void setKeycloakSub(String keycloakSub) {
        this.keycloakSub = keycloakSub;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        this.nicknameNormalized = nickname == null ? null : nickname.toLowerCase(Locale.ROOT);
    }

    public String getNicknameNormalized() {
        return nicknameNormalized;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public UserStatus getStatus()
    {
        return status;
    }

    public void setStatus(UserStatus status)
    {
        this.status = status;
    }

    public Integer getTotalPagesRead()
    {
        return totalPagesRead;
    }

    public void setTotalPagesRead(Integer totalPagesRead)
    {
        this.totalPagesRead = totalPagesRead;
    }

    public Integer getTotalBooksRead()
    {
        return totalBooksRead;
    }

    public void setTotalBooksRead(Integer totalBooksRead)
    {
        this.totalBooksRead = totalBooksRead;
    }

    public Integer getTotalXp()
    {
        return totalXp;
    }

    public void setTotalXp(Integer totalXp)
    {
        this.totalXp = totalXp;
    }

    public PortraitDefinition getSelectedPortrait() {
        return selectedPortrait;
    }

    public void setSelectedPortrait(PortraitDefinition selectedPortrait) {
        this.selectedPortrait = selectedPortrait;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }

    public List<CodexEntry> getCodexEntries() {
        return codexEntries;
    }

    public void setCodexEntries(List<CodexEntry> codexEntries) {
        this.codexEntries = codexEntries;
    }

    public List<WordLookup> getWordLookups()
    {
        return wordLookups;
    }

    public void setWordLookups(List<WordLookup> wordLookups)
    {
        this.wordLookups = wordLookups;
    }

    public List<DailyActivity> getDailyActivities() {
        return dailyActivities;
    }

    public void setDailyActivities(List<DailyActivity> dailyActivities) {
        this.dailyActivities = dailyActivities;
    }

    public List<ChallengeAttempt> getChallengeAttempts() {
        return challengeAttempts;
    }

    public void setChallengeAttempts(List<ChallengeAttempt> challengeAttempts) {
        this.challengeAttempts = challengeAttempts;
    }

    public StreakMetaData getStreakMetaData() {
        return streakMetaData;
    }

    public void setStreakMetaData(StreakMetaData streakMetaData) {
        this.streakMetaData = streakMetaData;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
}
