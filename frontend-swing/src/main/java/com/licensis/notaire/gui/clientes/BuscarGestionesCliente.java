/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.clientes;

import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.gestiones.documentacion.ConsultarDeudasDocumentos;
import com.licensis.notaire.gui.gestiones.documentacion.IngresarDocumento;
import com.licensis.notaire.gui.gestiones.documentacion.RegistrarEntregaDocumentos;
import com.licensis.notaire.gui.gestiones.documentacion.ReingresarDocumentos;
import com.licensis.notaire.gui.gestiones.escrituras.PrepararEscritura;
import com.licensis.notaire.gui.gestiones.gestion.BuscarGestion;
import com.licensis.notaire.gui.gestiones.gestion.DetalleGestion;
import com.licensis.notaire.gui.gestiones.gestion.ModificarGestion;
import com.licensis.notaire.gui.gestiones.gestion.VerHistorialGestion;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.ControllerNegocio;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
public class BuscarGestionesCliente extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaBuscarGestionesCliente = new JMenuItem("Ventana Buscar Gestiones Cliente");
    private static BuscarGestionesCliente instancia;
    private String tipoDeBusqueda;

    /**
     * Creates new form BuscarGestionesCliente
     */
    private BuscarGestionesCliente()
    {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        grillaGestionesCliente.setAutoCreateRowSorter(true);
        this.setSize(900, 600);

    }

    public static BuscarGestionesCliente getInstancia()
    {
        if (instancia == null)
        {
            instancia = new BuscarGestionesCliente();
        }
        instancia.ocultarBotonBuscar();
        instancia.ocultarLabelBuscar();
        return instancia;
    }

    private void salir()
    {
        this.dispose();
    }

    public void ocultarBotonBuscar()
    {
        this.botonBuscarCliente.setVisible(false);
    }

    public void mostrarBotonBuscar()
    {
        this.botonBuscarCliente.setVisible(true);
    }

    public void mostrarLabelBuscar()
    {
        this.labelBuscar.setVisible(true);
    }

    public void ocultarLabelBuscar()
    {
        this.labelBuscar.setVisible(false);
    }

    public static JMenuItem getVentanaBuscarGestionesCliente()
    {
        return ventanaBuscarGestionesCliente;
    }

    public Boolean cargarGrillaGestionesCliente(DtoPersona miDtoPersona, String tipoBusqueda)
    {

        Boolean flag = false;
        ArrayList<DtoGestionDeEscritura> listaDtoGestionDeEscrituras = new ArrayList<>();

        this.limpiarJtable();
        this.activarGrilla();
        this.labelTitulo.setText(tipoBusqueda);
        this.setTipoDeBusqueda(tipoBusqueda);
        //LLamo la instancia de BuscarGestiones

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        //Busco sus gestiones
        listaDtoGestionDeEscrituras = miDtoPersona.getListaDtoGestionDeEscriturasPersona();
        listaDtoGestionDeEscrituras = this.elimimarDuplicados(listaDtoGestionDeEscrituras);
        List<DtoGestionDeEscritura> miListaGestiones = new ArrayList<>(listaDtoGestionDeEscrituras);

        if (tipoBusqueda.equals(ConstantesGui.PREPARAR_ESCRITURA))
        {
            listaDtoGestionDeEscrituras = new ArrayList<>();

            for (Iterator<DtoGestionDeEscritura> it = miListaGestiones.iterator(); it.hasNext();)
            {
                DtoGestionDeEscritura dtoGestionDeEscritura = it.next();
                if (!dtoGestionDeEscritura.getEstado().getNombre().equals(ConstantesNegocio.GESTION_ARCHIVADA))
                {
                    listaDtoGestionDeEscrituras.add(dtoGestionDeEscritura);
                }
            }
        }

        if (listaDtoGestionDeEscrituras != null && !listaDtoGestionDeEscrituras.isEmpty())
        {
            flag = true;
            DtoGestionDeEscritura miDtoGestion = null;

            for (int i = 0; i < listaDtoGestionDeEscrituras.size(); i++)
            {
                flag = true;
                miDtoGestion = listaDtoGestionDeEscrituras.get(i);

                Object[] datos =
                {
                    miDtoGestion.getNumero(),
                    miDtoGestion.getEncabezado(),
                    formatter.format(miDtoGestion.getFechaInicio()),
                    miDtoGestion.getEstado().getNombre(),
                    miDtoGestion.getNumeroBibliorato(),
                    miDtoGestion.getObservaciones(),
                    miDtoGestion,//Cargo el id de la gestion
                    miDtoPersona //Cargo la persona con la gestion correspondiente
                };

                ((DefaultTableModel) grillaGestionesCliente.getModel()).addRow(datos);

            }

            Principal.cargarFormulario(BuscarGestionesCliente.getInstancia());
            this.toFront();
        }

        return flag;
    }

    public ArrayList<DtoGestionDeEscritura> elimimarDuplicados(ArrayList<DtoGestionDeEscritura> listaDtoGestiones)
    {

        //Creamos un objeto HashSet
        HashSet hs = new HashSet();

        //Lo cargamos con los valores del array, esto hace quite los repetidos
        hs.addAll(listaDtoGestiones);

        //Limpiamos el array
        listaDtoGestiones.clear();
        listaDtoGestiones.addAll(hs);

        return listaDtoGestiones;

    }

    public void activarGrilla()
    {
        this.grillaGestionesCliente.setEnabled(true);

    }

    public void desactivarGrilla()
    {
        this.grillaGestionesCliente.setEnabled(false);
        this.limpiarJtable();
    }

    public void limpiarJtable()
    {
        int i = ((DefaultTableModel) grillaGestionesCliente.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaGestionesCliente.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaGestionesCliente.getModel()).removeRow(i);
            i--;
        }

    }

    public String getTipoDeBusqueda()
    {
        return tipoDeBusqueda;
    }

    public void setTipoDeBusqueda(String tipoDeBusqueda)
    {
        this.tipoDeBusqueda = tipoDeBusqueda;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBuscarGestionesCliente = new javax.swing.JPanel();
        labelTitulo = new javax.swing.JLabel();
        labelBuscar = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaGestionesCliente = new javax.swing.JTable();
        botonBuscarCliente = new javax.swing.JButton();
        botonAceptar = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Buscar Gestiones Cliente");
        setPreferredSize(new java.awt.Dimension(908, 475));
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        labelTitulo.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        labelTitulo.setText("Buscar Gestiones de Cliente");

        labelBuscar.setText("Buscar Cliente:");

        grillaGestionesCliente.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nro de Gestion", "Encabezado", "Fecha de Inicio", "Estado", "Nro Bibliorato", "Observaciones", "DtoGestion", "DtoPersona"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
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
        grillaGestionesCliente.setEnabled(false);
        grillaGestionesCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grillaGestionesClienteMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(grillaGestionesCliente);
        grillaGestionesCliente.getColumnModel().getColumn(6).setResizable(false);
        grillaGestionesCliente.getColumnModel().getColumn(7).setResizable(false);
        grillaGestionesCliente.getColumnModel().getColumn(6).setMaxWidth(0);
        grillaGestionesCliente.getColumnModel().getColumn(6).setMinWidth(0);
        grillaGestionesCliente.getColumnModel().getColumn(7).setMaxWidth(0);
        grillaGestionesCliente.getColumnModel().getColumn(7).setMinWidth(0);
        grillaGestionesCliente.getColumnModel().getColumn(6).setMaxWidth(0);
        grillaGestionesCliente.getColumnModel().getColumn(6).setMinWidth(0);
        grillaGestionesCliente.getColumnModel().getColumn(7).setMaxWidth(0);
        grillaGestionesCliente.getColumnModel().getColumn(7).setMinWidth(0);

        botonBuscarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/buscar.png"))); // NOI18N
        botonBuscarCliente.setText("Buscar");
        botonBuscarCliente.setIconTextGap(10);
        botonBuscarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarClienteActionPerformed(evt);
            }
        });

        botonAceptar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar.png"))); // NOI18N
        botonAceptar.setText("Cerrar");
        botonAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAceptarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBuscarGestionesClienteLayout = new javax.swing.GroupLayout(panelBuscarGestionesCliente);
        panelBuscarGestionesCliente.setLayout(panelBuscarGestionesClienteLayout);
        panelBuscarGestionesClienteLayout.setHorizontalGroup(
            panelBuscarGestionesClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBuscarGestionesClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBuscarGestionesClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBuscarGestionesClienteLayout.createSequentialGroup()
                        .addGap(878, 878, 878)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 6, Short.MAX_VALUE))
                    .addGroup(panelBuscarGestionesClienteLayout.createSequentialGroup()
                        .addGap(878, 878, 878)
                        .addComponent(jSeparator1))
                    .addGroup(panelBuscarGestionesClienteLayout.createSequentialGroup()
                        .addGroup(panelBuscarGestionesClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(labelTitulo)
                            .addGroup(panelBuscarGestionesClienteLayout.createSequentialGroup()
                                .addComponent(labelBuscar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(botonBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBuscarGestionesClienteLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelBuscarGestionesClienteLayout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addGap(8, 8, 8)))
                .addContainerGap())
        );
        panelBuscarGestionesClienteLayout.setVerticalGroup(
            panelBuscarGestionesClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBuscarGestionesClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTitulo)
                .addGap(18, 18, 18)
                .addGroup(panelBuscarGestionesClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelBuscar)
                    .addComponent(botonBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBuscarGestionesCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBuscarGestionesCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaBuscarGestionesCliente);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAceptarActionPerformed
        this.dispose();
    }//GEN-LAST:event_botonAceptarActionPerformed

    private void botonBuscarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuscarClienteActionPerformed
        BuscarGestion formBuscarGestion = new BuscarGestion();

        //El tipo de busqueda lo deftine el titulo del formularo, que es una constante
        formBuscarGestion.setTipoBusqueda(labelTitulo.getText());

        Principal.cargarFormulario(formBuscarGestion);
        Principal.setVentanasActivas(BuscarCliente.getVentanaBuscarCliente());
    }//GEN-LAST:event_botonBuscarClienteActionPerformed

    private void grillaGestionesClienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grillaGestionesClienteMouseClicked
        TableModel miGrilla = grillaGestionesCliente.getModel();
        int filaSeleccionada = grillaGestionesCliente.getSelectedRow();
        filaSeleccionada = grillaGestionesCliente.convertRowIndexToModel(filaSeleccionada);

        int filas = miGrilla.getRowCount();
        int columnas = miGrilla.getColumnCount();

        DtoPersona miDtoPersona = new DtoPersona();
        DtoGestionDeEscritura dtoGestionEscritura = new DtoGestionDeEscritura();

        //Instancia del formulario DetalleGestion
        DetalleGestion formDetalleGestion = DetalleGestion.getInstancia();

        //Recorro la grilla completa, buscando la gestion seleccionada
        for (int i = 0; i < filas; i++)
        {
            if (i == filaSeleccionada)
            {
                switch (this.getTipoDeBusqueda())
                {
                    case ConstantesGui.VER_GESTIONES:
                    {
                        try
                        {
                            formDetalleGestion.setTitle(ConstantesGui.VER_GESTIONES);

                            //Importante
                            //La grilla contiene el DtoGestion en la fila nro 6
                            //y DtoPersona en la fila nro 7
                            dtoGestionEscritura = (DtoGestionDeEscritura) (miGrilla.getValueAt(i, 6));
                            miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 7));

                            //Llamo un metodo que solo permite ver la gestion
                            Boolean flag = formDetalleGestion.cargarFormuarlioSoloVista(dtoGestionEscritura);

                            Principal.cargarFormulario(formDetalleGestion);
                            Principal.setVentanasActivas(DetalleGestion.getVentanaDetalleGestion());
                            break;
                        }
                        catch (NonexistentJpaException ex)
                        {
                            Logger.getLogger(BuscarGestionesCliente.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                    case ConstantesGui.MODIFICAR_GESTION:
                    {
                        dtoGestionEscritura = (DtoGestionDeEscritura) (miGrilla.getValueAt(i, 6));

                        if (dtoGestionEscritura.getEstado().getNombre().contains(ConstantesNegocio.GESTION_ARCHIVADA))
                        {
                            JOptionPane.showMessageDialog(this, "La gestion selecciona esta archivada, por lo tanto, no se puede modificar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        } else
                        {
                            miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 7));

                            dtoGestionEscritura.setClienteReferencia(miDtoPersona);

                            // TOFIX: cambiar el parametro de "obtener formulario", no se porque no funciona con constantesGui.ModificarGestion
                            ModificarGestion formModificarGestion = (ModificarGestion) Principal.obtenerFormularioActivo("Ventana Modificar Gestion");

                            formModificarGestion.cargarDatosGestion(dtoGestionEscritura);
                        }
                        this.salir();
                        break;
                    }
                    case ConstantesGui.DOCUMENTACION_INGRESO:
                    {
                        dtoGestionEscritura = (DtoGestionDeEscritura) (miGrilla.getValueAt(i, 6));
                        miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 7));

                        Boolean flag = RegistrarEntregaDocumentos.getInstancia().cargarFormulario(dtoGestionEscritura);

                        break;
                    }
                    case ConstantesGui.DOCUMENTACION_DEUDA:
                    {
                        dtoGestionEscritura = (DtoGestionDeEscritura) (miGrilla.getValueAt(i, 6));
                        miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 7));
                        try
                        {
                            Boolean flag = ConsultarDeudasDocumentos.getInstancia().cargarFormulario(dtoGestionEscritura);
                        }
                        catch (NonexistentJpaException ex)
                        {
                            Logger.getLogger(BuscarGestionesCliente.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;

                    }
                    case ConstantesGui.PREPARAR_ESCRITURA:
                    {
                        //Importante
                        //La grilla contiene el DtoGestion en la fila nro 6
                        //y DtoPersona en la fila nro 7
                        dtoGestionEscritura = (DtoGestionDeEscritura) (miGrilla.getValueAt(i, 6));

                        if (dtoGestionEscritura.getEstado().getNombre().equals(ConstantesNegocio.DOCUMENTACION_COMPLETA))
                        {
                            PrepararEscritura prepararEscrituraForm = new PrepararEscritura();
                            prepararEscrituraForm.cargarFormulario(dtoGestionEscritura);
                            Principal.cargarFormulario(prepararEscrituraForm);
                        } else
                        {
                            JOptionPane.showMessageDialog(this, "Aun no se ha entregado toda la documentacion necesaria.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                        }
                        salir();
                        break;
                    }
                    case ConstantesGui.VER_HISTORIAL_GESTION:
                    {

                        //Importante
                        //La grilla contiene el DtoGestion en la fila nro 6
                        //y DtoPersona en la fila nro 7
                        dtoGestionEscritura = (DtoGestionDeEscritura) (miGrilla.getValueAt(i, 6));
                        miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 7));

                        //  Asociamos el cliente de referencia.
                        dtoGestionEscritura.setClienteReferencia(miDtoPersona);

                        // buscamos el registro del historial.
                        ControllerNegocio miControler = ControllerNegocio.getInstancia();
                        dtoGestionEscritura.setRegistroHistorial(miControler.obtenerHistorialGestion(dtoGestionEscritura));

                        VerHistorialGestion formHistorial = (VerHistorialGestion) Principal.obtenerFormularioActivo(ConstantesGui.VER_HISTORIAL_GESTION);

                        formHistorial.cargarGestion(dtoGestionEscritura);

                        //formDetalleGestion.toFront();
                        Principal.cargarFormulario(formHistorial);
                        Principal.setVentanasActivas(VerHistorialGestion.getVentanaVerHistorialGestion());
                        this.salir();
                        break;
                    }
                    case ConstantesGui.DOCUMENTACION_ENTIDAD_EXTERNA:
                    {
                        dtoGestionEscritura = (DtoGestionDeEscritura) (miGrilla.getValueAt(i, 6));
                        miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 7));
                        try
                        {
                            Boolean flag = IngresarDocumento.getInstancia().cargarFormulario(dtoGestionEscritura);
                        }
                        catch (NonexistentJpaException ex)
                        {
                            Logger.getLogger(BuscarGestionesCliente.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;

                    }

                    case ConstantesGui.DOCUMENTACION_REINGRESO:
                    {
                        dtoGestionEscritura = (DtoGestionDeEscritura) (miGrilla.getValueAt(i, 6));
                        miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 7));
                        try
                        {
                            Boolean flag = ReingresarDocumentos.getInstancia().cargarFormulario(dtoGestionEscritura);
                        }
                        catch (NonexistentJpaException ex)
                        {
                            Logger.getLogger(BuscarGestionesCliente.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;

                    }

                }
            }

            //Cargo el formPrincipal
            //Principal.getInstancia().cargarFormulario(formDetalleGestion);
        }
    }//GEN-LAST:event_grillaGestionesClienteMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    public javax.swing.JButton botonBuscarCliente;
    private javax.swing.JTable grillaGestionesCliente;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelBuscar;
    public javax.swing.JLabel labelTitulo;
    private javax.swing.JPanel panelBuscarGestionesCliente;
    // End of variables declaration//GEN-END:variables
}
