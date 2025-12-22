/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.servicios;

import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.gui.administracion.conceptos.IngresarConcepto;
import com.licensis.notaire.gui.administracion.conceptos.ModificarConcepto;
import com.licensis.notaire.gui.administracion.documentos.IngresarDocumento;
import com.licensis.notaire.gui.administracion.documentos.ModificarDocumento;
import com.licensis.notaire.gui.administracion.usuarios.DarAltaUsuario;
import com.licensis.notaire.gui.administracion.usuarios.ModificarUsuario;
import com.licensis.notaire.gui.clientes.AdministrarCliente;
import com.licensis.notaire.gui.clientes.DarAltaPersona;
import com.licensis.notaire.negocio.ControllerNegocio;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que reune la mayoria de los metodos de validaciones requeridos en distintas etapas y capas
 * de la aplicacion. Permite centralizar todos los metodos asociados a distintos tipos de
 * validaciones en una misma clase, alentando de esta manera la reutilizacion de dichos metodos y
 * tecnicas.
 *
 * @author matias
 */
public class AdministradorValidaciones
{

    private static AdministradorValidaciones instancia = null;

    private AdministradorValidaciones()
    {
    }

    public static AdministradorValidaciones getInstancia()
    {
        if (instancia == null)
        {
            instancia = new AdministradorValidaciones();
        }
        return instancia;
    }

    // <editor-fold defaultstate="collapsed" desc="Validaciones por Tipo de Campo">
    /**
     * Permite determina si un campo (de tipo de dato String) es vacio o no.
     *
     * @param campoParaValidar El campo tipo string a ser validado.
     * @return Verdadero si el campo es vacio, falso en caso contrario.
     */
    public Boolean validarCampoVacio(String campoParaValidar)
    {
        return campoParaValidar.isEmpty();
    }

    /**
     * Determina si el argumento tipo string contiene algun caracter que no sea un letra del
     * alfabeto. Basicamente, devuelve verdadero si todos los caracteres que componen el string son
     * unicamente letras, y falso si existe algun otro caracter, como por ejemplo, un numero, un
     * signo de puntuacion. etc.
     *
     * @param campoParaValidar Un string que debe ser validado.
     * @return resultado Verdadero si el string solo contiene letras, falso en caso contrario.
     */
    public Boolean validarCampoSoloTexto(String campoParaValidar)
    {
        Boolean resultado = Boolean.TRUE;
        char[] caractes = campoParaValidar.toCharArray();
        for (char c : caractes)
        {
            if (!((Character.isLetter(c)) || (Character.isSpaceChar(c))))
            {
                resultado = Boolean.FALSE;
            }
        }
        return resultado;
    }

    /**
     * Determina si el argumento tipo string contiene algun caracter que no sea un numero decimal.
     * Basicamente, devuelve verdadero si todos los caracteres que componen el string son unicamente
     * numeros decimales, y falso si existe algun otro caracter, como por ejemplo, una letra del
     * alfabeto, un signo de puntuacion, etc.
     *
     * @param campoParaValidar Un string que debe ser validado.
     * @return resultado Verdadero si el string solo contiene numeros decimales, falso en caso
     * contrario.
     */
    public Boolean validarCampoSoloNumerosEnteros(String campoParaValidar)
    {
        Boolean resultado = Boolean.TRUE;
        char[] caractes = campoParaValidar.toCharArray();
        for (char c : caractes)
        {
            if (!Character.isDigit(c))
            {
                resultado = Boolean.FALSE;
            }
        }
        return resultado;
    }

    /**
     * Determina si el argumento tipo string contiene algun caracter que no sea ni letra ni numero.
     *
     * @param campoParaValidar Un string que debe ser validado.
     * @return resultado Verdadero si el el argumento solo contiene letras y numeros, falso en caso
     * contrario.
     */
    public Boolean validarCampoLetrasYNumeros(String campoParaValidar)
    {
        Boolean resultado = Boolean.TRUE;
        try
        {
            if (campoParaValidar.isEmpty())
            {
                return false;
            }
            char[] caractes = campoParaValidar.toCharArray();
            for (char c : caractes)
            {
                if (!Character.isLetterOrDigit(c))
                {
                    if (!Character.isWhitespace(c))
                    {
                        resultado = Boolean.FALSE;
                    }
                }
            }

        }
        catch (NullPointerException e)
        {
            return false;
        }
        return resultado;
    }

