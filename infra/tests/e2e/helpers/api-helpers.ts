/**
 * API verification helpers for full-stack E2E tests.
 *
 * These helpers call the backend API via the Next.js proxy (/api/v1/*)
 * to verify that data created through the UI was persisted correctly.
 */
import { type Page, expect } from "@playwright/test";
import type { Concepto, TipoDeDocumento, EstadoDeGestion, Folio, Persona, Usuario, TipoDeTramite, PlantillaPresupuesto, Presupuesto } from "./test-data";

// ======================= Generic API helpers =======================

export async function apiGet<T>(page: Page, path: string): Promise<{ ok: boolean; status: number; data?: T; error?: string }> {
  const response = await page.request.get(`/api/v1${path}`);
  const ok = response.ok();
  let data: T | undefined;
  let error: string | undefined;
  try {
    if (ok) {
      const text = await response.text();
      if (text) data = JSON.parse(text) as T;
    } else {
      error = await response.text();
    }
  } catch {
    error = "Failed to parse response";
  }
  return { ok, status: response.status(), data, error };
}

export async function apiPost<T = void>(page: Page, path: string, body: unknown): Promise<{ ok: boolean; status: number; data?: T; error?: string }> {
  const response = await page.request.post(`/api/v1${path}`, {
    data: JSON.stringify(body),
    headers: { "Content-Type": "application/json" },
  });
  const ok = response.ok();
  let data: T | undefined;
  let error: string | undefined;
  try {
    if (ok) {
      const text = await response.text();
      if (text) data = JSON.parse(text) as T;
    } else {
      error = await response.text();
    }
  } catch {
    error = "Failed to parse response";
  }
  return { ok, status: response.status(), data, error };
}

export async function apiPut<T = void>(page: Page, path: string, body: unknown): Promise<{ ok: boolean; status: number; data?: T; error?: string }> {
  const response = await page.request.put(`/api/v1${path}`, {
    data: JSON.stringify(body),
    headers: { "Content-Type": "application/json" },
  });
  const ok = response.ok();
  let data: T | undefined;
  let error: string | undefined;
  try {
    if (ok) {
      const text = await response.text();
      if (text) data = JSON.parse(text) as T;
    } else {
      error = await response.text();
    }
  } catch {
    error = "Failed to parse response";
  }
  return { ok, status: response.status(), data, error };
}

export async function apiDelete(page: Page, path: string): Promise<{ ok: boolean; status: number; error?: string }> {
  const response = await page.request.delete(`/api/v1${path}`);
  const ok = response.ok();
  let error: string | undefined;
  try {
    if (!ok) error = await response.text();
  } catch {
    error = "Failed to parse response";
  }
  return { ok, status: response.status(), error };
}

// ======================= Specific verification helpers =======================

/** Verify a Concepto was created with the expected name */
export async function verifyConceptoCreado(page: Page, expectedNombre: string): Promise<number> {
  const { ok, data, error } = await apiGet<Concepto[]>(page, "/conceptos");
  expect(ok, `GET /api/v1/conceptos failed: ${error}`).toBe(true);
  const found = data?.find((c) => c.nombre?.trim() === expectedNombre.trim());
  expect(found, `Concepto "${expectedNombre}" not found in API response`).toBeDefined();
  return found!.idConcepto!;
}

/** Verify a TipoDeDocumento was created with the expected name */
export async function verifyTipoDocumentoCreado(page: Page, expectedNombre: string): Promise<number> {
  const { ok, data, error } = await apiGet<TipoDeDocumento[]>(page, "/tipo-de-documento");
  expect(ok, `GET /api/v1/tipo-de-documento failed: ${error}`).toBe(true);
  const found = data?.find((t) => t.nombre?.trim() === expectedNombre.trim());
  expect(found, `Tipo de documento "${expectedNombre}" not found in API response`).toBeDefined();
  return found!.idTipoDeDocumento!;
}

/** Verify an EstadoDeGestion was created with the expected name */
export async function verifyEstadoGestionCreado(page: Page, expectedNombre: string): Promise<number> {
  const { ok, data, error } = await apiGet<EstadoDeGestion[]>(page, "/estado-gestion");
  expect(ok, `GET /api/v1/estado-gestion failed: ${error}`).toBe(true);
  const found = data?.find((e) => e.nombre?.trim() === expectedNombre.trim());
  expect(found, `Estado de gestion "${expectedNombre}" not found in API response`).toBeDefined();
  return found!.idEstadoDeGestion!;
}

