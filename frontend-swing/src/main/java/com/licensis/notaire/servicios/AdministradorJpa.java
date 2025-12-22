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
    private GenericRestClient folioClient;
    private GenericRestClient historialClient;
    private GenericRestClient inmuebleClient;
    private GenericRestClient itemClient;
    private GenericRestClient movimientoTestimonioClient;
    private GenericRestClient pagoClient;
    private GenericRestClient presupuestoClient;
    private GenericRestClient suplenciaClient;
    private GenericRestClient testimonioClient;
    private GenericRestClient tipoDeDocumentoClient;
    private GenericRestClient tipoIdentificacionClient;
    private GenericRestClient tramiteClient;
    
    /**
     * Constructor privado (Singleton)
     */
    private AdministradorJpa() {
        ApiConfig.loadFromProperties();
        
        // Inicializa todos los clientes REST genéricos
        conceptoClient = new GenericRestClient("/conceptos");
        copiaClient = new GenericRestClient("/copias");
        documentoPresentadoClient = new GenericRestClient("/documentos-presentados");
        escrituraClient = new GenericRestClient("/escrituras");
        folioClient = new GenericRestClient("/folios");
        historialClient = new GenericRestClient("/historiales");
        inmuebleClient = new GenericRestClient("/inmuebles");
        itemClient = new GenericRestClient("/items");
        movimientoTestimonioClient = new GenericRestClient("/movimientos-testimonios");
        pagoClient = new GenericRestClient("/pagos");
        presupuestoClient = new GenericRestClient("/presupuestos");
        suplenciaClient = new GenericRestClient("/suplencias");
        testimonioClient = new GenericRestClient("/testimonios");
        tipoDeDocumentoClient = new GenericRestClient("/tipos-documentos");
        tipoIdentificacionClient = new GenericRestClient("/tipos-identificacion");
        tramiteClient = new GenericRestClient("/tramites");
        
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
    
    public GenericRestClient getTipoIdentificacionJpa() {
        return tipoIdentificacionClient;
    }
    
    public GenericRestClient getTramiteJpa() {
        return tramiteClient;
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
