/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.gui;

/**
 * Interfaz que agrupar todas las constantes utilizadas en la capa presentacion (GUI)
 *
 * @author matias
 */
public interface ConstantesGui
{

    /**
     * Constante general para indicar algun tipo de error (valor -1).
     */
    public int ERROR = -1;
    /**
     * Indica que no existen registros disponibles para un tipo de entidad en particular.
     */
    public int SIN_REGISTRO = 0;
    // Constantes utilizadas en ABM Personas-Clientes
    public String MODIFICAR_PERSONA = "Modificar Persona";
    public String MODIFICAR_CLIENTE = "Modificar Cliente";
    public String DAR_ALTA_CLIENTE = "Dar Alta Cliente";
    public String BUSCAR_CLIENTE = "Buscar Cliente";
    public String DAR_ALTA_PERSONA = "Dar Alta Persona";
    public String BUSCAR_PERSONA = "Buscar Persona";
    public String BUSCAR_PERSONA_CLIENTE = "Personas - Clientes";
    public String PERSONAS = "PERSONAS";
    public String CLIENTE = "Cliente";
    public String PERSONA = "Persona";
    public String VENTANA_PERSONA = "Ventana Persona";
    public String VENTANA_CLIENTE = "Ventana Cliente";
    public String VENTANA_PERSONA_CLIENTES = "Ventana Persona - Cliente";
    //  Constantes utilizadas en ABM Usuarios
    public String DAR_ALTA_USUARIO = "Dar Alta Usuario";
    public String MODIFICAR_USUARIO = "Modificar Usuario";
    //  Constantes utilizadas en los formularios.
    public String DAR_ALTA_ESCRIBANO = "Dar Alta Escribano";
    public String MODIFICAR_ESCRIBANO = "Modificar Escribano";
    public String REGISTRAR_SUPLENCIA = "Registrar Suplencia";
    //  Tipos de folios
    public String INGRESAR_NUEVO_TIPO_FOLIO = "Ingresar nuevo tipo de folio";
    public String MODIFICAR_TIPO_FOLIO = "Modificar tipo de folio";
    public String ELIMINAR_TIPO_FOLIO = "Eliminar tipo de folio";    
    public String INGRESAR_NUEVOS_FOLIOS = "Ingresar nuevos folios";
    public String MODIFICAR_FOLIO = "Modificar Folio";
    public String FOLIO_PROTOCOLO_PRINCIPAL = "Principal";
    public String FOLIO_PROTOCOLO_AUXILIAR = "Auxiliar";
    public String FOLIO_PROTOCOLO_ESPECIAL = "Especial";
    //  Tramite
    public String INGRESAR_NUEVO_TIPO_DE_TRAMITE = "Ingresar nuevo tipo de tramite";
    public String INGRESAR_NUEVA_PLANTILLA_TRAMITE = "Ingresar Nueva Plantilla de tramite";
    public String MODIFICAR_TIPO_DE_TRAMITE = "Modifica tipo de tramite";
    public String MODIFICAR_PLANTILLA_TRAMITE = "Modificar plantilla de tramite"; 
    public String ELIMINAR_TIPO_DE_TRAMITE = "Eliminar tipo de tramite";
    public String ELIMINAR_PLANTILLA_TRAMITE = "Eliminar plantilla de tramite";
    //  Constantes Gestion
    public String INICIAR_GESTION = "Iniciar Gestion";
    public String MODIFICAR_GESTION = "Modificar gestion";
    public String MODIFICAR_GESTION_AGREGAR_CLIENTE = "Modificar Gestion Agregar Cliente";
    public String MODIFICAR_GESTION_QUITAR_CLIENTE = "Modificar Gestion Quitar Cliente";
    public String DETALLE_GESTION = "Detalle Gestion";
    public String VER_GESTIONES = "Ver Gestiones Clientes";
    public String VER_HISTORIAL_GESTION = "Ver Historial Gestion";
    public String INGRESAR_ESTADO_GESTION = "Ingresar Estado Gestion";
    public String MODIFICAR_ESTADO_GESTION = "Modificar Estado Gestion";
    public String ARCHIVAR_GESTION = "Archivar Gestion";
    //  Constantes Presupuestos
    public String CREAR_PRESUPUESTO = "Crear Presupuesto";
    public String MODIFICAR_PRESUPUESTO = "Modificar Presupuesto";
    public String MODULO_PRESUPUESTO = "Presupuestos";
    public String BUSCAR_PRESUPUESTO = "Buscar Presupuesto";
    //  Pagos
    public String REGISTRAR_PAGO = "Registrar Pago";
    public String CONSULTAR_PAGOS = "Consultar Pagos";
    //  Escrituras
    public String PREPARAR_ESCRITURA = "Preparar Escritura";
    public String MODIFICAR_ESCRITURA = "Modificar Escritura";
    public String BUSCAR_ESCRITURA = "Buscar Escritura";
    //Documentacion
    public String INGRESAR_NUEVO_TIPO_DOCUMENTO = "Ingresar nuevo tipo de documento";
    public String MODIFICAR_TIPO_DOCUMENTO = "Modificar tipo de documento";
    public String ELIMINAR_TIPO_DOCUMENTO = "Eliminar tipo de documento";
    //  Documentacion
    public String DOCUMENTACION_INGRESO = "Registrar Documentos";
    public String DOCUMENTACION_DEUDA = "Documentacion Con Deuda";
    public String DOCUMENTACION_ENTIDAD_EXTERNA = "Entidad Externa";
    public String DOCUMENTACION_REINGRESO = "Reingresar Documentacion";
    //  Testimonios
    public String GENERAR_TESTIMONIO = "Generar Testimonio";
    public String VERIFICAR_TESTIMONIO = "Verificar Testimonio";
    public String RETIRAR_TESTIMONIO = "Retirar Testimonio";
    //  Inscripciones
    public String INGRESAR_PARA_INSCRIPCION = "Ingresar para inscripcion";
    public String REGISTRAR_REINGRESO = "Registrar Reingreso";
    public String REGISTRAR_INSCRIPCION = "Registrar Inscripcion";
    // Conceptos
    public String INGRESAR_NUEVO_CONCEPTO = "Ingresar nuevo concepto";
    public String MODIFICAR_CONCEPTO = "Modificar Concepto";
    public String ELIMINAR_CONCEPTO = "Eliminar concepto";
    //  plantillas de presupuesto
    public String CREAR_PLANTILLA_DE_PRESUPUESTO = "Crear plantilla de presupuesto";
    public String MODIFICAR_PLANTILLA_DE_PRESUPUESTO = "Modificar plantilla de presupuesto";
    public String ELIMINAR_PLANTILLA_DE_PRESUPUESTO = "Eliminar plantilla de presupuesto";
    // Usuario Administrador
    public String ADMINISTRADOR = "Administrador";
    
    //Error de campo
    public String CAMPO_FECHA_INGRESO = "Fecha de ingreso";
       public String CAMPO_FECHA_REINGRESO = "Fecha de reigreso";
    public String CAMPO_FECHA_SALIDA = "Fecha de salida";
    public String CAMPO_FECHA_PAGO = "Fecha de pago";
    public String CAMPO_FECHA_lIBERACION = "Fecha de liberacion";
    public String CAMPO_PREPARADO= "Preparado";
    public String CAMPO_NRO_CARTON = "Nro Carton";
    public String CAMPO_OBERVADO = "Observado";
    public String CAMPO_IMPORTE= "Importe";
    public String CAMPO_OBSERVACIONES = "Observaciones";
    public String CAMPO_NOMBRE = "Nombre";
    public String CAMPO_LIBERADO = "Liberado";
   public String CAMPO_FECHA_VENCIMIENTO = "Fecha Vencimiento";
      public String CAMPO_DIAS_VENCIMIENTO = "Dias Vencimiento";
}
