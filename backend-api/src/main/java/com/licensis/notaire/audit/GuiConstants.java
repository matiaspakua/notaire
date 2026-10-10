package com.licensis.notaire.audit;

/**
 * Shared constants used for audit operations (subset of GUI constants).
 * This copy lives in `audit` for backend use so frontend GUI can keep using
 * `com.licensis.notaire.gui.GuiConstants`.
 */
public interface GuiConstants
{
    public int ERROR = -1;
    public int NoRecord = 0;
    public String UpdatePerson = "Modificar Person";
    public String UpdateClient = "Modificar Cliente";
    public String CreateClient = "Dar Alta Cliente";
    public String SearchClient = "Search Client";
    public String CreatePerson = "Dar Alta Person";
    public String SearchPerson = "Search Person";
    public String SearchPersonClient = "Personas - Clientes";
    public String Persons = "PERSONAS";
    public String Client = "Cliente";
    public String Person = "Person";
    public String WindowPerson = "Ventana Person";
    public String WindowClient = "Ventana Cliente";
    public String WindowPersonClients = "Ventana Person - Cliente";
    public String CreateUser = "Dar Alta UserAccount";
    public String UpdateUser = "Modificar UserAccount";
    public String CreateNotary = "Create Notary";
    public String UpdateNotary = "Update Notary";
    public String RegisterSubstitution = "Registrar Substitution";
    public String EnterNewTypeFolio = "Enter new folio type";
    public String UpdateTypeFolio = "Modificar tipo de folio";
    public String DeleteTypeFolio = "Delete folio type";
    public String ENTER_NEW_FOLIOS = "Ingresar nuevos folios";
    public String UpdateFolio = "Modificar Folio";
    public String FolioProtocolMain = "Principal";
    public String FolioProtocolAuxiliary = "Auxiliar";
    public String FolioProtocolSpecial = "Especial";
    public String EnterNewProcedureType = "Enter new procedure type";
    public String EnterNewProcedureTemplate = "Enter New Procedure template";
    public String UpdateProcedureType = "Modifica procedure type";
    public String UpdateProcedureTemplate = "Update procedure template";
    public String DeleteProcedureType = "Delete procedure type";
    public String DeleteProcedureTemplate = "Delete procedure template";
    public String StartManagement = "Start Management";
    public String UpdateManagement = "Update management";
    public String UpdateManagementAddClient = "Update Management Add Client";
    public String UpdateManagementRemoveClient = "Update Management Quitar Client";
    public String DetailManagement = "Detalle Management";
    public String VIEW_MANAGEMENTS = "Ver Managements Clients";
    public String ViewHistoryManagement = "Ver ManagementHistory Management";
    public String EnterStatusManagement = "Enter Status Management";
    public String UpdateStatusManagement = "Update Status Management";
    public String ArchivingManagement = "Archivar Management";
    public String CreateBudget = "Create Budget";
    public String UpdateBudget = "Update Budget";
    public String ModuleBudget = "Budgets";
    public String SearchBudget = "Search Budget";
    public String RegisterPayment = "Registrar Payment";
    public String QueryPayments = "Consultar Pagos";
    public String PrepareDeed = "Preparar Deed";
    public String UpdateDeed = "Modificar Deed";
    public String SearchDeed = "Search Deed";
    public String EnterNewTypeDocument = "Enter new document type";
    public String UpdateTypeDocument = "Modificar tipo de documento";
    public String DeleteTypeDocument = "Delete document type";
    public String DocumentationEntry = "Registrar Documentos";
    public String DocumentationDebt = "Documentation With Debt";
    public String DOCUMENTATION_EXTERNAL_ENTITY = "Entidad Externa";
    public String DOCUMENTATION_REENTRY = "Reenter Documentation";
    public String GenerateTestimony = "Generar Testimony";
    public String VerifyTestimony = "Verificar Testimony";
    public String WithdrawTestimony = "Retirar Testimony";
    public String EnterForRegistration = "Enter for registration";
    public String REGISTER_REENTRY = "Record Reentry";
    public String RecordRegistration = "Record Registration";
    public String EnterNewConcept = "Enter new concept";
    public String UpdateConcept = "Modificar Concept";
    public String DeleteConcept = "Delete concept";
    public String CreateBudgetTemplate = "Create budget template";
    public String UpdateBudgetTemplate = "Update budget template";
    public String DeleteBudgetTemplate = "Delete budget template";
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
    public String FieldName = "Name";
    public String FieldReleased = "Liberado";
    public String FieldDateDue = "Date Expiration";
    public String FieldDueDays = "Days Expiration";
}
