/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui.clientes;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.gui.Principal;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.servicios.AdministradorValidaciones;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author matias
 */
public class AdministrarCliente extends javax.swing.JInternalFrame
{

    private static Boolean estadoFormulario = Boolean.FALSE;
    private ControllerNegocio miController;
    private static JMenuItem ventanaAdministrarCliente = new JMenuItem("");
    private static AdministrarCliente instancia = null;
    private DtoPersona dtoPersonaOriginal = null;

    /**
     * Creates new form AdministrarCliente
     */
    public AdministrarCliente() {
        initComponents();
        estadoFormulario = Boolean.TRUE;
        //this.setSize(Principal.tamanioGrandeHorizontal, Principal.tamanioGrandeVertical);
        miController = ControllerNegocio.getInstancia();
        this.cargarComboTipoIdentificacion();
        this.desactivarFormulario();

    }

    public static JMenuItem getVentanaAdministrarCliente() {
        return ventanaAdministrarCliente;
    }

    public static void setVentanaAdministrarCliente(JMenuItem ventanaAdministrarCliente) {
        AdministrarCliente.ventanaAdministrarCliente = ventanaAdministrarCliente;
    }

    private void salir() {
        this.dispose();
    }

    public void cargarTipoFormulario(String boton) {
        switch (boton)
        {
            case ConstantesGui.MODIFICAR_PERSONA:
            {
                this.setTamanioModificarPersona();
                labelTitulo.setText("Modificar Persona");
                labelBusqueda.setText("Persona");
                comboTipoIdentificacion.setVisible(true);
                panelDatosCliente.setVisible(false);

                break;
            }
            case ConstantesGui.MODIFICAR_CLIENTE:
            {
                setTamanioCliente();
                labelTitulo.setText("Modificar Cliente");
                labelBusqueda.setText("Cliente");
                comboTipoIdentificacion.setVisible(true);
                panelDatosCliente.setVisible(true);
                break;
            }
            case ConstantesGui.DAR_ALTA_CLIENTE:
            {
                setTamanioCliente();
                labelTitulo.setText("Dar Alta Cliente");
                labelBusqueda.setText("Cliente");
                comboTipoIdentificacion.setVisible(true);
                panelDatosCliente.setVisible(true);
                this.activarCamposCliente();
                break;
            }
            case ConstantesGui.BUSCAR_CLIENTE:
            {
                setTamanioCliente();
                labelTitulo.setText("Buscar Cliente");
                labelBusqueda.setText("Cliente");
                comboTipoIdentificacion.setVisible(true);
                panelDatosCliente.setVisible(true);

                break;
            }

        }
    }

    /**
     * Carga una pesona o cliente dependiendo el tipo de busqueda
     *
     * @param dtoPersonaCliente
     * @param tipoBusqueda
     */
    public Boolean cargarFormulario(DtoPersona dtoPersonaCliente, String tipoBusqueda) {

        //Guarda el Dto de origen para control de modificacion de datos. 
        //impido que el numero y tipo de identificacion se repitan en una modificacion

        Boolean flag = false; // Se utiliza para saber si debe activarse el formulario

        dtoPersonaOriginal = dtoPersonaCliente;

        this.toFront();
        this.updateUI();
        
        
        switch (tipoBusqueda)
        {
            case ConstantesGui.MODIFICAR_PERSONA:
            {
                flag = this.cargarPersona(dtoPersonaCliente);
                this.activarCamposPersona();
                this.botonModoEdicion();
                this.abilitarFormulario();
                break;
            }
            case ConstantesGui.MODIFICAR_CLIENTE:
            {
                flag = this.modificarCliente(dtoPersonaCliente);
                this.activarCamposCliente();
                this.botonModoEdicion();
                this.abilitarFormulario();
                break;

            }
            case ConstantesGui.DAR_ALTA_CLIENTE:
            {
                flag = this.cargarPersonaCliente(dtoPersonaCliente);
                this.activarCamposCliente();    
                this.botonModoEdicion();
                this.abilitarFormulario();
                this.campoNumeroNupcias.setEnabled(iconable);
                
                break;

            }
            case ConstantesGui.BUSCAR_CLIENTE:
            {
                flag = this.modificarCliente(dtoPersonaCliente);
                activarCamposCliente();
                desabilitarFormulario(); //No poder editarlos
                this.botonModoVista();
                break;

            }
            case ConstantesGui.BUSCAR_PERSONA_CLIENTE:
            {
                if (!dtoPersonaCliente.getEsCliente())
                {
                    flag = this.cargarPersona(dtoPersonaCliente);

                    this.labelTitulo.setText(ConstantesGui.PERSONA);
                    this.botonBuscarCliente.setVisible(false);
                    this.labelBusqueda.setVisible(false);
                    this.botonAceptar.setVisible(false);
                    this.panelDatosCliente.setVisible(false);
                    this.botonCancelar.setAlignmentX(118);
                    this.botonCancelar.setAlignmentX(35);
                    this.activarCamposPersona();
                    this.desabilitarFormulario(); //No puede editar
                    this.botonModoVista();

                }
                else
                {
                    flag = this.cargarCliente(dtoPersonaCliente);

                    this.labelTitulo.setText(ConstantesGui.CLIENTE);
                    this.labelBusqueda.setVisible(false);
                    this.panelDatosCliente.setVisible(true);;
                    this.botonBuscarCliente.setVisible(false);
                    this.botonAceptar.setVisible(false);
                    this.botonCancelar.setAlignmentX(118);
                    this.botonCancelar.setAlignmentX(35);
                    this.botonModoEdicion();
                    this.activarCamposCliente();
                    this.desabilitarFormulario(); //No puede editar
                }

                break;
            }

        }
        /*   if (flag == true)
         { //Si se concreto una de las opciones el formualrio se activa.
         this.ActivarFormulario(tipoBusqueda);
         }*/
        this.toFront();
        return flag;
    }

