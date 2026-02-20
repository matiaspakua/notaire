package com.licensis.notaire.api.client;

import com.licensis.notaire.dto.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Maps GenericDto (REST API responses) to domain DTOs.
 * Used by Swing forms migrated from ControllerNegocio to REST.
 */
public final class RestMapper {

    private RestMapper() {}

    @SuppressWarnings("unchecked")
    static GenericDto asGenericDto(Object value) {
        if (value instanceof GenericDto gd) {
            return gd;
        }
        if (value instanceof Map<?, ?> map) {
            GenericDto dto = new GenericDto();
            dto.setData((Map<String, Object>) map);
            return dto;
        }
        return null;
    }

    public static DtoTipoDeTramite toDtoTipoTramite(GenericDto dto) {
        DtoTipoDeTramite tipo = new DtoTipoDeTramite();
        if (dto == null) return tipo;
        tipo.setIdTipoTramite(dto.getInt("idTipoTramite"));
        tipo.setNombre(dto.getString("nombre"));
        tipo.setObservaciones(dto.getString("observaciones"));
        tipo.setAsociaInmuebles(Boolean.TRUE.equals(dto.getBoolean("asociaInmuebles")));
        tipo.setHabilitado(Boolean.TRUE.equals(dto.getBoolean("habilitado")));
        return tipo;
    }

    public static DtoTipoDeDocumento toDtoTipoDeDocumento(GenericDto dto) {
        DtoTipoDeDocumento tipo = new DtoTipoDeDocumento();
        if (dto == null) return tipo;
        tipo.setIdTipoDocumento(dto.getInt("idTipoDocumento"));
        tipo.setNombre(dto.getString("nombre"));
        tipo.setVence(Boolean.TRUE.equals(dto.getBoolean("vence")));
        tipo.setDiasVencimiento(dto.getInt("diasVencimiento"));
        tipo.setQuienEntrega(dto.getString("quienEntrega"));
        return tipo;
    }

    public static DtoPlantillaTramite toDtoPlantillaTramite(GenericDto dto) {
        DtoPlantillaTramite pt = new DtoPlantillaTramite();
        if (dto == null) return pt;
        pt.setObservaciones(dto.getString("observaciones"));
        GenericDto tipoDoc = asGenericDto(dto.get("tipoDeDocumento"));
        if (tipoDoc != null) pt.setTiposDeDocumento(toDtoTipoDeDocumento(tipoDoc));
        return pt;
    }

    public static DtoDocumentoPresentado toDtoDocumentoPresentado(GenericDto dto) {
        DtoDocumentoPresentado doc = new DtoDocumentoPresentado();
        if (dto == null) return doc;
        doc.setIdDocumentoPresentado(dto.getInt("idDocumentoPresentado"));
        doc.setNombre(dto.getString("nombre"));
        doc.setNumeroCarton(dto.getInt("numeroCarton"));
        doc.setDiasVencimiento(dto.getInt("diasVencimiento"));
        doc.setObservaciones(dto.getString("observaciones"));
        doc.setPreparado(Boolean.TRUE.equals(dto.getBoolean("preparado")));
        doc.setVence(Boolean.TRUE.equals(dto.getBoolean("vence")));
        doc.setLiberado(Boolean.TRUE.equals(dto.getBoolean("liberado")));
        doc.setObservado(Boolean.TRUE.equals(dto.getBoolean("observado")));
        doc.setEntregado(Boolean.TRUE.equals(dto.getBoolean("entregado")));
        Object importe = dto.get("importeAPagar");
        if (importe instanceof Number n) doc.setImporteApagar(n.floatValue());
        Object fechaIng = dto.get("fechaIngreso");
        if (fechaIng instanceof Number n) doc.setFechaIngreso(new java.util.Date(n.longValue()));
        Object fechaSal = dto.get("fechaSalida");
        if (fechaSal instanceof Number n) doc.setFechaSalida(new java.util.Date(n.longValue()));
        Object fechaVen = dto.get("fechaVencimiento");
        if (fechaVen instanceof Number n) doc.setFechaVencimiento(new java.util.Date(n.longValue()));
        Object fechaPag = dto.get("fechaPago");
        if (fechaPag instanceof Number n) doc.setFechaPago(new java.util.Date(n.longValue()));
        Object fechaLib = dto.get("fechaLiberado");
        if (fechaLib instanceof Number n) doc.setFechaLiberado(new java.util.Date(n.longValue()));
        doc.setQuienEntrega(dto.getString("quienEntrega"));
        GenericDto tramiteDto = asGenericDto(dto.get("fkIdTramite"));
        if (tramiteDto != null) doc.setFkTramite(toDtoTramiteMinimal(tramiteDto));
        return doc;
    }

