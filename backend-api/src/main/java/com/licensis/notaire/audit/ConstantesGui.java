package com.licensis.notaire.audit;

/**
 * Shared constants used for audit operations (subset of GUI constants).
 * This copy lives in `audit` for backend use so frontend GUI can keep using
 * `com.licensis.notaire.gui.ConstantesGui`.
 */
public interface ConstantesGui
{
    public int ERROR = -1;
    public int NoRecord = 0;
    public String UpdatePerson = "Modificar Persona";
    public String UpdateClient = "Modificar Cliente";
    public String CreateClient = "Dar Alta Cliente";
    public String SearchClient = "Buscar Cliente";
    public String CreatePerson = "Dar Alta Persona";
    public String SearchPerson = "Buscar Persona";
    public String SearchPersonClient = "Personas - Clientes";
    public String Persons = "PERSONAS";
    public String Client = "Cliente";
    public String Person = "Persona";
    public String WindowPerson = "Ventana Persona";
    public String WindowClient = "Ventana Cliente";
    public String WindowPersonClients = "Ventana Persona - Cliente";
    public String CreateUser = "Dar Alta Usuario";
    public String UpdateUser = "Modificar Usuario";
    public String CreateNotary = "Dar Alta Escribano";
    public String UpdateNotary = "Modificar Escribano";
    public String RegisterSubstitution = "Registrar Suplencia";
    public String EnterNewTypeFolio = "Ingresar nuevo tipo de folio";
    public String UpdateTypeFolio = "Modificar tipo de folio";
    public String DeleteTypeFolio = "Eliminar tipo de folio";
    public String ENTER_NEW_FOLIOS = "Ingresar nuevos folios";
    public String UpdateFolio = "Modificar Folio";
    public String FolioProtocolMain = "Principal";
    public String FolioProtocolAuxiliary = "Auxiliar";
    public String FolioProtocolSpecial = "Especial";
    public String EnterNewProcedureType = "Ingresar nuevo tipo de tramite";
    public String EnterNewProcedureTemplate = "Ingresar Nueva Plantilla de tramite";
    public String UpdateProcedureType = "Modifica tipo de tramite";
    public String UpdateProcedureTemplate = "Modificar plantilla de tramite";
    public String DeleteProcedureType = "Eliminar tipo de tramite";
    public String DeleteProcedureTemplate = "Eliminar plantilla de tramite";
    public String StartManagement = "Iniciar Gestion";
    public String UpdateManagement = "Modificar gestion";
    public String UpdateManagementAddClient = "Modificar Gestion Agregar Cliente";
    public String UpdateManagementRemoveClient = "Modificar Gestion Quitar Cliente";
    public String DetailManagement = "Detalle Gestion";
    public String VIEW_MANAGEMENTS = "Ver Gestiones Clientes";
    public String ViewHistoryManagement = "Ver Historial Gestion";
    public String EnterStatusManagement = "Ingresar Estado Gestion";
    public String UpdateStatusManagement = "Modificar Estado Gestion";
    public String ArchivingManagement = "Archivar Gestion";
    public String CreateBudget = "Crear Presupuesto";
    public String UpdateBudget = "Modificar Presupuesto";
    public String ModuleBudget = "Presupuestos";
    public String SearchBudget = "Buscar Presupuesto";
    public String RegisterPayment = "Registrar Pago";
    public String QueryPayments = "Consultar Pagos";
    public String PrepareDeed = "Preparar Escritura";
    public String UpdateDeed = "Modificar Escritura";
    public String SearchDeed = "Buscar Escritura";
    public String EnterNewTypeDocument = "Ingresar nuevo tipo de documento";
    public String UpdateTypeDocument = "Modificar tipo de documento";
    public String DeleteTypeDocument = "Eliminar tipo de documento";
    public String DocumentationEntry = "Registrar Documentos";
    public String DocumentationDebt = "Documentacion Con Deuda";
    public String DOCUMENTATION_EXTERNAL_ENTITY = "Entidad Externa";
    public String DOCUMENTATION_REENTRY = "Reingresar Documentacion";
    public String GenerateTestimony = "Generar Testimonio";
    public String VerifyTestimony = "Verificar Testimonio";
    public String WithdrawTestimony = "Retirar Testimonio";
    public String EnterForRegistration = "Ingresar para inscripcion";
    public String REGISTER_REENTRY = "Registrar Reingreso";
    public String RecordRegistration = "Registrar Inscripcion";
    public String EnterNewConcept = "Ingresar nuevo concepto";
    public String UpdateConcept = "Modificar Concepto";
    public String DeleteConcept = "Eliminar concepto";
    public String CreateBudgetTemplate = "Crear plantilla de presupuesto";
    public String UpdateBudgetTemplate = "Modificar plantilla de presupuesto";
    public String DeleteBudgetTemplate = "Eliminar plantilla de presupuesto";
    public String ADMINISTRATOR = "Administrador";
    public String FieldDateEntry = "Fecha de ingreso";
    public String FieldDateReentry = "Fecha de reigreso";
    public String FieldDateExit = "Fecha de salida";
    public String FieldDatePayment = "Fecha de pago";
    public String FieldDateRelease = "Fecha de liberacion";
    public String FieldPrepared = "Preparado";
    public String FieldCardNumber = "Nro Carton";
    public String FIELD_OBSERVED = "Observado";
    public String FieldAmount = "Importe";
    public String FieldNotes = "Observaciones";
    public String FieldName = "Nombre";
    public String FieldReleased = "Liberado";
    public String FieldDateDue = "Fecha Vencimiento";
    public String FieldDueDays = "Dias Vencimiento";
}
