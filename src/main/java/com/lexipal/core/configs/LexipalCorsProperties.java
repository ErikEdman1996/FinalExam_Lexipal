package com.lexipal.core.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class LexipalCorsProperties
{
    private static final String DEFAULT_ALLOWED_ORIGIN = "http://localhost:5173";

    private final String[] allowedOrigins;

    public LexipalCorsProperties(@Value("${lexipal.allowed-origins:http://localhost:5173}") String rawAllowedOrigins)
    {
        String[] configuredOrigins = Arrays.stream(rawAllowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);

        this.allowedOrigins = configuredOrigins.length == 0
                ? new String[]{DEFAULT_ALLOWED_ORIGIN}
                : configuredOrigins;
    }

    public String[] getAllowedOrigins()
    {
        return allowedOrigins.clone();
    }
}
