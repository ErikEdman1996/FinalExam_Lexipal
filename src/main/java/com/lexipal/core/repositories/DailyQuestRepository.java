package com.lexipal.core.repositories;

import com.lexipal.core.entities.DailyQuest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyQuestRepository extends JpaRepository<DailyQuest, Long>
{
    Optional<DailyQuest> findByUser_IdAndQuestDate(Long userId, LocalDate questDate);

    Optional<DailyQuest> findFirstByUser_IdAndQuestDateLessThanOrderByQuestDateDesc(Long userId, LocalDate questDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from DailyQuest q where q.id = :id")
    Optional<DailyQuest> findByIdForUpdate(@Param("id") Long id);
}
