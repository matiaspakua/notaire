package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.PlantillaPresupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantillaPresupuestoRepository extends JpaRepository<PlantillaPresupuesto, Integer> {

    Optional<PlantillaPresupuesto> findByNombre(String nombre);

    @Query("SELECT p FROM PlantillaPresupuesto p WHERE p.tipoTramite = :tipoTramite")
    List<PlantillaPresupuesto> findByTipoTramite(@Param("tipoTramite") String tipoTramite);

    boolean existsByNombre(String nombre);
}
