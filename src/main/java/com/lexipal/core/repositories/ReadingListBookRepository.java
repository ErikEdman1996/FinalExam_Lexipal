package com.lexipal.core.repositories;

import com.lexipal.core.entities.ReadingListBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingListBookRepository extends JpaRepository<ReadingListBook, Long>
{
    List<ReadingListBook> findByList_IdOrderByPositionAsc(Long listId);

    long countByList_Id(Long listId);
}
