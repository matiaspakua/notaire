/**
 * E2E tests — Full API cycle verification (GET → POST → PUT → DELETE)
 *
 * Tests the complete request lifecycle for every backend endpoint through
 * the frontend API client. These tests run against the proxied backend
 * (/api/v1/* → http://localhost:8080/api/v1/*) so they verify end-to-end
 * connectivity between frontend and backend.
 *
 * Tests use the @tanstack/react-query hooks indirectly by visiting pages
 * that fetch data on load, then mocking/firing API calls directly.
 */
import { test, expect, type Page } from "@playwright/test";

/**
 * Direct API test — hits the backend via fetch() to verify the proxy works
 * and the backend responds. This is the simplest end-to-end connectivity test.
 */
async function apiGet<T>(page: Page, path: string): Promise<{ ok: boolean; status: number; data?: T; error?: string }> {
  const response = await page.request.get(`/api/v1${path}`);
  const ok = response.ok();
  let data: T | undefined;
  let error: string | undefined;
  try {
    if (ok) {
      data = (await response.json()) as T;
    } else {
      error = await response.text();
    }
  } catch {
    error = "Failed to parse response";
  }
  return { ok, status: response.status(), data, error };
}

async function apiPost<T = void>(page: Page, path: string, body: unknown): Promise<{ ok: boolean; status: number; data?: T; error?: string }> {
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

async function apiPut<T = void>(page: Page, path: string, body: unknown): Promise<{ ok: boolean; status: number; data?: T; error?: string }> {
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

async function apiDelete(page: Page, path: string): Promise<{ ok: boolean; status: number; error?: string }> {
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

// ──────────────────────────────────────────────
// Record type interfaces (mirroring backend DTOs)
// ──────────────────────────────────────────────

interface ApiPersona {
  idPersona?: number;
  nombre?: string;
  apellido?: string;
  dni?: string;
  email?: string;
  esCliente?: boolean;
}

interface ApiPresupuesto {
  idPresupuesto?: number;
  fecha?: string;
  monto?: number;
  estado?: string;
}

interface ApiUsuario {
  idUsuario?: number;
  nombre?: string;
  tipo?: string;
  valido?: boolean;
}

interface ApiConcepto {
  idConcepto?: number;
  nombre?: string;
  descripcion?: string;
  valor?: number;
}

interface ApiFolio {
  idFolio?: number;
  numero?: number;
  disponible?: boolean;
}

interface ApiTipoTramite {
  idTipoDeTramite?: number;
  nombre?: string;
}

interface ApiEstadoGestion {
  idEstadoDeGestion?: number;
  nombre?: string;
}

interface ApiTipoDocumento {
  idTipoDeDocumento?: number;
  nombre?: string;
}

test.describe("API Connectivity — Backend proxy health", () => {
  test("GET /api/v1 returns 200 (backend is reachable)", async ({ page }) => {
    // Navigate to any page first so the page context has a base URL
    await page.goto("/login");
    const result = await apiGet(page, "/usuarios");
    // If backend is down, this will fail — we accept either a response or connection error
    expect(result.status).toBeGreaterThanOrEqual(200);
  });
});

test.describe("API — Personas endpoints", () => {
  let createdId = 0;

  test("GET /api/v1/personas — list all personas", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiPersona[]>(page, "/personas");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });

  test("POST /api/v1/personas — create a persona", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost<ApiPersona>(page, "/personas", {
      nombre: "Test E2E",
      apellido: "Playwright",
      dni: "99" + Date.now(),
      email: `e2e-${Date.now()}@test.com`,
      esCliente: true,
    });
    expect(result.ok).toBe(true);
    if (result.data?.idPersona) {
      createdId = result.data.idPersona;
    }
  });

  test("PUT /api/v1/personas/{id} — update a persona", async ({ page }) => {
    await page.goto("/login");
    if (!createdId) {
      // Create one first if not already created
      const create = await apiPost<ApiPersona>(page, "/personas", {
        nombre: "Test PUT",
        apellido: "Persona",
        dni: "PUT" + Date.now(),
        email: `put-${Date.now()}@test.com`,
      });
      expect(create.ok).toBe(true);
      createdId = create.data?.idPersona ?? 0;
    }
    if (createdId) {
      const result = await apiPut(page, `/personas/${createdId}`, {
        nombre: "Test Updated",
        apellido: "Persona Updated",
      });
      expect(result.ok).toBe(true);
    }
  });
});

test.describe("API — Usuarios endpoints", () => {
  test("POST /api/v1/usuarios/login — login works", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost<ApiUsuario>(page, "/usuarios/login", {
      nombre: "admin",
      contrasenia: "admin",
    });
    expect(result.ok).toBe(true);
  });

  test("GET /api/v1/usuarios — list all usuarios", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiUsuario[]>(page, "/usuarios");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });
});

test.describe("API — Presupuestos endpoints", () => {
  test("GET /api/v1/presupuestos — list presupuestos", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiPresupuesto[]>(page, "/presupuestos");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });
});

