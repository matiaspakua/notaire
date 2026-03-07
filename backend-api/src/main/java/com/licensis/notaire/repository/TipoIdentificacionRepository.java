package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.TipoIdentificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoIdentificacionRepository extends JpaRepository<TipoIdentificacion, Integer> {

    Optional<TipoIdentificacion> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
