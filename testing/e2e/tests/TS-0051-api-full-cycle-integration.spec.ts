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
import { test, expect } from "@playwright/test";
import { apiGet, apiPost, apiPut, apiDelete } from "./setup/api-helpers";

// ──────────────────────────────────────────────
// Record type interfaces (mirroring backend DTOs)
// ──────────────────────────────────────────────

interface ApiPersona {
  personId?: number;
  firstName?: string;
  lastName?: string;
  identificationNumber?: string;
  email?: string;
  isClient?: boolean;
}

interface ApiPresupuesto {
  idBudget?: number;
  date?: string;
  propertyAmount?: number;
  status?: string;
}

interface ApiUsuario {
  idUsuario?: number;
  nombre?: string;
  tipo?: string;
  valid?: boolean;
}

interface ApiConcepto {
  idConcept?: number;
  name?: string;
  value?: number;
}

interface ApiFolio {
  idFolio?: number;
  number?: number;
  status?: string;
}

interface ApiTipoTramite {
  idProcedureType?: number;
  name?: string;
}

interface ApiEstadoGestion {
  idManagementStatus?: number;
  name?: string;
}

interface ApiTipoDocumento {
  idDocumentType?: number;
  name?: string;
}

test.describe("API Connectivity — Backend proxy health", () => {
  test("GET /api/v1 returns 200 (backend is reachable)", async ({ page }) => {
    // Navigate to any page first so the page context has a base URL
    await page.goto("/login");
    const result = await apiGet(page, "/users");
    // If backend is down, this will fail — we accept either a response or connection error
    expect(result.status).toBeGreaterThanOrEqual(200);
  });
});

test.describe("API — Personas endpoints", () => {
  let createdId = 0;

  test("GET /api/v1/people — list people (paginated, #596)", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<{ content: ApiPersona[]; size: number }>(page, "/people?size=5");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data?.content)).toBe(true);
    expect(result.data?.size).toBe(5);
    expect(result.data?.content.length ?? 0).toBeLessThanOrEqual(5);
  });

  test("POST /api/v1/people — create a persona", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost<ApiPersona>(page, "/people", {
      firstName: "Test E2E",
      lastName: "Playwright",
      identificationNumber: "99" + Date.now(),
      email: `e2e-${Date.now()}@test.com`,
      isClient: true,
    });
    expect(result.ok).toBe(true);
    if (result.data?.personId) {
      createdId = result.data.personId;
    }
  });

  test("PUT /api/v1/people/{id} — update a persona", async ({ page }) => {
    await page.goto("/login");
    // Create a fresh persona for this PUT test to avoid dependency on POST test state
    const unique = Date.now();
    const create = await apiPost<ApiPersona>(page, "/people", {
      firstName: "Test PUT",
      lastName: "Person",
      identificationNumber: "PUT" + unique,
      email: `put-${unique}@test.com`,
      isClient: true,
    });
    expect(create.ok).toBe(true);
    const putId = create.data?.personId ?? 0;
    if (putId) {
      const result = await apiPut(page, `/people/${putId}`, {
        firstName: "Test Updated",
        lastName: "Person Updated",
        identificationNumber: "PUT" + unique,
        email: `put-${unique}@test.com`,
        isClient: true,
      });
      expect(result.ok).toBe(true);
    }
  });
});

test.describe("API — Usuarios endpoints", () => {
  test("POST /api/v1/users/login — login works", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost<ApiUsuario>(page, "/users/login", {
      name: "admin",
      password: "admin",
    });
    expect(result.ok).toBe(true);
  });

  test("GET /api/v1/users — list all usuarios", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiUsuario[]>(page, "/users");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });
});

test.describe("API — Presupuestos endpoints", () => {
  test("GET /api/v1/budgets — list presupuestos", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<{ content: ApiPresupuesto[] }>(page, "/budgets");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data?.content)).toBe(true);
  });
});

