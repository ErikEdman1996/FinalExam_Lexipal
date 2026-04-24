package com.lexipal.core.repositories;

import com.lexipal.core.entities.ReadingListSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface ReadingListSubscriptionRepository extends JpaRepository<ReadingListSubscription, Long>
{
    Optional<ReadingListSubscription> findByList_IdAndUser_Id(Long listId, Long userId);

    boolean existsByList_IdAndUser_Id(Long listId, Long userId);

    List<ReadingListSubscription> findByUser_KeycloakSubOrderBySubscribedAtDesc(String keycloakSub);

    @Query(value = """
            SELECT coalesce(max(subscriber_count), 0)
            FROM (
                SELECT count(s.id) as subscriber_count
                FROM reading_lists rl
                LEFT JOIN reading_list_subscriptions s ON s.list_id = rl.id
                WHERE rl.owner_user_id = :ownerId
                GROUP BY rl.id
            ) counts
            """, nativeQuery = true)
    int findMaxSubscriberCountByOwnerId(@Param("ownerId") Long ownerId);

    @Query("""
            SELECT DISTINCT s FROM ReadingListSubscription s
            JOIN s.list rl
            JOIN rl.books rlb
            WHERE s.user.id = :userId
              AND rlb.catalogBook.id = :catalogBookId
              AND s.completedAt IS NULL
            """)
    List<ReadingListSubscription> findActiveSubscriptionsByCatalogBookForUser(
            @Param("userId") Long userId,
            @Param("catalogBookId") Long catalogBookId);

    long countBySubscribedAtGreaterThanEqualAndSubscribedAtLessThan(LocalDateTime start, LocalDateTime end);

    long countByCompletedAtGreaterThanEqualAndCompletedAtLessThan(LocalDateTime start, LocalDateTime end);

    @Query("SELECT MIN(s.subscribedAt) FROM ReadingListSubscription s WHERE s.subscribedAt IS NOT NULL")
    LocalDateTime findEarliestSubscribedAt();

    @Query("SELECT MIN(s.completedAt) FROM ReadingListSubscription s WHERE s.completedAt IS NOT NULL")
    LocalDateTime findEarliestCompletedAt();
}
