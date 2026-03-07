package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.EstadoDeGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoDeGestionRepository extends JpaRepository<EstadoDeGestion, Integer> {

    Optional<EstadoDeGestion> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
