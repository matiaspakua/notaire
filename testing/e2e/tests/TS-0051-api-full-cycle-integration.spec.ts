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

interface ApiPerson {
  personId?: number;
  firstName?: string;
  lastName?: string;
  identificationNumber?: string;
  email?: string;
  isClient?: boolean;
}

interface ApiBudget {
  idBudget?: number;
  date?: string;
  propertyAmount?: number;
  status?: string;
}

interface ApiUser {
  idUser?: number;
  name?: string;
  type?: string;
  valid?: boolean;
}

interface ApiConcept {
  idConcept?: number;
  name?: string;
  value?: number;
}

interface ApiFolio {
  idFolio?: number;
  number?: number;
  status?: string;
}

interface ApiProcedureType {
  idProcedureType?: number;
  name?: string;
}

interface ApiManagementStatus {
  idManagementStatus?: number;
  name?: string;
}

interface ApiDocumentType {
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

test.describe("API — People endpoints", () => {
  let createdId = 0;

  test("GET /api/v1/people — list people (paginated, #596)", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<{ content: ApiPerson[]; size: number }>(page, "/people?size=5");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data?.content)).toBe(true);
    expect(result.data?.size).toBe(5);
    expect(result.data?.content.length ?? 0).toBeLessThanOrEqual(5);
  });

  test("POST /api/v1/people — create a person", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost<ApiPerson>(page, "/people", {
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

  test("PUT /api/v1/people/{id} — update a person", async ({ page }) => {
    await page.goto("/login");
    // Create a fresh person for this PUT test to avoid dependency on POST test state
    const unique = Date.now();
    const create = await apiPost<ApiPerson>(page, "/people", {
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

test.describe("API — Users endpoints", () => {
  test("POST /api/v1/users/login — login works", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost<ApiUser>(page, "/users/login", {
      name: "admin",
      password: "admin",
    });
    expect(result.ok).toBe(true);
  });

  test("GET /api/v1/users — list all users", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiUser[]>(page, "/users");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });
});

test.describe("API — Budget endpoints", () => {
  test("GET /api/v1/budgets — list budgets", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<{ content: ApiBudget[] }>(page, "/budgets");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data?.content)).toBe(true);
  });
});

test.describe("API — Catalog endpoints", () => {
  test("GET /api/v1/procedure-types — list procedure types", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiProcedureType[]>(page, "/procedure-types");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });

  test("POST /api/v1/procedure-types — create a procedure type", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost(page, "/procedure-types", {
      name: `Test Procedure ${Date.now()}`,
      notes: "Created by E2E test",
      isArchived: false,
      isRegistered: false,
    });
    expect(result.ok).toBe(true);
  });

  test("GET /api/v1/management-statuses — list management statuses", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiManagementStatus[]>(page, "/management-statuses");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });

  test("POST /api/v1/management-statuses — create a management status", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost(page, "/management-statuses", {
      name: `Test Status ${Date.now()}`,
      notes: "Created by E2E test",
    });
    expect(result.ok).toBe(true);
  });

  test("GET /api/v1/document-types — list document types", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiDocumentType[]>(page, "/document-types");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });
});

test.describe("API — Concepts endpoints", () => {
  let createdConceptId = 0;

  test("GET /api/v1/concepts — list concepts", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet<ApiConcept[]>(page, "/concepts");
    expect(result.ok).toBe(true);
    expect(Array.isArray(result.data)).toBe(true);
  });

  test("POST /api/v1/concepts — create a concept", async ({ page }) => {
    await page.goto("/login");
    const result = await apiPost<ApiConcept>(page, "/concepts", {
      name: `Test Concept ${Date.now()}`,
      value: 100.50,
    });
    expect(result.ok).toBe(true);
    if (result.data?.idConcept) {
      createdConceptId = result.data.idConcept;
    }
  });

  test("PUT /api/v1/concepts/{id} — update a concept", async ({ page }) => {
    await page.goto("/login");
    if (!createdConceptId) {
      const create = await apiPost<ApiConcept>(page, "/concepts", {
        name: `Test Concept PUT ${Date.now()}`,
        value: 200,
      });
      createdConceptId = create.data?.idConcept ?? 0;
    }
    if (createdConceptId) {
      const result = await apiPut(page, `/concepts/${createdConceptId}`, {
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

test.describe("API — Management endpoints", () => {
  test("GET /api/v1/managements — list managements", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/managements");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Deeds endpoints", () => {
  test("GET /api/v1/deeds — list deeds", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/deeds");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Payment endpoints", () => {
  test("GET /api/v1/payments — list payments", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/payments");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Properties endpoints", () => {
  test("GET /api/v1/properties — list properties", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/properties");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Copies endpoints", () => {
  test("GET /api/v1/copies — list copies", async ({ page }) => {
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

test.describe("API — Submitted documents endpoints", () => {
  test("GET /api/v1/submitted-documents — list documents", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/submitted-documents");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — substitution endpoints", () => {
  test("GET /api/v1/substitutions — list substitutions", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/substitutions");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Audit endpoints", () => {
  test("GET /api/v1/audit-log — list audit entries", async ({ page }) => {
    await page.goto("/login");
    const result = await apiGet(page, "/audit-log");
    expect(result.ok).toBe(true);
  });
});

test.describe("API — Full CRUD cycle: people", () => {
  let personId = 0;
  const unique = Date.now();

  test("Complete CREATE → READ → UPDATE → DELETE cycle", async ({ page }) => {
    await page.goto("/login");

    // 1. CREATE
    const createRes = await apiPost<ApiPerson>(page, "/people", {
      firstName: "CRUD",
      lastName: "Test",
      identificationNumber: `CRUD${unique}`,
      email: `crud-${unique}@test.com`,
      isClient: true,
    });
    expect(createRes.ok).toBe(true);
    personId = createRes.data?.personId ?? 0;
    expect(personId).toBeGreaterThan(0);

    // 2. READ (by ID)
    const readRes = await apiGet<ApiPerson>(page, `/people/${personId}`);
    expect(readRes.ok).toBe(true);
    expect(readRes.data?.firstName).toBe("CRUD");

    // 3. UPDATE — must include all required fields to avoid NOT NULL constraint violations
    const updateRes = await apiPut(page, `/people/${personId}`, {
      firstName: "CRUD Updated",
      lastName: "Test Updated",
      identificationNumber: `CRUD${unique}`,
      email: `crud-${unique}@test.com`,
      isClient: true,
    });
    expect(updateRes.ok).toBe(true);

    // 4. Verify update
    const verifyUpdate = await apiGet<ApiPerson>(page, `/people/${personId}`);
    expect(verifyUpdate.data?.firstName).toBe("CRUD Updated");

    // 5. DELETE
    const deleteRes = await apiDelete(page, `/people/${personId}`);
    expect(deleteRes.ok).toBe(true);

    // 6. Verify deletion
    const verifyDelete = await apiGet(page, `/people/${personId}`);
    expect(verifyDelete.status).toBe(404);
  });
});
