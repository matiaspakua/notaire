package com.licensis.notaire.gui.util;

import com.licensis.notaire.gui.administracion.conceptos.IngresarConcepto;
import com.licensis.notaire.gui.administracion.conceptos.ModificarConcepto;
import com.licensis.notaire.gui.administracion.documentos.IngresarDocumento;
import com.licensis.notaire.gui.administracion.documentos.ModificarDocumento;
import com.licensis.notaire.gui.administracion.usuarios.DarAltaUsuario;
import com.licensis.notaire.gui.administracion.usuarios.ModificarUsuario;
import com.licensis.notaire.gui.clientes.AdministrarCliente;
import com.licensis.notaire.gui.clientes.DarAltaPersona;
import com.licensis.notaire.servicios.AdministradorValidaciones;
import com.licensis.notaire.negocio.ControllerNegocio;

/**
 * Clase que centraliza las validaciones que dependen de componentes de la GUI.
 */
public class AdministradorValidacionesGui {

    private static AdministradorValidacionesGui instancia = null;

    private AdministradorValidacionesGui() {
    }

    public static AdministradorValidacionesGui getInstancia() {
        if (instancia == null) {
            instancia = new AdministradorValidacionesGui();
        }
        return instancia;
    }

    public String validarDarAltaPersona(DarAltaPersona formulario) {
        String msj = "";
        String campoNombre = DarAltaPersona.getInstancia().campoNombre.getText();
        String campoApellido = DarAltaPersona.getInstancia().campoApellido.getText();
        String campoNroIdentificacion = DarAltaPersona.getInstancia().campoNumeroIdentificacion.getText();

        if (AdministradorValidaciones.getInstancia().validarCampoVacio(campoNombre)
                || AdministradorValidaciones.getInstancia().validarCampoVacio(campoApellido)
                || AdministradorValidaciones.getInstancia().validarCampoVacio(campoNroIdentificacion)) {
            msj = "Debe completar al menos: Nombre, Apellido, Tipo y Nro ident.";
        }
        return msj;
    }

    public String validarModificarPersona(AdministrarCliente formulario) {
        String msj = "";
        String campoNombre = AdministrarCliente.getInstancia().campoNombre.getText();
        String campoApellido = AdministrarCliente.getInstancia().campoApellido.getText();
        String campoNroIdentificacion = AdministrarCliente.getInstancia().campoNumeroIdentificacion.getText();

        if (AdministradorValidaciones.getInstancia().validarCampoVacio(campoNombre)
                || AdministradorValidaciones.getInstancia().validarCampoVacio(campoApellido)
                || AdministradorValidaciones.getInstancia().validarCampoVacio(campoNroIdentificacion)) {
            msj = "Debe completar al menos: Nombre, Apellido, Tipo y Nro ident.";
        }
        return msj;
    }

    public String validarCliente(AdministrarCliente formulario) {
        String msj = "";
        msj = this.validarModificarPersona(formulario);
        if ("".equals(msj)) {
            if (formulario.campoNombre.getText().isEmpty()
                    || formulario.campoApellido.getText().isEmpty()
                    || formulario.campoNacionalidad.getText().isEmpty()
                    || formulario.campoOcupacion.getText().isEmpty()
                    || formulario.campoDomicilio.getText().isEmpty()
                    || formulario.campoCuit.getText().isEmpty()) {
                msj = "Debe completar Todos los datos del Cliente";
            }
        }
        return msj;
    }

    public String validarAltaUsuario(DarAltaUsuario formulario) {
        String msj = "";
        if (formulario.campoNombreUsuario.getText().isEmpty()
                && formulario.campoContrasenia.getText().isEmpty()
                && formulario.campoRepetirNuevaContrasenia.getText().isEmpty()) {
            return msj = "Completar todos los campos del Usuario.";
        }
        if (!ControllerNegocio.getInstancia().isPasswordCorrect(formulario.campoContrasenia.getPassword(),
                formulario.campoRepetirNuevaContrasenia.getPassword()).getFlag()) {
            return msj = "La contrasenia no coincide";
        }
        Integer longContrasenia = formulario.campoContrasenia.getText().length();
        if (longContrasenia <= 4) {
            return msj = "La constrasenia debe contener un minimo de 5 caracteres";
        }
        String campo = formulario.campoNombreUsuario.getText();
        if (AdministradorValidaciones.getInstancia().validarCampoEspacios(campo)) {
            return msj = "Nombre de Usuario NO permite espacios.";
        }
        if (AdministradorValidaciones.getInstancia().validarCaracteres(campo)) {
            return msj = "Nombre de Usuario solo permite Letras.";
        }
        return msj;
    }

