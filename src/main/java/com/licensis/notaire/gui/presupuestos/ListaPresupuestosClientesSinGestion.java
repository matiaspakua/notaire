/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.presupuestos;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.gestiones.gestion.IniciarGestion;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.negocio.ControllerNegocio;

/**
 *
 * @author matias
 */
public class ListaPresupuestosClientesSinGestion extends javax.swing.JInternalFrame
{

    private static JMenuItem ventanaListaPresupuestoClienteSinGestion = new JMenuItem("Ventana Lista Presupuestos Cliente");
    private String formularioInvocador;
    private DtoPersona clienteSeleccionado;
    private List<DtoPresupuesto> listaPresupuestosCargados = new ArrayList<>();
    private ControllerNegocio miController;

    /**
     * Creates new form ListaPresupuestosClientesSinGestion
     */
    public ListaPresupuestosClientesSinGestion() {
        initComponents();
        this.setSize(Principal.tamanioGrandeHorizontal, Principal.tamanioGrandeVertical);
        miController = ControllerNegocio.getInstancia();
    }

    public static JMenuItem getVentanaListaPresupuestoClienteSinGestion() {
        return ventanaListaPresupuestoClienteSinGestion;
    }

    public static void setVentanaListaPresupuestoClienteSinGestion(JMenuItem ventanaListaPresupuestoClienteSinGestion) {
        ListaPresupuestosClientesSinGestion.ventanaListaPresupuestoClienteSinGestion = ventanaListaPresupuestoClienteSinGestion;
    }

    public String getFormularioInvocador() {
        return formularioInvocador;
    }

    public void setFormularioInvocador(String formularioInvocador) {
        this.formularioInvocador = formularioInvocador;
    }

    public DtoPersona getClienteSeleccionado() {
        return clienteSeleccionado;
    }

    public void setClienteSeleccionado(DtoPersona clienteSeleccionado) {
        this.clienteSeleccionado = clienteSeleccionado;
        this.cargarPresupuestosCliente();

    }

    public List<DtoPresupuesto> getListaPresupuestosCargados() {
        return listaPresupuestosCargados;
    }

    public void setListaPresupuestosCargados(List<DtoPresupuesto> listaPresupuestosSeleccionados) {
        this.listaPresupuestosCargados = listaPresupuestosSeleccionados;
    }

    public void salir() {
        this.dispose();
    }

    public void limpiarGrilla() {
        int i = ((DefaultTableModel) grillaPresupuesto.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaPresupuesto.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaPresupuesto.getModel()).removeRow(i);
            i--;
        }
    }

    public void cargarPresupuestosCliente() {

        if (this.getClienteSeleccionado() != null)
        {
            try
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                this.setListaPresupuestosCargados(miController.buscarPresupuestosPersona(this.getClienteSeleccionado()));


                if ((this.getListaPresupuestosCargados() != null) && (!this.getListaPresupuestosCargados().isEmpty()))
                {
                    DtoPresupuesto miDto = null;

                    for (int i = 0; i < this.getListaPresupuestosCargados().size(); i++)
                    {

                        miDto = this.getListaPresupuestosCargados().get(i);

                        Object[] datos =
                        {
                            miDto.getIdPresupuesto().toString(),
                            miDto.getTramite().getTipoDeTramite().getNombre(),
                            formatter.format(miDto.getFecha()),
                            miDto.getTotal().toString(),
                            miDto.getObservaciones(),
                            false
                        };
                        ((DefaultTableModel) grillaPresupuesto.getModel()).addRow(datos);

                    }
                }
                else
                {

                    JOptionPane.showMessageDialog(this, "No existen presupuestos asociados a la persona indicada.", "Error", JOptionPane.ERROR_MESSAGE);
                    this.dispose();
                }
            }
            catch (NonexistentJpaException ex)
            {
                JOptionPane.showMessageDialog(this, "Error al buscar los presupuestos del cliente seleccionado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public List<DtoPresupuesto> obtenerListaPresupuestosSeleccionados() {
        DefaultTableModel miGrilla = (DefaultTableModel) grillaPresupuesto.getModel();
        List<DtoPresupuesto> listaDtoPresupuestosSeleccionados = new ArrayList<>();
        int i = 0;
        for (Iterator<DtoPresupuesto> it = this.getListaPresupuestosCargados().iterator(); it.hasNext();)
        {
            DtoPresupuesto dtoPresupuesto = it.next();

            boolean seleccionado = (boolean) miGrilla.getValueAt(i, 5);

            if (seleccionado == true)
            {
                listaDtoPresupuestosSeleccionados.add(dtoPresupuesto);
            }
            i++;
        }
        return listaDtoPresupuestosSeleccionados;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        labelTitulo = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        grillaPresupuesto = new javax.swing.JTable();
        botonCancelar = new javax.swing.JButton();
        botonSeleccionar = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Lista Presupuestos Cliente");
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
        labelTitulo.setText("Lista de presupuesto de cliente");

        grillaPresupuesto.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Numero", "Tramite", "Fecha", "Total", "Observaciones", "Seleccionado"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, false, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(grillaPresupuesto);

        botonCancelar.setText("Cancelar");
        botonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCancelarActionPerformed(evt);
            }
        });

        botonSeleccionar.setText("Seleccionar");
        botonSeleccionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonSeleccionarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelTitulo)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 843, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonSeleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonSeleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 858, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed
        Principal.removeVentanaActivas(ventanaListaPresupuestoClienteSinGestion);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCancelarActionPerformed
        this.salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void botonSeleccionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonSeleccionarActionPerformed

        List<DtoPresupuesto> listaDtoPresupuestosSeleccionados = this.obtenerListaPresupuestosSeleccionados();

        if (!listaDtoPresupuestosSeleccionados.isEmpty())
        {
            IniciarGestion miForm = (IniciarGestion) Principal.obtenerFormularioActivo(ConstantesGui.INICIAR_GESTION);

            for (Iterator<DtoPresupuesto> it = listaDtoPresupuestosSeleccionados.iterator(); it.hasNext();)
            {
                DtoPresupuesto dtoPresupuesto = it.next();

                if (dtoPresupuesto.getTramite().getGestion() != null)
                {
                    JOptionPane.showMessageDialog(this, "El presupuesto: " + dtoPresupuesto.getIdPresupuesto() + ", ya se encuentra asociado a una gestion.", "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                else
                {
                    miForm.setPresupuestoSeleccionado(dtoPresupuesto);
                }
            }
            miForm.cargarGrillaTramitesPresupuesto();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No se ha seleccionado ningun presupuesto", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
        this.salir();

    }//GEN-LAST:event_botonSeleccionarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCancelar;
    private javax.swing.JButton botonSeleccionar;
    private javax.swing.JTable grillaPresupuesto;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelTitulo;
    // End of variables declaration//GEN-END:variables
}
