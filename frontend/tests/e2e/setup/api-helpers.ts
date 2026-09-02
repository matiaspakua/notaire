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
// Domain-specific helper types (mirroring Bruno DTOs)
// ──────────────────────────────────────────────

export interface PersonaPayload {
  nombre: string;
  apellido: string;
  numeroIdentificacion: string;
  email?: string;
  telefono?: string;
  esCliente?: boolean;
  tipoIdentificacion?: { idTipoIdentificacion: number };
  nacionalidad?: string;
  fechaNacimiento?: string;
  cuit?: string;
  estadoCivil?: string;
  sexo?: string;
  domicilio?: string;
  ocupacion?: string;
  registroEscribano?: string;
}

export interface PresupuestoPayload {
  fkIdPersona?: { idPersona: number };
  fecha?: string;
  encabezado?: string;
  estado?: string;
  observaciones?: string;
  monto?: number;
}

export interface CompleteCaseGestionPayload {
  numero: number;
  encabezado?: string;
  observaciones?: string;
  presupuestoId: number;
  escribanoId: number;
  estadoGestionId: number;
  tipoTramiteId: number;
  inmuebleId?: number;
}

export interface EscrituraPayload {
  fecha?: string;
  cuerpo?: string;
  estado?: string;
  idPersonaOtorgante?: number;
  idPersonaFirmante?: number;
  idGestion?: number;
  idFolio?: number;
  numeroEscritura?: string;
  tomo?: string;
  folio?: string;
}

export interface UsuarioPayload {
  nombre: string;
  contrasenia: string;
  tipo: string;
  activo?: boolean;
}

export interface PagoPayload {
  idPresupuesto?: number;
  monto: number;
  fecha?: string;
  metodoPago?: string;
  observaciones?: string;
}

export interface TestimonioPayload {
  idEscritura: number;
  fecha?: string;
  estado?: string;
  observaciones?: string;
}

export interface DocumentoPresentadoPayload {
  nombre: string;
  tipoDocumento?: string;
  idGestion?: number;
  tieneDeuda?: boolean;
  fechaVencimiento?: string;
}

export interface MovimientoTestimonioPayload {
  idTestimonio: number;
  fecha: string;
  tipoMovimiento: string;
  observaciones?: string;
}

export interface SuplenciaPayload {
  fkIdSuplente: { idPersona: number };
  fkIdSuplantado: { idPersona: number };
  fechaInicio: string;
  fechaFin?: string;
  observaciones?: string;
}

// ──────────────────────────────────────────────
// Domain-specific helper functions
// ──────────────────────────────────────────────

/**
 * Seed test helpers — create entities with unique test IDs
 */

let _testCounter = Date.now();

/** Generate a unique test identifier */
export function uniqueId(): number {
  return ++_testCounter;
}

/** Generate a unique string suitable for test names/IDs */
export function uniqueLabel(prefix: string): string {
  return `${prefix}-${uniqueId()}`;
}

/**
 * Persona helpers
 */
export async function createPersona(
  page: Page,
  overrides: Partial<PersonaPayload> = {},
): Promise<ApiResult<{ idPersona: number }>> {
  const id = uniqueId();
  return apiPost(page, "/personas", {
    nombre: "Test",
    apellido: `Persona-${id}`,
    numeroIdentificacion: `E2E${id}`,
    email: `e2e-${id}@notaire.test`,
    esCliente: true,
    tipoIdentificacion: { idTipoIdentificacion: 1 },
    nacionalidad: "Argentina",
    fechaNacimiento: "1990-01-01",
    estadoCivil: "Soltero",
    sexo: "Masculino",
    ...overrides,
  });
}

/**
 * Presupuesto helpers
 */
export async function createPresupuesto(
  page: Page,
  personaId: number,
  _conceptoId?: number,
  overrides: Partial<PresupuestoPayload> = {},
): Promise<ApiResult<{ idPresupuesto: number }>> {
  return apiPost(page, "/presupuestos", {
    fkIdPersona: { idPersona: personaId },
    fecha: new Date().toISOString().split("T")[0],
    encabezado: `Presupuesto E2E ${uniqueId()}`,
    estado: "Pendiente",
    observaciones: `Presupuesto E2E ${uniqueId()}`,
    ...overrides,
  });
}

