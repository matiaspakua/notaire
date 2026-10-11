// ──────────────────────────────────────────────
// Core domain types — mirrors backend JPA entities / DTOs (all English, per #977)
// ──────────────────────────────────────────────

/** Generated from OpenAPI (#1260) — see `api.ts` / `api.generated.ts`. */
import type {
  SubmittedDocument,
  SubmittedDocumentRequest,
  SubmittedDocumentUpdateRequest,
  DtoOutstandingBalance,
  DeedManagement,
  Budget,
  BudgetSummary,
} from "@/types/api";

export type {
  SubmittedDocument,
  SubmittedDocumentRequest,
  SubmittedDocumentUpdateRequest,
  DtoOutstandingBalance,
  DeedManagement,
  Budget,
  BudgetSummary,
};

/** POST /users/login response — hand-built Map in UserController#login. */
export interface DtoLoginUser {
  idUser?: number;
  name: string;
  type?: string;
  valid?: boolean;
  status?: boolean;
  version?: number;
  person?: { idPerson?: number; firstName?: string; lastName?: string };
  /** JWT issued by POST /users/login; absent outside the login response. */
  token?: string;
}

/** GET /api/v1/people — raw Person entity. */
export interface Person {
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
  /** PersonResponse/PersonRequest identification type id (GET /identification-types); null on legacy rows. */
  identificationTypeId?: number | null;
  version?: number;
}

/** GET /api/v1/identification-types — IdentificationTypeResponse. */
export interface IdentificationType {
  idIdentificationType: number;
  name: string;
  characters?: string | null;
  version?: number;
}

/**
 * Nested person shape used by other entities' DTOs (DtoBudget.person, DtoFolio.personNotary, etc).
 * Field is `personId`, not `idPerson` — Person.java's getter/setter break the `id<Entity>` naming
 * convention used everywhere else (see #1006); this mirrors the actual wire format, not the convention.
 */
export interface DtoPerson {
  personId?: number;
  name?: string;
  lastName?: string;
}

export interface Role {
  idRole?: number;
  name?: string;
  description?: string;
  active?: boolean;
  modulos?: string[];
}

export interface UserAccount {
  idUser?: number;
  name?: string;
  password?: string;
  type?: string;
  active?: boolean;
  person?: Person;
  role?: { idRole: number; name: string } | null;
}

