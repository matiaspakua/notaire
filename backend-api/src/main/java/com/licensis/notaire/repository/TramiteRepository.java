package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.GestionDeEscritura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TramiteRepository extends JpaRepository<Tramite, Integer> {

    List<Tramite> findByFkIdTipoTramite(TipoDeTramite tipoTramite);

    List<Tramite> findByFkIdTipoTramiteIdTipoTramite(Integer idTipoTramite);

    List<Tramite> findByFkIdGestion(GestionDeEscritura gestion);

    List<Tramite> findByFkIdGestionIdGestion(Integer idGestion);

    List<Tramite> findByFkIdPresupuestoIdPresupuesto(Integer idPresupuesto);

    @Query("SELECT t FROM Tramite t JOIN t.personaList p WHERE p.idPersona = :idPersona")
    List<Tramite> findByPersonaListIdPersona(@Param("idPersona") Integer idPersona);
}
