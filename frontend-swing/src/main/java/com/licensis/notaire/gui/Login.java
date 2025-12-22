package com.licensis.notaire.gui;

import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.servicios.AuthService;
import com.licensis.notaire.util.SessionManager;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Formulario de Login migrado para usar REST API
 * @author matias
 */
public class Login extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private Principal formPrincipal = null;
    private AuthService authService = new AuthService();

    /**
     * Creates new form login
     */
    public Login() {
        initComponents();
        grupo.setBorder(BorderFactory.createTitledBorder("Login"));
        this.centrarFormulario();
        this.setMinimumSize(new Dimension(400, 450));
        this.setMaximumSize(new Dimension(400, 450));
        this.getRootPane().setDefaultButton(botonAceptar);
    }

    private void centrarFormulario() {
        this.setLocationRelativeTo(null);
    }

    private void salir() {
        System.exit(0);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        panelLongin = new javax.swing.JPanel();
        botonImagen = new javax.swing.JButton();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        grupo = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        campoUsuario = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        campoContrasenia = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelLongin.setVerifyInputWhenFocusTarget(false);
        panelLongin.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                panelLonginKeyPressed(evt);
            }
        });

        botonImagen.setBackground(new java.awt.Color(0, 51, 102));
        botonImagen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/logoRojoPalabra.png")));
        botonImagen.setBorder(null);
        botonImagen.setBorderPainted(false);
        botonImagen.setContentAreaFilled(false);
        botonImagen.setFocusPainted(false);
        botonImagen.setFocusable(false);

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

        grupo.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Cantarell", 0, 14));
        jLabel2.setText("Usuario:");

        jLabel3.setFont(new java.awt.Font("Cantarell", 0, 14));
        jLabel3.setText("Contraseña:");

        javax.swing.GroupLayout grupoLayout = new javax.swing.GroupLayout(grupo);
        grupo.setLayout(grupoLayout);
        grupoLayout.setHorizontalGroup(
            grupoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(grupoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(grupoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(grupoLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(campoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(grupoLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(campoContrasenia, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        grupoLayout.setVerticalGroup(
            grupoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(grupoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(grupoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(campoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(grupoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(campoContrasenia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelLonginLayout = new javax.swing.GroupLayout(panelLongin);
        panelLongin.setLayout(panelLonginLayout);
        panelLonginLayout.setHorizontalGroup(
            panelLonginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLonginLayout.createSequentialGroup()
                .addGroup(panelLonginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelLonginLayout.createSequentialGroup()
                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelLonginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelLonginLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(grupo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(panelLonginLayout.createSequentialGroup()
                            .addGap(88, 88, 88)
                            .addComponent(botonImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        panelLonginLayout.setVerticalGroup(
            panelLonginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLonginLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(botonImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(grupo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addGroup(panelLonginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelLongin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelLongin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {
        String nombre = campoUsuario.getText();
        String contrasenia = new String(campoContrasenia.getPassword());

        if (nombre == null || nombre.trim().isEmpty() || contrasenia == null || contrasenia.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese usuario y contraseña", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DtoUsuario usuarioLogin = authService.login(nombre, contrasenia);

            if (usuarioLogin != null && usuarioLogin.isValido()) {
                formPrincipal = Principal.getInstancia();
                String nombreUsuario = SessionManager.getInstancia().getNombreCompletoUsuario();
                formPrincipal.cargarVentanaUsuario(usuarioLogin);
                formPrincipal.itemUsuario.setText(nombreUsuario);
                formPrincipal.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error de Login", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error de conexión con el servidor.\nVerifique que el backend esté corriendo en http://localhost:8080", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error inesperado: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {
        salir();
    }

    private void panelLonginKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            botonAceptarActionPerformed(null);
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Login().setVisible(true);
            }
        });
    }

    // Variables declaration
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JButton botonImagen;
    private javax.swing.JPasswordField campoContrasenia;
    private javax.swing.JTextField campoUsuario;
    private javax.swing.JPanel grupo;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel panelLongin;
}

