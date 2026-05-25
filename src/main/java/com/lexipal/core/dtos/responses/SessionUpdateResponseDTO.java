package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;
import java.util.List;

public class SessionUpdateResponseDTO
{
    private Long id;
    private Long bookId;
    private Long durationSeconds;
    private Integer startPage;
    private Integer endPage;
    private Integer pagesRead;
    private String reflection;
    private String status;
    private LocalDateTime sessionDate;
    private LocalDateTime completedAt;
    private XpUpdateResponseDTO xpUpdate;
    private DailyQuestResponseDTO dailyQuest;
    private List<AchievementResponseDTO> newlyUnlockedAchievements = List.of();

    public SessionUpdateResponseDTO()
    {

    }

    public SessionUpdateResponseDTO(Long id, Long bookId, Long durationSeconds, Integer startPage, Integer endPage,
                                    Integer pagesRead, String reflection, String status, LocalDateTime sessionDate,
                                    LocalDateTime completedAt, XpUpdateResponseDTO xpUpdate)
    {
        this(id, bookId, durationSeconds, startPage, endPage, pagesRead, reflection, status, sessionDate,
                completedAt, xpUpdate, List.of());
    }

    public SessionUpdateResponseDTO(Long id, Long bookId, Long durationSeconds, Integer startPage, Integer endPage,
                                    Integer pagesRead, String reflection, String status, LocalDateTime sessionDate,
                                    LocalDateTime completedAt, XpUpdateResponseDTO xpUpdate,
                                    List<AchievementResponseDTO> newlyUnlockedAchievements)
    {
        this(id, bookId, durationSeconds, startPage, endPage, pagesRead, reflection, status, sessionDate,
                completedAt, xpUpdate, null, newlyUnlockedAchievements);
    }

    public SessionUpdateResponseDTO(Long id, Long bookId, Long durationSeconds, Integer startPage, Integer endPage,
                                    Integer pagesRead, String reflection, String status, LocalDateTime sessionDate,
                                    LocalDateTime completedAt, XpUpdateResponseDTO xpUpdate,
                                    DailyQuestResponseDTO dailyQuest,
                                    List<AchievementResponseDTO> newlyUnlockedAchievements)
    {
        this.id = id;
        this.bookId = bookId;
        this.durationSeconds = durationSeconds;
        this.startPage = startPage;
        this.endPage = endPage;
        this.pagesRead = pagesRead;
        this.reflection = reflection;
        this.status = status;
        this.sessionDate = sessionDate;
        this.completedAt = completedAt;
        this.xpUpdate = xpUpdate;
        this.dailyQuest = dailyQuest;
        setNewlyUnlockedAchievements(newlyUnlockedAchievements);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }

    public Integer getEndPage() {
        return endPage;
    }

    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
    }

    public Integer getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(Integer pagesRead) {
        this.pagesRead = pagesRead;
    }

    public String getReflection() {
        return reflection;
    }

    public void setReflection(String reflection) {
        this.reflection = reflection;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalDateTime getCompletedAt()
    {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt)
    {
        this.completedAt = completedAt;
    }

    public XpUpdateResponseDTO getXpUpdate()
    {
        return xpUpdate;
    }

    public void setXpUpdate(XpUpdateResponseDTO xpUpdate)
    {
        this.xpUpdate = xpUpdate;
    }

    public DailyQuestResponseDTO getDailyQuest()
    {
        return dailyQuest;
    }

    public void setDailyQuest(DailyQuestResponseDTO dailyQuest)
    {
        this.dailyQuest = dailyQuest;
    }

    public List<AchievementResponseDTO> getNewlyUnlockedAchievements()
    {
        return newlyUnlockedAchievements;
    }

    public void setNewlyUnlockedAchievements(List<AchievementResponseDTO> newlyUnlockedAchievements)
    {
        this.newlyUnlockedAchievements = newlyUnlockedAchievements == null ? List.of() : newlyUnlockedAchievements;
    }
}
