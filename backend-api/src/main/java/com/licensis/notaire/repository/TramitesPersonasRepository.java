package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.TramitesPersonas;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TramitesPersonasRepository extends JpaRepository<TramitesPersonas, Integer> {

    List<TramitesPersonas> findByPersona(Persona persona);

    List<TramitesPersonas> findByPersonaIdPersona(Integer idPersona);

    List<TramitesPersonas> findByTramite(Tramite tramite);

    List<TramitesPersonas> findByTramiteIdTramite(Integer idTramite);

    @Query("SELECT tp FROM TramitesPersonas tp WHERE tp.persona.idPersona = :idPersona AND tp.tramite.idTramite = :idTramite")
    List<TramitesPersonas> findByPersonaIdPersonaAndTramiteIdTramite(
            @Param("idPersona") Integer idPersona, @Param("idTramite") Integer idTramite);
}
