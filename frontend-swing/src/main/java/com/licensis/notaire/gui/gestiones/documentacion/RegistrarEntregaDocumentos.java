/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.gestiones.documentacion;

import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.dto.DtoFlag;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.clientes.BuscarGestionesCliente;
import com.licensis.notaire.gui.gestiones.gestion.BuscarGestion;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.negocio.ControllerNegocio;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author matias
 */
public class RegistrarEntregaDocumentos extends javax.swing.JInternalFrame {

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaRegistrarEntregaDocumentos = new JMenuItem(
            "Ventana Registrar Entrega de Documentos");
    public static RegistrarEntregaDocumentos instancia = null;
    private DtoGestionDeEscritura dtoGestion;
    private boolean documentacionCompleta;

    /**
     * Creates new form RegistrarEntregaDocumentos
     */
    private RegistrarEntregaDocumentos() {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(800, 600);
    }

    private void salir() {
        this.dispose();
    }

    public void limpiarJtable() {
        int i = ((DefaultTableModel) grillaDetalleDocumentos.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaDetalleDocumentos.getModel()).getRowCount() > 0) {
            ((DefaultTableModel) grillaDetalleDocumentos.getModel()).removeRow(i);
            i--;
        }

    }

    public DtoGestionDeEscritura getDtoGestion() {
        return dtoGestion;
    }

    public void setDtoGestion(DtoGestionDeEscritura dtoGestion) {
        this.dtoGestion = dtoGestion;
    }

    public void limpiarFormulario() {
        labelFechaInicio.setText("");
        labelEncabezado.setText("");
        labelEscribano.setText("");
        labelNroGestion.setText("");
        this.botonAceptar.setEnabled(false);
        this.limpiarJtable();
    }

    public static RegistrarEntregaDocumentos getInstancia() {
        if (instancia == null) {
            instancia = new RegistrarEntregaDocumentos();
        }
        return instancia;
    }

    public boolean getDocumentacionCompleta() {
        return documentacionCompleta;
    }

    public void setDocumentacionCompleta(boolean documentacionCompleta) {
        this.documentacionCompleta = documentacionCompleta;
    }

    public static JMenuItem getVentanaRegistrarEntregaDocumentos() {
        return ventanaRegistrarEntregaDocumentos;
    }

    public boolean cargarFormulario(DtoGestionDeEscritura dtoGestionDeEscritura) {
        boolean flag = false;

        this.setDtoGestion(dtoGestionDeEscritura);
        this.limpiarFormulario();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        labelFechaInicio.setText(formatter.format(this.getDtoGestion().getFechaInicio()));
        labelEncabezado.setText(this.getDtoGestion().getEncabezado());
        labelEscribano.setText(this.getDtoGestion().getPersonaEscribano().getNombre() + ", "
                + this.getDtoGestion().getPersonaEscribano().getApellido());
        labelNroGestion.setText(Integer.toString(this.getDtoGestion().getNumero()));

        this.cargarTramites(this.getDtoGestion());

        return flag;
    }

    public void cargarTramites(DtoGestionDeEscritura dtoGestion) {
        Boolean flag = false;
        ArrayList<DtoDocumentoPresentado> listaDocumentos = new ArrayList<>();
        ArrayList<DtoDocumentoPresentado> listaDocumentosNoPresentados = null;
        ArrayList<DtoDocumentoPresentado> listaDocumentosPresentados = null;
        ArrayList<DtoTramite> listaTramites = null;
        String estado = null;

        this.limpiarJtable();

        estado = dtoGestion.getEstado().getNombre();

        dtoGestion = ControllerNegocio.getInstancia().obtenerDocNecesarioEntregadosNoEntregadosDeGestion(dtoGestion);

        listaTramites = (ArrayList<DtoTramite>) dtoGestion.getListaTramitesAsociados();

        for (int i = 0; i < listaTramites.size(); i++) {
            DtoTramite dtoTramite = listaTramites.get(i);
            String nombreTramite = listaTramites.get(i).getTipoDeTramite().getNombre();
            listaDocumentosNoPresentados = (ArrayList<DtoDocumentoPresentado>) listaTramites.get(i)
                    .getListaDocumentosNoPrecentados();
            listaDocumentosPresentados = (ArrayList<DtoDocumentoPresentado>) listaTramites.get(i)
                    .getListaDocumentosGestion();

            flag = cargarGrilla(listaDocumentosPresentados, nombreTramite, dtoTramite);
        }

        if (flag) {
            RegistrarEntregaDocumentos.getInstancia().toFront();
            Principal.getInstancia().cargarFormulario(RegistrarEntregaDocumentos.getInstancia());
        } else {
            JOptionPane.showConfirmDialog(this, "Erro al cargar la documentación", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            RegistrarEntregaDocumentos.getInstancia().dispose();
        }

    }

    public Boolean cargarGrilla(ArrayList<DtoDocumentoPresentado> listaDocumentosGestion, String tipoTramite,
            DtoTramite dtoTramite) {

        boolean flag = false;
        boolean entregado = false;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String fechaVencimiento = null;
        int diasVencimiento = 0;

        if (!listaDocumentosGestion.isEmpty()) {
            this.setDocumentacionCompleta(false);

            for (int i = 0; i < listaDocumentosGestion.size(); i++) {
                // Si es documentacion de cliente se carga.
                if (listaDocumentosGestion.get(i).getQuienEntrega().equals(ConstantesGui.CLIENTE)) {
                    flag = true;

                    DtoDocumentoPresentado dtoDocumentoPresentado = listaDocumentosGestion.get(i);

                    String nombreDocumento = listaDocumentosGestion.get(i).getNombre();

                    if (listaDocumentosGestion.get(i).getDiasVencimiento() != null) {
                        diasVencimiento = listaDocumentosGestion.get(i).getDiasVencimiento();
                    } else {
                        diasVencimiento = 0;
                    }

                    if (listaDocumentosGestion.get(i).getFechaVencimiento() != null) {
                        fechaVencimiento = formatter.format(listaDocumentosGestion.get(i).getFechaVencimiento());
                    } else {
                        fechaVencimiento = null;
                    }

                    entregado = listaDocumentosGestion.get(i).isEntregado();

                    Object[] datos = {
                            tipoTramite,
                            nombreDocumento,
                            diasVencimiento,
                            fechaVencimiento,
                            entregado,
                            dtoTramite,
                            dtoDocumentoPresentado
                    };

                    ((DefaultTableModel) grillaDetalleDocumentos.getModel()).addRow(datos);
                }
            }

            Principal.cargarFormulario(BuscarGestionesCliente.getInstancia());
            this.toFront();
        }

        return flag;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelRegistrarEntregaDocumentos = new javax.swing.JPanel();
        labelTitulo = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        botonBuscarGestion = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaDetalleDocumentos = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        labelNroGestion = new javax.swing.JLabel();
        labelEncabezado = new javax.swing.JLabel();
        labelFechaInicio = new javax.swing.JLabel();
        labelEscribano = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Registrar Entrega de Documentos");
        setPreferredSize(new java.awt.Dimension(800, 600));
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

        panelRegistrarEntregaDocumentos.setPreferredSize(new java.awt.Dimension(800, 600));

        labelTitulo.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        labelTitulo.setText("Registrar Entrega de Documentos Cliente");

        botonAceptar.setText("Aceptar");
        botonAceptar.setEnabled(false);
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

        jLabel2.setText("Buscar Gestión");

        botonBuscarGestion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/buscar.png"))); // NOI18N
        botonBuscarGestion.setText("Buscar");
        botonBuscarGestion.setIconTextGap(10);
        botonBuscarGestion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarGestionActionPerformed(evt);
            }
        });

        jLabel3.setText("Número de Gestión:");

        jLabel6.setText("Encabezado:");

        jLabel4.setText("Fecha de Inicio:");

        jLabel8.setText("Escribano a Cargo:");

        grillaDetalleDocumentos.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {

                },
                new String[] {
                        "Tramite", "Tipo de Documento", "Dias Vencimiento", "Fecha Vencimiento (*)", "Entregado?",
                        "DtoTramite", "DtoDocumentoPresentado"
                }) {
            Class[] types = new Class[] {
                    java.lang.String.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Object.class,
                    java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean[] {
                    false, true, true, true, true, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        grillaDetalleDocumentos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grillaDetalleDocumentosMouseClicked(evt);
            }
        });
        grillaDetalleDocumentos.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                grillaDetalleDocumentosMouseMoved(evt);
            }
        });
        jScrollPane1.setViewportView(grillaDetalleDocumentos);
        grillaDetalleDocumentos.getColumnModel().getColumn(5).setResizable(false);
        grillaDetalleDocumentos.getColumnModel().getColumn(6).setResizable(false);
        grillaDetalleDocumentos.getColumnModel().getColumn(5).setMaxWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(5).setMinWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(5).setMaxWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(5).setMinWidth(0);

        grillaDetalleDocumentos.getColumnModel().getColumn(6).setMaxWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(6).setMinWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(6).setMaxWidth(0);
        grillaDetalleDocumentos.getColumnModel().getColumn(6).setMinWidth(0);

        jLabel9.setText("Detalle de Documentos:");

        labelNroGestion.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        labelNroGestion.setBorder(null);

        labelEncabezado.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        labelEncabezado.setBorder(null);

        labelFechaInicio.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        labelFechaInicio.setBorder(null);

        labelEscribano.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        labelEscribano.setBorder(null);

        jLabel5.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 0, 51));
        jLabel5.setText("(*) Formato Fecha: dd/MM/aaaa");

        javax.swing.GroupLayout panelRegistrarEntregaDocumentosLayout = new javax.swing.GroupLayout(
                panelRegistrarEntregaDocumentos);
        panelRegistrarEntregaDocumentos.setLayout(panelRegistrarEntregaDocumentosLayout);
        panelRegistrarEntregaDocumentosLayout.setHorizontalGroup(
                panelRegistrarEntregaDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelRegistrarEntregaDocumentosLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelRegistrarEntregaDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, 776, Short.MAX_VALUE)
                                        .addGroup(panelRegistrarEntregaDocumentosLayout.createSequentialGroup()
                                                .addGroup(panelRegistrarEntregaDocumentosLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(labelTitulo)
                                                        .addGroup(panelRegistrarEntregaDocumentosLayout
                                                                .createSequentialGroup()
                                                                .addComponent(jLabel2)
                                                                .addGap(34, 34, 34)
                                                                .addComponent(botonBuscarGestion,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(jLabel9)
                                                        .addGroup(panelRegistrarEntregaDocumentosLayout
                                                                .createSequentialGroup()
                                                                .addGroup(panelRegistrarEntregaDocumentosLayout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel3)
                                                                        .addComponent(jLabel6)
                                                                        .addComponent(jLabel4)
                                                                        .addComponent(jLabel8))
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(panelRegistrarEntregaDocumentosLayout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(labelEscribano,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(labelNroGestion,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(labelEncabezado,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(labelFechaInicio,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE))))
                                                .addGap(0, 413, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                panelRegistrarEntregaDocumentosLayout.createSequentialGroup()
                                                        .addComponent(jLabel5)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(botonAceptar,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(28, 28, 28)
                                                        .addComponent(botonCancelar,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        panelRegistrarEntregaDocumentosLayout.setVerticalGroup(
                panelRegistrarEntregaDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelRegistrarEntregaDocumentosLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(labelTitulo)
                                .addGap(18, 18, 18)
                                .addGroup(panelRegistrarEntregaDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(botonBuscarGestion, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(panelRegistrarEntregaDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(labelNroGestion, javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelRegistrarEntregaDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(labelEncabezado, javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelRegistrarEntregaDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(labelFechaInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 23,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(5, 5, 5)
                                .addGroup(panelRegistrarEntregaDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel8)
                                        .addComponent(labelEscribano, javax.swing.GroupLayout.DEFAULT_SIZE, 21,
                                                Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 270,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(panelRegistrarEntregaDocumentosLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5))
                                .addContainerGap()));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelRegistrarEntregaDocumentos, javax.swing.GroupLayout.DEFAULT_SIZE, 788,
                                Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelRegistrarEntregaDocumentos, javax.swing.GroupLayout.DEFAULT_SIZE, 572,
                                Short.MAX_VALUE));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_botonAceptarActionPerformed
    {// GEN-HEADEREND:event_botonAceptarActionPerformed

        int i = JOptionPane.showConfirmDialog(this, "¿Desea confirmar los documentos seleccionados", "Confirmar Salida",
                JOptionPane.YES_NO_OPTION);
        if (i == 0) {
            DtoFlag flag = null;

            try {
                try {
                    flag = this.ingresarDocumentos();
                } catch (ClassModifiedException ex) {
                    Logger.getLogger(RegistrarEntregaDocumentos.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (NonexistentEntityException ex) {
                Logger.getLogger(RegistrarEntregaDocumentos.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (flag.getFlag()) {
                JOptionPane.showMessageDialog(this, "Los documentos fueron Modificados con Exito");
                this.setDtoGestion(ControllerNegocio.getInstancia().buscarDtoGestion(this.getDtoGestion()));

                boolean parcial = ControllerNegocio.getInstancia().documentacionCompletaCliente(this.getDtoGestion());

                if (parcial) {
                    JOptionPane.showMessageDialog(this, "Toda la documentacion de Cliente fue ingresada", "Atencion",
                            JOptionPane.INFORMATION_MESSAGE);

                    boolean completa = ControllerNegocio.getInstancia().iscompletaDocumentacion(dtoGestion);

                    if (completa) {
                        JOptionPane.showMessageDialog(this, "La Gestion se encuentra en estado: Documentacion Completa",
                                "Atencion", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                this.limpiarFormulario();
                this.cargarFormulario(this.getDtoGestion());
            } else {
                JOptionPane.showMessageDialog(this, "Los documentos no se modificaron", "Advertencia",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            this.cargarFormulario(this.getDtoGestion());

        } else {
            this.toFront();
        }

    }// GEN-LAST:event_botonAceptarActionPerformed

    public DtoFlag ingresarDocumentos() throws NonexistentEntityException, ClassModifiedException {

        DtoFlag dtoFlag = new DtoFlag();
        Integer filaSeleccionada = this.grillaDetalleDocumentos.getSelectedRow();
        TableModel miGrilla = this.grillaDetalleDocumentos.getModel();
        boolean flag = false;
        int filas = miGrilla.getRowCount();
        int columnas = miGrilla.getColumnCount();
        int cont = 0;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date fechaVencimiento = null;

        DtoTramite dtoTramite = null;
        dtoFlag.setFlag(false);

        ArrayList<DtoDocumentoPresentado> lisDtoDocumentosPresentados = new ArrayList<>();
        DtoDocumentoPresentado dtoDocumentoPresentado = null;

        // Recorro la grilla completa, buscando la gestion seleccionada
        for (int i = 0; i < filas; i++) {
            dtoDocumentoPresentado = (DtoDocumentoPresentado) grillaDetalleDocumentos.getValueAt(i, 6);

            dtoDocumentoPresentado.setFechaIngreso(new Date());

            String nombreTipoDocumento = (String) grillaDetalleDocumentos.getValueAt(i, 1);

            dtoTramite = (DtoTramite) grillaDetalleDocumentos.getValueAt(i, 5);

            // Tramite que pertenece
            dtoDocumentoPresentado.setFkTramite(dtoTramite);

            // Nombre Documento
            dtoDocumentoPresentado.setNombre(nombreTipoDocumento);

            // Quien entrega
            dtoDocumentoPresentado.setQuienEntrega(ConstantesGui.CLIENTE);

            // Entregado
            dtoDocumentoPresentado.setEntregado((boolean) grillaDetalleDocumentos.getValueAt(i, 4));

            // fecha de vencimiento
            String fecha = null;
            try {
                // dias vencimiento
                Integer diasVencimiento = (Integer) grillaDetalleDocumentos.getValueAt(i, 2);
                dtoDocumentoPresentado.setDiasVencimiento(diasVencimiento);

                fecha = (String) grillaDetalleDocumentos.getValueAt(i, 3);
                if (grillaDetalleDocumentos.getValueAt(i, 3) != null && !fecha.isEmpty()) {
                    // fecha vencimiento
                    fechaVencimiento = dateFormat.parse((String) miGrilla.getValueAt(i, 3));
                    dtoDocumentoPresentado.setFechaVencimiento(fechaVencimiento);

                } else {
                    fechaVencimiento = null;
                    dtoDocumentoPresentado.setFechaVencimiento(fechaVencimiento);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Algun campo es incorrecto", "Advertencia",
                        JOptionPane.WARNING_MESSAGE);
                return dtoFlag;
            }
            lisDtoDocumentosPresentados.add(dtoDocumentoPresentado);
            cont++;

        }
        if (cont > 0) {
            try {
                dtoFlag = ControllerNegocio.getInstancia().modificarDocumentacion(lisDtoDocumentosPresentados,
                        this.getDtoGestion());
            } catch (Exception ex) {
                String mensaje = "Error al modificar documentacion: " + ex.getMessage();
                JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            dtoFlag.setFlag(false);
        }

        return dtoFlag;
    }

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_botonCancelarActionPerformed
    {// GEN-HEADEREND:event_botonCancelarActionPerformed
        salir();
    }// GEN-LAST:event_botonCancelarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)// GEN-FIRST:event_formInternalFrameClosed
    {// GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaRegistrarEntregaDocumentos);
        Principal.eliminarFormulario(this);
    }// GEN-LAST:event_formInternalFrameClosed

    private void botonBuscarGestionActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_botonBuscarGestionActionPerformed
    {// GEN-HEADEREND:event_botonBuscarGestionActionPerformed

        BuscarGestion formBuscarGestion = new BuscarGestion();
        formBuscarGestion.setTipoBusqueda(ConstantesGui.DOCUMENTACION_INGRESO);

        Principal.cargarFormulario(formBuscarGestion);
        Principal.setVentanasActivas(BuscarGestion.getVentanaBuscarGestion());
    }// GEN-LAST:event_botonBuscarGestionActionPerformed

    private void grillaDetalleDocumentosMouseMoved(java.awt.event.MouseEvent evt)// GEN-FIRST:event_grillaDetalleDocumentosMouseMoved
    {// GEN-HEADEREND:event_grillaDetalleDocumentosMouseMoved
     // TODO add your handling code here:
    }// GEN-LAST:event_grillaDetalleDocumentosMouseMoved

    private void grillaDetalleDocumentosMouseClicked(java.awt.event.MouseEvent evt)// GEN-FIRST:event_grillaDetalleDocumentosMouseClicked
    {// GEN-HEADEREND:event_grillaDetalleDocumentosMouseClicked
        this.botonAceptar.setEnabled(true);
    }// GEN-LAST:event_grillaDetalleDocumentosMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonBuscarGestion;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JTable grillaDetalleDocumentos;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelEncabezado;
    private javax.swing.JLabel labelEscribano;
    private javax.swing.JLabel labelFechaInicio;
    private javax.swing.JLabel labelNroGestion;
    private javax.swing.JLabel labelTitulo;
    private javax.swing.JPanel panelRegistrarEntregaDocumentos;
    // End of variables declaration//GEN-END:variables
}
