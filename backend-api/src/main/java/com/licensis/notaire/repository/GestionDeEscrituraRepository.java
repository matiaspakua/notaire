package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.EstadoDeGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GestionDeEscrituraRepository extends JpaRepository<GestionDeEscritura, Integer> {

    List<GestionDeEscritura> findByFkIdPersonaEscribano(Persona escribano);

    List<GestionDeEscritura> findByFkIdPersonaEscribanoIdPersona(Integer idEscribano);

    List<GestionDeEscritura> findByFkIdEstado(EstadoDeGestion estado);

    List<GestionDeEscritura> findByFkIdEstadoIdEstadoGestion(Integer idEstado);

    @Query("SELECT g FROM GestionDeEscritura g WHERE g.fkIdPersonaEscribano.idPersona = :idEscribano AND g.fkIdEstado.idEstadoGestion = :idEstado")
    List<GestionDeEscritura> findByFkIdPersonaEscribanoIdPersonaAndFkIdEstadoIdEstadoGestion(
            @Param("idEscribano") Integer idEscribano, @Param("idEstado") Integer idEstado);

    @Query("SELECT g FROM GestionDeEscritura g WHERE g.fechaInicio BETWEEN :startDate AND :endDate")
    List<GestionDeEscritura> findByFechaInicioBetween(@Param("startDate") java.util.Date startDate, @Param("endDate") java.util.Date endDate);

    @Query("SELECT g FROM GestionDeEscritura g WHERE g.observaciones LIKE %:keyword%")
    List<GestionDeEscritura> findByObservacionesContaining(@Param("keyword") String keyword);
}
