package com.lexipal.core.services;

import com.lexipal.core.entities.User;

public interface UserServiceInterface
{
    User getOrCreateUser(String keycloakSub);

    User getRequiredUser(String keycloakSub);

    User setNickname(String keycloakSub, String nickname);
}
