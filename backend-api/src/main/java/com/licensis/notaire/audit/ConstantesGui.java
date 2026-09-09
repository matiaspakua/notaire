package com.licensis.notaire.audit;

/**
 * Shared constants used for audit operations (subset of GUI constants).
 * This copy lives in `audit` for backend use so frontend GUI can keep using
 * `com.licensis.notaire.gui.ConstantesGui`.
 */
public interface ConstantesGui
{
    public int ERROR = -1;
    public int SINRecord = 0;
    public String MODIFICARPerson = "Modificar Persona";
    public String MODIFICARClient = "Modificar Cliente";
    public String DARALTAClient = "Dar Alta Cliente";
    public String SearchClient = "Buscar Cliente";
    public String DARALTAPerson = "Dar Alta Persona";
    public String SearchPerson = "Buscar Persona";
    public String SearchPersonClient = "Personas - Clientes";
    public String Persons = "PERSONAS";
    public String Client = "Cliente";
    public String Person = "Persona";
    public String VENTANAPerson = "Ventana Persona";
    public String VENTANAClient = "Ventana Cliente";
    public String VENTANAPersonCLIENTES = "Ventana Persona - Cliente";
    public String DARALTAUser = "Dar Alta Usuario";
    public String MODIFICARUser = "Modificar Usuario";
    public String DARALTANotary = "Dar Alta Escribano";
    public String MODIFICARNotary = "Modificar Escribano";
    public String REGISTRARSubstitution = "Registrar Suplencia";
    public String INGRESARNUEVOTypeFolio = "Ingresar nuevo tipo de folio";
    public String MODIFICARTypeFolio = "Modificar tipo de folio";
    public String ELIMINARTypeFolio = "Eliminar tipo de folio";
    public String INGRESAR_NUEVOS_FOLIOS = "Ingresar nuevos folios";
    public String MODIFICARFolio = "Modificar Folio";
    public String FolioPROTOCOLOPRINCIPAL = "Principal";
    public String FolioPROTOCOLOAuxiliary = "Auxiliar";
    public String FolioPROTOCOLOESPECIAL = "Especial";
    public String INGRESARNUEVOProcedureType = "Ingresar nuevo tipo de tramite";
    public String INGRESARNUEVAProcedureTemplate = "Ingresar Nueva Plantilla de tramite";
    public String MODIFICARProcedureType = "Modifica tipo de tramite";
    public String MODIFICARProcedureTemplate = "Modificar plantilla de tramite";
    public String ELIMINARProcedureType = "Eliminar tipo de tramite";
    public String ELIMINARProcedureTemplate = "Eliminar plantilla de tramite";
    public String INICIARManagement = "Iniciar Gestion";
    public String MODIFICARManagement = "Modificar gestion";
    public String MODIFICARManagementAGREGARClient = "Modificar Gestion Agregar Cliente";
    public String MODIFICARManagementQUITARClient = "Modificar Gestion Quitar Cliente";
    public String DetailManagement = "Detalle Gestion";
    public String VER_GESTIONES = "Ver Gestiones Clientes";
    public String VERHistoryManagement = "Ver Historial Gestion";
    public String INGRESARStatusManagement = "Ingresar Estado Gestion";
    public String MODIFICARStatusManagement = "Modificar Estado Gestion";
    public String ArchivingManagement = "Archivar Gestion";
    public String CREARBudget = "Crear Presupuesto";
    public String MODIFICARBudget = "Modificar Presupuesto";
    public String ModuleBudget = "Presupuestos";
    public String SearchBudget = "Buscar Presupuesto";
    public String REGISTRARPayment = "Registrar Pago";
    public String CONSULTARPayments = "Consultar Pagos";
    public String PREPARARDeed = "Preparar Escritura";
    public String MODIFICARDeed = "Modificar Escritura";
    public String SearchDeed = "Buscar Escritura";
    public String INGRESARNUEVOTypeDocument = "Ingresar nuevo tipo de documento";
    public String MODIFICARTypeDocument = "Modificar tipo de documento";
    public String ELIMINARTypeDocument = "Eliminar tipo de documento";
    public String DOCUMENTACIONEntry = "Registrar Documentos";
    public String DOCUMENTACIONDebt = "Documentacion Con Deuda";
    public String DOCUMENTACION_ENTIDAD_EXTERNA = "Entidad Externa";
    public String DOCUMENTACION_REINGRESO = "Reingresar Documentacion";
    public String GENERARTestimony = "Generar Testimonio";
    public String VERIFICARTestimony = "Verificar Testimonio";
    public String RETIRARTestimony = "Retirar Testimonio";
    public String INGRESARPARARegistration = "Ingresar para inscripcion";
    public String REGISTRAR_REINGRESO = "Registrar Reingreso";
    public String REGISTRARRegistration = "Registrar Inscripcion";
    public String INGRESARNUEVOConcept = "Ingresar nuevo concepto";
    public String MODIFICARConcept = "Modificar Concepto";
    public String ELIMINARConcept = "Eliminar concepto";
    public String CREARTemplateDEBudget = "Crear plantilla de presupuesto";
    public String MODIFICARTemplateDEBudget = "Modificar plantilla de presupuesto";
    public String ELIMINARTemplateDEBudget = "Eliminar plantilla de presupuesto";
    public String ADMINISTRADOR = "Administrador";
    public String CAMPODateEntry = "Fecha de ingreso";
    public String CAMPODateREINGRESO = "Fecha de reigreso";
    public String CAMPODateExit = "Fecha de salida";
    public String CAMPODatePayment = "Fecha de pago";
    public String CAMPODateLIBERACION = "Fecha de liberacion";
    public String CAMPOPrepared = "Preparado";
    public String CAMPONROCardnumber = "Nro Carton";
    public String CAMPO_OBERVADO = "Observado";
    public String CAMPOAmount = "Importe";
    public String CAMPONotes = "Observaciones";
    public String CAMPOName = "Nombre";
    public String CAMPOReleased = "Liberado";
    public String CAMPODateDue = "Fecha Vencimiento";
    public String CAMPODueDays = "Dias Vencimiento";
}
