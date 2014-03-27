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
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
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
import org.hibernate.StaleObjectStateException;

/**
 *
 * @author matias
 */
public class IngresarDocumento extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaIngresarDocumento = new JMenuItem("Ventana Ingresar Documento");
    private static IngresarDocumento instancia = null;
    private DtoGestionDeEscritura dtoGestion = null;
    private ControllerNegocio miController;
    private String error = null;

    /**
     * Creates new form IngresarDocumento
     */
    private IngresarDocumento()
    {
        initComponents();
        estadoFormulario = Boolean.TRUE;

        miController = ControllerNegocio.getInstancia();
    }

    public static IngresarDocumento getInstancia()
    {
        if (instancia == null)
        {
            instancia = new IngresarDocumento();
        }
        instancia.setSize(1150, 600);
        return instancia;
    }

    public DtoGestionDeEscritura getDtoGestion()
    {
        return dtoGestion;
    }

    public void setDtoGestion(DtoGestionDeEscritura DtoGestion)
    {
        this.dtoGestion = DtoGestion;
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

    public static JMenuItem getVentanaIngresarDocumento()
    {
        return ventanaIngresarDocumento;
    }

    public boolean cargarFormulario(DtoGestionDeEscritura dtoGestionDeEscritura) throws NonexistentJpaException
    {
        boolean flag = false;

        this.setDtoGestion(dtoGestionDeEscritura);

        this.limpiarFormulario();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");


        fechaInicio.setText(formatter.format(this.getDtoGestion().getFechaInicio()));
        labelEncabezado.setText(this.getDtoGestion().getEncabezado());
        escribanoAcargo.setText(this.getDtoGestion().getPersonaEscribano().getNombre());
        nroGestion.setText(Integer.toString(this.getDtoGestion().getNumero()));

        NomenclaturaCatastral.getInstancia().cargarGrilla(this.getDtoGestion().getListaTramitesAsociados());

        this.cargarTramites(this.getDtoGestion());

        return flag;
    }

    public void limpiarFormulario()
    {
        fechaInicio.setText("");
        labelEncabezado.setText("");
        escribanoAcargo.setText("");
        nroGestion.setText("");
        botonNomenclaturas.setEnabled(false);

        this.limpiarJtable();
    }

    public void limpiarJtable()
    {
        int i = ((DefaultTableModel) grillaMovimientoDocumentacion.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaMovimientoDocumentacion.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaMovimientoDocumentacion.getModel()).removeRow(i);
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
                cargarGrilla(listaDocumentosPresentados, nombreTramite, dtoTramite);
                cont++;
            }

        }
        if (cont > 0)
        {
            IngresarDocumento.getInstancia().toFront();
            Principal.cargarFormulario(IngresarDocumento.getInstancia());
        } else
        {
            JOptionPane.showMessageDialog(this, "No hay Documentacion de Entidades externas registrados", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }


    }

    public void cargarGrilla(ArrayList<DtoDocumentoPresentado> listaDocumentosPresentados, String tipoTramite, DtoTramite dtoTramite)
    {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        DtoDocumentoPresentado dtoDocumentoPresentado = null;

        String fechaPago = null;
        String fechaIngreso = null;
        String fechaLiberado = null;
        String fechaSalida = null;
        String fechaVencimiento = null;

        if (!listaDocumentosPresentados.isEmpty())
        {
            for (int i = 0; i < listaDocumentosPresentados.size(); i++)
            {
                String entrega = listaDocumentosPresentados.get(i).getQuienEntrega();

                if (entrega.equals(ConstantesGui.DOCUMENTACION_ENTIDAD_EXTERNA))
                {

                    dtoDocumentoPresentado = listaDocumentosPresentados.get(i);
                    //     DtoTipoDeDocumento dtoTipoDocumento = dtoDocumentoPresentado.getFkTipoDeDocumento();

                    if (dtoDocumentoPresentado.getFechaIngreso() != null)
                    {
                        fechaIngreso = formatter.format(dtoDocumentoPresentado.getFechaIngreso());
                    } else
                    {
                        fechaIngreso = null;
                    }
                    if (dtoDocumentoPresentado.getFechaLiberado() != null)
                    {
                        fechaLiberado = formatter.format(dtoDocumentoPresentado.getFechaLiberado());
                    } else
                    {
                        fechaLiberado = null;
                    }
                    if (dtoDocumentoPresentado.getFechaPago() != null)
                    {
                        fechaPago = formatter.format(dtoDocumentoPresentado.getFechaPago());
                    } else
                    {
                        fechaPago = null;
                    }
                    if (dtoDocumentoPresentado.getFechaSalida() != null)
                    {
                        fechaSalida = formatter.format(dtoDocumentoPresentado.getFechaSalida());
                    } else
                    {
                        fechaSalida = null;
                    }
                    if (dtoDocumentoPresentado.getFechaVencimiento() != null)
                    {
                        fechaVencimiento = formatter.format(dtoDocumentoPresentado.getFechaVencimiento());
                    } else
                    {
                        fechaVencimiento = null;
                    }

                    Object[] datos =
                    {
                        dtoDocumentoPresentado.getNombre(),
                        dtoDocumentoPresentado.isPreparado(),
                        dtoDocumentoPresentado.getNumeroCarton(),
                        fechaIngreso,
                        fechaSalida,
                        dtoDocumentoPresentado.isObservado(),
                        dtoDocumentoPresentado.getImporteAPagar(),
                        fechaPago,
                        dtoDocumentoPresentado.isLiberado(),
                        fechaLiberado,
                        dtoDocumentoPresentado.getObservaciones(),
                        dtoDocumentoPresentado,
                        dtoTramite,
                        dtoDocumentoPresentado.getDiasVencimiento(),
                        fechaVencimiento,                        
                    };

                    ((DefaultTableModel) grillaMovimientoDocumentacion.getModel()).addRow(datos);
                }
            }

            Principal.cargarFormulario(BuscarGestionesCliente.getInstancia());
            this.toFront();
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        panelIngresarDocumentos = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        botonBuscarGestion = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaMovimientoDocumentacion = new javax.swing.JTable()
        {

            //Implement table cell tool tips.
            public String getToolTipText(MouseEvent e)
            {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);

                if (realColumnIndex == 3 ||realColumnIndex == 4 ||realColumnIndex == 7||realColumnIndex == 9
                    || realColumnIndex == 14) { //Sport column
                    tip = "El formato de fecha es: "
                    + "01/01/2012 ";
                }

                return tip;
            }

        };
        nroGestion = new javax.swing.JLabel();
        labelEncabezado = new javax.swing.JLabel();
        fechaInicio = new javax.swing.JLabel();
        escribanoAcargo = new javax.swing.JLabel();
        botonNomenclaturas = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Ingresar Documento");
        setPreferredSize(new java.awt.Dimension(900, 600));
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
        jLabel1.setText("Registrar Movimientos de Documentación Entidades Externas");

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

        jLabel2.setText("Buscar Gestion");

        botonBuscarGestion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/licensis/notaire/gui/iconos/buscar.png"))); // NOI18N
        botonBuscarGestion.setText("Buscar");
        botonBuscarGestion.setIconTextGap(10);
        botonBuscarGestion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarGestionActionPerformed(evt);
            }
        });

        jLabel3.setText("Numero de Gestion:");

        jLabel4.setText("Encabezado:");

        jLabel7.setText("Escribano a Cargo:");

        jLabel5.setText("Fecha de Inicio:");

        grillaMovimientoDocumentacion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String []
            {
                "Tipo Documento", "Preparado?", "Número Cartón", "Fecha Ingreso", "Fecha Salida", "Observado", "Monto Deuda", "Fecha Pago", "Liberado", "Fecha Liberación", "Observaciones", "DtoDocumentoPresentado", "DtoTramite", "Dias Vencimiento", "Fecha Vencimiento"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Float.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true, true, true, true, true, true, true, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        grillaMovimientoDocumentacion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grillaMovimientoDocumentacionMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(grillaMovimientoDocumentacion);
        grillaMovimientoDocumentacion.getColumnModel().getColumn(0).setPreferredWidth(175);
        grillaMovimientoDocumentacion.getColumnModel().getColumn(11).setMaxWidth(0);
        grillaMovimientoDocumentacion.getColumnModel().getColumn(11).setMinWidth(0);
        grillaMovimientoDocumentacion.getColumnModel().getColumn(11).setMaxWidth(0);
        grillaMovimientoDocumentacion.getColumnModel().getColumn(11).setMinWidth(0);
        grillaMovimientoDocumentacion.getColumnModel().getColumn(12).setMaxWidth(0);
        grillaMovimientoDocumentacion.getColumnModel().getColumn(12).setMinWidth(0);
        grillaMovimientoDocumentacion.getColumnModel().getColumn(12).setMaxWidth(0);
        grillaMovimientoDocumentacion.getColumnModel().getColumn(12).setMinWidth(0);

        nroGestion.setText("jLabel6");

        labelEncabezado.setText("jLabel8");

        fechaInicio.setText("jLabel10");

        escribanoAcargo.setText("jLabel11");

        botonNomenclaturas.setText("Ver Nomenclatura Catastral");
        botonNomenclaturas.setEnabled(false);
        botonNomenclaturas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonNomenclaturasActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 0, 51));
        jLabel6.setText("(*) Formato Fecha: dd/MM/aaaa");

        javax.swing.GroupLayout panelIngresarDocumentosLayout = new javax.swing.GroupLayout(panelIngresarDocumentos);
        panelIngresarDocumentos.setLayout(panelIngresarDocumentosLayout);
        panelIngresarDocumentosLayout.setHorizontalGroup(
            panelIngresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIngresarDocumentosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelIngresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(panelIngresarDocumentosLayout.createSequentialGroup()
                        .addGroup(panelIngresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(panelIngresarDocumentosLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nroGestion))
                            .addGroup(panelIngresarDocumentosLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelEncabezado))
                            .addGroup(panelIngresarDocumentosLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(34, 34, 34)
                                .addComponent(botonBuscarGestion, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelIngresarDocumentosLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(escribanoAcargo))
                            .addGroup(panelIngresarDocumentosLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fechaInicio))
                            .addComponent(botonNomenclaturas, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelIngresarDocumentosLayout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 441, Short.MAX_VALUE)
                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelIngresarDocumentosLayout.setVerticalGroup(
            panelIngresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIngresarDocumentosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(panelIngresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(botonBuscarGestion, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelIngresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(nroGestion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIngresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(labelEncabezado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIngresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(fechaInicio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIngresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(escribanoAcargo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botonNomenclaturas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIngresarDocumentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap())
        );

        jScrollPane2.setViewportView(panelIngresarDocumentos);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 888, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonAceptarActionPerformed
    {//GEN-HEADEREND:event_botonAceptarActionPerformed
        int i = JOptionPane.showConfirmDialog(this, "Desea confirmar los documentos seleccionados", "Confirmar Salida", JOptionPane.YES_NO_OPTION);

        //Hacer controles de grilla, y revisar grilla RegistrarDomentacion
        if (i == 0)
        {
            try
            {
                DtoFlag flag = this.modificarDocumentacion();
                if (flag.getFlag())
                {
                    JOptionPane.showMessageDialog(this, "Los documentos fueron actualizados correctamente", "Infomacion", JOptionPane.INFORMATION_MESSAGE);
                    this.setDtoGestion(miController.buscarDtoGestion(this.getDtoGestion()));

                    boolean parcial = ControllerNegocio.getInstancia().documentacionCompletaExterna(this.getDtoGestion());

                    if (parcial)
                    {
                        JOptionPane.showMessageDialog(this, "Toda la documentacion del Externa fue ingresada", "Atencion", JOptionPane.INFORMATION_MESSAGE);

                        boolean completa = ControllerNegocio.getInstancia().iscompletaDocumentacion(dtoGestion);

                        if (completa)
                        {
                            JOptionPane.showMessageDialog(this, "La Gestion se encuentra en estado: Documentacion Completa", "Atencion", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }

                      try
                        {
                            this.limpiarFormulario(); 
                            this.cargarFormulario(this.getDtoGestion());
                        } catch (NonexistentJpaException ex)
                        {
                            Logger.getLogger(ConsultarDeudasDocumentos.class.getName()).log(Level.SEVERE, null, ex);
                        }

                } else
                {
                    JOptionPane.showMessageDialog(this, "Los documentos no se modificaron", "Advertencia", JOptionPane.INFORMATION_MESSAGE);

                }
            } catch (NonexistentEntityException ex)
            {
                Logger.getLogger(IngresarDocumento.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
        {
            this.toFront();
        }
    }//GEN-LAST:event_botonAceptarActionPerformed

    public DtoFlag modificarDocumentacion() throws NonexistentEntityException
    {

        DtoFlag dtoFlag = new DtoFlag();
        TableModel miGrilla = grillaMovimientoDocumentacion.getModel();
        int filaSeleccionada = grillaMovimientoDocumentacion.getSelectedRow();
        boolean flag = false;
        int filas = miGrilla.getRowCount();
        int columnas = miGrilla.getColumnCount();
        int cont = 0;
        Date fechaPago = null;
        Date fechaIngreso = null;
        Date fechaLiberado = null;
        Date fechaSalida = null;
        Date fechaVencimiento = null;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dtoFlag.setFlag(false);
         int fila = 0;

        DtoTramite dtoTramite = null;
        DtoTipoDeDocumento dtoTipoDeDocumento = null;

        ArrayList<DtoDocumentoPresentado> lisDtoDocumentosPresentados = new ArrayList<>();
        DtoDocumentoPresentado dtoDocumentoPresentado = null;

        try
        {
            for (int i = 0; i < filas; i++)
            {
                 fila = i +1 ;
                
                dtoDocumentoPresentado = (DtoDocumentoPresentado) miGrilla.getValueAt(i, 11);

                dtoTramite = (DtoTramite) miGrilla.getValueAt(i, 12);

                dtoDocumentoPresentado.setFkTramite(dtoTramite);

                //Fecha ingreso
                String fecha = (String) miGrilla.getValueAt(i, 3);

                if (miGrilla.getValueAt(i, 3) != null && !fecha.isEmpty())
                {
                    this.setError(ConstantesGui.CAMPO_FECHA_INGRESO);
                    fechaIngreso = dateFormat.parse((String) miGrilla.getValueAt(i, 3));
                    dtoDocumentoPresentado.setFechaIngreso(fechaIngreso);
                    dtoDocumentoPresentado.setEntregado(true);
                } else
                {
                    dtoDocumentoPresentado.setFechaIngreso(null);
                    dtoDocumentoPresentado.setEntregado(false);
                }
                //Fecha Salida
                fecha = (String) miGrilla.getValueAt(i, 4);

                if (miGrilla.getValueAt(i, 4) != null && !fecha.isEmpty())
                {
                    this.setError(ConstantesGui.CAMPO_FECHA_SALIDA);
                    fechaSalida = dateFormat.parse((String) miGrilla.getValueAt(i, 4));
                    dtoDocumentoPresentado.setFechaSalida(fechaSalida);
                } else
                {
                    dtoDocumentoPresentado.setFechaSalida(null);
                }
                //Fecha Pago
                fecha = (String) miGrilla.getValueAt(i, 7);

                if (miGrilla.getValueAt(i, 7) != null && !fecha.isEmpty())
                {
                    this.setError(ConstantesGui.CAMPO_FECHA_PAGO);
                    fechaPago = dateFormat.parse((String) miGrilla.getValueAt(i, 7));
                    dtoDocumentoPresentado.setFechaPago(fechaPago);
                } else
                {
                    dtoDocumentoPresentado.setFechaPago(null);
                }
                //Fecha Liberado
                fecha = (String) miGrilla.getValueAt(i, 9);

                if (miGrilla.getValueAt(i, 9) != null && !fecha.isEmpty())
                {
                    this.setError(ConstantesGui.CAMPO_FECHA_lIBERACION);
                    fechaLiberado = dateFormat.parse((String) miGrilla.getValueAt(i, 9));
                    dtoDocumentoPresentado.setFechaLiberado(fechaLiberado);
                } else
                {
                    dtoDocumentoPresentado.setFechaLiberado(null);
                }
                  //Fecha Vencimiento
                fecha = (String) miGrilla.getValueAt(i, 14);
                                
                if (miGrilla.getValueAt(i, 14) != null && !fecha.isEmpty())
                {
                    this.setError(ConstantesGui.CAMPO_FECHA_VENCIMIENTO);
                    fechaLiberado = dateFormat.parse((String) miGrilla.getValueAt(i, 14));
                    dtoDocumentoPresentado.setFechaVencimiento(fechaLiberado);
                } else
                {
                    dtoDocumentoPresentado.setFechaVencimiento(null);
                }
                
                this.setError(ConstantesGui.CAMPO_DIAS_VENCIMIENTO);
                dtoDocumentoPresentado.setDiasVencimiento((Integer) miGrilla.getValueAt(i, 13));

                this.setError(ConstantesGui.CAMPO_PREPARADO);
                dtoDocumentoPresentado.setPreparado((boolean) miGrilla.getValueAt(i, 1));

                this.setError(ConstantesGui.CAMPO_PREPARADO);
                dtoDocumentoPresentado.setNumeroCarton((Integer) miGrilla.getValueAt(i, 2));

                this.setError(ConstantesGui.CAMPO_NRO_CARTON);
                dtoDocumentoPresentado.setObservado((boolean) miGrilla.getValueAt(i, 5));

                this.setError(ConstantesGui.CAMPO_OBERVADO);
                dtoDocumentoPresentado.setImporteApagar((Float) miGrilla.getValueAt(i, 6));

                this.setError(ConstantesGui.CAMPO_IMPORTE);
                dtoDocumentoPresentado.setLiberado((boolean) miGrilla.getValueAt(i, 8));

                this.setError(ConstantesGui.CAMPO_LIBERADO);
                dtoDocumentoPresentado.setObservaciones((String) miGrilla.getValueAt(i, 10));

                this.setError(ConstantesGui.CAMPO_OBSERVACIONES);
                dtoDocumentoPresentado.setNombre(dtoDocumentoPresentado.getNombre());

                lisDtoDocumentosPresentados.add(dtoDocumentoPresentado);
                cont++;
            }
        } catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Algun campo es incorrecto: "+ "Fila: " +fila+   " -" +this.getError(), "Advertencia", JOptionPane.WARNING_MESSAGE);
            return dtoFlag;
        }
        try
        {
            if (cont > 0)
            {
                dtoFlag = miController.modificarDocumentacionEntidadesExternas(lisDtoDocumentosPresentados, this.getDtoGestion());
            } else
            {
                JOptionPane.showMessageDialog(this, "No realizo modificaciones o algun dato es incorrecto", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } catch (ClassModifiedException e)
        {
            String mensaje = "Documentacion modificada con anterioridad - Accion Cancelada";
            JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
        }

        return dtoFlag;
    }

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCancelarActionPerformed
    {//GEN-HEADEREND:event_botonCancelarActionPerformed
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaIngresarDocumento);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonBuscarGestionActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonBuscarGestionActionPerformed
    {//GEN-HEADEREND:event_botonBuscarGestionActionPerformed
        BuscarGestion formBuscarGestion = new BuscarGestion();
        formBuscarGestion.setTipoBusqueda(ConstantesGui.DOCUMENTACION_ENTIDAD_EXTERNA);
        Principal.cargarFormulario(formBuscarGestion);
        Principal.setVentanasActivas(BuscarGestion.getVentanaBuscarGestion());
    }//GEN-LAST:event_botonBuscarGestionActionPerformed

    private void grillaMovimientoDocumentacionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grillaMovimientoDocumentacionMouseClicked
        this.botonAceptar.setEnabled(true);
    }//GEN-LAST:event_grillaMovimientoDocumentacionMouseClicked

    private void botonNomenclaturasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonNomenclaturasActionPerformed
        NomenclaturaCatastral formNomenclaturas = NomenclaturaCatastral.getInstancia();
        Principal.cargarFormulario(formNomenclaturas);
        Principal.setVentanasActivas(NomenclaturaCatastral.getVentanaNomenclaturas());
    }//GEN-LAST:event_botonNomenclaturasActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonBuscarGestion;
    private javax.swing.JButton botonCancelar;
    public javax.swing.JButton botonNomenclaturas;
    private javax.swing.JLabel escribanoAcargo;
    private javax.swing.JLabel fechaInicio;
    private javax.swing.JTable grillaMovimientoDocumentacion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelEncabezado;
    private javax.swing.JLabel nroGestion;
    private javax.swing.JPanel panelIngresarDocumentos;
    // End of variables declaration//GEN-END:variables
}