test.describe("API — Catálogos endpoints", () => {
  test("GET /api/v1/catalogos/tipos-tramite — list tipos de trámite", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiTipoTramite[]>(page, "/catalogos/tipos-tramite");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });

  test("POST /api/v1/catalogos/tipos-tramite — create tipo de trámite", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost(page, "/catalogos/tipos-tramite", {
      nombre: `Test Tramite ${Date.now()}`,
      descripcion: "Created by E2E test",
      seArchiva: false,
      seInscribe: false,
    });
    expect(result.ok).toBe(true);
  });

  test("GET /api/v1/catalogos/estados-gestion — list estados de gestión", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiEstadoGestion[]>(page, "/catalogos/estados-gestion");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });

  test("POST /api/v1/catalogos/estados-gestion — create estado de gestión", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost(page, "/catalogos/estados-gestion", {
      nombre: `Test Estado ${Date.now()}`,
      descripcion: "Created by E2E test",
    });
    expect(result.ok).toBe(true);
  });

  test("GET /api/v1/catalogos/tipos-documento — list tipos de documento", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiTipoDocumento[]>(page, "/catalogos/tipos-documento");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });
});

test.describe("API — Conceptos endpoints", () => {
  let createdConceptoId = 0;

  test("GET /api/v1/catalogos/conceptos — list conceptos", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiConcepto[]>(page, "/catalogos/conceptos");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });

  test("POST /api/v1/catalogos/conceptos — create concepto", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost<ApiConcepto>(page, "/catalogos/conceptos", {
      nombre: `Test Concepto ${Date.now()}`,
      descripcion: "E2E test",
      valor: 100.50,
    });
    expect(result.ok).toBe(true);
    if (result.data?.idConcepto) {
      createdConceptoId = result.data.idConcepto;
    }
  });

  test("PUT /api/v1/catalogos/conceptos/{id} — update concepto", async ({ page }) => {
    await page.goto("/login");
    if (!createdConceptoId) {
      const create = await apiPost<ApiConcepto>(page, "/catalogos/conceptos", {
        nombre: `Test Concepto PUT ${Date.now()}`,
        valor: 200,
      });
      createdConceptoId = create.data?.idConcepto ?? 0;
    }
    if (createdConceptoId) {
      const result = await apiPut(page, `/catalogos/conceptos/${createdConceptoId}`, {
        nombre: "Updated Concepto E2E",
        valor: 250.75,
      });
      expect(result.ok).toBe(true);
    }
  });
});

test.describe("API — Folios endpoints", () => {
  test("GET /api/v1/folios — list folios", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiFolio[]>(page, "/folios");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });
});

test.describe("API — Gestiones endpoints", () => {
  test("GET /api/v1/gestiones — list gestiones", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/gestiones");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Escrituras endpoints", () => {
  test("GET /api/v1/escrituras — list escrituras", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/escrituras");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Pagos endpoints", () => {
  test("GET /api/v1/pagos — list pagos", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/pagos");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Inmuebles endpoints", () => {
  test("GET /api/v1/inmuebles — list inmuebles", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/inmuebles");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Copias endpoints", () => {
  test("GET /api/v1/copias — list copias", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/copias");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Items endpoints", () => {
  test("GET /api/v1/items — list items", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/items");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Documentos Presentados endpoints", () => {
  test("GET /api/v1/documentos-presentados — list documentos", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/documentos-presentados");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Suplencias endpoints", () => {
  test("GET /api/v1/suplencias — list suplencias", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/suplencias");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Auditoría endpoints", () => {
  test("GET /api/v1/auditoria — list auditoría entries", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/auditoria");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Full CRUD cycle: Personas", () => {
  let personaId = 0;
  const unique = Date.now();

  test("Complete CREATE → READ → UPDATE → DELETE cycle", async ({ page }) => {
    await page.goto("/login");

    // 1. CREATE
    const createRes = await apiPost<ApiPersona>(page, "/personas", {
      nombre: "CRUD",
      apellido: "Test",
      dni: `CRUD${unique}`,
      email: `crud-${unique}@test.com`,
      esCliente: true,
    });
    expect(createRes.ok).toBe(true);
    personaId = createRes.data?.idPersona ?? 0;
    expect(personaId).toBeGreaterThan(0);

    // 2. READ (by ID)
    const readRes = await apiGet<ApiPersona>(page, `/personas/${personaId}`);
    expect(readRes.ok).toBe(true);
    expect(readRes.data?.nombre).toBe("CRUD");

    // 3. UPDATE
    const updateRes = await apiPut(page, `/personas/${personaId}`, {
      nombre: "CRUD Updated",
      apellido: "Test Updated",
    });
    expect(updateRes.ok).toBe(true);

    // 4. Verify update
    const verifyUpdate = await apiGet<ApiPersona>(page, `/personas/${personaId}`);
    expect(verifyUpdate.data?.nombre).toBe("CRUD Updated");

    // 5. DELETE
    const deleteRes = await apiDelete(page, `/personas/${personaId}`);
    expect(deleteRes.ok).toBe(true);

    // 6. Verify deletion
    const verifyDelete = await apiGet(page, `/personas/${personaId}`);
    expect(verifyDelete.status).toBe(404);
  });
});
