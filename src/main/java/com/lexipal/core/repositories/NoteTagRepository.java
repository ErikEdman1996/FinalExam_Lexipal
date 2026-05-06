package com.lexipal.core.repositories;

import com.lexipal.core.dtos.responses.NoteTagUsageResponseDTO;
import com.lexipal.core.entities.NoteTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteTagRepository extends JpaRepository<NoteTag, Long>
{
    Optional<NoteTag> findByUser_IdAndNameNormalized(Long userId, String nameNormalized);

    List<NoteTag> findAllByIdInAndUser_Id(Collection<Long> ids, Long userId);

    @Query("""
            SELECT new com.lexipal.core.dtos.responses.NoteTagUsageResponseDTO(
                t.id,
                t.name,
                t.colorHex,
                COUNT(a.id)
            )
            FROM NoteTag t
            LEFT JOIN t.assignments a
            WHERE t.user.keycloakSub = :sub
            GROUP BY t.id, t.name, t.colorHex, t.nameNormalized
            ORDER BY t.nameNormalized ASC
            """)
    List<NoteTagUsageResponseDTO> findAllWithUsageCountsByUserSub(@Param("sub") String sub);
}
