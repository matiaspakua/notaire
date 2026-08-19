/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.gestiones.testimonios;

import com.licensis.notaire.dto.DtoCopia;
import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoMovimientoTestimonio;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoTestimonio;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.negocio.ControllerNegocio;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author matias
 */
public class VerificarTestimonio extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaVerificarTestimonio = new JMenuItem("Ventana Verificar Testimonio");
    private ControllerNegocio miController = ControllerNegocio.getInstancia();

    /**
     * Creates new form VerificarTestimonio
     */
    public VerificarTestimonio()
    {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioNormalVertical);
        grillaTestimonios.setAutoCreateRowSorter(true);
    }

    private void salir()
    {
        this.dispose();
    }

    public static JMenuItem getVentanaVerificarTestimonio()
    {
        return ventanaVerificarTestimonio;
    }

    public void cargarFormulario(DtoEscritura miDtoEscritura)
    {

        List<DtoTestimonio> testimoniosEscritura = miController.obtenerTestimoniosEscritura(miDtoEscritura);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        campoNumeroEscritura.setText(new Integer(miDtoEscritura.getNumero()).toString());
        campoFechaEscritura.setText(formatter.format(miDtoEscritura.getFechaEscrituracion()).toString());
        campoFolioDesde.setText(new Integer(miDtoEscritura.getFolios().get(0).getNumero()).toString());
        campoFolioHasta.setText(new Integer(miDtoEscritura.getFolios().get(miDtoEscritura.getFolios().size() - 1).getNumero()).toString());

        DtoPersona escribano = miController.obtenerEscribanoEscritura(miDtoEscritura);
        campoRegistro.setText(escribano.getRegistroEscribano().toString());

        if (testimoniosEscritura != null && !testimoniosEscritura.isEmpty())
        {
            cargarGrilla(testimoniosEscritura);
        } else
        {
            JOptionPane.showMessageDialog(this, "No existen testimonios generados para la Escritura seleccionada.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            salir();
        }

    }

    private void cargarGrilla(List<DtoTestimonio> testimonios)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        for (Iterator<DtoTestimonio> it = testimonios.iterator(); it.hasNext();)
        {
            DtoTestimonio dtoTestimonio = it.next();
            Boolean inscripto = false;
            String fechaIngreso = " ";
            String fechaSalida = " ";
            String fechaInscripcion = " ";
            String matricula = " ";
            String numeroCarton = " ";

            List<DtoMovimientoTestimonio> movimientoTestimonios = miController.obtenerMovimientosTestimonio(dtoTestimonio);
            List<DtoCopia> copias = miController.obtenerCopiasTestimonio(dtoTestimonio);

            if (movimientoTestimonios != null && !movimientoTestimonios.isEmpty())
            {
                for (Iterator<DtoMovimientoTestimonio> it1 = movimientoTestimonios.iterator(); it1.hasNext();)
                {
                    DtoMovimientoTestimonio dtoMovimientoTestimonio = it1.next();
                    if (dtoMovimientoTestimonio.getFechaIngreso() != null)
                    {
                        fechaIngreso = formatter.format(dtoMovimientoTestimonio.getFechaIngreso()).toString();
                        numeroCarton = new Integer(dtoMovimientoTestimonio.getNumeroCarton()).toString();
                    }
                    if (dtoMovimientoTestimonio.isInscripta())
                    {
                        DtoEscritura miDtoEscritura = miController.buscarEscritura(dtoTestimonio.getEscritura());
                        fechaInscripcion = formatter.format(dtoMovimientoTestimonio.getFechaInscripcion()).toString();
                        matricula = miDtoEscritura.getMatriculaInscripcion();
                        inscripto = true;
                    } else
                    {
                        inscripto = false;
                    }
                    Integer numero = (Integer) dtoTestimonio.getNumero();
                    String fechaImpresion = null;
                    fechaSalida = formatter.format(dtoMovimientoTestimonio.getFechaSalida()).toString();

                    if (copias != null && !copias.isEmpty())
                    {
                        fechaImpresion = formatter.format(copias.get(0).getFechaImpresion()).toString();
                        String observaciones = " ";

                        if (dtoTestimonio.getObservaciones() != null && !dtoTestimonio.getObservaciones().equals(""))
                        {
                            observaciones = observaciones + dtoTestimonio.getObservaciones();
                        }

                        for (Iterator<DtoCopia> it2 = copias.iterator(); it2.hasNext();)
                        {
                            DtoCopia dtoCopia = it2.next();

                            if (dtoCopia.getObservaciones() != null && !dtoCopia.getObservaciones().equals(""))
                            {
                                observaciones = observaciones + " " + dtoCopia.getObservaciones();
                            }
                        }

                        Object[] datos =
                        {
                            numero,
                            fechaImpresion,
                            copias.size(),
                            observaciones,
                            fechaIngreso,
                            numeroCarton,
                            fechaSalida,
                            inscripto,
                            fechaInscripcion,
                            matricula
                        };
                        ((DefaultTableModel) grillaTestimonios.getModel()).addRow(datos);
                    }
                }
            } else
            {
                Integer numero = (Integer) dtoTestimonio.getNumero();
                String fechaImpresion = null;

                if (copias != null && !copias.isEmpty())
                {
                    fechaImpresion = formatter.format(copias.get(0).getFechaImpresion()).toString();

                    String observaciones = " ";

                    if (dtoTestimonio.getObservaciones() != null && !dtoTestimonio.getObservaciones().equals(""))
                    {
                        observaciones = observaciones + dtoTestimonio.getObservaciones();
                    }

                    for (Iterator<DtoCopia> it2 = copias.iterator(); it2.hasNext();)
                    {
                        DtoCopia dtoCopia = it2.next();

                        if (dtoCopia.getObservaciones() != null && !dtoCopia.getObservaciones().equals(""))
                        {
                            observaciones = observaciones + " " + dtoCopia.getObservaciones();
                        }
                    }

                    Object[] datos =
                    {
                        numero,
                        fechaImpresion,
                        copias.size(),
                        observaciones,
                        fechaIngreso,
                        numeroCarton,
                        fechaSalida,
                        inscripto,
                        fechaInscripcion,
                        matricula
                    };
                    ((DefaultTableModel) grillaTestimonios.getModel()).addRow(datos);
                }
            }

        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelVerificarTestimonio = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        botonCerrar = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        grillaTestimonios = new javax.swing.JTable();
        campoFolioHasta = new javax.swing.JTextField();
        campoFolioDesde = new javax.swing.JTextField();
        campoNumeroEscritura = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        campoFechaEscritura = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        campoRegistro = new javax.swing.JTextField();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Verificar Testimonio");
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
        jLabel1.setText("Verificar Testimonio");

        botonCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar.png"))); // NOI18N
        botonCerrar.setText("Cerrar");
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });

        grillaTestimonios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Número de Testimonio", "Fecha de Expedición", "Cantidad de Copias", "Observaciones", "Fecha Ingreso", "Numero Carton", "Fecha Salida", "Inscripto", "Fecha Inscripcion", "Matricula"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(grillaTestimonios);

        campoFolioHasta.setEditable(false);

        campoFolioDesde.setEditable(false);

        campoNumeroEscritura.setEditable(false);

        jLabel6.setText("Inclusive");

        jLabel3.setText("Fecha:");

        jLabel10.setText("Desde:");

        jLabel5.setText("Hasta:");

        jLabel4.setText("Folios Utilizados:");

        jLabel2.setText("Número de Escritura:");

        campoFechaEscritura.setEditable(false);

        jLabel7.setText("Registro Escribano:");

        campoRegistro.setEditable(false);

        javax.swing.GroupLayout panelVerificarTestimonioLayout = new javax.swing.GroupLayout(panelVerificarTestimonio);
        panelVerificarTestimonio.setLayout(panelVerificarTestimonioLayout);
        panelVerificarTestimonioLayout.setHorizontalGroup(
            panelVerificarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVerificarTestimonioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelVerificarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 951, Short.MAX_VALUE)
                    .addGroup(panelVerificarTestimonioLayout.createSequentialGroup()
                        .addGroup(panelVerificarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(panelVerificarTestimonioLayout.createSequentialGroup()
                                .addGroup(panelVerificarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelVerificarTestimonioLayout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel10))
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelVerificarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(campoNumeroEscritura)
                                    .addComponent(campoFolioDesde, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                                    .addComponent(campoRegistro))
                                .addGap(18, 18, 18)
                                .addGroup(panelVerificarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelVerificarTestimonioLayout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(12, 12, 12)
                                        .addComponent(campoFolioHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel6))
                                    .addGroup(panelVerificarTestimonioLayout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(campoFechaEscritura, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 502, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelVerificarTestimonioLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonCerrar)))
                .addContainerGap())
        );
        panelVerificarTestimonioLayout.setVerticalGroup(
            panelVerificarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVerificarTestimonioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(panelVerificarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(campoNumeroEscritura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(campoFechaEscritura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelVerificarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel4)
                    .addComponent(campoFolioDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoFolioHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelVerificarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(campoRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelVerificarTestimonio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelVerificarTestimonio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCerrarActionPerformed
    {//GEN-HEADEREND:event_botonCerrarActionPerformed
        salir();
    }//GEN-LAST:event_botonCerrarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaVerificarTestimonio);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCerrar;
    private javax.swing.JTextField campoFechaEscritura;
    private javax.swing.JTextField campoFolioDesde;
    private javax.swing.JTextField campoFolioHasta;
    private javax.swing.JTextField campoNumeroEscritura;
    private javax.swing.JTextField campoRegistro;
    private javax.swing.JTable grillaTestimonios;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel panelVerificarTestimonio;
    // End of variables declaration//GEN-END:variables
}