/**
 * Gestión helpers (CU02 - complete-case: gestión + trámite in one call)
 */
export async function createCompleteCaseGestion(
  page: Page,
  overrides: Partial<CompleteCaseGestionPayload> & { presupuestoId: number },
): Promise<ApiResult<{ idGestion: number; numero: number; estadoActual: string }>> {
  return apiPost(page, "/gestiones/complete-case", {
    // `numero` is a Postgres `integer` column; uniqueId() is Date.now()-based and overflows it.
    numero: uniqueId() % 1_000_000,
    encabezado: `Gestión E2E ${uniqueId()}`,
    escribanoId: 1,
    estadoGestionId: 1,
    tipoTramiteId: 4,
    ...overrides,
  });
}

/**
 * CU43 - trámites of a gestión with their required documentation. `DtoGestionSummary`
 * (the `complete-case`/`GET /gestiones/{id}` read-model) never exposes trámite IDs, so
 * this is the only way to learn the ID of the trámite `complete-case` created.
 */
export async function getReingresoDocumentacion(
  page: Page,
  idGestion: number,
): Promise<ApiResult<{ idGestion: number; numero: number; tramites: Array<{ idTramite: number }> }>> {
  return apiGet(page, `/gestiones/${idGestion}/reingreso-documentacion`);
}

/**
 * Plain gestión helper (CU43) — unlike `createCompleteCaseGestion`, this does
 * not create a `Tramite`, so the resulting gestión has zero trámites.
 */
export async function createGestionSinTramite(
  page: Page,
  escribanoId: number,
  overrides: { encabezado?: string; numero?: number } = {},
): Promise<ApiResult<{ idGestion: number; numero: number }>> {
  return apiPost(page, "/gestiones", {
    encabezado: `Gestión E2E ${uniqueId()}`,
    fechaInicio: new Date().toISOString().split("T")[0],
    numero: uniqueId() % 1_000_000,
    fkIdPersonaEscribano: { idPersona: escribanoId },
    ...overrides,
  });
}

/**
 * Escritura helpers
 */
export async function createEscritura(
  page: Page,
  gestionId: number,
  personaId: number,
  overrides: Partial<EscrituraPayload> = {},
): Promise<ApiResult<{ idEscritura: number }>> {
  const id = uniqueId();
  return apiPost(page, "/escrituras", {
    numeroEscritura: `E2E-${id}`,
    fecha: new Date().toISOString().split("T")[0],
    cuerpo: `Contenido de escritura E2E ${id}`,
    estado: "Pendiente",
    idPersonaOtorgante: personaId,
    idPersonaFirmante: personaId,
    idGestion: gestionId,
    ...overrides,
  });
}

/**
 * Usuario helpers
 */
export async function createUsuario(
  page: Page,
  personaId?: number,
  overrides: Partial<UsuarioPayload> = {},
): Promise<ApiResult<{ idUsuario: number }>> {
  const id = uniqueId();
  return apiPost(page, "/usuarios", {
    nombre: `e2euser-${id}`,
    contrasenia: "Test1234!",
    tipo: "EMPLEADO",
    activo: true,
    ...overrides,
  });
}

/**
 * Pago helpers
 */
export async function createPago(
  page: Page,
  presupuestoId: number,
  overrides: Partial<PagoPayload> = {},
): Promise<ApiResult<{ idPago: number }>> {
  return apiPost(page, "/pagos", {
    idPresupuesto: presupuestoId,
    monto: 5000,
    fecha: new Date().toISOString().split("T")[0],
    metodoPago: "Efectivo",
    ...overrides,
  });
}

/**
 * Folio helpers
 */
