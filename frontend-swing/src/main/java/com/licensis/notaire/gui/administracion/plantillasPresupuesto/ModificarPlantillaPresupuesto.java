/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.administracion.plantillasPresupuesto;

import com.licensis.notaire.dto.GenericDto;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.servicios.AdministradorJpa;
import com.licensis.notaire.servicios.GenericRestClient;
import com.licensis.notaire.dto.DtoTipoDeTramite;
import com.licensis.notaire.negocio.ControllerNegocio;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Tefi
 */
public class ModificarPlantillaPresupuesto extends javax.swing.JInternalFrame {

    private static JMenuItem ventanaModificarPlantillaPresupuesto = new JMenuItem(
            "Ventana Modificar Plantilla Presupuesto");
    private GenericRestClient presupuestoClient;
    private GenericRestClient tipoTramiteClient = null;
    private GenericRestClient conceptoClient = null;
    private ControllerNegocio miController = ControllerNegocio.getInstancia();
    private GenericRestClient plantillaPresupuestoClient = null;
    private List<GenericDto> tramitesDisponibles = null;
    private List<GenericDto> conceptosDisponibles = null;
    private GenericDto miDtoSeleccionado = null;
    private List<GenericDto> plantillas = null;
    private static final Logger logger = Logger.getLogger(ModificarPlantillaPresupuesto.class.getName());

