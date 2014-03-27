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
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorValidaciones;

/**
 *
 * @author tefi
 */
public class RegistrarInscripcion extends javax.swing.JInternalFrame
{

    private static JMenuItem ventanaRegistrarInscripcion = new JMenuItem("Ventana Registrar Inscripcion");
    private DtoEscritura escrituraSeleccionada;
    private ControllerNegocio miController = ControllerNegocio.getInstancia();
    private List<DtoTestimonio> testimonios = null;
    private AdministradorValidaciones admin = AdministradorValidaciones.getInstancia();
    private List<DtoMovimientoTestimonio> movimientos;

    /**
     * Creates new form RegistrarInscripcion
     */
    public RegistrarInscripcion() {
        initComponents();
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioNormalVertical);
    }

    private void salir() {
        this.dispose();
    }

    public static JMenuItem getVentanaRegistrarInscripcion() {
        return ventanaRegistrarInscripcion;
    }

    @SuppressWarnings("unchecked")
    public Boolean cargarFormulario(DtoEscritura dtoEscritura) {
        Boolean cargardo = true;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        escrituraSeleccionada = miController.buscarEscritura(dtoEscritura);

        if (escrituraSeleccionada != null)
        {
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
                        cargardo = false;
                    }
                    else if (movimientos.get(movimientos.size() - 1).getFechaIngreso() != null)
                    {
                        cargardo = true;

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
                        campoNumeroCarton.setText(new Integer(movimientos.get(movimientos.size() - 1).getNumeroCarton()).toString());
                        campoFechaIngreso.setText(formatter.format(movimientos.get(movimientos.size() - 1).getFechaIngreso()).toString());
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "La Escritura seleccionada no tiene Testimonios ingresados para Inscribir.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                    cargardo = false;
                }
            }
            else
            {
                JOptionPane.showMessageDialog(this, "La Escritura seleccionada no tiene Testimonios generados para Inscribir.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                cargardo = false;
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Error grave, Accion cancelada: La escritura no existe.", "Error", JOptionPane.ERROR_MESSAGE);
            this.salir();
        }

        return cargardo;
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
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        campoFecha = new javax.swing.JTextField();
        campoFolioDesde = new javax.swing.JTextField();
        campoFolioHasta = new javax.swing.JTextField();
        campoNumeroEscritura = new javax.swing.JTextField();
        campoNumeroTestimonio = new javax.swing.JTextField();
        campoNumeroCarton = new javax.swing.JTextField();
        campoFechaIngreso = new javax.swing.JTextField();
        campoMatricula = new javax.swing.JTextField();
        selectorFechaInscripcion = new com.toedter.calendar.JDateChooser();
        selectorFechaSalida = new com.toedter.calendar.JDateChooser();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listaNomenclaturas = new javax.swing.JList();
        jLabel7 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        campoObservaciones = new javax.swing.JTextArea();
        jLabel11 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Registrar Inscripción");
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

        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

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

        campoFecha.setEditable(false);

        campoFolioDesde.setEditable(false);

        campoFolioHasta.setEditable(false);

        campoNumeroEscritura.setEditable(false);

        campoNumeroTestimonio.setEditable(false);

        campoNumeroCarton.setEditable(false);

        campoFechaIngreso.setEditable(false);

        jLabel5.setText("Hasta:");

        jLabel8.setText("Detalle Inscripción:");

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Registrar Inscripción");

        jLabel10.setText("Desde:");

        jLabel6.setText("Inclusive");

        jLabel3.setText("Fecha:");

        jLabel2.setText("Número de Escritura:");

        jLabel4.setText("Folios Utilizados:");

        listaNomenclaturas.setEnabled(false);
        jScrollPane1.setViewportView(listaNomenclaturas);

        jLabel7.setText("Nomenclaturas:");

        jLabel13.setText("Numero Testimonio:");

        campoObservaciones.setColumns(20);
        campoObservaciones.setRows(5);
        jScrollPane3.setViewportView(campoObservaciones);

        jLabel11.setText("Fecha de Ingreso:");

        jLabel9.setText("Numero de Carton:");

        jLabel12.setText("Observaciones:");

        jLabel14.setText("Fecha Inscripcion:");

        jLabel15.setText("Matricula de Inscripcion:");

        jLabel16.setText("Fecha Salida:");

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                        .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1)
                            .addComponent(jLabel8)
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
                            .addComponent(jSeparator1)
                            .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                            .addComponent(jLabel14)
                                            .addGap(33, 33, 33))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRegistrarInscripcionLayout.createSequentialGroup()
                                            .addComponent(jLabel15)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                    .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addGap(68, 68, 68)))
                                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane3)
                                    .addComponent(campoMatricula)
                                    .addComponent(selectorFechaInscripcion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel11))
                                .addGap(39, 39, 39)
                                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(selectorFechaSalida, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(campoFechaIngreso)
                                    .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                                        .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(campoNumeroCarton, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(campoNumeroTestimonio, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addGap(0, 97, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelRegistrarInscripcionLayout.setVerticalGroup(
            panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRegistrarInscripcionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addGap(10, 10, 10)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(campoNumeroTestimonio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(campoNumeroCarton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(campoFechaIngreso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(selectorFechaSalida, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectorFechaInscripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(campoMatricula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(panelRegistrarInscripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
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
            .addComponent(jScrollPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonAceptarActionPerformed
    {//GEN-HEADEREND:event_botonAceptarActionPerformed
        if (!admin.validarCampoVacio(campoMatricula.getText())
                && selectorFechaSalida != null
                && selectorFechaInscripcion != null)
        {
            try
            {
                DtoMovimientoTestimonio miDtoMovimientoTestimonio = miController.buscarMovimientoTestimonio(movimientos.get(movimientos.size() - 1));

                miDtoMovimientoTestimonio.setFechaSalida(selectorFechaSalida.getDate());
                miDtoMovimientoTestimonio.setFechaInscripcion(selectorFechaInscripcion.getDate());
                miDtoMovimientoTestimonio.setInscripta(true);
                miDtoMovimientoTestimonio.setObservaciones(campoObservaciones.getText());

                DtoEscritura miDtoEscritura = new DtoEscritura();
                miDtoEscritura.setMatriculaInscripcion(campoMatricula.getText());
                miDtoEscritura.setFechaInscripcion(selectorFechaInscripcion.getDate());
                miDtoEscritura.setEstado(ConstantesNegocio.ESCRITURA_INSCRIPTA);

                Boolean modificado = miController.modificarMovimientoTestimonioInscripcion(miDtoMovimientoTestimonio, miDtoEscritura);

                if (modificado)
                {
                    JOptionPane.showMessageDialog(this, "Se ha registrado la inscripcion.", "INFORMACION", JOptionPane.INFORMATION_MESSAGE);
                    salir();
                }
            }
            catch (ClassEliminatedException ex)
            {
                JOptionPane.showMessageDialog(this, "Error grave, Accion cancelada", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(RegistrarInscripcion.class.getName()).log(Level.SEVERE, null, ex);
                this.salir();
            }
            catch (ClassModifiedException ex)
            {
                JOptionPane.showMessageDialog(this, "El Testimonio ha sido recientemente modificado por otro usuario.", "ERROR", JOptionPane.ERROR_MESSAGE);
                salir();
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Debe completar todos los campos necesarios.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }



    }//GEN-LAST:event_botonAceptarActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCancelarActionPerformed
    {//GEN-HEADEREND:event_botonCancelarActionPerformed
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        Principal.removeVentanaActivas(ventanaRegistrarInscripcion);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JTextField campoFecha;
    private javax.swing.JTextField campoFechaIngreso;
    private javax.swing.JTextField campoFolioDesde;
    private javax.swing.JTextField campoFolioHasta;
    private javax.swing.JTextField campoMatricula;
    private javax.swing.JTextField campoNumeroCarton;
    private javax.swing.JTextField campoNumeroEscritura;
    private javax.swing.JTextField campoNumeroTestimonio;
    private javax.swing.JTextArea campoObservaciones;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
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
    private com.toedter.calendar.JDateChooser selectorFechaInscripcion;
    private com.toedter.calendar.JDateChooser selectorFechaSalida;
    // End of variables declaration//GEN-END:variables
}
