/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.gestiones.gestion;

import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.gui.gestiones.escrituras.ListaEscrituras;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.negocio.ControllerNegocio;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author User
 */
public class DetalleGestion extends javax.swing.JInternalFrame
{

    private static DetalleGestion instancia;
    private static JMenuItem ventanaDetalleGestion = new JMenuItem("Ventana Detalle Gestion");
    private DtoGestionDeEscritura miDtoGestionDeEscritura;
    private ControllerNegocio miController = ControllerNegocio.getInstancia();

    /**
     * Creates new form NewJInternalFrame
     */
    private DetalleGestion() {
        initComponents();
        this.setSize(Principal.tamanioNormalHorizontal, Principal.tamanioGrandeVertical);
    }

    public static DetalleGestion getInstancia() {
        if (instancia == null)
        {
            instancia = new DetalleGestion();
        }
        return instancia;
    }

    public static JMenuItem getVentanaDetalleGestion() {
        return ventanaDetalleGestion;
    }

    public boolean cargarFormuarlioSoloVista(DtoGestionDeEscritura miDtoGestion) throws NonexistentJpaException {
        miDtoGestionDeEscritura = miDtoGestion;
        boolean flag = false;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String fecha = formatter.format(miDtoGestion.getFechaInicio());

        //Guardo el DtoGestion recibido
        this.setMiDtoGestionDeEscritura(miDtoGestion);

        labelNrogestion.setText(Integer.toString(miDtoGestion.getNumero()));
        labelFechaInicio.setText(fecha);
        labelEscribano.setText(miDtoGestion.getPersonaEscribano().getNombre() + ", " + miDtoGestion.getPersonaEscribano().getApellido());

        DtoPersona clienteReferencia = miController.obtenerClienteReferenciaGestion(miDtoGestion);

        labelClienteReferencia.setText(clienteReferencia.getNombre() + ", " + clienteReferencia.getApellido());

        labelEstado.setText(miDtoGestion.getEstado().getNombre());

        this.campoObservaciones.setText(miDtoGestion.getObservaciones());
        this.campoEncabezado.setText(miDtoGestion.getEncabezado());

        if (miDtoGestion.getNumeroBibliorato() != null)
        {
            labelNumeroBibliorato.setText(Integer.toString(miDtoGestion.getNumeroBibliorato()));
        }
        if (miDtoGestion.getNumeroArchivo() != null)
        {
            labelNumeroArchivo.setText(Integer.toString(miDtoGestion.getNumeroArchivo()));
        }

        cargarTramitesAsociados(miDtoGestion);
        cargarClientesInvolucrados(miDtoGestion);


        return flag;
    }

    public void cargarClientesInvolucrados(DtoGestionDeEscritura miDtoGestionDeEscritura) {
        String nombrePersona = null;
        DefaultListModel lista = new DefaultListModel();

        ArrayList<DtoPersona> listaPersonas = new ArrayList<>();
        listaPersonas = (ArrayList<DtoPersona>) miDtoGestionDeEscritura.getListaClientesInvolucrados();
        boolean flag = false;
        for (int i = 0; i < listaPersonas.size(); i++)
        {
            flag = true;
            nombrePersona = listaPersonas.get(i).getNombre() + " " + listaPersonas.get(i).getApellido();
            lista.addElement(nombrePersona);
        }
        this.listaClientesInvolucrados.setModel(lista);
        if (!flag)
        {
            JOptionPane.showMessageDialog(this, "El tramite no tiene Clientes Asociados");
        }


    }

    public void cargarTramitesAsociados(DtoGestionDeEscritura miDtoGestion) {
        String nombreTramite = null;
        DefaultListModel lista = new DefaultListModel();

        ArrayList<DtoTramite> listaTramites = new ArrayList<>();
        listaTramites = (ArrayList<DtoTramite>) miDtoGestion.getListaTramitesAsociados();
        boolean flag = false;
        for (int i = 0; i < listaTramites.size(); i++)
        {
            flag = true;
            nombreTramite = listaTramites.get(i).getTipoDeTramite().getNombre();
            lista.addElement(nombreTramite);
        }
        this.listaTramiteAsociados.setModel(lista);
        if (!flag)
        {
            JOptionPane.showMessageDialog(this, "La Gestion no tiene Tramites Asociados");
        }

    }

    private void salir() {
        this.dispose();
    }

    public DtoGestionDeEscritura getMiDtoGestionDeEscritura() {
        return miDtoGestionDeEscritura;
    }

