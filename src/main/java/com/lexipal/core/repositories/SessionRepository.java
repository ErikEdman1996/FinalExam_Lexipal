package com.lexipal.core.repositories;

import com.lexipal.core.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>
{
    @Query("SELECT MAX(s.endPage) FROM Session s WHERE s.book.id = :bookId")
    Integer findMaxEndPageByBookId(@Param("bookId") Long bookId);

    List<Session> findByBookIdOrderBySessionDateDesc(Long bookId);
}