    public boolean cargarPersona(DtoPersona dtoPersona) {

        boolean flag = false;

        campoNombre.setText(dtoPersona.getNombre());
        campoApellido.setText(dtoPersona.getApellido());
        campoNumeroIdentificacion.setText(dtoPersona.getNumeroIdentificacion());
        campoTelefono.setText(dtoPersona.getTelefono());
        campoEmail.setText(dtoPersona.getEmail());
        comboTipoIdentificacion.setSelectedItem(dtoPersona.getDtoTipoIdentificacion().getNombre());

        flag = true;

        return flag;
    }

    public boolean cargarPersonaCliente(DtoPersona dtoPersona) {

        boolean flag = false;

        //Si no es cliente, lo cargo
        if (!dtoPersona.getEsCliente())
        {

            campoNombre.setText(dtoPersona.getNombre());
            campoApellido.setText(dtoPersona.getApellido());
            campoNumeroIdentificacion.setText(dtoPersona.getNumeroIdentificacion());
            campoTelefono.setText(dtoPersona.getTelefono());
            campoEmail.setText(dtoPersona.getEmail());

            //Cargo combos
            comboTipoIdentificacion.setSelectedItem(dtoPersona.getDtoTipoIdentificacion().getNombre());

            flag = true;
        }
        else
        {
            JOptionPane.showMessageDialog(this, "La Persona Esta Registrada Como Cliente");
            this.limpiarFormulario();
            this.desactivarFormulario();
        }
        return flag;
    }

    public boolean modificarCliente(DtoPersona dtoPersona) {
        boolean flag = false;

        //Si es cliente, lo cargo
        if (dtoPersona.getEsCliente())
        {
            campoNombre.setText(dtoPersona.getNombre());
            campoApellido.setText(dtoPersona.getApellido());
            campoNumeroIdentificacion.setText(dtoPersona.getNumeroIdentificacion());
            campoTelefono.setText(dtoPersona.getTelefono());
            campoEmail.setText(dtoPersona.getEmail());

            //Cargo combos
            comboTipoIdentificacion.setSelectedItem(dtoPersona.getDtoTipoIdentificacion().getNombre());
            comboEstadoCivil.setSelectedItem(dtoPersona.getEstadoCivil());

            campoCuit.setText(dtoPersona.getCuit());
            campoNacionalidad.setText(dtoPersona.getNacionalidad());
            campoFechaNacimiento.setDate(dtoPersona.getFechaNacimiento());
            campoNumeroNupcias.setValue(dtoPersona.getNumeroNupcias());
            comboSexo.setSelectedItem(dtoPersona.getSexo());
            campoOcupacion.setText(dtoPersona.getOcupacion());
            campoDomicilio.setText(dtoPersona.getDomicilio());

            flag = true;
        }
        else
        {
            JOptionPane.showMessageDialog(this, "La Persona No Esta Registrada Como Cliente");
            this.limpiarFormulario();
            this.desactivarFormulario();
        }
        return flag;
    }

    public boolean cargarCliente(DtoPersona dtoPersona) {

        boolean flag = false;

        //Si es cliente, lo cargo
        if (dtoPersona.getEsCliente())
        {

            campoNombre.setText(dtoPersona.getNombre());
            campoApellido.setText(dtoPersona.getApellido());
            campoNumeroIdentificacion.setText(dtoPersona.getNumeroIdentificacion());
            campoTelefono.setText(dtoPersona.getTelefono());
            campoEmail.setText(dtoPersona.getEmail());

            //cargo combos
            comboTipoIdentificacion.setSelectedItem(dtoPersona.getDtoTipoIdentificacion().getNombre());
            comboEstadoCivil.setSelectedItem(dtoPersona.getEstadoCivil());
            comboSexo.setSelectedItem(dtoPersona.getSexo());

            campoCuit.setText(dtoPersona.getCuit());
            campoNacionalidad.setText(dtoPersona.getNacionalidad());
            campoFechaNacimiento.setDate(dtoPersona.getFechaNacimiento());
            campoNumeroNupcias.setValue(dtoPersona.getNumeroNupcias());
            campoOcupacion.setText(dtoPersona.getOcupacion());
            campoDomicilio.setText(dtoPersona.getDomicilio());

            flag = true;
        }
        else
        {
            JOptionPane.showMessageDialog(this, "La Persona No Esta Registrada Como Cliente");
            this.limpiarFormulario();
            this.desactivarFormulario();
        }
        return flag;
    }

    public void ActivarFormulario(String tipoBusqueda) {


        switch (this.labelTitulo.getText())
        {
            case ConstantesGui.MODIFICAR_PERSONA:
            case ConstantesGui.BUSCAR_PERSONA:
            case ConstantesGui.PERSONA:
            {
                this.activarCamposPersona();
                break;
            }
            case ConstantesGui.MODIFICAR_CLIENTE:
            case ConstantesGui.DAR_ALTA_CLIENTE:
            case ConstantesGui.BUSCAR_CLIENTE:
            case ConstantesGui.BUSCAR_PERSONA_CLIENTE:
            {
                this.activarCamposCliente();
                break;

            }


        }
    }