    public void setMiDtoGestionDeEscritura(DtoGestionDeEscritura miDtoGestionDeEscritura) {
        this.miDtoGestionDeEscritura = miDtoGestionDeEscritura;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane5 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        labelClienteReferencia = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        labelNumeroArchivo = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        labelNumeroBibliorato = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        campoEncabezado = new javax.swing.JTextArea();
        labelEstado = new javax.swing.JLabel();
        labelEscribano = new javax.swing.JLabel();
        labelFechaInicio = new javax.swing.JLabel();
        labelNrogestion = new javax.swing.JLabel();
        botonCerrar = new javax.swing.JButton();
        botonListarEscrituras = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        campoObservaciones = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listaClientesInvolucrados = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listaTramiteAsociados = new javax.swing.JList();

        setClosable(true);
        setMaximizable(true);
        setTitle("Ventana Detalle Gestión");
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

        labelClienteReferencia.setText("Cliente Referencia");
        labelClienteReferencia.setBorder(null);

        labelNumeroArchivo.setText("n/a");
        labelNumeroArchivo.setBorder(null);

        jLabel13.setText("Cliente Ref:");

        jLabel12.setText("Número Archivo");

        labelNumeroBibliorato.setText("n/a");
        labelNumeroBibliorato.setBorder(null);

        campoEncabezado.setEditable(false);
        campoEncabezado.setColumns(20);
        campoEncabezado.setLineWrap(true);
        campoEncabezado.setRows(5);
        jScrollPane4.setViewportView(campoEncabezado);

        labelEstado.setForeground(new java.awt.Color(255, 0, 0));
        labelEstado.setText("Estado");
        labelEstado.setBorder(null);

        labelEscribano.setText("Escribano");
        labelEscribano.setBorder(null);

        labelFechaInicio.setText("Fecha");
        labelFechaInicio.setBorder(null);

        labelNrogestion.setText("Nombre");
        labelNrogestion.setBorder(null);

        botonCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/src/main/resources/iconos/cerrar.png"))); // NOI18N
        botonCerrar.setText("Cerrar");
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });

        botonListarEscrituras.setText("Listar Escrituras");
        botonListarEscrituras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonListarEscriturasActionPerformed(evt);
            }
        });

        jLabel11.setText("Escrituras:");

        jLabel9.setText("Número Bibliorato:");

        campoObservaciones.setEditable(false);
        campoObservaciones.setColumns(20);
        campoObservaciones.setRows(5);
        jScrollPane1.setViewportView(campoObservaciones);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Detalle de Gestión");

        jLabel10.setText("Observaciones:");

        jLabel8.setText("Estado Actual:");

        jLabel6.setText("Clientes Involucrados:");

        jScrollPane2.setViewportView(listaClientesInvolucrados);

        jLabel5.setText("Trámites Asociados:");

        jLabel2.setText("Número de Gestión:");

        jLabel3.setText("Encabezado:");

        jLabel4.setText("Fecha de Inicio:");

        jLabel7.setText("Escribano a Cargo:");

        listaTramiteAsociados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listaTramiteAsociadosMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(listaTramiteAsociados);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(botonListarEscrituras))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel5)
                                .addComponent(jLabel10))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel11)
                                        .addComponent(jLabel6))
                                    .addGap(0, 0, Short.MAX_VALUE)))))
                    .addComponent(jSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel13))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(labelEscribano, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelClienteReferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel8)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(39, 39, 39))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelNrogestion, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelFechaInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addGap(18, 18, 18)
                                        .addComponent(labelNumeroBibliorato, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addGap(36, 36, 36)
                                        .addComponent(labelNumeroArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelNrogestion)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(labelFechaInicio)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(labelNumeroArchivo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelNumeroBibliorato)
                            .addComponent(jLabel9))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelEstado)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelEscribano, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(labelClienteReferencia))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonListarEscrituras, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botonCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jScrollPane5.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        Principal.removeVentanaActivas(ventanaDetalleGestion);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void listaTramiteAsociadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listaTramiteAsociadosMouseClicked
    }//GEN-LAST:event_listaTramiteAsociadosMouseClicked

    private void botonListarEscriturasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonListarEscriturasActionPerformed

        List<DtoEscritura> escriturasGestion = miController.buscarEscriturasGestion(miDtoGestionDeEscritura);

        if (!escriturasGestion.isEmpty())
        {
            ListaEscrituras listaEscriturasForm = new ListaEscrituras();
            listaEscriturasForm.setFormularioInvocador(ConstantesGui.DETALLE_GESTION);
            listaEscriturasForm.cargarGrilla(escriturasGestion);
            Principal.cargarFormulario(listaEscriturasForm);
            Principal.setVentanasActivas(ListaEscrituras.getVentanaListaEscritura());
        }
        else
        {
            JOptionPane.showMessageDialog(this, "La gestion no tiene escrituras generadas.", "INFORMACION", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_botonListarEscriturasActionPerformed

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCerrarActionPerformed

        salir();
    }//GEN-LAST:event_botonCerrarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCerrar;
    private javax.swing.JButton botonListarEscrituras;
    private javax.swing.JTextArea campoEncabezado;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelClienteReferencia;
    private javax.swing.JLabel labelEscribano;
    private javax.swing.JLabel labelEstado;
    private javax.swing.JLabel labelFechaInicio;
    private javax.swing.JLabel labelNrogestion;
    private javax.swing.JLabel labelNumeroArchivo;
    private javax.swing.JLabel labelNumeroBibliorato;
    private javax.swing.JList listaClientesInvolucrados;
    private javax.swing.JList listaTramiteAsociados;
    // End of variables declaration//GEN-END:variables
}
