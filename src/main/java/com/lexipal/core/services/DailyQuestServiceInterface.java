package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.DailyQuestResponseDTO;
import com.lexipal.core.entities.Session;

public interface DailyQuestServiceInterface
{
    DailyQuestResponseDTO getTodayQuest(String keycloakSub);
    DailyQuestResponseDTO getTodayQuestForUser(com.lexipal.core.entities.User user);
    DailyQuestEvaluationResult evaluateSessionCompletion(Session session);

    record DailyQuestEvaluationResult(DailyQuestResponseDTO dailyQuest,
                                      com.lexipal.core.dtos.responses.XpUpdateResponseDTO xpUpdate)
    {
    }
}
