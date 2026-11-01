package com.licensis.notaire.config;

import com.licensis.notaire.business.IdentificationTypeLookup;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Wires {@link IdentificationTypeLookup#shared()} from Spring Data so remaining {@code business}
 * entity helpers can resolve identification-type names without the retired {@code jpa} package (#576).
 */
@Configuration
public class IdentificationTypeLookupConfig {

    @Autowired
    public void wireIdentificationTypeLookup(IdentificationTypeRepository identificationTypes) {
        IdentificationTypeLookup.wireCatalog(identificationTypes::findAll);
    }
}
