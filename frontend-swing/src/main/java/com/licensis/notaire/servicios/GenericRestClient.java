package com.licensis.notaire.servicios;

import com.licensis.notaire.api.client.BaseRestClient;
import com.licensis.notaire.dto.GenericDto;

/**
 * Cliente REST genérico que usa GenericDto
 * No requiere tipos específicos, permite trabajar con cualquier entidad
 */
public class GenericRestClient extends BaseRestClient<GenericDto> {
    
    private String endpoint;
    
    public GenericRestClient(String endpoint) {
        super(endpoint, GenericDto.class);
        this.endpoint = endpoint;
    }
    
    @Override
    protected Integer extractId(GenericDto entity) {
        // Intenta extraer el ID de varios campos comunes
        Object id = entity.get("id");
        if (id != null) {
            if (id instanceof Integer) return (Integer) id;
            if (id instanceof Number) return ((Number) id).intValue();
        }
        
        // Intenta varios nombres comunes de campo ID
        String[] idFieldNames = {"idConcepto", "idCopia", "idDocumentoPresentado", "idEscritura",
            "idEstadoGestion", "idFolio", "idHistorial", "idInmueble", "idItem", "idMovimientoTestimonio",
            "idPago", "idPresupuesto", "idSuplencia", "idTestimonio", "idTipoDocumento",
            "idTipoIdentificacion", "idTramite", "idUsuario"};
        
        for (String fieldName : idFieldNames) {
            Object val = entity.get(fieldName);
            if (val != null) {
                if (val instanceof Integer) return (Integer) val;
                if (val instanceof Number) return ((Number) val).intValue();
            }
        }
        
        return null;
    }
    
    @Override
    public String getNombreJpa() {
        // Retorna un nombre genérico
        return "com.licensis.notaire.api.client.GenericRestClient:" + endpoint;
    }
}
