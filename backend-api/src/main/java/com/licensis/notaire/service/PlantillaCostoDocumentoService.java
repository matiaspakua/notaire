package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.PlantillaCostoDocumento;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.repository.PlantillaCostoDocumentoRepository;
import com.licensis.notaire.repository.TipoDeDocumentoRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU27/CU39 - Costos (fijos o variables) esperados por tipo de documento dentro
 * de la plantilla de presupuesto de un tipo de trámite (Issue #823).
 */
@Service
@Transactional
public class PlantillaCostoDocumentoService {

    private final PlantillaCostoDocumentoRepository plantillaCostoDocumentoRepository;
    private final TipoDeTramiteRepository tipoDeTramiteRepository;
    private final TipoDeDocumentoRepository tipoDeDocumentoRepository;

    public PlantillaCostoDocumentoService(PlantillaCostoDocumentoRepository plantillaCostoDocumentoRepository,
                                           TipoDeTramiteRepository tipoDeTramiteRepository,
                                           TipoDeDocumentoRepository tipoDeDocumentoRepository) {
        this.plantillaCostoDocumentoRepository = plantillaCostoDocumentoRepository;
        this.tipoDeTramiteRepository = tipoDeTramiteRepository;
        this.tipoDeDocumentoRepository = tipoDeDocumentoRepository;
    }

    public PlantillaCostoDocumento crear(Integer idTipoTramite, Integer idTipoDocumento,
                                          Float montoFijo, Float porcentajeVariable) {
        validarExactamenteUnCosto(montoFijo, porcentajeVariable);

        TipoDeTramite tipoDeTramite = tipoDeTramiteRepository.findById(idTipoTramite)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el tipo de trámite con ID: " + idTipoTramite));
        TipoDeDocumento tipoDeDocumento = tipoDeDocumentoRepository.findById(idTipoDocumento)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el tipo de documento con ID: " + idTipoDocumento));

        PlantillaCostoDocumento costo = new PlantillaCostoDocumento(
                tipoDeTramite.getIdTipoTramite(), tipoDeDocumento.getIdTipoDocumento());
        costo.setTipoDeTramite(tipoDeTramite);
        costo.setTipoDeDocumento(tipoDeDocumento);
        costo.setMontoFijo(montoFijo);
        costo.setPorcentajeVariable(porcentajeVariable);
        return plantillaCostoDocumentoRepository.save(costo);
    }

    @Transactional(readOnly = true)
    public List<PlantillaCostoDocumento> findByTipoTramite(Integer idTipoTramite) {
        return plantillaCostoDocumentoRepository.findByTipoDeTramite_IdTipoTramite(idTipoTramite);
    }

    private void validarExactamenteUnCosto(Float montoFijo, Float porcentajeVariable) {
        boolean tieneMontoFijo = montoFijo != null;
        boolean tienePorcentajeVariable = porcentajeVariable != null;
        if (tieneMontoFijo == tienePorcentajeVariable) {
            throw new BusinessValidationException(
                    "Debe indicarse exactamente uno entre monto fijo y porcentaje variable");
        }
    }
}
