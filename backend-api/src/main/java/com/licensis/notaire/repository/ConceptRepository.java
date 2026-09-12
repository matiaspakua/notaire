package com.licensis.notaire.repository;

import com.licensis.notaire.business.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptRepository extends JpaRepository<Concept, Integer> {

    Optional<Concept> findByName(String name);

    @Query("SELECT c FROM Concept c WHERE c.name LIKE %:nombre%")
    List<Concept> findByNameContaining(@Param("nombre") String name);

    List<Concept> findByFixedConcept(boolean fixedConcept);

    List<Concept> findByEnabled(boolean enabled);
}
