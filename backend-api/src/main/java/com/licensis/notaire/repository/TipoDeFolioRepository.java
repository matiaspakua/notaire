package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.TipoDeFolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoDeFolioRepository extends JpaRepository<TipoDeFolio, Integer> {

    Optional<TipoDeFolio> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    @Query("SELECT t FROM TipoDeFolio t WHERE t.nombre LIKE %:nombre%")
    List<TipoDeFolio> findByNombreContaining(@Param("nombre") String nombre);
}
