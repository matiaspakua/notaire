package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.PlantillaCostoDocumento;
import com.licensis.notaire.negocio.PlantillaCostoDocumentoPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaCostoDocumentoRepository
        extends JpaRepository<PlantillaCostoDocumento, PlantillaCostoDocumentoPK> {

    List<PlantillaCostoDocumento> findByTipoDeTramite_IdTipoTramite(Integer idTipoTramite);
}
