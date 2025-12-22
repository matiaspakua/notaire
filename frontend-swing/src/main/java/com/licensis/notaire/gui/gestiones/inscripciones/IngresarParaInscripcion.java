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
public class IngresarParaInscripcion extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaIngresarParaInscribir = new JMenuItem("Ventana Ingresar Para Inscribir");
    private DtoEscritura escrituraSeleccionada;
    private ControllerNegocio miController = ControllerNegocio.getInstancia();
    private List<DtoTestimonio> testimonios = null;

    /**
     * Creates new form RegistrarInscripcion
     */
    public IngresarParaInscripcion()
    {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioNormalVertical);
    }

    private void salir()
    {
        this.dispose();
    }

    public static JMenuItem getVentanaIngresarParaInscribir()
    {
        return ventanaIngresarParaInscribir;
    }

    public Boolean cargarFormulario(DtoEscritura dtoEscritura)
    {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        escrituraSeleccionada = miController.buscarEscritura(dtoEscritura);
        Boolean cargada = true;
        testimonios = miController.obtenerTestimoniosEscritura(dtoEscritura);

        if (testimonios != null && !testimonios.isEmpty())
        {
            DtoTestimonio ultimoTestimonio = testimonios.get(testimonios.size() - 1);

            List<DtoMovimientoTestimonio> movimientos = miController.obtenerMovimientosTestimonio(ultimoTestimonio);

            if (movimientos != null && !movimientos.isEmpty())
            {
                if (movimientos.get(movimientos.size() - 1).getFechaInscripcion() != null)
                {
                    JOptionPane.showMessageDialog(this, "<HTML>La Escritura ya se encuentra inscripta <BR>con fecha: " + movimientos.get(movimientos.size() - 1).getFechaInscripcion().toString() + " <BR>y matricula: " + escrituraSeleccionada.getMatriculaInscripcion() + "</HTML>", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                    cargada = false;
                } else if (movimientos.get(movimientos.size() - 1).getFechaIngreso() != null)
                {
                    JOptionPane.showMessageDialog(this, "<HTML>La Escritura ya se encuentra ingresada para inscribir<BR> con fecha: " + movimientos.get(movimientos.size() - 1).getFechaIngreso() + "<BR> y numero de carton: " + movimientos.get(movimientos.size() - 1).getNumeroCarton() + "</HTML>", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                    cargada = false;
                }
            } else
            {
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
        } else
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

        jScrollPane2 = new javax.swing.JScrollPane();
        panelRegistrarInscripcion = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        campoNumeroEscritura = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listaNomenclaturas = new javax.swing.JList();
        jLabel8 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        campoFolioDesde = new javax.swing.JTextField();
        campoFolioHasta = new javax.swing.JTextField();
        campoFecha = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        campoNumeroCarton = new javax.swing.JTextField();
        selectorFechaIngreso = new com.toedter.calendar.JDateChooser();
        jScrollPane3 = new javax.swing.JScrollPane();
        campoObservaciones = new javax.swing.JTextArea();
        jLabel13 = new javax.swing.JLabel();
        campoNumeroTestimonio = new javax.swing.JTextField();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Ingresar Para Inscribir");
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
        jLabel1.setText("Ingresar para Inscripción");

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

        campoNumeroEscritura.setEditable(false);

        jLabel10.setText("Desde:");

        jLabel5.setText("Hasta:");

        jLabel6.setText("Inclusive");

        jLabel3.setText("Fecha:");

        jLabel2.setText("Número de Escritura:");

        jLabel4.setText("Folios Utilizados:");

        jLabel7.setText("Nomenclaturas:");

        listaNomenclaturas.setEnabled(false);
        jScrollPane1.setViewportView(listaNomenclaturas);

        jLabel8.setText("Detalle Inscripción:");

        campoFolioDesde.setEditable(false);

        campoFolioHasta.setEditable(false);

        campoFecha.setEditable(false);

        jLabel9.setText("Numero de Carton:");

        jLabel11.setText("Fecha de Ingreso:");

        jLabel12.setText("Observaciones:");

        campoNumeroCarton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoNumeroCartonActionPerformed(evt);
            }
        });

        campoObservaciones.setColumns(20);
        campoObservaciones.setRows(5);
        jScrollPane3.setViewportView(campoObservaciones);

        jLabel13.setText("Numero Testimonio:");

        campoNumeroTestimonio.setEditable(false);

        javax.swing.GroupLayout panelRegistrarInscripcionLayout = new javax.swing.GroupLayout(panelRegistrarInscripcion);
        panelRegistrarInscripcion.setLayout(panelRegistrarInscripcionLayout);
        panelRegistrarInscripcionLayout.setHorizontalGroup(
            panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRegistrarInscripcionLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6))
                    .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                        .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSeparator1)
                            .addComponent(jLabel1)
                            .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                        .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(campoNumeroEscritura)
                                            .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                                .addComponent(jLabel10)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(campoFolioDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(18, 18, 18)
                                        .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                                .addComponent(jLabel3)
                                                .addGap(18, 18, 18)
                                                .addComponent(campoFecha))
                                            .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addGap(18, 18, 18)
                                                .addComponent(campoFolioHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel6))))
                                    .addComponent(jScrollPane1)))
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(campoNumeroTestimonio, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                            .addComponent(campoNumeroCarton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(jScrollPane3)
                                        .addComponent(selectorFechaIngreso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                        .addContainerGap(223, Short.MAX_VALUE))))
        );
        panelRegistrarInscripcionLayout.setVerticalGroup(
            panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(4, 4, 4)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(campoNumeroEscritura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(campoFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel4)
                    .addComponent(campoFolioDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoFolioHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(campoNumeroTestimonio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(campoNumeroCarton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addComponent(selectorFechaIngreso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jScrollPane2.setViewportView(panelRegistrarInscripcion);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonAceptarActionPerformed
    {//GEN-HEADEREND:event_botonAceptarActionPerformed
        AdministradorValidaciones admin = AdministradorValidaciones.getInstancia();

        if (!admin.validarCampoVacio(campoNumeroCarton.getText())
                && selectorFechaIngreso.getDate() != null)
        {
            try
            {
                DtoTestimonio miDtoTestimonio = testimonios.get(testimonios.size() - 1);
                DtoMovimientoTestimonio miDtoMovimientoTestimonio = new DtoMovimientoTestimonio();

                miDtoMovimientoTestimonio.setTestimonio(miDtoTestimonio);

                miDtoMovimientoTestimonio.setNumeroCarton(Integer.parseInt(campoNumeroCarton.getText()));
                miDtoMovimientoTestimonio.setFechaIngreso(selectorFechaIngreso.getDate());
                miDtoMovimientoTestimonio.setObservaciones(campoObservaciones.getText());

                Boolean creado = miController.crearMovimientoTestimonio(miDtoMovimientoTestimonio);

                if (creado)
                {
                    JOptionPane.showMessageDialog(this, "Se ha registrado el ingreso de inscripcion.", "INFORMACION", JOptionPane.INFORMATION_MESSAGE);
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
                Logger.getLogger(IngresarParaInscripcion.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
        {
            JOptionPane.showMessageDialog(this, "Debe completar al menos el Numero de Carton y Fecha de Ingreso.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_botonAceptarActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCancelarActionPerformed
    {//GEN-HEADEREND:event_botonCancelarActionPerformed
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaIngresarParaInscribir);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void campoNumeroCartonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoNumeroCartonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoNumeroCartonActionPerformed

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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList listaNomenclaturas;
    private javax.swing.JPanel panelRegistrarInscripcion;
    private com.toedter.calendar.JDateChooser selectorFechaIngreso;
    // End of variables declaration//GEN-END:variables
}
