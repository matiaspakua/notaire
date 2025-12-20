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
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorValidaciones;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author matias
 */
public class RetirarTestimonio extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private static JMenuItem ventanaRetirarTestimonio = new JMenuItem("Ventana Retirar Testimonio");
    private ControllerNegocio miController = ControllerNegocio.getInstancia();
    private List<DtoCopia> copias;
    private List<DtoTestimonio> testimoniosEscritura;
    private AdministradorValidaciones admin = AdministradorValidaciones.getInstancia();
    private DtoEscritura miEscritura;
    private Boolean retirados = true;
    private Integer cantidadSinRetirar = 0;

    /**
     * Creates new form RetirarTestimonio
     */
    public RetirarTestimonio()
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

    public static JMenuItem getVentanaRetirarTestimonio()
    {
        return ventanaRetirarTestimonio;
    }

    public Boolean cargarFormulario(DtoEscritura miDtoEscritura)
    {
        miEscritura = miDtoEscritura;

        testimoniosEscritura = miController.obtenerTestimoniosEscritura(miDtoEscritura);

        Boolean cargado = true;
        if (testimoniosEscritura != null && !testimoniosEscritura.isEmpty())
        {
            DtoTestimonio dtoTestimonio = testimoniosEscritura.get(testimoniosEscritura.size() - 1);
            copias = miController.obtenerCopiasTestimonio(dtoTestimonio);

            for (Iterator<DtoCopia> it1 = copias.iterator(); it1.hasNext();)
            {
                DtoCopia dtoCopia = it1.next();

                if (dtoCopia.getFechaRetiro() == null)
                {
                    retirados = false;
                    cantidadSinRetirar++;
                }
            }

            if (retirados != true)
            {

                campoNumeroEscritura.setText(new Integer(miDtoEscritura.getNumero()).toString());
                campoFechaEscritura.setText(miDtoEscritura.getFechaEscrituracion().toString());
                campoFolioDesde.setText(new Integer(miDtoEscritura.getFolios().get(0).getNumero()).toString());
                campoFolioHasta.setText(new Integer(miDtoEscritura.getFolios().get(miDtoEscritura.getFolios().size() - 1).getNumero()).toString());
                campoCopias.setText(cantidadSinRetirar.toString());

                DtoPersona escribano = miController.obtenerEscribanoEscritura(miDtoEscritura);
                campoRegistro.setText(escribano.getRegistroEscribano().toString());

                cargarGrilla(testimoniosEscritura);
                cargarCombo();
            } else
            {
                JOptionPane.showMessageDialog(this, "<HTML>Las copias del testimonio de la escritura seleccionada<BR> han sido todos retirados.</HTML>", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                cargado = false;
                salir();
            }
        } else
        {
            JOptionPane.showMessageDialog(this, "No existen testimonios generados para la Escritura seleccionada.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            cargado = false;
            salir();
        }
        return cargado;

    }

    private void cargarCombo()
    {
        String nombre = "";
        ArrayList<DtoTipoIdentificacion> listaDtoIdentificaciones;
        listaDtoIdentificaciones = ControllerNegocio.getInstancia().listarTiposIdentificacion();

        for (int i = 0; i < listaDtoIdentificaciones.size(); i++)
        {
            nombre = listaDtoIdentificaciones.get(i).getNombre();
            comboTipoIdentificacion.addItem(nombre);
        }

    }

    private void cargarGrilla(List<DtoTestimonio> testimonios)
    {

        for (Iterator<DtoTestimonio> it = testimonios.iterator(); it.hasNext();)
        {
            DtoTestimonio dtoTestimonio = it.next();
            Boolean inscripto = false;

            List<DtoMovimientoTestimonio> movimientoTestimonios = miController.obtenerMovimientosTestimonio(dtoTestimonio);
            copias = miController.obtenerCopiasTestimonio(dtoTestimonio);

            if (movimientoTestimonios != null && !movimientoTestimonios.isEmpty())
            {
                for (Iterator<DtoMovimientoTestimonio> it1 = movimientoTestimonios.iterator(); it1.hasNext();)
                {
                    DtoMovimientoTestimonio dtoMovimientoTestimonio = it1.next();

                    if (dtoMovimientoTestimonio.isInscripta())
                    {
                        inscripto = true;
                    } else
                    {
                        inscripto = false;
                    }
                }
            }

            Integer numero = (Integer) dtoTestimonio.getNumero();
            String fechaImpresion = null;

            if (copias != null && !copias.isEmpty())
            {
                fechaImpresion = copias.get(0).getFechaImpresion().toString();

                Object[] datos =
                {
                    numero,
                    fechaImpresion,
                    inscripto,
                    copias.size(),
                    dtoTestimonio.getObservaciones()
                };
                ((DefaultTableModel) grillaTestimonios.getModel()).addRow(datos);
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

        panelRetirarTestimonio = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        campoFolioDesde = new javax.swing.JTextField();
        campoNumeroEscritura = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        grillaTestimonios = new javax.swing.JTable();
        campoFolioHasta = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        comboTipoIdentificacion = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        campoNumeroIdentificacion = new javax.swing.JTextField();
        campoNombreApellido = new javax.swing.JTextField();
        campoFechaEscritura = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        selectorCopias = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        campoRegistro = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        campoCopias = new javax.swing.JTextField();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Retirar Testimonio");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Retirar Testimonio");

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

        jLabel3.setText("Fecha:");

        jLabel10.setText("Desde:");

        jLabel6.setText("Inclusive");

        jLabel2.setText("Número de Escritura:");

        jLabel5.setText("Hasta:");

        jLabel4.setText("Folios Utilizados:");

        campoFolioDesde.setEditable(false);

        campoNumeroEscritura.setEditable(false);

        grillaTestimonios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Número de Testimonio", "Fecha de Expedición", "Inscripto", "Cantidad Copias", "Observaciones"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
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

        jLabel7.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        jLabel7.setText("Datos de quién retira");

        jLabel8.setText("Nombre y Apellido:");

        jLabel9.setText("Tipo Identificación:");

        jLabel11.setText("Número:");

        campoFechaEscritura.setEditable(false);

        jLabel12.setText("Cantidad de copias:");

        selectorCopias.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jLabel13.setText("Escribano Registro:");

        campoRegistro.setEditable(false);

        jLabel14.setText("Copias restantes sin retirar:");

        campoCopias.setEditable(false);

        javax.swing.GroupLayout panelRetirarTestimonioLayout = new javax.swing.GroupLayout(panelRetirarTestimonio);
        panelRetirarTestimonio.setLayout(panelRetirarTestimonioLayout);
        panelRetirarTestimonioLayout.setHorizontalGroup(
            panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRetirarTestimonioLayout.createSequentialGroup()
                        .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(selectorCopias, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel7)
                            .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(campoNombreApellido, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(comboTipoIdentificacion, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(campoNumeroIdentificacion, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                        .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelRetirarTestimonioLayout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(campoFolioDesde, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                                    .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                                            .addComponent(jLabel13)
                                            .addGap(30, 30, 30)
                                            .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(campoRegistro, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                                                .addComponent(campoNumeroEscritura)))
                                        .addComponent(jLabel2)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel3))
                                .addGap(18, 18, 18)
                                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                                        .addComponent(campoFolioHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel6))
                                    .addComponent(campoFechaEscritura)))
                            .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoCopias, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelRetirarTestimonioLayout.setVerticalGroup(
            panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(campoRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(campoNumeroEscritura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoFechaEscritura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel4)
                    .addComponent(campoFolioDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoFolioHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(campoCopias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(campoNombreApellido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(comboTipoIdentificacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(campoNumeroIdentificacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                        .addGap(21, 70, Short.MAX_VALUE)
                        .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelRetirarTestimonioLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelRetirarTestimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(selectorCopias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRetirarTestimonio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRetirarTestimonio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonAceptarActionPerformed
    {//GEN-HEADEREND:event_botonAceptarActionPerformed

        /*
         * ESTE CODIGO VA CUANDO IMPLEMENTEMOS LO DE PAGOS!
         *
         * DtoEscritura escritura = miController.buscarEscritura(miEscritura); Float saldo = 0.0f;
         * Boolean flag = true; for (Iterator<DtoTramite> it = escritura.getTramites().iterator();
         * it.hasNext();) { DtoTramite dtoTramite = it.next(); DtoPresupuesto presupuesto =
         * miController.buscarPresupuesto(dtoTramite.getPresupuesto());
         *
         * saldo = saldo + presupuesto.getSaldo(); }
         *
         * if(saldo > 0.0f){ flag = false; }
         *
         * if(flag = false){ int opcion = JOptionPane.showConfirmDialog(this, "<HTML>El Cliente
         * posee una deuda sobre la Escritura de $"+ saldo + ". <BR> ¿Desea seguir registrando el
         * retiro de copias?</HTML>", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE); }
         */
        if (retirados != true)
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            if (!admin.validarCampoVacio(campoNombreApellido.getText())
                    && !admin.validarCampoVacio(campoNumeroIdentificacion.getText()))
            {
                Integer cantidadCopiasRetiradas = (Integer) selectorCopias.getValue();
                String nombre = campoNombreApellido.getText();
                String tipoDoc = comboTipoIdentificacion.getSelectedItem().toString();
                String numeroDoc = campoNumeroIdentificacion.getText();
                String fechaActual = formatter.format(Calendar.getInstance().getTime()).toString();

                DtoTestimonio miTestimonio = testimoniosEscritura.get(testimoniosEscritura.size() - 1);
                copias = miController.obtenerCopiasTestimonio(miTestimonio);

                List<DtoCopia> copiasModificar = new ArrayList<>();
                Integer modificadasCopias = cantidadCopiasRetiradas;

                if (cantidadCopiasRetiradas <= copias.size()
                        && cantidadSinRetirar > 0
                        && cantidadCopiasRetiradas <= cantidadSinRetirar)
                {
                    try
                    {
                        for (int i = 0; i < copias.size(); i++)
                        {
                            DtoCopia miCopia = copias.get(i);

                            if (miCopia.getFechaRetiro() == null)
                            {
                                if (modificadasCopias != 0)
                                {
                                    miCopia.setFechaRetiro(Calendar.getInstance().getTime());
                                    miCopia.setObservaciones("Retiro " + nombre + " " + tipoDoc + " " + numeroDoc + ", el dia " + fechaActual + ".");
                                    copiasModificar.add(miCopia);
                                    modificadasCopias--;
                                }
                            }
                        }

                        Boolean modificadas = miController.modificarCopiasTestimonio(copias, miTestimonio);

                        if (modificadas)
                        {
                            JOptionPane.showMessageDialog(this, "El retiro ha sido registrado.", "INFORMACION", JOptionPane.INFORMATION_MESSAGE);
                            salir();
                        }
                    }
                    catch (ClassModifiedException ex)
                    {
                        JOptionPane.showMessageDialog(this, "Esta informacion ha sido recientemente modificada por otro usuario.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                        salir();
                    }
                    catch (ClassEliminatedException ex)
                    {
                        Logger.getLogger(RetirarTestimonio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else
                {
                    JOptionPane.showMessageDialog(this, "<HTML>El maximo de copias a retirar es de " + copias.size() + ".<BR>No puede retirar mas copias de <BR>las que existen sin retirar.</HTML>", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                }
            } else
            {
                JOptionPane.showMessageDialog(this, "Debe completar todos los datos.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            }
        } else
        {
            JOptionPane.showMessageDialog(this, "Todas las copias del testimonio, han sido retiradas.", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            salir();
        }
    }//GEN-LAST:event_botonAceptarActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_botonCancelarActionPerformed
    {//GEN-HEADEREND:event_botonCancelarActionPerformed
        salir();
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaRetirarTestimonio);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonCancelar;
    private javax.swing.JTextField campoCopias;
    private javax.swing.JTextField campoFechaEscritura;
    private javax.swing.JTextField campoFolioDesde;
    private javax.swing.JTextField campoFolioHasta;
    private javax.swing.JTextField campoNombreApellido;
    private javax.swing.JTextField campoNumeroEscritura;
    private javax.swing.JTextField campoNumeroIdentificacion;
    private javax.swing.JTextField campoRegistro;
    private javax.swing.JComboBox comboTipoIdentificacion;
    private javax.swing.JTable grillaTestimonios;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel panelRetirarTestimonio;
    private javax.swing.JSpinner selectorCopias;
    // End of variables declaration//GEN-END:variables
}
