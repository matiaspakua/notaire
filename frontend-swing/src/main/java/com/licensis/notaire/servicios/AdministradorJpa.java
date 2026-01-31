package com.licensis.notaire.servicios;

import com.licensis.notaire.api.client.ApiConfig;
import com.licensis.notaire.api.client.BaseRestClient;
import com.licensis.notaire.dto.GenericDto;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdministradorJpa que usa capa REST en lugar de JPA directo
 * Proporciona acceso a servicios REST del backend
 */
public class AdministradorJpa {
    
    private static AdministradorJpa instancia = null;
    private static final Logger logger = Logger.getLogger(AdministradorJpa.class.getName());
    
    // Clientes REST generales para cada entidad
    private GenericRestClient conceptoClient;
    private GenericRestClient copiaClient;
    private GenericRestClient documentoPresentadoClient;
    private GenericRestClient escrituraClient;
    private GenericRestClient estadoDeGestionClient;
    private GenericRestClient folioClient;
    private GenericRestClient historialClient;
    private GenericRestClient inmuebleClient;
    private GenericRestClient itemClient;
    private GenericRestClient movimientoTestimonioClient;
    private GenericRestClient pagoClient;
    private GenericRestClient personaClient;
    private GenericRestClient presupuestoClient;
    private GenericRestClient suplenciaClient;
    private GenericRestClient testimonioClient;
    private GenericRestClient tipoDeDocumentoClient;
    private GenericRestClient tipoDeFolioClient;
    private GenericRestClient tipoDeTramiteClient;
    private GenericRestClient tipoIdentificacionClient;
    private GenericRestClient tramiteClient;
    private GenericRestClient usuarioClient;
    
    /**
     * Constructor privado (Singleton)
     */
    private AdministradorJpa() {
        ApiConfig.loadFromProperties();
        
        // Inicializa todos los clientes REST (paths deben coincidir con backend /api/v1/...)
        conceptoClient = new GenericRestClient("/conceptos");
        copiaClient = new GenericRestClient("/copia");
        documentoPresentadoClient = new GenericRestClient("/documento-presentado");
        escrituraClient = new GenericRestClient("/escrituras");
        estadoDeGestionClient = new GenericRestClient("/estado-gestion");
        folioClient = new GenericRestClient("/folio");
        historialClient = new GenericRestClient("/historial");
        inmuebleClient = new GenericRestClient("/inmueble");
        itemClient = new GenericRestClient("/items");
        movimientoTestimonioClient = new GenericRestClient("/movimiento-testimonio");
        pagoClient = new GenericRestClient("/pago");
        personaClient = new GenericRestClient("/personas");
        presupuestoClient = new GenericRestClient("/presupuestos");
        suplenciaClient = new GenericRestClient("/suplencia");
        testimonioClient = new GenericRestClient("/testimonio");
        tipoDeDocumentoClient = new GenericRestClient("/tipo-de-documento");
        tipoDeFolioClient = new GenericRestClient("/tipo-folio");
        tipoDeTramiteClient = new GenericRestClient("/tipo-tramite");
        tipoIdentificacionClient = new GenericRestClient("/tipo-identificacion");
        tramiteClient = new GenericRestClient("/tramites");
        usuarioClient = new GenericRestClient("/usuarios");
        
        logger.info("AdministradorJpa REST inicializado con GenericDto. Base URL: " + ApiConfig.getApiBaseUrl());
    }
    
    /**
     * Obtiene la instancia singleton
     */
    public static AdministradorJpa getInstancia() {
        if (instancia == null) {
            instancia = new AdministradorJpa();
        }
        return instancia;
    }
    
    // ===================== Accesos a clientes REST =====================
    
    public GenericRestClient getConceptoJpa() {
        return conceptoClient;
    }
    
    public GenericRestClient getCopiaJpa() {
        return copiaClient;
    }
    
    public GenericRestClient getDocumentoPresentadoJpa() {
        return documentoPresentadoClient;
    }
    
    public GenericRestClient getEscrituraJpa() {
        return escrituraClient;
    }
    
    public GenericRestClient getEstadoDeGestionJpa() {
        return estadoDeGestionClient;
    }
    
    public GenericRestClient getFolioJpa() {
        return folioClient;
    }
    
    public GenericRestClient getHistorialJpa() {
        return historialClient;
    }
    
    public GenericRestClient getInmuebleJpa() {
        return inmuebleClient;
    }
    
    public GenericRestClient getItemJpa() {
        return itemClient;
    }
    
    public GenericRestClient getMovimientoTestimonioJpa() {
        return movimientoTestimonioClient;
    }
    
    public GenericRestClient getPagoJpa() {
        return pagoClient;
    }
    
    public GenericRestClient getPersonaJpa() {
        return personaClient;
    }
    
    public GenericRestClient getPresupuestoJpa() {
        return presupuestoClient;
    }
    
    public GenericRestClient getSuplenciaJpa() {
        return suplenciaClient;
    }
    
    public GenericRestClient getTestimonioJpa() {
        return testimonioClient;
    }
    
    public GenericRestClient getTipoDeDocumentoJpa() {
        return tipoDeDocumentoClient;
    }
    
    public GenericRestClient getTipoDeFolioJpa() {
        return tipoDeFolioClient;
    }

    public GenericRestClient getTipoDeTramiteJpa() {
        return tipoDeTramiteClient;
    }

    public GenericRestClient getTipoIdentificacionJpa() {
        return tipoIdentificacionClient;
    }
    
    public GenericRestClient getTramiteJpa() {
        return tramiteClient;
    }
    
    public GenericRestClient getUsuarioJpa() {
        return usuarioClient;
    }
    
    /**
     * Test de conectividad
     */
    public void testConexion() {
        try {
            conceptoClient.findAll();
            logger.info("Conexión exitosa a la API REST del backend");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al conectar con la API: " + e.getMessage(), e);
            throw new RuntimeException("No se puede conectar con la API: " + e.getMessage());
        }
    }
}
