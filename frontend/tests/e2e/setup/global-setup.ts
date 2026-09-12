/**
 * Global setup for Playwright E2E tests
 *
 * Responsibilities:
 *  1. Authenticate as admin and store auth cookie/localStorage
 *  2. Seed base catalog data via API (tipos de trámite, conceptos, etc.)
 *  3. Export shared test data as environment variables
 *  4. Verify backend and frontend are reachable
 *
 * This runs once before ALL test suites.
 */
import { chromium, type Browser, type Page } from "@playwright/test";
import { apiPost, apiGet, apiPut, apiDelete } from "./api-helpers";

interface SeedData {
  testId: number;
  adminAuth: { token: string; user: any } | null;
  seedPersonaId: number | null;
  seedPresupuestoId: number | null;
  seedGestionId: number | null;
  seedEscrituraId: number | null;
  seedConceptoId: number | null;
  seedTipoTramiteId: number | null;
  seedEstadoGestionId: number | null;
  seedTipoDocumentoId: number | null;
  seedUsuarioId: number | null;
  seedFolioId: number | null;
  seedSuplenciaId: number | null;
  seedPagoId: number | null;
  seedTestimonioId: number | null;
}

// Global seed data accessible by tests via process.env
export const seedData: SeedData = {
  testId: Date.now(),
  adminAuth: null,
  seedPersonaId: null,
  seedPresupuestoId: null,
  seedGestionId: null,
  seedEscrituraId: null,
  seedConceptoId: null,
  seedTipoTramiteId: null,
  seedEstadoGestionId: null,
  seedTipoDocumentoId: null,
  seedUsuarioId: null,
  seedFolioId: null,
  seedSuplenciaId: null,
  seedPagoId: null,
  seedTestimonioId: null,
};

/**
 * Authenticate as admin and store auth state for all tests
 */
