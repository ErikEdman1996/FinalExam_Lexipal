package com.lexipal.core.services;

import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.repositories.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserServiceInterface
{
    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(final UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Override
    public User getOrCreateUser(String keycloakSub)
    {
        validateKeycloakSub(keycloakSub);

        return userRepository.findByKeycloakSub(keycloakSub)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setKeycloakSub(keycloakSub);
                    User savedUser = userRepository.save(newUser);
                    logger.info("Created user {} for keycloak subject {}", savedUser.getId(), keycloakSub);
                    return savedUser;
                });
    }

    @Override
    public User getRequiredUser(String keycloakSub)
    {
        validateKeycloakSub(keycloakSub);

        return userRepository.findByKeycloakSub(keycloakSub)
                .orElseThrow(() -> new ResourceNotFoundException("User", "sub", keycloakSub));
    }

    private void validateKeycloakSub(String keycloakSub)
    {
        if (keycloakSub == null || keycloakSub.isBlank())
        {
            throw new IllegalArgumentException("Authenticated user subject is required.");
        }
    }
}
