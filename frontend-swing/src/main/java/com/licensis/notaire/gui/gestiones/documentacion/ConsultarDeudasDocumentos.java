/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.gestiones.documentacion;

import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.dto.DtoFlag;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoTipoDeDocumento;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.clientes.BuscarGestionesCliente;
import com.licensis.notaire.gui.gestiones.gestion.BuscarGestion;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorReportes;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author matias
 */
public class ConsultarDeudasDocumentos extends javax.swing.JInternalFrame {

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaConsultarDeudasDocumento = new JMenuItem("Ventana Consultar Deudas Documentos");
    private static ConsultarDeudasDocumentos instancia = null;
    private DtoGestionDeEscritura dtoGestionDeEscritura = null;
    private ArrayList<DtoDocumentoPresentado> listaDocumentosPresentados = null;
    private ControllerNegocio miController;
    private String error = null;

    /**
     * Creates new form ConsultarDeudasDocumentos
     */
    private ConsultarDeudasDocumentos() {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(800, 600);
        miController = ControllerNegocio.getInstancia();
    }

    public DtoGestionDeEscritura getDtoGestion() {
        return dtoGestionDeEscritura;
    }

    public void setDtoGestion(DtoGestionDeEscritura dtoGestionDeEscritura) {
        this.dtoGestionDeEscritura = dtoGestionDeEscritura;
    }

    public ArrayList<DtoDocumentoPresentado> getListaDocumentosPresentados() {
        return listaDocumentosPresentados;
    }

    public void setListaDocumentosPresentados(ArrayList<DtoDocumentoPresentado> listaDocumentosPresentados) {
        this.listaDocumentosPresentados = listaDocumentosPresentados;
    }

    public static ConsultarDeudasDocumentos getInstancia() {
        if (instancia == null) {
            instancia = new ConsultarDeudasDocumentos();
        }
        return instancia;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    private void salir() {
        this.dispose();
    }

    public static JMenuItem getVentanaConsultarDeudasDocumento() {
        return ventanaConsultarDeudasDocumento;
    }

    public boolean cargarFormulario(DtoGestionDeEscritura dtoGestionDeEscritura) throws NonexistentJpaException {
        boolean flag = false;

        this.setDtoGestion(dtoGestionDeEscritura);

        this.limpiarForm();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        fechaInicio.setText(formatter.format(this.getDtoGestion().getFechaInicio()));
        labelEncabezado.setText(this.getDtoGestion().getEncabezado());
        escribanoAcargo.setText(this.getDtoGestion().getPersonaEscribano().getNombre());
        nroGestion.setText(Integer.toString(this.getDtoGestion().getNumero()));

        this.cargarTramites(this.getDtoGestion());

        return flag;
    }

    public void limpiarForm() {
        fechaInicio.setText("");
        labelEncabezado.setText("");
        escribanoAcargo.setText("");
        nroGestion.setText("");
        botonImprimir.setEnabled(false);
        this.limpiarJtable();

    }

    public void limpiarJtable() {
        int i = ((DefaultTableModel) grillaDetalleDocumentos.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaDetalleDocumentos.getModel()).getRowCount() > 0) {
            ((DefaultTableModel) grillaDetalleDocumentos.getModel()).removeRow(i);
            i--;
        }

    }

    public void cargarTramites(DtoGestionDeEscritura dtoGestion) throws NonexistentJpaException {
        Boolean flag = false;
        ArrayList<DtoDocumentoPresentado> listaDocumentosPresentados = null;
        ArrayList<DtoTramite> listaTramites = null;
        String estado = null;
        Integer cont = 0;

        this.limpiarJtable();

        estado = dtoGestion.getEstado().getNombre();

        dtoGestion = miController.obtenerDocNecesarioEntregadosNoEntregadosDeGestion(dtoGestion);

        listaTramites = (ArrayList<DtoTramite>) dtoGestion.getListaTramitesAsociados();

        for (int i = 0; i < listaTramites.size(); i++) {
            DtoTramite dtoTramite = listaTramites.get(i);
            String nombreTramite = listaTramites.get(i).getTipoDeTramite().getNombre();
            listaDocumentosPresentados = (ArrayList<DtoDocumentoPresentado>) listaTramites.get(i)
                    .getListaDocumentosGestion();

            if (!listaDocumentosPresentados.isEmpty()) {
                flag = cargarGrilla(listaDocumentosPresentados, nombreTramite, dtoTramite);
                this.setListaDocumentosPresentados(listaDocumentosPresentados);
            }
        }
        if (flag) {
            ConsultarDeudasDocumentos.getInstancia().botonImprimir.setEnabled(true);
            ConsultarDeudasDocumentos.getInstancia().toFront();
            Principal.cargarFormulario(ConsultarDeudasDocumentos.getInstancia());
        } else {
            this.dispose();
            JOptionPane.showMessageDialog(this, "No hay documentos presentados, para la gestion");
        }
    }

