package com.licensis.notaire.service;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Controla la numeración correlativa de escrituras por protocolo, año y escribano (CU86).
 */
@Service
@Transactional(readOnly = true)
public class NumeracionEscrituraService {

    private final FolioRepository folioRepository;

    public NumeracionEscrituraService(FolioRepository folioRepository) {
        this.folioRepository = folioRepository;
    }

    public int calcularSiguienteCorrelativo(Persona escribano, int anio, boolean esAuxiliar) {
        return folioRepository.findMaxNumeroEscrituraByEscribanoAnioYTipo(
                escribano.getIdPersona(), anio, esAuxiliar, null).orElse(0) + 1;
    }

    public ResultadoValidacionNumeracion validar(int numero, Persona escribano, int anio, boolean esAuxiliar,
            String justificacionSalto, Integer idEscrituraExcluir) {
        boolean duplicado = folioRepository.existsNumeroEscrituraByEscribanoAnioYTipo(
                numero, escribano.getIdPersona(), anio, esAuxiliar, idEscrituraExcluir);
        if (duplicado) {
            return ResultadoValidacionNumeracion.DUPLICADO;
        }

        int siguienteEsperado = folioRepository.findMaxNumeroEscrituraByEscribanoAnioYTipo(
                escribano.getIdPersona(), anio, esAuxiliar, idEscrituraExcluir).orElse(0) + 1;
        if (numero == siguienteEsperado) {
            return ResultadoValidacionNumeracion.OK;
        }

        boolean tieneJustificacion = justificacionSalto != null && !justificacionSalto.isBlank();
        return tieneJustificacion
                ? ResultadoValidacionNumeracion.SALTO_JUSTIFICADO
                : ResultadoValidacionNumeracion.SALTO_SIN_JUSTIFICAR;
    }
}
