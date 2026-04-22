package com.lexipal.core.services;

import com.lexipal.core.exceptions.ExternalServiceException;
import com.lexipal.core.repositories.BookCircleRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class InviteCodeGenerator
{
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 10;

    private final SecureRandom random = new SecureRandom();

    public String generateUnique(BookCircleRepository repository)
    {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++)
        {
            String candidate = generate();
            if (!repository.existsByInviteCode(candidate))
            {
                return candidate;
            }
        }
        throw new ExternalServiceException(
                "Could not generate a unique invite code after " + MAX_ATTEMPTS + " attempts");
    }

    private String generate()
    {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++)
        {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
