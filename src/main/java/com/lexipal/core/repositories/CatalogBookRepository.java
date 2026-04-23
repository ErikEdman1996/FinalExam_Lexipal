package com.lexipal.core.repositories;

import com.lexipal.core.entities.CatalogBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogBookRepository extends JpaRepository<CatalogBook, Long>
{
    Optional<CatalogBook> findByTitleNormalizedAndAuthorNormalized(String titleNormalized, String authorNormalized);
}
