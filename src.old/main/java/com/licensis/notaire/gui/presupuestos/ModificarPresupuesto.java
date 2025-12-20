/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.presupuestos;

import com.licensis.notaire.dto.DtoInmueble;
import com.licensis.notaire.dto.DtoItem;
import com.licensis.notaire.dto.DtoPago;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.negocio.ControllerNegocio;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * <p>
 * REGLA DE NEGOCIO:
 * <p>
 *
 * <lo> <li> Si queda un saldo a favor del Cliente, al recalcular el total del Presupuesto, se crea
 * un pago con valor negativo para que quede bien el saldo restante. </li> </lo>
 *
 *
 * @author Tefi
 */
public class ModificarPresupuesto extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaModificarPresupuesto = new JMenuItem("Ventana Modificar Presupuestos");
    private DtoPersona miDtoPersona = null;
    private DtoPresupuesto miDtoPresupuesto = null;
    private ArrayList<DtoItem> listaItems = null;
    private ControllerNegocio miController = null;
    private Integer cantidadFilasGrilla = 0;
    private Integer cantidadFilasNuevasGrilla = 0;
    private Float total = 0f;
    private Float totalNuevo = 0f;
    private Float saldo = 0f;

    /**
     * Creates new form ModificarPresupuesto
     */
    public ModificarPresupuesto()
    {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioGrandeVertical);
        miController = ControllerNegocio.getInstancia();

        botonDetalleInmueble.setEnabled(false);
        campoObservaciones.setEnabled(false);
        botonAceptar.setEnabled(false);
        botonAgregarItem.setEnabled(false);
        botonEliminarItem.setEnabled(false);
        botonCalcular.setEnabled(false);

    }

    public void setPersona(DtoPersona persona)
    {
        miDtoPersona = persona;
    }

    public void setPresupuesto(DtoPresupuesto dto)
    {
        miDtoPresupuesto = dto;
    }

    private void salir()
    {
        this.dispose();
    }

    public static JMenuItem getVentanaModificarPresupuesto()
    {
        return ventanaModificarPresupuesto;
    }

    public void cargarFormulario()
    {

        campoNombrePersona.setText(miDtoPersona.getNombre() + " " + miDtoPersona.getApellido());
        campoObservaciones.setText(miDtoPresupuesto.getObservaciones());
        campoNombreTramite.setText(miDtoPresupuesto.getTramite().getTipoDeTramite().getNombre());
        campoTotal.setText(miDtoPresupuesto.getTotal().toString());

        if (miDtoPresupuesto.getTramite().getTipoDeTramite().getAsociaInmuebles())
        {
            botonDetalleInmueble.setEnabled(true);
        } else
        {
            botonDetalleInmueble.setEnabled(false);
        }

        listaItems = miController.buscarItemsPresupuesto(miDtoPresupuesto);

        if (listaItems != null)
        {
            DtoItem miDto = null;

            for (int i = 0; i < listaItems.size(); i++)
            {

                miDto = listaItems.get(i);

                if (miDto.isFijo())
                {
                    Object[] datos =
                    {
                        miDto.getNombre(),
                        miDto.getValor()
                    };
                    ((DefaultTableModel) grillaValoresFijos.getModel()).addRow(datos);
                } else
                {

                    Object[] datos =
                    {
                        miDto.getNombre(),
                        miDto.getValor(),
                        miDto.getPorcentaje(),
                        miDto.getObservaciones()
                    };
                    ((DefaultTableModel) grillaValoresVariables.getModel()).addRow(datos);
                }
            }

            cantidadFilasGrilla = grillaValoresVariables.getRowCount();

            campoObservaciones.setEnabled(true);
            botonAceptar.setEnabled(true);
            botonAgregarItem.setEnabled(true);
            botonEliminarItem.setEnabled(true);
            botonCalcular.setEnabled(true);
        } else
        {
            JOptionPane.showMessageDialog(this, "ERROR: Este presupuesto no tiene items asociados.");
        }
    }

    public void limpiarFormulario()
    {
        campoNombrePersona.setText(" ");
        campoObservaciones.setText(" ");
        campoNombreTramite.setText(" ");
        campoTotal.setText(" ");

        miDtoPresupuesto = null;
        miDtoPersona = null;
        listaItems = null;

        int i = ((DefaultTableModel) grillaValoresVariables.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaValoresVariables.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaValoresVariables.getModel()).removeRow(i);
            i--;
        }

        int j = ((DefaultTableModel) grillaValoresFijos.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaValoresFijos.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaValoresFijos.getModel()).removeRow(j);
            j--;
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
        panelModificarPresupuesto = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        botonBuscarPresupuesto = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        campoNombreTramite = new javax.swing.JTextField();
        botonAgregarItem = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        campoTotal = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        botonCalcular = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaValoresVariables = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        campoNombrePersona = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        botonDetalleInmueble = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        campoObservaciones = new javax.swing.JTextArea();
        jSeparator3 = new javax.swing.JSeparator();
        botonEliminarItem = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        grillaValoresFijos = new javax.swing.JTable();
        labelSaldo = new javax.swing.JLabel();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Modificar Presupuestos");
        setAutoscrolls(true);
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

        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane4.setAutoscrolls(true);

        panelModificarPresupuesto.setAutoscrolls(true);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Modificar Presupuesto");

        jLabel2.setText("Buscar Presupuesto:");

        botonBuscarPresupuesto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/buscar.png"))); // NOI18N
        botonBuscarPresupuesto.setText("Buscar");
        botonBuscarPresupuesto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarPresupuestoActionPerformed(evt);
            }
        });

        jLabel3.setText("Nombre del Trámite:");

        campoNombreTramite.setEditable(false);

        botonAgregarItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar.png"))); // NOI18N
        botonAgregarItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAgregarItemActionPerformed(evt);
            }
        });

        campoTotal.setEditable(false);

        jLabel7.setText("Total:");

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

        botonCalcular.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/presupuestos/actualizar.png"))); // NOI18N
        botonCalcular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCalcularActionPerformed(evt);
            }
        });

        grillaValoresVariables.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Item", "Valor", "Porcentaje", "Observaciones"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Float.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        grillaValoresVariables.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(grillaValoresVariables);

        jLabel4.setText("Persona:");

        campoNombrePersona.setEditable(false);

        jLabel5.setText("Inmueble:");

        botonDetalleInmueble.setText("Ver Detalle");
        botonDetalleInmueble.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonDetalleInmuebleActionPerformed(evt);
            }
        });

        jLabel6.setText("Observaciones:");

        campoObservaciones.setColumns(20);
        campoObservaciones.setRows(5);
        jScrollPane2.setViewportView(campoObservaciones);

        botonEliminarItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/borrar.png"))); // NOI18N
        botonEliminarItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonEliminarItemActionPerformed(evt);
            }
        });

        jLabel8.setText("Detalle valores variables:");

        jLabel9.setText("Detalle valores fijos:");

        grillaValoresFijos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Item", "Valor"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(grillaValoresFijos);

        javax.swing.GroupLayout panelModificarPresupuestoLayout = new javax.swing.GroupLayout(panelModificarPresupuesto);
        panelModificarPresupuesto.setLayout(panelModificarPresupuestoLayout);
        panelModificarPresupuestoLayout.setHorizontalGroup(
            panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModificarPresupuestoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelModificarPresupuestoLayout.createSequentialGroup()
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(campoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(botonCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelModificarPresupuestoLayout.createSequentialGroup()
                                    .addGap(0, 88, Short.MAX_VALUE)
                                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap())
                                .addGroup(panelModificarPresupuestoLayout.createSequentialGroup()
                                    .addComponent(labelSaldo, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE))))
                        .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelModificarPresupuestoLayout.createSequentialGroup()
                        .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(panelModificarPresupuestoLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(botonBuscarPresupuesto))
                            .addGroup(panelModificarPresupuestoLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(campoNombreTramite, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelModificarPresupuestoLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(botonAgregarItem, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(botonEliminarItem, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 445, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelModificarPresupuestoLayout.createSequentialGroup()
                                    .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel6)
                                        .addComponent(jLabel5))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(botonDetalleInmueble)
                                        .addComponent(jScrollPane2))))
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelModificarPresupuestoLayout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addGap(36, 36, 36)
                                    .addComponent(campoNombrePersona))
                                .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(17, Short.MAX_VALUE))))
        );
        panelModificarPresupuestoLayout.setVerticalGroup(
            panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModificarPresupuestoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelModificarPresupuestoLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(botonBuscarPresupuesto, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(campoNombrePersona, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(botonDetalleInmueble))
                        .addGap(18, 18, 18)
                        .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(campoNombreTramite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(botonAgregarItem, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonEliminarItem, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7)
                    .addComponent(campoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelSaldo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                .addGroup(panelModificarPresupuestoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jScrollPane4.setViewportView(panelModificarPresupuesto);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonCalcularActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCalcularActionPerformed
    {//GEN-HEADEREND:event_botonCalcularActionPerformed

        this.calcularTotal();
        campoTotal.setText(totalNuevo.toString());

    }//GEN-LAST:event_botonCalcularActionPerformed

    private void calcularTotal()
    {

        if (miDtoPresupuesto != null)
        {

            TableModel miGrilla = grillaValoresVariables.getModel();
            int filas = miGrilla.getRowCount();

            TableModel miGrillaFijos = grillaValoresFijos.getModel();
            int filasFijos = miGrillaFijos.getRowCount();

            total = miDtoPresupuesto.getTotal();
            totalNuevo = 0f;

            for (int i = 0; i < filas; i++)
            {
                totalNuevo = totalNuevo + Float.parseFloat((miGrilla.getValueAt(i, 1)).toString());
            }

            for (int i = 0; i < filasFijos; i++)
            {
                totalNuevo = totalNuevo + Float.parseFloat((miGrillaFijos.getValueAt(i, 1)).toString());
            }

            if (totalNuevo > total)
            {
                saldo = miDtoPresupuesto.getSaldo() + totalNuevo - total;
            } else if (totalNuevo < total)
            {
                //Traigo los pagos ya realizados por el cliente para este presupuesto:
                List<DtoPago> pagosPresupuesto = miController.buscarPagosPresupuesto(miDtoPresupuesto);

                if (pagosPresupuesto != null && !pagosPresupuesto.isEmpty())
                {
                    Float montoPagos = 0f;

                    for (Iterator<DtoPago> it = pagosPresupuesto.iterator(); it.hasNext();)
                    {
                        DtoPago dtoPago = it.next();

                        montoPagos += dtoPago.getMonto();
                    }

                    saldo = totalNuevo - montoPagos;

                    if (saldo < 0)
                    {
                        labelSaldo.setText("SALDO A FAVOR: $" + (-saldo));
                    } else
                    {
                        labelSaldo.setText("");
                    }
                } else
                {
                    saldo = totalNuevo;
                }

            } else
            {
                saldo = miDtoPresupuesto.getSaldo();
            }

        } else
        {
            JOptionPane.showMessageDialog(this, "Previamente debe buscar un Presupuesto.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaModificarPresupuesto);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonBuscarPresupuestoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonBuscarPresupuestoActionPerformed
    {//GEN-HEADEREND:event_botonBuscarPresupuestoActionPerformed
        limpiarFormulario();

        BuscarPresupuesto miBuscarPresupuesto = new BuscarPresupuesto();
        miBuscarPresupuesto.setFormularioInvocador(ConstantesGui.MODIFICAR_PRESUPUESTO);
        miBuscarPresupuesto.setFormModificarPresupuesto(this);
        Principal.cargarFormulario(miBuscarPresupuesto);
        Principal.setVentanasActivas(BuscarPresupuesto.getVentanaBuscarPresupuesto());
    }//GEN-LAST:event_botonBuscarPresupuestoActionPerformed

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonAceptarActionPerformed
    {//GEN-HEADEREND:event_botonAceptarActionPerformed
        this.calcularTotal();

        miDtoPresupuesto.setObservaciones(campoObservaciones.getText());
        miDtoPresupuesto.setTotal(totalNuevo);

        TableModel miGrilla = grillaValoresVariables.getModel();
        int filas = miGrilla.getRowCount();

        ArrayList<DtoItem> itemsNuevos = new ArrayList<>();

        Boolean flag = true;

        for (int i = 0; i < filas; i++)
        {
            DtoItem miItem = new DtoItem();

            if (((Float) miGrilla.getValueAt(i, 1) != 0f) || ((Integer) miGrilla.getValueAt(i, 2) != 0))
            {

                miItem.setValor((Float) miGrilla.getValueAt(i, 1));

                if (((Integer) miGrilla.getValueAt(i, 2) <= 100))
                {
                    miItem.setPorcentaje((Integer) miGrilla.getValueAt(i, 2));

                } else
                {
                    JOptionPane.showMessageDialog(this, "Debe indicar un numero entre 0 y 100 en el Porcentaje del Item " + miGrilla.getValueAt(i, 0).toString(), "ADVERTENCIA", JOptionPane.YES_NO_OPTION);

                    flag = false;
                    break;
                }

            } else
            {
                int option = JOptionPane.showConfirmDialog(this, "¿Esta seguro que el Valor o Porcentaje del Item" + miGrilla.getValueAt(i, 0).toString() + " es 0?", "ADVERTENCIA", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.NO_OPTION)
                {
                    flag = false;
                    break;
                }
            }

            if (!((miGrilla.getValueAt(i, 0)).toString()).equals(""))
            {
                miItem.setNombre((miGrilla.getValueAt(i, 0)).toString());

                if (miGrilla.getValueAt(i, 3) != null)
                {
                    miItem.setObservaciones((String) miGrilla.getValueAt(i, 3));
                } else
                {
                    miItem.setObservaciones("");
                }

                itemsNuevos.add(miItem);
            } else
            {
                JOptionPane.showMessageDialog(this, "Debe completar el Nombre del Item de la fila " + (i + 1), "ERROR", JOptionPane.ERROR_MESSAGE);
                flag = false;
                break;
            }
        }

        if (flag)
        {
            if (saldo < 0)
            {
                int option = JOptionPane.showConfirmDialog(this, "<HTML>El cliente cuenta con $" + (-saldo) + " a favor.<BR><BR>¿Desea cancelar deuda con el cliente?</HTML>", "CONFIRMACION", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION)
                {
                    DtoPago miDtoPago = new DtoPago();

                    miDtoPago.setMonto(saldo);
                    miDtoPago.setFecha(Calendar.getInstance().getTime());
                    miDtoPago.setPresupuesto(miDtoPresupuesto);
                    miDtoPago.setObservaciones("Devolucion al cliente.");

                    Boolean creado = miController.darAltaPago(miDtoPago);

                    miDtoPresupuesto.setSaldo(new Float(0f));

                } else
                {
                    miDtoPresupuesto.setSaldo(saldo);
                }
            }

            Boolean modificado;
            try
            {
                modificado = miController.modificarPresupuesto(miDtoPresupuesto, listaItems, itemsNuevos);

                if (modificado)
                {
                    JOptionPane.showMessageDialog(this, "Se ha modificado el presupuesto.", "CONFIRMACION", JOptionPane.INFORMATION_MESSAGE);
                    this.limpiarFormulario();
                } else
                {
                    JOptionPane.showMessageDialog(this, "No se ha modificado el presupuesto.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    this.limpiarFormulario();
                }
            }
            catch (ClassModifiedException ex)
            {
                JOptionPane.showMessageDialog(this, "El Presupuesto ha sido recientemente modificado por otro usuario.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                limpiarFormulario();
            }
        }
    }//GEN-LAST:event_botonAceptarActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCancelarActionPerformed
    {//GEN-HEADEREND:event_botonCancelarActionPerformed
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void botonDetalleInmuebleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonDetalleInmuebleActionPerformed

        if (miDtoPersona != null)
        {
            DtoInmueble miInmueble = null;

            miInmueble = miController.buscarInmueble(miDtoPresupuesto.getTramite());

            if (miInmueble != null)
            {
                //Muestro detalle de Inmueble
                DetalleInmueble miDetalleInmueble = new DetalleInmueble();
                miDetalleInmueble.setInmueble(miInmueble);
                miDetalleInmueble.cargarFormulario();

                Principal.cargarFormulario(miDetalleInmueble);
            }
        } else
        {
            JOptionPane.showMessageDialog(this, "Debe buscar primero a una Persona.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_botonDetalleInmuebleActionPerformed

    private void botonAgregarItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAgregarItemActionPerformed

        if (miDtoPresupuesto != null)
        {
            // TODO add your handling code here:
            Object[] datos =
            {
                "",
                0.0f,
                0,
                ""
            };
            ((DefaultTableModel) grillaValoresVariables.getModel()).addRow(datos);

            cantidadFilasNuevasGrilla++;
        } else
        {
            JOptionPane.showMessageDialog(this, "Previamente debe buscar un Presupuesto.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_botonAgregarItemActionPerformed

    private void botonEliminarItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonEliminarItemActionPerformed

        TableModel miGrilla = grillaValoresVariables.getModel();
        TableModel miGrillaFijos = grillaValoresFijos.getModel();
        int filasFijos = miGrillaFijos.getRowCount();
        int filas = miGrilla.getRowCount();

        if (filas >= 1 || filasFijos >= 1)
        {
            int filaSeleccionada = grillaValoresVariables.getSelectedRow();

            ((DefaultTableModel) grillaValoresVariables.getModel()).removeRow(filaSeleccionada);
        } else
        {
            JOptionPane.showMessageDialog(this, "El Presupuesto debe tener al menos un Item.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_botonEliminarItemActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonAgregarItem;
    private javax.swing.JButton botonBuscarPresupuesto;
    private javax.swing.JButton botonCalcular;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JButton botonDetalleInmueble;
    private javax.swing.JButton botonEliminarItem;
    private javax.swing.JTextField campoNombrePersona;
    private javax.swing.JTextField campoNombreTramite;
    private javax.swing.JTextArea campoObservaciones;
    private javax.swing.JTextField campoTotal;
    private javax.swing.JTable grillaValoresFijos;
    private javax.swing.JTable grillaValoresVariables;
    private javax.swing.JLabel jLabel1;
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
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labelSaldo;
    private javax.swing.JPanel panelModificarPresupuesto;
    // End of variables declaration//GEN-END:variables
}
