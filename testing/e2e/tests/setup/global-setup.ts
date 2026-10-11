/**
 * Global setup for Playwright E2E tests
 *
 * Responsibilities:
 *  1. Authenticate as admin and store auth cookie/localStorage
 *  2. Seed base catalog data via API (tipos de trámite, concepts, etc.)
 *  3. Export shared test data as environment variables
 *  4. Verify backend and frontend are reachable
 *
 * This runs once before ALL test suites.
 */
import fs from "node:fs";
import { chromium, type Browser, type Page } from "@playwright/test";
import { apiPost, apiGet, apiPut, apiDelete } from "./api-helpers";

interface SeedData {
  testId: number;
  adminAuth: { token: string; user: any } | null;
  seedPersonId: number | null;
  seedBudgetId: number | null;
  seedManagementId: number | null;
  seedDeedId: number | null;
  seedConceptId: number | null;
  seedTypeProcedureId: number | null;
  seedStatusManagementId: number | null;
  seedTypeDocumentId: number | null;
  seedUsuarioId: number | null;
  seedFolioId: number | null;
  seedSubstitutionId: number | null;
  seedPagoId: number | null;
  seedTestimonyId: number | null;
}

// Global seed data accessible by tests via process.env
export const seedData: SeedData = {
  testId: Date.now(),
  adminAuth: null,
  seedPersonId: null,
  seedBudgetId: null,
  seedManagementId: null,
  seedDeedId: null,
  seedConceptId: null,
  seedTypeProcedureId: null,
  seedStatusManagementId: null,
  seedTypeDocumentId: null,
  seedUsuarioId: null,
  seedFolioId: null,
  seedSubstitutionId: null,
  seedPagoId: null,
  seedTestimonyId: null,
};

/**
 * Authenticate as admin and store auth state for all tests
 */
async function authenticateAdmin(page: Page): Promise<void> {
  await page.goto("/login");
  await page.waitForLoadState("networkidle");

  // Direct API login. Response is a flat map (valid, token, idUser, name, ...) —
  // see UserController#login.
  const loginResult = await apiPost<{
    valid: boolean;
    token: string;
    idUser: number;
    name: string;
    type: string;
  }>(page, "/users/login", { name: "admin", password: "admin" });

  if (loginResult.ok && loginResult.data?.valid && loginResult.data.token) {
    const { token, idUser, name, type } = loginResult.data;
    // HttpOnly JWT cookie for browser proxy auth + UX cookies (#1051 / #1052).
    await page.context().addCookies([
      {
        name: "notaire-auth-token",
        value: token,
        domain: "localhost",
        path: "/",
        httpOnly: true,
        sameSite: "Lax",
      },
      { name: "notaire-auth-status", value: "authenticated", domain: "localhost", path: "/" },
      { name: "notaire-auth-role", value: (type ?? "ADMIN").toUpperCase(), domain: "localhost", path: "/" },
    ]);
    // Persist client profile only — never the JWT in localStorage (#1051).
    // Only hydrate when the UX status cookie is present (same as setup/auth.ts).
    await page.addInitScript(
      ([user]) => {
        if (!document.cookie.split(";").some((c) => c.trim().startsWith("notaire-auth-status="))) {
          return;
        }
        localStorage.setItem(
          "notaire-auth",
          JSON.stringify({
            state: { user, isAuthenticated: true },
            version: 0,
          })
        );
      },
      [{ name, type, valid: true, idUser }] as const
    );
    seedData.adminAuth = { token, user: { idUser, name, type } };
    // Visible to every worker process spawned after global-setup completes — read by
    // tests/setup/api-helpers.ts so page.request-based seed/cleanup calls authenticate.
    process.env.E2E_ADMIN_TOKEN = token;
  } else {
    // Fallback: UI-based login (Set-Cookie establishes HttpOnly JWT).
    await page.getByTestId("input-username").fill("admin");
    await page.getByTestId("input-password").fill("admin");
    await page.getByTestId("btn-sign-in").click();
    await page.waitForURL(/\/dashboard/, { timeout: 15000 });

    const cookies = await page.context().cookies();
    const authCookie = cookies.find((c) => c.name === "notaire-auth-token");
    if (authCookie?.value) {
      process.env.E2E_ADMIN_TOKEN = authCookie.value;
    }
  }
}

/**
 * Seed catalog data needed by most tests
 */
