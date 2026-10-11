/**
 * Shared API helpers for Playwright E2E tests
 *
 * These functions wrap page.request calls to the Next.js API proxy
 * (/api/v1/* → http://localhost:8080/api/v1/*) with proper error handling.
 *
 * Synced with Bruno API test conventions:
 *  - Same endpoints as Bruno .bru files
 *  - Same JSON payload structure
 *  - Same environment variable naming
 *
 * Field names mirror the renamed (English) backend DTOs/entities — see #977.
 */

import type { Page, APIResponse } from "@playwright/test";

// ──────────────────────────────────────────────
// Generic API helpers
// ──────────────────────────────────────────────

export interface ApiResult<T = any> {
  ok: boolean;
  status: number;
  data?: T;
  error?: string;
}

function baseURL(): string {
  return process.env.BASE_URL || "http://localhost:3000";
}

/**
 * The backend's security chain requires a JWT Bearer token on every endpoint except login.
 * `page.request` is a separate fetch context from the browser page — it does not read the
 * app's own localStorage-persisted token — so it must be attached explicitly here. global-setup
 * stores the admin token in process.env.E2E_ADMIN_TOKEN, which — since it's set before the
 * worker processes are spawned — is visible to every test file's process.env at call time.
 */
function authHeaders(extra: Record<string, string> = {}): Record<string, string> {
  const token = process.env.E2E_ADMIN_TOKEN;
  return token ? { ...extra, Authorization: `Bearer ${token}` } : extra;
}

export async function apiGet<T = any>(page: Page, path: string): Promise<ApiResult<T>> {
  const response = await page.request.get(`${baseURL()}/api/v1${path}`, {
    headers: authHeaders(),
  });
  return parseResponse<T>(response);
}

export async function apiPost<T = any>(
  page: Page,
  path: string,
  body: unknown,
): Promise<ApiResult<T>> {
  const response = await page.request.post(`${baseURL()}/api/v1${path}`, {
    data: JSON.stringify(body),
    headers: authHeaders({ "Content-Type": "application/json" }),
  });
  return parseResponse<T>(response);
}

export async function apiPut<T = any>(
  page: Page,
  path: string,
  body: unknown,
): Promise<ApiResult<T>> {
  const response = await page.request.put(`${baseURL()}/api/v1${path}`, {
    data: JSON.stringify(body),
    headers: authHeaders({ "Content-Type": "application/json" }),
  });
  return parseResponse<T>(response);
}

export async function apiPatch<T = any>(
  page: Page,
  path: string,
  body: unknown,
): Promise<ApiResult<T>> {
  const response = await page.request.patch(`${baseURL()}/api/v1${path}`, {
    data: JSON.stringify(body),
    headers: authHeaders({ "Content-Type": "application/json" }),
  });
  return parseResponse<T>(response);
}

export async function apiDelete<T = any>(page: Page, path: string): Promise<ApiResult<T>> {
  const response = await page.request.delete(`${baseURL()}/api/v1${path}`, {
    headers: authHeaders(),
  });
  return parseResponse<T>(response);
}

async function parseResponse<T>(response: APIResponse): Promise<ApiResult<T>> {
  const ok = response.ok();
  let data: T | undefined;
  let error: string | undefined;
  try {
    const text = await response.text();
    if (text) {
      data = JSON.parse(text) as T;
    }
  } catch {
    error = response.ok() ? undefined : `HTTP ${response.status()}: ${response.statusText()}`;
  }
  return { ok, status: response.status(), data, error };
}

// ──────────────────────────────────────────────
// Domain-specific helper types (mirroring backend DTOs)
// ──────────────────────────────────────────────

export interface PersonPayload {
  firstName: string;
  lastName: string;
  identificationNumber: string;
  email?: string;
  phone?: string;
  isClient?: boolean;
  nationality?: string;
  birthDate?: string;
  taxId?: string;
  maritalStatus?: string;
  sex?: string;
  address?: string;
  occupation?: string;
  notaryRegistrationNumber?: number;
}

