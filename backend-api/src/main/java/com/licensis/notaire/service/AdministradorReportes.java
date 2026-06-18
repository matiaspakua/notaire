package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.licensis.notaire.dto.DtoTipoDeTramite;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class AdministradorReportes {

    private static AdministradorReportes instancia = null;
    private Map parameters = new HashMap<>();
    private JasperPrint print = null;
    private JasperViewer jasperViewer = null;
    public static String PATH = "";
    public static String WINDOWS_PATH = "C:\\reportes\\";
    public static String LINUX_PATH = "/home/matias/reportes/";
    public String RUTA_REPORTE_PRESUPUESTO = "reportePresupuestoSinInmueble.jasper";
    public String RUTA_REPORTE_PRESUPUESTO_INMUEBLES = "reportePresupuestoInmuebles.jasper";
    public String RUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE = "reporteListaDocumetosTramite.jasper";
    public String RUTA_REPORTE_HISTORIAL_GESTION = "reporteHistorialGestion.jasper";
    public String RUTA_REPORTO_CONSULTAR_VENCIMIENTO_DOCUMENTOS = "reporteConsultarVencimientosDocumentos.jasper";
    public String RUTA_REPORTE_CONSULTAR_DEUDA_VENCIMIENTO_DOCUMENTOS = "reporteConsultarDeudaDocumentos.jasper";

    private AdministradorReportes() {
    }

    public static AdministradorReportes getInstancia() {
        if (instancia == null) {
            instancia = new AdministradorReportes();

            String os = System.getProperty("os.name");

            if (os.contains("Linux")) {
                AdministradorReportes.setPATH(LINUX_PATH);
            } else {
                AdministradorReportes.setPATH(WINDOWS_PATH);
            }
        }
        return instancia;
    }

    public static String getPATH() {
        return PATH;
    }

    public static void setPATH(String PATH) {
        AdministradorReportes.PATH = PATH;
    }

    public String getRUTA_REPORTE_PRESUPUESTO() {
        return PATH + RUTA_REPORTE_PRESUPUESTO;
    }

    public void setRUTA_REPORTE_PRESUPUESTO(String RUTA_REPORTE_PRESUPUESTO) {
        this.RUTA_REPORTE_PRESUPUESTO = RUTA_REPORTE_PRESUPUESTO;
    }

    public String getRUTA_REPORTE_PRESUPUESTO_INMUEBLES() {
        return PATH + RUTA_REPORTE_PRESUPUESTO_INMUEBLES;
    }

    public void setRUTA_REPORTE_PRESUPUESTO_INMUEBLES(String RUTA_REPORTE_PRESUPUESTO_INMUEBLES) {
        this.RUTA_REPORTE_PRESUPUESTO_INMUEBLES = RUTA_REPORTE_PRESUPUESTO_INMUEBLES;
    }

    public String getRUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE() {
        return PATH + RUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE;
    }

    public void setRUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE(String RUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE) {
        this.RUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE = RUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE;
    }

    public String getRUTA_REPORTE_HISTORIAL_GESTION() {
        return PATH + RUTA_REPORTE_HISTORIAL_GESTION;
    }

    public void setRUTA_REPORTE_HISTORIAL_GESTION(String RUTA_REPORTE_HISTORIAL_GESTION) {
        this.RUTA_REPORTE_HISTORIAL_GESTION = RUTA_REPORTE_HISTORIAL_GESTION;
    }

    public String getRUTA_REPORTO_CONSULTAR_VENCIMIENTO_DOCUMENTOS() {
        return PATH + RUTA_REPORTO_CONSULTAR_VENCIMIENTO_DOCUMENTOS;
    }

    public void setRUTA_REPORTO_CONSULTAR_VENCIMIENTO_DOCUMENTOS(String RUTA_REPORTO_CONSULTAR_VENCIMIENTO_DOCUMENTOS) {
        this.RUTA_REPORTO_CONSULTAR_VENCIMIENTO_DOCUMENTOS = RUTA_REPORTO_CONSULTAR_VENCIMIENTO_DOCUMENTOS;
    }

    public String getRUTA_REPORTE_CONSULTAR_DEUDA_VENCIMIENTO_DOCUMENTOS() {
        return PATH + RUTA_REPORTE_CONSULTAR_DEUDA_VENCIMIENTO_DOCUMENTOS;
    }

    public void setRUTA_REPORTE_CONSULTAR_DEUDA_VENCIMIENTO_DOCUMENTOS(
            String RUTA_REPORTE_CONSULTAR_DEUDA_VENCIMIENTO_DOCUMENTOS) {
        this.RUTA_REPORTE_CONSULTAR_DEUDA_VENCIMIENTO_DOCUMENTOS = RUTA_REPORTE_CONSULTAR_DEUDA_VENCIMIENTO_DOCUMENTOS;
    }

    public void generarReportePresupuesto(DtoPresupuesto presupuesto) throws IOException {
        try {

            if (presupuesto.getTramite().getInmueble() != null) {
                parameters.put("idPresupuestoParam", presupuesto.getIdPresupuesto());
                print = JasperFillManager.fillReport(this.getRUTA_REPORTE_PRESUPUESTO_INMUEBLES(), parameters,
                        Conexion.getInstancia().getConexion());
                jasperViewer = new JasperViewer(print, false);
                jasperViewer.setVisible(true);
            } else {
                parameters.put("pIdPresupuesto", presupuesto.getIdPresupuesto());
                print = JasperFillManager.fillReport(this.getRUTA_REPORTE_PRESUPUESTO(), parameters,
                        Conexion.getInstancia().getConexion());
                jasperViewer = new JasperViewer(print, false);
                jasperViewer.setVisible(true);
            }

        } catch (JRException ex) {
            Logger.getLogger(AdministradorReportes.class.getName()).log(Level.SEVERE, null, ex);
        }
        parameters.clear();
    }

    public void generarReporteListaDocumentos(List<DtoTipoDeTramite> listaTiposTramites) throws IOException {
        for (Iterator<DtoTipoDeTramite> it = listaTiposTramites.iterator(); it.hasNext();) {
            try {
                DtoTipoDeTramite dtoTipoDeTramite = it.next();
                parameters.put("nombreTipoTramite", dtoTipoDeTramite.getNombre());
                print = JasperFillManager.fillReport(this.getRUTA_REPORTE_LISTA_DOCUMENTOS_TRAMITE(), parameters,
                        Conexion.getInstancia().getConexion());
                jasperViewer = new JasperViewer(print, false);
                jasperViewer.setVisible(true);
            } catch (JRException ex) {
                Logger.getLogger(AdministradorReportes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        parameters.clear();
    }

    public void generarReporteHistorialGestion(DtoGestionDeEscritura gestion) throws IOException {
        try {
            parameters.put("idGestion", gestion.getIdGestion());
            print = JasperFillManager.fillReport(this.getRUTA_REPORTE_HISTORIAL_GESTION(), parameters,
                    Conexion.getInstancia().getConexion());
            jasperViewer = new JasperViewer(print, false);
            jasperViewer.setVisible(true);
        } catch (JRException ex) {
            Logger.getLogger(AdministradorReportes.class.getName()).log(Level.SEVERE, null, ex);
        }
        parameters.clear();

    }

    public void generarReporteDocumentosPorVencer(List<DtoDocumentoPresentado> listaDocumentosPorVencer)
            throws IOException {
        for (Iterator<DtoDocumentoPresentado> it = listaDocumentosPorVencer.iterator(); it.hasNext();) {
            try {
                DtoDocumentoPresentado dtoDocumentoPresentado = it.next();

                parameters.put("idDocumentoPresentado", dtoDocumentoPresentado.getIdDocumentoPresentado());
                print = JasperFillManager.fillReport(this.getRUTA_REPORTO_CONSULTAR_VENCIMIENTO_DOCUMENTOS(),
                        parameters, Conexion.getInstancia().getConexion());
            } catch (JRException ex) {
                Logger.getLogger(AdministradorReportes.class.getName()).log(Level.SEVERE, null, ex);
            }
            jasperViewer = new JasperViewer(print, false);
            jasperViewer.setVisible(true);
        }
    }

    public void generarReporteConsultarDeudaDocumentos(Integer pNumeroGestion) throws IOException {
        try {
            parameters.put("numeroGestion", pNumeroGestion);
            print = JasperFillManager.fillReport(this.getRUTA_REPORTE_CONSULTAR_DEUDA_VENCIMIENTO_DOCUMENTOS(),
                    parameters, Conexion.getInstancia().getConexion());
            jasperViewer = new JasperViewer(print, false);
            jasperViewer.setVisible(true);
        } catch (JRException ex) {
            Logger.getLogger(AdministradorReportes.class.getName()).log(Level.SEVERE, null, ex);
        }

        parameters.clear();
    }

    public JasperViewer getJasperViewer() {
        return jasperViewer;
    }

    public void setJasperViewer(JasperViewer jasperViewer) {
        this.jasperViewer = jasperViewer;
    }

    public JasperPrint getPrint() {
        return print;
    }

    public void setPrint(JasperPrint print) {
        this.print = print;
    }
}
