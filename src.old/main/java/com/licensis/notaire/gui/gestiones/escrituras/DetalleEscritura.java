/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.gestiones.escrituras;

import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoEstadoDeGestion;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorValidaciones;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author matias
 */
public class DetalleEscritura extends javax.swing.JInternalFrame
{

    private static JMenuItem ventanaDetalleEscritura = new JMenuItem("Ventana Detalle Escritura");
    private List<DtoFolio> foliosDisponibles = new ArrayList<>();
    private ControllerNegocio miController = ControllerNegocio.getInstancia();
    private DtoGestionDeEscritura miGestionDeEscritura = null;
    private DtoEscritura miEscritura = null;
    private String formularioInvocador = null;
    private Integer folioHastaSeleccionado = null;

    /**
     * Creates new form DetalleEscritura
     */
    public DetalleEscritura()
    {
        initComponents();
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioGrandeVertical);
        grillaListaTramitesGestion.setAutoCreateRowSorter(true);
        grupoRadioButtons.add(radioSinFirmar);
        grupoRadioButtons.add(radioFirmada);
        grupoRadioButtons.add(radioAnulada);
        grupoRadioButtons.add(radioNoPaso);
        radioSinFirmar.setSelected(true);
    }

    public void setFormularioInvocador(String formulario)
    {
        this.formularioInvocador = formulario;
    }

    public Boolean cargarFormularioCrearEscritura(DtoGestionDeEscritura miDtoGestionDeEscritura)
    {
        miGestionDeEscritura = miDtoGestionDeEscritura;
        Boolean flag = false;
        Boolean ok = true;

        botonCerrar.setVisible(false);
        labelDesde1.setVisible(false);
        labelHasta1.setVisible(false);
        labelIndicados.setVisible(false);
        labelInclusive1.setVisible(false);

        if (miGestionDeEscritura.getListaTramitesAsociados() != null
                && !miGestionDeEscritura.getListaTramitesAsociados().isEmpty())
        {

            //Verifico que existan Tramites de la gestion sin escrituras para utilizar:
            for (Iterator<DtoTramite> it = miGestionDeEscritura.getListaTramitesAsociados().iterator(); it.hasNext();)
            {
                DtoTramite dtoTramite = it.next();

                DtoTramite tramiteEncontrado = miController.buscarTramite(dtoTramite);

                if (tramiteEncontrado != null)
                {
                    if (tramiteEncontrado.getEscritura() == null)
                    {
                        Object[] datos =
                        {
                            tramiteEncontrado.getTipoDeTramite().getNombre(),
                            Boolean.TRUE,
                            tramiteEncontrado
                        };
                        ((DefaultTableModel) grillaListaTramitesGestion.getModel()).addRow(datos);
                        flag = true;
                    }
                }
            }

            if (flag)
            {

                labelDesde.setVisible(false);
                labelHasta.setVisible(false);
                labelInclusive1.setVisible(false);
                labelIndicados.setVisible(false);

                //Buscar folios disponibles
                foliosDisponibles = miController.buscarFoliosDisponibles(miGestionDeEscritura.getPersonaEscribano().getRegistroEscribano());

                if (foliosDisponibles != null && !foliosDisponibles.isEmpty())
                {
                    for (Iterator<DtoFolio> it = foliosDisponibles.iterator(); it.hasNext();)
                    {
                        DtoFolio dtoFolio = it.next();

                        comboFolioDesde.addItem(dtoFolio.getNumero());
                        comboFolioHasta.addItem(dtoFolio.getNumero());
                    }

                } else
                {
                    JOptionPane.showMessageDialog(this, "No existen folios disponibles para utilizar.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                    ok = false;
                    salir();
                }
            } else
            {
                JOptionPane.showMessageDialog(this, "La gestion ya tiene todos sus tramites asociados a una escritura.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                ok = false;
                salir();
            }
        } else
        {
            JOptionPane.showMessageDialog(this, "No existen tramites asociados a la gestion actual.");
            ok = false;
            salir();
        }

        return ok;
    }

    public Boolean cargarFormularioModificarEscritura(DtoEscritura miDtoEscritura)
    {
        Boolean ok = true;

        miGestionDeEscritura = miDtoEscritura.getTramites().get(0).getGestion();
        miEscritura = miDtoEscritura;

        campoNumeroEscritura.setText(new Integer(miDtoEscritura.getNumero()).toString());
        campoNumeroEscritura.setEditable(false);

        selectorFecha.setDate(miDtoEscritura.getFechaEscrituracion());

        Integer folioDesdeEscritura = miDtoEscritura.getFolios().get(0).getNumero();
        Integer folioHastaEscritura = miDtoEscritura.getFolios().get(miDtoEscritura.getFolios().size() - 1).getNumero();

        labelDesde.setText(folioDesdeEscritura.toString());
        labelHasta.setText(folioHastaEscritura.toString());

        //Buscar folios disponibles
        foliosDisponibles = miController.buscarFoliosDisponibles(miGestionDeEscritura.getPersonaEscribano().getRegistroEscribano());

        comboFolioDesde.setVisible(false);
        labelDesdeDisponible.setVisible(false);

        if (foliosDisponibles != null && !foliosDisponibles.isEmpty())
        {
            Integer primerFolioHasta = foliosDisponibles.get(0).getNumero();

            if (formularioInvocador.equals(ConstantesGui.MODIFICAR_ESCRITURA))
            {
                if (primerFolioHasta != (folioHastaEscritura + 1))
                {
                    labelValidacion.setText("<HTML>No se puede modificar los folios ya que <BR>los folios diponibles no son correlativos.</HTML>");
                    labelHastaNuevo.setVisible(false);
                    comboFolioHasta.setVisible(false);
                    labelInclusiveNuevo.setVisible(false);
                    labelFoliosDisp.setVisible(false);
                } else
                {

                    for (Iterator<DtoFolio> it = foliosDisponibles.iterator(); it.hasNext();)
                    {
                        DtoFolio dtoFolio = it.next();
                        comboFolioHasta.addItem(dtoFolio.getNumero());
                    }
                    labelValidacion.setText("");
                }
            }
        } else
        {
            comboFolioHasta.setEnabled(false);
            labelValidacion.setText("No existen folios disponibles para utilizar.");
        }

        DtoGestionDeEscritura gestionEncontrada = miController.buscarDtoGestion(miGestionDeEscritura);
        //Cargar grilla de tramites
        miGestionDeEscritura.setListaTramitesAsociados(gestionEncontrada.getListaTramitesAsociados());

        if (miGestionDeEscritura.getListaTramitesAsociados() != null
                && !miGestionDeEscritura.getListaTramitesAsociados().isEmpty())
        {
            for (Iterator<DtoTramite> it = miGestionDeEscritura.getListaTramitesAsociados().iterator(); it.hasNext();)
            {
                DtoTramite miDtoTramite = it.next();

                miDtoTramite = miController.buscarTramite(miDtoTramite);

                if (miDtoTramite.getEscritura() != null)
                {
                    if (miDtoTramite.getEscritura().getIdEscritura().intValue() == miEscritura.getIdEscritura().intValue())
                    {
                        Object[] datos =
                        {
                            miDtoTramite.getTipoDeTramite().getNombre(),
                            Boolean.TRUE,
                            miDtoTramite
                        };
                        ((DefaultTableModel) grillaListaTramitesGestion.getModel()).addRow(datos);
                    }
                }
            }

            campoCuerpoEscritura.setText(miEscritura.getCuerpo());

            switch (miEscritura.getEstado())
            {
                case ConstantesNegocio.ESCRITURA_FIRMADA:
                {
                    radioFirmada.setSelected(true);
                    radioSinFirmar.setEnabled(false);
                    break;
                }
                case ConstantesNegocio.ESCRITURA_ANULADA:
                {
                    radioAnulada.setSelected(true);
                    radioSinFirmar.setEnabled(false);
                    break;
                }
                case ConstantesNegocio.ESCRITURA_NO_PASO:
                {
                    radioNoPaso.setSelected(true);
                    radioSinFirmar.setEnabled(false);
                    break;
                }
            }

            if (formularioInvocador.equals(ConstantesGui.DETALLE_GESTION)
                    || formularioInvocador.equals(ConstantesGui.BUSCAR_ESCRITURA))
            {
                selectorFecha.setEnabled(false);
                comboFolioHasta.setVisible(false);
                labelHastaNuevo.setVisible(false);
                labelInclusiveNuevo.setVisible(false);
                labelFoliosDisp.setVisible(false);
                grillaListaTramitesGestion.setEnabled(false);
                campoCuerpoEscritura.setEditable(false);
                radioAnulada.setEnabled(false);
                radioFirmada.setEnabled(false);
                radioSinFirmar.setEnabled(false);
                radioNoPaso.setEnabled(false);

                botonAceptar.setVisible(false);
                botonCancelar.setVisible(false);
            } else
            {
                botonCerrar.setVisible(false);
            }
        } else
        {
            JOptionPane.showMessageDialog(this, "No existen tramites asociados a la gestion actual.", "Informacion", JOptionPane.INFORMATION_MESSAGE);
            ok = false;
            salir();
        }
        return ok;
    }

    private void salir()
    {
        this.dispose();
    }

    public static JMenuItem getVentanaDetalleEscritura()
    {
        return ventanaDetalleEscritura;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grupoRadioButtons = new javax.swing.ButtonGroup();
        jScrollPane3 = new javax.swing.JScrollPane();
        panelDetalleEscritura = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        labelIndicados = new javax.swing.JLabel();
        comboFolioDesde = new javax.swing.JComboBox();
        comboFolioHasta = new javax.swing.JComboBox();
        labelHastaNuevo = new javax.swing.JLabel();
        labelInclusiveNuevo = new javax.swing.JLabel();
        campoNumeroEscritura = new javax.swing.JTextField();
        selectorFecha = new com.toedter.calendar.JDateChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaListaTramitesGestion = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        campoCuerpoEscritura = new javax.swing.JTextArea();
        labelDesdeDisponible = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        labelValidacion = new javax.swing.JLabel();
        labelFoliosDisp = new javax.swing.JLabel();
        labelInclusive1 = new javax.swing.JLabel();
        labelHasta1 = new javax.swing.JLabel();
        labelDesde = new javax.swing.JLabel();
        labelHasta = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        radioSinFirmar = new javax.swing.JRadioButton();
        radioFirmada = new javax.swing.JRadioButton();
        radioAnulada = new javax.swing.JRadioButton();
        botonCerrar = new javax.swing.JButton();
        radioNoPaso = new javax.swing.JRadioButton();
        labelDesde1 = new javax.swing.JLabel();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Detalle Escritura");
        setToolTipText("Detalle de Escritura");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        panelDetalleEscritura.setAutoscrolls(true);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Detalle Escritura");

        jLabel2.setText("Número de Escritura:");

        jLabel3.setText("Fecha:");

        labelIndicados.setText("Folios Indicados:");

        comboFolioHasta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                comboFolioHastaMouseClicked(evt);
            }
        });
        comboFolioHasta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboFolioHastaActionPerformed(evt);
            }
        });

        labelHastaNuevo.setText("Hasta:");

        labelInclusiveNuevo.setText("Inclusive");

        campoNumeroEscritura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoNumeroEscrituraActionPerformed(evt);
            }
        });
        campoNumeroEscritura.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoNumeroEscrituraKeyPressed(evt);
            }
        });

        grillaListaTramitesGestion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Trámite", "Seleccionado", "DtoTramite"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(grillaListaTramitesGestion);
        grillaListaTramitesGestion.getColumnModel().getColumn(2).setMaxWidth(0);
        grillaListaTramitesGestion.getColumnModel().getColumn(2).setMinWidth(0);
        grillaListaTramitesGestion.getColumnModel().getColumn(2).setMaxWidth(0);
        grillaListaTramitesGestion.getColumnModel().getColumn(2).setMinWidth(0);

        jLabel7.setText("Lista Trámites Gestión:");

        jLabel8.setText("Cuerpo de Escritura:");

        campoCuerpoEscritura.setColumns(20);
        campoCuerpoEscritura.setLineWrap(true);
        campoCuerpoEscritura.setRows(5);
        campoCuerpoEscritura.setWrapStyleWord(true);
        jScrollPane2.setViewportView(campoCuerpoEscritura);

        labelDesdeDisponible.setText("Desde:");

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

        labelValidacion.setForeground(new java.awt.Color(255, 51, 0));

        labelFoliosDisp.setText("Folios Disponibles:");

        labelInclusive1.setText("Inclusive");

        labelHasta1.setText("Hasta:");

        labelDesde.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelHasta.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setText("Estado:");

        radioSinFirmar.setText("Sin Firmar");

        radioFirmada.setText("Firmada");

        radioAnulada.setText("Anulada");

        botonCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar.png"))); // NOI18N
        botonCerrar.setText("Cerrar");
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });

        radioNoPaso.setText("No Paso");
        radioNoPaso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioNoPasoActionPerformed(evt);
            }
        });

        labelDesde1.setText("Desde:");

        javax.swing.GroupLayout panelDetalleEscrituraLayout = new javax.swing.GroupLayout(panelDetalleEscritura);
        panelDetalleEscritura.setLayout(panelDetalleEscrituraLayout);
        panelDetalleEscrituraLayout.setHorizontalGroup(
            panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                        .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDetalleEscrituraLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(labelValidacion, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                                .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                                        .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                                                .addComponent(labelIndicados)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(labelDesde1)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                                                .addComponent(campoNumeroEscritura, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(selectorFecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                                                .addComponent(labelDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(labelHasta1)
                                                .addGap(12, 12, 12)
                                                .addComponent(labelHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(labelInclusive1))))
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel7)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 578, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 578, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 578, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                        .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                                .addComponent(labelFoliosDisp)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelDesdeDisponible)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboFolioDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelHastaNuevo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(comboFolioHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelInclusiveNuevo))
                            .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(radioNoPaso)
                                    .addComponent(radioAnulada)
                                    .addComponent(radioFirmada)
                                    .addComponent(radioSinFirmar))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        panelDetalleEscrituraLayout.setVerticalGroup(
            panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetalleEscrituraLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(campoNumeroEscritura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3))
                    .addComponent(selectorFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelIndicados)
                        .addComponent(labelDesde1))
                    .addComponent(labelDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelHasta1)
                    .addComponent(labelHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelInclusive1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFoliosDisp)
                    .addComponent(labelDesdeDisponible)
                    .addComponent(comboFolioDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelHastaNuevo)
                    .addComponent(comboFolioHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelInclusiveNuevo))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioSinFirmar)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioFirmada)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioNoPaso)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioAnulada)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                .addComponent(labelValidacion, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(panelDetalleEscrituraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jScrollPane3.setViewportView(panelDetalleEscritura);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 675, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        Principal.removeVentanaActivas(ventanaDetalleEscritura);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void radioNoPasoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioNoPasoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radioNoPasoActionPerformed

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCerrarActionPerformed
        salir();
    }//GEN-LAST:event_botonCerrarActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCancelarActionPerformed
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAceptarActionPerformed
        AdministradorValidaciones admin = AdministradorValidaciones.getInstancia();

        if (selectorFecha.getDate() != null)
        {
            if (!admin.validarCampoVacio(campoNumeroEscritura.getText())
                    && !admin.validarCampoVacio(selectorFecha.getDate().toString())
                    && !admin.validarCampoVacio(campoCuerpoEscritura.getText()))
            {
                DtoEscritura miDtoEscritura = new DtoEscritura();

                miDtoEscritura.setTramites(new ArrayList<DtoTramite>());

                TableModel miGrilla = grillaListaTramitesGestion.getModel();
                int filas = miGrilla.getRowCount();
                DtoTramite miTramiteGrilla = null;

                for (int i = 0; i < filas; i++)
                {
                    if ((boolean) miGrilla.getValueAt(i, 1) == true)
                    {
                        miTramiteGrilla = (DtoTramite) miGrilla.getValueAt(i, 2);

                        for (Iterator<DtoTramite> it = miGestionDeEscritura.getListaTramitesAsociados().iterator(); it.hasNext();)
                        {
                            DtoTramite dtoTramite = it.next();
                            dtoTramite = miController.buscarTramite(dtoTramite);

                            if (dtoTramite.getIdTramite().intValue() == miTramiteGrilla.getIdTramite().intValue())
                            {
                                miDtoEscritura.getTramites().add(dtoTramite);
                            }
                        }
                    }
                }

                if (miDtoEscritura.getTramites().isEmpty())
                {
                    JOptionPane.showMessageDialog(this, "Debe elegir por lo menos un Tramite.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                } else
                {
                    Integer numeroEscritura = Integer.parseInt(campoNumeroEscritura.getText());
                    Date fecha = selectorFecha.getDate();
                    DtoEstadoDeGestion estadoGestionConEscritura = null;
                    DtoEstadoDeGestion nombre = new DtoEstadoDeGestion();
                    String cuerpo = campoCuerpoEscritura.getText();

                    if (radioAnulada.isSelected())
                    {
                        miDtoEscritura.setEstado(ConstantesNegocio.ESCRITURA_ANULADA);
                        nombre.setNombre(ConstantesNegocio.GESTION_CON_ESCRITURA_ANULADA);
                    } else if (radioFirmada.isSelected())
                    {
                        miDtoEscritura.setEstado(ConstantesNegocio.ESCRITURA_FIRMADA);
                        nombre.setNombre(ConstantesNegocio.GESTION_CON_ESCRITURA_FIRMADA);
                    } else if (radioSinFirmar.isSelected())
                    {
                        miDtoEscritura.setEstado(ConstantesNegocio.ESCRITURA_SIN_FIRMAR);
                        nombre.setNombre(ConstantesNegocio.GESTION_CON_ESCRITURA_SIN_FIRMAR);
                    } else if (radioNoPaso.isSelected())
                    {
                        miDtoEscritura.setEstado(ConstantesNegocio.ESCRITURA_NO_PASO);
                        nombre.setNombre(ConstantesNegocio.GESTION_CON_ESCRITURA_NO_PASO);
                    }

                    estadoGestionConEscritura = miController.obtenerDtoEstadoDeGestion(nombre);

                    miDtoEscritura.setNumero(numeroEscritura.intValue());
                    miDtoEscritura.setFechaEscrituracion(fecha);
                    miDtoEscritura.setCuerpo(cuerpo);

                    switch (formularioInvocador)
                    {
                        case ConstantesGui.PREPARAR_ESCRITURA:
                        {
                            try
                            {
                                if (!admin.validarCampoVacio(comboFolioDesde.getSelectedItem().toString())
                                        && !admin.validarCampoVacio(comboFolioHasta.getSelectedItem().toString()))
                                {
                                    Integer folioDesde = Integer.parseInt(comboFolioDesde.getSelectedItem().toString());
                                    Integer folioHasta = Integer.parseInt(comboFolioHasta.getSelectedItem().toString());

                                    if (folioDesde <= folioHasta)
                                    {

                                        miDtoEscritura.setFolios(new ArrayList<DtoFolio>());

                                        for (Iterator<DtoFolio> it = foliosDisponibles.iterator(); it.hasNext();)
                                        {
                                            DtoFolio dtoFolio = it.next();

                                            if (dtoFolio.getNumero() >= folioDesde && dtoFolio.getNumero() <= folioHasta)
                                            {
                                                dtoFolio.setEstado(ConstantesNegocio.ESTADO_FOLIO_UTILIZADO);
                                                miDtoEscritura.getFolios().add(dtoFolio);
                                            }
                                        }

                                        Boolean creada = miController.crearEscritura(miDtoEscritura);

                                        if (creada)
                                        {
                                            //  asigno a la gestion el nuevo estado
                                            miGestionDeEscritura.setEstado(estadoGestionConEscritura);

                                            // modifico el estado de la gestion de escritura.
                                            miController.modificarEstadoDeGestionDeEscritura(miGestionDeEscritura);

                                            // registro en el historial de la gestion, en nuevo cambio de estado
                                            miController.registrarMovimientoHistorial(miGestionDeEscritura);

                                            JOptionPane.showMessageDialog(this, "Ha sido registrada la escritura.", "CONFIRMACION", JOptionPane.INFORMATION_MESSAGE);
                                            salir();
                                        }
                                    } else
                                    {
                                        JOptionPane.showMessageDialog(this, "El Folio Desde no puede ser mayor al Folio Hasta.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                                    }
                                } else
                                {
                                    JOptionPane.showMessageDialog(this, "Debe seleccionar los folios utilizados.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                                }
                            }
                            catch (ClassModifiedException | ClassEliminatedException ex)
                            {
                                JOptionPane.showMessageDialog(this, "La gestion que se esta modificando fue modificada por otro usuario", "Advertencia", JOptionPane.ERROR_MESSAGE);
                                this.salir();
                            }
                            catch (PreexistingEntityException ex)
                            {
                                JOptionPane.showMessageDialog(this, ex.getMessage(), "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                            }
                            break;
                        }
                        case ConstantesGui.MODIFICAR_ESCRITURA:
                        {
                            miDtoEscritura.setFolios(miEscritura.getFolios());
                            miDtoEscritura.setIdEscritura(miEscritura.getIdEscritura());
                            miDtoEscritura.setVersion(miEscritura.getVersion());

                            Boolean modificada;
                            try
                            {
                                Integer folioHasta = folioHastaSeleccionado;

                                if (folioHasta != null)
                                {

                                    Integer folioHastaUtilizado = Integer.parseInt(labelHasta.getText());

                                    for (Iterator<DtoFolio> it = foliosDisponibles.iterator(); it.hasNext();)
                                    {
                                        DtoFolio dtoFolio = it.next();

                                        if (folioHasta != null)
                                        {
                                            if (dtoFolio.getNumero() >= folioHastaUtilizado && dtoFolio.getNumero() <= folioHasta)
                                            {
                                                dtoFolio.setEstado(ConstantesNegocio.ESTADO_FOLIO_UTILIZADO);
                                                miDtoEscritura.getFolios().add(dtoFolio);
                                            }
                                        }
                                    }
                                }

                                modificada = miController.modificarEscritura(miDtoEscritura);

                                if (modificada)
                                {
                                    //  asigno a la gestion el nuevo estado
                                    miGestionDeEscritura.setEstado(estadoGestionConEscritura);

                                    // modifico el estado de la gestion de escritura.
                                    miController.modificarEstadoDeGestionDeEscritura(miGestionDeEscritura);

                                    // registro en el historial de la gestion, en nuevo cambio de estado
                                    miController.registrarMovimientoHistorial(miGestionDeEscritura);

                                    JOptionPane.showMessageDialog(this, "La Escritura ha sido modificada.", "INFORMACION", JOptionPane.INFORMATION_MESSAGE);
                                    salir();
                                }
                                break;

                            }
                            catch (ClassEliminatedException ex)
                            {
                                JOptionPane.showMessageDialog(this, "La Escritura ha sido recientemente eliminada por otro usuario.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                                salir();
                            }
                            catch (ClassModifiedException ex)
                            {
                                JOptionPane.showMessageDialog(this, "La Escritura ha sido recientemente modificada por otro usuario.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                                salir();
                            }
                        }

                    }
                }
                labelValidacion.setText("");

            } else
            {
                JOptionPane.showMessageDialog(this, "Debe completar todo el formulario.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_botonAceptarActionPerformed

    private void campoNumeroEscrituraKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoNumeroEscrituraKeyPressed
        if (!AdministradorValidaciones.getInstancia().validarCampoSoloNumerosEnteros(campoNumeroEscritura.getText()))
        {
            labelValidacion.setText("Numero Escritura debe contener solo numeros enteros.");
        } else
        {
            labelValidacion.setText("");
        }
    }//GEN-LAST:event_campoNumeroEscrituraKeyPressed

    private void comboFolioHastaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboFolioHastaActionPerformed
   }//GEN-LAST:event_comboFolioHastaActionPerformed

    private void comboFolioHastaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_comboFolioHastaMouseClicked
        try
        {
            folioHastaSeleccionado = Integer.parseInt(comboFolioHasta.getSelectedItem().toString());
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "El numero de folio no es valido", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_comboFolioHastaMouseClicked

    private void campoNumeroEscrituraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoNumeroEscrituraActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoNumeroEscrituraActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JButton botonCerrar;
    private javax.swing.JTextArea campoCuerpoEscritura;
    private javax.swing.JTextField campoNumeroEscritura;
    private javax.swing.JComboBox comboFolioDesde;
    private javax.swing.JComboBox comboFolioHasta;
    private javax.swing.JTable grillaListaTramitesGestion;
    private javax.swing.ButtonGroup grupoRadioButtons;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelDesde;
    private javax.swing.JLabel labelDesde1;
    private javax.swing.JLabel labelDesdeDisponible;
    private javax.swing.JLabel labelFoliosDisp;
    private javax.swing.JLabel labelHasta;
    private javax.swing.JLabel labelHasta1;
    private javax.swing.JLabel labelHastaNuevo;
    private javax.swing.JLabel labelInclusive1;
    private javax.swing.JLabel labelInclusiveNuevo;
    private javax.swing.JLabel labelIndicados;
    private javax.swing.JLabel labelValidacion;
    private javax.swing.JPanel panelDetalleEscritura;
    private javax.swing.JRadioButton radioAnulada;
    private javax.swing.JRadioButton radioFirmada;
    private javax.swing.JRadioButton radioNoPaso;
    private javax.swing.JRadioButton radioSinFirmar;
    private com.toedter.calendar.JDateChooser selectorFecha;
    // End of variables declaration//GEN-END:variables
}