export interface BudgetPayload {
  person?: { personId: number };
  date?: string;
  heading?: string;
  status?: string;
  notes?: string;
  propertyAmount?: number;
}

export interface CompleteCaseManagementPayload {
  number: number;
  heading?: string;
  notes?: string;
  budgetId: number;
  notaryId: number;
  statusManagementId: number;
  procedureTypeId: number;
  propertyId?: number;
}

export interface DeedPayload {
  dateDeedrecording?: string;
  body?: string;
  status?: string;
  idFolio?: number;
  notes?: string;
}

export interface UserPayload {
  name: string;
  password: string;
  type: string;
  active?: boolean;
}

export interface PaymentPayload {
  idBudget?: number;
  amount: number;
  date?: string;
  paymentMethod?: string;
  notes?: string;
}

export interface TestimonyPayload {
  idDeed: number;
  notes?: string;
}

export interface SubmittedDocumentPayload {
  name: string;
  typeId?: number;
  procedureId?: number;
  deliveredBy?: string;
  delivered?: boolean;
}

export interface SubstitutionPayload {
  fkIdSubstitute: { personId: number };
  fkIdSubstituted: { personId: number };
  dateStart: string;
  dateEnd?: string;
  notes?: string;
}

// ──────────────────────────────────────────────
// Domain-specific helper functions
// ──────────────────────────────────────────────

/**
 * Seed test helpers — create entities with unique test IDs
 */

// Each Playwright worker draws from its own residue class modulo ID_STRIDE
// (global setup, outside any worker, takes slot 0), so workers started a few
// milliseconds apart can never hand out the same id. Kept below Java's 32-bit
// `int` max (2,147,483,647) so IDs fed into `int`-typed entity fields
// (e.g. Deed.number) don't overflow and fail JSON deserialization.
const ID_STRIDE = 16;
const workerSlot = (Number(process.env.TEST_PARALLEL_INDEX ?? -1) + 1) % ID_STRIDE;
let _testCounter = (Date.now() % 100_000_000) * ID_STRIDE + workerSlot;

/** Generate a unique test identifier */
export function uniqueId(): number {
  _testCounter += ID_STRIDE;
  return _testCounter;
}

/** Generate a unique string suitable for test names/IDs */
export function uniqueLabel(prefix: string): string {
  return `${prefix}-${uniqueId()}`;
}

/**
 * Person helpers — PersonController accepts the raw Person entity at /people.
 */
export async function createPerson(
  page: Page,
  overrides: Partial<PersonPayload> = {},
): Promise<ApiResult<{ personId: number; firstName?: string; lastName?: string }>> {
  const id = uniqueId();
  return apiPost(page, "/people", {
    firstName: "Test",
    lastName: `Person-${id}`,
    identificationNumber: `E2E${id}`,
    email: `e2e-${id}@notaire.test`,
    isClient: true,
    nationality: "Argentina",
    birthDate: "1990-01-01",
    maritalStatus: "Soltero",
    sex: "Masculino",
    ...overrides,
  });
}

/**
 * Budget helpers — BudgetController accepts the raw Budget entity.
 */
export async function createBudget(
  page: Page,
  personId: number,
  _conceptId?: number,
  overrides: Partial<BudgetPayload> = {},
): Promise<ApiResult<{ idBudget: number }>> {
  return apiPost(page, "/budgets", {
    person: { personId: personId },
    date: new Date().toISOString().split("T")[0],
    heading: `Budget E2E ${uniqueId()}`,
    status: "PENDIENTE",
    notes: `Budget E2E ${uniqueId()}`,
    ...overrides,
  });
}

/**
 * Management helpers (CU02 - complete-case: management and procedure in one call)
 */
