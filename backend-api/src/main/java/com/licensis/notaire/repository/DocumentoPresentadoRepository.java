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

    @Query("SELECT d FROM DocumentoPresentado d WHERE d.estado = :estado")
    List<DocumentoPresentado> findByEstado(@Param("estado") String estado);

    @Query("SELECT d FROM DocumentoPresentado d WHERE d.tipoDocumento = :tipoDocumento")
    List<DocumentoPresentado> findByTipoDocumento(@Param("tipoDocumento") String tipoDocumento);
}
