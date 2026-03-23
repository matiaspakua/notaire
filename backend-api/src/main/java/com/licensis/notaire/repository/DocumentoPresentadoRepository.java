package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.negocio.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoPresentadoRepository extends JpaRepository<DocumentoPresentado, Integer> {

    List<DocumentoPresentado> findByFkIdTramite(Tramite tramite);

    List<DocumentoPresentado> findByFkIdTramiteIdTramite(Integer idTramite);

    List<DocumentoPresentado> findByLiberado(Boolean liberado);

    List<DocumentoPresentado> findByObservado(Boolean observado);

    List<DocumentoPresentado> findByPreparado(Boolean preparado);
}