export async function createCompleteCaseManagement(
  page: Page,
  overrides: Partial<CompleteCaseManagementPayload> & { budgetId: number },
): Promise<ApiResult<{ idManagement: number; number: number; currentStatus: string }>> {
  const merged: CompleteCaseManagementPayload = {
    // `number` is a Postgres `integer` column; uniqueId() is Date.now()-based and overflows it.
    number: uniqueId() % 1_000_000,
    heading: `Management E2E ${uniqueId()}`,
    notaryId: 1,
    statusManagementId: 1,
    procedureTypeId: 4,
    ...overrides,
  };
  return apiPost(page, "/managements/complete-case", merged);
}

/**
 * CU43 - procedures of a management with their required documentation. `DtoManagementSummary`
 * (the `complete-case`/`GET /managements/{id}` read-model) never exposes procedure IDs, so
 * this is the only way to learn the ID of the procedure `complete-case` created.
 */
export async function getDocumentReentry(
  page: Page,
  idManagement: number,
): Promise<ApiResult<{ idManagement: number; number: number; procedures: Array<{ idProcedure: number }> }>> {
  return apiGet(page, `/managements/${idManagement}/document-reentry`);
}

/**
 * Plain management helper (CU43) — unlike `createCompleteCaseManagement`, this does
 * not create a `Procedure`, so the resulting management has zero procedures.
 * ManagementController accepts the raw DeedManagement entity.
 */
export async function createManagementWithoutProcedure(
  page: Page,
  notaryId: number,
  overrides: { heading?: string; number?: number } = {},
): Promise<ApiResult<{ idManagement: number; number: number }>> {
  return apiPost(page, "/managements", {
    heading: `Management E2E ${uniqueId()}`,
    dateStart: new Date().toISOString().split("T")[0],
    number: uniqueId() % 1_000_000,
    notaryPersonId: notaryId,
    ...overrides,
  });
}

/**
 * Deed helpers — DeedController accepts the raw Deed entity.
 */
export async function createDeed(
  page: Page,
  managementId: number,
  personId: number,
  overrides: Partial<DeedPayload> = {},
): Promise<ApiResult<{ idDeed: number }>> {
  const id = uniqueId();
  return apiPost(page, "/deeds", {
    number: id % 1_000_000,
    dateDeedrecording: new Date().toISOString().split("T")[0],
    body: `E2E deed body ${id}`,
    status: "Pendiente",
    ...overrides,
  });
}

/**
 * UserAccount helpers — UserController's UserRequest is (name, password, type, active).
 */
export async function createUser(
  page: Page,
  personId?: number,
  overrides: Partial<UserPayload> = {},
): Promise<ApiResult<{ idUser: number }>> {
  const id = uniqueId();
  return apiPost(page, "/users", {
    name: `e2euser-${id}`,
    password: "Test1234!",
    type: "EMPLEADO",
    active: true,
    ...overrides,
  });
}

/**
 * Payment helpers — PaymentController's PaymentRequest is (idBudget, amount, date, notes, paymentMethod).
 */
export async function createPayment(
  page: Page,
  budgetId: number,
  overrides: Partial<PaymentPayload> = {},
): Promise<ApiResult<{ idPayment: number }>> {
  return apiPost(page, "/payments", {
    idBudget: budgetId,
    amount: 5000,
    date: new Date().toISOString().split("T")[0],
    paymentMethod: "Efectivo",
    ...overrides,
  });
}

/**
 * Folio helpers — FolioController's FolioRequest is
 * (number, year, status, notes, typeFolioId, notaryId, deedId).
 */
export async function createFolio(
  page: Page,
  personId?: number,
  overrides: {
    number?: number;
    year?: number;
    status?: string;
    typeFolioId?: number;
    notaryId?: number;
    deedId?: number;
  } = {},
): Promise<ApiResult<{ idFolio: number }>> {
  return apiPost(page, "/folios", {
    number: Math.floor(10000 + Math.random() * 90000),
    year: 2026,
    status: "Nuevo",
    typeFolioId: 1,
    notaryId: personId || 1,
    ...overrides,
  });
}

