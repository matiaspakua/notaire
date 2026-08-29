package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoDocumentoNecesario;
import com.licensis.notaire.dto.DtoDocumentoReingresado;
import com.licensis.notaire.dto.DtoGestionReingresoDocumentacion;
import com.licensis.notaire.dto.DtoReingresoDocumentacionRequest;
import com.licensis.notaire.dto.DtoTramiteDocumentacionNecesaria;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.PlantillaTramitePK;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.DocumentoPresentadoRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.PlantillaTramiteRepository;
import com.licensis.notaire.repository.TramiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU43 - Reingresar documentación: expone, para una gestión, sus trámites
 * junto con la documentación necesaria de cada uno (según su
 * {@code PlantillaTramite}), y permite reingresar un tipo de documento
 * creando un nuevo {@code DocumentoPresentado} con {@code reingresado=true}.
 */
@Service
public class ReingresoDocumentacionService {

    private final GestionDeEscrituraRepository gestionRepository;
    private final TramiteRepository tramiteRepository;
    private final PlantillaTramiteRepository plantillaTramiteRepository;
    private final DocumentoPresentadoRepository documentoPresentadoRepository;

    public ReingresoDocumentacionService(GestionDeEscrituraRepository gestionRepository,
            TramiteRepository tramiteRepository, PlantillaTramiteRepository plantillaTramiteRepository,
            DocumentoPresentadoRepository documentoPresentadoRepository) {
        this.gestionRepository = gestionRepository;
        this.tramiteRepository = tramiteRepository;
        this.plantillaTramiteRepository = plantillaTramiteRepository;
        this.documentoPresentadoRepository = documentoPresentadoRepository;
    }

    @Transactional(readOnly = true)
    public DtoGestionReingresoDocumentacion obtenerDocumentacionNecesaria(Integer idGestion) {
        GestionDeEscritura gestion = findGestionOrThrow(idGestion);
        List<DtoTramiteDocumentacionNecesaria> tramites = tramiteRepository
                .findByFkIdGestionIdGestion(idGestion).stream()
                .map(this::toDtoTramite)
                .toList();
        return new DtoGestionReingresoDocumentacion(gestion.getIdGestion(), gestion.getNumero(),
                gestion.getEncabezado(), tramites);
    }

    @Transactional
    public DtoDocumentoReingresado reingresar(Integer idGestion, DtoReingresoDocumentacionRequest request) {
        findGestionOrThrow(idGestion);
        Tramite tramite = tramiteRepository.findById(request.idTramite())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trámite no encontrado con ID: " + request.idTramite()));
        validarPerteneceAGestion(tramite, idGestion);
        TipoDeDocumento tipoDocumento = validarDocumentacionNecesaria(tramite, request.idTipoDocumento());

        DocumentoPresentado documento = crearDocumentoPresentado(tramite, tipoDocumento);
        DocumentoPresentado guardado = documentoPresentadoRepository.save(documento);

        return toDto(guardado);
    }

    private GestionDeEscritura findGestionOrThrow(Integer idGestion) {
        return gestionRepository.findById(idGestion)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con ID: " + idGestion));
    }

    private static void validarPerteneceAGestion(Tramite tramite, Integer idGestion) {
        GestionDeEscritura gestion = tramite.getFkIdGestion();
        if (gestion == null || !idGestion.equals(gestion.getIdGestion())) {
            throw new BusinessValidationException(
                    "El trámite " + tramite.getIdTramite() + " no pertenece a la gestión " + idGestion);
        }
    }

    private TipoDeDocumento validarDocumentacionNecesaria(Tramite tramite, Integer idTipoDocumento) {
        Integer idTipoTramite = tramite.getFkIdTipoTramite().getIdTipoTramite();
        PlantillaTramitePK pk = new PlantillaTramitePK(idTipoTramite, idTipoDocumento);
        PlantillaTramite plantilla = plantillaTramiteRepository.findById(pk)
                .orElseThrow(() -> new BusinessValidationException(
                        "El tipo de documento " + idTipoDocumento
                                + " no forma parte de la documentación necesaria del trámite "
                                + tramite.getIdTramite()));
        return plantilla.getTipoDeDocumento();
    }

    private static DocumentoPresentado crearDocumentoPresentado(Tramite tramite, TipoDeDocumento tipoDocumento) {
        DocumentoPresentado documento = new DocumentoPresentado();
        documento.setFkIdTramite(tramite);
        documento.setFkIdTipoDocumento(tipoDocumento.getIdTipoDocumento());
        documento.setNombre(tipoDocumento.getNombre());
        documento.setVence(tipoDocumento.getVence());
        documento.setDiasVencimiento(tipoDocumento.getDiasVencimiento());
        documento.setQuienEntrega(tipoDocumento.getQuienEntrega());
        documento.setReingresado(true);
        // liberado/observado are NOT NULL in Postgres; a freshly reingresado
        // document starts as neither liberado nor observado.
        documento.setLiberado(false);
        documento.setObservado(false);
        return documento;
    }

    private DtoTramiteDocumentacionNecesaria toDtoTramite(Tramite tramite) {
        Integer idTipoTramite = tramite.getFkIdTipoTramite().getIdTipoTramite();
        List<DtoDocumentoNecesario> documentos = plantillaTramiteRepository
                .findByTipoDeTramiteIdTipoTramite(idTipoTramite).stream()
                .map(plantilla -> toDtoDocumentoNecesario(plantilla.getTipoDeDocumento()))
                .toList();
        return new DtoTramiteDocumentacionNecesaria(tramite.getIdTramite(),
                tramite.getFkIdTipoTramite().getNombre(), documentos);
    }

    private static DtoDocumentoNecesario toDtoDocumentoNecesario(TipoDeDocumento tipoDocumento) {
        return new DtoDocumentoNecesario(tipoDocumento.getIdTipoDocumento(), tipoDocumento.getNombre(),
                tipoDocumento.getVence(), tipoDocumento.getDiasVencimiento(), tipoDocumento.getQuienEntrega());
    }

    private static DtoDocumentoReingresado toDto(DocumentoPresentado documento) {
        return new DtoDocumentoReingresado(
                documento.getIdDocumentoPresentado(),
                documento.getFkIdTramite().getIdTramite(),
                documento.getFkIdTipoDocumento(),
                documento.getNombre(),
                documento.getVence(),
                documento.getDiasVencimiento(),
                documento.getQuienEntrega(),
                Boolean.TRUE.equals(documento.getReingresado()));
    }
}
