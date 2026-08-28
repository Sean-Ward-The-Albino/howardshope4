package org.howards4hope.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Multi-Tier L1 (Caffeine) & L2 Caching Configuration.
 * Provides sub-millisecond local in-memory caching for account profiles,
 * public events, JWKS keys, and full-text search results.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String ACCOUNTS_CACHE = "accounts";
    public static final String EVENTS_CACHE = "events";
    public static final String RESOURCES_CACHE = "resources";
    public static final String JWKS_CACHE = "jwks_keys";

    /**
     * L1 In-Memory Caffeine Cache Manager.
     * Fast near-cache with 10-minute expiry and max 10,000 items.
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                ACCOUNTS_CACHE,
                EVENTS_CACHE,
                RESOURCES_CACHE,
                JWKS_CACHE
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10_000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
