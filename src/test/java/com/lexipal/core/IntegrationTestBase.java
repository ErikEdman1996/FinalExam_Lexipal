package com.lexipal.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexipal.core.config.TestSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import jakarta.transaction.Transactional;

/**
 * Base class for integration tests.
 * Boots the full Spring context with H2, auto-configures MockMvc,
 * and provides JWT helper methods.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
public abstract class IntegrationTestBase
{
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Creates a mock JWT RequestPostProcessor with the given Keycloak subject.
     */
    protected RequestPostProcessor jwtWithSub(String sub)
    {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .subject(sub)
                        .claim("preferred_username", "test-user")
                );
    }
}
