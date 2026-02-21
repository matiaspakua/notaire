package com.licensis.notaire.api.client;

import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.dto.DtoEstadoDeGestion;
import com.licensis.notaire.dto.DtoFlag;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.dto.GenericDto;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.servicios.AdministradorJpa;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper for documentacion forms using REST API.
 * Replaces ControllerNegocio calls for obtenerDocNecesario, buscarDtoGestion, modificarDocumentacion, etc.
 */
public final class DocumentacionRestHelper {

    private static final Logger LOG = Logger.getLogger(DocumentacionRestHelper.class.getName());
    private static final AdministradorJpa ADMIN = AdministradorJpa.getInstancia();

    private DocumentacionRestHelper() {}

    /** Resolves estado by name. */
    public static DtoEstadoDeGestion obtenerDtoEstadoDeGestion(DtoEstadoDeGestion dto) {
        if (dto == null || dto.getNombre() == null) return dto;
        try {
            for (GenericDto raw : ADMIN.getEstadoDeGestionJpa().findAll()) {
                if (dto.getNombre().equals(raw.getString("nombre"))) {
                    return RestMapper.toDtoEstado(raw);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error obteniendo estado de gestion", e);
        }
        return new DtoEstadoDeGestion("Estado no valido");
    }

    /** Fetches full gestion with tramites. */
    public static DtoGestionDeEscritura buscarDtoGestion(DtoGestionDeEscritura dto) {
        if (dto == null) return null;
        try {
            GenericDto gestionRaw = null;
            if (dto.getIdGestion() != null && dto.getIdGestion() > 0) {
                gestionRaw = ADMIN.getGestionJpa().find(dto.getIdGestion());
            } else if (dto.getNumero() > 0) {
                for (GenericDto g : ADMIN.getGestionJpa().findAll()) {
                    if (Objects.equals(g.getInt("numero"), dto.getNumero())) {
                        gestionRaw = g;
                        break;
                    }
                }
            }
            if (gestionRaw == null) return dto;

            DtoGestionDeEscritura gestion = RestMapper.toDtoGestion(gestionRaw);
            if (gestion.getListaTramitesAsociados().isEmpty()) {
                for (GenericDto tramiteRaw : ADMIN.getTramiteJpa().findAll()) {
                    DtoTramite tramite = RestMapper.toDtoTramite(tramiteRaw);
                    if (tramite.getGestion() != null
                            && Objects.equals(tramite.getGestion().getIdGestion(), gestion.getIdGestion())) {
                        gestion.getListaTramitesAsociados().add(tramite);
                    }
                }
            }
            return gestion;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error buscando gestion", e);
            return dto;
        }
    }

    /** Populates tramites with listaDocumentosGestion from documento-presentado. */
    public static DtoGestionDeEscritura obtenerDocNecesarioEntregadosNoEntregadosDeGestion(DtoGestionDeEscritura dto) {
        if (dto == null) return null;
        DtoGestionDeEscritura gestion = buscarDtoGestion(dto);
        if (gestion == null) return dto;
        try {
            List<GenericDto> documentos = ADMIN.getDocumentoPresentadoJpa().findAll();
            for (DtoTramite tramite : gestion.getListaTramitesAsociados()) {
                tramite.getListaDocumentosGestion().clear();
                tramite.getListaDocumentosNoPrecentados().clear();
                tramite.getListaDocumentosNecesarios().clear();
                for (GenericDto docRaw : documentos) {
                    DtoDocumentoPresentado doc = RestMapper.toDtoDocumentoPresentado(docRaw);
                    if (doc.getFkTramite() != null && doc.getFkTramite().getIdTramite() != null
                            && Objects.equals(doc.getFkTramite().getIdTramite(), tramite.getIdTramite())) {
                        tramite.getListaDocumentosGestion().add(doc);
                    }
                }
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error obteniendo documentacion de gestion", e);
        }
        return gestion;
    }

    private static GenericDto tramiteRef(int idTramite) {
        GenericDto ref = new GenericDto();
        ref.put("idTramite", idTramite);
        return ref;
    }

    private static GenericDto estadoRef(int idEstado) {
        GenericDto ref = new GenericDto();
        ref.put("idEstadoGestion", idEstado);
        return ref;
    }

    /** Updates/creates documentos via REST. On success, calls iscompletaDocumentacion. */
    public static DtoFlag modificarDocumentacion(ArrayList<DtoDocumentoPresentado> lista, DtoGestionDeEscritura dto) {
        DtoFlag resultado = new DtoFlag();
        resultado.setFlag(false);
        if (lista == null || lista.isEmpty()) return resultado;
        boolean ok = true;
        for (DtoDocumentoPresentado documento : lista) {
            try {
                GenericDto payload = new GenericDto();
                payload.put("idDocumentoPresentado", documento.getIdDocumentoPresentado());
                payload.put("nombre", documento.getNombre());
                payload.put("numeroCarton", documento.getNumeroCarton());
                payload.put("fechaIngreso", documento.getFechaIngreso());
                payload.put("fechaSalida", documento.getFechaSalida());
                payload.put("preparado", documento.isPreparado());
                payload.put("vence", documento.isVence());
                payload.put("fechaVencimiento", documento.getFechaVencimiento());
                payload.put("diasVencimiento", documento.getDiasVencimiento());
                payload.put("importeAPagar", documento.getImporteAPagar());
                payload.put("fechaPago", documento.getFechaPago());
                payload.put("liberado", documento.isLiberado());
                payload.put("fechaLiberado", documento.getFechaLiberado());
                payload.put("observado", documento.isObservado());
                payload.put("observaciones", documento.getObservaciones());
                payload.put("reingresado", documento.isReingresado());
                payload.put("quienEntrega", documento.getQuienEntrega());
                payload.put("entregado", documento.isEntregado());
                if (documento.getFkTramite() != null && documento.getFkTramite().getIdTramite() != null) {
                    payload.put("fkIdTramite", tramiteRef(documento.getFkTramite().getIdTramite()));
                }
                if (documento.getIdDocumentoPresentado() != null && documento.getIdDocumentoPresentado() > 0) {
                    ADMIN.getDocumentoPresentadoJpa().edit(payload);
                } else {
                    ADMIN.getDocumentoPresentadoJpa().create(payload);
                }
            } catch (Exception e) {
                ok = false;
                LOG.log(Level.WARNING, "Error modificando documentacion", e);
            }
        }
        resultado.setFlag(ok);
        if (ok && dto != null) {
            iscompletaDocumentacion(dto);
        }
        return resultado;
    }

    /** If all docs entregado, updates gestion estado to Documentacion Completa. */
    public static boolean iscompletaDocumentacion(DtoGestionDeEscritura dto) {
        if (dto == null) return false;
        for (DtoTramite tramite : dto.getListaTramitesAsociados()) {
            for (DtoDocumentoPresentado documento : tramite.getListaDocumentosGestion()) {
                if (!documento.isEntregado()) return false;
            }
        }
        DtoEstadoDeGestion nuevoEstado = new DtoEstadoDeGestion();
        nuevoEstado.setNombre(ConstantesNegocio.DOCUMENTACION_COMPLETA);
        DtoEstadoDeGestion estado = obtenerDtoEstadoDeGestion(nuevoEstado);
        dto.setEstado(estado);
        modificarEstadoDeGestionDeEscritura(dto);
        registrarMovimientoHistorial(dto);
        return true;
    }

    private static boolean modificarEstadoDeGestionDeEscritura(DtoGestionDeEscritura dto) {
        if (dto == null || dto.getIdGestion() == null || dto.getEstado() == null
                || dto.getEstado().getIdEstadoGestion() == null) return false;
        try {
            GenericDto gestion = ADMIN.getGestionJpa().find(dto.getIdGestion());
            GenericDto payload = new GenericDto();
            payload.put("idGestion", dto.getIdGestion());
            payload.put("numero", gestion.getInt("numero"));
            payload.put("fechaInicio", gestion.get("fechaInicio"));
            payload.put("encabezado", gestion.getString("encabezado"));
            payload.put("observaciones", gestion.getString("observaciones"));
            payload.put("numeroArchivo", gestion.getInt("numeroArchivo"));
            payload.put("numeroBibliorato", gestion.getInt("numeroBibliorato"));
            GenericDto escribano = RestMapper.asGenericDto(gestion.get("fkIdPersonaEscribano"));
            if (escribano != null) payload.put("fkIdPersonaEscribano", escribano);
            payload.put("fkIdEstadoDeGestion", estadoRef(dto.getEstado().getIdEstadoGestion()));
            ADMIN.getGestionJpa().edit(payload);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error modificando estado de gestion", e);
            return false;
        }
    }

    private static boolean registrarMovimientoHistorial(DtoGestionDeEscritura dto) {
        if (dto == null || dto.getIdGestion() == null || dto.getEstado() == null
                || dto.getEstado().getIdEstadoGestion() == null) return false;
        try {
            GenericDto payload = new GenericDto();
            payload.put("fecha", new Date());
            payload.put("observaciones", "Gestion: " + dto.getNumero() + ", Estado: " + dto.getEstado().getNombre());
            GenericDto gestionRef = new GenericDto();
            gestionRef.put("idGestion", dto.getIdGestion());
            payload.put("fkIdGestion", gestionRef);
            payload.put("fkIdEstadoGestion", estadoRef(dto.getEstado().getIdEstadoGestion()));
            ADMIN.getHistorialJpa().create(payload);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error registrando movimiento historial", e);
            return false;
        }
    }

    public static boolean documentacionCompletaCliente(DtoGestionDeEscritura dto) {
        if (dto == null) return false;
        for (DtoTramite tramite : dto.getListaTramitesAsociados()) {
            for (DtoDocumentoPresentado documento : tramite.getListaDocumentosGestion()) {
                if (ConstantesNegocio.DOCUMENTACION_CLIENTE.equals(documento.getQuienEntrega())
                        && !documento.isEntregado()) return false;
            }
        }
        return true;
    }

    /** Wrapper for reingreso flow. */
    public static DtoFlag modificarDocumentacionReingreso(ArrayList<DtoDocumentoPresentado> lista,
            DtoGestionDeEscritura dto) {
        return modificarDocumentacion(lista, dto);
    }

    /** Wrapper for entidades externas flow. */
    public static DtoFlag modificarDocumentacionEntidadesExternas(ArrayList<DtoDocumentoPresentado> lista,
            DtoGestionDeEscritura dto) {
        return modificarDocumentacion(lista, dto);
    }

    public static boolean documentacionCompletaExterna(DtoGestionDeEscritura dto) {
        if (dto == null) return false;
        for (DtoTramite tramite : dto.getListaTramitesAsociados()) {
            for (DtoDocumentoPresentado documento : tramite.getListaDocumentosGestion()) {
                if (ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA.equals(documento.getQuienEntrega())
                        && !documento.isEntregado()) return false;
            }
        }
        return true;
    }
}
