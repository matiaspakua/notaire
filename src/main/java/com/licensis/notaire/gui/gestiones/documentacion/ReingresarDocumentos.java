/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.gestiones.documentacion;

import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.dto.DtoFlag;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoTipoDeDocumento;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.clientes.BuscarGestionesCliente;
import com.licensis.notaire.gui.gestiones.gestion.BuscarGestion;
import java.awt.event.MouseEvent;
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
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.negocio.ControllerNegocio;

/**
 *
 * @author matias
 */
public class ReingresarDocumentos extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaReingresarDocumento = new JMenuItem("Ventana Reingresar Documento");
    private static ReingresarDocumentos instancia = null;
    private static DtoGestionDeEscritura dtoGestion = null;
    private ControllerNegocio miController;
    private String error = null;

    /**
     * Creates new form ReingresarDocumentos
     */
    public ReingresarDocumentos()
    {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(800, 600);
        miController = ControllerNegocio.getInstancia();
    }

    private static DtoGestionDeEscritura getDtoGestion()
    {
        return dtoGestion;
    }

    public static ReingresarDocumentos getInstancia()
    {
        if (instancia == null)
        {
            instancia = new ReingresarDocumentos();
        }
        return instancia;
    }

    public static void setDtoGestion(DtoGestionDeEscritura dtoGestion)
    {
        ReingresarDocumentos.dtoGestion = dtoGestion;
    }

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    private void salir()
    {
        this.dispose();
    }

    public static JMenuItem getVentanaReingresarDocumento()
    {
        return ventanaReingresarDocumento;
    }

    public boolean cargarFormulario(DtoGestionDeEscritura dtoGestionDeEscritura) throws NonexistentJpaException
    {
        boolean flag = false;

        this.setDtoGestion(dtoGestionDeEscritura);

        this.limpiarForm();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");


        fechaInicio.setText(formatter.format(this.getDtoGestion().getFechaInicio()));
        labelEncabezado.setText(this.getDtoGestion().getEncabezado());
        escribanoAcargo.setText(this.getDtoGestion().getPersonaEscribano().getNombre());
        nroGestion.setText(Integer.toString(this.getDtoGestion().getNumero()));
        //Hay que saber si tiene escrituras

        this.cargarTramites(this.getDtoGestion());

        return flag;
    }

    public void limpiarForm()
    {
        fechaInicio.setText("");
        labelEncabezado.setText("");
        escribanoAcargo.setText("");
        nroGestion.setText("");

        this.limpiarJtable();
    }

    public void limpiarJtable()
    {
        int i = ((DefaultTableModel) gillaDetalleDocumentos.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) gillaDetalleDocumentos.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) gillaDetalleDocumentos.getModel()).removeRow(i);
            i--;
        }

    }

    public void cargarTramites(DtoGestionDeEscritura dtoGestion) throws NonexistentJpaException
    {

        Boolean flag = false;
        ArrayList<DtoDocumentoPresentado> listaDocumentosPresentados = null;
        ArrayList<DtoTramite> listaTramites = null;
        String estado = null;
        int cont = 0;
        this.limpiarJtable();

        estado = dtoGestion.getEstado().getNombre();

        dtoGestion = miController.obtenerDocNecesarioEntregadosNoEntregadosDeGestion(dtoGestion);

        listaTramites = (ArrayList<DtoTramite>) dtoGestion.getListaTramitesAsociados();

        for (int i = 0; i < listaTramites.size(); i++)
        {
            DtoTramite dtoTramite = listaTramites.get(i);
            String nombreTramite = listaTramites.get(i).getTipoDeTramite().getNombre();
            listaDocumentosPresentados = (ArrayList<DtoDocumentoPresentado>) listaTramites.get(i).getListaDocumentosGestion();

            if (!listaDocumentosPresentados.isEmpty())
            {
                flag = cargarGrilla(listaDocumentosPresentados, nombreTramite, dtoTramite);
            }
        }
        if (flag)
        {
            ReingresarDocumentos.getInstancia().toFront();
            Principal.cargarFormulario(ReingresarDocumentos.getInstancia());
        } else
        {
              this.dispose();
            JOptionPane.showMessageDialog(this, "No hay documentos presentados, para la gestion");
        }

    }

    public boolean cargarGrilla(ArrayList<DtoDocumentoPresentado> listaDocumentosPresentados, String tipoTramite, DtoTramite dtoTramite)
    {

        DtoDocumentoPresentado dtoDocumentoPresentado = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String fechaReingreso = null;
        boolean reingresado = false;
        boolean flag = false;

           for (int i = 0; i < listaDocumentosPresentados.size(); i++)
            {

                dtoDocumentoPresentado = listaDocumentosPresentados.get(i);

                if (dtoDocumentoPresentado.getQuienEntrega().equals(ConstantesGui.DOCUMENTACION_ENTIDAD_EXTERNA)
                        && dtoDocumentoPresentado.isEntregado())
                {

                    flag = true;

                    if (dtoDocumentoPresentado.getFechaIngreso() != null)
                    {
                        fechaReingreso = formatter.format(dtoDocumentoPresentado.getFechaIngreso());
                    } else
                    {
                        fechaReingreso = "";
                    }

                    reingresado = dtoDocumentoPresentado.isReingresado();


                    Object[] datos =
                    {
                        tipoTramite,
                        dtoDocumentoPresentado.getNombre(),
                        reingresado,
                        fechaReingreso,
                        dtoDocumentoPresentado,
                        dtoTramite,
                    };

                    ((DefaultTableModel) gillaDetalleDocumentos.getModel()).addRow(datos);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelReingresarDocumentos = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        botonBuscarGestion = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        nroGestion = new javax.swing.JLabel();
        labelEncabezado = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        fechaInicio = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        escribanoAcargo = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        gillaDetalleDocumentos = new javax.swing.JTable()
        {
            //Implement table cell tool tips.
            public String getToolTipText(MouseEvent e)
            {
                String tip = null;

                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);

                if (realColumnIndex == 3) { //Sport column
                    tip = "El formato de fecha es: "
                    + "01/01/2012 ";
                }
                return tip;
            }
        };
        jLabel7 = new javax.swing.JLabel();

        setClosable(true);
        setMaximizable(true);
        setTitle("Reingresar Documentos");
        setPreferredSize(new java.awt.Dimension(912, 508));
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

        panelReingresarDocumentos.setPreferredSize(new java.awt.Dimension(800, 600));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Reingresar Documentos Externos");

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

        jLabel9.setText("Detalle de Documentos:");

        jLabel3.setText("Número de Gestión:");

        nroGestion.setText("label");
        nroGestion.setBorder(null);

        labelEncabezado.setText("ilabel");
        labelEncabezado.setBorder(null);

        jLabel6.setText("Encabezado:");

        jLabel5.setText("Fecha de Inicio:");

        fechaInicio.setText("jLabel5");
        fechaInicio.setBorder(null);

        jLabel8.setText("Escribano a Cargo:");

        escribanoAcargo.setText("jLabel5");
        escribanoAcargo.setBorder(null);

        gillaDetalleDocumentos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tipo de Tramite", "Tipo de Documento", "Reingresado", "Fecha (*)", "DtoDocumentoPresentado", "DtoTramite"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        gillaDetalleDocumentos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gillaDetalleDocumentosMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(gillaDetalleDocumentos);
        gillaDetalleDocumentos.getColumnModel().getColumn(4).setMaxWidth(0);
        gillaDetalleDocumentos.getColumnModel().getColumn(4).setMinWidth(0);
        gillaDetalleDocumentos.getColumnModel().getColumn(4).setMaxWidth(0);
        gillaDetalleDocumentos.getColumnModel().getColumn(4).setMinWidth(0);

        gillaDetalleDocumentos.getColumnModel().getColumn(5).setMaxWidth(0);
        gillaDetalleDocumentos.getColumnModel().getColumn(5).setMinWidth(0);
        gillaDetalleDocumentos.getColumnModel().getColumn(5).setMaxWidth(0);
        gillaDetalleDocumentos.getColumnModel().getColumn(5).setMinWidth(0);

        jLabel7.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 0, 51));
        jLabel7.setText("(*) Formato Fecha: dd/MM/aaaa");

        javax.swing.GroupLayout panelReingresarDocumentosLayout = new javax.swing.GroupLayout(panelReingresarDocumentos);
        panelReingresarDocumentos.setLayout(panelReingresarDocumentosLayout);
        panelReingresarDocumentosLayout.setHorizontalGroup(
            panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelReingresarDocumentosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelReingresarDocumentosLayout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelReingresarDocumentosLayout.createSequentialGroup()
                        .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelReingresarDocumentosLayout.createSequentialGroup()
                                .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel9)
                                    .addGroup(panelReingresarDocumentosLayout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addGap(32, 32, 32)
                                        .addComponent(botonBuscarGestion, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(panelReingresarDocumentosLayout.createSequentialGroup()
                                        .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel6)
                                            .addComponent(jLabel5))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(nroGestion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(labelEncabezado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(fechaInicio, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)))
                                    .addGroup(panelReingresarDocumentosLayout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addGap(18, 18, 18)
                                        .addComponent(escribanoAcargo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 888, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        panelReingresarDocumentosLayout.setVerticalGroup(
            panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelReingresarDocumentosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(botonBuscarGestion, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nroGestion, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(labelEncabezado, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(fechaInicio))
                .addGap(5, 5, 5)
                .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(escribanoAcargo))
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addGap(24, 24, 24)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelReingresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelReingresarDocumentos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelReingresarDocumentos, javax.swing.GroupLayout.PREFERRED_SIZE, 587, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public DtoFlag modificarDocumentacionPresentada()
    {

        DtoFlag dtoFlag = new DtoFlag();
        TableModel miGrilla = gillaDetalleDocumentos.getModel();
        int filaSeleccionada = gillaDetalleDocumentos.getSelectedRow();
        boolean flag = false;
        int filas = miGrilla.getRowCount();
        int columnas = miGrilla.getColumnCount();
        int cont = 0;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Boolean reingresado = false;

        DtoTramite dtoTramite = null;
        DtoTipoDeDocumento dtoTipoDeDocumento = null;
        dtoFlag.setFlag(false);

        ArrayList<DtoDocumentoPresentado> lisDtoDocumentosPresentados = new ArrayList<>();
        DtoDocumentoPresentado dtoDocumentoPresentado = null;


        for (int i = 0; i < filas; i++)
        {
            try
            {
                dtoDocumentoPresentado = (DtoDocumentoPresentado) miGrilla.getValueAt(i, 4);

                this.setError(ConstantesGui.CAMPO_FECHA_REINGRESO);
                String fecha = (String) miGrilla.getValueAt(i, 3);

                if (miGrilla.getValueAt(i, 3) != null && !fecha.isEmpty())
                {
                    Date fechaReingreso = dateFormat.parse((String) miGrilla.getValueAt(i, 3));
                    dtoDocumentoPresentado.setFechaIngreso(fechaReingreso);
                } else
                {
                    dtoDocumentoPresentado.setFechaIngreso(null);
                }

                dtoTramite = (DtoTramite) miGrilla.getValueAt(i, 5);

                dtoDocumentoPresentado.setFkTramite(dtoTramite);

                dtoDocumentoPresentado.setNombre(dtoDocumentoPresentado.getNombre());

                reingresado = (boolean) miGrilla.getValueAt(i, 2);

                dtoDocumentoPresentado.setReingresado(reingresado);

                lisDtoDocumentosPresentados.add(dtoDocumentoPresentado);

                cont++;

            } catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, "Algun campo es incorrecto " + this.getError() + " fila nro: " + i, "Advertencia", JOptionPane.WARNING_MESSAGE);
                return dtoFlag;
            }
        }

        if (cont > 0)
        {
            try
            {
                try
                {
                    dtoFlag = miController.modificarDocumentacionReingreso(lisDtoDocumentosPresentados, this.getDtoGestion());
                } catch (NonexistentEntityException ex)
                {
                    Logger.getLogger(ReingresarDocumentos.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassModifiedException ex)
                {
                    String mensaje = "Documentacion modificada con anterioridad - Accion Cancelada";
                    JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
                    this.salir();
                }
                this.cargarFormulario(this.getDtoGestion());
            } catch (NonexistentJpaException ex)
            {
                Logger.getLogger(ReingresarDocumentos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return dtoFlag;
    }

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaReingresarDocumento);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonBuscarGestionActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonBuscarGestionActionPerformed
    {//GEN-HEADEREND:event_botonBuscarGestionActionPerformed
        BuscarGestion formBuscarGestion = new BuscarGestion();
        formBuscarGestion.setTipoBusqueda(ConstantesGui.DOCUMENTACION_REINGRESO);

        Principal.cargarFormulario(formBuscarGestion);
        Principal.setVentanasActivas(BuscarGestion.getVentanaBuscarGestion());
    }//GEN-LAST:event_botonBuscarGestionActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCancelarActionPerformed
    {//GEN-HEADEREND:event_botonCancelarActionPerformed
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonAceptarActionPerformed
    {//GEN-HEADEREND:event_botonAceptarActionPerformed
        int i = JOptionPane.showConfirmDialog(this, "Desea confirmar los documentos seleccionados", "Confirmar Salida", JOptionPane.YES_NO_OPTION);

        //Hacer controles de grilla, y revisar grilla RegistrarDomentacion
        if (i == 0)
        {
            DtoFlag flag = this.modificarDocumentacionPresentada();

            if (flag.getFlag())
            {
                try
                {

                    JOptionPane.showMessageDialog(this, "Los documentos fueron Modificados Correctamente", "Informacion", JOptionPane.INFORMATION_MESSAGE);
                    this.setDtoGestion(miController.buscarDtoGestion(this.getDtoGestion()));
                    this.cargarFormulario(this.getDtoGestion());

                } catch (NonexistentJpaException ex)
                {
                    Logger.getLogger(ReingresarDocumentos.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else
            {
                JOptionPane.showMessageDialog(this, "Los documentos no se modificaron", "Advertencia", JOptionPane.INFORMATION_MESSAGE);
            }

        } else
        {
            this.toFront();
        }
    }//GEN-LAST:event_botonAceptarActionPerformed

    private void gillaDetalleDocumentosMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_gillaDetalleDocumentosMouseClicked
    {//GEN-HEADEREND:event_gillaDetalleDocumentosMouseClicked
             this.botonAceptar.setEnabled(true);
    }//GEN-LAST:event_gillaDetalleDocumentosMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonBuscarGestion;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JLabel escribanoAcargo;
    private javax.swing.JLabel fechaInicio;
    private javax.swing.JTable gillaDetalleDocumentos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelEncabezado;
    private javax.swing.JLabel nroGestion;
    private javax.swing.JPanel panelReingresarDocumentos;
    // End of variables declaration//GEN-END:variables
}