    /**
     * Metodo que permite saber cuando se ingresa un caracter raro o especial
     *
     * @param pCampo
     * @return Retorno verdadero si hay carcateres raros o especiales como / ( ) ? ; y Falso si solo
     * son numeros
     */
    public Boolean validarCaracteres(String pCampo)
    {
        Boolean flag = false;

        for (int i = 0; i < pCampo.length(); i++)
        {
            char c = pCampo.charAt(i);

            if ((!Character.isLetter(c)
                    || Character.isSpaceChar(c)
                    || c == '-' || c == '_' || c == '(' || c == '.' || c == ','
                    || c == '@' || c == '<' || c == '>' || c == '/' || c == '?'
                    || c == '¿') || c == '=' || c == '!' || c == '"' || c == '#'
                    || c == '&' || c == '¡' || c == '+' || c == '*' || c == ' ')
            {
                return flag = true;

            }
        }
        return flag;
    }

    /**
     * Metodo que permite validar que el campo unicamente contega letras y/o numeros
     *
     * @param pCampo
     * @return Retorna Verdadero si no contiene caracteres y/o numeros, y Falso si el campo contiene
     * caracteres raros
     */
    public Boolean validarSoloLetrasNumeros(String pCampo)
    {
        Boolean flag = true;

        for (int i = 0; i < pCampo.length(); i++)
        {
            char c = pCampo.charAt(i);

            if (c == '-' || c == '_' || c == '(' || c == '.' || c == ','
                    || c == '@' || c == '<' || c == '>' || c == '/' || c == '?'
                    || c == '¿' || c == '=' || c == '!' || c == '"' || c == '#'
                    || c == '&' || c == '¡' || c == '+' || c == '*')
            {
                return flag = false;

            }
        }
        return flag;

    }

    /**
     * Valido, si es letra o guion o numero.
     *
     * @param pCampo
     * @return Retorno FALSE si no es valido.
     */
    public Boolean validarLetrasGuiones(String pCampo)
    {
        Boolean flag = true;

        for (int i = 0; i < pCampo.length(); i++)
        {
            char c = pCampo.charAt(i);

            if (!Character.isLetterOrDigit(c))
            {
                if (c != '-')
                {
                    return flag = false;
                }
            }
        }
        return flag;
    }

    /**
     * Metodo que permite validar una cantidad de caracteres en un campo determinado
     *
     * @param pCantCharter La cantidad de campos que se desea que tenga el campo como maximo
     * @param pCampo La cadena que se desea contar
     * @return True si la cadena supera pCantCharter y false si no se cumple
     */
    public Boolean validarCantidadCaracteres(int pCantCharter, String pCampo)
    {
        Boolean flag = false;
        int count = 0;

        for (int i = 0; i < pCampo.length(); i++)
        {
            count++;
        }

        if (count > pCantCharter)
        {
            flag = true;
        }

        return flag;
    }

    /**
     * Determina que el valor entero sea mayor o igual que cero.
     *
     * @param numeroParaValidar
     * @return
     */
    public Boolean validarNumero(int numeroParaValidar)
    {
        Boolean resultado = Boolean.TRUE;

        if (numeroParaValidar < 0)
        {
            return false;
        }

        return resultado;
    }

    /**
     * Metodo que permite saber cuando una cadena de caracteres contiene un espacio
     *
     * @param pCampo
     * @return Verdadero si contiene espacios, y falso si no contiene
     */
    public Boolean validarCampoEspacios(String pCampo)
    {
        Boolean flag = false;

        for (int i = 0; i < pCampo.length(); i++)
        {
            char c = pCampo.charAt(i);

            if ((Character.isSpaceChar(c)))
            {
                return flag = true;
            }

        }
        return flag;
    }

    /**
     * Metodo que permite saber si el string contiene un numero Floar o una letra
     *
     * @param numero
     * @return Verdadero si es el string es un numero, falso de lo contrarip
     */
    public Boolean validarNumeroFloat(String numero)
    {
        Boolean valido = true;

        try
        {
            Float numeroFloat = Float.parseFloat(numero);
        }
        catch (NumberFormatException e)
        {
            valido = false;
        }
        finally
        {
            return valido;
        }
    }

    /**
     * Determina si la fecha indicada es anterior a la fecha actual.
     *
     * @param fechaParaValidar La fecha para validar.
     * @return resultado Verdadero si la fecha es igual o mayor a hoy, falso en caso contrario.
     *
     */
    public Boolean validarFechaPosteriorHoy(Date fechaParaValidar)
    {
        boolean resultado = true;

        if (fechaParaValidar.before(Calendar.getInstance().getTime()))
        {
            return false;
        }

        return resultado;
    }