    /**
     * Activa los campos de la persona, estos pueden ser editados el contenido se puede apreciar
     * claramente.
     */
    public void activarCamposPersona() {

        comboTipoIdentificacion.setEnabled(true);
        campoNombre.setEnabled(true);
        campoApellido.setEnabled(true);
        campoEmail.setEnabled(true);
        campoNumeroIdentificacion.setEnabled(true);
        campoTelefono.setEnabled(true);
        botonAceptar.setEnabled(true);

        botonCancelar.setEnabled(true);
        this.updateUI();

    }

    /**
     * Activa los campos del cliente, estos pueden ser editados el contenido se puede apreciar
     * claramente.
     */
    public void activarCamposCliente() {

        //Activo los campos de persona
        activarCamposPersona();

        //Activo los campos del cliente  
        campoNacionalidad.setEnabled(true);
        campoFechaNacimiento.setEnabled(true);
        comboTipoIdentificacion.setEnabled(true);
        campoNumeroIdentificacion.setEnabled(true);
        campoCuit.setEnabled(true);
        comboSexo.setEnabled(true);
        comboEstadoCivil.setEnabled(true);
        campoOcupacion.setEnabled(true);
        campoDomicilio.setEnabled(true);

        botonAceptar.setEnabled(true);
        this.updateUI();
    }

    /**
     * Desactiva los campos del cliente
     */
    public void desactivarFormulario() {

        campoNombre.setEnabled(false);
        campoApellido.setEnabled(false);
        campoEmail.setEnabled(false);
        campoTelefono.setEnabled(false);
        campoNumeroIdentificacion.setEnabled(false);
        comboTipoIdentificacion.setEnabled(false);

        campoNacionalidad.setEnabled(false);
        campoFechaNacimiento.setEnabled(false);
        campoCuit.setEnabled(false);
        comboEstadoCivil.setEnabled(false);
        campoNumeroNupcias.setEnabled(false);
        campoNumeroIdentificacion.setEnabled(false);
        campoCuit.setEnabled(false);
        comboSexo.setEnabled(false);
        campoOcupacion.setEnabled(false);
        campoDomicilio.setEnabled(false);

        botonAceptar.setEnabled(false);
    }

    /**
     * Pone los campos del formulario en estado no editable
     */
    public void desabilitarFormulario() {

        campoNombre.setEditable(false);
        campoApellido.setEditable(false);
        campoEmail.setEditable(false);
        campoTelefono.setEditable(false);
        campoNumeroIdentificacion.setEditable(false);
        comboTipoIdentificacion.setEnabled(false);

        //Quito el combo
        //layerpanelPersona.remove(comboTipoIdentificacion);

        campoNacionalidad.setEditable(false);
        campoFechaNacimiento.setEnabled(false);
        campoCuit.setEditable(false);

        comboEstadoCivil.setEnabled(false);

        campoNumeroNupcias.setEnabled(false);
        campoNumeroIdentificacion.setEditable(false);
        campoCuit.setEditable(false);

        comboSexo.setEnabled(false);

        campoOcupacion.setEditable(false);
        campoDomicilio.setEditable(false);
        botonAceptar.setEnabled(false);


    }

        public void abilitarFormulario() {

        campoNombre.setEditable(true);
        campoApellido.setEditable(true);
        campoEmail.setEditable(true);
        campoTelefono.setEditable(true);
        campoNumeroIdentificacion.setEditable(true);
        comboTipoIdentificacion.setEnabled(true);

        //Quito el combo
        //layerpanelPersona.remove(comboTipoIdentificacion);

        campoNacionalidad.setEditable(true);
        campoFechaNacimiento.setEnabled(true);
        campoCuit.setEditable(true);

        comboEstadoCivil.setEnabled(true);

        campoNumeroNupcias.setEnabled(true);
        campoNumeroIdentificacion.setEditable(true);
        campoCuit.setEditable(true);

        comboSexo.setEnabled(true);

        campoOcupacion.setEditable(true);
        campoDomicilio.setEditable(true);
        botonAceptar.setEnabled(true);


    }
        
