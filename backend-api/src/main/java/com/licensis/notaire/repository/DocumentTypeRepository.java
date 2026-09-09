package com.licensis.notaire.repository;

import com.licensis.notaire.business.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer> {

    Optional<DocumentType> findByName(String name);

    boolean existsByName(String name);

    List<DocumentType> findByNameContaining(String name);
}
