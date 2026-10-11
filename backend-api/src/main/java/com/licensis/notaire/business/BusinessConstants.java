package com.licensis.notaire.business;

/**
 * Stored business vocabulary. Field names are English. The string values are
 * persisted labels and stay as recorded.
 *
 * @author matias
 */
public interface BusinessConstants
{

    /**
     * Sentinel id for an object that is not yet consistent: it was just created
     * and not persisted, or a persistence or lookup error left it invalid.
     * <p>
     * An object is consistent when its id is different from {@link #INVALID_OBJECT_ID}.
     */
    public int INVALID_OBJECT_ID = -1;
    // Management statuses
    /**
     * Initial history status when a management starts: "Iniciada".
     */
    public String ManagementStatusInitial = "Iniciada";
    public String ManagementStatusModified = "Gestion Modificada";
    public String ManagementInProcedure = "En Tramite";
    public String DocumentationComplete = "Documentacion Completa";
    public String ManagementWithSignedDeed = "Gestion con Escritura Firmada";
    public String ManagementWithUnsignedDeed = "Gestion con Escritura Sin Firmar";
    public String ManagementWithVoidedDeed = "Gestion con Escritura Anulada";
    public String ManagementWithDeedNotPassed = "Gestion con Escritura No Paso";
    public String ManagementWithRegisteredDeed = "Gestion con Escritura Inscripta";
    public String ManagementWithCompleteDocumentation = "Documentacion Completa";
    public String ManagementArchived = "Archivada";
    /**
     * A folio type that can still be used.
     */
    public boolean FolioTypeEnabled = true;
    public boolean FolioTypeDisabled = false;

    /**
     * A folio type that was removed or can no longer be used.
     */
    // Possible folio statuses.
    public String FolioStatusNew = "Folio Nuevo";
    public String FolioStatusUsed = "Folio Utilizado";
    public String FolioStatusDamaged = "Folio Errose";

    public String ModuleUpdatePerson = "Modificar Persona";
    public String ModuleUpdateClient = "Modificar Cliente";
    /**
     * Possible deed statuses.
     */
    public String DeedSigned = "Firmada";
    public String DeedUnsigned = "Sin Firmar";
    public String DeedVoided = "Anulada";
    public String DeedNotPassed = "No Paso";
    public String DeedRegistered = "Inscripta";
    /**
     * Registry-circuit statuses of a registration draft (CU82).
     */
    public String RegistrationDraftGenerated = "Generada";
    public String RegistrationDraftSubmitted = "Presentado para inscripción";
    public String RegistrationDraftObserved = "Observado";
    public String RegistrationDraftRegistered = "Inscripto";
    /**
     * User types. {@code Escribano} is an administrator type.
     */
    public String UserEmployee = "Empleado";
    public String UserNotary = "Escribano";

    /**
     * Who delivers the documentation.
     */
    public String DocumentationExternalEntity = "Entidad Externa";
    public String DocumentationClient = "Cliente";
}