export async function createFolioType(
  page: Page,
  overrides: { name?: string; isAuxiliary?: boolean; enabled?: boolean } = {},
): Promise<ApiResult<{ idFolioType: number }>> {
  const id = uniqueId();
  return apiPost(page, "/folio-types", {
    name: `Tipo Folio E2E ${id}`,
    enabled: true,
    isAuxiliary: false,
    ...overrides,
  });
}

/**
 * Catalog helpers
 */
export async function createProcedureType(
  page: Page,
  overrides: { name?: string; notes?: string } = {},
): Promise<ApiResult<{ idProcedureType: number }>> {
  const id = uniqueId();
  return apiPost(page, "/procedure-types", {
    name: `Procedure type E2E ${id}`,
    notes: "Created by E2E test",
    isArchived: false,
    isRegistered: false,
    ...overrides,
  });
}

/**
 * Procedure and submitted-document helpers (CU10) — ProcedureController takes
 * a plain-id ProcedureRequest (issue #981), not the raw Procedure entity.
 */
export async function createProcedure(
  page: Page,
  managementId: number,
  procedureTypeId: number,
): Promise<ApiResult<{ idProcedure: number }>> {
  return apiPost(page, "/procedures", {
    idProcedureType: procedureTypeId,
    idManagement: managementId,
  });
}

/**
 * SubmittedDocumentController's create request is
 * (typeId, date, delivered, procedureId, deliveredBy, name); typeId and
 * procedureId are required (#655). Without an explicit typeId a
 * non-expiring document type is created first.
 */
export async function createExternalEntityDocument(
  page: Page,
  procedureId: number,
  overrides: { name?: string; deliveredBy?: string; typeId?: number } = {},
): Promise<ApiResult<{ idSubmittedDocument: number }>> {
  let typeId = overrides.typeId;
  if (typeId === undefined) {
    const documentType = await createDocumentType(page, { expires: false });
    if (!documentType.ok) {
      return { ok: false, status: documentType.status, error: documentType.error ?? "createDocumentType failed" };
    }
    typeId = documentType.data!.idDocumentType;
  }
  return apiPost(page, "/submitted-documents", {
    typeId,
    procedureId: procedureId,
    deliveredBy: "Entidad Externa",
    delivered: false,
    ...overrides,
  });
}

/**
 * Workflow helpers (CU83) — used to seed a self-contained workflow
 * (definition + nodes + transition) for management transition E2E tests,
 * independent of demo/seed data.
 */
export async function createWorkflowDefinition(
  page: Page,
  overrides: { name?: string; description?: string; active?: boolean } = {},
): Promise<ApiResult<{ id: number }>> {
  const id = uniqueId();
  return apiPost(page, "/workflow-definition", {
    name: `Workflow E2E ${id}`,
    description: "Created by E2E test",
    active: true,
    ...overrides,
  });
}

export async function createWorkflowNode(
  page: Page,
  workflowDefinitionId: number,
  managementStatusId: number,
  type: "INITIAL" | "INTERMEDIATE" | "FINAL",
): Promise<ApiResult<{ id: number }>> {
  return apiPost(page, "/workflow-node", {
    workflowDefinitionId,
    statusManagementId: managementStatusId,
    type,
    positionX: 0,
    positionY: 0,
  });
}

export async function createWorkflowTransition(
  page: Page,
  workflowDefinitionId: number,
  originNodeId: number,
  destinationNodeId: number,
): Promise<ApiResult<{ id: number }>> {
  return apiPost(page, "/workflow-transition", {
    workflowDefinitionId,
    originNodeId,
    destinationNodeId,
  });
}

export async function assignWorkflowToProcedureType(
  page: Page,
  procedureTypeId: number,
  workflowDefinitionId: number,
): Promise<ApiResult<unknown>> {
  return apiPut(page, `/procedure-types/${procedureTypeId}/workflow`, { workflowDefinitionId });
}

/**
 * Seeds a self-contained two-node workflow (INITIAL -> FINAL) with a single
 * valid transition, assigns it to a fresh procedure type, and creates a
 * management whose initial status matches the workflow's INITIAL node — ready
 * for a status change to the FINAL node's status.
 */
