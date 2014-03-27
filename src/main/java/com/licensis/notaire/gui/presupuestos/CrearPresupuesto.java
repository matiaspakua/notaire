/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.presupuestos;

import com.licensis.notaire.dto.DtoInmueble;
import com.licensis.notaire.dto.DtoItem;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.licensis.notaire.dto.DtoTipoDeTramite;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.clientes.BuscarCliente;
import java.util.*;
import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorReportes;

/**
 *
 * @author Tefi
 */
public class CrearPresupuesto extends javax.swing.JInternalFrame
{

    // <editor-fold defaultstate="collapsed" desc="ATRIBUTOS">
    private static CrearPresupuesto instancia = null;
    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaCrearPresupuesto = new JMenuItem("Ventana Crear Presupuestos");
    private List<DtoTipoDeTramite> tramitesDisponibles = new ArrayList<>();
    private ControllerNegocio miController = null;
    private Boolean configurado = false;
    private DtoTipoDeTramite miDtoSeleccionado;
    private DtoPersona miDtoPersona = null;
    private DtoInmueble inmuebleAsociado = null;
    private DtoTramite tramite = null;
    private DtoPresupuesto presupuestoCreado = null;
    private ArrayList<DtoItem> itemsAsociados = null;
    private DetalleValoresTramites detalle = null;
    private Map parameters = new HashMap();

// </editor-fold>
    /**
     * Creates new form CrearPresupuesto
     */
    private CrearPresupuesto() {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioGrandeVertical);

        miController = ControllerNegocio.getInstancia();