    public boolean cargarGrilla(ArrayList<DtoDocumentoPresentado> listaDocumentosPresentados, String tipoTramite,
            DtoTramite dtoTramite) {
        boolean flag = false;
        DtoDocumentoPresentado dtoDocumentoPresentado = null;
        boolean presentaDeuda = false;
        String fechaPago = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        for (int i = 0; i < listaDocumentosPresentados.size(); i++) {
            dtoDocumentoPresentado = listaDocumentosPresentados.get(i);

            if (dtoDocumentoPresentado.getQuienEntrega().equals(ConstantesGui.CLIENTE)
                    && dtoDocumentoPresentado.isEntregado()) {
                flag = true;
                if (dtoDocumentoPresentado.getFechaPago() != null) {
                    fechaPago = formatter.format(dtoDocumentoPresentado.getFechaPago());
                } else {
                    fechaPago = null;
                }

                Object[] datos = {
                        tipoTramite,
                        dtoDocumentoPresentado.getNombre(),
                        dtoDocumentoPresentado.getImporteAPagar(),
                        fechaPago,
                        dtoDocumentoPresentado,
                        dtoTramite,
                };

                ((DefaultTableModel) grillaDetalleDocumentos.getModel()).addRow(datos);
            }

        }
        Principal.cargarFormulario(BuscarGestionesCliente.getInstancia());
        this.toFront();

        return flag;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelConsultarDeudasDocumentos = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        botonImprimir = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        botonBuscarGestion = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaDetalleDocumentos = new javax.swing.JTable();
        nroGestion = new javax.swing.JLabel();
        labelEncabezado = new javax.swing.JLabel();
        fechaInicio = new javax.swing.JLabel();
        escribanoAcargo = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Consultar Deudas Documentos");
        setPreferredSize(new java.awt.Dimension(900, 600));
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }

            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }

            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
            }

            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }

            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }

            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }

            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        panelConsultarDeudasDocumentos.setPreferredSize(new java.awt.Dimension(900, 600));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Registrar deudas documentos de Cliente\u0000");

        botonImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/impresora.png"))); // NOI18N
        botonImprimir.setText("Imprimir");
        botonImprimir.setEnabled(false);
        botonImprimir.setIconTextGap(10);
        botonImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonImprimirActionPerformed(evt);
            }
        });

        botonCancelar.setText("Cancelar");
        botonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCancelarActionPerformed(evt);
            }
        });

        jLabel2.setText("Buscar Gestion");

        botonBuscarGestion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/buscar.png"))); // NOI18N
        botonBuscarGestion.setText("Buscar");
        botonBuscarGestion.setIconTextGap(10);
        botonBuscarGestion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarGestionActionPerformed(evt);
            }
        });

        jLabel4.setText("Fecha de Inicio:");

        jLabel3.setText("Numero de Gestion:");

        jLabel6.setText("Encabezado:");

        jLabel8.setText("Escribano a Cargo:");

        jLabel9.setText("Detalle de Documentos:");

        grillaDetalleDocumentos.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {

                },
                new String[] {
                        "Tramite", "Tipo de Documento", "Monto Deuda", "Fecha Pago (*)", "DtoDocumentoPresentado",
                        "DtoTipoTramite"
                }) {
            Class[] types = new Class[] {
                    java.lang.String.class, java.lang.String.class, java.lang.Float.class, java.lang.Object.class,
                    java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean[] {
                    false, false, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        grillaDetalleDocumentos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grillaDetalleDocumentosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(grillaDetalleDocumentos);
        grillaDetalleDocumentos.getColumnModel().getColumn(1).setPreferredWidth(150);
        grillaDetalleDocumentos.getColumnModel().getColumn(4).setMaxWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(4).setMinWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(4).setMaxWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(4).setMinWidth(0);

        grillaDetalleDocumentos.getColumnModel().getColumn(5).setMaxWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(5).setMinWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(5).setMaxWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(5).setMinWidth(0);

        nroGestion.setText("jLabel5");

        labelEncabezado.setText("jLabel5");

        fechaInicio.setText("jLabel5");

        escribanoAcargo.setText("jLabel5");

        botonAceptar.setText("Aceptar");
        botonAceptar.setEnabled(false);
        botonAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAceptarActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 0, 51));
        jLabel5.setText("(*) Formato Fecha: dd/MM/aaaa");

        javax.swing.GroupLayout panelConsultarDeudasDocumentosLayout = new javax.swing.GroupLayout(
                panelConsultarDeudasDocumentos);
        panelConsultarDeudasDocumentos.setLayout(panelConsultarDeudasDocumentosLayout);
        panelConsultarDeudasDocumentosLayout.setHorizontalGroup(
                panelConsultarDeudasDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelConsultarDeudasDocumentosLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelConsultarDeudasDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                panelConsultarDeudasDocumentosLayout.createSequentialGroup()
                                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                309, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(botonImprimir,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(botonAceptar,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(botonCancelar,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 876,
                                                Short.MAX_VALUE)
                                        .addGroup(panelConsultarDeudasDocumentosLayout.createSequentialGroup()
                                                .addGroup(panelConsultarDeudasDocumentosLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(panelConsultarDeudasDocumentosLayout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                                        false)
                                                                .addGroup(panelConsultarDeudasDocumentosLayout
                                                                        .createSequentialGroup()
                                                                        .addGroup(panelConsultarDeudasDocumentosLayout
                                                                                .createParallelGroup(
                                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(jLabel6)
                                                                                .addComponent(jLabel4)
                                                                                .addComponent(jLabel9)
                                                                                .addComponent(jLabel1)
                                                                                .addComponent(jLabel2))
                                                                        .addGap(67, 67, Short.MAX_VALUE))
                                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                        panelConsultarDeudasDocumentosLayout
                                                                                .createSequentialGroup()
                                                                                .addComponent(jLabel8)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(escribanoAcargo,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        Short.MAX_VALUE)))
                                                        .addGroup(panelConsultarDeudasDocumentosLayout
                                                                .createSequentialGroup()
                                                                .addComponent(jLabel3)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addGroup(panelConsultarDeudasDocumentosLayout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(botonBuscarGestion,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                120,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGroup(panelConsultarDeudasDocumentosLayout
                                                                                .createParallelGroup(
                                                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                                                        false)
                                                                                .addComponent(nroGestion,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        548, Short.MAX_VALUE)
                                                                                .addComponent(labelEncabezado,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        Short.MAX_VALUE)
                                                                                .addComponent(fechaInicio,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        Short.MAX_VALUE)))))
                                                .addGap(0, 0, Short.MAX_VALUE)))));
        panelConsultarDeudasDocumentosLayout.setVerticalGroup(
                panelConsultarDeudasDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelConsultarDeudasDocumentosLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addGroup(panelConsultarDeudasDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(botonBuscarGestion, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(panelConsultarDeudasDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(nroGestion))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelConsultarDeudasDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(labelEncabezado))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelConsultarDeudasDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(fechaInicio))
                                .addGap(5, 5, 5)
                                .addGroup(panelConsultarDeudasDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel8)
                                        .addComponent(escribanoAcargo))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelConsultarDeudasDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelConsultarDeudasDocumentosLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(botonImprimir, javax.swing.GroupLayout.Alignment.TRAILING,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(panelConsultarDeudasDocumentosLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(botonCancelar,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(botonAceptar,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(jLabel5))
                                .addGap(6, 6, 6)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(panelConsultarDeudasDocumentos, javax.swing.GroupLayout.DEFAULT_SIZE, 882,
                                        Short.MAX_VALUE)
                                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelConsultarDeudasDocumentos, javax.swing.GroupLayout.DEFAULT_SIZE, 569,
                                Short.MAX_VALUE));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonImprimirActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_botonImprimirActionPerformed
    {// GEN-HEADEREND:event_botonImprimirActionPerformed

        if (!this.listaDocumentosPresentados.isEmpty()) {
            try {
                AdministradorReportes reportes = AdministradorReportes.getInstancia();
                reportes.generarReporteConsultarDeudaDocumentos(this.getDtoGestion().getNumero());
            } catch (IOException ex) {
                Logger.getLogger(ConsultarDeudasDocumentos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }// GEN-LAST:event_botonImprimirActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_botonCancelarActionPerformed
    {// GEN-HEADEREND:event_botonCancelarActionPerformed
        this.limpiarForm();
        this.salir();
    }// GEN-LAST:event_botonCancelarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)// GEN-FIRST:event_formInternalFrameClosed
    {// GEN-HEADEREND:event_formInternalFrameClosed
        this.limpiarForm();
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaConsultarDeudasDocumento);
        Principal.eliminarFormulario(this);
    }// GEN-LAST:event_formInternalFrameClosed

    private void botonBuscarGestionActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_botonBuscarGestionActionPerformed
    {// GEN-HEADEREND:event_botonBuscarGestionActionPerformed
        BuscarGestion formBuscarGestion = new BuscarGestion();
        formBuscarGestion.setTipoBusqueda(ConstantesGui.DOCUMENTACION_DEUDA);
        Principal.cargarFormulario(formBuscarGestion);
        Principal.setVentanasActivas(BuscarGestion.getVentanaBuscarGestion());
    }// GEN-LAST:event_botonBuscarGestionActionPerformed

    private void grillaDetalleDocumentosMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_grillaDetalleDocumentosMouseClicked
        this.botonAceptar.setEnabled(true);
    }// GEN-LAST:event_grillaDetalleDocumentosMouseClicked

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_botonAceptarActionPerformed

        int i = JOptionPane.showConfirmDialog(this, "¿Desea confirmar los documentos seleccionados?",
                "Confirmar Salida", JOptionPane.YES_NO_OPTION);

        if (i == 0) {
            DtoFlag flag = null;
            try {
                flag = this.modificarDocumentacionPresentada();
            } catch (NonexistentEntityException ex) {
                Logger.getLogger(ConsultarDeudasDocumentos.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassModifiedException ex) {
                Logger.getLogger(ConsultarDeudasDocumentos.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(ConsultarDeudasDocumentos.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (flag.getFlag()) {
                JOptionPane.showMessageDialog(this, "Los documentos fueron actualizados correctamente.", "INFORMACION",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            this.setDtoGestion(miController.buscarDtoGestion(this.getDtoGestion()));
            try {
                this.cargarFormulario(this.getDtoGestion());
            } catch (NonexistentJpaException ex) {
                Logger.getLogger(ConsultarDeudasDocumentos.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Los documentos no se modificaron", "Advertencia",
                    JOptionPane.INFORMATION_MESSAGE);

        }
    }// GEN-LAST:event_botonAceptarActionPerformed

    public DtoFlag modificarDocumentacionPresentada()
            throws NonexistentEntityException, ClassModifiedException, ParseException {

        DtoFlag dtoFlag = new DtoFlag();
        TableModel miGrilla = grillaDetalleDocumentos.getModel();
        int filaSeleccionada = grillaDetalleDocumentos.getSelectedRow();
        boolean flag = false;
        int filas = miGrilla.getRowCount();
        int columnas = miGrilla.getColumnCount();
        int cont = 0;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        DtoTramite dtoTramite = null;
        DtoTipoDeDocumento dtoTipoDeDocumento = null;
        dtoFlag.setFlag(false);

        ArrayList<DtoDocumentoPresentado> lisDtoDocumentosPresentados = new ArrayList<>();
        DtoDocumentoPresentado dtoDocumentoPresentado = null;
        Integer fila = null;

        try {

            for (int i = 0; i < filas; i++) {
                fila = i + 1;

                dtoDocumentoPresentado = (DtoDocumentoPresentado) miGrilla.getValueAt(i, 4);
                dtoTramite = (DtoTramite) miGrilla.getValueAt(i, 5);
                dtoDocumentoPresentado.setImporteApagar((Float) miGrilla.getValueAt(i, 2));
                dtoDocumentoPresentado.setFkTramite(dtoTramite);

                // Fecha Pago
                String fecha = (String) miGrilla.getValueAt(i, 3);
                this.setError(ConstantesGui.CAMPO_FECHA_PAGO);

                if (miGrilla.getValueAt(i, 3) != null && !fecha.isEmpty()) {
                    Date fechaPago = dateFormat.parse((String) miGrilla.getValueAt(i, 3));
                    dtoDocumentoPresentado.setFechaPago(fechaPago);
                } else {
                    dtoDocumentoPresentado.setFechaPago(null);
                }
                lisDtoDocumentosPresentados.add(dtoDocumentoPresentado);

                cont++;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Algun campo es incorrecto: " + "Fila: " + fila + " -" + this.getError(), "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return dtoFlag;
        }
        try {
            if (cont > 0) {
                dtoFlag = miController.modificarDocumentacion(lisDtoDocumentosPresentados, this.getDtoGestion());
            } else {
                JOptionPane.showMessageDialog(this, "No realizo modificaciones o algun dato es incorrecto",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            String mensaje = "Error al modificar documentacion: " + e.getMessage();
            JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        }

        return dtoFlag;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonBuscarGestion;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JButton botonImprimir;
    private javax.swing.JLabel escribanoAcargo;
    private javax.swing.JLabel fechaInicio;
    private javax.swing.JTable grillaDetalleDocumentos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelEncabezado;
    private javax.swing.JLabel nroGestion;
    private javax.swing.JPanel panelConsultarDeudasDocumentos;
    // End of variables declaration//GEN-END:variables
}
