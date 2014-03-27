/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.gestiones.inscripciones;

import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoMovimientoTestimonio;
import com.licensis.notaire.dto.DtoTestimonio;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.gui.Principal;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorValidaciones;

/**
 *
 * @author matias
 */
public class RegistrarReingreso extends javax.swing.JInternalFrame
{

    private static JMenuItem ventanaRegistrarReingreso = new JMenuItem("Ventana Registrar Reingreso");
    private static Boolean estadoFormulario = Boolean.FALSE;
    private DtoEscritura escrituraSeleccionada;
    private ControllerNegocio miController = ControllerNegocio.getInstancia();
    private List<DtoTestimonio> testimonios = null;
    private List<DtoMovimientoTestimonio> movimientos = null;

    /**
     * Creates new form RegistrarReingreso
     */
    public RegistrarReingreso() {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioNormalVertical);
    }

    private void salir() {
        this.dispose();
    }

    public static JMenuItem getVentanaRegistrarReingreso() {
        return ventanaRegistrarReingreso;
    }

    public Boolean cargarFormulario(DtoEscritura dtoEscritura) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        
        escrituraSeleccionada = miController.buscarEscritura(dtoEscritura);
        Boolean cargada = true;
        testimonios = miController.obtenerTestimoniosEscritura(dtoEscritura);

        if (testimonios != null && !testimonios.isEmpty())
        {
            DtoTestimonio ultimoTestimonio = testimonios.get(testimonios.size() - 1);

            movimientos = miController.obtenerMovimientosTestimonio(ultimoTestimonio);

            if (movimientos != null && !movimientos.isEmpty())
            {
                if (movimientos.get(movimientos.size() - 1).getFechaInscripcion() != null)
                {
                    JOptionPane.showMessageDialog(this, "<HTML>La Escritura ya se encuentra inscripta <BR>con fecha: " + movimientos.get(movimientos.size() - 1).getFechaInscripcion().toString() + " <BR>y matricula: " + escrituraSeleccionada.getMatriculaInscripcion() + "</HTML>", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                    cargada = false;
                }
                else if (movimientos.get(movimientos.size() - 1).getFechaIngreso() != null)
                {
                    cargada = true;

                    this.escrituraSeleccionada = dtoEscritura;
                    this.campoNumeroEscritura.setText(Integer.toString(dtoEscritura.getNumero()));
                    this.campoFecha.setText(formatter.format(dtoEscritura.getFechaEscrituracion()).toString());
                    this.campoFolioDesde.setText(new Integer(dtoEscritura.getFolios().get(0).getNumero()).toString());
                    this.campoFolioHasta.setText(new Integer(dtoEscritura.getFolios().get(dtoEscritura.getFolios().size() - 1).getNumero()).toString());

                    List<DtoTramite> tramitesEscritura = dtoEscritura.getTramites();
                    DefaultListModel lista = new DefaultListModel();

                    for (Iterator<DtoTramite> it = tramitesEscritura.iterator(); it.hasNext();)
                    {
                        DtoTramite dtoTramite = it.next();
                        if (dtoTramite.getInmueble() != null)
                        {
                            String nomenclatura = dtoTramite.getInmueble().getNomenclaturaCatastral();
                            lista.addElement(nomenclatura);
                        }
                    }

                    listaNomenclaturas.setModel(lista);

                    campoNumeroTestimonio.setText(new Integer(testimonios.get(testimonios.size() - 1).getNumero()).toString());
                }
            }
            else
            {
                JOptionPane.showMessageDialog(this, "La Escritura seleccionada no tiene Testimonios ingresados para Inscribir.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                cargada = false;
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "La Escritura seleccionada no tiene Testimonios generados para Inscribir.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            cargada = false;
        }

        return cargada;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelRegistrarReingreso = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        campoFecha = new javax.swing.JTextField();
        campoFolioHasta = new javax.swing.JTextField();
        campoFolioDesde = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listaNomenclaturas = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        botonCancelar = new javax.swing.JButton();
        campoNumeroEscritura = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        campoNumeroTestimonio = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        campoNumeroCarton = new javax.swing.JTextField();
        selectorFechaIngreso = new com.toedter.calendar.JDateChooser();
        jScrollPane3 = new javax.swing.JScrollPane();
        campoObservaciones = new javax.swing.JTextArea();
        jLabel11 = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        checkBoxObservado = new javax.swing.JCheckBox();
        jLabel15 = new javax.swing.JLabel();
        selectorFechaSalida = new com.toedter.calendar.JDateChooser();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Registrar Reingreso");
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

        jLabel9.setText("Numero de Carton:");

        campoFecha.setEditable(false);

        campoFolioHasta.setEditable(false);

        campoFolioDesde.setEditable(false);

        jLabel8.setText("Detalle Inscripción:");

        listaNomenclaturas.setEnabled(false);
        jScrollPane1.setViewportView(listaNomenclaturas);

        jLabel4.setText("Folios Utilizados:");

        jLabel7.setText("Nomenclaturas:");

        botonCancelar.setText("Cancelar");
        botonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCancelarActionPerformed(evt);
            }
        });

        campoNumeroEscritura.setEditable(false);

        jLabel10.setText("Desde:");

        jLabel5.setText("Hasta:");

        jLabel6.setText("Inclusive");

        jLabel3.setText("Fecha:");

        jLabel2.setText("Número de Escritura:");

        jLabel13.setText("Numero Testimonio:");

        campoNumeroTestimonio.setEditable(false);

        jLabel12.setText("Observaciones:");

        campoObservaciones.setColumns(20);
        campoObservaciones.setRows(5);
        jScrollPane3.setViewportView(campoObservaciones);

        jLabel11.setText("Fecha de Reingreso:");

        botonAceptar.setText("Aceptar");
        botonAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAceptarActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Reingresar para Inscripción");

        jLabel14.setText("Observado:");

        jLabel15.setText("Fecha Salida:");

        javax.swing.GroupLayout panelRegistrarReingresoLayout = new javax.swing.GroupLayout(panelRegistrarReingreso);
        panelRegistrarReingreso.setLayout(panelRegistrarReingresoLayout);
        panelRegistrarReingresoLayout.setHorizontalGroup(
            panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRegistrarReingresoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRegistrarReingresoLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelRegistrarReingresoLayout.createSequentialGroup()
                        .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelRegistrarReingresoLayout.createSequentialGroup()
                                    .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel13)
                                        .addComponent(jLabel15))
                                    .addGap(22, 22, 22)
                                    .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(campoNumeroTestimonio, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(selectorFechaSalida, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel1)
                                    .addGroup(panelRegistrarReingresoLayout.createSequentialGroup()
                                        .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel7))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(panelRegistrarReingresoLayout.createSequentialGroup()
                                                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(campoNumeroEscritura)
                                                    .addGroup(panelRegistrarReingresoLayout.createSequentialGroup()
                                                        .addComponent(jLabel10)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(campoFolioDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(18, 18, 18)
                                                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addGroup(panelRegistrarReingresoLayout.createSequentialGroup()
                                                        .addComponent(jLabel3)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(campoFecha))
                                                    .addGroup(panelRegistrarReingresoLayout.createSequentialGroup()
                                                        .addComponent(jLabel5)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(campoFolioHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(jLabel6))))
                                            .addComponent(jScrollPane1)))
                                    .addGroup(panelRegistrarReingresoLayout.createSequentialGroup()
                                        .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel9)
                                            .addComponent(jLabel11))
                                        .addGap(18, 18, 18)
                                        .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(checkBoxObservado)
                                            .addComponent(campoNumeroCarton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(selectorFechaIngreso, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel12))))
                        .addGap(0, 217, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelRegistrarReingresoLayout.setVerticalGroup(
            panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRegistrarReingresoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(4, 4, 4)
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(campoNumeroEscritura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(campoFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel4)
                    .addComponent(campoFolioDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoFolioHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(campoNumeroTestimonio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel15)
                    .addComponent(selectorFechaSalida, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(campoNumeroCarton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addComponent(selectorFechaIngreso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(checkBoxObservado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(panelRegistrarReingresoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRegistrarReingreso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRegistrarReingreso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaRegistrarReingreso);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCancelarActionPerformed
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAceptarActionPerformed

        AdministradorValidaciones admin = AdministradorValidaciones.getInstancia();

        if (!admin.validarCampoVacio(campoNumeroCarton.getText())
                && selectorFechaIngreso.getDate() != null
                && selectorFechaSalida.getDate() != null)
        {
            
            DtoMovimientoTestimonio miDtoMovimientoTestimonioViejo = miController.buscarMovimientoTestimonio(movimientos.get(movimientos.size() - 1));

            miDtoMovimientoTestimonioViejo.setFechaSalida(selectorFechaSalida.getDate());
            try
            {
                Boolean modificado = miController.modificarMovimientoTestimonio(miDtoMovimientoTestimonioViejo);

                if (modificado)
                {
                    DtoTestimonio miDtoTestimonio = testimonios.get(testimonios.size() - 1);
                    miDtoTestimonio.setObservado(checkBoxObservado.isSelected());

                    DtoMovimientoTestimonio miDtoMovimientoTestimonio = new DtoMovimientoTestimonio();

                    miDtoMovimientoTestimonio.setTestimonio(miDtoTestimonio);

                    miDtoMovimientoTestimonio.setNumeroCarton(Integer.parseInt(campoNumeroCarton.getText()));
                    miDtoMovimientoTestimonio.setFechaIngreso(selectorFechaIngreso.getDate());
                    miDtoMovimientoTestimonio.setObservaciones(campoObservaciones.getText());

                    int opcion = JOptionPane.showConfirmDialog(this, "<HTML>¿Esta seguro que desea reingresar <BR> el testimonio numero: " + miDtoTestimonio.getNumero() + "<BR>de la escritura numero: " + escrituraSeleccionada.getNumero() + "?</HTML>", "CONFIRMACION", JOptionPane.YES_NO_OPTION);

                    if (opcion == JOptionPane.YES_OPTION)
                    {
                        try
                        {
                            Boolean creado = miController.crearMovimientoTestimonio(miDtoMovimientoTestimonio);

                            if (creado)
                            {
                                JOptionPane.showMessageDialog(this, "Se ha registrado el reingreso de inscripcion.", "INFORMACION", JOptionPane.INFORMATION_MESSAGE);
                                salir();
                            }
                        }
                        catch (ClassModifiedException ex)
                        {
                            JOptionPane.showMessageDialog(this, "El Testimonio ha sido recientemente modificado por otro usuario.", "ERROR", JOptionPane.ERROR_MESSAGE);
                            salir();
                        }
                        catch (ClassEliminatedException ex)
                        {
                            Logger.getLogger(RegistrarReingreso.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }
            catch (ClassEliminatedException ex)
            {
                Logger.getLogger(RegistrarReingreso.class.getName()).log(Level.SEVERE, null, ex);
                salir();
            }
            catch (ClassModifiedException ex)
            {
                JOptionPane.showMessageDialog(this, "El Testimonio ha sido recientemente modificado por otro usuario.", "ERROR", JOptionPane.ERROR_MESSAGE);
                salir();
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "<HTML>Debe completar al menos el Fecha de Salida,<BR>Numero de Carton y Fecha de Ingreso.</HTML>", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_botonAceptarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JTextField campoFecha;
    private javax.swing.JTextField campoFolioDesde;
    private javax.swing.JTextField campoFolioHasta;
    private javax.swing.JTextField campoNumeroCarton;
    private javax.swing.JTextField campoNumeroEscritura;
    private javax.swing.JTextField campoNumeroTestimonio;
    private javax.swing.JTextArea campoObservaciones;
    private javax.swing.JCheckBox checkBoxObservado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList listaNomenclaturas;
    private javax.swing.JPanel panelRegistrarReingreso;
    private com.toedter.calendar.JDateChooser selectorFechaIngreso;
    private com.toedter.calendar.JDateChooser selectorFechaSalida;
    // End of variables declaration//GEN-END:variables
}
