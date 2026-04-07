package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.ProfileReadResponseDTO;

public interface ProfileServiceInterface
{
    ProfileReadResponseDTO getMyProfile(String keycloakSub);
}
