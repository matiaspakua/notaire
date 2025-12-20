/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.clientes;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.gestiones.gestion.IniciarGestion;
import com.licensis.notaire.gui.gestiones.gestion.ModificarGestion;
import com.licensis.notaire.gui.presupuestos.CrearPresupuesto;
import com.licensis.notaire.negocio.ControllerNegocio;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author juanca
 */
public class ListarPersonas extends javax.swing.JInternalFrame
{

    private static ListarPersonas instancia = null;
    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaListadoPersonas = new JMenuItem("Ventana Listar Personas");
    private ControllerNegocio miController;
    public String tipoDeBusqueda = "";

    /**
     * Creates new form ListarPersonas
     */
    private ListarPersonas()
    {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioGrandeHorizontal, Principal.tamanioGrandeVertical);
        miController = ControllerNegocio.getInstancia();
        grillaPersonas.setAutoCreateRowSorter(true);
    }

    public static ListarPersonas getInstancia()
    {
        if (instancia == null)
        {
            instancia = new ListarPersonas();
        }
        return instancia;
    }

    public void salir()
    {
        this.dispose();
    }

    public static JMenuItem getVentanaListadoPersonas()
    {
        return ventanaListadoPersonas;
    }

    public static void setVentanaListadoPersonas(JMenuItem ventanaListadoPersonas)
    {
        ListarPersonas.ventanaListadoPersonas = ventanaListadoPersonas;
    }

    public Boolean cargarGrillaClientes(ArrayList<DtoPersona> miListaDtoPersonas, String tipoBusqueda)
    {

        this.labelTitulo.setText(tipoBusqueda);
        this.ventanaListadoPersonas.setName(tipoBusqueda);
        this.setTipoDeBusqueda(tipoBusqueda);
        this.limpiarJtable();

        Boolean flag = false;

        //  TODO: corregir el null
        if (miListaDtoPersonas != null)
        {
            DtoPersona miDtoPersona = null;

            for (int i = 0; i < miListaDtoPersonas.size(); i++)
            {
                if (miListaDtoPersonas.get(i).getEsCliente())
                {
                    flag = true;
                    miDtoPersona = miListaDtoPersonas.get(i);

                    String esCliente = new String();

                    if (miDtoPersona.getEsCliente())
                    {
                        esCliente = "Si";
                    } else
                    {
                        esCliente = "No";
                    }

                    Object[] datos =
                    {
                        miDtoPersona.getNombre(),
                        miDtoPersona.getApellido(),
                        miDtoPersona.getDtoTipoIdentificacion().getNombre(),
                        miDtoPersona.getNumeroIdentificacion(),
                        esCliente,
                        miDtoPersona.getVersion(), //Version del objeto
                        miListaDtoPersonas.get(i) //cargo la red de objetos
                    };

                    ((DefaultTableModel) grillaPersonas.getModel()).addRow(datos);
                }
            }
        }
        if (!flag)
        {
            JOptionPane.showMessageDialog(this, "No existen Coincidencias en la Busqueda de Clientes");
            this.salir();

        } else
        {
            Principal.cargarFormulario(ListarPersonas.getInstancia());

        }
        return flag;
    }

    public Boolean cargarGrillaPersonas(ArrayList<DtoPersona> miListaDtoPersonas, String tipoBusqueda)
    {

        this.labelTitulo.setText(tipoBusqueda);
        this.setTipoDeBusqueda(tipoBusqueda);

        boolean flag = false;

        if (miListaDtoPersonas != null)
        {
            DtoPersona miDtoPersona = null;

            for (int i = 0; i < miListaDtoPersonas.size(); i++)
            {

                if (!miListaDtoPersonas.get(i).getEsCliente())
                {
                    flag = true;
                    miDtoPersona = miListaDtoPersonas.get(i);

                    String esCliente = new String();

                    if (miDtoPersona.getEsCliente())
                    {
                        esCliente = "Si";
                    } else
                    {
                        esCliente = "No";
                    }

                    Object[] datos =
                    {
                        miDtoPersona.getNombre(),
                        miDtoPersona.getApellido(),
                        miDtoPersona.getDtoTipoIdentificacion().getNombre(),
                        miDtoPersona.getNumeroIdentificacion(),
                        esCliente,
                        miDtoPersona.getVersion() //Version del objeto
                    };
                    ((DefaultTableModel) grillaPersonas.getModel()).addRow(datos);
                }

            }

        }

        if (!flag)
        {
            JOptionPane.showMessageDialog(this, "No existen Coincidencias en la Busqueda");

        } else
        {
            Principal.cargarFormulario(ListarPersonas.getInstancia());
        }
        return flag;
    }

    public Boolean cargarGrillaPersonasClientes(ArrayList<DtoPersona> miListaDtoPersonas, String tipoBusqueda)
    {
        boolean flag = false;
        this.setTipoDeBusqueda(tipoBusqueda);
        this.labelTitulo.setText(tipoBusqueda);

        if (miListaDtoPersonas != null)
        {
            DtoPersona miDtoPersona = null;

            for (int i = 0; i < miListaDtoPersonas.size(); i++)
            {

                flag = true;
                miDtoPersona = miListaDtoPersonas.get(i);

                String esCliente = new String();

                if (miDtoPersona.getEsCliente())
                {
                    esCliente = "Si";
                } else
                {
                    esCliente = "No";
                }
                Object[] datos =
                {
                    miDtoPersona.getNombre(),
                    miDtoPersona.getApellido(),
                    miDtoPersona.getDtoTipoIdentificacion().getNombre(),
                    miDtoPersona.getNumeroIdentificacion(),
                    esCliente
                };
                ((DefaultTableModel) grillaPersonas.getModel()).addRow(datos);

            }
        }

        if (!flag)
        {
            JOptionPane.showMessageDialog(this, "No existen Coincidencias en la Busqueda");

        } else
        {
            Principal.cargarFormulario(ListarPersonas.getInstancia());
        }
        return flag;
    }

    public void limpiarJtable()
    {
        int i = ((DefaultTableModel) grillaPersonas.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaPersonas.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaPersonas.getModel()).removeRow(i);
            i--;
        }

    }

    public JMenuItem getVentanaActiva()
    {
        ventanaListadoPersonas.setText(labelTitulo.getText());
        this.setTitle(labelTitulo.getText());
        return ventanaListadoPersonas;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        panelListadoPersonas = new javax.swing.JPanel();
        labelTitulo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaPersonas = new javax.swing.JTable();
        botonSalir = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Listar Personas");
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

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        labelTitulo.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        labelTitulo.setText("Lista Personas");

        grillaPersonas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nombre", "Apellido", "Identificación", "Número", "Cliente", "Version", "Persona"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
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
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                grillaPersonasMouseEntered(evt);
            }
        });
        jScrollPane1.setViewportView(grillaPersonas);
        grillaPersonas.getColumnModel().getColumn(5).setResizable(false);
        //Oculto columnas
        grillaPersonas.getColumnModel().getColumn(5).setMaxWidth(0);
        grillaPersonas.getColumnModel().getColumn(5).setMinWidth(0);
        grillaPersonas.getColumnModel().getColumn(5).setMaxWidth(0);
        grillaPersonas.getColumnModel().getColumn(5).setMinWidth(0);
        grillaPersonas.getColumnModel().getColumn(6).setMaxWidth(0);
        grillaPersonas.getColumnModel().getColumn(6).setMinWidth(0);
        grillaPersonas.getColumnModel().getColumn(6).setMaxWidth(0);
        grillaPersonas.getColumnModel().getColumn(6).setMinWidth(0);

        botonSalir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar.png"))); // NOI18N
        botonSalir.setText("Cerrar");
        botonSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonSalirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelListadoPersonasLayout = new javax.swing.GroupLayout(panelListadoPersonas);
        panelListadoPersonas.setLayout(panelListadoPersonasLayout);
        panelListadoPersonasLayout.setHorizontalGroup(
            panelListadoPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelListadoPersonasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelListadoPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelListadoPersonasLayout.createSequentialGroup()
                        .addComponent(labelTitulo)
                        .addGap(0, 691, Short.MAX_VALUE))
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelListadoPersonasLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelListadoPersonasLayout.setVerticalGroup(
            panelListadoPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelListadoPersonasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jScrollPane2.setViewportView(panelListadoPersonas);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonSalirActionPerformed
        this.limpiarJtable();
        this.salir();
    }//GEN-LAST:event_botonSalirActionPerformed

    @Override
    public void dispose()
    {
        super.dispose();
    }

    public String getTipoDeBusqueda()
    {
        return tipoDeBusqueda;
    }

    public void setTipoDeBusqueda(String tipoDeBusqueda)
    {
        this.tipoDeBusqueda = tipoDeBusqueda;
    }

    private void grillaPersonasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grillaPersonasMouseClicked

        TableModel miGrilla = grillaPersonas.getModel();
        int filaSeleccionada = grillaPersonas.getSelectedRow();
        filaSeleccionada = grillaPersonas.convertRowIndexToModel(filaSeleccionada);

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
                miDtoPersona.setVersion((Integer) miGrilla.getValueAt(i, 5));

                //Asocio el nombre tipo Identificacion con su id para buscar la persona
                dtoTipoIdentificacion.setIdTipoIdentificacion(ControllerNegocio.getInstancia().asociarFkTipoIdentificacion(miDtoPersona));

                //Busco todos los datos de la persona seleccionada y cargo  el formulario
                miDtoPersona = ControllerNegocio.getInstancia().buscarPersonaTipoNumeroIdentificacion(miDtoPersona);

                if (miDtoPersona.isValido())
                {
                    switch (this.getTipoDeBusqueda())
                    {
                        case ConstantesGui.CREAR_PRESUPUESTO:
                        {
                            CrearPresupuesto.getInstancia().cargarFormulario(miDtoPersona);
                            break;
                        }
                        case ConstantesGui.INICIAR_GESTION:
                        {
                            IniciarGestion miForm = (IniciarGestion) Principal.obtenerFormularioActivo(ConstantesGui.INICIAR_GESTION);
                            if (miForm.agregarClienteGestion(miDtoPersona))
                            {
                                // No se pudo registrar el nuevo cliente.
                            } else
                            {
                                JOptionPane.showMessageDialog(this, "Se ha agregado un cliente a la gestion indicada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                            }
                            break;
                        }
                        case ConstantesGui.MODIFICAR_GESTION_AGREGAR_CLIENTE:
                        {
                            ModificarGestion miForm = (ModificarGestion) Principal.obtenerFormularioActivo("Ventana Modificar Gestion");
                            miForm.agregarClienteGestion(miDtoPersona);
                            JOptionPane.showMessageDialog(this, "Se ha agregado un cliente a la gestion indicada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                        case ConstantesGui.MODIFICAR_GESTION:
                        {
                            miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 6));
                            BuscarGestionesCliente formBuscarGestionesCliente = BuscarGestionesCliente.getInstancia();

                            formBuscarGestionesCliente.cargarGrillaGestionesCliente(miDtoPersona, ConstantesGui.MODIFICAR_GESTION);
                            formBuscarGestionesCliente.activarGrilla();

                            Principal.cargarFormulario(formBuscarGestionesCliente);
                            Principal.setVentanasActivas(BuscarGestionesCliente.getVentanaBuscarGestionesCliente());

                            this.salir();
                            break;
                        }
                        case ConstantesGui.PREPARAR_ESCRITURA:
                        {
                            miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 6));

                            BuscarGestionesCliente listaGestionesCliente = BuscarGestionesCliente.getInstancia();
                            Boolean cargada = listaGestionesCliente.cargarGrillaGestionesCliente(miDtoPersona, ConstantesGui.PREPARAR_ESCRITURA);

                            if (cargada)
                            {
                                Principal.cargarFormulario(listaGestionesCliente);
                                Principal.setVentanasActivas(BuscarGestionesCliente.getVentanaBuscarGestionesCliente());
                            } else
                            {
                                JOptionPane.showMessageDialog(this, "La persona no tiene gestiones asociadas sin archivar.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                            }
                            break;
                        }
                        case ConstantesGui.VER_GESTIONES:
                        {

                            //La grilla contiene el objet persona con su red de objetos
                            miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 6));
                            BuscarGestionesCliente.getInstancia().cargarGrillaGestionesCliente(miDtoPersona, ConstantesGui.VER_GESTIONES);

                            Principal.cargarFormulario(BuscarGestionesCliente.getInstancia());
                            Principal.setVentanasActivas(BuscarGestionesCliente.getVentanaBuscarGestionesCliente());
                            break;
                        }
                        case ConstantesGui.DOCUMENTACION_DEUDA:
                        {
                            //La grilla contiene el objet persona con su red de objetos
                            miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 6));
                            BuscarGestionesCliente.getInstancia().cargarGrillaGestionesCliente(miDtoPersona, ConstantesGui.DOCUMENTACION_DEUDA);

                            Principal.cargarFormulario(BuscarGestionesCliente.getInstancia());
                            Principal.setVentanasActivas(BuscarGestionesCliente.getVentanaBuscarGestionesCliente());
                            break;
                        }
                        case ConstantesGui.DOCUMENTACION_INGRESO:
                        {
                            //La grilla contiene el objet persona con su red de objetos
                            miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 6));
                            BuscarGestionesCliente.getInstancia().cargarGrillaGestionesCliente(miDtoPersona, ConstantesGui.DOCUMENTACION_INGRESO);

                            Principal.cargarFormulario(BuscarGestionesCliente.getInstancia());
                            Principal.setVentanasActivas(BuscarGestionesCliente.getVentanaBuscarGestionesCliente());
                            this.salir();
                            break;
                        }
                        case ConstantesGui.DOCUMENTACION_ENTIDAD_EXTERNA:
                        {
                            //La grilla contiene el objet persona con su red de objetos
                            miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 6));
                            BuscarGestionesCliente.getInstancia().cargarGrillaGestionesCliente(miDtoPersona, ConstantesGui.DOCUMENTACION_ENTIDAD_EXTERNA);

                            Principal.cargarFormulario(BuscarGestionesCliente.getInstancia());
                            Principal.setVentanasActivas(BuscarGestionesCliente.getVentanaBuscarGestionesCliente());
                            break;
                        }
                        case ConstantesGui.DOCUMENTACION_REINGRESO:
                        {
                            //La grilla contiene el objet persona con su red de objetos
                            miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 6));
                            BuscarGestionesCliente.getInstancia().cargarGrillaGestionesCliente(miDtoPersona, ConstantesGui.DOCUMENTACION_REINGRESO);

                            Principal.cargarFormulario(BuscarGestionesCliente.getInstancia());
                            Principal.setVentanasActivas(BuscarGestionesCliente.getVentanaBuscarGestionesCliente());
                            break;
                        }
                        case ConstantesGui.BUSCAR_PERSONA_CLIENTE:
                        {

                            AdministrarCliente formAdministrarCliente = AdministrarCliente.getInstancia();

                            formAdministrarCliente.setTitle(ConstantesGui.VENTANA_PERSONA_CLIENTES);

                            Boolean flag = formAdministrarCliente.cargarFormulario(miDtoPersona, this.getTipoDeBusqueda());

                            Principal.cargarFormulario(formAdministrarCliente);
                            Principal.setVentanasActivas(AdministrarCliente.getVentanaAdministrarCliente());

                            formAdministrarCliente.toFront();
                            break;

                        }
                        case ConstantesGui.BUSCAR_CLIENTE:
                        {

                            AdministrarCliente formAdministrarCliente = AdministrarCliente.getInstancia();

                            formAdministrarCliente.setTitle(ConstantesGui.VENTANA_CLIENTE);

                            Boolean flag = formAdministrarCliente.cargarFormulario(miDtoPersona, this.getTipoDeBusqueda());

                            Principal.cargarFormulario(formAdministrarCliente);
                            Principal.setVentanasActivas(AdministrarCliente.getVentanaAdministrarCliente());

                            formAdministrarCliente.toFront();
                            break;

                        }

                        case ConstantesGui.MODIFICAR_PERSONA:
                        {
                            AdministrarCliente formAdministrarCliente = AdministrarCliente.getInstancia();

                            formAdministrarCliente.setTitle(ConstantesGui.VENTANA_PERSONA);

                            formAdministrarCliente.toFront();

                            Boolean flag = formAdministrarCliente.cargarFormulario(miDtoPersona, this.getTipoDeBusqueda());

                            Principal.cargarFormulario(formAdministrarCliente);
                            Principal.setVentanasActivas(AdministrarCliente.getVentanaAdministrarCliente());

                            this.limpiarJtable();

                            this.salir();
                            break;
                        }

                        case ConstantesGui.DAR_ALTA_CLIENTE:
                        {
                            AdministrarCliente formAdministrarCliente = AdministrarCliente.getInstancia();

                            formAdministrarCliente.setTitle(ConstantesGui.VENTANA_PERSONA);

                            formAdministrarCliente.toFront();

                            Boolean flag = formAdministrarCliente.cargarFormulario(miDtoPersona, this.getTipoDeBusqueda());

                            Principal.cargarFormulario(formAdministrarCliente);
                            Principal.setVentanasActivas(AdministrarCliente.getVentanaAdministrarCliente());

                            this.limpiarJtable();

                            this.salir();
                            break;
                        }

                        case ConstantesGui.MODIFICAR_CLIENTE:
                        {
                            AdministrarCliente formAdministrarCliente = AdministrarCliente.getInstancia();

                            formAdministrarCliente.setTitle(ConstantesGui.VENTANA_CLIENTE);

                            formAdministrarCliente.toFront();

                            formAdministrarCliente.botonModoEdicion();

                            Boolean flag = formAdministrarCliente.cargarFormulario(miDtoPersona, this.getTipoDeBusqueda());

                            Principal.cargarFormulario(formAdministrarCliente);
                            Principal.setVentanasActivas(AdministrarCliente.getVentanaAdministrarCliente());

                            this.limpiarJtable();

                            this.salir();
                            break;
                        }
                        case ConstantesGui.VER_HISTORIAL_GESTION:
                        {
                            //La grilla contiene el objet persona con su red de objetos
                            miDtoPersona = (DtoPersona) (miGrilla.getValueAt(i, 6));
                            BuscarGestionesCliente.getInstancia().cargarGrillaGestionesCliente(miDtoPersona, ConstantesGui.VER_HISTORIAL_GESTION);

                            Principal.cargarFormulario(BuscarGestionesCliente.getInstancia());
                            Principal.setVentanasActivas(BuscarGestionesCliente.getVentanaBuscarGestionesCliente());

                            this.salir();
                            break;
                        }
                    }

                }
            }
        }

    }//GEN-LAST:event_grillaPersonasMouseClicked
    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaListadoPersonas);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void grillaPersonasMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grillaPersonasMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_grillaPersonasMouseEntered
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonSalir;
    private javax.swing.JTable grillaPersonas;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JLabel labelTitulo;
    private javax.swing.JPanel panelListadoPersonas;
    // End of variables declaration//GEN-END:variables
}
