/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.gestiones.documentacion;

import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.gui.Principal;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorReportes;

/**
 *
 * @author matias
 */
public class ConsultarVencimientosDocumentos extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaConsultarVencimientosDocumento = new JMenuItem("Ventana Consultar Vencimientos Documentos");
    private ControllerNegocio miController;
    private List<DtoDocumentoPresentado> listaDocumentos;

    /**
     * Creates new form ConsultarVencimientosDocumentos
     */
    public ConsultarVencimientosDocumentos() {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(1000,600);
        miController = ControllerNegocio.getInstancia();
        grillaDocumentosPorVencer.setAutoCreateRowSorter(true);

    }

    private void salir() {
        this.dispose();
    }

    public static JMenuItem getVentanaConsultarVencimientosDocumento() {
        return ventanaConsultarVencimientosDocumento;
    }

    public void cargarListaDocumentosConVencimiento() {
        this.listaDocumentos = miController.consultarDocumentosProximosVencer();

        if (!listaDocumentos.isEmpty())
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            for (Iterator<DtoDocumentoPresentado> it = listaDocumentos.iterator(); it.hasNext();)
            {
                DtoDocumentoPresentado dtoDocumentoPresentado = it.next();

                String numeroCarton = "";
                if(dtoDocumentoPresentado.getNumeroCarton() != null)
                {
                    numeroCarton = dtoDocumentoPresentado.getNumeroCarton().toString();
                }
                
                String fechaSalida = "";
                
                if(dtoDocumentoPresentado.getFechaSalida() != null)
                {
                    fechaSalida = formatter.format(dtoDocumentoPresentado.getFechaSalida());
                }
                String fechaVencimiento = "";                
                
                if(dtoDocumentoPresentado.getFechaVencimiento() != null)
                {
                    fechaVencimiento = formatter.format(dtoDocumentoPresentado.getFechaVencimiento());
                }
                    
                String importePagar = "";
                
                if(dtoDocumentoPresentado.getImporteAPagar() != null)
                {
                    importePagar = dtoDocumentoPresentado.getImporteAPagar().toString();
                }
                                    
                String fechaPago = "";
                
                if(dtoDocumentoPresentado.getFechaPago() != null)
                {
                    fechaPago = formatter.format(dtoDocumentoPresentado.getFechaPago());
                }
                String fechaLiberado = "";
                if(dtoDocumentoPresentado.getFechaLiberado() != null)
                {
                    fechaLiberado = formatter.format(dtoDocumentoPresentado.getFechaLiberado());
                }
                
                String observaciones = "";                
                
                if(dtoDocumentoPresentado.getObservaciones() != null)
                {
                    observaciones = dtoDocumentoPresentado.getObservaciones();
                }
                
                Object[] datos =
                {
                    dtoDocumentoPresentado.getNombre(),
                    dtoDocumentoPresentado.getFkTramite().getGestion().getNumero(),
                    dtoDocumentoPresentado.getFkTramite().getGestion().getEncabezado(),
                    dtoDocumentoPresentado.getFkTramite().getGestion().getClienteReferencia().getNombre() + ", " + dtoDocumentoPresentado.getFkTramite().getGestion().getClienteReferencia().getApellido(),
                    dtoDocumentoPresentado.isPreparado(),
                    formatter.format(dtoDocumentoPresentado.getFechaIngreso()),
                    fechaSalida,
                    numeroCarton,
                    dtoDocumentoPresentado.isObservado(),
                    importePagar,
                    fechaPago,
                    fechaLiberado,
                    fechaVencimiento,
                    observaciones
                };

                ((DefaultTableModel) this.grillaDocumentosPorVencer.getModel()).addRow(datos);

            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No existen documentos proximos a vencer.", "Informacion", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        panelConsultarVencimientosDocumentos = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        botonCerrar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaDocumentosPorVencer = new javax.swing.JTable();
        botonImprimir = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Consultar Vencimientos Documentos");
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

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Consultar Vencimientos de Documentos");

        botonCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar.png"))); // NOI18N
        botonCerrar.setText("Cerrar");
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });

        grillaDocumentosPorVencer.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tipo de Documento", "Gestión", "Encabezado", "Cliente", "Preparado?", "Fecha Ingreso", "Fecha Salida", "Número Cartón", "Observado", "Monto Deuda", "Fecha Pago", "Fecha Liberación", "Fecha Vencimiento", "Observaciones"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(grillaDocumentosPorVencer);

        botonImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/impresora.png"))); // NOI18N
        botonImprimir.setText("Imprimir");
        botonImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonImprimirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelConsultarVencimientosDocumentosLayout = new javax.swing.GroupLayout(panelConsultarVencimientosDocumentos);
        panelConsultarVencimientosDocumentos.setLayout(panelConsultarVencimientosDocumentosLayout);
        panelConsultarVencimientosDocumentosLayout.setHorizontalGroup(
            panelConsultarVencimientosDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConsultarVencimientosDocumentosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelConsultarVencimientosDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelConsultarVencimientosDocumentosLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelConsultarVencimientosDocumentosLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 929, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelConsultarVencimientosDocumentosLayout.setVerticalGroup(
            panelConsultarVencimientosDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConsultarVencimientosDocumentosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelConsultarVencimientosDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jScrollPane2.setViewportView(panelConsultarVencimientosDocumentos);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCerrarActionPerformed
    {//GEN-HEADEREND:event_botonCerrarActionPerformed
        salir();
    }//GEN-LAST:event_botonCerrarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaConsultarVencimientosDocumento);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonImprimirActionPerformed

        if (!this.listaDocumentos.isEmpty())
        {
            AdministradorReportes reportes = AdministradorReportes.getInstancia();
            reportes.generarReporteDocumentosPorVencer(this.listaDocumentos);
        }
    }//GEN-LAST:event_botonImprimirActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCerrar;
    private javax.swing.JButton botonImprimir;
    private javax.swing.JTable grillaDocumentosPorVencer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel panelConsultarVencimientosDocumentos;
    // End of variables declaration//GEN-END:variables
}