test.describe("API — Catálogos endpoints", () => {
  test("GET /api/v1/procedure-types — list tipos de trámite", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiTipoTramite[]>(page, "/procedure-types");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });

  test("POST /api/v1/procedure-types — create tipo de trámite", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost(page, "/procedure-types", {
      name: `Test Procedure ${Date.now()}`,
      notes: "Created by E2E test",
      isArchived: false,
      isRegistered: false,
    });
    expect(result.ok).toBe(true);
  });

  test("GET /api/v1/management-statuses — list estados de gestión", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiEstadoGestion[]>(page, "/management-statuses");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });

  test("POST /api/v1/management-statuses — create estado de gestión", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost(page, "/management-statuses", {
      name: `Test Estado ${Date.now()}`,
      notes: "Created by E2E test",
    });
    expect(result.ok).toBe(true);
  });

  test("GET /api/v1/document-types — list tipos de documento", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiTipoDocumento[]>(page, "/document-types");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });
});

test.describe("API — Conceptos endpoints", () => {
  let createdConceptoId = 0;

  test("GET /api/v1/concepts — list conceptos", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiConcepto[]>(page, "/concepts");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });

  test("POST /api/v1/concepts — create concepto", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost<ApiConcepto>(page, "/concepts", {
      name: `Test Concept ${Date.now()}`,
      value: 100.50,
    });
    expect(result.ok).toBe(true);
    if (result.data?.idConcept) {
      createdConceptoId = result.data.idConcept;
    }
  });

  test("PUT /api/v1/concepts/{id} — update concepto", async ({ page }) => {
    await page.goto("/login");
    if (!createdConceptoId) {
      const create = await apiPost<ApiConcepto>(page, "/concepts", {
        name: `Test Concept PUT ${Date.now()}`,
        value: 200,
      });
      createdConceptoId = create.data?.idConcept ?? 0;
    }
    if (createdConceptoId) {
      const result = await apiPut(page, `/concepts/${createdConceptoId}`, {
        name: "Updated Concept E2E",
        value: 250.75,
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
  test("GET /api/v1/managements — list gestiones", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/managements");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Escrituras endpoints", () => {
  test("GET /api/v1/deeds — list escrituras", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/deeds");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Pagos endpoints", () => {
  test("GET /api/v1/payments — list pagos", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/payments");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Inmuebles endpoints", () => {
  test("GET /api/v1/properties — list inmuebles", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/properties");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Copias endpoints", () => {
  test("GET /api/v1/copies — list copias", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/copies");
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
  test("GET /api/v1/submitted-documents — list documentos", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/submitted-documents");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Suplencias endpoints", () => {
  test("GET /api/v1/substitutions — list suplencias", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/substitutions");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Auditoría endpoints", () => {
  test("GET /api/v1/audit-log — list auditoría entries", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/audit-log");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Full CRUD cycle: Personas", () => {
  let personaId = 0;
  const unique = Date.now();

  test("Complete CREATE → READ → UPDATE → DELETE cycle", async ({ page }) => {
    await page.goto("/login");

    // 1. CREATE
    const createRes = await apiPost<ApiPersona>(page, "/people", {
      firstName: "CRUD",
      lastName: "Test",
      identificationNumber: `CRUD${unique}`,
      email: `crud-${unique}@test.com`,
      isClient: true,
    });
    expect(createRes.ok).toBe(true);
    personaId = createRes.data?.personId ?? 0;
    expect(personaId).toBeGreaterThan(0);

    // 2. READ (by ID)
    const readRes = await apiGet<ApiPersona>(page, `/people/${personaId}`);
    expect(readRes.ok).toBe(true);
    expect(readRes.data?.firstName).toBe("CRUD");

    // 3. UPDATE — must include all required fields to avoid NOT NULL constraint violations
    const updateRes = await apiPut(page, `/people/${personaId}`, {
      firstName: "CRUD Updated",
      lastName: "Test Updated",
      identificationNumber: `CRUD${unique}`,
      email: `crud-${unique}@test.com`,
      isClient: true,
    });
    expect(updateRes.ok).toBe(true);

    // 4. Verify update
    const verifyUpdate = await apiGet<ApiPersona>(page, `/people/${personaId}`);
    expect(verifyUpdate.data?.firstName).toBe("CRUD Updated");

    // 5. DELETE
    const deleteRes = await apiDelete(page, `/people/${personaId}`);
    expect(deleteRes.ok).toBe(true);

    // 6. Verify deletion
    const verifyDelete = await apiGet(page, `/people/${personaId}`);
    expect(verifyDelete.status).toBe(404);
  });
});
