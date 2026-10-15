package com.licensis.notaire.application.port.out.management;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.management.ManagementStatus;

/** Management status storage in domain terms. */
public interface ManagementStatusCatalogPort {

    List<ManagementStatus> findAll();

    List<ManagementStatus> findByNameContaining(String text);

    Optional<ManagementStatus> findById(int id);

    ManagementStatus save(ManagementStatus status);

    /**
     * @return false when no status had that id
     * @throws com.licensis.notaire.domain.management.ManagementStatusReferencedException when other records still
     *         reference it
     */
    boolean deleteById(int id);
}
