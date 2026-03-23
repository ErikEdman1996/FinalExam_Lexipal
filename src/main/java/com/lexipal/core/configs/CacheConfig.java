package com.lexipal.core.configs;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig
{
    public Caffeine<Object, Object> caffeineConfig()
    {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(100);
    }

    public CacheManager cacheManager(Caffeine<Object, Object> caffeine)
    {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("dictionary");
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}
