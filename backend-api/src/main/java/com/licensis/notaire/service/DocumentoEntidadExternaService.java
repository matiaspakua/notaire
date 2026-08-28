package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoDocumentoEntidadExterna;
import com.licensis.notaire.dto.DtoGestionDocumentosEntidadesExternas;
import com.licensis.notaire.dto.DtoMovimientoDocumentoEntidadExterna;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.DocumentoPresentadoRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.TramiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU10 - Registrar movimientos de documentación de entidades externas: expone
 * la documentación de una gestión que debe ser presentada por entidades
 * externas (registros, catastro) y permite registrar los datos de su
 * movimiento, disparando la transición automática de la gestión a
 * "Documentacion Completa" cuando todos esos documentos quedan entregados.
 */
@Service
public class DocumentoEntidadExternaService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoEntidadExternaService.class);

    private final GestionDeEscrituraRepository gestionRepository;
    private final TramiteRepository tramiteRepository;
    private final DocumentoPresentadoRepository documentoPresentadoRepository;
    private final GestionTransitionService gestionTransitionService;

    public DocumentoEntidadExternaService(GestionDeEscrituraRepository gestionRepository,
            TramiteRepository tramiteRepository, DocumentoPresentadoRepository documentoPresentadoRepository,
            GestionTransitionService gestionTransitionService) {
        this.gestionRepository = gestionRepository;
        this.tramiteRepository = tramiteRepository;
        this.documentoPresentadoRepository = documentoPresentadoRepository;
        this.gestionTransitionService = gestionTransitionService;
    }

    @Transactional(readOnly = true)
    public DtoGestionDocumentosEntidadesExternas obtenerDocumentos(Integer idGestion) {
        GestionDeEscritura gestion = findGestionOrThrow(idGestion);
        List<DocumentoPresentado> documentos = documentoPresentadoRepository
                .findByFkIdTramiteFkIdGestionIdGestionAndQuienEntrega(idGestion,
                        ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA);
        return toDto(gestion, documentos);
    }

    @Transactional
    public DtoDocumentoEntidadExterna registrarMovimiento(Integer idGestion, Integer idDocumentoPresentado,
            DtoMovimientoDocumentoEntidadExterna movimiento) {
        findGestionOrThrow(idGestion);
        DocumentoPresentado documento = documentoPresentadoRepository.findById(idDocumentoPresentado)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento presentado no encontrado con ID: " + idDocumentoPresentado));
        validarPerteneceAGestion(documento, idGestion);
        validarEsEntidadExterna(documento);

        aplicarMovimiento(documento, movimiento);
        DocumentoPresentado guardado = documentoPresentadoRepository.save(documento);

        return toDto(guardado);
    }

    /**
     * Intenta transicionar la gestión a "Documentacion Completa" cuando todos sus
     * documentos de entidad externa quedaron entregados. Se invoca como un paso
     * independiente después de {@link #registrarMovimiento}, en su propia
     * transacción de nivel superior: una transición no definida en el workflow es
     * un efecto colateral "best effort" que nunca debe invalidar el movimiento ya
     * guardado, y solo una llamada fuera de la transacción de {@code
     * registrarMovimiento} evita que Spring marque esa transacción como
     * rollback-only cuando {@link GestionTransitionService#transicionar} falla.
     */
    public void intentarCompletarDocumentacion(Integer idGestion) {
        List<DocumentoPresentado> documentos = documentoPresentadoRepository
                .findByFkIdTramiteFkIdGestionIdGestionAndQuienEntrega(idGestion,
                        ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA);
        boolean todosEntregados = !documentos.isEmpty()
                && documentos.stream().allMatch(doc -> Boolean.TRUE.equals(doc.getEntregado()));
        if (!todosEntregados) {
            return;
        }
        try {
            gestionTransitionService.transicionar(idGestion, ConstantesNegocio.GESTION_CON_DOCUMENTACION_COMPLETA);
        } catch (BusinessValidationException | ResourceNotFoundException e) {
            log.warn("No se pudo transicionar automáticamente la gestión {} a '{}': {}", idGestion,
                    ConstantesNegocio.GESTION_CON_DOCUMENTACION_COMPLETA, e.getMessage());
        }
    }

    private GestionDeEscritura findGestionOrThrow(Integer idGestion) {
        return gestionRepository.findById(idGestion)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con ID: " + idGestion));
    }

    private static void validarPerteneceAGestion(DocumentoPresentado documento, Integer idGestion) {
        Tramite tramite = documento.getFkIdTramite();
        GestionDeEscritura gestion = tramite != null ? tramite.getFkIdGestion() : null;
        if (gestion == null || !idGestion.equals(gestion.getIdGestion())) {
            throw new BusinessValidationException(
                    "El documento " + documento.getIdDocumentoPresentado() + " no pertenece a la gestión "
                            + idGestion);
        }
    }

    private static void validarEsEntidadExterna(DocumentoPresentado documento) {
        if (!ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA.equals(documento.getQuienEntrega())) {
            throw new BusinessValidationException(
                    "El documento " + documento.getIdDocumentoPresentado() + " no es de entidad externa");
        }
    }

    private static void aplicarMovimiento(DocumentoPresentado documento,
            DtoMovimientoDocumentoEntidadExterna movimiento) {
        if (movimiento.preparado() != null) {
            documento.setPreparado(movimiento.preparado());
        }
        documento.setNumeroCarton(movimiento.numeroCarton());
        documento.setFechaIngreso(movimiento.fechaIngreso());
        documento.setFechaSalida(movimiento.fechaSalida());
        documento.setObservado(movimiento.observado());
        documento.setImporteAPagar(movimiento.importeAPagar());
        documento.setFechaPago(movimiento.fechaPago());
        documento.setFechaLiberado(movimiento.fechaLiberado());
        documento.setObservaciones(movimiento.observaciones());
        documento.setEntregado(movimiento.entregado());
    }

    private DtoGestionDocumentosEntidadesExternas toDto(GestionDeEscritura gestion,
            List<DocumentoPresentado> documentos) {
        return new DtoGestionDocumentosEntidadesExternas(
                gestion.getIdGestion(),
                gestion.getNumero(),
                gestion.getEncabezado(),
                gestion.getFechaInicio(),
                nombreEscribano(gestion.getFkIdPersonaEscribano()),
                resolverNomenclaturaCatastral(gestion.getIdGestion()),
                documentos.stream().map(DocumentoEntidadExternaService::toDto).toList());
    }

    private static String nombreEscribano(Persona escribano) {
        if (escribano == null) {
            return null;
        }
        return (escribano.getNombre() + " " + escribano.getApellido()).trim();
    }

    private String resolverNomenclaturaCatastral(Integer idGestion) {
        return tramiteRepository.findByFkIdGestionIdGestion(idGestion).stream()
                .map(Tramite::getFkIdInmueble)
                .filter(java.util.Objects::nonNull)
                .map(Inmueble::getNomenclaturaCatastral)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static DtoDocumentoEntidadExterna toDto(DocumentoPresentado documento) {
        return new DtoDocumentoEntidadExterna(
                documento.getIdDocumentoPresentado(),
                documento.getNombre(),
                documento.getPreparado(),
                documento.getNumeroCarton(),
                documento.getFechaIngreso(),
                documento.getFechaSalida(),
                documento.getObservado(),
                documento.getImporteAPagar(),
                documento.getFechaPago(),
                documento.getFechaLiberado(),
                documento.getObservaciones(),
                documento.getEntregado());
    }
}