async function seedCatalogData(page: Page): Promise<void> {
  // 1. Create a tipo de trámite — ProcedureTypeController accepts a DtoProcedureType
  // (name, notes, isArchived, isRegistered, associatesProperties).
  const ttResult = await apiPost<{ idProcedureType: number; name: string }>(
    page,
    "/procedure-types",
    {
      name: `Test Procedure ${seedData.testId}`,
      notes: "Seeded by global-setup for E2E tests",
      isArchived: false,
      isRegistered: false,
    }
  );
  if (ttResult.ok && ttResult.data?.idProcedureType) {
    seedData.seedTypeProcedureId = ttResult.data.idProcedureType;
  }

  // 2. Create a concept — ConceptController accepts a DtoConcept (name, value, percentage).
  const concResult = await apiPost<{ idConcept: number; name: string }>(
    page,
    "/concepts",
    {
      name: `Test Concept ${seedData.testId}`,
      value: 1000.0,
    }
  );
  if (concResult.ok && concResult.data?.idConcept) {
    seedData.seedConceptId = concResult.data.idConcept;
  }

  // 3. Create a person (client) — PersonController accepts the raw Person entity.
  const persResult = await apiPost<{ personId: number; firstName: string; lastName: string }>(
    page,
    "/people",
    {
      firstName: "Seed",
      lastName: `Person-${seedData.testId}`,
      identificationNumber: `SEED${seedData.testId}`,
      email: `seed-${seedData.testId}@notaire.test`,
      isClient: true,
      nationality: "Argentina",
      birthDate: "1990-01-01",
      taxId: `20-${String(seedData.testId).padStart(8, "0")}-9`,
      maritalStatus: "Soltero",
      sex: "Masculino",
    }
  );
  if (persResult.ok && persResult.data?.personId) {
    seedData.seedPersonId = persResult.data.personId;
  }

  // 4. Create a budget (if we have a person) — BudgetController accepts the raw
  // Budget entity (heading, status, notes, person).
  if (seedData.seedPersonId && seedData.seedConceptId) {
    const presResult = await apiPost<{ idBudget: number }>(
      page,
      "/budgets",
      {
        person: { personId: seedData.seedPersonId },
        date: "2026-05-27",
        heading: "Presupuesto E2E Seed",
        status: "PENDIENTE",
        notes: `Presupuesto semilla ${seedData.testId}`,
      }
    );
    if (presResult.ok && presResult.data?.idBudget) {
      seedData.seedBudgetId = presResult.data.idBudget;
    }
  }

  // 5. Create a usuario — UserController's UserRequest is (name, password, type, active).
  const usrResult = await apiPost<{ idUser: number }>(
    page,
    "/users",
    {
      name: `testuser-${seedData.testId}`,
      password: "Test1234!",
      type: "EMPLEADO",
      active: true,
    }
  );
  if (usrResult.ok && usrResult.data?.idUser) {
    seedData.seedUsuarioId = usrResult.data.idUser;
  }

  // 6. Create a folio — FolioController's FolioRequest is (number, year, status, notes,
  // typeFolioId, notaryId, deedId).
  const folioResult = await apiPost<{ idFolio: number }>(
    page,
    "/folios",
    {
      number: Math.floor(10000 + Math.random() * 90000),
      year: 2026,
      status: "Nuevo",
      typeFolioId: 1,
      notaryId: seedData.seedPersonId || 1,
    }
  );
  if (folioResult.ok && folioResult.data?.idFolio) {
    seedData.seedFolioId = folioResult.data.idFolio;
  }

  // 7. Create an estado de gestión — ManagementStatusController accepts a
  // DtoManagementStatus (name, notes).
  const egResult = await apiPost<{ idManagementStatus: number }>(
    page,
    "/management-statuses",
    {
      name: `Seed Estado ${seedData.testId}`,
      notes: "Seeded by global-setup",
    }
  );
  if (egResult.ok && egResult.data?.idManagementStatus) {
    seedData.seedStatusManagementId = egResult.data.idManagementStatus;
  }
}

/**
 * Main global setup function
 */
async function globalSetup(): Promise<void> {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    baseURL: process.env.BASE_URL || "http://localhost:3000",
  });
  const page = await context.newPage();

  // Expose helpers on page context
  await page.addInitScript(() => {
    (window as any).__SEED_DATA__ = {
      testId: seedData.testId,
    };
  });

  try {
    // Step 1: Authenticate
    console.log("[global-setup] Authenticating as admin...");
    await authenticateAdmin(page);
    console.log("[global-setup] Auth OK");

    // Step 2: Seed catalog data
    console.log("[global-setup] Seeding test data...");
    await seedCatalogData(page);
    console.log("[global-setup] Seed complete:", {
      personId: seedData.seedPersonId,
      budgetId: seedData.seedBudgetId,
      conceptId: seedData.seedConceptId,
      procedureTypeId: seedData.seedTypeProcedureId,
      usuarioId: seedData.seedUsuarioId,
      folioId: seedData.seedFolioId,
      managementStatusId: seedData.seedStatusManagementId,
    });

    // Step 3: Export seed data to environment for test specs
    process.env.E2E_TEST_ID = String(seedData.testId);
    process.env.E2E_SEED_PERSONA_ID = String(seedData.seedPersonId ?? "");
    process.env.E2E_SEED_PRESUPUESTO_ID = String(seedData.seedBudgetId ?? "");
    process.env.E2E_SEED_CONCEPTO_ID = String(seedData.seedConceptId ?? "");
    process.env.E2E_SEED_TIPO_TRAMITE_ID = String(seedData.seedTypeProcedureId ?? "");
    process.env.E2E_SEED_USUARIO_ID = String(seedData.seedUsuarioId ?? "");
    process.env.E2E_SEED_FOLIO_ID = String(seedData.seedFolioId ?? "");
    process.env.E2E_SEED_ESTADO_GESTION_ID = String(seedData.seedStatusManagementId ?? "");

    // Persist JWT for workers — process.env set here is not inherited by test
    // workers, and mid-suite login 429 (lockout) must not strand later specs.
    const tokenPath = "tests/fixtures/e2e-admin-token.txt";
    if (process.env.E2E_ADMIN_TOKEN) {
      fs.mkdirSync("tests/fixtures", { recursive: true });
      fs.writeFileSync(tokenPath, process.env.E2E_ADMIN_TOKEN, "utf8");
    }

    // Step 4: Save storage state for reuse
    await context.storageState({ path: "tests/fixtures/admin-auth.json" });
    console.log("[global-setup] Storage state saved");

  } catch (err) {
    console.error("[global-setup] FAILED:", err);
    throw err;
  } finally {
    await page.close();
    await context.close();
    await browser.close();
  }
}

export default globalSetup;
