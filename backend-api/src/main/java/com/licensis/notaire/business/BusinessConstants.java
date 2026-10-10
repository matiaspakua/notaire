/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

/**
 * Clase that agrupa all the constantes utilizadas in the capa of negocio.
 *
 * @author matias
 */
public interface BusinessConstants
{

    /**
     * All the objejos must be instanciados in a status valid. If a objeto is creado, it
     * asigna to the idxx this valor for indicar that is a objeto in a status inconsistente, already sea
     * by that aun not has been persistido or by that has habido algun tipo of error peristiendo the
     * entidad, buscandola, etc.
     * <p>
     * For saber if a objeto esta in a status consistente, must
     * verificar que su ID sea distinto a {@link ID_OBJETO_NO_VALIDO}.
     */
    public int ID_OBJETO_NO_VALIDO = -1;
    //Estados Management
    /**
     * Status inicial of a history when inicia a management: "Management iniciada"
     */
    public String ManagementStatusINICIAL = "Iniciada";
    public String ManagementStatusMODIFICADA = "Management Modificada";
    public String ManagementENProcedure = "En Procedure";
    public String DOCUMENTACION_COMPLETA = "Documentation Completa";
    public String ManagementCONDeedFIRMADA = "Management with Deed Firmada";
    public String ManagementCONDeedSINFIRMAR = "Management with Deed Sin Firmar";
    public String ManagementCONDeedANULADA = "Management with Deed Anulada";
    public String ManagementCONDeedNOPASO = "Management with Deed Not Step";
    public String ManagementCONDeedRegistered = "Management with Deed Inscripta";
    public String ManagementCONDOCUMENTACIONCOMPLETA = "Documentation Completa";
    public String ManagementARCHIVADA = "Archivada";
    /**
     * Determina que un tipo de folio en particular se puede usar.
     */
    public boolean FolioTypeEnabled = true;
    public boolean FolioTypeDESHABILITADO = false;

    /**
     * Determina que un tipo de folio ha sido eliminado o no se puede utilizar.
     */
    //  All the estados posibles of the folios.
    public String StatusFolioNUEVOS = "Folio New";
    public String StatusFolioUTILIZADO = "Folio Utilizado";
    public String StatusFolioERROSE = "Folio Errose";

    public String ModuleMODIFICARPerson = "Modificar Person";
    public String ModuleMODIFICARClient = "Modificar Cliente";
    /**
     * The distintos status of a deed.
     */
    public String DeedFIRMADA = "Firmada";
    public String DeedSINFIRMAR = "Sin Firmar";
    public String DeedANULADA = "Anulada";
    public String DeedNOPASO = "No Paso";
    public String DeedRegistered = "Inscripta";
    /**
     * The distintos estados of the circuito registral of a Registration draft (CU82).
     */
    public String RegistrationDraftGENERADA = "Generada";
    public String RegistrationDraftPRESENTADA = "Presented for registration";
    public String RegistrationDraftOBSERVADA = "Observado";
    public String RegistrationDraftRegistered = "Inscripto";
    /**
     * The distintos tipos of users.
     */
    public String UserEMPLEADO = "Empleado";
    public String UserNotary = "Notary";

    /**
     * Quienes entregan the documentation
     */
    public String DOCUMENTACION_ENTIDAD_EXTERNA = "Entidad Externa";
    public String DOCUMENTACIONClient = "Cliente";
}
