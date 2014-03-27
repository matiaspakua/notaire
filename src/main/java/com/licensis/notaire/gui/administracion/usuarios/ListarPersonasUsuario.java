/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.administracion.usuarios;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.administracion.escribanos.DarAltaEscribano;
import com.licensis.notaire.gui.clientes.BuscarCliente;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.negocio.ControllerNegocio;

/**
 *
 * @author juanca
 */
public class ListarPersonasUsuario extends javax.swing.JInternalFrame
{

    private static ListarPersonasUsuario instancia = null;
    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaListadoPersonas = new JMenuItem("Ventana Listado Personas");
    private String formulario = new String();
    private ArrayList<DtoPersona> miListaPersonas = null;

    /**
     * Creates new form ListarPersonasUsuario
     */
    private ListarPersonasUsuario() {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioNormalVertical);
        grillaPersonas.setAutoCreateRowSorter(true);
    }

    public static ListarPersonasUsuario getInstancia() {
        if (instancia == null)
        {
            instancia = new ListarPersonasUsuario();
        }
        return instancia;
    }

    public static JMenuItem getVentanaListadoPersonas() {
        return ventanaListadoPersonas;
    }

    private void salir() {
        this.dispose();
    }

    public Boolean cargarGrillaPersonasClientes(ArrayList<DtoPersona> miListaDtoPersonas) {
        Boolean flag = false;

        flag = cargarPersonasClientes(miListaDtoPersonas);
        //Principal.cargarFormulario(ListarPersonasUsuario.getInstancia());
        Principal.setVentanasActivas(ventanaListadoPersonas);

        return flag;
    }

    public Boolean cargarPersonasEscribano(ArrayList<DtoPersona> miListaDtoPersonas, String tipoFormulario) {
        boolean flag = false;

        miListaDtoPersonas = miListaDtoPersonas;

        formulario = tipoFormulario;

        Principal.cargarFormulario(ListarPersonasUsuario.getInstancia());
        Principal.setVentanasActivas(ventanaListadoPersonas);

        this.labelTitulo.setText("Listado Personas - Escribanos");

        if (miListaDtoPersonas != null)
        {
            flag = true;

            DtoPersona miDtoPersona = null;

            for (int i = 0; i < miListaDtoPersonas.size(); i++)
            {

                miDtoPersona = miListaDtoPersonas.get(i);

                Object[] datos =
                {
                    miDtoPersona.getNombre(),
                    miDtoPersona.getApellido(),
                    miDtoPersona.getDtoTipoIdentificacion().getNombre(),
                    miDtoPersona.getNumeroIdentificacion(),
                    miDtoPersona.getEsCliente()
                };

                ((DefaultTableModel) grillaPersonas.getModel()).addRow(datos);
            }
            BuscarCliente.getInstancia().dispose();

        }
        else
        {
            JOptionPane.showMessageDialog(this, "No existen Coincidencias en la Busqueda");
            this.dispose();

        }
        return flag;
    }

    public boolean cargarPersonasClientes(ArrayList<DtoPersona> miListaDtoPersonas) {
        Boolean flag = false;

        this.labelTitulo.setText("ListadoPersonas - Clientes");


        if (!miListaDtoPersonas.isEmpty())
        {
            flag = true;
            DtoPersona miDtoPersona = null;

            for (int i = 0; i < miListaDtoPersonas.size(); i++)
            {
                miDtoPersona = miListaDtoPersonas.get(i);

                Object[] datos =
                {
                    miDtoPersona.getNombre(),
                    miDtoPersona.getApellido(),
                    miDtoPersona.getDtoTipoIdentificacion().getNombre(),
                    miDtoPersona.getNumeroIdentificacion(),
                    miDtoPersona.getEsCliente()
                };

                ((DefaultTableModel) grillaPersonas.getModel()).addRow(datos);
            }
            BuscarCliente.getInstancia().dispose();

        }
        if (!flag)
        {
            JOptionPane.showMessageDialog(this, "No existen Coincidencias en la Busqueda de Personas");
            this.salir();

        }
        else
        {
            Principal.cargarFormulario(ListarPersonasUsuario.getInstancia());
            Principal.setVentanasActivas(ListarPersonasUsuario.getVentanaListadoPersonas());

        }
        return flag;
    }

    public void limpiarJtable() {
        int i = ((DefaultTableModel) grillaPersonas.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaPersonas.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaPersonas.getModel()).removeRow(i);
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

        panelListaPersonas = new javax.swing.JPanel();
        labelTitulo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaPersonas = new javax.swing.JTable();
        botonCancelar = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Listado Personas");
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
        labelTitulo.setText("Lista Personas");

        grillaPersonas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nombre", "Apellido", "Identificación", "Número", "Cliente"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        grillaPersonas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grillaPersonasMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(grillaPersonas);

        botonCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/src/main/resources/iconos/cerrar.png"))); // NOI18N
        botonCancelar.setText("Cerrar");
        botonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelListaPersonasLayout = new javax.swing.GroupLayout(panelListaPersonas);
        panelListaPersonas.setLayout(panelListaPersonasLayout);
        panelListaPersonasLayout.setHorizontalGroup(
            panelListaPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelListaPersonasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelListaPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 669, Short.MAX_VALUE)
                    .addGroup(panelListaPersonasLayout.createSequentialGroup()
                        .addComponent(labelTitulo)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelListaPersonasLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        panelListaPersonasLayout.setVerticalGroup(
            panelListaPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelListaPersonasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelListaPersonas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelListaPersonas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCancelarActionPerformed
        this.limpiarJtable();
        this.dispose();
    }//GEN-LAST:event_botonCancelarActionPerformed

    @Override
    public void dispose() {
        super.dispose();
    }

    private void grillaPersonasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grillaPersonasMouseClicked

        Integer filaSeleccionada = grillaPersonas.getSelectedRow();
        TableModel miGrilla = grillaPersonas.getModel();

        int filas = miGrilla.getRowCount();
        int columnas = miGrilla.getColumnCount();

        DtoPersona miDtoPersona = new DtoPersona();
        DtoTipoIdentificacion dtoTipoIdentificacion = new DtoTipoIdentificacion();


        //Recorro la grilla completa, buscando el cliente seleccionado
        for (int i = 0; i < filas; i++)
        {
            if (i == filaSeleccionada)
            {
                dtoTipoIdentificacion.setNombre(miGrilla.getValueAt(i, 2).toString());
                miDtoPersona.setDtoTipoIdentificacion(dtoTipoIdentificacion);
                miDtoPersona.setNumeroIdentificacion(miGrilla.getValueAt(i, 3).toString());

                //Asocio el nombre tipo Identificacion con su id para buscar la persona
                dtoTipoIdentificacion.setIdTipoIdentificacion(ControllerNegocio.getInstancia().asociarFkTipoIdentificacion(miDtoPersona));

                //Busco todos los datos de la persona seleccionada y cargo  el formulario
                miDtoPersona = ControllerNegocio.getInstancia().buscarPersonaTipoNumeroIdentificacion(miDtoPersona);

                if (formulario.contains(ConstantesGui.DAR_ALTA_ESCRIBANO))
                {
                    if (miDtoPersona.getRegistroEscribano() != null)
                    {
                        JOptionPane.showMessageDialog(this, "La persona seleccionada ya se encuentra registradas como 'Escribano'", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        this.salir();
                    }
                    else
                    {
                        DarAltaEscribano form = new DarAltaEscribano();
                        miDtoPersona.setRegistroEscribano(0);
                        form.cargarEscribano(miDtoPersona, ConstantesGui.DAR_ALTA_ESCRIBANO);
                        
                        Principal.cargarFormulario(form);
                        Principal.setVentanasActivas(DarAltaEscribano.getVentanaDarAltaEscribano());
                    }
                }
                else if (formulario.contains(ConstantesGui.MODIFICAR_ESCRIBANO))
                {
                    if (miDtoPersona.getRegistroEscribano() == null)
                    {
                        JOptionPane.showMessageDialog(this, "La personas seleccionada no esta registrada como Escribano", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        this.salir();
                    }
                    else
                    {
                        DarAltaEscribano form = new DarAltaEscribano();
                        form.cargarEscribano(miDtoPersona, ConstantesGui.MODIFICAR_ESCRIBANO);
                        
                        Principal.cargarFormulario(form);
                        Principal.setVentanasActivas(DarAltaEscribano.getVentanaDarAltaEscribano());
                    }
                }

                try
                {
                    DarAltaUsuario.getInstancia().cargarFormulario(miDtoPersona);
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(this, "Error grave, Accion cancelada", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(ListarPersonasUsuario.class.getName()).log(Level.SEVERE, null, ex);
                    this.salir();                    
                }

                this.limpiarJtable();
                this.salir();
            }
        }

    }//GEN-LAST:event_grillaPersonasMouseClicked

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaListadoPersonas);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCancelar;
    private javax.swing.JTable grillaPersonas;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelTitulo;
    private javax.swing.JPanel panelListaPersonas;
    // End of variables declaration//GEN-END:variables
}
