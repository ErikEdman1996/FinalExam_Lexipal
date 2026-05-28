package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.ProfileReadResponseDTO;
import com.lexipal.core.dtos.responses.ProfileReadingSeriesRange;
import com.lexipal.core.dtos.responses.ProfileReadingSeriesResponseDTO;

public interface ProfileServiceInterface
{
    ProfileReadResponseDTO getMyProfile(String keycloakSub);

    ProfileReadingSeriesResponseDTO getMyReadingSeries(String keycloakSub, ProfileReadingSeriesRange range);

    ProfileReadResponseDTO setNickname(String keycloakSub, String nickname);
}
