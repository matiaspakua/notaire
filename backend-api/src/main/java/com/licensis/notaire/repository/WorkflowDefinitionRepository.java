package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Integer> {

    List<WorkflowDefinition> findByActivo(boolean activo);

    boolean existsByNombre(String nombre);
}
