/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.administracion.tramites;

import com.licensis.notaire.dto.GenericDto;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.servicios.AdministradorJpa;
import com.licensis.notaire.servicios.GenericRestClient;
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
public class ModificarTipoTramite extends javax.swing.JInternalFrame
{

    private static JMenuItem ventanaModificarTipoTramite = new JMenuItem("Ventana Modificar Tipo de Tramites");
    private GenericRestClient tipoTramiteClient = null;
    private GenericRestClient tipoDocumentoClient = null;
    private List<GenericDto> tramitesDisponibles = new ArrayList<>();
    private List<GenericDto> plantillas = null;
    private List<GenericDto> documentosDisponibles = null;
    private GenericDto miDtoSeleccionado = null;
    private static final Logger logger = Logger.getLogger(ModificarTipoTramite.class.getName());

    /**
     * Creates new form ModificarTipoTramite
     */
    public ModificarTipoTramite()
    {
        initComponents();
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioGrandeVertical);
        tipoTramiteClient = AdministradorJpa.getInstancia().getTipoDeTramiteJpa();
        tipoDocumentoClient = AdministradorJpa.getInstancia().getTipoDeDocumentoJpa();
        inicializarFormulario();
    }

    private void salir()
    {
        this.dispose();
    }

    public static JMenuItem getVentanaModificarTipoTramite()
    {
        return ventanaModificarTipoTramite;
    }

    private void inicializarFormulario()
    {
        try
        {
            // Obtener todos los tipos de trámite y filtrar los habilitados
            List<GenericDto> todosLosTramites = tipoTramiteClient.findAll();
            tramitesDisponibles = new ArrayList<>();
            
            for (GenericDto tramite : todosLosTramites)
            {
                Boolean habilitado = tramite.getBoolean("habilitado");
                if (habilitado != null && habilitado)
                {
                    tramitesDisponibles.add(tramite);
                }
            }

            if (tramitesDisponibles.isEmpty())
            {
                listaTramitesDisponibles.setEnabled(false);
                botonSeleccionar.setEnabled(false);
                botonAceptar.setEnabled(false);
                campoObservaciones.setEnabled(false);
                checkBoxInmueble.setEnabled(false);
                checkBoxSeArchiva.setEnabled(false);
                checkRequiereInscripcion.setEnabled(false);

                JOptionPane.showMessageDialog(this, "No existen tipos de tramite registrados.", "INFORMACION", JOptionPane.INFORMATION_MESSAGE);
                salir();
            } else
            {
                listaTramitesDisponibles.setEnabled(true);
                botonSeleccionar.setEnabled(true);
                botonAceptar.setEnabled(true);
                campoObservaciones.setEnabled(true);
                checkBoxInmueble.setEnabled(true);
                checkBoxSeArchiva.setEnabled(true);
                checkRequiereInscripcion.setEnabled(true);

                DefaultListModel lista = new DefaultListModel();

                for (Iterator<GenericDto> it = tramitesDisponibles.iterator(); it.hasNext();)
                {
                    GenericDto miDto = it.next();
                    String nombre = miDto.getString("nombre");
                    if (nombre != null)
                    {
                        lista.addElement(nombre);
                    }
                }

                this.listaTramitesDisponibles.setModel(lista);
            }
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Error al cargar tipos de trámite: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "Error al inicializar formulario", ex);
        }
    }

    private void limpiarFormulario()
    {
        //listaTramitesDisponibles.removeAll();
        campoNombreTramite.setText("");
        campoObservaciones.setText("");
        checkBoxInmueble.setSelected(false);
        checkBoxSeArchiva.setSelected(false);
        checkRequiereInscripcion.setSelected(false);

        int i = ((DefaultTableModel) grillaDocumentosNecesarios.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaDocumentosNecesarios.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaDocumentosNecesarios.getModel()).removeRow(i);
            i--;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane4 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        panelModificarTipoTramite = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listaTramitesDisponibles = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        botonSeleccionar = new javax.swing.JButton();
        botonAceptar = new javax.swing.JButton();
        botonCerrar = new javax.swing.JButton();
        campoNombreTramite = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        checkBoxSeArchiva = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        checkRequiereInscripcion = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        grillaDocumentosNecesarios = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        checkBoxInmueble = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        campoObservaciones = new javax.swing.JTextArea();

        jScrollPane4.setViewportView(jEditorPane1);

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Ventana Modificar Tipo de Tramites");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        panelModificarTipoTramite.setAutoscrolls(true);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Modificar Tipo de Tramite");

        jScrollPane3.setViewportView(listaTramitesDisponibles);

        jLabel6.setText("Trámites disponibles");

        botonSeleccionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/aceptar.png"))); // NOI18N
        botonSeleccionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonSeleccionarActionPerformed(evt);
            }
        });

        botonAceptar.setText("Aceptar");
        botonAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAceptarActionPerformed(evt);
            }
        });

        botonCerrar.setText("Cancelar");
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });

        campoNombreTramite.setEditable(false);

        jLabel4.setText("Observaciones");

        jLabel2.setText("Nombre Trámite");

        jLabel7.setText("Se archiva");

        checkRequiereInscripcion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkRequiereInscripcionActionPerformed(evt);
            }
        });

        jLabel3.setText("Requiere Inscripción");

        grillaDocumentosNecesarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nombre Documento", "Seleccionar"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(grillaDocumentosNecesarios);

        jLabel5.setText("Documentos Necesarios:");

        jLabel8.setText("Se asocia con Inmueble");

        campoObservaciones.setColumns(20);
        campoObservaciones.setRows(5);
        jScrollPane1.setViewportView(campoObservaciones);

        javax.swing.GroupLayout panelModificarTipoTramiteLayout = new javax.swing.GroupLayout(panelModificarTipoTramite);
        panelModificarTipoTramite.setLayout(panelModificarTipoTramiteLayout);
        panelModificarTipoTramiteLayout.setHorizontalGroup(
            panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModificarTipoTramiteLayout.createSequentialGroup()
                .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelModificarTipoTramiteLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelModificarTipoTramiteLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE))
                            .addComponent(jSeparator2)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelModificarTipoTramiteLayout.createSequentialGroup()
                                .addGap(0, 637, Short.MAX_VALUE)
                                .addComponent(botonSeleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelModificarTipoTramiteLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(352, 352, 352))
                            .addGroup(panelModificarTipoTramiteLayout.createSequentialGroup()
                                .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(campoNombreTramite)
                                    .addGroup(panelModificarTipoTramiteLayout.createSequentialGroup()
                                        .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(checkRequiereInscripcion)
                                            .addComponent(checkBoxSeArchiva)
                                            .addComponent(checkBoxInmueble))
                                        .addGap(0, 0, Short.MAX_VALUE))))))
                    .addGroup(panelModificarTipoTramiteLayout.createSequentialGroup()
                        .addGap(167, 167, 167)
                        .addComponent(jScrollPane1))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addGroup(panelModificarTipoTramiteLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelModificarTipoTramiteLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelModificarTipoTramiteLayout.setVerticalGroup(
            panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModificarTipoTramiteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonSeleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(campoNombreTramite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkRequiereInscripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxSeArchiva)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(checkBoxInmueble))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panelModificarTipoTramiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane5.setViewportView(panelModificarTipoTramite);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAceptarActionPerformed

        if (miDtoSeleccionado != null)
        {
            miDtoSeleccionado.put("seInscribe", checkRequiereInscripcion.isSelected());
            miDtoSeleccionado.put("seArchiva", checkBoxSeArchiva.isSelected());
            miDtoSeleccionado.put("asociaInmuebles", checkBoxInmueble.isSelected());
            miDtoSeleccionado.put("observaciones", campoObservaciones.getText());

            List<GenericDto> documentos = obtenerDocumentosSeleccionados();

            if (documentos != null && !documentos.isEmpty())
            {
                try
                {
                    tipoTramiteClient.edit(miDtoSeleccionado);
                    // TODO: Actualizar relaciones con documentos si es necesario
                    // Esto puede requerir un endpoint específico en el backend
                    
                    JOptionPane.showMessageDialog(this, "Se ha modificado el Tipo de Tramite " + miDtoSeleccionado.getString("nombre") + ".", "CONFIRMACION", JOptionPane.INFORMATION_MESSAGE);
                    salir();
                }
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(this, "Error al comunicarse con el servidor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    logger.log(Level.SEVERE, "Error al modificar tipo de trámite", ex);
                    limpiarFormulario();
                    inicializarFormulario();
                }
            } else
            {
                JOptionPane.showMessageDialog(this, "Debe seleccionar al menos un Documento.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            }
        } else
        {

            JOptionPane.showMessageDialog(this, "Debe seleccionar un Tipo de Tramite.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_botonAceptarActionPerformed

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCerrarActionPerformed
        salir();
    }//GEN-LAST:event_botonCerrarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        Principal.removeVentanaActivas(ventanaModificarTipoTramite);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void checkRequiereInscripcionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkRequiereInscripcionActionPerformed
   }//GEN-LAST:event_checkRequiereInscripcionActionPerformed
    private List<GenericDto> obtenerDocumentosSeleccionados()
    {
        TableModel miGrilla = grillaDocumentosNecesarios.getModel();
        int filas = miGrilla.getRowCount();
        Boolean flag = true;
        List<GenericDto> documentosSeleccionados = new ArrayList<>();

        for (int i = 0; i < filas; i++)
        {
            flag = (Boolean) miGrilla.getValueAt(i, 1);

            if (flag == true)
            {
                String nombreDocumento = (String) miGrilla.getValueAt(i, 0);
                
                for (GenericDto dtoTipoDeDocumento : documentosDisponibles)
                {
                    if (nombreDocumento.equals(dtoTipoDeDocumento.getString("nombre")))
                    {
                        documentosSeleccionados.add(dtoTipoDeDocumento);
                        break;
                    }
                }
            }
        }

        return documentosSeleccionados;
    }

    private void botonSeleccionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonSeleccionarActionPerformed
        // TODO add your handling code here:
        limpiarFormulario();

        if (listaTramitesDisponibles.getSelectedValue() != null)
        {
            String seleccionado = listaTramitesDisponibles.getSelectedValue().toString();

            //listaTramitesDisponibles.setEnabled(false);
            // botonSeleccionar.setEnabled(false);
            try
            {
                for (Iterator<GenericDto> it = tramitesDisponibles.iterator(); it.hasNext();)
                {
                    GenericDto dtoTipoDeTramite = it.next();

                    // Completo los datos en los componentes
                    if (seleccionado.equals(dtoTipoDeTramite.getString("nombre")))
                    {
                        miDtoSeleccionado = dtoTipoDeTramite;

                        campoNombreTramite.setText(dtoTipoDeTramite.getString("nombre"));
                        checkBoxSeArchiva.setSelected(dtoTipoDeTramite.getBoolean("seArchiva") != null && dtoTipoDeTramite.getBoolean("seArchiva"));
                        checkRequiereInscripcion.setSelected(dtoTipoDeTramite.getBoolean("seInscribe") != null && dtoTipoDeTramite.getBoolean("seInscribe"));
                        checkBoxInmueble.setSelected(dtoTipoDeTramite.getBoolean("asociaInmuebles") != null && dtoTipoDeTramite.getBoolean("asociaInmuebles"));
                        campoObservaciones.setText(dtoTipoDeTramite.getString("observaciones"));

                        // Obtengo los documentos disponibles
                        documentosDisponibles = tipoDocumentoClient.findAll();
                        // TODO: Obtener plantillas de trámite asociadas (requiere endpoint específico)

                        if (documentosDisponibles != null && !documentosDisponibles.isEmpty())
                        {
                            for (GenericDto miDtoTipoDeDocumento : documentosDisponibles)
                            {
                                String nombreDocumento = miDtoTipoDeDocumento.getString("nombre");

                                if (nombreDocumento != null)
                                {
                                    // Por ahora, todos los documentos aparecen sin seleccionar
                                    // TODO: Verificar si están en plantillas cuando el endpoint esté disponible
                                    Object[] datos =
                                    {
                                        nombreDocumento,
                                        Boolean.FALSE
                                    };
                                    ((DefaultTableModel) grillaDocumentosNecesarios.getModel()).addRow(datos);
                                }
                            }
                        } else
                        {
                            grillaDocumentosNecesarios.setEnabled(false);
                            JOptionPane.showMessageDialog(this, "No existen Documentos en el sistema.", "ERROR", JOptionPane.ERROR_MESSAGE);
                            salir();
                        }

                        break;
                    }
                }
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                logger.log(Level.SEVERE, "Error al seleccionar trámite", ex);
            }
        } else
        {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Tipo de Tramite.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_botonSeleccionarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonCerrar;
    private javax.swing.JButton botonSeleccionar;
    private javax.swing.JTextField campoNombreTramite;
    private javax.swing.JTextArea campoObservaciones;
    private javax.swing.JCheckBox checkBoxInmueble;
    private javax.swing.JCheckBox checkBoxSeArchiva;
    private javax.swing.JCheckBox checkRequiereInscripcion;
    private javax.swing.JTable grillaDocumentosNecesarios;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JList listaTramitesDisponibles;
    private javax.swing.JPanel panelModificarTipoTramite;
    // End of variables declaration//GEN-END:variables
}