    public static DtoEstadoDeGestion toDtoEstado(GenericDto dto) {
        DtoEstadoDeGestion estado = new DtoEstadoDeGestion();
        if (dto == null) return estado;
        estado.setIdEstadoGestion(dto.getInt("idEstadoGestion"));
        estado.setNombre(dto.getString("nombre"));
        estado.setObservaciones(dto.getString("observaciones"));
        return estado;
    }

    public static DtoPersona toDtoPersona(GenericDto dto) {
        DtoPersona persona = new DtoPersona();
        if (dto == null) return persona;
        persona.setIdPersona(dto.getInt("idPersona"));
        persona.setNombre(dto.getString("nombre"));
        persona.setApellido(dto.getString("apellido"));
        persona.setNumeroIdentificacion(dto.getString("numeroIdentificacion"));
        persona.setTelefono(dto.getString("telefono"));
        persona.setDomicilio(dto.getString("domicilio"));
        String email = dto.getString("eMail");
        if (email == null) email = dto.getString("email");
        persona.setEmail(email);
        return persona;
    }

    public static DtoGestionDeEscritura toDtoGestion(GenericDto dto) {
        DtoGestionDeEscritura gestion = new DtoGestionDeEscritura();
        if (dto == null) return gestion;
        gestion.setIdGestion(dto.getInt("idGestion"));
        Integer numero = dto.getInt("numero");
        if (numero != null) gestion.setNumero(numero);
        gestion.setEncabezado(dto.getString("encabezado"));
        gestion.setObservaciones(dto.getString("observaciones"));
        gestion.setNumeroBibliorato(dto.getInt("numeroBibliorato"));
        gestion.setNumeroArchivo(dto.getInt("numeroArchivo"));
        Object fecha = dto.get("fechaInicio");
        if (fecha instanceof Date date) gestion.setFechaInicio(date);
        else if (fecha instanceof Number n) gestion.setFechaInicio(new Date(n.longValue()));
        GenericDto estado = asGenericDto(dto.get("fkIdEstadoDeGestion"));
        if (estado != null) gestion.setEstado(toDtoEstado(estado));
        GenericDto escribano = asGenericDto(dto.get("fkIdPersonaEscribano"));
        if (escribano != null) gestion.setPersonaEscribano(toDtoPersona(escribano));
        GenericDto cliente = asGenericDto(dto.get("clienteReferencia"));
        if (cliente == null && dto.get("listaClientesInvolucrados") instanceof List<?> list && !list.isEmpty()) {
            cliente = asGenericDto(list.get(0));
        }
        if (cliente != null) gestion.setClienteReferencia(toDtoPersona(cliente));
        return gestion;
    }

