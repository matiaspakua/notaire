// ──────────────────────────────────────────────
// Core domain types — mirrors backend JPA entities / DTOs (all English, per #977)
// ──────────────────────────────────────────────

/** POST /usuarios/login response — hand-built Map in UserController#login. */
export interface DtoUsuario {
  idUsuario?: number;
  nombre: string;
  tipo?: string;
  valido?: boolean;
  estado?: boolean;
  version?: number;
  personas?: { idPersona?: number; nombre?: string; apellido?: string };
  /** JWT issued by POST /usuarios/login; absent outside the login response. */
  token?: string;
}

/** GET /api/v1/people — raw Person entity. */
export interface Persona {
  personId?: number;
  firstName?: string;
  lastName?: string;
  nationality?: string;
  identificationNumber?: string;
  taxId?: string;
  sex?: string;
  birthDate?: string;
  maritalStatus?: string;
  marriageCount?: number;
  occupation?: string;
  address?: string;
  phone?: string;
  email?: string;
  notaryRegistrationNumber?: number;
  isClient?: boolean;
  fkIdIdentificationType?: { idIdentificationType?: number; name?: string; characters?: string };
  version?: number;
}

/** Nested person shape used by other entities' DTOs (DtoBudget.person, DtoFolio.personNotary, etc). */
export interface DtoPerson {
  idPerson?: number;
  name?: string;
  lastName?: string;
}

export interface Rol {
  idRole?: number;
  name?: string;
  description?: string;
  active?: boolean;
  modulos?: string[];
}

export interface Usuario {
  idUser?: number;
  name?: string;
  password?: string;
  type?: string;
  active?: boolean;
  person?: Persona;
  role?: { idRole: number; name: string } | null;
}

/** GET /api/v1/tipo-tramite — raw ProcedureType entity. */
export interface TipoDeTramite {
  idProcedureType?: number;
  name?: string;
  notes?: string;
  isArchived?: boolean;
  isRegistered?: boolean;
  associatesProperties?: boolean;
  enabled?: boolean;
  workflowDefinitionId?: number | null;
  workflowDefinitionName?: string | null;
}

/** GET /api/v1/tipo-de-documento — raw DocumentType entity. */
export interface TipoDeDocumento {
  idDocumentType?: number;
  name?: string;
  expires?: boolean;
  dueDays?: number | null;
  deliveredBy?: string;
  enabled?: boolean;
}

export interface PlantillaTramite {
  notes?: string;
  tipoDeTramite?: { idProcedureType?: number; name?: string };
  tipoDeDocumento?: TipoDeDocumento;
}

/** GET /api/v1/tipo-folio — raw FolioType entity. */
export interface TipoDeFolio {
  idFolioType?: number;
  name?: string;
  notes?: string;
  enabled?: boolean;
  isAuxiliary?: boolean;
}

/** GET /api/v1/estado-gestion — raw ManagementStatus entity. */
export interface EstadoDeGestion {
  idManagementStatus?: number;
  name?: string;
  notes?: string;
  version?: number;
}

/** GET /api/v1/conceptos — raw Concept entity. */
export interface Concepto {
  idConcept?: number;
  name?: string;
  value?: number;
  percentage?: number;
  fixed?: boolean;
  enabled?: boolean;
  version?: number;
}

/** GET /api/v1/folio — raw Folio entity. */
export interface Folio {
  idFolio?: number;
  number?: number;
  year?: number;
  status?: string;
  notes?: string;
  fkIdFolioType?: TipoDeFolio;
  fkIdNotaryPerson?: { idPerson?: number; notaryRegistrationNumber?: number };
  fkIdDeed?: { idDeed?: number; number?: number };
  version?: number;
}

export interface Cuaderno {
  idNotebook?: number;
  number?: number;
  year?: number;
  notes?: string;
  fkIdNotaryPerson?: { idPerson?: number; notaryRegistrationNumber?: number };
  version?: number;
}

export interface Tramite {
  idProcedure?: number;
  procedureType?: TipoDeTramite;
  listaPersons?: Persona[];
  documentosPresentados?: DocumentoPresentado[];
}

export interface DocumentoPresentado {
  idSubmittedDocument?: number;
  fkDocumentType?: TipoDeDocumento;
  delivered?: boolean;
  dateEntry?: string;
}

export interface DocumentoPresentadoRequest {
  tipoId: number | null;
  fecha: string | null;
  entregado: boolean;
}

/** GET /api/v1/carpetas — raw ProcedureFolder DTO. */
export interface CarpetaTramite {
  idFolder?: number;
  number?: number;
  status?: string;
  waitReason?: string;
  idManagement?: number;
  idProcedure?: number;
}

/** GET /gestiones/{id}/historial — DtoHistorySummary. */
export interface Historial {
  idHistory?: number;
  date?: string;
  notes?: string;
  managementId?: number;
  statusManagementId?: number;
  statusManagementName?: string;
}

/** GET /gestiones — DtoManagementSummary. */
export interface GestionDeEscritura {
  idManagement?: number;
  number?: number;
  encabezado?: string;
  dateStart?: string;
  statusActual?: string;
  procedureCount?: number;
  notes?: string;
}