export async function seedManagementWithWorkflow(
  page: Page,
  budgetId: number,
): Promise<{
  idManagement: number;
  number: number;
  initialStatus: string;
  finalStatus: string;
}> {
  const initialStatus = await createManagementStatus(page);
  const finalStatus = await createManagementStatus(page);
  const workflow = await createWorkflowDefinition(page);
  const workflowId = workflow.data!.id;

  const initialNode = await createWorkflowNode(
    page,
    workflowId,
    initialStatus.data!.idManagementStatus,
    "INITIAL",
  );
  const finalNode = await createWorkflowNode(
    page,
    workflowId,
    finalStatus.data!.idManagementStatus,
    "FINAL",
  );
  await createWorkflowTransition(page, workflowId, initialNode.data!.id, finalNode.data!.id);

  const procedureType = await createProcedureType(page);
  await assignWorkflowToProcedureType(page, procedureType.data!.idProcedureType, workflowId);

  const management = await createCompleteCaseManagement(page, {
    budgetId,
    procedureTypeId: procedureType.data!.idProcedureType,
    statusManagementId: initialStatus.data!.idManagementStatus,
  });

  return {
    idManagement: management.data!.idManagement,
    number: management.data!.number,
    initialStatus: initialStatus.data!.name,
    finalStatus: finalStatus.data!.name,
  };
}

export async function createConcept(
  page: Page,
  overrides: { name?: string; value?: number } = {},
): Promise<ApiResult<{ idConcept: number }>> {
  const id = uniqueId();
  return apiPost(page, "/concepts", {
    name: `Concept E2E ${id}`,
    value: 1000,
    ...overrides,
  });
}

/**
 * CU39 - BudgetTemplate: associates a Concept's price with a ProcedureType.
 * BudgetTemplateController accepts the raw BudgetTemplate entity.
 */
export async function createBudgetTemplate(
  page: Page,
  procedureTypeId: number,
  conceptId: number,
): Promise<ApiResult<Record<string, unknown>>> {
  return apiPost(page, "/budget-templates", {
    budgetTemplatePK: { fkIdProcedureType: procedureTypeId, fkIdConcept: conceptId },
    procedureType: { idProcedureType: procedureTypeId },
    concept: { idConcept: conceptId },
  });
}

/**
 * CU71 - Item catalog entry (reusable, not yet attached to a budget).
 */
export async function createItem(
  page: Page,
  overrides: { name?: string; value?: number } = {},
): Promise<ApiResult<{ idItem: number }>> {
  const id = uniqueId();
  return apiPost(page, "/items", {
    name: `Item E2E ${id}`,
    value: 500,
    percentage: 0,
    ...overrides,
  });
}

export async function createManagementStatus(
  page: Page,
  overrides: { name?: string } = {},
): Promise<ApiResult<{ idManagementStatus: number; name: string }>> {
  const id = uniqueId();
  return apiPost(page, "/management-statuses", {
    name: `Status E2E ${id}`,
    notes: "Created by E2E test",
    ...overrides,
  });
}

export async function createDocumentType(
  page: Page,
  overrides: {
    name?: string;
    expires?: boolean;
    dueDays?: number;
    deliveredBy?: string;
    enabled?: boolean;
    returned?: boolean;
  } = {},
): Promise<ApiResult<{ idDocumentType: number }>> {
  const id = uniqueId();
  return apiPost(page, "/document-types", {
    name: `Document type E2E ${id}`,
    enabled: true,
    returned: false,
    expires: true,
    dueDays: 30,
    deliveredBy: "Cliente",
    ...overrides,
  });
}

/**
 * ProcedureTemplate helper (CU03/CU43) — links a procedure type to a document
 * type as required documentation. ProcedureTemplateController
 * accepts the raw ProcedureTemplate entity.
 */
