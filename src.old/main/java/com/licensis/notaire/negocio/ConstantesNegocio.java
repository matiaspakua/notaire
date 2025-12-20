/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

/**
 * Clase que agrupa todas las constantes utilizadas en la capa de negocio.
 *
 * @author matias
 */
public interface ConstantesNegocio
{

    /**
     * Todos los objejos deben ser instanciados en un estado valido. Si un objeto es creado, se le
     * asigna al idxx este valor para indicar que es un objeto en un estado inconsistente, ya sea
     * por que aun no ha sido persistido o por que ha habido algun tipo de error peristiendo la
     * entidad, buscandola, etc.
     * <p>
     * Para saber si un objeto esta en un estado consistente, se debe
     * verificar que su ID sea distinto a {@link ID_OBJETO_NO_VALIDO}.
     */
    public int ID_OBJETO_NO_VALIDO = -1;
    //Estados Gestion
    /**
     * Estado inicial de un historial cuando se inicia una gestion: "Gestion iniciada"
     */
    public String ESTADO_DE_GESTION_INICIAL = "Iniciada";
    public String ESTADO_DE_GESTION_MODIFICADA = "Gestion Modificada";
    public String GESTION_EN_TRAMITE = "En Tramite";
    public String DOCUMENTACION_COMPLETA = "Documentacion Completa";
    public String GESTION_CON_ESCRITURA_FIRMADA = "Gestion con Escritura Firmada";
    public String GESTION_CON_ESCRITURA_SIN_FIRMAR = "Gestion con Escritura Sin Firmar";
    public String GESTION_CON_ESCRITURA_ANULADA = "Gestion con Escritura Anulada";
    public String GESTION_CON_ESCRITURA_NO_PASO = "Gestion con Escritura No Paso";
    public String GESTION_CON_ESCRITURA_INSCRIPTA = "Gestion con Escritura Inscripta";
    public String GESTION_CON_DOCUMENTACION_COMPLETA = "Documentacion Completa";
    public String GESTION_ARCHIVADA = "Archivada";
    /**
     * Determina que un tipo de folio en particular se puede usar.
     */
    public boolean TIPO_DE_FOLIO_HABILITADO = true;
    public boolean TIPO_DE_FOLIO_DESHABILITADO = false;

    /**
     * Determina que un tipo de folio ha sido eliminado o no se puede utilizar.
     */
    //  Todos los estados posibles de los folios.
    public String ESTADO_FOLIO_NUEVOS = "Folio Nuevo";
    public String ESTADO_FOLIO_UTILIZADO = "Folio Utilizado";
    public String ESTADO_FOLIO_ERROSE = "Folio Errose";

    public String MODULO_MODIFICAR_PERSONA = "Modificar Persona";
    public String MODULO_MODIFICAR_CLIENTE = "Modificar Cliente";
    /**
     * Los distintos estado de una escritura.
     */
    public String ESCRITURA_FIRMADA = "Firmada";
    public String ESCRITURA_SIN_FIRMAR = "Sin Firmar";
    public String ESCRITURA_ANULADA = "Anulada";
    public String ESCRITURA_NO_PASO = "No Paso";
    public String ESCRITURA_INSCRIPTA = "Inscripta";
    /**
     * Los distintos tipos de usuarios.
     */
    public String USUARIO_EMPLEADO = "Empleado";
    public String USUARIO_ESCRIBANO = "Escribano";

    /**
     * Quienes entregan la documentacion
     */
    public String DOCUMENTACION_ENTIDAD_EXTERNA = "Entidad Externa";
    public String DOCUMENTACION_CLIENTE = "Cliente";
}
