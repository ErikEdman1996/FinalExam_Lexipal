package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.StreakMetaDataCreateRequestDTO;
import com.lexipal.core.dtos.responses.StreakMetaDataReadResponseDTO;
import com.lexipal.core.entities.User;

import java.time.LocalDate;

public interface StreakMetaDataServiceInterface
{
    StreakMetaDataReadResponseDTO initializeStreakMetaData(StreakMetaDataCreateRequestDTO dto, String keycloakSub);
    StreakMetaDataReadResponseDTO getStreakMetaData(String keycloakSub);
    void recordActivity(User user, LocalDate activityDate);
    int getAvailableStreakFreezes(User user);
    void addAvailableStreakFreeze(User user);
}