export interface DtoSaldoPendiente {
  pendingBalance: number;
}

export interface DtoGestionArchivada {
  idManagement: number;
  pendingBalance: number;
  pendingDebtAtArchiving: boolean;
}

export interface CreateCompleteGestionInput {
  number: number;
  encabezado?: string;
  notes?: string;
  presupuestoId: number;
  escribanoId: number;
  estadoGestionId: number;
  tipoTramiteId: number;
  inmuebleId?: number;
}

export type TipoItem = "NORMAL" | "DESCUENTO" | "RECARGO";

/** GET /api/v1/items — raw Item entity. */
export interface Item {
  idItem?: number;
  name?: string;
  value?: number;
  percentage?: number;
  notes?: string;
  type?: TipoItem;
  reason?: string;
  fixedConcept?: boolean;
  fkIdBudget?: { idBudget?: number };
}

export interface PlantillaPresupuestoPK {
  fkIdProcedureType: number;
  fkIdConcept: number;
}

/** GET/POST /api/v1/plantilla-presupuestos — raw BudgetTemplate entity. */
export interface PlantillaPresupuesto {
  budgetTemplatePK?: PlantillaPresupuestoPK;
  procedureType?: TipoDeTramite;
  concept?: Concepto;
  notes?: string;
  version?: number;
}

export interface PlantillaCostoDocumentoPK {
  fkIdProcedureType: number;
  fkIdDocumentType: number;
}

/** GET /api/v1/plantilla-costos-documento — raw DocumentCostTemplate entity;
 * POST body is the flat CreateCostRequest shape. */
export interface PlantillaCostoDocumento {
  documentCostTemplatePK?: PlantillaCostoDocumentoPK;
  procedureType?: TipoDeTramite;
  documentType?: TipoDeDocumento;
  fixedAmount?: number;
  variablePercentage?: number;
  version?: number;
}

/** GET /api/v1/presupuestos — raw Budget entity. */
export interface Presupuesto {
  idBudget?: number;
  number?: number;
  date?: string;
  encabezado?: string;
  status?: string;
  propertyAmount?: number;
  notes?: string;
  person?: DtoPerson;
  itemList?: Item[];
  version?: number;
}

/** GET /api/v1/escrituras — raw Deed entity. */
export interface Escritura {
  idDeed?: number;
  number?: number;
  dateDeedrecording?: string;
  body?: string;
  status?: string;
  registrationEntryNumber?: string;
  notes?: string;
  idFolio?: number;
}

export interface MovimientoTestimonio {
  idTestimonyMovement?: number;
  dateEntry?: string;
  dateExit?: string;
  dateRegistration?: string;
  registered?: boolean;
  cardNumber?: number;
  notes?: string;
  testimony?: { idTestimony?: number };
}

/** CU07/CU08 - Testimonio generado a partir de una escritura firmada. */
export interface Testimonio {
  idTestimony?: number;
  number?: number;
  flagged?: boolean;
  verified?: boolean;
  notes?: string;
  deed?: Escritura;
  movimientosTestimonios?: MovimientoTestimonio[];
}

/** GET /api/v1/pagos — raw Payment entity; POST/PUT body is the flat PaymentRequest shape. */
export interface Pago {
  idPayment?: number;
  idBudget?: number;
  amount?: number;
  date?: string;
  paymentMethod?: string;
  notes?: string;
  fkIdBudget?: { idBudget?: number };
}

/** CU47 - GET /presupuestos/{id}/resumen response — DtoBudgetResumen. */
export interface PresupuestoResumen {
  idBudget: number;
  numberBudget: number;
  idManagement?: number;
  numberManagement?: number;
  encabezadoManagement?: string;
  total: number;
  pendingBalance: number;
  payments: Pago[];
}

/** GET /api/v1/suplencia — raw Substitution entity. */
export interface Suplencia {
  idSubstitution?: number;
  fkIdSubstitute?: DtoPerson;
  fkIdSubstituted?: DtoPerson;
  dateStart?: string;
  dateEnd?: string;
  notes?: string;
}

/** GET /api/v1/audit-log — raw AuditRecord entity. */
export interface RegistroAuditoria {
  idAuditRecord?: number;
  users?: { name?: string };
  operationDetail?: string;
  date?: string;
  module?: string;
}

// ──────────────────────────────────────────────
// Workflow types (CU70, CU71, CU72, CU73)
// ──────────────────────────────────────────────
export type WorkflowNodeType = "INITIAL" | "INTERMEDIATE" | "FINAL";

export interface WorkflowDefinition {
  id?: number;
  name?: string;
  description?: string;
  active?: boolean;
  version?: number;
}

export interface WorkflowNode {
  id?: number;
  workflowDefinitionId?: number;
  statusManagementId?: number;
  statusManagementName?: string;
  type?: WorkflowNodeType;
  positionX?: number;
  positionY?: number;
  version?: number;
}