    /**
     * Creates new form ModificarPlantillaPresupuesto
     */
    public ModificarPlantillaPresupuesto() {
        initComponents();
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioNormalVertical);
        tipoTramiteClient = AdministradorJpa.getInstancia().getTipoDeTramiteJpa();
        conceptoClient = AdministradorJpa.getInstancia().getConceptoJpa();
        plantillaPresupuestoClient = AdministradorJpa.getInstancia().getPlantillaPresupuestoJpa();
        inicializarFormulario();
    }

    private void salir() {
        this.dispose();
    }

    public static JMenuItem getVentanaModificarPlantillaPresupuesto() {
        return ventanaModificarPlantillaPresupuesto;
    }

    public void limpiarGrilla() {
        int i = ((DefaultTableModel) grillaConceptosDisponibles.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaConceptosDisponibles.getModel()).getRowCount() > 0) {
            ((DefaultTableModel) grillaConceptosDisponibles.getModel()).removeRow(i);
            i--;
        }
    }

    private void inicializarFormulario() {
        miDtoSeleccionado = null;
        tramitesDisponibles = null;
        plantillas = null;
        conceptosDisponibles = null;

        try {
            // Obtener todos los tipos de trámite y filtrar los habilitados
            List<GenericDto> todosLosTramites = tipoTramiteClient.findAll();
            tramitesDisponibles = new ArrayList<>();

            for (GenericDto tramite : todosLosTramites) {
                Boolean habilitado = tramite.getBoolean("habilitado");
                if (habilitado != null && habilitado) {
                    tramitesDisponibles.add(tramite);
                }
            }

            if (tramitesDisponibles.isEmpty()) {
                listaTramitesDisponibles.setEnabled(false);
                botonAceptar.setEnabled(false);
                botonSeleccionar.setEnabled(false);

                JOptionPane.showMessageDialog(this, "No existen Tipos de Tramite registrados.", "INFORMACION",
                        JOptionPane.INFORMATION_MESSAGE);
                salir();
            } else {
                listaTramitesDisponibles.setEnabled(true);
                botonAceptar.setEnabled(false);
                botonSeleccionar.setEnabled(true);

                DefaultListModel lista = new DefaultListModel();

                for (Iterator<GenericDto> it = tramitesDisponibles.iterator(); it.hasNext();) {
                    GenericDto miDto = it.next();
                    // TODO: Verificar si tiene plantilla cuando el endpoint esté disponible
                    // Por ahora, mostramos todos los trámites habilitados
                    String nombre = miDto.getString("nombre");
                    if (nombre != null) {
                        lista.addElement(nombre);
                    }
                }

                if (lista.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "<HTML>Los Tipos de Tramite registrados <BR>no tienen una plantilla asociada.</HTML>",
                            "INFORMACION", JOptionPane.INFORMATION_MESSAGE);

                    listaTramitesDisponibles.setEnabled(false);
                    botonAceptar.setEnabled(false);
                    botonSeleccionar.setEnabled(false);
                } else {
                    this.listaTramitesDisponibles.setModel(lista);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tipos de trámite: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "Error al inicializar formulario", ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the
     * Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panleModificarPlantillaPresupuesto = new javax.swing.JPanel();
        botonCancelar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        botonSeleccionar = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaConceptosDisponibles = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        listaTramitesDisponibles = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Modificar Plantilla Presupuesto");
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

        botonCancelar.setText("Cancelar");
        botonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCancelarActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Modificar Plantilla Presupuesto");

        jLabel6.setText("Trámites:");

        botonSeleccionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/aceptar.png"))); // NOI18N
        botonSeleccionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonSeleccionarActionPerformed(evt);
            }
        });

        grillaConceptosDisponibles.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {

                },
                new String[] {
                        "Nombre", "Seleccionar"
                }) {
            Class[] types = new Class[] {
                    java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean[] {
                    false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        jScrollPane1.setViewportView(grillaConceptosDisponibles);

        listaTramitesDisponibles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(listaTramitesDisponibles);

        jLabel2.setText("Conceptos:");

        botonAceptar.setText("Aceptar");
        botonAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAceptarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panleModificarPlantillaPresupuestoLayout = new javax.swing.GroupLayout(
                panleModificarPlantillaPresupuesto);
        panleModificarPlantillaPresupuesto.setLayout(panleModificarPlantillaPresupuestoLayout);
        panleModificarPlantillaPresupuestoLayout.setHorizontalGroup(
                panleModificarPlantillaPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panleModificarPlantillaPresupuestoLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panleModificarPlantillaPresupuestoLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                panleModificarPlantillaPresupuestoLayout.createSequentialGroup()
                                                        .addGap(0, 0, Short.MAX_VALUE)
                                                        .addComponent(botonAceptar,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(botonCancelar,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(panleModificarPlantillaPresupuestoLayout.createSequentialGroup()
                                                .addGroup(panleModificarPlantillaPresupuestoLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                false)
                                                        .addComponent(jScrollPane1,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 0,
                                                                Short.MAX_VALUE)
                                                        .addComponent(jSeparator1,
                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(botonSeleccionar,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 32,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel1,
                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel2,
                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel6,
                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jScrollPane3,
                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 350,
                                                                Short.MAX_VALUE))
                                                .addGap(0, 124, Short.MAX_VALUE)))
                                .addContainerGap()));
        panleModificarPlantillaPresupuestoLayout.setVerticalGroup(
                panleModificarPlantillaPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panleModificarPlantillaPresupuestoLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addGap(13, 13, 13)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 88,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(botonSeleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, 32,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 119,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91,
                                        Short.MAX_VALUE)
                                .addGroup(panleModificarPlantillaPresupuestoLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panleModificarPlantillaPresupuesto, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panleModificarPlantillaPresupuesto, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_botonAceptarActionPerformed
        if (miDtoSeleccionado != null) {
            List<GenericDto> conceptos = obtenerConceptosSeleccionados();

            if (conceptos != null && !conceptos.isEmpty()) {
                try {
                    Integer idTipoTramite = miDtoSeleccionado.getInt("idTipoTramite");

                    if (idTipoTramite != null) {
                        // Eliminar plantillas existentes y crear nuevas
                        // TODO: Implementar endpoint para obtener/eliminar plantillas por tipo de
                        // trámite
                        // Por ahora, creamos las nuevas plantillas
                        for (GenericDto concepto : conceptos) {
                            Integer idConcepto = concepto.getInt("idConcepto");
                            if (idConcepto != null) {
                                GenericDto plantilla = new GenericDto();
                                plantilla.put("fkIdTipoTramite", idTipoTramite);
                                plantilla.put("fkIdConcepto", idConcepto);

                                // Intentar crear (si ya existe, el backend debería manejarlo)
                                try {
                                    plantillaPresupuestoClient.create(plantilla);
                                } catch (IOException ex) {
                                    // Si falla, puede ser que ya exista, continuamos
                                    logger.log(Level.WARNING,
                                            "No se pudo crear plantilla (puede existir): " + ex.getMessage());
                                }
                            }
                        }

                        JOptionPane.showMessageDialog(this, "Se ha modificado la Plantilla de Presupuesto.",
                                "CONFIRMACION", JOptionPane.INFORMATION_MESSAGE);
                        limpiarGrilla();
                        inicializarFormulario();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error: No se pudo obtener el ID del tipo de trámite",
                                "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al procesar la operación: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    logger.log(Level.SEVERE, "Error al modificar plantilla de presupuesto", ex);
                    limpiarGrilla();
                    inicializarFormulario();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Debe seleccionar al menos un concepto.", "ADVERTENCIA",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Tipo de Tramite.", "ADVERTENCIA",
                    JOptionPane.WARNING_MESSAGE);
        }
    }// GEN-LAST:event_botonAceptarActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_botonCancelarActionPerformed

        salir();
    }// GEN-LAST:event_botonCancelarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)// GEN-FIRST:event_formInternalFrameClosed
    {// GEN-HEADEREND:event_formInternalFrameClosed
        Principal.removeVentanaActivas(ventanaModificarPlantillaPresupuesto);
        Principal.eliminarFormulario(this);
    }// GEN-LAST:event_formInternalFrameClosed

    private void cargarConceptosDisponibles() {

        limpiarGrilla();
        if (miDtoSeleccionado != null) {
            try {
                // Obtengo los conceptos disponibles
                conceptosDisponibles = conceptoClient.findAll();

                // TODO: Obtener plantillas de presupuesto asociadas al trámite (requiere
                // endpoint específico)
                // Por ahora, obtenemos todas las plantillas y filtramos por tipo de trámite
                Integer idTipoTramite = miDtoSeleccionado.getInt("idTipoTramite");
                if (idTipoTramite != null) {
                    List<GenericDto> todasLasPlantillas = plantillaPresupuestoClient.findAll();
                    plantillas = new ArrayList<>();

                    for (GenericDto plantilla : todasLasPlantillas) {
                        Integer fkIdTipoTramite = plantilla.getInt("fkIdTipoTramite");
                        if (fkIdTipoTramite != null && fkIdTipoTramite.equals(idTipoTramite)) {
                            plantillas.add(plantilla);
                        }
                    }
                }

                if (conceptosDisponibles != null && !conceptosDisponibles.isEmpty()) {
                    for (GenericDto miDtoConcepto : conceptosDisponibles) {
                        String nombreConcepto = miDtoConcepto.getString("nombre");

                        if (nombreConcepto != null) {
                            Boolean flag = false;

                            if (plantillas != null && !plantillas.isEmpty()) {
                                Integer idConcepto = miDtoConcepto.getInt("idConcepto");
                                for (GenericDto dtoPlantillaPresupuesto : plantillas) {
                                    Integer fkIdConcepto = dtoPlantillaPresupuesto.getInt("fkIdConcepto");
                                    if (fkIdConcepto != null && fkIdConcepto.equals(idConcepto)) {
                                        flag = true;
                                        break;
                                    }
                                }
                            }

                            Object[] datos = {
                                    nombreConcepto,
                                    flag
                            };
                            ((DefaultTableModel) grillaConceptosDisponibles.getModel()).addRow(datos);
                        }
                    }
                } else {
                    grillaConceptosDisponibles.setEnabled(false);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar plantillas: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                logger.log(Level.SEVERE, "Error al cargar conceptos", ex);
            }
        }
    }

    private List<GenericDto> obtenerConceptosSeleccionados() {
        TableModel miGrilla = grillaConceptosDisponibles.getModel();
        int filas = miGrilla.getRowCount();
        Boolean flag = true;
        List<GenericDto> conceptosSeleccionados = new ArrayList<>();

        for (int i = 0; i < filas; i++) {
            flag = (Boolean) miGrilla.getValueAt(i, 1);

            if (flag == true) {
                String nombreConcepto = (String) miGrilla.getValueAt(i, 0);

                for (GenericDto dtoConcepto : conceptosDisponibles) {
                    if (nombreConcepto.equals(dtoConcepto.getString("nombre"))) {
                        conceptosSeleccionados.add(dtoConcepto);
                        break;
                    }
                }
            }
        }

        return conceptosSeleccionados;
    }

    private void botonSeleccionarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_botonSeleccionarActionPerformed
        // TODO add your handling code here:
        miDtoSeleccionado = null;
        if (listaTramitesDisponibles.getSelectedValue() != null) {
            String seleccionado = listaTramitesDisponibles.getSelectedValue().toString();

            for (Iterator<GenericDto> it = tramitesDisponibles.iterator(); it.hasNext();) {
                GenericDto dtoTipoDeTramite = it.next();

                // Completo los datos en los componentes
                if (seleccionado.equals(dtoTipoDeTramite.getString("nombre"))) {

                    miDtoSeleccionado = dtoTipoDeTramite;

                    cargarConceptosDisponibles();

                    break;
                }
            }

            botonAceptar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Tipo de Tramite.", "ADVERTENCIA",
                    JOptionPane.WARNING_MESSAGE);
        }
    }// GEN-LAST:event_botonSeleccionarActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables

    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JButton botonSeleccionar;
    private javax.swing.JTable grillaConceptosDisponibles;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList listaTramitesDisponibles;
    private javax.swing.JPanel panleModificarPlantillaPresupuesto;
    // End of variables declaration//GEN-END:variables
}