export async function createFolio(
  page: Page,
  personaId?: number,
  overrides: { numero?: number; anio?: number; estado?: string; tipoFolioId?: number; escribanoId?: number } = {},
): Promise<ApiResult<{ idFolio: number }>> {
  return apiPost(page, "/folio", {
    numero: Math.floor(10000 + Math.random() * 90000),
    anio: 2026,
    estado: "Nuevo",
    tipoFolioId: 1,
    escribanoId: personaId || 1,
    ...overrides,
  });
}

export async function createTipoDeFolio(
  page: Page,
  overrides: { nombre?: string; esAuxiliar?: boolean; habilitado?: boolean } = {},
): Promise<ApiResult<{ idTipoFolio: number }>> {
  const id = uniqueId();
  return apiPost(page, "/tipo-folio", {
    nombre: `Tipo Folio E2E ${id}`,
    habilitado: true,
    esAuxiliar: false,
    ...overrides,
  });
}

/**
 * Catálogo helpers
 */
export async function createTipoTramite(
  page: Page,
  overrides: { nombre?: string; descripcion?: string } = {},
): Promise<ApiResult<{ idTipoDeTramite: number }>> {
  const id = uniqueId();
  return apiPost(page, "/tipo-tramite", {
    nombre: `Tipo Tramite E2E ${id}`,
    descripcion: "Created by E2E test",
    seArchiva: false,
    seInscribe: false,
    ...overrides,
  });
}

/**
 * Trámite / documento presentado helpers (CU10)
 */
export async function createTramite(
  page: Page,
  gestionId: number,
  tipoTramiteId: number,
): Promise<ApiResult<{ idTramite: number }>> {
  return apiPost(page, "/tramites", {
    fkIdTipoTramite: { idTipoTramite: tipoTramiteId },
    fkIdGestion: { idGestion: gestionId },
  });
}