/** Verify a Folio was created with the expected numero */
export async function verifyFolioCreado(page: Page, expectedNumero: number): Promise<number> {
  const { ok, data, error } = await apiGet<Folio[]>(page, "/folio");
  expect(ok, `GET /api/v1/folio failed: ${error}`).toBe(true);
  const found = data?.find((f) => f.numero === expectedNumero);
  expect(found, `Folio #${expectedNumero} not found in API response`).toBeDefined();
  return found!.idFolio!;
}

/** Verify a Persona was created with the expected nombre+apellido */
export async function verifyPersonaCreada(page: Page, expectedNombre: string, expectedApellido: string): Promise<number> {
  const { ok, data, error } = await apiGet<Persona[]>(page, "/personas");
  expect(ok, `GET /api/v1/personas failed: ${error}`).toBe(true);
  const found = data?.find(
    (p) => p.nombre?.trim() === expectedNombre.trim() && p.apellido?.trim() === expectedApellido.trim()
  );
  expect(found, `Persona "${expectedNombre} ${expectedApellido}" not found in API response`).toBeDefined();
  return found!.idPersona!;
}

/** Verify a Usuario was created with the expected username */
export async function verifyUsuarioCreado(page: Page, expectedNombre: string): Promise<number> {
  const { ok, data, error } = await apiGet<Usuario[]>(page, "/usuarios");
  expect(ok, `GET /api/v1/usuarios failed: ${error}`).toBe(true);
  const found = data?.find((u) => u.nombre?.trim() === expectedNombre.trim());
  expect(found, `Usuario "${expectedNombre}" not found in API response`).toBeDefined();
  return found!.idUsuario!;
}

/** Verify a TipoDeTramite was created with the expected name */
export async function verifyTipoTramiteCreado(page: Page, expectedNombre: string): Promise<number> {
  const { ok, data, error } = await apiGet<TipoDeTramite[]>(page, "/tipo-tramite");
  expect(ok, `GET /api/v1/tipo-tramite failed: ${error}`).toBe(true);
  const found = data?.find((t) => t.nombre?.trim() === expectedNombre.trim());
  expect(found, `Tipo de tramite "${expectedNombre}" not found in API response`).toBeDefined();
  return found!.idTipoDeTramite!;
}

/** Verify a PlantillaPresupuesto was created */
export async function verifyPlantillaCreada(page: Page, expectedNombre: string): Promise<number> {
  const { ok, data, error } = await apiGet<PlantillaPresupuesto[]>(page, "/plantilla-presupuestos");
  expect(ok, `GET /api/v1/plantilla-presupuestos failed: ${error}`).toBe(true);
  const found = data?.find((p) => p.nombre?.trim() === expectedNombre.trim());
  expect(found, `Plantilla "${expectedNombre}" not found in API response`).toBeDefined();
  return found!.idPlantillaPresupuesto!;
}

/** Verify a Presupuesto was created */
export async function verifyPresupuestoCreado(page: Page, expectedMonto?: number): Promise<number> {
  const { ok, data, error } = await apiGet<Presupuesto[]>(page, "/presupuestos");
  expect(ok, `GET /api/v1/presupuestos failed: ${error}`).toBe(true);
  if (expectedMonto !== undefined) {
    const found = data?.find((p) => p.monto === expectedMonto);
    expect(found, `Presupuesto with monto ${expectedMonto} not found in API response`).toBeDefined();
    return found!.idPresupuesto!;
  }
  // Return the latest one
  expect(data?.length, "No presupuestos found in API response").toBeGreaterThan(0);
  return data![data!.length - 1].idPresupuesto!;
}

/** Clean up a created entity by ID */
export async function eliminarConcepto(page: Page, id: number): Promise<boolean> {
  const { ok } = await apiDelete(page, `/conceptos/${id}`);
  return ok;
}

export async function eliminarPersona(page: Page, id: number): Promise<boolean> {
  const { ok } = await apiDelete(page, `/personas/${id}`);
  return ok;
}

export async function eliminarUsuario(page: Page, id: number): Promise<boolean> {
  const { ok } = await apiDelete(page, `/usuarios/${id}`);
  return ok;
}

export async function eliminarPresupuesto(page: Page, id: number): Promise<boolean> {
  const { ok } = await apiDelete(page, `/presupuestos/${id}`);
  return ok;
}
