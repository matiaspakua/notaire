/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.administracion.usuarios;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.dto.GenericDto;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.servicios.AdministradorJpa;
import com.licensis.notaire.servicios.AdministradorValidaciones;
import com.licensis.notaire.servicios.GenericRestClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class ModificarUsuario extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaModificarUsuario = new JMenuItem("Ventana Modificar Usuario");
    private static ModificarUsuario instancia = null;
    private DtoUsuario dtoUsuarioModificado = null;
    private GenericRestClient usuarioClient = null;
    private GenericRestClient personaClient = null;
    private static final Logger logger = Logger.getLogger(ModificarUsuario.class.getName());

    /**
     * Creates new form ModificarUsuario
     */
    public ModificarUsuario()
    {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioGrandeHorizontal, Principal.tamanioGrandeVertical);
        usuarioClient = AdministradorJpa.getInstancia().getUsuarioJpa();
        personaClient = AdministradorJpa.getInstancia().getPersonaJpa();

        grillaUsuarioDisponibles.setAutoCreateRowSorter(true);
        //Oculto columnas
        grillaUsuarioDisponibles.getColumnModel().getColumn(5).setMaxWidth(0);
        grillaUsuarioDisponibles.getColumnModel().getColumn(5).setMinWidth(0);
        grillaUsuarioDisponibles.getColumnModel().getColumn(6).setMaxWidth(0);
        grillaUsuarioDisponibles.getColumnModel().getColumn(6).setMinWidth(0);
    }

    public static ModificarUsuario getInstancia()
    {

        if (instancia == null)
        {
            instancia = new ModificarUsuario();
        }
        return instancia;
    }

    public DtoUsuario getDtoUsuarioModificado()
    {
        return dtoUsuarioModificado;
    }

    public void setDtoUsuarioModificado(DtoUsuario dtoUsuarioModificado)
    {
        this.dtoUsuarioModificado = dtoUsuarioModificado;
    }

    private void salir()
    {
        this.dispose();
    }

    public static JMenuItem getVentanaModificarUsuario()
    {
        return ventanaModificarUsuario;
    }

    public void limpiarFormulario()
    {
        limpiarJtable();
        campoNuevoNombreUsuario.setText("");
        campoNuevaContrasenia.setText("");
        campoRepetirNuevaContrasenia.setText("");
        checkHabilitado.setSelected(false);
        comboTipoDeUsuario.setSelectedItem("Recepcionista");
    }

    public void limpiarJtable()
    {

        int i = ((DefaultTableModel) grillaUsuarioDisponibles.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaUsuarioDisponibles.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaUsuarioDisponibles.getModel()).removeRow(i);
            i--;
        }

    }

    public void desabilitarFormulario()
    {

        campoNuevoNombreUsuario.setEnabled(false);
        campoNuevaContrasenia.setEnabled(false);
        campoRepetirNuevaContrasenia.setEnabled(false);
        comboTipoDeUsuario.setEnabled(false);
        checkHabilitado.setEnabled(false);

        botonGuardar.setEnabled(false);
    }

    public void habilitarFormulario()
    {
        campoNuevoNombreUsuario.setEnabled(true);
        campoNuevaContrasenia.setEnabled(true);
        campoRepetirNuevaContrasenia.setEnabled(true);
        comboTipoDeUsuario.setEnabled(true);
        checkHabilitado.setEnabled(true);

        botonGuardar.setEnabled(true);
    }

    public void cargarUsuariosDisponibles()
    {
        try
        {
            List<GenericDto> todosLosUsuarios = usuarioClient.findAll();
            List<GenericDto> todasLasPersonas = personaClient.findAll();
            java.util.Map<Integer, GenericDto> personasMap = new java.util.HashMap<>();
            
            for (GenericDto persona : todasLasPersonas)
            {
                Integer idPersona = persona.getInt("idPersona");
                if (idPersona != null)
                {
                    personasMap.put(idPersona, persona);
                }
            }

            if (todosLosUsuarios != null && !todosLosUsuarios.isEmpty())
            {
                for (GenericDto miDtoUsuario : todosLosUsuarios)
                {
                    Integer fkIdPersona = miDtoUsuario.getInt("fkIdPersona");
                    GenericDto persona = personasMap.get(fkIdPersona);
                    
                    String nombrePersona = persona != null ? persona.getString("nombre") : "N/A";
                    String apellidoPersona = persona != null ? persona.getString("apellido") : "N/A";
                    String nombreUsuario = miDtoUsuario.getString("nombre");
                    
                    Object[] datos =
                    {
                        nombrePersona,
                        apellidoPersona,
                        nombreUsuario,
                        miDtoUsuario.getString("tipo"),
                        miDtoUsuario.getBoolean("estado"),
                        miDtoUsuario.getInt("idUsuario"),
                        //Controlo la version del objeto
                        miDtoUsuario.getInt("version")
                    };

                    ((DefaultTableModel) grillaUsuarioDisponibles.getModel()).addRow(datos);
                }
            } else
            {
                JOptionPane.showMessageDialog(this, "No existen Usuarios Registrados");
            }
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void ocultarColumnasJtable()
    {
    }

    public void cargarVistaUsuario(DtoUsuario dtoUsuario)
    {
        campoNuevoNombreUsuario.setText(dtoUsuario.getNombre());
        campoNuevaContrasenia.setText(dtoUsuario.getContrasenia());
        campoRepetirNuevaContrasenia.setText(dtoUsuario.getContrasenia());
        comboTipoDeUsuario.setSelectedItem(dtoUsuario.getTipo());
        checkHabilitado.setSelected(dtoUsuario.isEstado());
        this.habilitarFormulario();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        panelModificarUsuario = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        botonCerrar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaUsuarioDisponibles = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        checkHabilitado = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        campoNuevoNombreUsuario = new javax.swing.JTextField();
        botonGuardar = new javax.swing.JButton();
        botonSeleccionar = new javax.swing.JButton();
        campoNuevaContrasenia = new javax.swing.JPasswordField();
        campoRepetirNuevaContrasenia = new javax.swing.JPasswordField();
        comboTipoDeUsuario = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        labelAdvertencia = new javax.swing.JLabel();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Modificar Usuario");
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
        jLabel1.setText("Modificar Usuario");

        botonCerrar.setText("Cancelar");
        botonCerrar.setIconTextGap(10);
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });

        jLabel2.setText("Lista de Usuarios Disponibles:");

        grillaUsuarioDisponibles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nombre", "Apellido", "Usuario", "Tipo", "Estado", "id_usuario", "version"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(grillaUsuarioDisponibles);
        grillaUsuarioDisponibles.getColumnModel().getColumn(5).setResizable(false);
        grillaUsuarioDisponibles.getColumnModel().getColumn(6).setResizable(false);
        //Oculto columnas
        grillaUsuarioDisponibles.getColumnModel().getColumn(5).setMaxWidth(0);
        grillaUsuarioDisponibles.getColumnModel().getColumn(5).setMinWidth(0);
        grillaUsuarioDisponibles.getColumnModel().getColumn(6).setMaxWidth(0);
        grillaUsuarioDisponibles.getColumnModel().getColumn(6).setMinWidth(0);

        checkHabilitado.setText("Habilitado");

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/tarjeta usuario.png"))); // NOI18N

        jLabel9.setText("Nombre de Usuario:");

        jLabel10.setText("Contraseña:");

        jLabel11.setText("Repetir Contraseña:");

        campoNuevoNombreUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                campoNuevoNombreUsuarioKeyReleased(evt);
            }
        });

        botonGuardar.setText("Aceptar");
        botonGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonGuardarActionPerformed(evt);
            }
        });

        botonSeleccionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/aceptar.png"))); // NOI18N
        botonSeleccionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonSeleccionarActionPerformed(evt);
            }
        });

        campoNuevaContrasenia.setText("jPasswordField1");

        campoRepetirNuevaContrasenia.setText("jPasswordField1");

        comboTipoDeUsuario.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Empleado", "Escribano" }));

        jLabel12.setText("Tipo de Usuario:");

        labelAdvertencia.setFont(new java.awt.Font("Cantarell", 1, 12)); // NOI18N
        labelAdvertencia.setForeground(new java.awt.Color(248, 11, 11));

        javax.swing.GroupLayout panelModificarUsuarioLayout = new javax.swing.GroupLayout(panelModificarUsuario);
        panelModificarUsuario.setLayout(panelModificarUsuarioLayout);
        panelModificarUsuarioLayout.setHorizontalGroup(
            panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModificarUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addComponent(jScrollPane1)
                    .addGroup(panelModificarUsuarioLayout.createSequentialGroup()
                        .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addGroup(panelModificarUsuarioLayout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel11))
                                .addGap(37, 37, 37)
                                .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(comboTipoDeUsuario, 0, 168, Short.MAX_VALUE)
                                    .addGroup(panelModificarUsuarioLayout.createSequentialGroup()
                                        .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(checkHabilitado)
                                            .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(campoNuevaContrasenia, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                                .addComponent(campoRepetirNuevaContrasenia)
                                                .addComponent(campoNuevoNombreUsuario)))
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 55, Short.MAX_VALUE)
                        .addComponent(labelAdvertencia, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelModificarUsuarioLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelModificarUsuarioLayout.createSequentialGroup()
                                .addComponent(botonGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(botonSeleccionar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        panelModificarUsuarioLayout.setVerticalGroup(
            panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModificarUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonSeleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelModificarUsuarioLayout.createSequentialGroup()
                        .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelAdvertencia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel9)
                                .addComponent(campoNuevoNombreUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(campoNuevaContrasenia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(campoRepetirNuevaContrasenia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboTipoDeUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)))
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(checkHabilitado)
                .addGap(18, 18, 18)
                .addGroup(panelModificarUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jScrollPane2.setViewportView(panelModificarUsuario);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaModificarUsuario);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonGuardarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonGuardarActionPerformed
    {//GEN-HEADEREND:event_botonGuardarActionPerformed
        String msj = AdministradorValidaciones.getInstancia().validarModificarUsuario(this);

        if (msj == "")
        {

            // Validar contraseñas
            String nuevaContrasenia = new String(campoNuevaContrasenia.getPassword());
            String repetirContrasenia = new String(campoRepetirNuevaContrasenia.getPassword());
            boolean flag = nuevaContrasenia.equals(repetirContrasenia);

            if (flag)
            {
                DtoUsuario miDtoUsuario = this.getDtoUsuarioModificado();
                
                if (miDtoUsuario != null && miDtoUsuario.getIdUsuario() != null)
                {
                    // Convertir DtoUsuario a GenericDto
                    GenericDto usuarioDto = new GenericDto();
                    usuarioDto.put("idUsuario", miDtoUsuario.getIdUsuario());
                    usuarioDto.put("nombre", campoNuevoNombreUsuario.getText());
                    usuarioDto.put("contrasenia", nuevaContrasenia);
                    usuarioDto.put("estado", checkHabilitado.isSelected());
                    usuarioDto.put("version", miDtoUsuario.getVersion());
                    
                    switch (comboTipoDeUsuario.getSelectedItem().toString())
                    {
                        case ConstantesNegocio.USUARIO_EMPLEADO:
                        {
                            usuarioDto.put("tipo", ConstantesNegocio.USUARIO_EMPLEADO);
                            break;
                        }
                        case ConstantesNegocio.USUARIO_ESCRIBANO:
                        {
                            usuarioDto.put("tipo", ConstantesNegocio.USUARIO_ESCRIBANO);
                            break;
                        }
                    }
                    
                    if (miDtoUsuario.getPersonas() != null && miDtoUsuario.getPersonas().getIdPersona() != null)
                    {
                        usuarioDto.put("fkIdPersona", miDtoUsuario.getPersonas().getIdPersona());
                    }

                    try
                    {
                        usuarioClient.edit(usuarioDto);
                        
                        JOptionPane.showMessageDialog(this, "Se ha modificado un usuario: " + miDtoUsuario.getPersonas().getNombre() + " " + miDtoUsuario.getPersonas().getApellido(), "Informacion", JOptionPane.INFORMATION_MESSAGE);
                        this.limpiarFormulario();
                        this.desabilitarFormulario();
                        ModificarUsuario.getInstancia().cargarUsuariosDisponibles();
                    }
                    catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(this, "Error al comunicarse con el servidor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        logger.log(Level.SEVERE, "Error al modificar usuario", ex);
                        this.limpiarFormulario();
                        try
                        {
                            ModificarUsuario.getInstancia().cargarUsuariosDisponibles();
                        }
                        catch (IOException ex2)
                        {
                            JOptionPane.showMessageDialog(this, "Error grave, accion cancelada", "Error", JOptionPane.ERROR_MESSAGE);
                            logger.log(Level.SEVERE, "Error al cargar usuarios", ex2);
                            this.salir();
                        }
                        return;
                    }
                }
            }
        } else
        {
            JOptionPane.showMessageDialog(this, msj);
        }

    }//GEN-LAST:event_botonGuardarActionPerformed

    private void botonSeleccionarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonSeleccionarActionPerformed
    {//GEN-HEADEREND:event_botonSeleccionarActionPerformed

        TableModel miGrilla = grillaUsuarioDisponibles.getModel();
        int filaSeleccionada = grillaUsuarioDisponibles.getSelectedRow();
        filaSeleccionada = grillaUsuarioDisponibles.convertRowIndexToModel(filaSeleccionada);

        if (filaSeleccionada > -1)
        {
            int filas = miGrilla.getRowCount();
            int columnas = miGrilla.getColumnCount();

            //Recorro la grilla completa, buscando el cliente seleccionado
            for (int i = 0; i < filas; i++)
            {
                if (i == filaSeleccionada)
                {
                    dtoUsuarioModificado = new DtoUsuario();
                    //Set id usuario para modificarlo
                    DtoPersona miPersona = new DtoPersona();
                    miPersona.setNombre(miGrilla.getValueAt(i, 0).toString());
                    miPersona.setApellido(miGrilla.getValueAt(i, 1).toString());
                    dtoUsuarioModificado.setPersonas(miPersona);

                    dtoUsuarioModificado.setNombre(miGrilla.getValueAt(i, 2).toString());
                    dtoUsuarioModificado.setTipo(miGrilla.getValueAt(i, 3).toString());
                    dtoUsuarioModificado.setEstado((Boolean) (miGrilla.getValueAt(i, 4)));
                    dtoUsuarioModificado.setIdUsuario(miGrilla.getValueAt(i, 5).hashCode());

                    //Conotrlo Version del Objeto
                    dtoUsuarioModificado.setVersion(miGrilla.getValueAt(i, 6).hashCode());

                    //Cargo datos del usuario en el formulario
                    cargarVistaUsuario(dtoUsuarioModificado);
                }
            }
        } else
        {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Usuario");
        }

    }//GEN-LAST:event_botonSeleccionarActionPerformed

    private void campoNuevoNombreUsuarioKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoNuevoNombreUsuarioKeyReleased
        String campo = this.campoNuevoNombreUsuario.getText();
        Boolean flag = AdministradorValidaciones.getInstancia().validarCantidadCaracteres(10, campo);

        if (flag)
        {
            this.labelAdvertencia.setText("Maximo 10(diez) Caracteres");
            this.campoNuevoNombreUsuario.setText(campo.substring(0, 10));
        } else
        {
            this.labelAdvertencia.setText("");
        }

    }//GEN-LAST:event_campoNuevoNombreUsuarioKeyReleased

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCerrarActionPerformed
        salir();
        Principal.removeVentanaActivas(ModificarUsuario.getInstancia().getVentanaModificarUsuario());
    }//GEN-LAST:event_botonCerrarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCerrar;
    private javax.swing.JButton botonGuardar;
    private javax.swing.JButton botonSeleccionar;
    public javax.swing.JPasswordField campoNuevaContrasenia;
    public javax.swing.JTextField campoNuevoNombreUsuario;
    public javax.swing.JPasswordField campoRepetirNuevaContrasenia;
    private javax.swing.JCheckBox checkHabilitado;
    private javax.swing.JComboBox comboTipoDeUsuario;
    private javax.swing.JTable grillaUsuarioDisponibles;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelAdvertencia;
    private javax.swing.JPanel panelModificarUsuario;
    // End of variables declaration//GEN-END:variables
}