export async function createDocumentoEntidadExterna(
  page: Page,
  tramiteId: number,
  overrides: { nombre?: string; quienEntrega?: string } = {},
): Promise<ApiResult<{ idDocumentoPresentado: number }>> {
  return apiPost(page, "/documento-presentado", {
    tramiteId,
    quienEntrega: "Entidad Externa",
    entregado: false,
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
  overrides: { nombre?: string; descripcion?: string; activo?: boolean } = {},
): Promise<ApiResult<{ id: number }>> {
  const id = uniqueId();
  return apiPost(page, "/workflow-definition", {
    nombre: `Workflow E2E ${id}`,
    descripcion: "Created by E2E test",
    activo: true,
    ...overrides,
  });
}

export async function createWorkflowNode(
  page: Page,
  workflowDefinitionId: number,
  estadoGestionId: number,
  tipo: "INITIAL" | "INTERMEDIATE" | "FINAL",
): Promise<ApiResult<{ id: number }>> {
  return apiPost(page, "/workflow-node", {
    workflowDefinitionId,
    estadoGestionId,
    tipo,
    posicionX: 0,
    posicionY: 0,
  });
}

export async function createWorkflowTransition(
  page: Page,
  workflowDefinitionId: number,
  nodoOrigenId: number,
  nodoDestinoId: number,
): Promise<ApiResult<{ id: number }>> {
  return apiPost(page, "/workflow-transition", {
    workflowDefinitionId,
    nodoOrigenId,
    nodoDestinoId,
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
    estadoInicial.data!.idEstadoGestion,
    "INITIAL",
  );
  const nodoFinal = await createWorkflowNode(
    page,
    workflowId,
    estadoFinal.data!.idEstadoGestion,
    "FINAL",
  );
  await createWorkflowTransition(page, workflowId, nodoInicial.data!.id, nodoFinal.data!.id);

  const tipoTramite = await createTipoTramite(page);
  await assignWorkflowToTipoTramite(page, tipoTramite.data!.idTipoDeTramite, workflowId);

  const gestion = await createCompleteCaseGestion(page, {
    presupuestoId,
    tipoTramiteId: tipoTramite.data!.idTipoDeTramite,
    estadoGestionId: estadoInicial.data!.idEstadoGestion,
  });

  return {
    idGestion: gestion.data!.idGestion,
    numero: gestion.data!.numero,
    estadoInicial: estadoInicial.data!.nombre,
    estadoFinal: estadoFinal.data!.nombre,
  };
}

export async function createConcepto(
  page: Page,
  overrides: { nombre?: string; valor?: number } = {},
): Promise<ApiResult<{ idConcepto: number }>> {
  const id = uniqueId();
  return apiPost(page, "/conceptos", {
    nombre: `Concepto E2E ${id}`,
    descripcion: "Created by E2E test",
    valor: 1000,
    ...overrides,
  });
}

export async function createEstadoGestion(
  page: Page,
  overrides: { nombre?: string } = {},
): Promise<ApiResult<{ idEstadoGestion: number; nombre: string }>> {
  const id = uniqueId();
  return apiPost(page, "/estado-gestion", {
    nombre: `Estado E2E ${id}`,
    descripcion: "Created by E2E test",
    ...overrides,
  });
}

export async function createTipoDocumento(
  page: Page,
  overrides: { nombre?: string; vence?: boolean; diasVencimiento?: number; quienEntrega?: string } = {},
): Promise<ApiResult<{ idTipoDocumento: number }>> {
  const id = uniqueId();
  return apiPost(page, "/tipo-de-documento", {
    nombre: `Tipo Documento E2E ${id}`,
    habilitado: true,
    devuelto: false,
    vence: true,
    diasVencimiento: 30,
    quienEntrega: "Cliente",
    ...overrides,
  });
}

/**
 * PlantillaTramite helper (CU03/CU43) — links a tipo de trámite to a tipo de
 * documento as required documentación necesaria.
 */
export async function createPlantillaTramite(
  page: Page,
  idTipoTramite: number,
  idTipoDocumento: number,
): Promise<ApiResult<unknown>> {
  return apiPost(page, "/plantilla-tramite", {
    plantillaTramitePK: { fkIdTipoTramite: idTipoTramite, fkIdTipoDocumento: idTipoDocumento },
    tipoDeTramite: { idTipoTramite },
    tipoDeDocumento: { idTipoDocumento },
  });
}

/**
 * Suplencia helpers
 */
export async function createSuplencia(
  page: Page,
  idSuplente: number,
  idSuplantado: number,
  overrides: Partial<SuplenciaPayload> = {},
): Promise<ApiResult<{ idSuplencia: number }>> {
  return apiPost(page, "/suplencia", {
    fkIdSuplente: { idPersona: idSuplente },
    fkIdSuplantado: { idPersona: idSuplantado },
    fechaInicio: new Date().toISOString().split("T")[0],
    observaciones: "Suplencia E2E de prueba",
    ...overrides,
  });
}

/**
 * Testimonio helpers
 */
export async function createTestimonio(
  page: Page,
  idEscritura: number,
  overrides: Partial<TestimonioPayload> = {},
): Promise<ApiResult<{ idTestimonio: number }>> {
  return apiPost(page, "/testimonio", {
    idEscritura,
    fecha: new Date().toISOString().split("T")[0],
    estado: "Pendiente",
    ...overrides,
  });
}

/**
 * Historial helpers
 */
export async function createHistorialEntry(
  page: Page,
  idGestion: number,
  estado: string = "Iniciado",
): Promise<ApiResult<any>> {
  return apiPost(page, "/historial", {
    idGestion,
    estado,
    fecha: new Date().toISOString().split("T")[0],
    observaciones: `Historial E2E ${uniqueId()}`,
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
    nombre: username,
    contrasenia: password,
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
  return apiGet(page, `/personas/buscar?q=${encodeURIComponent(query)}`);
}

export async function brunoGetRegistrosAuditoria(
  page: Page,
  usuarioId: number,
): Promise<ApiResult> {
  return apiGet(page, `/registro-auditoria/usuario/${usuarioId}`);
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
    fechaInicio: new Date().toISOString().split("T")[0],
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
