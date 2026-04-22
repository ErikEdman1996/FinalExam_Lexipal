package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.BookCircleCreateRequestDTO;
import com.lexipal.core.dtos.responses.BookCircleCreateResponseDTO;
import com.lexipal.core.dtos.responses.BookCircleReadResponseDTO;
import com.lexipal.core.dtos.responses.CircleMemberDetailResponseDTO;

import java.util.List;

public interface BookCircleServiceInterface
{
    BookCircleCreateResponseDTO createCircle(BookCircleCreateRequestDTO request, String keycloakSub);

    BookCircleReadResponseDTO joinCircle(String inviteCode, String keycloakSub);

    BookCircleReadResponseDTO getCircle(Long circleId, String keycloakSub);

    List<BookCircleReadResponseDTO> getMyCircles(String keycloakSub);

    CircleMemberDetailResponseDTO getMemberDetails(Long circleId, Long targetUserId, String keycloakSub);

    void leaveCircle(Long circleId, String keycloakSub);

    void closeCircle(Long circleId, String keycloakSub);
}
