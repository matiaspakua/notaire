package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Concepto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaPresupuestoRepository extends JpaRepository<PlantillaPresupuesto, Integer> {

    List<PlantillaPresupuesto> findByTipoDeTramite(TipoDeTramite tipoDeTramite);

    List<PlantillaPresupuesto> findByTipoDeTramiteIdTipoTramite(Integer idTipoTramite);

    List<PlantillaPresupuesto> findByConcepto(Concepto concepto);

    List<PlantillaPresupuesto> findByConceptoIdConcepto(Integer idConcepto);
}
