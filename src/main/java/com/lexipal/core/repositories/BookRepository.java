package com.lexipal.core.repositories;
import com.lexipal.core.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>
{
    @Query("SELECT b FROM Book b WHERE b.user.keycloakSub = :sub AND b.status != 'DELETED'")
    List<Book> findAllActiveBooks(@Param("sub") String sub);

    @Query("""
            SELECT DISTINCT cb.id FROM Book b, CatalogBook cb
            WHERE b.user.id = :userId
              AND b.status = com.lexipal.core.entities.Book.BookStatus.COMPLETE
              AND LOWER(TRIM(b.title)) = cb.titleNormalized
              AND LOWER(TRIM(b.author)) = cb.authorNormalized
              AND cb.id IN :catalogBookIds
            """)
    List<Long> findCompletedCatalogBookIdsForUser(@Param("userId") Long userId,
                                                  @Param("catalogBookIds") List<Long> catalogBookIds);

    @Query("""
            SELECT DISTINCT cb.id FROM Book b, CatalogBook cb
            WHERE b.user.id = :userId
              AND b.status <> com.lexipal.core.entities.Book.BookStatus.DELETED
              AND LOWER(TRIM(b.title)) = cb.titleNormalized
              AND LOWER(TRIM(b.author)) = cb.authorNormalized
              AND cb.id IN :catalogBookIds
            """)
    List<Long> findOwnedCatalogBookIdsForUser(@Param("userId") Long userId,
                                              @Param("catalogBookIds") List<Long> catalogBookIds);

    @Query("""
            SELECT cb.id FROM CatalogBook cb
            WHERE cb.titleNormalized = :titleNormalized
              AND cb.authorNormalized = :authorNormalized
            """)
    List<Long> findCatalogBookIdsByNormalized(@Param("titleNormalized") String titleNormalized,
                                              @Param("authorNormalized") String authorNormalized);
}
