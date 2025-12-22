package com.licensis.notaire.gui;

import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.negocio.ConstantesNegocio;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal de la aplicación
 * @author matias
 */
public class Principal extends javax.swing.JFrame {

    private static Principal instancia = null;
    public static final Integer tamanioMinimoHorizontal = 650;
    public static final Integer tamanioMinimoVertical = 350;
    public static final Integer tamanioNormalHorizontal = 700;
    public static final Integer tamanioNormalVertical = 600;
    public static final Integer tamanioGrandeHorizontal = 980;
    public static final Integer tamanioGrandeVertical = 690;

    private Principal() {
        initComponents();
        Principal.AreaTrabajo.setBackground(Color.LIGHT_GRAY);
        this.setExtendedState(Principal.MAXIMIZED_BOTH);
        menuOpciones.setVisible(false);
        this.scrollAreaTrabajo.setEnabled(true);
        this.scrollAreaTrabajo.setWheelScrollingEnabled(true);
        this.scrollAreaTrabajo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scrollAreaTrabajo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public static Principal getInstancia() {
        if (instancia == null) {
            instancia = new Principal();
        }
        return instancia;
    }

    public void cargarVentanaUsuario(DtoUsuario miDtoUsuario) {
        if (miDtoUsuario == null) return;
        
        switch (miDtoUsuario.getTipo()) {
            case ConstantesNegocio.USUARIO_EMPLEADO:
                cargarVentanaEmpleado();
                break;
            case ConstantesNegocio.USUARIO_ESCRIBANO:
                cargarVentanaEscribano();
                break;
        }
    }

    private void cargarVentanaEmpleado() {
        botonModuloAdministracion.setVisible(false);
    }

    private void cargarVentanaEscribano() {
        // Configuración para escribano si es necesario
    }

    static class MenuActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String nombreMenu = (((JMenuItem) e.getSource()).getText());
            for (int i = 0; i < AreaTrabajo.getAllFrames().length; i++) {
                JInternalFrame miFrame = AreaTrabajo.getAllFrames()[i];
                if (miFrame.getTitle().contains(nombreMenu)) {
                    miFrame.show();
                    miFrame.toFront();
                    break;
                }
            }
        }
    }

    public static JInternalFrame obtenerFormularioActivo(String nombreFormulario) {
        for (int i = 0; i < AreaTrabajo.getAllFrames().length; i++) {
            JInternalFrame miFrame = AreaTrabajo.getAllFrames()[i];
            if (miFrame.getTitle().contains(nombreFormulario)) {
                return miFrame;
            }
        }
        return null;
    }

    public static void setVentanasActivas(JMenuItem nuevaVentana) {
        nuevaVentana.addActionListener(new MenuActionListener());
        ventanasActivas.add(nuevaVentana);
    }

    public static void removeVentanaActivas(JMenuItem ventana) {
        ventanasActivas.remove(ventana);
    }

    public static void cargarFormulario(JInternalFrame form) {
        AreaTrabajo.remove(form);
        AreaTrabajo.add(form);
        form.show();
        form.toFront();
    }

    public static void eliminarFormulario(JInternalFrame form) {
        AreaTrabajo.remove(form);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        Modulos = new javax.swing.JPanel();
        botonModuloClientes = new javax.swing.JButton();
        botonModuloPresupuestos = new javax.swing.JButton();
        botonModuloGestiones = new javax.swing.JButton();
        botonModuloProtocolo = new javax.swing.JButton();
        botonSalir = new javax.swing.JButton();
        botonModuloAdministracion = new javax.swing.JButton();
        botonModuloPagos = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        scrollAreaTrabajo = new javax.swing.JScrollPane();
        AreaTrabajo = new javax.swing.JDesktopPane();
        Menu = new javax.swing.JMenuBar();
        menuArchivo = new javax.swing.JMenu();
        menuCerrarSesion = new javax.swing.JMenuItem();
        menuSalir = new javax.swing.JMenuItem();
        menuOpciones = new javax.swing.JMenu();
        ventanasActivas = new javax.swing.JMenu();
        menuAyuda = new javax.swing.JMenu();
        menuManualUsuario = new javax.swing.JMenuItem();
        menuAcercaNotaire = new javax.swing.JMenuItem();
        jMenuseparador = new javax.swing.JMenu();
        itemUsuario = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Notaire");

        botonModuloClientes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/barraLateral/barraClientes.png")));
        botonModuloClientes.setText("Clientes");
        botonModuloClientes.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        botonModuloClientes.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        botonModuloClientes.setIconTextGap(25);
        botonModuloClientes.setMargin(new java.awt.Insets(2, 4, 2, 4));
        botonModuloClientes.setPreferredSize(new java.awt.Dimension(60, 30));
        botonModuloClientes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonModuloClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonModuloClientesActionPerformed(evt);
            }
        });

        botonModuloPresupuestos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/barraLateral/barraPresup.png")));
        botonModuloPresupuestos.setText("Presupuestos");
        botonModuloPresupuestos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        botonModuloPresupuestos.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        botonModuloPresupuestos.setIconTextGap(0);
        botonModuloPresupuestos.setMargin(new java.awt.Insets(2, 4, 2, 0));
        botonModuloPresupuestos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonModuloPresupuestos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonModuloPresupuestosActionPerformed(evt);
            }
        });

        botonModuloGestiones.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/barraLateral/barraGestion.png")));
        botonModuloGestiones.setText("Gestiones");
        botonModuloGestiones.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        botonModuloGestiones.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        botonModuloGestiones.setIconTextGap(18);
        botonModuloGestiones.setMargin(new java.awt.Insets(2, 4, 2, 4));
        botonModuloGestiones.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonModuloGestiones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonModuloGestionesActionPerformed(evt);
            }
        });

        botonModuloProtocolo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/barraLateral/barraProt.png")));
        botonModuloProtocolo.setText("Protocolo");
        botonModuloProtocolo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        botonModuloProtocolo.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        botonModuloProtocolo.setIconTextGap(16);
        botonModuloProtocolo.setMargin(new java.awt.Insets(2, 4, 2, 4));
        botonModuloProtocolo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonModuloProtocolo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonModuloProtocoloActionPerformed(evt);
            }
        });

        botonSalir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/barraLateral/barraSalir.png")));
        botonSalir.setText("Salir");
        botonSalir.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        botonSalir.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        botonSalir.setIconTextGap(10);
        botonSalir.setMargin(new java.awt.Insets(2, 4, 2, 4));
        botonSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonSalirActionPerformed(evt);
            }
        });

        botonModuloAdministracion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/barraLateral/barraAdmin.png")));
        botonModuloAdministracion.setText("Administración");
        botonModuloAdministracion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        botonModuloAdministracion.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        botonModuloAdministracion.setIconTextGap(2);
        botonModuloAdministracion.setMargin(new java.awt.Insets(2, 4, 2, 4));
        botonModuloAdministracion.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonModuloAdministracion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonModuloAdministracionActionPerformed(evt);
            }
        });

        botonModuloPagos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/barraLateral/barraPagos.png")));
        botonModuloPagos.setText("Pagos");
        botonModuloPagos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        botonModuloPagos.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        botonModuloPagos.setIconTextGap(42);
        botonModuloPagos.setMargin(new java.awt.Insets(2, 4, 2, 4));
        botonModuloPagos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonModuloPagos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonModuloPagosActionPerformed(evt);
            }
        });

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/barraLateral/logoPalabraRojoVertical.png")));
        jButton1.setContentAreaFilled(false);

        javax.swing.GroupLayout ModulosLayout = new javax.swing.GroupLayout(Modulos);
        Modulos.setLayout(ModulosLayout);
        ModulosLayout.setHorizontalGroup(
            ModulosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ModulosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ModulosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ModulosLayout.createSequentialGroup()
                        .addGroup(ModulosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(ModulosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(botonModuloGestiones, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(botonModuloClientes, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(botonModuloPresupuestos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(ModulosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(botonSalir, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(botonModuloProtocolo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(botonModuloPagos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(botonModuloAdministracion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ModulosLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        ModulosLayout.setVerticalGroup(
            ModulosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ModulosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(botonModuloClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonModuloPresupuestos, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonModuloGestiones, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonModuloProtocolo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonModuloPagos, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonModuloAdministracion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        AreaTrabajo.setAutoscrolls(true);
        scrollAreaTrabajo.setViewportView(AreaTrabajo);

        menuArchivo.setText("Archivo");
        menuCerrarSesion.setText("Cerrar Sesión");
        menuArchivo.add(menuCerrarSesion);
        menuSalir.setText("Salir");
        menuSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSalirActionPerformed(evt);
            }
        });
        menuArchivo.add(menuSalir);
        Menu.add(menuArchivo);

        menuOpciones.setText("Opciones");
        Menu.add(menuOpciones);

        ventanasActivas.setText("Ventanas");
        Menu.add(ventanasActivas);

        menuAyuda.setText("Ayuda");
        menuManualUsuario.setText("Manual de Usuario");
        menuAyuda.add(menuManualUsuario);
        menuAcercaNotaire.setText("Acerca de Notaire");
        menuAyuda.add(menuAcercaNotaire);
        Menu.add(menuAyuda);

        jMenuseparador.setText("                                                                                                                                                 ");
        jMenuseparador.setEnabled(false);
        jMenuseparador.setMaximumSize(new java.awt.Dimension(1000, 32767));
        jMenuseparador.setPreferredSize(new java.awt.Dimension(750, 23));
        Menu.add(jMenuseparador);

        itemUsuario.setForeground(new java.awt.Color(23, 56, 229));
        itemUsuario.setFocusable(false);
        itemUsuario.setFont(new java.awt.Font("SansSerif", 1, 12));
        itemUsuario.setPreferredSize(new java.awt.Dimension(250, 23));
        Menu.add(itemUsuario);

        setJMenuBar(Menu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(Modulos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollAreaTrabajo, javax.swing.GroupLayout.DEFAULT_SIZE, 1110, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Modulos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(scrollAreaTrabajo)
        );

        pack();
    }

    private void botonModuloAdministracionActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Módulo en construcción", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void botonSalirActionPerformed(java.awt.event.ActionEvent evt) {
        salir();
    }

    private void botonModuloClientesActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Módulo en construcción", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void botonModuloPresupuestosActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Módulo en construcción", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void botonModuloGestionesActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Módulo en construcción", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void botonModuloProtocoloActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Módulo en construcción", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void botonModuloPagosActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Módulo en construcción", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void menuSalirActionPerformed(java.awt.event.ActionEvent evt) {
        salir();
    }

    private void salir() {
        int n = JOptionPane.showConfirmDialog(this, "¿Desea Salir del Sistema?", "Sesion", JOptionPane.YES_NO_OPTION);
        if (n == 0) {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            System.exit(0);
        }
    }

    // Variables declaration
    public static javax.swing.JDesktopPane AreaTrabajo;
    private javax.swing.JMenuBar Menu;
    private javax.swing.JPanel Modulos;
    private javax.swing.JButton botonModuloAdministracion;
    private javax.swing.JButton botonModuloClientes;
    private javax.swing.JButton botonModuloGestiones;
    private javax.swing.JButton botonModuloPagos;
    private javax.swing.JButton botonModuloPresupuestos;
    private javax.swing.JButton botonModuloProtocolo;
    private javax.swing.JButton botonSalir;
    public javax.swing.JMenu itemUsuario;
    private javax.swing.JButton jButton1;
    private javax.swing.JMenu jMenuseparador;
    private javax.swing.JMenuItem menuAcercaNotaire;
    private javax.swing.JMenu menuArchivo;
    private javax.swing.JMenu menuAyuda;
    private javax.swing.JMenuItem menuCerrarSesion;
    private javax.swing.JMenuItem menuManualUsuario;
    private javax.swing.JMenu menuOpciones;
    private javax.swing.JMenuItem menuSalir;
    private javax.swing.JScrollPane scrollAreaTrabajo;
    public static javax.swing.JMenu ventanasActivas;
}

