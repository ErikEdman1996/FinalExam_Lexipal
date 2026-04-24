package com.lexipal.core.repositories;
import com.lexipal.core.entities.Book;
import com.lexipal.core.repositories.projections.AdminDailyBookProjection;
import com.lexipal.core.repositories.projections.AdminTopBookProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
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

    @Query("""
            SELECT COUNT(b) FROM Book b
            WHERE b.status = com.lexipal.core.entities.Book.BookStatus.COMPLETE
              AND b.completedAt >= :start
              AND b.completedAt < :end
            """)
    long countCompletedBetween(@Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query("""
            SELECT COUNT(b) FROM Book b
            WHERE b.status = com.lexipal.core.entities.Book.BookStatus.COMPLETE
            """)
    long countAllCompleted();

    long countByUser_IdAndStatus(Long userId, Book.BookStatus status);

    @Query("""
            SELECT COUNT(b) FROM Book b
            WHERE b.status <> com.lexipal.core.entities.Book.BookStatus.DELETED
            """)
    long countAllNonDeleted();

    @Query("""
            SELECT MIN(b.completedAt) FROM Book b
            WHERE b.status = com.lexipal.core.entities.Book.BookStatus.COMPLETE
              AND b.completedAt IS NOT NULL
            """)
    LocalDateTime findEarliestCompletedAt();

    @Query(value = """
            SELECT cast(b.completed_at as date) as "date",
                   count(*) as "completedBooks"
            FROM books b
            WHERE b.status = 'COMPLETE'
              AND b.completed_at >= :start
              AND b.completed_at < :end
            GROUP BY cast(b.completed_at as date)
            ORDER BY cast(b.completed_at as date)
            """, nativeQuery = true)
    List<AdminDailyBookProjection> findDailyCompletedBooks(@Param("start") LocalDateTime start,
                                                           @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT min(b.title) as "title",
                   min(b.author) as "author",
                   coalesce(sum(case when b.status = 'COMPLETE' then 1 else 0 end), 0) as "completedCount",
                   coalesce(sum(case when s.status = 'COMPLETED' then s.pages_read else 0 end), 0) as "pagesRead"
            FROM books b
            LEFT JOIN sessions s ON s.book_id = b.id
            WHERE b.status <> 'DELETED'
            GROUP BY lower(trim(b.title)), lower(trim(b.author))
            ORDER BY coalesce(sum(case when b.status = 'COMPLETE' then 1 else 0 end), 0) DESC,
                     coalesce(sum(case when s.status = 'COMPLETED' then s.pages_read else 0 end), 0) DESC,
                     min(b.title) ASC
            LIMIT 10
            """, nativeQuery = true)
    List<AdminTopBookProjection> findTopBooks();
}
