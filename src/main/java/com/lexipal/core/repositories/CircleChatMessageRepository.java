package com.lexipal.core.repositories;

import com.lexipal.core.entities.CircleChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CircleChatMessageRepository extends JpaRepository<CircleChatMessage, Long>
{
    List<CircleChatMessage> findByCircle_Id(Long circleId, Pageable pageable);

    List<CircleChatMessage> findByCircle_IdAndIdLessThan(Long circleId, Long beforeMessageId, Pageable pageable);

    Optional<CircleChatMessage> findByIdAndCircle_Id(Long messageId, Long circleId);
}
