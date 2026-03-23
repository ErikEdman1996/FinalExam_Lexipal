package com.lexipal.core.repositories;

import com.lexipal.core.entities.CapturedWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CapturedWordRepository extends JpaRepository<CapturedWord, Long>
{
    Optional<CapturedWord> findByUser_IdAndWord_Lemma(Long userId, String word);
}
