package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.TipoDeFolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoDeFolioRepository extends JpaRepository<TipoDeFolio, Integer> {

    Optional<TipoDeFolio> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
