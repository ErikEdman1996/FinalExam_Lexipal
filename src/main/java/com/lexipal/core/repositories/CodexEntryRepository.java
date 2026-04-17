package com.lexipal.core.repositories;

import com.lexipal.core.entities.CodexEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodexEntryRepository extends JpaRepository<CodexEntry, Long>
{
    Optional<CodexEntry> findByUser_IdAndWord_Lemma(Long userId, String word);

    List<CodexEntry> findAllByUser_Id(Long userId);

    @Query("SELECT c FROM CodexEntry c WHERE c.user.keycloakSub = :sub")
    List<CodexEntry> findAllBySub(@Param("sub") String sub);
}