export async function createProcedureTemplate(
  page: Page,
  idProcedureType: number,
  idDocumentType: number,
): Promise<ApiResult<unknown>> {
  return apiPost(page, "/procedure-templates", {
    procedureTemplatePK: { fkIdProcedureType: idProcedureType, fkIdDocumentType: idDocumentType },
    procedureType: { idProcedureType: idProcedureType },
    documentType: { idDocumentType: idDocumentType },
  });
}

/**
 * Substitution helpers — SubstitutionController accepts the raw Substitution entity.
 */
export async function createSubstitution(
  page: Page,
  idSuplente: number,
  idSuplantado: number,
  overrides: Partial<SubstitutionPayload> = {},
): Promise<ApiResult<{ idSubstitution: number }>> {
  return apiPost(page, "/substitutions", {
    fkIdSubstitute: { personId: idSuplente },
    fkIdSubstituted: { personId: idSuplantado },
    dateStart: new Date().toISOString().split("T")[0],
    notes: "E2E test substitution",
    ...overrides,
  });
}

/**
 * Testimony helpers — TestimonyController accepts a DtoTestimony (idTestimony, deed, notes).
 */
export async function createTestimony(
  page: Page,
  idDeed: number,
  overrides: Partial<TestimonyPayload> = {},
): Promise<ApiResult<{ idTestimony: number }>> {
  return apiPost(page, "/testimonies", {
    deed: { idDeed: idDeed },
    ...overrides,
  });
}

/**
 * ManagementHistory helpers — HistoryController accepts the raw History entity.
 */
export async function createHistoryEntry(
  page: Page,
  idManagement: number,
  status: string = "Iniciado",
): Promise<ApiResult<any>> {
  return apiPost(page, "/history", {
    fkIdManagement: { idManagement: idManagement },
    date: new Date().toISOString().split("T")[0],
    notes: `ManagementHistory E2E ${uniqueId()}`,
  });
}

/**
 * Bruno sync helpers — run the same request Bruno would
 * These mirror the exact HTTP calls in Bruno .bru files
 */
export async function brunoLogin(
  page: Page,
  username: string = "admin",
  password: string = "admin",
): Promise<ApiResult> {
  return apiPost(page, "/users/login", {
    name: username,
    password,
  });
}

export async function brunoGetBudgetsByPerson(
  page: Page,
  personId: number,
): Promise<ApiResult> {
  return apiGet(page, `/budgets/person/${personId}`);
}

export async function brunoGetManagementsByClient(
  page: Page,
  clientId: number,
): Promise<ApiResult> {
  return apiGet(page, `/managements/client/${clientId}`);
}

export async function brunoGetHistoryByManagement(
  page: Page,
  managementId: number,
): Promise<ApiResult> {
  return apiGet(page, `/history/management/${managementId}`);
}

export async function brunoSearchPeople(
  page: Page,
  query: string,
): Promise<ApiResult> {
  return apiGet(page, `/people/search?firstName=${encodeURIComponent(query)}`);
}

export async function brunoGetAuditRecords(
  page: Page,
  userId: number,
): Promise<ApiResult> {
  return apiGet(page, `/audit-log/user/${userId}`);
}

export async function brunoGetCurrentManagementStatus(
  page: Page,
  managementId: number,
): Promise<ApiResult> {
  return apiGet(page, `/managements/${managementId}/current-status`);
}

export async function brunoActivateManagement(
  page: Page,
  managementId: number,
): Promise<ApiResult> {
  return apiPut(page, `/managements/${managementId}`, {
    detalle: "Activada desde Playwright",
    dateStart: new Date().toISOString().split("T")[0],
  });
}

export async function brunoArchiveManagement(
  page: Page,
  managementId: number,
): Promise<ApiResult> {
  return apiPut(page, `/managements/${managementId}`, {
    detalle: "Archivada desde Playwright",
  });
}

export async function brunoGetAvailableNotaries(
  page: Page,
): Promise<ApiResult> {
  return apiGet(page, "/deeds/available-notaries");
}

export async function brunoGetBudgetReport(
  page: Page,
  budgetId: number,
): Promise<ApiResult> {
  return apiGet(page, `/reports/budget/${budgetId}`);
}
