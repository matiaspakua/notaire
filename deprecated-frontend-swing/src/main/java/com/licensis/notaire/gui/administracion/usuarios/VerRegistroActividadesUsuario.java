/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.administracion.usuarios;

import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.dto.GenericDto;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.servicios.AdministradorJpa;
import com.licensis.notaire.servicios.GenericRestClient;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;

/**
 *
 * @author matias
 */
public class VerRegistroActividadesUsuario extends javax.swing.JInternalFrame
{

    private static final Logger LOGGER = Logger.getLogger(VerRegistroActividadesUsuario.class.getName());
    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaRegistroActividadesUsuario = new JMenuItem("Ventana Registro de Actividades de Usuario");
    private static VerRegistroActividadesUsuario instancia = null;
    private DtoUsuario dtoUsuario = null;
    private final GenericRestClient usuarioClient;
    private final GenericRestClient registroAuditoriaClient;

    /**
     * Creates new form VerRegistroActividadesUsuario
     */
    public VerRegistroActividadesUsuario()
    {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioGrandeHorizontal, Principal.tamanioGrandeVertical);
        AdministradorJpa admin = AdministradorJpa.getInstancia();
        usuarioClient = admin.getUsuarioJpa();
        registroAuditoriaClient = admin.getRegistroAuditoriaJpa();
        grillaDetalleActividades.setAutoCreateRowSorter(true);
    }

    public static VerRegistroActividadesUsuario getInstancia()
    {
        if (instancia == null)
        {
            instancia = new VerRegistroActividadesUsuario();
        }
        return instancia;
    }

    private void salir()
    {
        this.dispose();
    }

    public DtoUsuario getDtoUsuario()
    {
        return dtoUsuario;
    }

    public void setDtoUsuario(DtoUsuario dtoUsuario)
    {
        this.dtoUsuario = dtoUsuario;
    }

    public static JMenuItem getVentanaRegistroActividadesUsuario()
    {
        return ventanaRegistroActividadesUsuario;
    }

    public void limpiarFormulario()
    {

        this.limpiarJtable();
    }

    ;

      public void limpiarJtable()
    {
        int i = ((DefaultTableModel) grillaDetalleActividades.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaDetalleActividades.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaDetalleActividades.getModel()).removeRow(i);
            i--;
        }

    }

    public void cargarRegistrosUsuario(DtoUsuario miDtoUsuario) throws NonexistentJpaException
    {
        limpiarFormulario();
        this.setDtoUsuario(miDtoUsuario);
        if (dtoUsuario == null || dtoUsuario.getIdUsuario() == null) {
            JOptionPane.showMessageDialog(this, "Usuario no seleccionado.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final Integer idUsuario = dtoUsuario.getIdUsuario();
        labelPersona.setText("...");
        labelUsuario.setText("...");
        fechaDesde.setDate(new Date());
        fechaHasta.setDate(new Date());

        new SwingWorker<GenericDto, Void>() {
            @Override
            protected GenericDto doInBackground() throws IOException {
                return usuarioClient.find(idUsuario);
            }

            @Override
            protected void done() {
                try {
                    GenericDto usuario = get();
                    if (usuario == null) {
                        JOptionPane.showMessageDialog(VerRegistroActividadesUsuario.this,
                                "Usuario no encontrado.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    labelUsuario.setText(usuario.getString("nombre"));
                    Object personaObj = usuario.get("fkIdPersona");
                    String nombrePersona = "";
                    String apellidoPersona = "";
                    if (personaObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> p = (Map<String, Object>) personaObj;
                        nombrePersona = p.get("nombre") != null ? p.get("nombre").toString() : "";
                        apellidoPersona = p.get("apellido") != null ? p.get("apellido").toString() : "";
                    }
                    labelPersona.setText(nombrePersona + ", " + apellidoPersona);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error cargando usuario", e);
                    JOptionPane.showMessageDialog(VerRegistroActividadesUsuario.this,
                            "Error al cargar usuario: " + (e.getMessage() != null ? e.getMessage() : "desconocido"),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                Principal.cargarFormulario(VerRegistroActividadesUsuario.getInstancia());
                Principal.setVentanasActivas(ventanaRegistroActividadesUsuario);
            }
        }.execute();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelVerRegistroActividadesUsuario = new javax.swing.JPanel();
        labelTitulo = new javax.swing.JLabel();
        botonCerrar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        labelPersona = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaDetalleActividades = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        labelUsuario = new javax.swing.JLabel();
        fechaHasta = new com.toedter.calendar.JDateChooser();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        fechaDesde = new com.toedter.calendar.JDateChooser();
        botonBuscar = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Registro de Actividades de Usuario");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
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
        labelTitulo.setText("Registro de Actividades de Usuario :");

        botonCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar.png"))); // NOI18N
        botonCerrar.setText("Cerrar");
        botonCerrar.setIconTextGap(10);
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });

        jLabel2.setText("Nombre y Apellido:");

        labelPersona.setText("Jose Perez");
        labelPersona.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        grillaDetalleActividades.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Fecha", "Modulo", "Detalle Operacion"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        grillaDetalleActividades.setEnabled(false);
        grillaDetalleActividades.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        grillaDetalleActividades.getTableHeader().setReorderingAllowed(false);
        grillaDetalleActividades.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grillaDetalleActividadesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(grillaDetalleActividades);
        grillaDetalleActividades.getColumnModel().getColumn(0).setPreferredWidth(150);
        grillaDetalleActividades.getColumnModel().getColumn(1).setPreferredWidth(150);
        grillaDetalleActividades.getColumnModel().getColumn(2).setPreferredWidth(300);

        jLabel4.setText("Detalle de actividades del usuario:");

        labelUsuario.setFont(new java.awt.Font("Cantarell", 3, 18)); // NOI18N
        labelUsuario.setForeground(java.awt.Color.blue);
        labelUsuario.setText("Usuario");
        labelUsuario.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Hasta");

        jLabel3.setText("Desde");

        botonBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/buscar.png"))); // NOI18N
        botonBuscar.setText("Buscar");
        botonBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelVerRegistroActividadesUsuarioLayout = new javax.swing.GroupLayout(panelVerRegistroActividadesUsuario);
        panelVerRegistroActividadesUsuario.setLayout(panelVerRegistroActividadesUsuarioLayout);
        panelVerRegistroActividadesUsuarioLayout.setHorizontalGroup(
            panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVerRegistroActividadesUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelVerRegistroActividadesUsuarioLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelVerRegistroActividadesUsuarioLayout.createSequentialGroup()
                        .addGroup(panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelVerRegistroActividadesUsuarioLayout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(fechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel1)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(fechaHasta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(botonBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelVerRegistroActividadesUsuarioLayout.createSequentialGroup()
                                    .addGroup(panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelVerRegistroActividadesUsuarioLayout.createSequentialGroup()
                                            .addComponent(jLabel2)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(labelPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(labelTitulo))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(labelUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 321, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelVerRegistroActividadesUsuarioLayout.setVerticalGroup(
            panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVerRegistroActividadesUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTitulo)
                    .addComponent(labelUsuario))
                .addGap(10, 10, 10)
                .addGroup(panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(labelPersona))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fechaHasta, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelVerRegistroActividadesUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(fechaDesde, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(botonBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelVerRegistroActividadesUsuario, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelVerRegistroActividadesUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaRegistroActividadesUsuario);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        // TODO add your handling code here:
    }//GEN-LAST:event_formInternalFrameClosing

    private void botonBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuscarActionPerformed
        final Date fechaDesdeVal = this.fechaDesde.getDate();
        final Date fechaHastaVal = this.fechaHasta.getDate();
        if (dtoUsuario == null || dtoUsuario.getIdUsuario() == null) {
            JOptionPane.showMessageDialog(this, "Usuario no seleccionado.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        grillaDetalleActividades.setEnabled(true);
        this.limpiarFormulario();

        final Integer idUsuario = dtoUsuario.getIdUsuario();
        new SwingWorker<List<GenericDto>, Void>() {
            @Override
            protected List<GenericDto> doInBackground() throws IOException {
                return registroAuditoriaClient.findAllByPath("usuario/" + idUsuario);
            }

            @Override
            protected void done() {
                try {
                    List<GenericDto> list = get();
                    if (list == null || list.isEmpty()) {
                        JOptionPane.showMessageDialog(VerRegistroActividadesUsuario.this,
                                "No existen actividades registradas por el Usuario", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    DefaultTableModel model = (DefaultTableModel) grillaDetalleActividades.getModel();
                    int count = 0;
                    for (GenericDto reg : list) {
                        Date fecha = parseFecha(reg);
                        if (fecha != null && fechaDesdeVal != null && fechaHastaVal != null
                                && !fecha.before(fechaDesdeVal) && !fecha.after(fechaHastaVal)) {
                            count++;
                            model.addRow(new Object[]{
                                    fecha,
                                    reg.getString("modulo"),
                                    reg.getString("detalleOperacion")
                            });
                        }
                    }
                    if (count == 0) {
                        JOptionPane.showMessageDialog(VerRegistroActividadesUsuario.this,
                                "No existen actividades en el rango de fechas seleccionado.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error buscando registros auditoría", e);
                    JOptionPane.showMessageDialog(VerRegistroActividadesUsuario.this,
                            "Error al buscar actividades: " + (e.getMessage() != null ? e.getMessage() : "desconocido"),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }//GEN-LAST:event_botonBuscarActionPerformed

    private static Date parseFecha(GenericDto reg) {
        Object val = reg.get("fecha");
        if (val == null) return null;
        if (val instanceof Date) return (Date) val;
        if (val instanceof Number) return new Date(((Number) val).longValue());
        if (val instanceof String) {
            try {
                return new Date(Long.parseLong(val.toString()));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void grillaDetalleActividadesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grillaDetalleActividadesMouseClicked
    }//GEN-LAST:event_grillaDetalleActividadesMouseClicked

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCerrarActionPerformed
        this.salir();
    }//GEN-LAST:event_botonCerrarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonBuscar;
    private javax.swing.JButton botonCerrar;
    private com.toedter.calendar.JDateChooser fechaDesde;
    private com.toedter.calendar.JDateChooser fechaHasta;
    private javax.swing.JTable grillaDetalleActividades;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelPersona;
    private javax.swing.JLabel labelTitulo;
    private javax.swing.JLabel labelUsuario;
    private javax.swing.JPanel panelVerRegistroActividadesUsuario;
    // End of variables declaration//GEN-END:variables
}
