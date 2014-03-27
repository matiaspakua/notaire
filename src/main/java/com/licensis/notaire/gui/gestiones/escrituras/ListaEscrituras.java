/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.gestiones.escrituras;

import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoTestimonio;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.gestiones.inscripciones.IngresarParaInscripcion;
import com.licensis.notaire.gui.gestiones.inscripciones.RegistrarInscripcion;
import com.licensis.notaire.gui.gestiones.inscripciones.RegistrarReingreso;
import com.licensis.notaire.gui.gestiones.testimonios.GenerarTestimonio;
import com.licensis.notaire.gui.gestiones.testimonios.RetirarTestimonio;
import com.licensis.notaire.gui.gestiones.testimonios.VerificarTestimonio;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import com.licensis.notaire.negocio.ControllerNegocio;

/**
 *
 * @author matias
 */
public class ListaEscrituras extends javax.swing.JInternalFrame
{

    private static JMenuItem ventanaListaEscritura = new JMenuItem("Ventana Lista Escritura");
    private List<DtoEscritura> miListaEscrituras = null;
    private String formularioInvocador;
    private ControllerNegocio miController = ControllerNegocio.getInstancia();

    /**
     * Creates new form ListaEscrituras
     */
    public ListaEscrituras() {
        initComponents();
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioNormalVertical);
        grillaListaEscrituras.setAutoCreateRowSorter(true);
    }

    public void setFormularioInvocador(String formulario) {
        this.formularioInvocador = formulario;
    }

    private void salir() {
        this.dispose();
    }

    public static JMenuItem getVentanaListaEscritura() {
        return ventanaListaEscritura;
    }

    public Boolean cargarGrilla(List<DtoEscritura> listaEscrituras) {
        miListaEscrituras = listaEscrituras;
        limpiarGrilla();

        Integer numero = 0;
        String fechaEscritura = "";
        String estado = "";
        String matricula = "";
        String fechaInscripcion = "";
        Integer registro = 0;
        Boolean flag = false;

        for (Iterator<DtoEscritura> it = miListaEscrituras.iterator(); it.hasNext();)
        {
            numero = 0;
            fechaEscritura = "";
            estado = "";
            matricula = "";
            fechaInscripcion = "";
            registro = 0;
            DtoEscritura dtoEscritura = it.next();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            if (formularioInvocador.equals(ConstantesGui.REGISTRAR_REINGRESO)
                    || formularioInvocador.equals(ConstantesGui.VERIFICAR_TESTIMONIO)
                    || formularioInvocador.equals(ConstantesGui.RETIRAR_TESTIMONIO)
                    || formularioInvocador.equals(ConstantesGui.INGRESAR_PARA_INSCRIPCION)
                    || formularioInvocador.equals(ConstantesGui.REGISTRAR_INSCRIPCION))
            {
                List<DtoTestimonio> testimonios = miController.obtenerTestimoniosEscritura(dtoEscritura);

                if (testimonios != null && !testimonios.isEmpty())
                {
                    flag = true;

                    DtoPersona miEscribano = miController.obtenerEscribanoEscritura(dtoEscritura);
                    registro = miEscribano.getRegistroEscribano();

                    numero = dtoEscritura.getNumero();
                    fechaEscritura = formatter.format(dtoEscritura.getFechaEscrituracion()).toString();
                    estado = dtoEscritura.getEstado();

                    if (dtoEscritura.getMatriculaInscripcion() != null)
                    {
                        matricula = dtoEscritura.getMatriculaInscripcion();
                    }

                    if (dtoEscritura.getFechaInscripcion() != null)
                    {
                        fechaInscripcion = formatter.format(dtoEscritura.getFechaInscripcion()).toString();
                    }

                    Object[] datos =
                    {
                        registro,
                        numero,
                        fechaEscritura,
                        estado,
                        matricula,
                        fechaInscripcion,
                        dtoEscritura
                    };
                    ((DefaultTableModel) grillaListaEscrituras.getModel()).addRow(datos);
                }
            }
            else
            {
                flag = true;

                DtoPersona miEscribano = miController.obtenerEscribanoEscritura(dtoEscritura);
                registro = miEscribano.getRegistroEscribano();

                numero = dtoEscritura.getNumero();
                fechaEscritura = formatter.format(dtoEscritura.getFechaEscrituracion()).toString();
                estado = dtoEscritura.getEstado();

                if (dtoEscritura.getMatriculaInscripcion() != null)
                {
                    matricula = dtoEscritura.getMatriculaInscripcion();
                }

                if (dtoEscritura.getFechaInscripcion() != null)
                {
                    fechaInscripcion = formatter.format(dtoEscritura.getFechaInscripcion()).toString();
                }

                Object[] datos =
                {
                    registro,
                    numero,
                    fechaEscritura,
                    estado,
                    matricula,
                    fechaInscripcion,
                    dtoEscritura
                };
                ((DefaultTableModel) grillaListaEscrituras.getModel()).addRow(datos);
            }
        }

        return flag;
    }

    public void limpiarGrilla() {
        int i = ((DefaultTableModel) grillaListaEscrituras.getModel()).getRowCount() - 1;

        while (((DefaultTableModel) grillaListaEscrituras.getModel()).getRowCount() > 0)
        {
            ((DefaultTableModel) grillaListaEscrituras.getModel()).removeRow(i);
            i--;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelListaEscrituras = new javax.swing.JPanel();
        labelTituloEscrituras = new javax.swing.JLabel();
        botonCancelar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        grillaListaEscrituras = new javax.swing.JTable();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Lista Escritura");
        setMinimumSize(new java.awt.Dimension(106, 30));
        setPreferredSize(new java.awt.Dimension(710, 486));
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

        labelTituloEscrituras.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        labelTituloEscrituras.setText("Lista Escrituras");

        botonCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/licensis/notaire/gui/iconos/cerrar.png"))); // NOI18N
        botonCancelar.setText("Cerrar");
        botonCancelar.setIconTextGap(8);
        botonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCancelarActionPerformed(evt);
            }
        });

        grillaListaEscrituras.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Registro Escribano", "Número Escritura", "Fecha Escrituración", "Estado", "Matricula Inscripción", "Fecha Inscripción", "DtoEscritura"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
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
        grillaListaEscrituras.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        grillaListaEscrituras.getTableHeader().setReorderingAllowed(false);
        grillaListaEscrituras.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grillaListaEscriturasMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                grillaListaEscriturasMouseEntered(evt);
            }
        });
        jScrollPane1.setViewportView(grillaListaEscrituras);
        grillaListaEscrituras.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        grillaListaEscrituras.getColumnModel().getColumn(6).setMaxWidth(0);
        grillaListaEscrituras.getColumnModel().getColumn(6).setMinWidth(0);
        grillaListaEscrituras.getColumnModel().getColumn(6).setMaxWidth(0);
        grillaListaEscrituras.getColumnModel().getColumn(6).setMinWidth(0);

        javax.swing.GroupLayout panelListaEscriturasLayout = new javax.swing.GroupLayout(panelListaEscrituras);
        panelListaEscrituras.setLayout(panelListaEscriturasLayout);
        panelListaEscriturasLayout.setHorizontalGroup(
            panelListaEscriturasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
            .addGroup(panelListaEscriturasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelListaEscriturasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelListaEscriturasLayout.createSequentialGroup()
                        .addComponent(labelTituloEscrituras)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelListaEscriturasLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonCancelar)))
                .addContainerGap())
        );
        panelListaEscriturasLayout.setVerticalGroup(
            panelListaEscriturasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelListaEscriturasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTituloEscrituras)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelListaEscrituras, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelListaEscrituras, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCancelarActionPerformed
    {//GEN-HEADEREND:event_botonCancelarActionPerformed
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        Principal.removeVentanaActivas(ventanaListaEscritura);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void grillaListaEscriturasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grillaListaEscriturasMouseClicked
        DtoEscritura escrituraSeleccionada = null;

        TableModel miGrilla = grillaListaEscrituras.getModel();
        int filaSeleccionada = grillaListaEscrituras.getSelectedRow();
        filaSeleccionada = grillaListaEscrituras.convertRowIndexToModel(filaSeleccionada);

        int filas = miGrilla.getRowCount();

        for (int i = 0; i < filas; i++)
        {
            if (i == filaSeleccionada)
            {
                escrituraSeleccionada = (DtoEscritura) miGrilla.getValueAt(i, 6);
                break;
            }
        }

        switch (formularioInvocador)
        {
            case ConstantesGui.MODIFICAR_ESCRITURA:
            {
                DetalleEscritura detalleEscrituraForm = new DetalleEscritura();
                detalleEscrituraForm.setFormularioInvocador(ConstantesGui.MODIFICAR_ESCRITURA);
                if (escrituraSeleccionada.getFechaInscripcion() != null)
                {
                    JOptionPane.showMessageDialog(this, "<HTML>La Escritura seleccionada no se puede modificar<BR>porque ya ha sido inscripta.</HTML>", "ERROR", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    Boolean ok = detalleEscrituraForm.cargarFormularioModificarEscritura(escrituraSeleccionada);

                    if (ok)
                    {
                        Principal.cargarFormulario(detalleEscrituraForm);
                        Principal.setVentanasActivas(DetalleEscritura.getVentanaDetalleEscritura());
                    }
                }
                salir();
                break;
            }
            case ConstantesGui.GENERAR_TESTIMONIO:
            {
                GenerarTestimonio generarTestimonioForm = new GenerarTestimonio();
                Boolean formGenerar = generarTestimonioForm.cargarFormulario(escrituraSeleccionada);

                
                if (formGenerar)
                {
                    Principal.cargarFormulario(generarTestimonioForm);
                    Principal.setVentanasActivas(GenerarTestimonio.getVentanaGenerarTestimonio());
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "No se pudo cargar el formulario de Generar Testimonio", "Error", JOptionPane.ERROR_MESSAGE);
                }
                salir();
                break;
            }
            case ConstantesGui.VERIFICAR_TESTIMONIO:
            {
                VerificarTestimonio verificarTestimonioForm = new VerificarTestimonio();
                verificarTestimonioForm.cargarFormulario(escrituraSeleccionada);
                
                Principal.cargarFormulario(verificarTestimonioForm);
                Principal.setVentanasActivas(VerificarTestimonio.getVentanaVerificarTestimonio());
                break;
            }
            case ConstantesGui.INGRESAR_PARA_INSCRIPCION:
            {
                Boolean ingresado = miController.verificarTestimonioIngresadoParaInscribir(escrituraSeleccionada);

                if (!ingresado)
                {
                    IngresarParaInscripcion ingresarParaInscripcionForm = new IngresarParaInscripcion();
                    Boolean cargada = ingresarParaInscripcionForm.cargarFormulario(escrituraSeleccionada);

                    if (cargada)
                    {
                        Principal.cargarFormulario(ingresarParaInscripcionForm);
                        Principal.setVentanasActivas(IngresarParaInscripcion.getVentanaIngresarParaInscribir());
                        salir();
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "El testimonio ya ha sido ingresado para inscribir.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                }


                break;
            }
            case ConstantesGui.REGISTRAR_REINGRESO:
            {
                RegistrarReingreso registrarReingresoForm = new RegistrarReingreso();
                Boolean cargada = registrarReingresoForm.cargarFormulario(escrituraSeleccionada);

                if (cargada)
                {
                    Principal.cargarFormulario(registrarReingresoForm);
                    Principal.setVentanasActivas(RegistrarReingreso.getVentanaRegistrarReingreso());
                }

                salir();
                break;
            }
            case ConstantesGui.REGISTRAR_INSCRIPCION:
            {
                RegistrarInscripcion registrarInscripcionForm = new RegistrarInscripcion();
                Boolean cargada = registrarInscripcionForm.cargarFormulario(escrituraSeleccionada);

                if (cargada)
                {
                    Principal.cargarFormulario(registrarInscripcionForm);
                    Principal.setVentanasActivas(RegistrarInscripcion.getVentanaRegistrarInscripcion());
                }

                salir();

                break;
            }
            case ConstantesGui.RETIRAR_TESTIMONIO:
            {
                Boolean seInscribe = miController.verificarSeInscribeEscritura(escrituraSeleccionada);
                Boolean cargado = false;
                RetirarTestimonio retirarTestimonioForm = new RetirarTestimonio();

                if (seInscribe)
                {
                    if (escrituraSeleccionada.getFechaInscripcion() == null)
                    {
                        JOptionPane.showMessageDialog(this, "<HTML>La Escritura se inscribe.<BR>No se puede retirar el testimonio asociado,<BR> hasta que se encuentre inscripto.</HTML>", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                    }
                    else
                    {
                        cargado = retirarTestimonioForm.cargarFormulario(escrituraSeleccionada);
                    }
                }
                else
                {
                    cargado = retirarTestimonioForm.cargarFormulario(escrituraSeleccionada);
                }
                if (cargado)
                {
                    Principal.cargarFormulario(retirarTestimonioForm);
                    Principal.setVentanasActivas(RetirarTestimonio.getVentanaRetirarTestimonio());
                }
                break;
            }
            case ConstantesGui.DETALLE_GESTION:
            {
                DetalleEscritura detalleEscrituraForm = new DetalleEscritura();
                detalleEscrituraForm.setFormularioInvocador(ConstantesGui.DETALLE_GESTION);
                detalleEscrituraForm.cargarFormularioModificarEscritura(escrituraSeleccionada);
                
                Principal.cargarFormulario(detalleEscrituraForm);
                Principal.setVentanasActivas(DetalleEscritura.getVentanaDetalleEscritura());
                break;
            }
            case ConstantesGui.BUSCAR_ESCRITURA:
            {
                DetalleEscritura detalleEscrituraForm = new DetalleEscritura();
                detalleEscrituraForm.setFormularioInvocador(ConstantesGui.BUSCAR_ESCRITURA);
                detalleEscrituraForm.cargarFormularioModificarEscritura(escrituraSeleccionada);
                
                Principal.cargarFormulario(detalleEscrituraForm);
                Principal.setVentanasActivas(DetalleEscritura.getVentanaDetalleEscritura());
                break;
            }
        }
    }//GEN-LAST:event_grillaListaEscriturasMouseClicked

    private void grillaListaEscriturasMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grillaListaEscriturasMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_grillaListaEscriturasMouseEntered
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCancelar;
    private javax.swing.JTable grillaListaEscrituras;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JLabel labelTituloEscrituras;
    private javax.swing.JPanel panelListaEscrituras;
    // End of variables declaration//GEN-END:variables
}
