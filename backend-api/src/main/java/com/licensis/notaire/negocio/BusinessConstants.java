/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

/**
 * Clase que agrupa todas las constantes utilizadas en la capa de negocio.
 *
 * @author matias
 */
public interface BusinessConstants
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
    public String ManagementStatusINICIAL = "Iniciada";
    public String ManagementStatusMODIFICADA = "Gestion Modificada";
    public String ManagementENProcedure = "En Tramite";
    public String DOCUMENTACION_COMPLETA = "Documentacion Completa";
    public String ManagementCONDeedFIRMADA = "Gestion con Escritura Firmada";
    public String ManagementCONDeedSINFIRMAR = "Gestion con Escritura Sin Firmar";
    public String ManagementCONDeedANULADA = "Gestion con Escritura Anulada";
    public String ManagementCONDeedNOPASO = "Gestion con Escritura No Paso";
    public String ManagementCONDeedRegistered = "Gestion con Escritura Inscripta";
    public String ManagementCONDOCUMENTACIONCOMPLETA = "Documentacion Completa";
    public String ManagementARCHIVADA = "Archivada";
    /**
     * Determina que un tipo de folio en particular se puede usar.
     */
    public boolean FolioTypeEnabled = true;
    public boolean FolioTypeDESHABILITADO = false;

    /**
     * Determina que un tipo de folio ha sido eliminado o no se puede utilizar.
     */
    //  Todos los estados posibles de los folios.
    public String StatusFolioNUEVOS = "Folio Nuevo";
    public String StatusFolioUTILIZADO = "Folio Utilizado";
    public String StatusFolioERROSE = "Folio Errose";

    public String ModuleMODIFICARPerson = "Modificar Persona";
    public String ModuleMODIFICARClient = "Modificar Cliente";
    /**
     * Los distintos estado de una escritura.
     */
    public String DeedFIRMADA = "Firmada";
    public String DeedSINFIRMAR = "Sin Firmar";
    public String DeedANULADA = "Anulada";
    public String DeedNOPASO = "No Paso";
    public String DeedRegistered = "Inscripta";
    /**
     * Los distintos estados del circuito registral de una Minuta de Inscripción (CU82).
     */
    public String RegistrationDraftGENERADA = "Generada";
    public String RegistrationDraftPRESENTADA = "Presentado para inscripción";
    public String RegistrationDraftOBSERVADA = "Observado";
    public String RegistrationDraftRegistered = "Inscripto";
    /**
     * Los distintos tipos de usuarios.
     */
    public String UserEMPLEADO = "Empleado";
    public String UserNotary = "Escribano";

    /**
     * Quienes entregan la documentacion
     */
    public String DOCUMENTACION_ENTIDAD_EXTERNA = "Entidad Externa";
    public String DOCUMENTACIONClient = "Cliente";
}
