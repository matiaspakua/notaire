package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.PlantillaTramitePK;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.TipoDeDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaTramiteRepository extends JpaRepository<PlantillaTramite, PlantillaTramitePK> {

    List<PlantillaTramite> findByTipoDeTramite(TipoDeTramite tipoDeTramite);

    List<PlantillaTramite> findByTipoDeTramiteIdTipoTramite(Integer idTipoTramite);

    List<PlantillaTramite> findByTipoDeDocumento(TipoDeDocumento tipoDeDocumento);

    List<PlantillaTramite> findByTipoDeDocumentoIdTipoDocumento(Integer idTipoDocumento);
}