    /**
     * Determinar que la fechaHasta sea mayor que FechaDesde.
     *
     * @param fechaDesde La fecha inicial.
     * @param fechaHasta La fecha final.
     * @return resultado Verdadero si fechaDesde es mayor que hoy, y fechaHasta es mayor que
     * fechaDesde.
     */
    public Boolean validarFechasPosterioresHoy(Date fechaDesde, Date fechaHasta)
    {
        try
        {
//            if (this.validarFechaPosteriorHoy(fechaDesde))
//            {
//            
//            }
//            else
//            {
//                return false;
//            }
            if (fechaHasta.after(fechaDesde))
            {
                return true;
            } else
            {
                return false;
            }

        }
        catch (NullPointerException ex)
        {
            return false;
        }
    }

    /**
     * Metodo que permite validar que el anio indicado no sea mayor que el anio actual.
     *
     * @param anio El anio a ser validado.
     * @return resultado Verdadero si el anio indicado es igual o menor que el anio actual, falso en
     * caso contrario.
     */
    public Boolean validarFechaAnioLimite(Date anio)
    {
        boolean resultado = false;

        Date actual = Calendar.getInstance().getTime();

        if (actual.before(anio))
        {
            resultado = true;
        }
        if (anio.getYear() == actual.getYear())
        {
            resultado = true;
        }

        return resultado;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Validaciones por Formulario">
// <editor-fold defaultstate="collapsed" desc="Login">    
    /**
     * Metodo que valida que el usuario ingresado sea valido para lo cual se tiene que cumplir que
     * exista en la BBDD; que el nombre de usuario y contraseña coincida; y que el estado del
     * usuario sea "valido" / "activo".
     *
     * @param usuarioLogin El DtoUsuario a ser validado.
     * @return resultadoValidacion Verdadero si es el usuario indicado existe y es valido, falso en
     * caso contrario.
     */
    /**
     * Método que valida un usuario usando REST API.
     * Nota: La validación real se hace en AdministradorSesion.validarUsuario()
     * Este método solo verifica que el usuario tenga datos válidos.
     */
    public Boolean validarUsuario(DtoUsuario usuarioLogin)
    {
        // La validación real se hace en AdministradorSesion.validarUsuario()
        // que usa REST API. Este método solo verifica que el usuario tenga datos válidos.
        if (usuarioLogin != null && usuarioLogin.isValido())
        {
            AdministradorSesion.getInstancia().setSesionUsuario(usuarioLogin);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Clientes">

    /**
     * Metodo que permite validar el formulario DarAltaPersona,
     *
     * @param formulario
     * @return Un string con el mensaje de error correspondiente
     */
    public String validarDarAltaPersona(DarAltaPersona formulario)
    {

        String msj = "";

        String campoNombre = DarAltaPersona.getInstancia().campoNombre.getText();
        String campoApellido = DarAltaPersona.getInstancia().campoApellido.getText();
        String campoNroIdentificacion = DarAltaPersona.getInstancia().campoNumeroIdentificacion.getText();

        if (this.validarCampoVacio(campoNombre)
                || this.validarCampoVacio(campoApellido)
                || this.validarCampoVacio(campoNroIdentificacion))
        {
            msj = "Debe completar al menos: Nombre, Apellido, Tipo y Nro ident.";
        }

        return msj;

    }

    /**
     * Metodo que permite validar el formulario AdministrarCliente, cuando es instanciado para
     * modificar una persona.
     *
     * @param formulario
     * @return Un string con el mensaje de error correspondiente
     */
    public String validarModificarPersona(AdministrarCliente formulario)
    {

        String msj = "";

        String campoNombre = AdministrarCliente.getInstancia().campoNombre.getText();
        String campoApellido = AdministrarCliente.getInstancia().campoApellido.getText();
        String campoNroIdentificacion = AdministrarCliente.getInstancia().campoNumeroIdentificacion.getText();

        if (this.validarCampoVacio(campoNombre)
                || this.validarCampoVacio(campoApellido)
                || this.validarCampoVacio(campoNroIdentificacion))
        {
            msj = "Debe completar al menos: Nombre, Apellido, Tipo y Nro ident.";
        }

        return msj;

    }

    /**
     * Metodo que permite validar el formulario AdministrarCliente, cuando es instanciado para
     * darAltaCliente y modificarCliente,
     *
     * @param formulario
     * @return Un string con el mensaje de error correspondiente
     */
    public String validarCliente(AdministrarCliente formulario)
    {
        String msj = "";

        // 1- Controlo los campos de persona
        msj = this.validarModificarPersona(formulario);

        // 2- Controlo los campos de cliente
        if ("".equals(msj))
        {

            if (formulario.campoNombre.getText().isEmpty()
                    || formulario.campoApellido.getText().isEmpty()
                    || formulario.campoNacionalidad.getText().isEmpty()
                    || formulario.campoOcupacion.getText().isEmpty()
                    || formulario.campoDomicilio.getText().isEmpty()
                    || formulario.campoCuit.getText().isEmpty())
            {
                msj = "Debe completar Todos los datos del Cliente";
            }
        }

        return msj;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Presupuestos">
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Gestiones">
// <editor-fold defaultstate="collapsed" desc="Gestion">
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Documentacion">

    /**
     * Metodo que valida el dto de un documento presenta, donde, para aquellos campos nulos (aun no
     * se
     * han registrado datos de ingresos, salidas, etc. para ese documento) se setean con un valor
     * por default.
     *
     * @param dtoDocumento
     */
    public DtoDocumentoPresentado validarDtoDocumentoPresentado(DtoDocumentoPresentado dtoDocumento)
    {

        if (dtoDocumento.getNumeroCarton() == null)
        {

        }
        return dtoDocumento;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Escrituras">
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Testimonios">
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Inscripciones">
// </editor-fold>
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Protocolo">
// <editor-fold defaultstate="collapsed" desc="Folios">
// </editor-fold>
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Pagos">
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Administracion">
// <editor-fold defaultstate="collapsed" desc="Usuarios">
    /**
     * Metodo que permite validar los campos del formulario DarAltaUsuario
     *
     * @param formulario
     * @return Un string con el mensaje de error correspondiente
     */
    public String validarAltaUsuario(DarAltaUsuario formulario)
    {

        String campo = "";
        Boolean flag = false;
        String msj = "";

        if (formulario.campoNombreUsuario.getText().isEmpty()
                && formulario.campoContrasenia.getText().isEmpty()
                && formulario.campoRepetirNuevaContrasenia.getText().isEmpty())
        {
            return msj = "Completar todos los campos del Usuario.";
        }

        if (!ControllerNegocio.getInstancia().isPasswordCorrect(formulario.campoContrasenia.getPassword(), formulario.campoRepetirNuevaContrasenia.getPassword()).getFlag())
        {
            return msj = "La contrasenia no coincide";
        }

        Integer longContrasenia = formulario.campoContrasenia.getText().length();
        if (longContrasenia <= 4)
        {
            return msj = "La constrasenia debe contener un minimo de 5 caracteres";
        }

        campo = formulario.campoNombreUsuario.getText();
        flag = AdministradorValidaciones.getInstancia().validarCampoEspacios(campo);

        if (flag)
        {
            return msj = "Nombre de Usuario NO permite espacios.";
        }

        flag = AdministradorValidaciones.getInstancia().validarCaracteres(campo);

        if (flag)
        {
            return msj = "Nombre de Usuario solo permite Letras.";
        }

        return msj;
    }

    /**
     * Metodo que permite validar los campos del formulario ModificarUsuario
     *
     * @param formulario
     * @return Un string con el mensaje de error correspondiente
     *
     */
    public String validarModificarUsuario(ModificarUsuario formulario)
    {

        String campo = "";
        Boolean flag = false;
        String msj = "";

        if (formulario.campoNuevoNombreUsuario.getText().isEmpty()
                && formulario.campoNuevaContrasenia.getText().isEmpty()
                && formulario.campoRepetirNuevaContrasenia.getText().isEmpty())
        {
            return msj = "Completar todos los campos de Usuario";
        }

        if (!ControllerNegocio.getInstancia().isPasswordCorrect(formulario.campoNuevaContrasenia.getPassword(), formulario.campoRepetirNuevaContrasenia.getPassword()).getFlag())
        {
            return msj = "La conrasenia no coincide";
        }

        Integer longContrasenia = formulario.campoNuevaContrasenia.getText().length();
        if (longContrasenia <= 4)
        {
            return msj = "La constrasenia debe contener un minimo de 5 caracteres";
        }

        campo = formulario.campoNuevoNombreUsuario.getText();
        flag = AdministradorValidaciones.getInstancia().validarCampoEspacios(campo);

        if (flag)
        {
            return msj = "Nombre de Usuario No permite especios";
        }
        return msj;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Escribanos">
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Tramites">
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Documentos">

    /**
     * Metodo que permite validar los campos del formulario IngresarDocumento
     *
     * @param pFormualrio
     * @return Un mensaje con el resultado de la validacion
     */
    public String validarIngresarDocumento(IngresarDocumento pFormualrio)
    {
        String msj = "";

        String nombreDocumento = pFormualrio.campoNombreDocumento.getText();
        Boolean vence = pFormualrio.checkVenceDocumento.isSelected();
        Integer diasVencimiento = new Integer(pFormualrio.selectorDiasVencimiento.getValue().hashCode());

        if (!nombreDocumento.isEmpty())
        {
            if (vence && diasVencimiento.intValue() <= 0)
            {//Si vence el docuemento tiene que tener dias de validez
                msj = "Debe ingresar la cantidad de dias a vencer del documento";
            }
        } else
        {
            msj = "Debe completar el campo Nombre del Documento";
        }

        return msj;
    }

    /**
     * Metodo que permite validar los campos del formulario ModificarDocumento
     *
     * @param pFormualrio
     * @return Un mensaje con el resultado de la validacion
     */
    public String validarModificarDocumento(ModificarDocumento pFormualrio)
    {
        String msj = "";

        String nombreDocumento = pFormualrio.campoNombreDocumento.getText();
        Boolean vence = pFormualrio.checkVenceDocumento.isSelected();
        Integer diasVencimiento = new Integer(pFormualrio.selectorDiasVencimiento.getValue().hashCode());

        if (!nombreDocumento.isEmpty())
        {
            if (vence && diasVencimiento.intValue() <= 0)
            {//Si vence el docuemento tiene que tener dias de validez
                msj = "Debe ingresar la cantidad de dias a vencer del documento";
            }
        } else
        {
            msj = "Debe completar el campo Nombre";
        }

        return msj;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Conceptos">   
    /**
     * Metodo que permite validar los campos del formulario Ingresar Concepto
     *
     * @param pformulario
     * @return Un mensaje con el resultado de la validacion
     */
    public String validarIngresarConcepto(IngresarConcepto pformulario)
    {

        String msj = "";

        String nombreConcepto = pformulario.campoNombreConcepto.getText();
        String valorConcepto = pformulario.campoValorConcepto.getText();
        String valorPorcentaje = pformulario.selectorPorcentaje.getValue().toString();

        if (!validarCampoVacio(nombreConcepto))
        {

            if (!validarCampoLetrasYNumeros(nombreConcepto))
            {
                msj = "Nombre debe contener solo letras o numeros.";
            } else
            {

                if (!validarCampoVacio(valorConcepto))
                {
                    if (!validarNumeroFloat(valorConcepto))
                    {
                        msj = "Debe completar un numero valido en valor, ej: 12.50";
                    }
                } else
                {
                    if (validarCampoVacio(valorPorcentaje))
                    {
                        msj = "Debe completar valor o porcentaje.";
                    } else
                    {
                        if ((Integer) pformulario.selectorPorcentaje.getValue() <= 0)
                        {
                            msj = "El porcentaje debe ser mayor a 0.";
                        }
                    }
                }
            }

        } else
        {
            msj = "Debe completar el nombre.";
        }
        return msj;
    }

    public String validarModificarConcepto(ModificarConcepto pformulario)
    {

        String msj = "";

        String nombreConcepto = pformulario.campoNuevoNombre.getText();
        String valorConcepto = pformulario.campoNuevoValor.getText();
        String valorPorcentaje = pformulario.selectorNuevoPorcentaje.getValue().toString();

        if (!validarCampoVacio(nombreConcepto))
        {

            if (!validarCampoLetrasYNumeros(nombreConcepto))
            {
                msj = "Nombre debe contener solo letras o numeros.";
            } else
            {

                if (!validarCampoVacio(valorConcepto))
                {
                    if (!validarNumeroFloat(valorConcepto))
                    {
                        msj = "Debe completar un numero valido en valor, ej: 12.50";
                    }
                } else
                {
                    if (!validarCampoVacio(valorPorcentaje))
                    {
                        msj = "Debe completar valor o porcentaje.";
                    }
                }
            }

        } else
        {
            msj = "Debe completar el nombre.";
        }
        return msj;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Estados de Gestion">
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Folios">
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Plantillas de Presupuesto">
// </editor-fold>
// </editor-fold>
// </editor-fold>
}
