package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.MinutaInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MinutaInscripcionRepository extends JpaRepository<MinutaInscripcion, Integer> {

    Optional<MinutaInscripcion> findByFkIdEscrituraIdEscritura(Integer idEscritura);

    Optional<MinutaInscripcion> findTopByOrderByNumeroDesc();
}
