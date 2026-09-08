package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Suplencia;
import com.licensis.notaire.negocio.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SuplenciaRepository extends JpaRepository<Suplencia, Integer> {

    List<Suplencia> findByFkIdSuplente(Person suplente);

    List<Suplencia> findByFkIdSuplenteIdPersona(Integer idSuplente);

    List<Suplencia> findByFkIdSuplantado(Person suplantado);

    List<Suplencia> findByFkIdSuplantadoIdPersona(Integer idSuplantado);

    @Query("SELECT s FROM Suplencia s WHERE s.fechaInicio <= :fecha AND s.fechaFin >= :fecha")
    List<Suplencia> findByFechaInicioBeforeAndFechaFinAfter(@Param("fecha") Date fecha);

    List<Suplencia> findByFkIdSuplantadoIdPersonaAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Integer idSuplantado, Date fechaInicio, Date fechaFin);
}
