package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.MinutaInscripcion;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.MinutaInscripcionRepository;
import com.licensis.notaire.repository.TramiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * CU82 - Generar Minuta de Inscripción: genera y hace seguimiento del
 * circuito registral (Generada -> Presentada -> Observada / Inscripta) de
 * una escritura sobre un inmueble ante el Registro de la Propiedad Inmueble.
 */
@Service
@Transactional
public class MinutaInscripcionService {

    private final MinutaInscripcionRepository minutaInscripcionRepository;
    private final EscrituraRepository escrituraRepository;
    private final TramiteRepository tramiteRepository;

    public MinutaInscripcionService(MinutaInscripcionRepository minutaInscripcionRepository,
            EscrituraRepository escrituraRepository, TramiteRepository tramiteRepository) {
        this.minutaInscripcionRepository = minutaInscripcionRepository;
        this.escrituraRepository = escrituraRepository;
        this.tramiteRepository = tramiteRepository;
    }

    public MinutaInscripcion generar(Integer idEscritura) {
        Escritura escritura = escrituraRepository.findById(idEscritura)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la escritura con ID: " + idEscritura));

        if (!ConstantesNegocio.ESCRITURA_FIRMADA.equals(escritura.getEstado())) {
            throw new BusinessValidationException(
                    "La escritura debe estar firmada para generar la minuta de inscripción");
        }

        Inmueble inmueble = buscarInmuebleDelTramite(idEscritura);
        validarDatosCompletos(inmueble);

        MinutaInscripcion minuta = new MinutaInscripcion();
        minuta.setNumero(calcularSiguienteNumero());
        minuta.setEstado(ConstantesNegocio.MINUTA_INSCRIPCION_GENERADA);
        minuta.setFechaGeneracion(new Date());
        minuta.setFkIdEscritura(escritura);
        return minutaInscripcionRepository.save(minuta);
    }

    private Inmueble buscarInmuebleDelTramite(Integer idEscritura) {
        return tramiteRepository.findByFkIdEscrituraIdEscritura(idEscritura).stream()
                .map(Tramite::getFkIdInmueble)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new BusinessValidationException(
                        "No existe un trámite con inmueble asociado a la escritura"));
    }

    private void validarDatosCompletos(Inmueble inmueble) {
        List<String> faltantes = new ArrayList<>();
        if (esVacio(inmueble.getNomenclaturaCatastral())) {
            faltantes.add("nomenclatura catastral");
        }
        if (inmueble.getValuacionFiscal() == null) {
            faltantes.add("valuación fiscal");
        }
        if (esVacio(inmueble.getDomicilio())) {
            faltantes.add("domicilio");
        }
        if (esVacio(inmueble.getMatricula())) {
            faltantes.add("matrícula");
        }
        if (esVacio(inmueble.getTomoFolioFinca())) {
            faltantes.add("tomo/folio/finca");
        }
        if (esVacio(inmueble.getLinderos())) {
            faltantes.add("linderos");
        }
        if (!faltantes.isEmpty()) {
            throw new BusinessValidationException(
                    "Faltan datos catastrales/registrales del inmueble: " + String.join(", ", faltantes));
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private int calcularSiguienteNumero() {
        return minutaInscripcionRepository.findTopByOrderByNumeroDesc()
                .map(m -> m.getNumero() + 1)
                .orElse(1);
    }

    @Transactional(readOnly = true)
    public Optional<MinutaInscripcion> findById(Integer id) {
        return minutaInscripcionRepository.findById(id);
    }

    public MinutaInscripcion presentar(Integer id, Date fechaPresentacion, String numeroEntradaRegistral) {
        MinutaInscripcion minuta = buscarMinuta(id);
        if (!ConstantesNegocio.MINUTA_INSCRIPCION_GENERADA.equals(minuta.getEstado())) {
            throw new BusinessValidationException(
                    "La minuta debe estar en estado Generada para registrar la presentación");
        }
        minuta.setFechaPresentacion(fechaPresentacion);
        minuta.setNumeroEntradaRegistral(numeroEntradaRegistral);
        minuta.setEstado(ConstantesNegocio.MINUTA_INSCRIPCION_PRESENTADA);
        return minutaInscripcionRepository.save(minuta);
    }

    public MinutaInscripcion observar(Integer id, String observacionesRegistro, Date fechaSubsanacion) {
        MinutaInscripcion minuta = buscarMinuta(id);
        if (!ConstantesNegocio.MINUTA_INSCRIPCION_PRESENTADA.equals(minuta.getEstado())) {
            throw new BusinessValidationException(
                    "La minuta debe estar presentada para registrar una observación del Registro");
        }
        minuta.setObservacionesRegistro(observacionesRegistro);
        minuta.setFechaSubsanacion(fechaSubsanacion);
        minuta.setEstado(ConstantesNegocio.MINUTA_INSCRIPCION_OBSERVADA);
        return minutaInscripcionRepository.save(minuta);
    }

    public MinutaInscripcion inscribir(Integer id, Date fechaRecepcion, String numeroInscripcionDefinitivo) {
        MinutaInscripcion minuta = buscarMinuta(id);
        if (!ConstantesNegocio.MINUTA_INSCRIPCION_PRESENTADA.equals(minuta.getEstado())) {
            throw new BusinessValidationException(
                    "La minuta debe estar presentada para registrar la inscripción definitiva");
        }
        minuta.setFechaRecepcion(fechaRecepcion);
        minuta.setNumeroInscripcionDefinitivo(numeroInscripcionDefinitivo);
        minuta.setEstado(ConstantesNegocio.MINUTA_INSCRIPCION_INSCRIPTA);
        return minutaInscripcionRepository.save(minuta);
    }

    private MinutaInscripcion buscarMinuta(Integer id) {
        return minutaInscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la minuta de inscripción con ID: " + id));
    }
}
