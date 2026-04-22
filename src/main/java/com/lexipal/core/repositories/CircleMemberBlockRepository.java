package com.lexipal.core.repositories;

import com.lexipal.core.entities.CircleMemberBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CircleMemberBlockRepository extends JpaRepository<CircleMemberBlock, Long>
{
    boolean existsByCircle_IdAndUser_Id(Long circleId, Long userId);
}
