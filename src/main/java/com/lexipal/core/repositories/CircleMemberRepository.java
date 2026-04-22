package com.lexipal.core.repositories;

import com.lexipal.core.entities.CircleMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CircleMemberRepository extends JpaRepository<CircleMember, Long>
{
    Optional<CircleMember> findByCircle_IdAndUser_Id(Long circleId, Long userId);

    long countByCircle_Id(Long circleId);

    List<CircleMember> findByCircle_Id(Long circleId);
}
