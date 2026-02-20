/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.servicios;

import com.licensis.notaire.api.client.RestClient;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Clase que se encarga de generar todos los reportes del sistema.
 * Versión migrada que utiliza la API REST del backend para generar PDFs.
 * 
 * Los reportes se generan en el backend y se descargan como archivos PDF
 * temporales que se abren con el visor de PDF predeterminado del sistema.
 *
 * @author matias
 */
public class AdministradorReportes {

    private static AdministradorReportes instancia = null;
    private static final Logger logger = Logger.getLogger(AdministradorReportes.class.getName());
    
    // Directorio temporal para guardar PDFs
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /**
     * Constructor privado para AdministradorReportes.
     */
    private AdministradorReportes() {
    }

    public static AdministradorReportes getInstancia() {
        if (instancia == null) {
            instancia = new AdministradorReportes();
        }
        return instancia;
    }

    /**
     * Genera el reporte correspondiente a la creacion de un nuevo presupuesto.
     * Elige automáticamente el template según si tiene inmuebles o no.
     *
     * @param idPresupuesto El ID del presupuesto para generar el reporte.
     * @param tieneInmuebles Indica si el presupuesto tiene inmuebles asociados.
     */
    public void generarReportePresupuesto(Integer idPresupuesto, boolean tieneInmuebles) throws IOException {
        try {
            String endpoint;
            if (tieneInmuebles) {
                endpoint = "/reportes/presupuesto-inmuebles/" + idPresupuesto;
            } else {
                endpoint = "/reportes/presupuesto/" + idPresupuesto;
            }
            
            byte[] pdfBytes = RestClient.getBytes(endpoint);
            String fileName = "presupuesto_" + idPresupuesto + ".pdf";
            abrirPdfEnVisor(pdfBytes, fileName);
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error al generar reporte de presupuesto", ex);
            JOptionPane.showMessageDialog(null, 
                "Error al generar el reporte: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            throw ex;
        }
    }

    /**
     * Genera reporte de presupuesto sin inmuebles (método legacy para compatibilidad)
     */
    public void generarReportePresupuesto(Integer idPresupuesto) throws IOException {
        generarReportePresupuesto(idPresupuesto, false);
    }

    /**
     * Genera el reporte correspondiente a la lista de documentacion asociada a un
     * determinado trámite.
     *
     * @param nombreTipoTramite El nombre del tipo de trámite.
     */
    public void generarReporteListaDocumentos(String nombreTipoTramite) throws IOException {
        try {
            String endpoint = "/reportes/lista-documentos-tramite?nombreTipoTramite=" + 
                             java.net.URLEncoder.encode(nombreTipoTramite, "UTF-8");
            
            byte[] pdfBytes = RestClient.getBytes(endpoint);
            String fileName = "lista_documentos_" + nombreTipoTramite.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
            abrirPdfEnVisor(pdfBytes, fileName);
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error al generar reporte de lista de documentos", ex);
            JOptionPane.showMessageDialog(null, 
                "Error al generar el reporte: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            throw ex;
        }
    }

    /**
     * Genera el reporte correspondiente al registro del historial de una gestion en
     * particular.
     *
     * @param idGestion El ID de la gestión.
     */
    public void generarReporteHistorialGestion(Integer idGestion) throws IOException {
        try {
            String endpoint = "/reportes/historial-gestion/" + idGestion;
            
            byte[] pdfBytes = RestClient.getBytes(endpoint);
            String fileName = "historial_gestion_" + idGestion + ".pdf";
            abrirPdfEnVisor(pdfBytes, fileName);
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error al generar reporte de historial", ex);
            JOptionPane.showMessageDialog(null, 
                "Error al generar el reporte: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            throw ex;
        }
    }

    /**
     * Genera reporte de documentos por vencer.
     *
     * @param idDocumentoPresentado El ID del documento presentado.
     */
    public void generarReporteDocumentosPorVencer(Integer idDocumentoPresentado) throws IOException {
        try {
            String endpoint = "/reportes/documentos-por-vencer/" + idDocumentoPresentado;
            
            byte[] pdfBytes = RestClient.getBytes(endpoint);
            String fileName = "documentos_vencer_" + idDocumentoPresentado + ".pdf";
            abrirPdfEnVisor(pdfBytes, fileName);
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error al generar reporte de documentos por vencer", ex);
            JOptionPane.showMessageDialog(null, 
                "Error al generar el reporte: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            throw ex;
        }
    }

    /**
     * Genera reporte de consulta de deuda de documentos.
     *
     * @param numeroGestion El número de gestión.
     */
    public void generarReporteConsultarDeudaDocumentos(Integer numeroGestion) throws IOException {
        try {
            String endpoint = "/reportes/consultar-deuda-documentos?numeroGestion=" + numeroGestion;
            
            byte[] pdfBytes = RestClient.getBytes(endpoint);
            String fileName = "deuda_documentos_" + numeroGestion + ".pdf";
            abrirPdfEnVisor(pdfBytes, fileName);
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error al generar reporte de deuda de documentos", ex);
            JOptionPane.showMessageDialog(null, 
                "Error al generar el reporte: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            throw ex;
        }
    }

    /**
     * Guarda los bytes del PDF en un archivo temporal y lo abre con el visor del sistema.
     */
    private void abrirPdfEnVisor(byte[] pdfBytes, String fileName) throws IOException {
        // Crear archivo temporal
        File tempFile = new File(TEMP_DIR, fileName);
        
        // Guardar el PDF
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(pdfBytes);
        }
        
        // Abrir con el visor de PDF predeterminado
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().open(tempFile);
        } else {
            // Fallback: mostrar mensaje con la ubicación del archivo
            JOptionPane.showMessageDialog(null, 
                "Reporte guardado en: " + tempFile.getAbsolutePath(), 
                "Reporte Generado", JOptionPane.INFORMATION_MESSAGE);
        }
        
        // Programar eliminación del archivo temporal al cerrar la aplicación
        tempFile.deleteOnExit();
    }
}
