/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.presupuestos;

import com.licensis.notaire.dto.DtoConcepto;
import com.licensis.notaire.dto.DtoInmueble;
import com.licensis.notaire.dto.DtoItem;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.licensis.notaire.dto.DtoTipoDeTramite;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorValidaciones;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Tefi
 */
public class DetalleValoresTramites extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaDetalleValoresTramitePresupuesto = new JMenuItem("Ventana Detalle Valores Tramite Presupuestos");
    private DtoTipoDeTramite miTipoTramite = null;
    private ArrayList<DtoConcepto> conceptos = null;
    private ControllerNegocio miController = null;
    private DtoInmueble inmuebleEncontrado = null;
    private AdministradorValidaciones validador = AdministradorValidaciones.getInstancia();
    private DtoTramite miDtoTramite = new DtoTramite();
    private DtoPresupuesto miDtoPresupuesto = new DtoPresupuesto();
    private DtoInmueble miDtoInmueble = null;

    /**
     * Creates new form DetalleValoresTramites
     */
    public DetalleValoresTramites() {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioGrandeHorizontal, Principal.tamanioGrandeVertical);
        miController = ControllerNegocio.getInstancia();
    }

    private void salir() {
        this.dispose();
    }

    public static JMenuItem getVentanaDetalleValoresTramitePresupuesto() {
        return ventanaDetalleValoresTramitePresupuesto;
    }

    public void setTramite(DtoTipoDeTramite tramite) {
        miTipoTramite = tramite;

        cargarFormulario();
    }

    public void setInmueble(DtoInmueble dtoInmueble) {
        inmuebleEncontrado = dtoInmueble;

        campoNomenclaturaCatastral.setText(inmuebleEncontrado.getNomenclaturaCatastral());
        campoValuacionFiscal.setText(inmuebleEncontrado.getValuacionFiscal());
        campoDomicilio.setText(inmuebleEncontrado.getDomicilio());
        campoTipoInmueble.setText(inmuebleEncontrado.getTipoInmueble());
        campoObservaciones.setText(inmuebleEncontrado.getObservaciones());

    }

    private void cargarFormulario() {

        campoNombreTramite.setText(miTipoTramite.getNombre());

        if (miTipoTramite.getAsociaInmuebles())
        {
            botonBuscarInmueble.setEnabled(true);
            campoNomenclaturaCatastral.setEditable(true);
            campoValuacionFiscal.setEditable(true);
            campoDomicilio.setEditable(true);
            campoTipoInmueble.setEditable(true);
            campoObservaciones.setEditable(true);
        }
        else
        {
            botonBuscarInmueble.setEnabled(false);
            campoNomenclaturaCatastral.setEnabled(false);
            campoValuacionFiscal.setEnabled(false);
            campoDomicilio.setEnabled(false);
            campoTipoInmueble.setEnabled(false);
            campoObservaciones.setEnabled(false);
        }

        cargarGrillas();
    }

    private void cargarGrillas() {
        conceptos = miController.obtenerConceptosTramite(miTipoTramite);

        if (!conceptos.isEmpty())
        {
            DtoConcepto miDtoConcepto;
            String nombreItem;
            Float valor;
            Integer porcentaje;
            String observaciones;

            for (int i = 0; i < conceptos.size(); i++)
            {

                miDtoConcepto = conceptos.get(i);

                nombreItem = miDtoConcepto.getNombre();

                if (miDtoConcepto.getValor() != null)
                {
                    valor = miDtoConcepto.getValor();
                }
                else
                {
                    valor = 0f;
                }

                if (miDtoConcepto.getPorcentaje() != null)
                {
                    porcentaje = miDtoConcepto.getPorcentaje();
                }
                else
                {
                    porcentaje = 0;
                }

                if (miDtoConcepto.isFijo())
                {
                    Object[] datos =
                    {
                        nombreItem,
                        valor
                    };
                    ((DefaultTableModel) grillaValoresFijos.getModel()).addRow(datos);
                }
                else
                {
                    Object[] datos =
                    {
                        nombreItem,
                        valor,
                        porcentaje
                    };
                    ((DefaultTableModel) grillaValoresVariables.getModel()).addRow(datos);
                }
            }

        }
        else
        {
            JOptionPane.showMessageDialog(this, "No existen conceptos asociados al Tramite seleccionado.");
            salir();
        }
    }

    public void cargarInmueble(DtoInmueble miDtoInmueble) {
        campoNomenclaturaCatastral.setText(miDtoInmueble.getNomenclaturaCatastral());
        campoValuacionFiscal.setText(miDtoInmueble.getValuacionFiscal());
        campoDomicilio.setText(miDtoInmueble.getDomicilio());
        campoTipoInmueble.setText(miDtoInmueble.getTipoInmueble());
        campoObservaciones.setText(miDtoInmueble.getObservaciones());
    }
    //TODO: limpiarFormulario detalle presupuesto!

    public void limpiarFormulario() {
        campoNombreTramite.setText("");
        campoNomenclaturaCatastral.setText("");
        campoValuacionFiscal.setText("");
        campoDomicilio.setText("");
        campoTipoInmueble.setText("");
        campoObservaciones.setText("");
        
        inmuebleEncontrado = null;

        int filas = grillaValoresVariables.getRowCount();

        for (int i = 0; i < filas; i++)
        {

            ((DefaultTableModel) grillaValoresVariables.getModel()).removeRow(i);
        }

        filas = grillaValoresFijos.getRowCount();

        for (int i = 0; i < filas; i++)
        {

            ((DefaultTableModel) grillaValoresFijos.getModel()).removeRow(i);
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
        panelDetalleValoresTrámites = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaValoresVariables = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        campoNomenclaturaCatastral = new javax.swing.JTextField();
        campoValuacionFiscal = new javax.swing.JTextField();
        botonAceptar = new javax.swing.JButton();
        campoNombreTramite = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        campoDomicilio = new javax.swing.JTextField();
        campoTipoInmueble = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        campoObservaciones = new javax.swing.JTextArea();
        jLabel9 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        botonCancelar = new javax.swing.JButton();
        botonBuscarInmueble = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        labelNomenclaturaValidacion = new javax.swing.JLabel();
        labelValuacionValidacion = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        grillaValoresFijos = new javax.swing.JTable();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Detalle Valores Trámite Presupuestos");
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
        jLabel1.setText("Detalle Valores de Trámites");

        jLabel2.setText("Nombre Trámite:");

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
            boolean[] canEdit = new boolean [] {
                false, true, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(grillaValoresVariables);

        jLabel4.setText("Nomenclatura Catastral:");

        jLabel5.setText("Valuación Fiscal:");

        campoNomenclaturaCatastral.setEditable(false);
        campoNomenclaturaCatastral.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoNomenclaturaCatastralKeyPressed(evt);
            }
        });

        campoValuacionFiscal.setEditable(false);
        campoValuacionFiscal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoValuacionFiscalKeyPressed(evt);
            }
        });

        botonAceptar.setText("Aceptar");
        botonAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAceptarActionPerformed(evt);
            }
        });

        campoNombreTramite.setEditable(false);

        jLabel3.setText("Detalle del Inmueble (Si corresponde):");

        jLabel6.setText("Domicilio:");

        jLabel7.setText("Tipo de Inmueble:");

        jLabel8.setText("Observaciones:");

        campoDomicilio.setEditable(false);

        campoTipoInmueble.setEditable(false);

        campoObservaciones.setColumns(20);
        campoObservaciones.setEditable(false);
        campoObservaciones.setRows(5);
        jScrollPane2.setViewportView(campoObservaciones);

        jLabel9.setText("Detalle de valores variables:");

        botonCancelar.setText("Cancelar");
        botonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCancelarActionPerformed(evt);
            }
        });

        botonBuscarInmueble.setText("Buscar Inmueble");
        botonBuscarInmueble.setEnabled(false);
        botonBuscarInmueble.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarInmuebleActionPerformed(evt);
            }
        });

        jLabel10.setForeground(new java.awt.Color(255, 0, 0));
        jLabel10.setText("(*)");

        jLabel11.setForeground(new java.awt.Color(255, 0, 0));
        jLabel11.setText("(*)");

        jLabel12.setForeground(new java.awt.Color(255, 0, 0));
        jLabel12.setText("(*)");

        jLabel13.setForeground(new java.awt.Color(255, 0, 0));
        jLabel13.setText("(*): Obligatorio en caso de asociarse a un Inmueble.");

        labelNomenclaturaValidacion.setForeground(new java.awt.Color(255, 0, 0));

        labelValuacionValidacion.setForeground(new java.awt.Color(255, 0, 0));

        jLabel14.setText("Detalle de valores fijos:");

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

        javax.swing.GroupLayout panelDetalleValoresTrámitesLayout = new javax.swing.GroupLayout(panelDetalleValoresTrámites);
        panelDetalleValoresTrámites.setLayout(panelDetalleValoresTrámitesLayout);
        panelDetalleValoresTrámitesLayout.setHorizontalGroup(
            panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetalleValoresTrámitesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addGroup(panelDetalleValoresTrámitesLayout.createSequentialGroup()
                        .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelDetalleValoresTrámitesLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(botonBuscarInmueble, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelDetalleValoresTrámitesLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(55, 55, 55)
                                .addComponent(campoNombreTramite, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelDetalleValoresTrámitesLayout.createSequentialGroup()
                                .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelDetalleValoresTrámitesLayout.createSequentialGroup()
                                            .addComponent(jLabel9)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jSeparator2))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelDetalleValoresTrámitesLayout.createSequentialGroup()
                                            .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel4)
                                                .addComponent(jLabel5))
                                            .addGap(18, 18, 18)
                                            .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(campoNomenclaturaCatastral)
                                                .addComponent(campoValuacionFiscal, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                                                .addComponent(campoDomicilio)
                                                .addComponent(campoTipoInmueble)
                                                .addComponent(jScrollPane2))))
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel10))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDetalleValoresTrámitesLayout.createSequentialGroup()
                                .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(labelNomenclaturaValidacion, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                                    .addComponent(labelValuacionValidacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(panelDetalleValoresTrámitesLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                                .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        panelDetalleValoresTrámitesLayout.setVerticalGroup(
            panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetalleValoresTrámitesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(campoNombreTramite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(botonBuscarInmueble, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(campoNomenclaturaCatastral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(labelNomenclaturaValidacion, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(campoValuacionFiscal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelValuacionValidacion, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDetalleValoresTrámitesLayout.createSequentialGroup()
                        .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(campoDomicilio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(campoTipoInmueble, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(1, 1, 1)
                        .addComponent(jLabel13)
                        .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDetalleValoresTrámitesLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelDetalleValoresTrámitesLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel9)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 37, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDetalleValoresTrámitesLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(panelDetalleValoresTrámitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jScrollPane4.setViewportView(panelDetalleValoresTrámites);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonAceptarActionPerformed
    {//GEN-HEADEREND:event_botonAceptarActionPerformed
        if (miTipoTramite.getAsociaInmuebles())
        {
            if (inmuebleEncontrado == null)
            {
                if (!validador.validarCampoVacio(campoNomenclaturaCatastral.getText())
                        && !validador.validarCampoVacio(campoTipoInmueble.getText())
                        && validador.validarLetrasGuiones(campoNomenclaturaCatastral.getText()))
                {

                    miDtoInmueble = new DtoInmueble();

                    miDtoInmueble.setNomenclaturaCatastral(campoNomenclaturaCatastral.getText());
                    miDtoInmueble.setDomicilio(campoDomicilio.getText());
                    miDtoInmueble.setTipoInmueble(campoTipoInmueble.getText());
                    miDtoInmueble.setObservaciones(campoObservaciones.getText());


                    if (!validador.validarCampoVacio(campoValuacionFiscal.getText()))
                    {

                        if (validador.validarNumeroFloat(this.campoValuacionFiscal.getText()))
                        {

                            miDtoInmueble.setValuacionFiscal(campoValuacionFiscal.getText());
                          
                            cargarDatos();

                        }
                        else
                        {
                            JOptionPane.showMessageDialog(this, "Escriba correctamente la valuacion fiscal.", "ERROR", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                }
                else
                {
                    JOptionPane.showMessageDialog(this, "Debe completar los campos obligatorios de Inmueble.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                }
            }
            else
            {
                cargarDatos();
            }
        }
        else
        {
            cargarDatos();
        }
    }//GEN-LAST:event_botonAceptarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaDetalleValoresTramitePresupuesto);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void cargarDatos() {

        Boolean flag = true;

        miDtoTramite.setTiposDeTramite(miTipoTramite);

        TableModel miGrilla = grillaValoresVariables.getModel();
        int filas = miGrilla.getRowCount();

        ArrayList<DtoItem> items = new ArrayList<>();
        Float total = 0f;

        for (int i = 0; i < filas; i++)
        {
            DtoItem miItem = new DtoItem();

            Float valor = Float.parseFloat((miGrilla.getValueAt(i, 1)).toString());

            if (valor == 0.0f)
            {

                int option = JOptionPane.showConfirmDialog(this, "<HTML>¿Esta seguro que el valor o porcentaje <BR>del Item " + (miGrilla.getValueAt(i, 0)).toString() + " , es 0?</HTML>", "ADVERTENCIA", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.NO_OPTION)
                {
                    flag = false;
                    break;
                }
                else
                {

                    miItem.setValor(valor);

                    miItem.setNombre((miGrilla.getValueAt(i, 0)).toString());

                    miItem.setPorcentaje(Integer.parseInt((miGrilla.getValueAt(i, 2)).toString()));

                    if (miGrilla.getValueAt(i, 3) != null)
                    {
                        miItem.setObservaciones((miGrilla.getValueAt(i, 3)).toString());
                    }
                    else
                    {
                        miItem.setObservaciones("");
                    }

                    miItem.setConceptoFijo(false);
                    items.add(miItem);

                    total = total + Float.parseFloat((miGrilla.getValueAt(i, 1)).toString());

                }
            }
            else
            {

                miItem.setValor(valor);

                miItem.setNombre((miGrilla.getValueAt(i, 0)).toString());

                miItem.setPorcentaje(Integer.parseInt((miGrilla.getValueAt(i, 2)).toString()));

                if (miGrilla.getValueAt(i, 3) != null)
                {
                    miItem.setObservaciones((miGrilla.getValueAt(i, 3)).toString());
                }
                else
                {
                    miItem.setObservaciones("");
                }
                miItem.setConceptoFijo(false);
                items.add(miItem);

                total = total + Float.parseFloat((miGrilla.getValueAt(i, 1)).toString());
            }
        }

        //-------------------------------------------------------------------------
        //Grilla fijos:

        TableModel miGrillaFijos = grillaValoresFijos.getModel();
        int filasFijos = miGrillaFijos.getRowCount();

        for (int i = 0; i < filasFijos; i++)
        {
            DtoItem miItem = new DtoItem();

            Float valor = Float.parseFloat((miGrillaFijos.getValueAt(i, 1)).toString());

            miItem.setValor(valor);

            miItem.setNombre((miGrillaFijos.getValueAt(i, 0)).toString());

            miItem.setPorcentaje(0);

            miItem.setObservaciones("");

            miItem.setConceptoFijo(true);

            items.add(miItem);

            total = total + Float.parseFloat((miGrillaFijos.getValueAt(i, 1)).toString());
        }

        //-------------------------------------------------------------------------
        if (flag)
        {
            miDtoPresupuesto.setTotal(total);
            miDtoPresupuesto.setSaldo(total);

            CrearPresupuesto.getInstancia().setConfigurado(true, miDtoInmueble, miDtoTramite, miDtoPresupuesto, items);

            salir();
        }
    }
    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCancelarActionPerformed
        // TODO add your handling code here:
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void botonBuscarInmuebleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuscarInmuebleActionPerformed

        BuscarInmueble buscador = new BuscarInmueble();

        buscador.setDetalle(this);

        Principal.cargarFormulario(buscador);
    }//GEN-LAST:event_botonBuscarInmuebleActionPerformed

    private void campoNomenclaturaCatastralKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoNomenclaturaCatastralKeyPressed
        // TODO add your handling code here:
        if (validador.validarLetrasGuiones(campoNomenclaturaCatastral.getText()) == false)
        {
            labelNomenclaturaValidacion.setText("Solo letras, numeros y guiones sin espacios.");
        }
        else if (validador.validarCampoEspacios(campoNomenclaturaCatastral.getText()) == true)
        {
            labelNomenclaturaValidacion.setText("Solo letras, numeros y guiones sin espacios.");
        }
        else
        {
            labelNomenclaturaValidacion.setText("");
        }
    }//GEN-LAST:event_campoNomenclaturaCatastralKeyPressed

    private void campoValuacionFiscalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoValuacionFiscalKeyPressed
        // TODO add your handling code here:
        if (!validador.validarNumeroFloat(this.campoValuacionFiscal.getText()))
        {
            labelValuacionValidacion.setText("Escriba un numero valido, ej: 25.50");
        }
        else
        {
            labelValuacionValidacion.setText("");
        }
    }//GEN-LAST:event_campoValuacionFiscalKeyPressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonBuscarInmueble;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JTextField campoDomicilio;
    private javax.swing.JTextField campoNombreTramite;
    private javax.swing.JTextField campoNomenclaturaCatastral;
    private javax.swing.JTextArea campoObservaciones;
    private javax.swing.JTextField campoTipoInmueble;
    private javax.swing.JTextField campoValuacionFiscal;
    private javax.swing.JTable grillaValoresFijos;
    private javax.swing.JTable grillaValoresVariables;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
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
    private javax.swing.JLabel labelNomenclaturaValidacion;
    private javax.swing.JLabel labelValuacionValidacion;
    private javax.swing.JPanel panelDetalleValoresTrámites;
    // End of variables declaration//GEN-END:variables
}