async function authenticateAdmin(page: Page): Promise<void> {
  await page.goto("/login");
  await page.waitForLoadState("networkidle");

  // Direct API login. Response is a flat map (valido, token, idUsuario, nombre, ...) —
  // not nested under "usuario" — see UsuarioController#login.
  const loginResult = await apiPost<{
    valido: boolean;
    token: string;
    idUsuario: number;
    nombre: string;
    tipo: string;
  }>(page, "/usuarios/login", { name: "admin", password: "admin" });

  if (loginResult.ok && loginResult.data?.valido && loginResult.data.token) {
    const { token, idUsuario, nombre, tipo } = loginResult.data;
    // Inject auth state via localStorage (same shape the frontend auth store persists)
    await page.addInitScript(
      ([t, user]) => {
        localStorage.setItem(
          "notaire-auth",
          JSON.stringify({
            state: { user, token: t, isAuthenticated: true },
            version: 0,
          })
        );
      },
      [token, { nombre, tipo, valido: true, idUsuario }] as const
    );
    seedData.adminAuth = { token, user: { idUsuario, nombre, tipo } };
    // Visible to every worker process spawned after global-setup completes — read by
    // tests/e2e/setup/api-helpers.ts so page.request-based seed/cleanup calls authenticate.
    process.env.E2E_ADMIN_TOKEN = token;
  } else {
    // Fallback: UI-based login
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await page.waitForURL(/\/dashboard/, { timeout: 15000 });

    const storedToken = await page.evaluate(() => {
      try {
        const raw = localStorage.getItem("notaire-auth");
        return raw ? JSON.parse(raw)?.state?.token ?? null : null;
      } catch {
        return null;
      }
    });
    if (storedToken) {
      process.env.E2E_ADMIN_TOKEN = storedToken;
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
    "/tipo-tramite",
    {
      name: `Test Tramite ${seedData.testId}`,
      notes: "Seeded by global-setup for E2E tests",
      isArchived: false,
      isRegistered: false,
    }
  );
  if (ttResult.ok && ttResult.data?.idProcedureType) {
    seedData.seedTipoTramiteId = ttResult.data.idProcedureType;
  }

  // 2. Create a concepto — ConceptController accepts a DtoConcept (name, value, percentage).
  const concResult = await apiPost<{ idConcept: number; name: string }>(
    page,
    "/conceptos",
    {
      name: `Test Concepto ${seedData.testId}`,
      value: 1000.0,
    }
  );
  if (concResult.ok && concResult.data?.idConcept) {
    seedData.seedConceptoId = concResult.data.idConcept;
  }

  // 3. Create a persona (client) — PersonController accepts the raw Person entity.
  const persResult = await apiPost<{ personId: number; firstName: string; lastName: string }>(
    page,
    "/people",
    {
      firstName: "Seed",
      lastName: `Persona-${seedData.testId}`,
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
    seedData.seedPersonaId = persResult.data.personId;
  }

  // 4. Create a presupuesto (if we have a persona) — BudgetController accepts the raw
  // Budget entity (encabezado, status, notes, person).
  if (seedData.seedPersonaId && seedData.seedConceptoId) {
    const presResult = await apiPost<{ idBudget: number }>(
      page,
      "/presupuestos",
      {
        person: { personId: seedData.seedPersonaId },
        date: "2026-05-27",
        encabezado: "Presupuesto E2E Seed",
        status: "Pendiente",
        notes: `Presupuesto semilla ${seedData.testId}`,
      }
    );
    if (presResult.ok && presResult.data?.idBudget) {
      seedData.seedPresupuestoId = presResult.data.idBudget;
    }
  }

  // 5. Create a usuario — UserController's UserRequest is (name, password, type, active).
  const usrResult = await apiPost<{ idUser: number }>(
    page,
    "/usuarios",
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
    "/folio",
    {
      number: Math.floor(10000 + Math.random() * 90000),
      year: 2026,
      status: "Nuevo",
      typeFolioId: 1,
      notaryId: seedData.seedPersonaId || 1,
    }
  );
  if (folioResult.ok && folioResult.data?.idFolio) {
    seedData.seedFolioId = folioResult.data.idFolio;
  }

  // 7. Create an estado de gestión — ManagementStatusController accepts a
  // DtoManagementStatus (name, notes).
  const egResult = await apiPost<{ idManagementStatus: number }>(
    page,
    "/estado-gestion",
    {
      name: `Seed Estado ${seedData.testId}`,
      notes: "Seeded by global-setup",
    }
  );
  if (egResult.ok && egResult.data?.idManagementStatus) {
    seedData.seedEstadoGestionId = egResult.data.idManagementStatus;
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
      personaId: seedData.seedPersonaId,
      presupuestoId: seedData.seedPresupuestoId,
      conceptoId: seedData.seedConceptoId,
      tipoTramiteId: seedData.seedTipoTramiteId,
      usuarioId: seedData.seedUsuarioId,
      folioId: seedData.seedFolioId,
      estadoGestionId: seedData.seedEstadoGestionId,
    });

    // Step 3: Export seed data to environment for test specs
    process.env.E2E_TEST_ID = String(seedData.testId);
    process.env.E2E_SEED_PERSONA_ID = String(seedData.seedPersonaId ?? "");
    process.env.E2E_SEED_PRESUPUESTO_ID = String(seedData.seedPresupuestoId ?? "");
    process.env.E2E_SEED_CONCEPTO_ID = String(seedData.seedConceptoId ?? "");
    process.env.E2E_SEED_TIPO_TRAMITE_ID = String(seedData.seedTipoTramiteId ?? "");
    process.env.E2E_SEED_USUARIO_ID = String(seedData.seedUsuarioId ?? "");
    process.env.E2E_SEED_FOLIO_ID = String(seedData.seedFolioId ?? "");
    process.env.E2E_SEED_ESTADO_GESTION_ID = String(seedData.seedEstadoGestionId ?? "");

    // Step 4: Save storage state for reuse
    await context.storageState({ path: "tests/e2e/fixtures/admin-auth.json" });
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