    private void modificarPersona() throws NonexistentJpaException {

        try
        {
            /*
             * Controlo que todos los campos que no contemplan null, esten
             * completos
             */

            String msj = AdministradorValidaciones.getInstancia().validarModificarPersona(this);

            if (msj == "")
            {

                DtoPersona dtoPersonaModificada = new DtoPersona();

                //Version del objeto
                dtoPersonaModificada.setVersion(dtoPersonaOriginal.getVersion());

                //Version del Cliente 
                dtoPersonaModificada.setVersion(dtoPersonaOriginal.getVersion());

                //Set id persona, en dto con datos modificados
                dtoPersonaModificada.setIdPersona(dtoPersonaOriginal.getIdPersona());

                //Set datos modificados
                DtoTipoIdentificacion dtoTipoIdentificacion = new com.licensis.notaire.dto.DtoTipoIdentificacion();

                dtoPersonaModificada.setNombre(campoNombre.getText());
                dtoPersonaModificada.setApellido(campoApellido.getText());
                dtoTipoIdentificacion.setNombre(comboTipoIdentificacion.getSelectedItem().toString());
                dtoPersonaModificada.setDtoTipoIdentificacion(dtoTipoIdentificacion);
                dtoPersonaModificada.setNumeroIdentificacion(campoNumeroIdentificacion.getText());
                dtoPersonaModificada.setTelefono(campoTelefono.getText());
                dtoPersonaModificada.setEmail(campoEmail.getText());
                dtoPersonaModificada.getDtoTipoIdentificacion().setIdTipoIdentificacion(miController.asociarFkTipoIdentificacion(dtoPersonaModificada));


                //Cotrol de modificacion por tipo y numero de identificacion, no puede repertirse 
                Boolean flag = miController.controlModificacionPersona(dtoPersonaOriginal, dtoPersonaModificada);

                //Si la persona fue encontrada, significa que ya existe alguien con el tipo y numero de indentificacion 
                //ingresado por el usuario en la modificacion
                if (!flag)
                {
                    miController.modificarPersona(dtoPersonaModificada);
                    JOptionPane.showMessageDialog(this, "La pesona se modifico correctamente", "Informacion", JOptionPane.INFORMATION_MESSAGE);
                    this.limpiarFormulario();
                    this.desactivarFormulario();

                }
                else
                {
                    JOptionPane.showMessageDialog(this, "La persona Existe Error en modificacion Tipo Numero Identificacion", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    campoNumeroIdentificacion.setText("");
                }


            }
            else
            {
                JOptionPane.showMessageDialog(this, msj, "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            }

        }
        catch (ClassEliminatedException ex)
        {
            JOptionPane.showMessageDialog(this, "La persona ha sido eliminada, accion cancelada", "Advertencia", JOptionPane.WARNING_MESSAGE);
            this.salir();
        }
        catch (ClassModifiedException e)
        {
            String mensaje = "Persona - Cliente modificado con anterioridad - Accion Cancelada";
            JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);

            //Limpio formulario 
            this.limpiarFormulario();
            this.desactivarFormulario();

            return;
        }
    }

    private void modificarCliente() throws NonexistentJpaException {

        try
        {
            /*
             * Controlo que todos los campos que no contemplan null, esten
             * completos
             */
            String msj = AdministradorValidaciones.getInstancia().validarCliente(this);

            if (msj == "")
            {

                DtoPersona dtoClienteModificado = new DtoPersona();
                DtoTipoIdentificacion dtoTipoIdentificacion = new com.licensis.notaire.dto.DtoTipoIdentificacion();

                //Version del Cliente 
                dtoClienteModificado.setVersion(dtoPersonaOriginal.getVersion());

                //Set atributos persona
                dtoClienteModificado.setNombre(campoNombre.getText());
                dtoClienteModificado.setApellido(campoApellido.getText());
                dtoTipoIdentificacion.setNombre(comboTipoIdentificacion.getSelectedItem().toString());
                dtoClienteModificado.setDtoTipoIdentificacion(dtoTipoIdentificacion);
                dtoClienteModificado.setNumeroIdentificacion(campoNumeroIdentificacion.getText());
                dtoClienteModificado.setTelefono(campoTelefono.getText());
                dtoClienteModificado.setEmail(campoEmail.getText());

                dtoClienteModificado.getDtoTipoIdentificacion().setIdTipoIdentificacion(miController.asociarFkTipoIdentificacion(dtoClienteModificado));

                //Set atributos cliente
                dtoClienteModificado.setNacionalidad(campoNacionalidad.getText());
                dtoClienteModificado.setFechaNacimiento(campoFechaNacimiento.getDate());
                dtoClienteModificado.setCuit(campoCuit.getText());
                dtoClienteModificado.setEstadoCivil(comboEstadoCivil.getSelectedItem().toString());
                dtoClienteModificado.setNumeroNupcias(campoNumeroNupcias.getValue().hashCode());
                dtoClienteModificado.setSexo(comboSexo.getSelectedItem().toString());
                dtoClienteModificado.setOcupacion(campoOcupacion.getText());
                dtoClienteModificado.setDomicilio(campoDomicilio.getText());

                //Set es Cliente
                dtoClienteModificado.setEsCliente(true);
                dtoClienteModificado.setIdPersona(dtoPersonaOriginal.getIdPersona());

                //Cotrol de modificacion por tipo y numero de identificacion
                Boolean flag = miController.controlModificacionPersona(dtoPersonaOriginal, dtoClienteModificado);

                if (flag == false)
                {
                    miController.modificarCliente(dtoClienteModificado);
                    JOptionPane.showMessageDialog(this, "El Cliente se modifico correctamente", "Informacion", JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    desactivarFormulario();

                }
                else
                {
                    JOptionPane.showMessageDialog(this, "La persona Existe Error en modificacion Tipo Numero Identificacion", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    campoNumeroIdentificacion.setText("");
                }

            }
            else
            {
                JOptionPane.showMessageDialog(this, msj, "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            }
        }
        catch (ClassModifiedException e)
        {
            String mensaje = "Persona - Cliente modificado con anterioridad - Accion Cancelada";
            JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);

            //Limpio formulario 
            this.limpiarFormulario();
            this.desactivarFormulario();

            return;
        }
        catch (ClassEliminatedException ex)
        {
            JOptionPane.showMessageDialog(this, "Error grave, Accion cancelada", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(AdministrarCliente.class.getName()).log(Level.SEVERE, null, ex);
            this.salir();
        }

    }

    public void limpiarFormulario() {
        campoNombre.setText("");
        campoApellido.setText("");
        campoEmail.setText("");
        campoNumeroIdentificacion.setText("");
        campoTelefono.setText("");
        campoNacionalidad.setText("");
        campoFechaNacimiento.setDate(new Date());
        campoCuit.setText("");
        comboEstadoCivil.setSelectedItem("Soltero");
        comboSexo.setSelectedItem("Masculino");
        campoNumeroNupcias.setValue(0);
        campoOcupacion.setText("");
        campoDomicilio.setText("");

        this.botonBuscarCliente.setVisible(true);
        this.labelBusqueda.setVisible(true);
        this.advertencia.setText("");
    }

    public void cargarComboTipoIdentificacion() {
        String nombre = null;
        ArrayList<DtoTipoIdentificacion> listaDtoIdentificaciones = new ArrayList<DtoTipoIdentificacion>();
        listaDtoIdentificaciones = miController.listarTiposIdentificacion();

        for (int i = 0; i < listaDtoIdentificaciones.size(); i++)
        {
            nombre = listaDtoIdentificaciones.get(i).getNombre();
            comboTipoIdentificacion.addItem(nombre);
        }
        //Set como primer opcion el DNI
        comboTipoIdentificacion.setSelectedItem("D.N.I.");


    }

    public static AdministrarCliente getInstancia() {
        if (instancia == null)
        {
            instancia = new AdministrarCliente();
        }
        instancia.setSize(649, 650);
        return instancia;
    }

    public void darAltaCliente() throws NonexistentJpaException {

        /*
         * Controlo que todos los campos que no contemplan null, esten completos
         */
        String msj = AdministradorValidaciones.getInstancia().validarCliente(this);
        if (msj == "")
        {
            DtoPersona dtoCliente = new DtoPersona();
            DtoTipoIdentificacion dtoTipoIdentificacion = new com.licensis.notaire.dto.DtoTipoIdentificacion();

            //Set atributos persona
            dtoCliente.setNombre(campoNombre.getText());
            dtoCliente.setApellido(campoApellido.getText());
            dtoTipoIdentificacion.setNombre(comboTipoIdentificacion.getSelectedItem().toString());
            dtoCliente.setDtoTipoIdentificacion(dtoTipoIdentificacion);
            dtoCliente.setNumeroIdentificacion(campoNumeroIdentificacion.getText());
            dtoCliente.setTelefono(campoTelefono.getText());
            dtoCliente.setEmail(campoEmail.getText());

            dtoCliente.getDtoTipoIdentificacion().setIdTipoIdentificacion(miController.asociarFkTipoIdentificacion(dtoCliente));

            //Set atributos cliente
            dtoCliente.setNacionalidad(campoNacionalidad.getText());
            dtoCliente.setFechaNacimiento(campoFechaNacimiento.getDate());
            dtoCliente.setCuit(campoCuit.getText());
            dtoCliente.setEstadoCivil(comboEstadoCivil.getSelectedItem().toString());
            dtoCliente.setNumeroNupcias(campoNumeroNupcias.getValue().hashCode());
            dtoCliente.setSexo(comboSexo.getSelectedItem().toString());
            dtoCliente.setOcupacion(campoOcupacion.getText());
            dtoCliente.setDomicilio(campoDomicilio.getText());

            //Set es Cliente
            dtoCliente.setEsCliente(true);

            //Datos originales del cliente - persona 
            dtoCliente.setIdPersona(dtoPersonaOriginal.getIdPersona());
            dtoCliente.setVersion(dtoPersonaOriginal.getVersion());



            //Controlo si la persona se modifico
            Boolean flag = true;
            flag = miController.controlModificacionPersona(dtoPersonaOriginal, dtoCliente);

            if (flag == false)
            {

                if (dtoCliente.getIdPersona() != -1)
                {
                    try
                    {
                        dtoCliente = miController.modificarCliente(dtoCliente);
                    }
                    catch (ClassModifiedException ex)
                    {
                        JOptionPane.showMessageDialog(this, "La persona indicada esta siendo modificada por otro usuario", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        Logger.getLogger(AdministrarCliente.class.getName()).log(Level.SEVERE, null, ex);
                        this.salir();
                    }
                    catch (ClassEliminatedException ex)
                    {
                        JOptionPane.showMessageDialog(this, "Error grave, Accion cancelada", "Error", JOptionPane.ERROR_MESSAGE);
                        Logger.getLogger(AdministrarCliente.class.getName()).log(Level.SEVERE, null, ex);
                        this.salir();
                    }
                }
                else
                {
                    dtoCliente = miController.darAltaPersona(dtoCliente);
                }

                if (dtoCliente != null)
                {
                    JOptionPane.showMessageDialog(this, "Fue dado de alta como Cliente: " + dtoCliente.getNombre() + " " + dtoCliente.getApellido(), "Informacion", JOptionPane.INFORMATION_MESSAGE);
                    this.limpiarFormulario();
                    this.desactivarFormulario();
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "La persona No pudo darse de alta como Cliente", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                }

            }
            else
            {
                JOptionPane.showMessageDialog(this, "La persona ya Esta Registrada como cliente", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                this.limpiarFormulario();
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, msj, "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
        }


    }

    public void botonModoVista() {
        this.botonCancelar.setText("Cerrar");
        this.botonCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/src/main/resources/iconos/cerrar.png")));
    }

    public void botonModoEdicion() {
        this.botonCancelar.setText("Cancelar");
        this.botonCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("")));
    }

    //Es publico porque dependiendo el tipo de utilizacion del formulario es como se carga
    //la ventana activa
    public JMenuItem getVentanaActiva() {
        this.ventanaAdministrarCliente.setText(labelTitulo.getText());
        this.setTitle(labelTitulo.getText());
        return ventanaAdministrarCliente;
    }

    public void setTamanioModificarPersona() {
        this.setSize(649, 366);
        this.panelDatosPersona.setSize(306, 184);
    }

    public void setTamanioCliente() {
        this.setSize(649, 650);
        this.panelDatosPersona.setSize(306, 184);
    }

    public DtoPersona getDtoControlModificacion() {
        return dtoPersonaOriginal;
    }

    public void setDtoControlModificacion(DtoPersona dtoControlModificacion) {
        this.dtoPersonaOriginal = dtoControlModificacion;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelDarAltaCliente = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        labelCorreo = new javax.swing.JLabel();
        labelTelefono = new javax.swing.JLabel();
        labelApellido = new javax.swing.JLabel();
        labelTipo = new javax.swing.JLabel();
        labelNumero = new javax.swing.JLabel();
        labelTitulo = new javax.swing.JLabel();
        labelBusqueda = new javax.swing.JLabel();
        botonBuscarCliente = new javax.swing.JButton();
        panelDatosCliente = new javax.swing.JPanel();
        campoOcupacion = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        campoDomicilio = new javax.swing.JTextArea();
        campoNumeroNupcias = new javax.swing.JSpinner();
        campoFechaNacimiento = new com.toedter.calendar.JDateChooser();
        comboSexo = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        campoNacionalidad = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        campoCuit = new javax.swing.JTextField();
        comboEstadoCivil = new javax.swing.JComboBox();
        panelDatosPersona = new javax.swing.JLayeredPane();
        campoNombre = new javax.swing.JTextField();
        campoApellido = new javax.swing.JTextField();
        comboTipoIdentificacion = new javax.swing.JComboBox();
        campoNumeroIdentificacion = new javax.swing.JTextField();
        campoTelefono = new javax.swing.JTextField();
        campoEmail = new javax.swing.JTextField();
        separador = new javax.swing.JSeparator();
        advertencia = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setTitle("Persona - Cliente");
        setToolTipText("");
        setVisible(true);
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

        panelDarAltaCliente.setPreferredSize(new java.awt.Dimension(650, 366));

        jLabel2.setText("Nombre:");

        labelCorreo.setText("Correo electrónico:");

        labelTelefono.setText("Teléfono:");

        labelApellido.setText("Apellido");

        labelTipo.setText("Tipo Ident.:");

        labelNumero.setText("Número Ident.:");

        labelTitulo.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        labelTitulo.setText("Administrar Cliente");
        labelTitulo.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelTitulo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        labelTitulo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        labelBusqueda.setText("Cliente");

        botonBuscarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/src/main/resources/iconos/buscar.png"))); // NOI18N
        botonBuscarCliente.setText("Buscar");
        botonBuscarCliente.setIconTextGap(10);
        botonBuscarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarClienteActionPerformed(evt);
            }
        });

        jLabel15.setText("Sexo:");

        jLabel11.setText("Estado Civil:");

        jLabel12.setText("Número Nupcias:");

        jLabel13.setText("Ocupación:");

        jLabel9.setText("Nacionalidad:");

        jLabel14.setText("Domicilio:");

        campoDomicilio.setColumns(20);
        campoDomicilio.setRows(5);
        jScrollPane1.setViewportView(campoDomicilio);
        campoDomicilio.getAccessibleContext().setAccessibleName("");
        campoDomicilio.getAccessibleContext().setAccessibleDescription("");

        campoNumeroNupcias.setEnabled(false);
        campoNumeroNupcias.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                campoNumeroNupciasStateChanged(evt);
            }
        });

        comboSexo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Masculino", "Femenino" }));
        comboSexo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboSexoActionPerformed(evt);
            }
        });

        jLabel10.setText("Fecha de Nacimiento:");

        jLabel1.setText("Cuit/Cuil");

        campoCuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoCuitActionPerformed(evt);
            }
        });

        comboEstadoCivil.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Soltero/a", "Casado/a", "Divorciado/a", "Viudo/a" }));
        comboEstadoCivil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboEstadoCivilActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelDatosClienteLayout = new javax.swing.GroupLayout(panelDatosCliente);
        panelDatosCliente.setLayout(panelDatosClienteLayout);
        panelDatosClienteLayout.setHorizontalGroup(
            panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(23, 23, 23)
                .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelDatosClienteLayout.createSequentialGroup()
                        .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(campoNacionalidad)
                                .addComponent(campoFechaNacimiento, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                                .addComponent(campoCuit))
                            .addComponent(campoOcupacion, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelDatosClienteLayout.createSequentialGroup()
                                .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(comboEstadoCivil, 0, 145, Short.MAX_VALUE)
                                    .addComponent(comboSexo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(2, 2, 2)
                                .addComponent(jLabel12)
                                .addGap(1, 1, 1)
                                .addComponent(campoNumeroNupcias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 10, Short.MAX_VALUE)))
                .addGap(142, 142, 142))
        );
        panelDatosClienteLayout.setVerticalGroup(
            panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosClienteLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(campoNacionalidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(campoFechaNacimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoCuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(14, 14, 14)
                .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDatosClienteLayout.createSequentialGroup()
                        .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(campoNumeroNupcias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboEstadoCivil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboSexo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)))
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(campoOcupacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)))
        );

        campoOcupacion.getAccessibleContext().setAccessibleName("");
        campoOcupacion.getAccessibleContext().setAccessibleDescription("");
        campoNumeroNupcias.getAccessibleContext().setAccessibleName("");
        campoNumeroNupcias.getAccessibleContext().setAccessibleDescription("");
        campoFechaNacimiento.getAccessibleContext().setAccessibleName("");
        campoFechaNacimiento.getAccessibleContext().setAccessibleDescription("");
        comboSexo.getAccessibleContext().setAccessibleName("");
        comboSexo.getAccessibleContext().setAccessibleDescription("");
        campoNacionalidad.getAccessibleContext().setAccessibleName("");
        campoNacionalidad.getAccessibleContext().setAccessibleDescription("");

        panelDatosPersona.setEnabled(false);
        campoNombre.setBounds(0, 0, 300, 27);
        panelDatosPersona.add(campoNombre, javax.swing.JLayeredPane.DEFAULT_LAYER);
        campoNombre.getAccessibleContext().setAccessibleName("");
        campoNombre.getAccessibleContext().setAccessibleDescription("");

        campoApellido.setBounds(0, 30, 300, 27);
        panelDatosPersona.add(campoApellido, javax.swing.JLayeredPane.DEFAULT_LAYER);
        campoApellido.getAccessibleContext().setAccessibleName("");
        campoApellido.getAccessibleContext().setAccessibleDescription("");

        comboTipoIdentificacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboTipoIdentificacionActionPerformed(evt);
            }
        });
        comboTipoIdentificacion.setBounds(0, 60, 150, 25);
        panelDatosPersona.add(comboTipoIdentificacion, javax.swing.JLayeredPane.DEFAULT_LAYER);
        comboTipoIdentificacion.getAccessibleContext().setAccessibleName("");
        comboTipoIdentificacion.getAccessibleContext().setAccessibleDescription("");

        campoNumeroIdentificacion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                campoNumeroIdentificacionKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                campoNumeroIdentificacionKeyTyped(evt);
            }
        });
        campoNumeroIdentificacion.setBounds(0, 90, 300, 27);
        panelDatosPersona.add(campoNumeroIdentificacion, javax.swing.JLayeredPane.DEFAULT_LAYER);
        campoNumeroIdentificacion.getAccessibleContext().setAccessibleName("");
        campoNumeroIdentificacion.getAccessibleContext().setAccessibleDescription("");

        campoTelefono.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoTelefonoActionPerformed(evt);
            }
        });
        campoTelefono.setBounds(0, 120, 300, 27);
        panelDatosPersona.add(campoTelefono, javax.swing.JLayeredPane.DEFAULT_LAYER);
        campoTelefono.getAccessibleContext().setAccessibleName("");
        campoTelefono.getAccessibleContext().setAccessibleDescription("");

        campoEmail.setBounds(0, 150, 300, 27);
        panelDatosPersona.add(campoEmail, javax.swing.JLayeredPane.DEFAULT_LAYER);
        campoEmail.getAccessibleContext().setAccessibleName("");
        campoEmail.getAccessibleContext().setAccessibleDescription("");

        advertencia.setFont(new java.awt.Font("Cantarell", 1, 12)); // NOI18N
        advertencia.setForeground(new java.awt.Color(241, 5, 5));

        botonAceptar.setText("Aceptar");
        botonAceptar.setPreferredSize(new java.awt.Dimension(120, 35));
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

        javax.swing.GroupLayout panelDarAltaClienteLayout = new javax.swing.GroupLayout(panelDarAltaCliente);
        panelDarAltaCliente.setLayout(panelDarAltaClienteLayout);
        panelDarAltaClienteLayout.setHorizontalGroup(
            panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                .addGroup(panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelTitulo)
                            .addComponent(labelBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(separador, javax.swing.GroupLayout.PREFERRED_SIZE, 545, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelDatosCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                        .addGroup(panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelApellido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelTipo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDarAltaClienteLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelTelefono)
                                    .addComponent(labelNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelCorreo))))
                        .addGap(32, 32, 32)
                        .addGroup(panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                                .addComponent(botonBuscarCliente, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                                .addGap(336, 336, 336))
                            .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                                .addComponent(panelDatosPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(advertencia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDarAltaClienteLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
        );
        panelDarAltaClienteLayout.setVerticalGroup(
            panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                .addComponent(labelTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botonBuscarCliente, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelBusqueda))
                .addGroup(panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDarAltaClienteLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelDatosPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                        .addGroup(panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelApellido)
                                .addGap(18, 18, 18)
                                .addComponent(labelTipo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelNumero))
                            .addGroup(panelDarAltaClienteLayout.createSequentialGroup()
                                .addGap(100, 100, 100)
                                .addComponent(advertencia, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelTelefono)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelCorreo)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(separador, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelDatosCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelDarAltaClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        labelTitulo.getAccessibleContext().setAccessibleDescription("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelDarAltaCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 645, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelDarAltaCliente, javax.swing.GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameClosed
    {//GEN-HEADEREND:event_formInternalFrameClosed
        estadoFormulario = Boolean.FALSE;
        Principal.removeVentanaActivas(ventanaAdministrarCliente);
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCancelarActionPerformed
        this.limpiarFormulario();
        this.dispose();
        salir();
        Principal.removeVentanaActivas(AdministrarCliente.getInstancia().getVentanaActiva());
        Principal.eliminarFormulario(this);
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void campoTelefonoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoTelefonoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoTelefonoActionPerformed

    private void campoNumeroIdentificacionKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoNumeroIdentificacionKeyTyped
    }//GEN-LAST:event_campoNumeroIdentificacionKeyTyped

    private void campoNumeroIdentificacionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoNumeroIdentificacionKeyReleased
        Boolean flag = false;
        String campo = campoNumeroIdentificacion.getText();

        flag = AdministradorValidaciones.getInstancia().validarCampoSoloNumerosEnteros(campo);

        if (!flag)
        {
            this.advertencia.setText("Esta Ingresando Caracteres");
        }
        else
        {
            this.advertencia.setText(" ");
        }

    }//GEN-LAST:event_campoNumeroIdentificacionKeyReleased

    private void comboTipoIdentificacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboTipoIdentificacionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboTipoIdentificacionActionPerformed

    private void comboEstadoCivilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboEstadoCivilActionPerformed
        if (comboEstadoCivil.getSelectedItem().toString().equals("Divorciado/a")
                || comboEstadoCivil.getSelectedItem().toString().equals("Casado/a")
                || comboEstadoCivil.getSelectedItem().toString().equals("Viudo/a"))
        {
            campoNumeroNupcias.setEnabled(true);
        }
        else
        {
            Integer num = new Integer(0);
            campoNumeroNupcias.setValue(num);
            campoNumeroNupcias.setEnabled(false);
        }

    }//GEN-LAST:event_comboEstadoCivilActionPerformed

    private void campoCuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoCuitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoCuitActionPerformed

    private void comboSexoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboSexoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboSexoActionPerformed

    private void campoNumeroNupciasStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_campoNumeroNupciasStateChanged
        int valor = this.campoNumeroNupcias.getValue().hashCode();
        if (valor < 0)
        {
            this.campoNumeroNupcias.setValue(0);
        }
    }//GEN-LAST:event_campoNumeroNupciasStateChanged

    private void botonBuscarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuscarClienteActionPerformed
        //Busca una persona, el parametro de tipo de busqueda es el titulo del formulario
        BuscarCliente formBuscarCliente = new BuscarCliente();
        formBuscarCliente.setTipoBusqueda(this.labelTitulo.getText());
        formBuscarCliente.setTituloForm(this.labelTitulo.getText());
        Principal.cargarFormulario(formBuscarCliente);

        Principal.setVentanasActivas(BuscarCliente.getVentanaBuscarCliente());
    }//GEN-LAST:event_botonBuscarClienteActionPerformed

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAceptarActionPerformed
        switch (labelTitulo.getText())
        {
            case ConstantesGui.MODIFICAR_PERSONA:
            {
                try
                {
                    this.modificarPersona();

                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(this, "Error grave, Accion cancelada: Modificar persona", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(AdministrarCliente.class.getName()).log(Level.SEVERE, null, ex);
                    this.salir();
                }
                break;
            }
            case ConstantesGui.MODIFICAR_CLIENTE:
            {
                try
                {
                    this.modificarCliente();

                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(this, "Error grave, Accion cancelada: Modificar cliente", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(AdministrarCliente.class.getName()).log(Level.SEVERE, null, ex);
                    this.salir();
                }
                break;
            }
            case ConstantesGui.DAR_ALTA_CLIENTE:
            {
                try
                {
                    this.darAltaCliente();
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(this, "Error grave, Accion cancelada:Dar alta cliente", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(AdministrarCliente.class.getName()).log(Level.SEVERE, null, ex);
                    this.salir();
                }
                break;

            }
        }
    }//GEN-LAST:event_botonAceptarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel advertencia;
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonBuscarCliente;
    private javax.swing.JButton botonCancelar;
    public javax.swing.JTextField campoApellido;
    public javax.swing.JTextField campoCuit;
    public javax.swing.JTextArea campoDomicilio;
    private javax.swing.JTextField campoEmail;
    private com.toedter.calendar.JDateChooser campoFechaNacimiento;
    public javax.swing.JTextField campoNacionalidad;
    public javax.swing.JTextField campoNombre;
    public javax.swing.JTextField campoNumeroIdentificacion;
    private javax.swing.JSpinner campoNumeroNupcias;
    public javax.swing.JTextField campoOcupacion;
    private javax.swing.JTextField campoTelefono;
    private javax.swing.JComboBox comboEstadoCivil;
    private javax.swing.JComboBox comboSexo;
    private javax.swing.JComboBox comboTipoIdentificacion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelApellido;
    private javax.swing.JLabel labelBusqueda;
    private javax.swing.JLabel labelCorreo;
    private javax.swing.JLabel labelNumero;
    private javax.swing.JLabel labelTelefono;
    private javax.swing.JLabel labelTipo;
    public javax.swing.JLabel labelTitulo;
    private javax.swing.JPanel panelDarAltaCliente;
    private javax.swing.JPanel panelDatosCliente;
    private javax.swing.JLayeredPane panelDatosPersona;
    private javax.swing.JSeparator separador;
    // End of variables declaration//GEN-END:variables
}
