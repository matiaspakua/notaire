/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.presupuestos;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.pagos.ConsultarPagos;
import com.licensis.notaire.gui.pagos.RegistrarPago;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorReportes;

/**
 *
 * @author Tefi
 */
public class ListaPresupuestosCliente extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaListaPresupuestosCliente = new JMenuItem("Ventana Lista Presupuestos Cliente");
    private String formularioInvocador = null;
    private DtoPersona personaSeleccionada = null;
    private DtoPresupuesto presupuesto = null;
    private ArrayList<DtoPresupuesto> listaPresupuestos = new ArrayList<>();
    private ControllerNegocio miController = ControllerNegocio.getInstancia();
    private ModificarPresupuesto modificarPresupuesto = null;
    private Map parameters = new HashMap();

    /**
     * Creates new form ListaPresupuestosCliente
     */
    public ListaPresupuestosCliente() {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioNormalVertical);
        grillaPresupuestosCliente.setAutoCreateRowSorter(true);
    }

    public void setFormModificarPresupuesto(ModificarPresupuesto formulario) {
        modificarPresupuesto = formulario;
    }

    private void extraerPresupuestoSeleccionado() throws NumberFormatException {
        TableModel miGrilla = grillaPresupuestosCliente.getModel();
        int filaSeleccionada = grillaPresupuestosCliente.getSelectedRow();
        filaSeleccionada = grillaPresupuestosCliente.convertRowIndexToModel(filaSeleccionada);

        int filas = miGrilla.getRowCount();

        for (int i = 0; i < filas; i++)
        {
            if (i == filaSeleccionada)
            {
                for (int j = 0; j < getListaPresupuestos().size(); j++)
                {
                    DtoPresupuesto dtoPresupuesto = getListaPresupuestos().get(j);

                    if (dtoPresupuesto.getIdPresupuesto() == (Integer) miGrilla.getValueAt(i, 2))
                    {

                        this.presupuesto = dtoPresupuesto;
                    }
                }

                break;
            }
        }
    }

    private void salir() {
        this.dispose();
    }

    public void limpiarGrilla() {
        int i = ((DefaultTableModel) grillaPresupuestosCliente.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaPresupuestosCliente.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaPresupuestosCliente.getModel()).removeRow(i);
            i--;
        }
    }

    public static JMenuItem getVentanaListaPresupuestosCliente() {
        return ventanaListaPresupuestosCliente;
    }

    public void setFormulario(String formulario) {
        formularioInvocador = formulario;
    }

    public void setPersona(DtoPersona dtoPersona) {
        personaSeleccionada = dtoPersona;
    }

    public void cargarGrillaPresupuestos() throws NonexistentJpaException {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        this.setListaPresupuestos(miController.buscarPresupuestosPersona(personaSeleccionada));

        if ((this.getListaPresupuestos() != null) && (!this.getListaPresupuestos().isEmpty()))
        {
            DtoPresupuesto miDto = null;

            for (int i = 0; i < getListaPresupuestos().size(); i++)
            {

                miDto = getListaPresupuestos().get(i);

                if (miDto.getTramite().getGestion() != null)
                {
                    Object[] datos =
                    {
                        miDto.getTramite().getGestion().getNumero(),
                        miDto.getTramite().getGestion().getEncabezado(),
                        miDto.getIdPresupuesto(),
                        miDto.getTramite().getTipoDeTramite().getNombre(),
                        formatter.format(miDto.getFecha()).toString(),
                        miDto.getTotal(),
                        miDto.getSaldo(),
                        miDto.getObservaciones(),
                        false
                    };
                    ((DefaultTableModel) grillaPresupuestosCliente.getModel()).addRow(datos);
                }
                else
                {
                    Object[] datos =
                    {
                        "",
                        "",
                        miDto.getIdPresupuesto(),
                        miDto.getTramite().getTipoDeTramite().getNombre(),
                        formatter.format(miDto.getFecha()).toString(),
                        miDto.getTotal(),
                        miDto.getSaldo(),
                        miDto.getObservaciones(),
                        false
                    };
                    ((DefaultTableModel) grillaPresupuestosCliente.getModel()).addRow(datos);
                }
            }
        }
        else
        {

            JOptionPane.showMessageDialog(this, "No existen presupuestos asociados a la persona indicada.", "Error", JOptionPane.ERROR_MESSAGE);
            this.dispose();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelListaPresupuestosCliente = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaPresupuestosCliente = new javax.swing.JTable();
        botonCerrar = new javax.swing.JButton();

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

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Lista Presupuestos Cliente");

        grillaPresupuestosCliente.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Número de Gestión", "Encabezado", "Número de Presupuesto", "Trámite", "Fecha de Emisión", "Total", "Saldo", "Observaciones"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Float.class, java.lang.Float.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        grillaPresupuestosCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grillaPresupuestosClienteMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(grillaPresupuestosCliente);

        botonCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar.png"))); // NOI18N
        botonCerrar.setText("Cerrar");
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelListaPresupuestosClienteLayout = new javax.swing.GroupLayout(panelListaPresupuestosCliente);
        panelListaPresupuestosCliente.setLayout(panelListaPresupuestosClienteLayout);
        panelListaPresupuestosClienteLayout.setHorizontalGroup(
            panelListaPresupuestosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelListaPresupuestosClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelListaPresupuestosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelListaPresupuestosClienteLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 710, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelListaPresupuestosClienteLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonCerrar)))
                .addContainerGap())
        );
        panelListaPresupuestosClienteLayout.setVerticalGroup(
            panelListaPresupuestosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelListaPresupuestosClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelListaPresupuestosCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelListaPresupuestosCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCerrarActionPerformed
    {//GEN-HEADEREND:event_botonCerrarActionPerformed
        salir();
    }//GEN-LAST:event_botonCerrarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaListaPresupuestosCliente);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void grillaPresupuestosClienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grillaPresupuestosClienteMouseClicked
        // TODO add your handling code here:        

        switch (formularioInvocador)
        {
            case ConstantesGui.MODIFICAR_PRESUPUESTO:
            {
                this.extraerPresupuestoSeleccionado();

                modificarPresupuesto = new ModificarPresupuesto();

                modificarPresupuesto.setPersona(personaSeleccionada);
                modificarPresupuesto.setPresupuesto(presupuesto);
                modificarPresupuesto.cargarFormulario();

                Principal.cargarFormulario(modificarPresupuesto);
                salir();
                break;
            }
            case "Registrar Pago":
            {
                Principal.cargarFormulario(new RegistrarPago());
                Principal.setVentanasActivas(RegistrarPago.getVentanaRegistrarPagos());

                break;
            }
            case "Consultar Pagos":
            {
                Principal.cargarFormulario(new ConsultarPagos());
                Principal.setVentanasActivas(ConsultarPagos.getVentanaConsultarPagos());
                break;
            }
            case "Modificar Gestion":
            {
                salir();
                break;
            }
            case ConstantesGui.BUSCAR_PRESUPUESTO:
            {
                this.extraerPresupuestoSeleccionado();

                AdministradorReportes reportes = AdministradorReportes.getInstancia();
                reportes.generarReportePresupuesto(presupuesto);
                break;
            }
        }
    }//GEN-LAST:event_grillaPresupuestosClienteMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCerrar;
    private javax.swing.JTable grillaPresupuestosCliente;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelListaPresupuestosCliente;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the listaPresupuestos
     */
    public ArrayList<DtoPresupuesto> getListaPresupuestos() {
        return listaPresupuestos;
    }

    /**
     * @param listaPresupuestos the listaPresupuestos to set
     */
    public void setListaPresupuestos(ArrayList<DtoPresupuesto> listaPresupuestos) {
        this.listaPresupuestos = listaPresupuestos;
    }
}
