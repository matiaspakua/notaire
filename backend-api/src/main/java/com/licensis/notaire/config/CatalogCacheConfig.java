package com.licensis.notaire.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.licensis.notaire.adapter.out.persistence.concept.CachingConceptCatalog;
import com.licensis.notaire.adapter.out.persistence.concept.ConceptOpsAdapter;
import com.licensis.notaire.adapter.out.persistence.document.CachingDocumentTypeCatalog;
import com.licensis.notaire.adapter.out.persistence.document.DocumentTypePersistenceAdapter;
import com.licensis.notaire.adapter.out.persistence.folio.CachingFolioTypeCatalog;
import com.licensis.notaire.adapter.out.persistence.folio.FolioTypePersistenceAdapter;
import com.licensis.notaire.adapter.out.persistence.identification.CachingIdentificationTypeCatalog;
import com.licensis.notaire.adapter.out.persistence.identification.IdentificationTypePersistenceAdapter;
import com.licensis.notaire.adapter.out.persistence.management.CachingManagementStatusCatalog;
import com.licensis.notaire.adapter.out.persistence.management.ManagementStatusPersistenceAdapter;
import com.licensis.notaire.adapter.out.persistence.user.CachingRoleCatalog;
import com.licensis.notaire.adapter.out.persistence.user.RoleCatalogPersistenceAdapter;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort;
import com.licensis.notaire.application.port.out.document.DocumentTypeCatalogPort;
import com.licensis.notaire.application.port.out.folio.FolioTypeCatalogPort;
import com.licensis.notaire.application.port.out.identification.IdentificationTypeCatalogPort;
import com.licensis.notaire.application.port.out.management.ManagementStatusCatalogPort;
import com.licensis.notaire.application.port.out.user.RoleCatalogPort;

/**
 * Turns on the catalog list caches (#597, CU74). The TTL lives in {@code spring.cache.caffeine.spec}. Each
 * caching adapter is the catalog port its use cases receive; the persistence adapter stays the delegate and the
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

    @Bean
    @Primary
    public DocumentTypeCatalogPort cachingDocumentTypeCatalog(DocumentTypePersistenceAdapter persistence) {
        return new CachingDocumentTypeCatalog(persistence);
    }

    @Bean
    @Primary
    public FolioTypeCatalogPort cachingFolioTypeCatalog(FolioTypePersistenceAdapter persistence) {
        return new CachingFolioTypeCatalog(persistence);
    }

    @Bean
    @Primary
    public ManagementStatusCatalogPort cachingManagementStatusCatalog(ManagementStatusPersistenceAdapter persistence) {
        return new CachingManagementStatusCatalog(persistence);
    }

    @Bean
    @Primary
    public RoleCatalogPort cachingRoleCatalog(RoleCatalogPersistenceAdapter persistence) {
        return new CachingRoleCatalog(persistence);
    }

    @Bean
    @Primary
    public ConceptOpsPort cachingConceptCatalog(ConceptOpsAdapter persistence) {
        return new CachingConceptCatalog(persistence);
    }
}
