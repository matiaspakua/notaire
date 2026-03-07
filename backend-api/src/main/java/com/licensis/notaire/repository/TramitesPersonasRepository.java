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

    List<TramitesPersonas> findByFkIdPersona(Persona persona);

    List<TramitesPersonas> findByFkIdPersonaIdPersona(Integer idPersona);

    List<TramitesPersonas> findByFkIdTramite(Tramite tramite);

    List<TramitesPersonas> findByFkIdTramiteIdTramite(Integer idTramite);

    @Query("SELECT tp FROM TramitesPersonas tp WHERE tp.fkIdPersona.idPersona = :idPersona AND tp.fkIdTramite.idTramite = :idTramite")
    List<TramitesPersonas> findByFkIdPersonaIdPersonaAndFkIdTramiteIdTramite(
            @Param("idPersona") Integer idPersona, @Param("idTramite") Integer idTramite);

    @Query("SELECT tp FROM TramitesPersonas tp WHERE tp.tipoRelacion = :tipoRelacion")
    List<TramitesPersonas> findByTipoRelacion(@Param("tipoRelacion") String tipoRelacion);
}