    /** Maps Presupuesto API response to DtoPresupuesto (avoids circular ref with tramite). */
    public static DtoPresupuesto toDtoPresupuesto(GenericDto dto) {
        DtoPresupuesto pres = new DtoPresupuesto();
        if (dto == null) return pres;
        pres.setIdPresupuesto(dto.getInt("idPresupuesto"));
        pres.setTotal(dto.getFloat("total"));
        pres.setSaldo(dto.getFloat("saldo"));
        pres.setObservaciones(dto.getString("observaciones"));
        Object fecha = dto.get("fecha");
        if (fecha instanceof Date date) pres.setFecha(date);
        else if (fecha instanceof Number n) pres.setFecha(new Date(n.longValue()));
        GenericDto tramiteDto = asGenericDto(dto.get("fkIdTramite"));
        if (tramiteDto != null) {
            DtoTramite tramite = new DtoTramite();
            tramite.setIdTramite(tramiteDto.getInt("idTramite"));
            GenericDto gestion = asGenericDto(tramiteDto.get("fkIdGestion"));
            if (gestion != null) tramite.setGestionDeEscritura(toDtoGestion(gestion));
            GenericDto tipoTramite = asGenericDto(tramiteDto.get("fkIdTipoTramite"));
            if (tipoTramite != null) tramite.setTiposDeTramite(toDtoTipoTramite(tipoTramite));
            pres.setTramite(tramite);
        }
        return pres;
    }

    /** Tramite with gestion + tipoDeTramite (for documento display). */
    private static DtoTramite toDtoTramiteMinimal(GenericDto dto) {
        DtoTramite tramite = new DtoTramite();
        if (dto == null) return tramite;
        tramite.setIdTramite(dto.getInt("idTramite"));
        GenericDto gestion = asGenericDto(dto.get("fkIdGestion"));
        if (gestion != null) tramite.setGestionDeEscritura(toDtoGestion(gestion));
        GenericDto tipoTramite = asGenericDto(dto.get("fkIdTipoTramite"));
        if (tipoTramite != null) tramite.setTiposDeTramite(toDtoTipoTramite(tipoTramite));
        return tramite;
    }

    public static DtoTramite toDtoTramite(GenericDto dto) {
        DtoTramite tramite = new DtoTramite();
        if (dto == null) return tramite;
        tramite.setIdTramite(dto.getInt("idTramite"));
        tramite.setObservaciones(dto.getString("observaciones"));
        GenericDto presupuesto = asGenericDto(dto.get("fkIdPresupuesto"));
        if (presupuesto != null) tramite.setPresupuesto(toDtoPresupuesto(presupuesto));
        GenericDto tipoTramite = asGenericDto(dto.get("fkIdTipoTramite"));
        if (tipoTramite != null) tramite.setTiposDeTramite(toDtoTipoTramite(tipoTramite));
        GenericDto gestion = asGenericDto(dto.get("fkIdGestion"));
        if (gestion != null) tramite.setGestionDeEscritura(toDtoGestion(gestion));
        return tramite;
    }

    public static DtoHistorial toDtoHistorial(GenericDto dto) {
        DtoHistorial hist = new DtoHistorial();
        if (dto == null) return hist;
        hist.setIdHistorial(dto.getInt("idHistorial"));
        hist.setObservaciones(dto.getString("observaciones"));
        Object fecha = dto.get("fecha");
        if (fecha instanceof Date date) hist.setFecha(date);
        else if (fecha instanceof Number n) hist.setFecha(new Date(n.longValue()));
        GenericDto estado = asGenericDto(dto.get("fkIdEstadoDeGestion"));
        if (estado != null) hist.setEstadosDeGestion(toDtoEstado(estado));
        return hist;
    }

    public static DtoItem toDtoItem(GenericDto dto) {
        DtoItem item = new DtoItem();
        if (dto == null) return item;
        item.setIdItem(dto.getInt("idItem"));
        item.setNombre(dto.getString("nombre"));
        item.setValor(dto.getFloat("valor"));
        item.setPorcentaje(dto.getInt("porcentaje"));
        item.setObservaciones(dto.getString("observaciones"));
        item.setConceptoFijo(Boolean.TRUE.equals(dto.getBoolean("fijo")));
        return item;
    }
}
