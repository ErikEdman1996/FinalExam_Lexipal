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
}
