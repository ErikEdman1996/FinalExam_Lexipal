package com.lexipal.core.services;

import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.NicknameConflictException;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.repositories.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;

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

    @Override
    public User setNickname(String keycloakSub, String nickname)
    {
        validateKeycloakSub(keycloakSub);

        if (nickname == null || nickname.isBlank())
        {
            throw new IllegalArgumentException("Nickname is required");
        }

        User user = getRequiredUser(keycloakSub);

        String normalized = nickname.toLowerCase(Locale.ROOT);

        if (Objects.equals(normalized, user.getNicknameNormalized()))
        {
            user.setNickname(nickname);
            return userRepository.save(user);
        }

        if (userRepository.existsByNicknameNormalized(normalized))
        {
            throw new NicknameConflictException(nickname);
        }

        user.setNickname(nickname);
        try
        {
            return userRepository.save(user);
        }
        catch (DataIntegrityViolationException ex)
        {
            throw new NicknameConflictException(nickname);
        }
    }

    private void validateKeycloakSub(String keycloakSub)
    {
        if (keycloakSub == null || keycloakSub.isBlank())
        {
            throw new IllegalArgumentException("Authenticated user subject is required.");
        }
    }
}
