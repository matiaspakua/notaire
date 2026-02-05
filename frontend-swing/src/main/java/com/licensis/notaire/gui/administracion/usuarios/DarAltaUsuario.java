/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.administracion.usuarios;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.dto.GenericDto;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.clientes.BuscarCliente;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorJpa;
import com.licensis.notaire.servicios.AdministradorValidaciones;
import com.licensis.notaire.servicios.GenericRestClient;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author matias
 */
public class DarAltaUsuario extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaDarAltaUsuario = new JMenuItem("Ventana Dar Alta Usuario");
    private static DarAltaUsuario instancia = null;
    private DtoPersona dtoPersona = null;
    private GenericRestClient usuarioClient = null;
    private GenericRestClient personaClient = null;
    private static final Logger logger = Logger.getLogger(DarAltaUsuario.class.getName());

    /**
     * Creates new form DarAltaUsuario
     */
    private DarAltaUsuario()
    {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioGrandeHorizontal, Principal.tamanioGrandeVertical);
        usuarioClient = AdministradorJpa.getInstancia().getUsuarioJpa();
        personaClient = AdministradorJpa.getInstancia().getPersonaJpa();
    }

    public DtoPersona getDtoPersona()
    {
        return dtoPersona;
    }

    public void setDtoPersona(DtoPersona dtoPersona)
    {
        this.dtoPersona = dtoPersona;
    }

    public static DarAltaUsuario getInstancia()
    {
        if (instancia == null)
        {
            instancia = new DarAltaUsuario();
        }
        return instancia;
    }

    private void salir()
    {
        this.dispose();
    }

    public static JMenuItem getVentanaDarAltaUsuario()
    {
        return ventanaDarAltaUsuario;
    }

    public void limpiarFormulario()
    {
        campoNombre.setText("");
        campoApellido.setText("");
        campoEmail.setText("");
        campoNumeroIdentificacion.setText("");
        campoTelefono.setText("");
        campoNombreUsuario.setText("");
        campoContrasenia.setText("");
        campoRepetirNuevaContrasenia.setText("");
        campoTipoIdentificacion.setText("");
        checkHabilitado.setSelected(true);
    }

    public Boolean cargarFormulario(DtoPersona dtoPersona) throws NonexistentJpaException
    {

        Boolean flag = false;

        //Controlo que la persona no tenga un usuario asociado
        DtoUsuario miDtoUsuario = new DtoUsuario();
        miDtoUsuario.setPersonas(dtoPersona);

        miDtoUsuario = ControllerNegocio.getInstancia().buscarUsuario(miDtoUsuario);

        if (miDtoUsuario != null)
        {
                String nombreUsuario = miDtoUsuario.getNombre() != null ? miDtoUsuario.getNombre() : "";
                JOptionPane.showMessageDialog(this, "La persona tiene un Usuario asignado : " + nombreUsuario);

        } else
        {
            //Habilito la vista pero no se puede modificar los datos de la persona.
            habilitarFormulario();

            //guardo ref a la persona para insertar el usuario, cuando se Acepte
            this.setDtoPersona(dtoPersona);

            campoNombre.setText(dtoPersona.getNombre());
            campoApellido.setText(dtoPersona.getApellido());
            campoNumeroIdentificacion.setText(dtoPersona.getNumeroIdentificacion());
            campoTelefono.setText(dtoPersona.getTelefono());
            campoEmail.setText(dtoPersona.getEmail());
            campoTipoIdentificacion.setText(dtoPersona.getDtoTipoIdentificacion().getNombre());

            this.toFront();
            flag = true;
        }
        return flag;
    }

    public void habilitarFormulario()
    {
        campoNombre.setEnabled(true);
        campoNombre.setEditable(false);

        campoApellido.setEditable(false);
        campoApellido.setEnabled(true);

        campoEmail.setEditable(false);
        campoEmail.setEnabled(true);

        campoTelefono.setEditable(false);
        campoTelefono.setEnabled(true);

        campoNumeroIdentificacion.setEditable(false);
        campoNumeroIdentificacion.setEnabled(true);

        campoTipoIdentificacion.setEditable(false);
        campoTipoIdentificacion.setEnabled(true);

        campoNombreUsuario.setEnabled(true);

        campoContrasenia.setEnabled(true);

        campoRepetirNuevaContrasenia.setEnabled(true);

        checkHabilitado.setEnabled(true);

        comboTipoDeUsuario.setEditable(false);
        comboTipoDeUsuario.setEnabled(true);

        botonAceptar.setEnabled(true);
    }

    public void desabilitarFormulario()
    {

        campoNombre.setEnabled(false);
        campoApellido.setEnabled(false);
        campoEmail.setEnabled(false);
        campoTelefono.setEnabled(false);
        campoNumeroIdentificacion.setEnabled(false);
        campoTipoIdentificacion.setEnabled(false);

        campoNombreUsuario.setEnabled(false);
        campoContrasenia.setEnabled(false);
        campoRepetirNuevaContrasenia.setEnabled(false);
        checkHabilitado.setEnabled(false);

        comboTipoDeUsuario.setEnabled(false);

        botonAceptar.setEnabled(false);
    }

    public static JMenuItem getVentanaDarAltaPersona()
    {
        return ventanaDarAltaUsuario;
    }

    private boolean isPasswordCorrect(char[] j1, char[] j2)
    {
        boolean valor = true;
        int puntero = 0;
        if (j1.length != j2.length)
        {
            valor = false;
        } else
        {
            while ((valor) && (puntero < j1.length))
            {
                if (j1[puntero] != j2[puntero])
                {
                    valor = false;
                }
                puntero++;
            }
        }
        return valor;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        panelDarAltaUsuario = new javax.swing.JPanel();
        labelTitulo = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        labelBusqueda = new javax.swing.JLabel();
        botonBuscarPersona = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        campoEmail = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        campoNombre = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        campoApellido = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        campoTelefono = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        campoNumeroIdentificacion = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        campoNombreUsuario = new  javax.swing.JTextField();

        ;
        jSeparator2 = new javax.swing.JSeparator();
        checkHabilitado = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        campoContrasenia = new javax.swing.JPasswordField();
        campoRepetirNuevaContrasenia = new javax.swing.JPasswordField();
        comboTipoDeUsuario = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        campoTipoIdentificacion = new javax.swing.JTextField();
        labelAdvertencia = new javax.swing.JLabel();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Dar Alta Usuario");
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

        labelTitulo.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        labelTitulo.setText("Dar Alta Usuario");

        botonAceptar.setText("Aceptar");
        botonAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAceptarActionPerformed(evt);
            }
        });

        botonCancelar.setText("Cancelar");
        botonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCancelarActionPerformed(evt);
            }
        });

        labelBusqueda.setText("Buscar Persona:");

        botonBuscarPersona.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/buscar.png"))); // NOI18N
        botonBuscarPersona.setText("Buscar");
        botonBuscarPersona.setIconTextGap(10);
        botonBuscarPersona.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarPersonaActionPerformed(evt);
            }
        });

        jLabel7.setText("E-Mail:");

        jLabel6.setText("Teléfono:");

        jLabel2.setText("Nombre:");

        jLabel3.setText("Apellido");

        jLabel4.setText("Tipo Ident.:");

        jLabel5.setText("Número Ident.:");

        jLabel9.setText("Nombre de Usuario:");

        jLabel10.setText("Contraseña:");

        jLabel11.setText("Repetir Contraseña:");

        campoNombreUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                campoNombreUsuarioKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoNombreUsuarioKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                campoNombreUsuarioKeyReleased(evt);
            }
        });

        checkHabilitado.setText("Habilitado");

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/tarjeta usuario.png"))); // NOI18N

        campoContrasenia.setText("jPasswordField1");

        campoRepetirNuevaContrasenia.setText("jPasswordField1");

        comboTipoDeUsuario.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Empleado", "Escribano" }));

        jLabel12.setText("Tipo de Usuario:");

        labelAdvertencia.setFont(new java.awt.Font("Cantarell", 1, 12)); // NOI18N
        labelAdvertencia.setForeground(new java.awt.Color(244, 3, 3));

        javax.swing.GroupLayout panelDarAltaUsuarioLayout = new javax.swing.GroupLayout(panelDarAltaUsuario);
        panelDarAltaUsuario.setLayout(panelDarAltaUsuarioLayout);
        panelDarAltaUsuarioLayout.setHorizontalGroup(
            panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                        .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelTitulo)
                            .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                                .addComponent(labelBusqueda)
                                .addGap(18, 18, 18)
                                .addComponent(botonBuscarPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel2)
                                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(campoTipoIdentificacion)
                                        .addComponent(campoApellido, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(campoNombre, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(campoTelefono, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(campoEmail, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(campoNumeroIdentificacion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                                    .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                                        .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel9)
                                            .addComponent(jLabel10)
                                            .addComponent(jLabel11)
                                            .addComponent(jLabel12))
                                        .addGap(37, 37, 37)
                                        .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(campoRepetirNuevaContrasenia)
                                            .addComponent(campoNombreUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                            .addComponent(campoContrasenia)
                                            .addComponent(checkHabilitado)
                                            .addComponent(comboTipoDeUsuario, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelAdvertencia, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 109, Short.MAX_VALUE))
                    .addComponent(jSeparator3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDarAltaUsuarioLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 802, Short.MAX_VALUE))
        );
        panelDarAltaUsuarioLayout.setVerticalGroup(
            panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelBusqueda)
                    .addComponent(botonBuscarPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jLabel2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDarAltaUsuarioLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(campoNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoApellido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(campoTipoIdentificacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoNumeroIdentificacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDarAltaUsuarioLayout.createSequentialGroup()
                        .addGap(0, 45, Short.MAX_VALUE)
                        .addComponent(labelAdvertencia, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(241, 241, 241)
                        .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                        .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(campoNombreUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel10)
                                            .addComponent(campoContrasenia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel11))
                                    .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                                        .addGap(47, 47, 47)
                                        .addComponent(campoRepetirNuevaContrasenia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(comboTipoDeUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel12))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(checkHabilitado)))
                        .addGap(0, 167, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(panelDarAltaUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelDarAltaUsuarioLayout.createSequentialGroup()
                    .addGap(90, 90, 90)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(565, Short.MAX_VALUE)))
        );

        jScrollPane1.setViewportView(panelDarAltaUsuario);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonAceptarActionPerformed
    {//GEN-HEADEREND:event_botonAceptarActionPerformed
        /*
         * Controlo que todos los campos que no contemplan null, esten completos
         */
        String msj = AdministradorValidaciones.getInstancia().validarAltaUsuario(this);

        if (msj == "")
        {
            try
            {
                DtoUsuario dtoUsuario = new DtoUsuario();

                //Version Inicial del objeto
                dtoUsuario.setVersion(0);

                //Armo Usuario
                dtoUsuario.setContrasenia(campoContrasenia.getText());
                dtoUsuario.setEstado(checkHabilitado.isSelected());
                dtoUsuario.setNombre(campoNombreUsuario.getText());

                switch (comboTipoDeUsuario.getSelectedItem().toString())
                {
                    case ConstantesNegocio.USUARIO_EMPLEADO:
                        dtoUsuario.setTipo(ConstantesNegocio.USUARIO_EMPLEADO);
                        break;
                    case ConstantesNegocio.USUARIO_ESCRIBANO:
                        dtoUsuario.setTipo(ConstantesNegocio.USUARIO_ESCRIBANO);
                        break;
                }

                //Set dtoPersona asociada al usuario
                dtoUsuario.setPersonas(this.getDtoPersona());

                // Convertir DtoUsuario a GenericDto
                GenericDto usuarioDto = new GenericDto();
                usuarioDto.put("nombre", dtoUsuario.getNombre());
                usuarioDto.put("contrasenia", dtoUsuario.getContrasenia());
                usuarioDto.put("estado", dtoUsuario.getEstado());
                usuarioDto.put("tipo", dtoUsuario.getTipo());
                usuarioDto.put("version", dtoUsuario.getVersion());
                
                if (dtoPersona != null && dtoPersona.getIdPersona() != null)
                {
                    usuarioDto.put("fkIdPersona", dtoPersona.getIdPersona());
                }

                // Verificar si ya existe un usuario con ese nombre
                List<GenericDto> todosLosUsuarios = usuarioClient.findAll();
                boolean usuarioExiste = false;
                for (GenericDto usuario : todosLosUsuarios)
                {
                    if (dtoUsuario.getNombre().equals(usuario.getString("nombre")))
                    {
                        usuarioExiste = true;
                        break;
                    }
                }

                if (!usuarioExiste)
                {
                    // Se da de alta el usuario
                    usuarioClient.create(usuarioDto);
                    JOptionPane.showMessageDialog(this, "Fue dado de alta: " + dtoUsuario.getNombre() + " " + dtoUsuario.getTipo(), "Informacion", JOptionPane.INFORMATION_MESSAGE);
                    this.limpiarFormulario();
                    this.desabilitarFormulario();
                } else
                {
                    JOptionPane.showMessageDialog(this, "Error - El nombre de usuario ya se encuentra registrado", "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, "Error al comunicarse con el servidor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                logger.log(Level.SEVERE, "Error al dar de alta usuario", ex);
            }
        } else
        {
            JOptionPane.showMessageDialog(this, msj);
        }

    }//GEN-LAST:event_botonAceptarActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCancelarActionPerformed
    {//GEN-HEADEREND:event_botonCancelarActionPerformed
        salir();
        Principal.removeVentanaActivas(DarAltaUsuario.getInstancia().getVentanaDarAltaUsuario());
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaDarAltaUsuario);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonBuscarPersonaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuscarPersonaActionPerformed

        //Busca una persona, el parametro de tipo de busqueda es el titulo del formulario
        this.limpiarFormulario();
        BuscarCliente formBuscarPersona = new BuscarCliente();
        formBuscarPersona.setTipoBusqueda(labelTitulo.getText());
        Principal.cargarFormulario(formBuscarPersona);

        Principal.setVentanasActivas(BuscarCliente.getVentanaBuscarCliente());
    }//GEN-LAST:event_botonBuscarPersonaActionPerformed

    private void campoNombreUsuarioKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoNombreUsuarioKeyReleased
        String campo = this.campoNombreUsuario.getText();
        Boolean flag = AdministradorValidaciones.getInstancia().validarCantidadCaracteres(10, campo);
        if (flag)
        {
            this.labelAdvertencia.setText("Maximo 10(diez) Caracteres");
            this.campoNombreUsuario.setText(campo.substring(0, 10));
        } else
        {
            this.labelAdvertencia.setText("");
        }

    }//GEN-LAST:event_campoNombreUsuarioKeyReleased

    private void campoNombreUsuarioKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoNombreUsuarioKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_campoNombreUsuarioKeyTyped

    private void campoNombreUsuarioKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoNombreUsuarioKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoNombreUsuarioKeyPressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonBuscarPersona;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JTextField campoApellido;
    public javax.swing.JPasswordField campoContrasenia;
    private javax.swing.JTextField campoEmail;
    private javax.swing.JTextField campoNombre;
    public javax.swing.JTextField campoNombreUsuario;
    private javax.swing.JTextField campoNumeroIdentificacion;
    public javax.swing.JPasswordField campoRepetirNuevaContrasenia;
    private javax.swing.JTextField campoTelefono;
    private javax.swing.JTextField campoTipoIdentificacion;
    private javax.swing.JCheckBox checkHabilitado;
    private javax.swing.JComboBox comboTipoDeUsuario;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labelAdvertencia;
    private javax.swing.JLabel labelBusqueda;
    private javax.swing.JLabel labelTitulo;
    private javax.swing.JPanel panelDarAltaUsuario;
    // End of variables declaration//GEN-END:variables
}
