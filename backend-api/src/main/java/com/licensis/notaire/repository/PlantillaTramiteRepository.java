package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.PlantillaTramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantillaTramiteRepository extends JpaRepository<PlantillaTramite, Integer> {

    Optional<PlantillaTramite> findByNombre(String nombre);

    @Query("SELECT p FROM PlantillaTramite p WHERE p.tipoTramite = :tipoTramite")
    List<PlantillaTramite> findByTipoTramite(@Param("tipoTramite") String tipoTramite);

    boolean existsByNombre(String nombre);
}
