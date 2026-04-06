package com.lexipal.core.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Map;

/**
 * Replaces the real Keycloak-backed JwtDecoder with a stub that accepts
 * any token string and returns a Jwt whose "sub" claim equals the token value.
 * This lets tests use SecurityMockMvcRequestPostProcessors.jwt() without
 * needing a running Keycloak instance.
 */
@TestConfiguration
public class TestSecurityConfig
{
    @Bean
    public JwtDecoder jwtDecoder()
    {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", token)
                .claim("preferred_username", "test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
