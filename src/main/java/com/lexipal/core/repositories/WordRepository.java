package com.lexipal.core.repositories;

import com.lexipal.core.entities.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long>
{
    Optional<Word> findByLemma(String lemma);

    List<Word> findAllByOrderByIdAsc();
}