/** GET /api/v1/procedure-types — raw ProcedureType entity. */
export interface ProcedureType {
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

/** GET /api/v1/document-types — raw DocumentType entity. */
export interface DocumentType {
  idDocumentType?: number;
  name?: string;
  expires?: boolean;
  dueDays?: number | null;
  deliveredBy?: string;
  enabled?: boolean;
  returned?: boolean;
}

export interface ProcedureTemplate {
  notes?: string;
  typeDeProcedure?: { idProcedureType?: number; name?: string };
  typeDeDocument?: DocumentType;
}

/** GET /api/v1/folio-types — raw FolioType entity. */
export interface FolioType {
  idFolioType?: number;
  name?: string;
  notes?: string;
  enabled?: boolean;
  isAuxiliary?: boolean;
}

/** GET /api/v1/management-statuses — raw ManagementStatus entity. */
export interface ManagementStatus {
  idManagementStatus?: number;
  name?: string;
  notes?: string;
  version?: number;
}

/** GET /api/v1/concepts — raw Concept entity. */
export interface Concept {
  idConcept?: number;
  name?: string;
  value?: number;
  percentage?: number;
  fixed?: boolean;
  enabled?: boolean;
  version?: number;
}

/** GET /api/v1/folios — raw Folio entity. */
export interface Folio {
  idFolio?: number;
  number?: number;
  year?: number;
  status?: string;
  notes?: string;
  fkIdFolioType?: FolioType;
  fkIdNotaryPerson?: { personId?: number; notaryRegistrationNumber?: number };
  fkIdDeed?: { idDeed?: number; number?: number };
  version?: number;
}

export interface Notebook {
  idNotebook?: number;
  number?: number;
  year?: number;
  notes?: string;
  fkIdNotaryPerson?: { personId?: number; notaryRegistrationNumber?: number };
  version?: number;
}

export interface Procedure {
  idProcedure?: number;
  procedureType?: ProcedureType;
  people?: Person[];
  submittedDocuments?: SubmittedDocument[];
}

/** GET /api/v1/procedure-folders — raw ProcedureFolder DTO. */
export interface ProcedureFolder {
  idFolder?: number;
  number?: number;
  status?: string;
  waitReason?: string;
  idManagement?: number;
  idProcedure?: number;
}

/** GET /managements/{id}/history — DtoHistorySummary. */
export interface ManagementHistory {
  idHistory?: number;
  date?: string;
  notes?: string;
  managementId?: number;
  statusManagementId?: number;
  statusManagementName?: string;
}

export interface DtoArchivedManagement {
  idManagement: number;
  pendingBalance: number;
  pendingDebtAtArchiving: boolean;
}

export interface CreateCompleteManagementInput {
  number: number;
  heading?: string;
  notes?: string;
  budgetId: number;
  notaryId: number;
  statusManagementId: number;
  procedureTypeId: number;
  propertyId?: number;
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

export interface BudgetTemplateKey {
  fkIdProcedureType: number;
  fkIdConcept: number;
}

/** GET/POST /api/v1/budget-templates — raw BudgetTemplate entity. */
export interface BudgetTemplate {
  budgetTemplatePK?: BudgetTemplateKey;
  procedureType?: ProcedureType;
  concept?: Concept;
  notes?: string;
  version?: number;
}

export interface DocumentCostTemplatePK {
  fkIdProcedureType: number;
  fkIdDocumentType: number;
}

/** GET /api/v1/document-cost-templates — raw DocumentCostTemplate entity;
 * POST body is the flat CreateCostRequest shape. */
export interface DocumentCostTemplate {
  documentCostTemplatePK?: DocumentCostTemplatePK;
  procedureType?: ProcedureType;
  documentType?: DocumentType;
  fixedAmount?: number;
  variablePercentage?: number;
  version?: number;
}

/** GET /api/v1/deeds — raw Deed entity. */
export interface Deed {
  idDeed?: number;
  number?: number;
  dateDeedrecording?: string;
  body?: string;
  status?: string;
  registrationEntryNumber?: string;
  notes?: string;
  idFolio?: number;
}

export interface TestimonyMovement {
  idTestimonyMovement?: number;
  dateEntry?: string;
  dateExit?: string;
  dateRegistration?: string;
  registered?: boolean;
  cardNumber?: number;
  observedByRegistry?: boolean;
  notes?: string;
  testimony?: { idTestimony?: number };
}

/** CU07/CU08 - Testimony generado a partir de una deed firmada. */
export interface Testimony {
  idTestimony?: number;
  number?: number;
  flagged?: boolean;
  verified?: boolean;
  notes?: string;
  deed?: Deed;
  testimonyMovements?: TestimonyMovement[];
}

/** GET /api/v1/payments — raw Payment entity; POST/PUT body is the flat PaymentRequest shape. */
export interface Payment {
  idPayment?: number;
  idBudget?: number;
  amount?: number;
  date?: string;
  paymentMethod?: string;
  notes?: string;
  fkIdBudget?: { idBudget?: number };
}

/** GET /api/v1/substitutions — raw Substitution entity. */
export interface Substitution {
  idSubstitution?: number;
  fkIdSubstitute?: DtoPerson;
  fkIdSubstituted?: DtoPerson;
  dateStart?: string;
  dateEnd?: string;
  notes?: string;
}

/** GET /api/v1/audit-log — raw AuditRecord entity. */
export interface AuditRecord {
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

/** GET /api/v1/properties — raw Property entity. */
export interface Property {
  idProperty?: number;
  cadastralDesignation?: string;
  fiscalAppraisal?: number;
  address?: string;
  notes?: string;
  registrationNumber?: string;
  volumeFolioLandRecord?: string;
  boundaries?: string;
}

/** GET /api/v1/registration-drafts — raw RegistrationDraft entity. */
export interface RegistrationDraft {
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

/** GET /api/v1/copies — raw Copy entity. */
export interface Copy {
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

/** A single history entry for the workflow trace. */
export interface HistoryEntry {
  idHistory?: number;
  statusManagementId?: number;
  statusManagementName?: string;
  date?: string;
  notes?: string;
}

/** Aggregated response from GET /managements/{id}/workflow-trace — DtoManagementWorkflowTrace. */
export interface ManagementWorkflowTrace {
  managementId: number;
  number?: number;
  heading?: string;
  dateStart?: string;
  statusActual?: string;
  workflowDefinition?: WorkflowDefinition;
  nodes: WorkflowNode[];
  transitions: WorkflowTransition[];
  history: HistoryEntry[];
  /** nodeId → "completed" | "in_progress" | "pending" */
  nodeStatuses: Record<number, string>;
  /** Secondary timeline for the unbounded reingreso loop (issue #841). */
  testimonyMovements?: TestimonyMovementEntry[];
}

/** Lightweight testimony movement on the workflow trace (reingreso loop). */
export interface TestimonyMovementEntry {
  dateEntry?: string;
  dateExit?: string;
  dateRegistration?: string;
  /** Derived: dateExit != null && !registered */
  returnedObserved?: boolean;
}

/** CU10 - a single "Entity Externa" document tracked within a gestión — DtoExternalEntityDocument. */
export interface ExternalEntityDocument {
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

/** CU10 - GET /managements/{id}/external-entity-documents response — DtoManagementExternalEntityDocuments. */
export interface ManagementExternalEntityDocuments {
  idManagement: number;
  number?: number;
  heading?: string;
  dateStart?: string;
  notary?: string;
  cadastralDesignation?: string;
  documents: ExternalEntityDocument[];
}

/** CU10 - PUT .../external-entity-documents/{idSubmittedDocument} request body. */
export interface ExternalEntityDocumentMovementInput {
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

/** CU43 - Tipo de document requerido por la ProcedureTemplate de un trámite — DtoRequiredDocument. */
export interface RequiredDocument {
  idDocumentType: number;
  name?: string;
  expires: boolean;
  dueDays?: number;
  deliveredBy?: string;
}

/** CU43 - Un trámite de la gestión junto con su documentación necesaria — DtoProcedureRequiredDocumentation. */
export interface ProcedureRequiredDocumentation {
  idProcedure: number;
  typeProcedureName?: string;
  documentsNecesarios: RequiredDocument[];
}

/** CU43 - GET /managements/{id}/document-reentry response — DtoManagementDocumentReentry. */
export interface ManagementDocumentReentry {
  idManagement: number;
  number?: number;
  heading?: string;
  procedures: ProcedureRequiredDocumentation[];
}

/** CU43 - POST /managements/{id}/document-reentry request body — DtoDocumentReentryRequest. */
export interface DocumentReentryInput {
  idProcedure: number;
  idDocumentType: number;
}

/** CU43 - POST /managements/{id}/document-reentry response — DtoDocumentReentered. */
export interface ReenteredDocument {
  idSubmittedDocument: number;
  idProcedure: number;
  idDocumentType: number;
  name?: string;
  expires: boolean;
  dueDays?: number;
  deliveredBy?: string;
  reentered: boolean;
}

/** CU42 - GET /submitted-documents/upcoming-expirations row — DtoUpcomingExpiration. */
export interface UpcomingExpiration {
  idSubmittedDocument: number;
  documentName?: string;
  managementNumber?: number;
  managementHeading?: string;
  prepared: boolean;
  dateEntry?: string;
  dateExit?: string;
  cardNumber?: number;
  observed: boolean;
  amountToPay?: number;
  datePayment?: string;
  dateReleased?: string;
  notes?: string;
  dateDue: string;
  daysRemaining: number;
}

/** #774 - GET /managements/{id}/case-summary — DtoManagementCaseSummary. */
export type TestimonyCaseStatus = "SIN_INGRESAR" | "INGRESADO" | "INSCRIPTO" | "RETIRADO";

export interface CaseTestimony {
  idTestimony: number;
  number: number;
  verified: boolean;
  flagged: boolean;
  state: TestimonyCaseStatus;
  copies: number;
}

export interface CaseDeed {
  idDeed: number;
  number: number;
  status?: string;
  testimonies: CaseTestimony[];
}

export interface CaseDocument {
  idSubmittedDocument: number;
  name?: string;
  typeName?: string;
  idProcedure: number;
  prepared: boolean;
  released: boolean;
  observed: boolean;
  delivered: boolean;
  reentered: boolean;
  dateDue?: string;
}

export interface ManagementCaseSummary {
  managementId: number;
  managementNumber: number;
  heading?: string;
  deeds: CaseDeed[];
  documents: CaseDocument[];
}

/** CU47/CU02 - GET /managements/{id}/financial-summary — DtoManagementFinancialSummary. */
export interface ManagementFinancialSummary {
  idManagement: number;
  totalPresupuestado: number;
  totalCobrado: number;
  pendingBalance: number;
}
