package com.licensis.notaire.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Turns on the catalog list cache (#597, CU74). The TTL lives in
 * {@code spring.cache.caffeine.spec}. Writes evict their own cache.
 */
@Configuration
@EnableCaching
public class CatalogCacheConfig {
}