export interface WorkflowTransition {
  id?: number;
  workflowDefinitionId?: number;
  originNodeId?: number;
  destinationNodeId?: number;
  condition?: string;
  description?: string;
  version?: number;
}

// ──────────────────────────────────────────────
// UI / Navigation types
// ──────────────────────────────────────────────

/** GET /api/v1/inmueble — raw Property entity. */
export interface Inmueble {
  idProperty?: number;
  cadastralDesignation?: string;
  fiscalAppraisal?: number;
  address?: string;
  notes?: string;
  registrationNumber?: string;
  volumeFolioLandRecord?: string;
  boundaries?: string;
}

/** GET /api/v1/minutas-inscripcion — raw RegistrationDraft entity. */
export interface MinutaInscripcion {
  idRegistrationDraft?: number;
  number?: number;
  status?: string;
  dateGeneration?: string;
  dateSubmission?: string;
  registryEntryNumber?: string;
  dateReception?: string;
  finalRegistrationNumber?: string;
  registryNotes?: string;
  dateCorrection?: string;
  idDeed?: number;
}

/** GET /api/v1/copia — raw Copy entity. */
export interface Copia {
  idCopy?: number;
  number?: number;
  datePrinting?: string;
  dateWithdrawal?: string;
  notes?: string;
  fkIdTestimony?: { idTestimony?: number; number?: number };
  fkIdPerson?: DtoPerson;
}

export interface NavItem {
  label: string;
  href: string;
  icon?: string;
  adminOnly?: boolean;
}

// ──────────────────────────────────────────────
// Workflow Trace (dashboard)
// ──────────────────────────────────────────────

/** A single historial entry for the workflow trace. */
export interface HistorialEntry {
  idHistory?: number;
  statusManagementId?: number;
  statusManagementName?: string;
  date?: string;
  notes?: string;
}

/** Aggregated response from GET /gestiones/{id}/workflow-trace — DtoManagementWorkflowTrace. */
export interface GestionWorkflowTrace {
  managementId: number;
  number?: number;
  encabezado?: string;
  dateStart?: string;
  statusActual?: string;
  workflowDefinition?: WorkflowDefinition;
  nodes: WorkflowNode[];
  transitions: WorkflowTransition[];
  history: HistorialEntry[];
  /** nodeId → "completed" | "in_progress" | "pending" */
  nodeStatuses: Record<number, string>;
}

/** CU10 - a single "Entidad Externa" document tracked within a gestión — DtoDocumentEntidadExterna. */
export interface DocumentoEntidadExterna {
  idSubmittedDocument: number;
  name?: string;
  prepared?: boolean;
  cardNumber?: number;
  dateEntry?: string;
  dateExit?: string;
  flagged?: boolean;
  amountToPay?: number;
  datePayment?: string;
  dateReleased?: string;
  notes?: string;
  delivered?: boolean;
}

/** CU10 - GET /gestiones/{id}/documentos-entidades-externas response — DtoManagementDocumentsEntidadesExternas. */
export interface GestionDocumentosEntidadesExternas {
  idManagement: number;
  number?: number;
  encabezado?: string;
  dateStart?: string;
  notary?: string;
  cadastralDesignation?: string;
  documents: DocumentoEntidadExterna[];
}

/** CU10 - PUT .../documentos-entidades-externas/{idSubmittedDocument} request body. */
export interface MovimientoDocumentoEntidadExternaInput {
  prepared?: boolean;
  cardNumber?: number;
  dateEntry?: string;
  dateExit?: string;
  flagged?: boolean;
  amountToPay?: number;
  datePayment?: string;
  dateReleased?: string;
  notes?: string;
  delivered?: boolean;
}

/** CU43 - Tipo de documento requerido por la PlantillaTramite de un trámite — DtoDocumentNecesario. */
export interface DocumentoNecesario {
  idDocumentType: number;
  name?: string;
  expires: boolean;
  dueDays?: number;
  deliveredBy?: string;
}

/** CU43 - Un trámite de la gestión junto con su documentación necesaria — DtoProcedureDocumentacionNecesaria. */
export interface TramiteDocumentacionNecesaria {
  idProcedure: number;
  typeProcedureName?: string;
  documentsNecesarios: DocumentoNecesario[];
}

/** CU43 - GET /gestiones/{id}/reingreso-documentacion response — DtoManagementReingresoDocumentacion. */
export interface GestionReingresoDocumentacion {
  idManagement: number;
  number?: number;
  encabezado?: string;
  procedures: TramiteDocumentacionNecesaria[];
}

/** CU43 - POST /gestiones/{id}/reingreso-documentacion request body. */
export interface ReingresoDocumentacionInput {
  idTramite: number;
  idTipoDocumento: number;
}

/** CU43 - POST /gestiones/{id}/reingreso-documentacion response — DtoDocumentReentered. */
export interface DocumentoReingresado {
  idSubmittedDocument: number;
  idProcedure: number;
  idDocumentType: number;
  name?: string;
  expires: boolean;
  dueDays?: number;
  deliveredBy?: string;
  reentered: boolean;
}
