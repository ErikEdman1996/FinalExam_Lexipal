package com.lexipal.core.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer
{
    private final LexipalCorsProperties corsProperties;

    public WebConfig(final LexipalCorsProperties corsProperties)
    {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        registry.addMapping("/**")
                .allowedOrigins(corsProperties.getAllowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
