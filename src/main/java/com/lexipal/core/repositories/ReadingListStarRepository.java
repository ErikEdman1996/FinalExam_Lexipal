package com.lexipal.core.repositories;

import com.lexipal.core.entities.ReadingListStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingListStarRepository extends JpaRepository<ReadingListStar, Long>
{
    boolean existsByList_IdAndUser_Id(Long listId, Long userId);

    @Modifying
    @Query("DELETE FROM ReadingListStar s WHERE s.list.id = :listId AND s.user.id = :userId")
    int deleteByListIdAndUserId(@Param("listId") Long listId, @Param("userId") Long userId);
}
