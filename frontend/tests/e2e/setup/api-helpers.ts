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
}

export interface GestionPayload {
  idPersona?: number;
  detalle?: string;
  fechaInicio?: string;
  idTipoDeTramite?: number;
  idEstadoDeGestion?: number;
  idEscritura?: number;
  idPresupuesto?: number;
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
  idGestion?: number;
  monto: number;
  fecha?: string;
  formaPago?: string;
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
 * Gestión helpers
 */
export async function createGestion(
  page: Page,
  personaId: number,
  tipoTramiteId: number,
  overrides: Partial<GestionPayload> = {},
): Promise<ApiResult<{ idGestion: number }>> {
  return apiPost(page, "/gestiones", {
    detalle: `Gestion E2E ${uniqueId()}`,
    fechaInicio: new Date().toISOString().split("T")[0],
    idPersona: personaId,
    idTipoDeTramite: tipoTramiteId,
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
  gestionId: number,
  overrides: Partial<PagoPayload> = {},
): Promise<ApiResult<{ idPago: number }>> {
  return apiPost(page, "/pagos", {
    idGestion: gestionId,
    monto: 5000,
    fecha: new Date().toISOString().split("T")[0],
    formaPago: "Efectivo",
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
): Promise<ApiResult<{ idEstadoGestion: number }>> {
  const id = uniqueId();
  return apiPost(page, "/estado-gestion", {
    nombre: `Estado E2E ${id}`,
    descripcion: "Created by E2E test",
    ...overrides,
  });
}

export async function createTipoDocumento(
  page: Page,
  overrides: { nombre?: string } = {},
): Promise<ApiResult<{ idTipoDeDocumento: number }>> {
  const id = uniqueId();
  return apiPost(page, "/tipo-de-documento", {
    nombre: `Tipo Documento E2E ${id}`,
    descripcion: "Created by E2E test",
    ...overrides,
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
