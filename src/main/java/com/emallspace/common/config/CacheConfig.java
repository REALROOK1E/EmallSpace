package com.emallspace.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Object> localPostCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000) // Max 1000 hot posts
                .expireAfterWrite(5, TimeUnit.MINUTES) // TTL 5 mins
                .recordStats() // Enable metrics
                .build();
    }
}
