package com.lexipal.core.repositories;

import com.lexipal.core.entities.NoteTagAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteTagAssignmentRepository extends JpaRepository<NoteTagAssignment, Long>
{
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM NoteTagAssignment a WHERE a.tag.id = :tagId")
    void deleteByTagId(@Param("tagId") Long tagId);
}
