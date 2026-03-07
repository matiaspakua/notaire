package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Integer> {

    Optional<Presupuesto> findByNumero(Integer numero);

    List<Presupuesto> findByFkIdPersona(Persona persona);

    List<Presupuesto> findByFkIdPersonaIdPersona(Integer idPersona);

    @Query("SELECT p FROM Presupuesto p WHERE p.estado = :estado")
    List<Presupuesto> findByEstado(@Param("estado") String estado);

    @Query("SELECT p FROM Presupuesto p WHERE p.fkIdPersona.idPersona = :idPersona AND p.estado = :estado")
    List<Presupuesto> findByFkIdPersonaIdPersonaAndEstado(@Param("idPersona") Integer idPersona, @Param("estado") String estado);

    @Query("SELECT p FROM Presupuesto p WHERE p.fechaEmision BETWEEN :startDate AND :endDate")
    List<Presupuesto> findByFechaEmisionBetween(@Param("startDate") java.util.Date startDate, @Param("endDate") java.util.Date endDate);
}
