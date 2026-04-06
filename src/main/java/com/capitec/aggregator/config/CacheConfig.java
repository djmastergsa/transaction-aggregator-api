package com.capitec.aggregator.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine-backed in-memory cache configuration.
 *
 * Each cache has a dedicated TTL tuned to how frequently the underlying data changes:
 *   - aggregation-summary / category-summary: 5 minutes (heavy stream computation over all rows)
 *   - monthly-trends / source-summary:        10 minutes (aggregation queries, changes less often)
 *
 * All caches are evicted on every successful sync via @CacheEvict in the service layer.
 * Cache statistics are recorded so they are visible under /actuator/metrics.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String AGGREGATION_SUMMARY = "aggregation-summary";
    public static final String CATEGORY_SUMMARY    = "category-summary";
    public static final String MONTHLY_TRENDS      = "monthly-trends";
    public static final String SOURCE_SUMMARY      = "source-summary";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCache(AGGREGATION_SUMMARY, 5,  100),
                buildCache(CATEGORY_SUMMARY,    5,  50),
                buildCache(MONTHLY_TRENDS,      10, 50),
                buildCache(SOURCE_SUMMARY,      10, 10)
        ));
        return manager;
    }

    private CaffeineCache buildCache(String name, int ttlMinutes, int maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .recordStats()
                .build());
    }
}
