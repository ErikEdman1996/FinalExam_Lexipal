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
import java.time.LocalDateTime;

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

    @Query(value = """
            SELECT DISTINCT rl FROM ReadingList rl
            LEFT JOIN rl.genres g
            LEFT JOIN rl.books rlb
            LEFT JOIN rlb.catalogBook cb
            WHERE rl.status = :status
              AND rl.starCount >= :minStars
              AND (
                    LOWER(rl.title) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(rl.title) LIKE LOWER(CONCAT('%', :normalizedQuery, '%'))
                    OR g.genreNormalized LIKE CONCAT('%', :normalizedQuery, '%')
                    OR cb.titleNormalized LIKE CONCAT('%', :normalizedQuery, '%')
                    OR cb.authorNormalized LIKE CONCAT('%', :normalizedQuery, '%')
              )
            """,
            countQuery = """
            SELECT COUNT(DISTINCT rl) FROM ReadingList rl
            LEFT JOIN rl.genres g
            LEFT JOIN rl.books rlb
            LEFT JOIN rlb.catalogBook cb
            WHERE rl.status = :status
              AND rl.starCount >= :minStars
              AND (
                    LOWER(rl.title) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(rl.title) LIKE LOWER(CONCAT('%', :normalizedQuery, '%'))
                    OR g.genreNormalized LIKE CONCAT('%', :normalizedQuery, '%')
                    OR cb.titleNormalized LIKE CONCAT('%', :normalizedQuery, '%')
                    OR cb.authorNormalized LIKE CONCAT('%', :normalizedQuery, '%')
              )
            """)
    Page<ReadingList> browseWithQuery(@Param("status") ReadingListStatus status,
                                      @Param("minStars") int minStars,
                                      @Param("query") String query,
                                      @Param("normalizedQuery") String normalizedQuery,
                                      Pageable pageable);

    List<ReadingList> findByOwner_KeycloakSubOrderByCreatedAtDesc(String keycloakSub);

    @Query("""
            SELECT COALESCE(MAX(rl.starCount), 0) FROM ReadingList rl
            WHERE rl.owner.id = :ownerId
            """)
    int findMaxStarCountByOwnerId(@Param("ownerId") Long ownerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReadingList rl SET rl.starCount = rl.starCount + 1 WHERE rl.id = :id")
    void incrementStarCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReadingList rl SET rl.starCount = CASE WHEN rl.starCount > 0 THEN rl.starCount - 1 ELSE 0 END WHERE rl.id = :id")
    void decrementStarCount(@Param("id") Long id);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    @Query("SELECT MIN(rl.createdAt) FROM ReadingList rl WHERE rl.createdAt IS NOT NULL")
    LocalDateTime findEarliestCreatedAt();
}
