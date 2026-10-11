package com.licensis.notaire.business;

/**
 * Stored business vocabulary. The string values are persisted labels and stay
 * as recorded; only the comments around them are English.
 *
 * @author matias
 */
public interface BusinessConstants
{

    /**
     * Sentinel id for an object that is not yet consistent: it was just created
     * and not persisted, or a persistence or lookup error left it invalid.
     * <p>
     * An object is consistent when its id is different from {@link #ID_OBJETO_NO_VALIDO}.
     */
    public int ID_OBJETO_NO_VALIDO = -1;
    // Management statuses
    /**
     * Initial history status when a management starts: "Iniciada".
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
     * A folio type that can still be used.
     */
    public boolean FolioTypeEnabled = true;
    public boolean FolioTypeDESHABILITADO = false;

    /**
     * A folio type that was removed or can no longer be used.
     */
    // Possible folio statuses.
    public String StatusFolioNUEVOS = "Folio Nuevo";
    public String StatusFolioUTILIZADO = "Folio Utilizado";
    public String StatusFolioERROSE = "Folio Errose";

    public String ModuleMODIFICARPerson = "Modificar Persona";
    public String ModuleMODIFICARClient = "Modificar Cliente";
    /**
     * Possible deed statuses.
     */
    public String DeedFIRMADA = "Firmada";
    public String DeedSINFIRMAR = "Sin Firmar";
    public String DeedANULADA = "Anulada";
    public String DeedNOPASO = "No Paso";
    public String DeedRegistered = "Inscripta";
    /**
     * Registry-circuit statuses of a registration draft (CU82).
     */
    public String RegistrationDraftGENERADA = "Generada";
    public String RegistrationDraftPRESENTADA = "Presentado para inscripción";
    public String RegistrationDraftOBSERVADA = "Observado";
    public String RegistrationDraftRegistered = "Inscripto";
    /**
     * User types. {@code Escribano} is an administrator type.
     */
    public String UserEMPLEADO = "Empleado";
    public String UserNotary = "Escribano";

    /**
     * Who delivers the documentation.
     */
    public String DOCUMENTACION_ENTIDAD_EXTERNA = "Entidad Externa";
    public String DOCUMENTACIONClient = "Cliente";
}
