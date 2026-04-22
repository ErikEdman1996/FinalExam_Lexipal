package com.lexipal.core.repositories;

import com.lexipal.core.entities.BookCircle;
import com.lexipal.core.entities.CircleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCircleRepository extends JpaRepository<BookCircle, Long>
{
    Optional<BookCircle> findByInviteCodeAndStatus(String inviteCode, CircleStatus status);

    boolean existsByInviteCode(String inviteCode);

    @Query("SELECT bc FROM BookCircle bc JOIN bc.members m " +
           "WHERE m.user.keycloakSub = :sub AND bc.status = 'ACTIVE'")
    List<BookCircle> findActiveCirclesForUser(@Param("sub") String sub);
}