    public String validarModificarUsuario(ModificarUsuario formulario) {
        String msj = "";
        if (formulario.campoNuevoNombreUsuario.getText().isEmpty()
                && formulario.campoNuevaContrasenia.getText().isEmpty()
                && formulario.campoRepetirNuevaContrasenia.getText().isEmpty()) {
            return msj = "Completar todos los campos de Usuario";
        }
        if (!ControllerNegocio.getInstancia().isPasswordCorrect(formulario.campoNuevaContrasenia.getPassword(),
                formulario.campoRepetirNuevaContrasenia.getPassword()).getFlag()) {
            return msj = "La conrasenia no coincide";
        }
        Integer longContrasenia = formulario.campoNuevaContrasenia.getText().length();
        if (longContrasenia <= 4) {
            return msj = "La constrasenia debe contener un minimo de 5 caracteres";
        }
        String campo = formulario.campoNuevoNombreUsuario.getText();
        if (AdministradorValidaciones.getInstancia().validarCampoEspacios(campo)) {
            return msj = "Nombre de Usuario No permite especios";
        }
        return msj;
    }

    public String validarIngresarDocumento(IngresarDocumento pFormualrio) {
        String msj = "";
        String nombreDocumento = pFormualrio.campoNombreDocumento.getText();
        Boolean vence = pFormualrio.checkVenceDocumento.isSelected();
        Integer diasVencimiento = new Integer(pFormualrio.selectorDiasVencimiento.getValue().hashCode());
        if (!nombreDocumento.isEmpty()) {
            if (vence && diasVencimiento.intValue() <= 0) {
                msj = "Debe ingresar la cantidad de dias a vencer del documento";
            }
        } else {
            msj = "Debe completar el campo Nombre del Documento";
        }
        return msj;
    }

    public String validarModificarDocumento(ModificarDocumento pFormualrio) {
        String msj = "";
        String nombreDocumento = pFormualrio.campoNombreDocumento.getText();
        Boolean vence = pFormualrio.checkVenceDocumento.isSelected();
        Integer diasVencimiento = new Integer(pFormualrio.selectorDiasVencimiento.getValue().hashCode());
        if (!nombreDocumento.isEmpty()) {
            if (vence && diasVencimiento.intValue() <= 0) {
                msj = "Debe ingresar la cantidad de dias a vencer del documento";
            }
        } else {
            msj = "Debe completar el campo Nombre";
        }
        return msj;
    }

    public String validarIngresarConcepto(IngresarConcepto pformulario) {
        String msj = "";
        String nombreConcepto = pformulario.campoNombreConcepto.getText();
        String valorConcepto = pformulario.campoValorConcepto.getText();
        String valorPorcentaje = pformulario.selectorPorcentaje.getValue().toString();
        if (!AdministradorValidaciones.getInstancia().validarCampoVacio(nombreConcepto)) {
            if (!AdministradorValidaciones.getInstancia().validarCampoLetrasYNumeros(nombreConcepto)) {
                msj = "Nombre debe contener solo letras o numeros.";
            } else {
                if (!AdministradorValidaciones.getInstancia().validarCampoVacio(valorConcepto)) {
                    if (!AdministradorValidaciones.getInstancia().validarNumeroFloat(valorConcepto)) {
                        msj = "Debe completar un numero valido en valor, ej: 12.50";
                    }
                } else {
                    if (AdministradorValidaciones.getInstancia().validarCampoVacio(valorPorcentaje)) {
                        msj = "Debe completar valor o porcentaje.";
                    } else {
                        if ((Integer) pformulario.selectorPorcentaje.getValue() <= 0) {
                            msj = "El porcentaje debe ser mayor a 0.";
                        }
                    }
                }
            }
        } else {
            msj = "Debe completar el nombre.";
        }
        return msj;
    }

    public String validarModificarConcepto(ModificarConcepto pformulario) {
        String msj = "";
        String nombreConcepto = pformulario.campoNuevoNombre.getText();
        String valorConcepto = pformulario.campoNuevoValor.getText();
        String valorPorcentaje = pformulario.selectorNuevoPorcentaje.getValue().toString();
        if (!AdministradorValidaciones.getInstancia().validarCampoVacio(nombreConcepto)) {
            if (!AdministradorValidaciones.getInstancia().validarCampoLetrasYNumeros(nombreConcepto)) {
                msj = "Nombre debe contener solo letras o numeros.";
            } else {
                if (!AdministradorValidaciones.getInstancia().validarCampoVacio(valorConcepto)) {
                    if (!AdministradorValidaciones.getInstancia().validarNumeroFloat(valorConcepto)) {
                        msj = "Debe completar un numero valido en valor, ej: 12.50";
                    }
                } else {
                    if (!AdministradorValidaciones.getInstancia().validarCampoVacio(valorPorcentaje)) {
                        msj = "Debe completar valor o porcentaje.";
                    }
                }
            }
        } else {
            msj = "Debe completar el nombre.";
        }
        return msj;
    }
}
