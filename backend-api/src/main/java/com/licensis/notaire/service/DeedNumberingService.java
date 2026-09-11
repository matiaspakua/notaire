package com.licensis.notaire.service;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Controla la numeración correlativa de escrituras por protocolo, año y escribano (CU86).
 */
@Service
@Transactional(readOnly = true)
public class NumeracionDeedService {

    private final FolioRepository folioRepository;

    public NumeracionDeedService(FolioRepository folioRepository) {
        this.folioRepository = folioRepository;
    }

    public int calcularSiguienteCorrelativo(Person notary, int year, boolean isAuxiliary) {
        return folioRepository.findMaxNumberDeedByNotaryYearYType(
                notary.getPersonId(), year, isAuxiliary, null).orElse(0) + 1;
    }

    public ResultadoValidacionNumeracion validar(int number, Person notary, int year, boolean isAuxiliary,
            String justificacionSalto, Integer idDeedExcluir) {
        boolean duplicado = folioRepository.existsNumberDeedByNotaryYearYType(
                number, notary.getPersonId(), year, isAuxiliary, idDeedExcluir);
        if (duplicado) {
            return ResultadoValidacionNumeracion.DUPLICADO;
        }

        int siguienteEsperado = folioRepository.findMaxNumberDeedByNotaryYearYType(
                notary.getPersonId(), year, isAuxiliary, idDeedExcluir).orElse(0) + 1;
        if (number == siguienteEsperado) {
            return ResultadoValidacionNumeracion.OK;
        }

        boolean tieneJustificacion = justificacionSalto != null && !justificacionSalto.isBlank();
        return tieneJustificacion
                ? ResultadoValidacionNumeracion.SALTO_JUSTIFICADO
                : ResultadoValidacionNumeracion.SALTO_SIN_JUSTIFICAR;
    }
}
