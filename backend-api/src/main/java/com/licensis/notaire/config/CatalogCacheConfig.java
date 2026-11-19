package com.licensis.notaire.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.licensis.notaire.adapter.out.persistence.identification.CachingIdentificationTypeCatalog;
import com.licensis.notaire.adapter.out.persistence.identification.IdentificationTypePersistenceAdapter;
import com.licensis.notaire.application.port.out.identification.IdentificationTypeCatalogPort;

/**
 * Turns on the catalog list cache (#597, CU74). The TTL lives in {@code spring.cache.caffeine.spec}. The
 * caching adapter is the catalog port use cases receive; the persistence adapter stays the delegate and the
 * usage port.
 */
@Configuration
@EnableCaching
public class CatalogCacheConfig {

    @Bean
    @Primary
    public IdentificationTypeCatalogPort cachingIdentificationTypeCatalog(
            IdentificationTypePersistenceAdapter persistence) {
        return new CachingIdentificationTypeCatalog(persistence);
    }
}
