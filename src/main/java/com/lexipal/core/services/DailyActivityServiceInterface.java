package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.DailyActivityCreateRequestDTO;
import com.lexipal.core.dtos.responses.DailyActivityReadResponseDTO;
import com.lexipal.core.entities.Session;

import java.util.List;

public interface DailyActivityServiceInterface
{
    DailyActivityReadResponseDTO createDailyActivity(DailyActivityCreateRequestDTO dto, String keycloakSub);
    DailyActivityReadResponseDTO recordSessionCompletedActivity(Session session);
    List<DailyActivityReadResponseDTO> getDailyActivitiesByUser(String keycloakSub);
}
