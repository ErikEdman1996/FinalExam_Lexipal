package com.lexipal.core.repositories;

import com.lexipal.core.entities.UserThemeUnlock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserThemeUnlockRepository extends JpaRepository<UserThemeUnlock, Long>
{
    @EntityGraph(attributePaths = "theme")
    List<UserThemeUnlock> findByUser_Id(Long userId);

    Optional<UserThemeUnlock> findByUser_IdAndTheme_Id(Long userId, Long themeId);

    boolean existsByUser_IdAndTheme_Id(Long userId, Long themeId);
}