        listaTramites.setEnabled(false);
        botonSeleccionar2.setEnabled(false);
        campoObservacionesPresupuesto.setEnabled(false);
        botonConfirmar.setEnabled(false);
        limpiarFormulario();

    }

    public static CrearPresupuesto getInstancia() {
        if (instancia == null)
        {
            instancia = new CrearPresupuesto();
        }
        return instancia;
    }

    private void salir() {
        this.dispose();
    }

    public static JMenuItem getVentanaCrearPresupuesto() {
        return ventanaCrearPresupuesto;
    }

    public void cargarFormulario(DtoPersona miPersona) {

        campoNombre.setText(miPersona.getNombre());
        campoApellido.setText(miPersona.getApellido());
        campoIdentificacion.setText(miPersona.getDtoTipoIdentificacion().getNombre());
        campoNumeroIdentificacion.setText(miPersona.getNumeroIdentificacion());
        campoTelefono.setText(miPersona.getTelefono());
        campoCorreo.setText(miPersona.getEmail());

        miDtoPersona = miPersona;

        cargarTramites();
    }

    public void limpiarFormulario() {
        miDtoSeleccionado = null;
        miDtoPersona = null;
        inmuebleAsociado = null;
        tramite = null;
        presupuestoCreado = null;
        itemsAsociados = null;
        
        detalle = null;
        campoNombre.setText("");
        campoApellido.setText("");
        campoIdentificacion.setText("");
        campoNumeroIdentificacion.setText("");
        campoTelefono.setText("");
        campoCorreo.setText("");

        campoTotalPresupuesto.setText("");

        DefaultListModel lista = new DefaultListModel();
        listaTramites.setModel(lista);
        
        configurado = false;
    }

    private void cargarTramites() {

        tramitesDisponibles = new ArrayList<>();
        tramitesDisponibles = miController.buscarTiposDeTramiteHabilitados();

        if (tramitesDisponibles.isEmpty())
        {

            JOptionPane.showMessageDialog(this, "No existen tipos de tramite registrados.", "INFORMACION", JOptionPane.INFORMATION_MESSAGE);
            salir();

        }
        else
        {

            listaTramites.setEnabled(true);
            botonSeleccionar2.setEnabled(true);

            DefaultListModel lista = new DefaultListModel();

            for (Iterator<DtoTipoDeTramite> it = tramitesDisponibles.iterator(); it.hasNext();)
            {

                DtoTipoDeTramite miDto = it.next();

                //Solo agrego las que tienen plantilla de presupuesto configurada
                if (miController.existePlantillaPresupuesto(miDto))
                {

                    lista.addElement(miDto.getNombre());
                }
            }

            if (lista.isEmpty())
            {
                JOptionPane.showMessageDialog(this, "No existen Tipos de Tramite con Plantilla de Presupuesto configurada.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                salir();
            }
            else
            {

                this.listaTramites.setModel(lista);
            }
        }
    }

    public void setConfigurado(Boolean valor, DtoInmueble miDtoInmueble, DtoTramite miDtoTramite, DtoPresupuesto miDtoPresupuesto, ArrayList<DtoItem> dtoItems) {
        configurado = valor;

        if (miDtoInmueble != null)
        {
            inmuebleAsociado = miDtoInmueble;
        }
        else
        {
            inmuebleAsociado = null;
        }

        tramite = miDtoTramite;
        presupuestoCreado = miDtoPresupuesto;
        itemsAsociados = dtoItems;

        campoTotalPresupuesto.setText(miDtoPresupuesto.getTotal().toString());

        campoObservacionesPresupuesto.setEnabled(true);
        botonConfirmar.setEnabled(true);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        panelCrearPresupuesto = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        campoNombre = new javax.swing.JTextField();
        campoApellido = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        campoNumeroIdentificacion = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        campoTotalPresupuesto = new javax.swing.JTextField();
        botonConfirmar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        campoTelefono = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        campoCorreo = new javax.swing.JTextField();
        botonBuscarCliente = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        listaTramites = new javax.swing.JList();
        botonSeleccionar2 = new javax.swing.JButton();
        campoIdentificacion = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        campoObservacionesPresupuesto = new javax.swing.JTextArea();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Crear Presupuestos");
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

        jScrollPane3.setAutoscrolls(true);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Crear Presupuesto");

        jLabel8.setText("Buscar Persona:");

        jLabel2.setText("Nombre:");

        campoNombre.setEditable(false);

        campoApellido.setEditable(false);

        jLabel3.setText("Apellido");

        jLabel4.setText("Tipo Ident.:");

        campoNumeroIdentificacion.setEditable(false);

        jLabel5.setText("Número Ident.:");

        jLabel6.setText("Tipo de Trámite:");

        jLabel7.setText("Total:");

        campoTotalPresupuesto.setEditable(false);

        botonConfirmar.setText("Aceptar");
        botonConfirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonConfirmarActionPerformed(evt);
            }
        });

        botonCancelar.setText("Cancelar");
        botonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCancelarActionPerformed(evt);
            }
        });

        jLabel9.setText("Teléfono:");

        campoTelefono.setEditable(false);

        jLabel10.setText("Correo Electrónico:");

        campoCorreo.setEditable(false);

        botonBuscarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/src/main/resources/iconos/buscar.png"))); // NOI18N
        botonBuscarCliente.setText("Buscar");
        botonBuscarCliente.setIconTextGap(10);
        botonBuscarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarClienteActionPerformed(evt);
            }
        });

        listaTramites.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(listaTramites);

        botonSeleccionar2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/src/main/resources/iconos/aceptar.png"))); // NOI18N
        botonSeleccionar2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonSeleccionar2ActionPerformed(evt);
            }
        });

        campoIdentificacion.setEditable(false);
        campoIdentificacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoIdentificacionActionPerformed(evt);
            }
        });

        jLabel11.setText("Observaciones:");

        campoObservacionesPresupuesto.setColumns(20);
        campoObservacionesPresupuesto.setRows(5);
        campoObservacionesPresupuesto.setEnabled(false);
        jScrollPane2.setViewportView(campoObservacionesPresupuesto);

        javax.swing.GroupLayout panelCrearPresupuestoLayout = new javax.swing.GroupLayout(panelCrearPresupuesto);
        panelCrearPresupuesto.setLayout(panelCrearPresupuestoLayout);
        panelCrearPresupuestoLayout.setHorizontalGroup(
            panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCrearPresupuestoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCrearPresupuestoLayout.createSequentialGroup()
                        .addComponent(botonConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelCrearPresupuestoLayout.createSequentialGroup()
                        .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(botonSeleccionar2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelCrearPresupuestoLayout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(18, 18, 18)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelCrearPresupuestoLayout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(botonBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelCrearPresupuestoLayout.createSequentialGroup()
                                        .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel5)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel9)
                                            .addComponent(jLabel10))
                                        .addGap(24, 24, 24)
                                        .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(campoCorreo)
                                            .addComponent(campoNumeroIdentificacion, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                            .addComponent(campoTelefono, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                            .addComponent(campoApellido)
                                            .addComponent(campoNombre)
                                            .addComponent(campoIdentificacion)))
                                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGroup(panelCrearPresupuestoLayout.createSequentialGroup()
                                    .addComponent(jLabel7)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(campoTotalPresupuesto, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(panelCrearPresupuestoLayout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane2)))
                        .addGap(0, 158, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelCrearPresupuestoLayout.setVerticalGroup(
            panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCrearPresupuestoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelCrearPresupuestoLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8))
                    .addComponent(botonBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(campoNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoApellido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(campoIdentificacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoNumeroIdentificacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonSeleccionar2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoTotalPresupuesto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCrearPresupuestoLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(panelCrearPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jScrollPane3.setViewportView(panelCrearPresupuesto);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaCrearPresupuesto);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonConfirmarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonConfirmarActionPerformed
    {//GEN-HEADEREND:event_botonConfirmarActionPerformed
        if (configurado == true)
        {
            if (!campoObservacionesPresupuesto.getText().isEmpty())
            {
                presupuestoCreado.setObservaciones(campoObservacionesPresupuesto.getText());
            }
            else
            {
                presupuestoCreado.setObservaciones("");
            }

            int creado = -1;

            if (inmuebleAsociado != null)
            {
                creado = miController.crearPresupuesto(miDtoPersona, presupuestoCreado, tramite, inmuebleAsociado, itemsAsociados);
            }
            else
            {
                creado = miController.crearPresupuesto(miDtoPersona, presupuestoCreado, tramite, itemsAsociados);
            }

            if (creado != -1)
            {

                int option = JOptionPane.showConfirmDialog(this, "<HTML>Se ha creado el presupuesto con numero: " + creado + "<BR>¿Desea imprimir el detalle del mismo?</HTML>", "CONFIRMACION", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION)
                {

                    DtoPresupuesto miDtoPresupuesto = new DtoPresupuesto();
                    miDtoPresupuesto.setIdPresupuesto(creado);

                    DtoPresupuesto miPresupuestoEncontrado = miController.buscarPresupuestoPorNumero(miDtoPresupuesto);

                    if (miPresupuestoEncontrado != null)
                    {

                        AdministradorReportes reportes = AdministradorReportes.getInstancia();
                        reportes.generarReportePresupuesto(miPresupuestoEncontrado);                        
                    }
                }
                this.limpiarFormulario();
                
                this.salir();
            }
            else
            {
                JOptionPane.showMessageDialog(this, "No se pudo crear el Presupuesto.", "Error de sistema", JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Debe configurar el presupuesto.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_botonConfirmarActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCancelarActionPerformed
    {//GEN-HEADEREND:event_botonCancelarActionPerformed
        limpiarFormulario();
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void botonBuscarClienteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonBuscarClienteActionPerformed
    {//GEN-HEADEREND:event_botonBuscarClienteActionPerformed
        BuscarCliente miForm = BuscarCliente.getInstancia();
        miForm.limpiarFormulario();
        this.limpiarFormulario();
        miForm.setTipoBusqueda(ConstantesGui.CREAR_PRESUPUESTO);
        
        Principal.cargarFormulario(miForm);
        Principal.setVentanasActivas(BuscarCliente.getVentanaBuscarCliente());
    }//GEN-LAST:event_botonBuscarClienteActionPerformed

    private void botonSeleccionar2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonSeleccionar2ActionPerformed
        Boolean flag = Boolean.FALSE;

        if (tramitesDisponibles != null && !tramitesDisponibles.isEmpty())
        {

            if (listaTramites.getSelectedValue() != null)
            {
                flag = Boolean.TRUE;

                String seleccionado = listaTramites.getSelectedValue().toString();

                for (Iterator<DtoTipoDeTramite> it = tramitesDisponibles.iterator(); it.hasNext();)
                {
                    DtoTipoDeTramite dtoTipoDeTramite = it.next();

                    // Completo los datos en los componentes

                    if (dtoTipoDeTramite.getNombre().equals(seleccionado))
                    {

                        miDtoSeleccionado = dtoTipoDeTramite;
                        break;
                    }
                }

                detalle = new DetalleValoresTramites();

                detalle.limpiarFormulario();
                detalle.setTramite(miDtoSeleccionado);


                listaTramites.setEnabled(false);
                botonSeleccionar2.setEnabled(false);
                botonConfirmar.setEnabled(true);

                Principal.cargarFormulario(detalle);
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un Tipo de Tramite.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            }
        }
        else if (miDtoPersona == null)
        {
            JOptionPane.showMessageDialog(this, "Debe buscar a una Persona.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_botonSeleccionar2ActionPerformed

    private void campoIdentificacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoIdentificacionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoIdentificacionActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonBuscarCliente;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JButton botonConfirmar;
    private javax.swing.JButton botonSeleccionar2;
    private javax.swing.JTextField campoApellido;
    private javax.swing.JTextField campoCorreo;
    private javax.swing.JTextField campoIdentificacion;
    private javax.swing.JTextField campoNombre;
    private javax.swing.JTextField campoNumeroIdentificacion;
    private javax.swing.JTextArea campoObservacionesPresupuesto;
    private javax.swing.JTextField campoTelefono;
    private javax.swing.JTextField campoTotalPresupuesto;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JList listaTramites;
    private javax.swing.JPanel panelCrearPresupuesto;
    // End of variables declaration//GEN-END:variables
}
