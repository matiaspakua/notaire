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

export interface PersonaPayload {
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

export interface PresupuestoPayload {
  person?: { idPerson: number };
  date?: string;
  encabezado?: string;
  status?: string;
  notes?: string;
  propertyAmount?: number;
}

export interface CompleteCaseGestionPayload {
  number: number;
  encabezado?: string;
  notes?: string;
  presupuestoId: number;
  escribanoId: number;
  estadoGestionId: number;
  tipoTramiteId: number;
  inmuebleId?: number;
}

export interface EscrituraPayload {
  dateDeedrecording?: string;
  body?: string;
  status?: string;
  idFolio?: number;
  notes?: string;
}

export interface UsuarioPayload {
  name: string;
  password: string;
  type: string;
  active?: boolean;
}

export interface PagoPayload {
  idBudget?: number;
  amount: number;
  date?: string;
  paymentMethod?: string;
  notes?: string;
}

export interface TestimonioPayload {
  idDeed: number;
  notes?: string;
}

export interface DocumentoPresentadoPayload {
  name: string;
  typeId?: number;
  procedureId?: number;
  deliveredBy?: string;
  delivered?: boolean;
}

export interface SuplenciaPayload {
  fkIdSubstitute: { idPerson: number };
  fkIdSubstituted: { idPerson: number };
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

// Kept below Java's 32-bit `int` max (2,147,483,647) so IDs fed into `int`-typed
// entity fields (e.g. Deed.number) don't overflow and fail JSON deserialization.
let _testCounter = Date.now() % 1_000_000_000;

/** Generate a unique test identifier */
export function uniqueId(): number {
  return ++_testCounter;
}

/** Generate a unique string suitable for test names/IDs */
export function uniqueLabel(prefix: string): string {
  return `${prefix}-${uniqueId()}`;
}

/**
 * Persona helpers — PersonController accepts the raw Person entity at /people.
 */
export async function createPersona(
  page: Page,
  overrides: Partial<PersonaPayload> = {},
): Promise<ApiResult<{ personId: number; firstName?: string; lastName?: string }>> {
  const id = uniqueId();
  return apiPost(page, "/people", {
    firstName: "Test",
    lastName: `Persona-${id}`,
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
 * Presupuesto helpers — BudgetController accepts the raw Budget entity.
 */
export async function createPresupuesto(
  page: Page,
  personaId: number,
  _conceptoId?: number,
  overrides: Partial<PresupuestoPayload> = {},
): Promise<ApiResult<{ idBudget: number }>> {
  return apiPost(page, "/presupuestos", {
    person: { idPerson: personaId },
    date: new Date().toISOString().split("T")[0],
    encabezado: `Presupuesto E2E ${uniqueId()}`,
    status: "Pendiente",
    notes: `Presupuesto E2E ${uniqueId()}`,
    ...overrides,
  });
}

/**
 * Gestión helpers (CU02 - complete-case: gestión + trámite in one call)
 */
export async function createCompleteCaseGestion(
  page: Page,
  overrides: Partial<CompleteCaseGestionPayload> & { presupuestoId: number },
): Promise<ApiResult<{ idManagement: number; number: number; statusActual: string }>> {
  return apiPost(page, "/gestiones/complete-case", {
    // `number` is a Postgres `integer` column; uniqueId() is Date.now()-based and overflows it.
    number: uniqueId() % 1_000_000,
    encabezado: `Gestión E2E ${uniqueId()}`,
    escribanoId: 1,
    estadoGestionId: 1,
    tipoTramiteId: 4,
    ...overrides,
  });
}

/**
 * CU43 - trámites of a gestión with their required documentation. `DtoManagementSummary`
 * (the `complete-case`/`GET /gestiones/{id}` read-model) never exposes trámite IDs, so
 * this is the only way to learn the ID of the trámite `complete-case` created.
 */
export async function getReingresoDocumentacion(
  page: Page,
  idGestion: number,
): Promise<ApiResult<{ idManagement: number; number: number; procedures: Array<{ idProcedure: number }> }>> {
  return apiGet(page, `/gestiones/${idGestion}/reingreso-documentacion`);
}

/**
 * Plain gestión helper (CU43) — unlike `createCompleteCaseGestion`, this does
 * not create a `Tramite`, so the resulting gestión has zero trámites.
 * ManagementController accepts the raw DeedManagement entity.
 */
export async function createGestionSinTramite(
  page: Page,
  escribanoId: number,
  overrides: { encabezado?: string; number?: number } = {},
): Promise<ApiResult<{ idManagement: number; number: number }>> {
  return apiPost(page, "/gestiones", {
    encabezado: `Gestión E2E ${uniqueId()}`,
    dateStart: new Date().toISOString().split("T")[0],
    number: uniqueId() % 1_000_000,
    fkIdNotaryPerson: { idPerson: escribanoId },
    ...overrides,
  });
}

/**
 * Escritura helpers — DeedController accepts the raw Deed entity.
 */
export async function createEscritura(
  page: Page,
  gestionId: number,
  personaId: number,
  overrides: Partial<EscrituraPayload> = {},
): Promise<ApiResult<{ idDeed: number }>> {
  const id = uniqueId();
  return apiPost(page, "/escrituras", {
    number: id % 1_000_000,
    dateDeedrecording: new Date().toISOString().split("T")[0],
    body: `Contenido de escritura E2E ${id}`,
    status: "Pendiente",
    ...overrides,
  });
}

/**
 * Usuario helpers — UserController's UserRequest is (name, password, type, active).
 */
export async function createUsuario(
  page: Page,
  personaId?: number,
  overrides: Partial<UsuarioPayload> = {},
): Promise<ApiResult<{ idUser: number }>> {
  const id = uniqueId();
  return apiPost(page, "/usuarios", {
    name: `e2euser-${id}`,
    password: "Test1234!",
    type: "EMPLEADO",
    active: true,
    ...overrides,
  });
}

/**
 * Pago helpers — PaymentController's PaymentRequest is (idBudget, amount, date, notes, paymentMethod).
 */
export async function createPago(
  page: Page,
  presupuestoId: number,
  overrides: Partial<PagoPayload> = {},
): Promise<ApiResult<{ idPayment: number }>> {
  return apiPost(page, "/pagos", {
    idBudget: presupuestoId,
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
  personaId?: number,
  overrides: {
    number?: number;
    year?: number;
    status?: string;
    typeFolioId?: number;
    notaryId?: number;
    deedId?: number;
  } = {},
): Promise<ApiResult<{ idFolio: number }>> {
  return apiPost(page, "/folio", {
    number: Math.floor(10000 + Math.random() * 90000),
    year: 2026,
    status: "Nuevo",
    typeFolioId: 1,
    notaryId: personaId || 1,
    ...overrides,
  });
}

export async function createTipoDeFolio(
  page: Page,
  overrides: { name?: string; isAuxiliary?: boolean; enabled?: boolean } = {},
): Promise<ApiResult<{ idFolioType: number }>> {
  const id = uniqueId();
  return apiPost(page, "/tipo-folio", {
    name: `Tipo Folio E2E ${id}`,
    enabled: true,
    isAuxiliary: false,
    ...overrides,
  });
}

/**
 * Catálogo helpers
 */
export async function createTipoTramite(
  page: Page,
  overrides: { name?: string; notes?: string } = {},
): Promise<ApiResult<{ idProcedureType: number }>> {
  const id = uniqueId();
  return apiPost(page, "/tipo-tramite", {
    name: `Tipo Tramite E2E ${id}`,
    notes: "Created by E2E test",
    isArchived: false,
    isRegistered: false,
    ...overrides,
  });
}

/**
 * Trámite / documento presentado helpers (CU10) — ProcedureController accepts
 * the raw Procedure entity.
 */
export async function createTramite(
  page: Page,
  gestionId: number,
  tipoTramiteId: number,
): Promise<ApiResult<{ idProcedure: number }>> {
  return apiPost(page, "/tramites", {
    fkIdProcedureType: { idProcedureType: tipoTramiteId },
    fkIdManagement: { idManagement: gestionId },
  });
}

/**
 * SubmittedDocumentController's SubmittedDocumentRequest is
 * (typeId, date, delivered, procedureId, deliveredBy, name).
 */
export async function createDocumentoEntidadExterna(
  page: Page,
  tramiteId: number,
  overrides: { name?: string; deliveredBy?: string } = {},
): Promise<ApiResult<{ idSubmittedDocument: number }>> {
  return apiPost(page, "/documento-presentado", {
    procedureId: tramiteId,
    deliveredBy: "Entidad Externa",
    delivered: false,
    ...overrides,
  });
}

/**
 * Workflow helpers (CU83) — used to seed a self-contained workflow
 * (definition + nodes + transition) for gestión transition E2E tests,
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
  estadoGestionId: number,
  type: "INITIAL" | "INTERMEDIATE" | "FINAL",
): Promise<ApiResult<{ id: number }>> {
  return apiPost(page, "/workflow-node", {
    workflowDefinitionId,
    statusManagementId: estadoGestionId,
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

export async function assignWorkflowToTipoTramite(
  page: Page,
  tipoTramiteId: number,
  workflowDefinitionId: number,
): Promise<ApiResult<unknown>> {
  return apiPut(page, `/tipo-tramite/${tipoTramiteId}/workflow`, { workflowDefinitionId });
}

/**
 * Seeds a self-contained two-node workflow (INITIAL -> FINAL) with a single
 * valid transition, assigns it to a fresh tipo de trámite, and creates a
 * gestión whose estado inicial matches the workflow's INITIAL node — ready
 * for a "Cambiar estado" transition to the FINAL node's estado.
 */
export async function seedGestionWithWorkflow(
  page: Page,
  presupuestoId: number,
): Promise<{
  idGestion: number;
  numero: number;
  estadoInicial: string;
  estadoFinal: string;
}> {
  const estadoInicial = await createEstadoGestion(page);
  const estadoFinal = await createEstadoGestion(page);
  const workflow = await createWorkflowDefinition(page);
  const workflowId = workflow.data!.id;

  const nodoInicial = await createWorkflowNode(
    page,
    workflowId,
    estadoInicial.data!.idManagementStatus,
    "INITIAL",
  );
  const nodoFinal = await createWorkflowNode(
    page,
    workflowId,
    estadoFinal.data!.idManagementStatus,
    "FINAL",
  );
  await createWorkflowTransition(page, workflowId, nodoInicial.data!.id, nodoFinal.data!.id);

  const tipoTramite = await createTipoTramite(page);
  await assignWorkflowToTipoTramite(page, tipoTramite.data!.idProcedureType, workflowId);

  const gestion = await createCompleteCaseGestion(page, {
    presupuestoId,
    tipoTramiteId: tipoTramite.data!.idProcedureType,
    estadoGestionId: estadoInicial.data!.idManagementStatus,
  });

  return {
    idGestion: gestion.data!.idManagement,
    numero: gestion.data!.number,
    estadoInicial: estadoInicial.data!.name,
    estadoFinal: estadoFinal.data!.name,
  };
}

export async function createConcepto(
  page: Page,
  overrides: { name?: string; value?: number } = {},
): Promise<ApiResult<{ idConcept: number }>> {
  const id = uniqueId();
  return apiPost(page, "/conceptos", {
    name: `Concepto E2E ${id}`,
    value: 1000,
    ...overrides,
  });
}

/**
 * CU39 - PlantillaPresupuesto: associates a Concepto's price with a TipoDeTramite.
 * BudgetTemplateController accepts the raw BudgetTemplate entity.
 */
export async function createPlantillaPresupuesto(
  page: Page,
  tipoTramiteId: number,
  conceptoId: number,
): Promise<ApiResult<Record<string, unknown>>> {
  return apiPost(page, "/plantilla-presupuestos", {
    budgetTemplatePK: { fkIdProcedureType: tipoTramiteId, fkIdConcept: conceptoId },
    procedureType: { idProcedureType: tipoTramiteId },
    concept: { idConcept: conceptoId },
  });
}

/**
 * CU71 - Item catalog entry (reusable, not yet attached to a presupuesto).
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

export async function createEstadoGestion(
  page: Page,
  overrides: { name?: string } = {},
): Promise<ApiResult<{ idManagementStatus: number; name: string }>> {
  const id = uniqueId();
  return apiPost(page, "/estado-gestion", {
    name: `Estado E2E ${id}`,
    notes: "Created by E2E test",
    ...overrides,
  });
}

export async function createTipoDocumento(
  page: Page,
  overrides: { name?: string; expires?: boolean; dueDays?: number; deliveredBy?: string } = {},
): Promise<ApiResult<{ idDocumentType: number }>> {
  const id = uniqueId();
  return apiPost(page, "/tipo-de-documento", {
    name: `Tipo Documento E2E ${id}`,
    enabled: true,
    expires: true,
    dueDays: 30,
    deliveredBy: "Cliente",
    ...overrides,
  });
}

/**
 * PlantillaTramite helper (CU03/CU43) — links a tipo de trámite to a tipo de
 * documento as required documentación necesaria. ProcedureTemplateController
 * accepts the raw ProcedureTemplate entity.
 */
export async function createPlantillaTramite(
  page: Page,
  idTipoTramite: number,
  idTipoDocumento: number,
): Promise<ApiResult<unknown>> {
  return apiPost(page, "/plantilla-tramite", {
    procedureTemplatePK: { fkIdProcedureType: idTipoTramite, fkIdDocumentType: idTipoDocumento },
    procedureType: { idProcedureType: idTipoTramite },
    documentType: { idDocumentType: idTipoDocumento },
  });
}

/**
 * Suplencia helpers — SubstitutionController accepts the raw Substitution entity.
 */
export async function createSuplencia(
  page: Page,
  idSuplente: number,
  idSuplantado: number,
  overrides: Partial<SuplenciaPayload> = {},
): Promise<ApiResult<{ idSubstitution: number }>> {
  return apiPost(page, "/suplencia", {
    fkIdSubstitute: { idPerson: idSuplente },
    fkIdSubstituted: { idPerson: idSuplantado },
    dateStart: new Date().toISOString().split("T")[0],
    notes: "Suplencia E2E de prueba",
    ...overrides,
  });
}

/**
 * Testimonio helpers — TestimonyController accepts a DtoTestimony (idTestimony, deed, notes).
 */
export async function createTestimonio(
  page: Page,
  idEscritura: number,
  overrides: Partial<TestimonioPayload> = {},
): Promise<ApiResult<{ idTestimony: number }>> {
  return apiPost(page, "/testimonio", {
    deed: { idDeed: idEscritura },
    ...overrides,
  });
}

/**
 * Historial helpers — HistoryController accepts the raw History entity.
 */
export async function createHistorialEntry(
  page: Page,
  idGestion: number,
  estado: string = "Iniciado",
): Promise<ApiResult<any>> {
  return apiPost(page, "/historial", {
    fkIdManagement: { idManagement: idGestion },
    date: new Date().toISOString().split("T")[0],
    notes: `Historial E2E ${uniqueId()}`,
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
  return apiPost(page, "/usuarios/login", {
    name: username,
    password,
  });
}

export async function brunoGetPresupuestosByPersona(
  page: Page,
  personaId: number,
): Promise<ApiResult> {
  return apiGet(page, `/presupuestos/persona/${personaId}`);
}

export async function brunoGetGestionesByCliente(
  page: Page,
  clienteId: number,
): Promise<ApiResult> {
  return apiGet(page, `/gestiones/cliente/${clienteId}`);
}

export async function brunoGetHistorialByGestion(
  page: Page,
  gestionId: number,
): Promise<ApiResult> {
  return apiGet(page, `/historial/gestion/${gestionId}`);
}

export async function brunoSearchPersonas(
  page: Page,
  query: string,
): Promise<ApiResult> {
  return apiGet(page, `/people/search?firstName=${encodeURIComponent(query)}`);
}

export async function brunoGetRegistrosAuditoria(
  page: Page,
  usuarioId: number,
): Promise<ApiResult> {
  return apiGet(page, `/audit-log/user/${usuarioId}`);
}

export async function brunoGetEstadoActualGestion(
  page: Page,
  gestionId: number,
): Promise<ApiResult> {
  return apiGet(page, `/gestiones/${gestionId}/estado-actual`);
}

export async function brunoActivarGestion(
  page: Page,
  gestionId: number,
): Promise<ApiResult> {
  return apiPut(page, `/gestiones/${gestionId}`, {
    detalle: "Activada desde Playwright",
    dateStart: new Date().toISOString().split("T")[0],
  });
}

export async function brunoArchivarGestion(
  page: Page,
  gestionId: number,
): Promise<ApiResult> {
  return apiPut(page, `/gestiones/${gestionId}`, {
    detalle: "Archivada desde Playwright",
  });
}

export async function brunoGetEscribanosDisponibles(
  page: Page,
): Promise<ApiResult> {
  return apiGet(page, "/escrituras/escribanos-disponibles");
}

export async function brunoGetReportePresupuesto(
  page: Page,
  presupuestoId: number,
): Promise<ApiResult> {
  return apiGet(page, `/reportes/presupuesto/${presupuestoId}`);
}
