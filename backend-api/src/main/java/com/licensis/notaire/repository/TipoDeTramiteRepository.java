package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.TipoDeTramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoDeTramiteRepository extends JpaRepository<TipoDeTramite, Integer> {

    Optional<TipoDeTramite> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
