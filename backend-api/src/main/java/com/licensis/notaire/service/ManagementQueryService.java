package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoManagementSummary;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.repository.DeedManagementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ManagementQueryService {

    private final DeedManagementRepository repository;

    public ManagementQueryService(DeedManagementRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<DtoManagementSummary> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(ManagementQueryService::toSummary);
    }

    @Transactional(readOnly = true)
    public Optional<DtoManagementSummary> findById(Integer id) {
        return repository.findById(id).map(ManagementQueryService::toSummary);
    }

    @Transactional(readOnly = true)
    public Optional<DtoManagementSummary> findByNumber(int number) {
        return repository.findByNumber(number).map(ManagementQueryService::toSummary);
    }

    private static DtoManagementSummary toSummary(DeedManagement management) {
        return new DtoManagementSummary(
                management.getIdManagement(),
                management.getNumber(),
                management.getEncabezado(),
                management.getDateStart(),
                management.getFkIdManagementStatus() != null ? management.getFkIdManagementStatus().getName() : null,
                management.getProcedureList() != null ? management.getProcedureList().size() : 0,
                management.getNotes());
    }
}
