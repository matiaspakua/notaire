package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Concepto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptoRepository extends JpaRepository<Concepto, Integer> {

    Optional<Concepto> findByNombre(String nombre);

    @Query("SELECT c FROM Concepto c WHERE c.nombre LIKE %:nombre%")
    List<Concepto> findByNombreContaining(@Param("nombre") String nombre);

    List<Concepto> findByConceptoFijo(boolean conceptoFijo);

    List<Concepto> findByHabilitado(boolean habilitado);
}
