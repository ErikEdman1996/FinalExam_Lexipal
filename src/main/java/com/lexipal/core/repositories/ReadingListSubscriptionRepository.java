package com.lexipal.core.repositories;

import com.lexipal.core.entities.ReadingListSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingListSubscriptionRepository extends JpaRepository<ReadingListSubscription, Long>
{
    Optional<ReadingListSubscription> findByList_IdAndUser_Id(Long listId, Long userId);

    boolean existsByList_IdAndUser_Id(Long listId, Long userId);

    List<ReadingListSubscription> findByUser_KeycloakSubOrderBySubscribedAtDesc(String keycloakSub);

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
}
