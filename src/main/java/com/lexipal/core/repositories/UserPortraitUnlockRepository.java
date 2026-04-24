package com.lexipal.core.repositories;

import com.lexipal.core.entities.UserPortraitUnlock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPortraitUnlockRepository extends JpaRepository<UserPortraitUnlock, Long>
{
    @EntityGraph(attributePaths = "portrait")
    List<UserPortraitUnlock> findByUser_Id(Long userId);

    boolean existsByUser_IdAndPortrait_Id(Long userId, Long portraitId);
}
