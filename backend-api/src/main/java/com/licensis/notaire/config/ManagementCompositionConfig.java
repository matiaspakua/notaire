package com.licensis.notaire.config;

import com.licensis.notaire.adapter.in.web.management.TransitionManagementWebMapper;
import com.licensis.notaire.adapter.out.persistence.management.ManagementBitacoraAdapter;
import com.licensis.notaire.adapter.out.persistence.management.ManagementRepositoryAdapter;
import com.licensis.notaire.adapter.out.persistence.management.StatusRepositoryAdapter;
import com.licensis.notaire.adapter.out.persistence.management.WorkflowLookupAdapter;
import com.licensis.notaire.adapter.out.persistence.management.WorkflowTransitionValidatorAdapter;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import com.licensis.notaire.application.usecase.management.TransitionManagementUseCaseImpl;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.service.ManagementBitacoraService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root for management transition use case (CU83).
 * Wires together all ports and adapters for the TransitionManagementUseCase.
 */
@Configuration
public class ManagementCompositionConfig {

    /**
     * Use case bean for transitioning management status.
     */
    @Bean
    public TransitionManagementUseCase transitionManagementUseCase(
            DeedManagementRepository managementRepository,
            ManagementStatusRepository statusRepository,
            ManagementBitacoraService bitacoraService,
            WorkflowTransitionRepository workflowTransitionRepository) {

        var managementRepositoryAdapter = new ManagementRepositoryAdapter(managementRepository);
        var statusRepositoryAdapter = new StatusRepositoryAdapter(statusRepository);
        var workflowLookupAdapter = new WorkflowLookupAdapter();
        var workflowTransitionValidatorAdapter =
                new WorkflowTransitionValidatorAdapter(workflowTransitionRepository);
        var bitacoraAdapter = new ManagementBitacoraAdapter(bitacoraService);

        return new TransitionManagementUseCaseImpl(managementRepositoryAdapter, statusRepositoryAdapter,
                workflowLookupAdapter, workflowTransitionValidatorAdapter, bitacoraAdapter);
    }

    /**
     * Web mapper bean for HTTP response mapping.
     */
    @Bean
    public TransitionManagementWebMapper transitionManagementWebMapper(
            DeedManagementRepository managementRepository) {
        return new TransitionManagementWebMapper(managementRepository);
    }
}
