package com.lexipal.core.repositories;

import com.lexipal.core.entities.ReadingList;
import com.lexipal.core.entities.ReadingListStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingListRepository extends JpaRepository<ReadingList, Long>
{
    @Query("""
            SELECT rl FROM ReadingList rl
            WHERE rl.status = :status
              AND rl.starCount >= :minStars
            """)
    Page<ReadingList> browse(@Param("status") ReadingListStatus status,
                             @Param("minStars") int minStars,
                             Pageable pageable);

    @Query("""
            SELECT rl FROM ReadingList rl
            WHERE rl.status = :status
              AND rl.starCount >= :minStars
              AND LOWER(rl.title) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<ReadingList> browseWithQuery(@Param("status") ReadingListStatus status,
                                      @Param("minStars") int minStars,
                                      @Param("query") String query,
                                      Pageable pageable);

    List<ReadingList> findByOwner_KeycloakSubOrderByCreatedAtDesc(String keycloakSub);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReadingList rl SET rl.starCount = rl.starCount + 1 WHERE rl.id = :id")
    void incrementStarCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReadingList rl SET rl.starCount = CASE WHEN rl.starCount > 0 THEN rl.starCount - 1 ELSE 0 END WHERE rl.id = :id")
    void decrementStarCount(@Param("id") Long id);
}
