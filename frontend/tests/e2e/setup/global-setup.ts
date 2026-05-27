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

  // Try direct API login first (more reliable)
  const loginResult = await apiPost<{ usuario: any; valido: boolean }>(
    page,
    "/usuarios/login",
    { nombre: "admin", contrasenia: "admin" }
  );

  if (loginResult.ok && loginResult.data?.usuario) {
    // Inject auth state via localStorage (same as frontend auth flow)
    await page.addInitScript(() => {
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: {
            user: { nombre: "admin", tipo: "ADMIN", valido: true },
            isAuthenticated: true,
          },
          version: 0,
        })
      );
    });
    seedData.adminAuth = { token: "", user: loginResult.data.usuario };
  } else {
    // Fallback: UI-based login
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  }
}

/**
 * Seed catalog data needed by most tests
 */
async function seedCatalogData(page: Page): Promise<void> {
  // 1. Create a tipo de trámite
  const ttResult = await apiPost<{ idTipoDeTramite: number; nombre: string }>(
    page,
    "/tipo-tramite",
    {
      nombre: `Test Tramite ${seedData.testId}`,
      descripcion: "Seeded by global-setup for E2E tests",
      seArchiva: false,
      seInscribe: false,
    }
  );
  if (ttResult.ok && ttResult.data?.idTipoDeTramite) {
    seedData.seedTipoTramiteId = ttResult.data.idTipoDeTramite;
  }

  // 2. Create a concepto
  const concResult = await apiPost<{ idConcepto: number; nombre: string }>(
    page,
    "/conceptos",
    {
      nombre: `Test Concepto ${seedData.testId}`,
      descripcion: "Seeded by global-setup",
      valor: 1000.0,
    }
  );
  if (concResult.ok && concResult.data?.idConcepto) {
    seedData.seedConceptoId = concResult.data.idConcepto;
  }

  // 3. Create a persona (client)
  const persResult = await apiPost<{ idPersona: number; nombre: string; apellido: string }>(
    page,
    "/personas",
    {
      nombre: "Seed",
      apellido: `Persona-${seedData.testId}`,
      numeroIdentificacion: `SEED${seedData.testId}`,
      email: `seed-${seedData.testId}@notaire.test`,
      esCliente: true,
      tipoIdentificacion: { idTipoIdentificacion: 1 },
      nacionalidad: "Argentina",
      fechaNacimiento: "1990-01-01",
      cuit: `20-${String(seedData.testId).padStart(8, "0")}-9`,
      estadoCivil: "Soltero",
      sexo: "Masculino",
    }
  );
  if (persResult.ok && persResult.data?.idPersona) {
    seedData.seedPersonaId = persResult.data.idPersona;
  }

  // 4. Create a presupuesto (if we have a persona)
  if (seedData.seedPersonaId && seedData.seedConceptoId) {
    const presResult = await apiPost<{ idPresupuesto: number }>(
      page,
      "/presupuestos",
      {
        fkIdPersona: { idPersona: seedData.seedPersonaId },
        fecha: "2026-05-27",
        encabezado: "Presupuesto E2E Seed",
        estado: "Pendiente",
        observaciones: `Presupuesto semilla ${seedData.testId}`,
      }
    );
    if (presResult.ok && presResult.data?.idPresupuesto) {
      seedData.seedPresupuestoId = presResult.data.idPresupuesto;
    }
  }

  // 5. Create a usuario (fkIdPersona references existing persona id=1 or seedPersonaId)
  const usrResult = await apiPost<{ idUsuario: number }>(
    page,
    "/usuarios",
    {
      nombre: `testuser-${seedData.testId}`,
      contrasenia: "Test1234!",
      tipo: "EMPLEADO",
      estado: true,
      fkIdPersona: { idPersona: seedData.seedPersonaId || 1 },
    }
  );
  if (usrResult.ok && usrResult.data?.idUsuario) {
    seedData.seedUsuarioId = usrResult.data.idUsuario;
  }

  // 6. Create a folio
  // References existing persona (id=1 "Juan Carlos Garcia") and tipo_de_folio (id=1 "De documento")
  const folioResult = await apiPost<{ idFolio: number }>(
    page,
    "/folio",
    {
      numero: Math.floor(10000 + Math.random() * 90000),
      anio: 2026,
      estado: "Nuevo",
      disponible: true,
      fkIdPersonaEscribano: { idPersona: seedData.seedPersonaId || 1 },
      fkIdTipoFolio: { idTipoFolio: 1 },
    }
  );
  if (folioResult.ok && folioResult.data?.idFolio) {
    seedData.seedFolioId = folioResult.data.idFolio;
  }

  // 7. Create an estado de gestión
  const egResult = await apiPost<{ idEstadoGestion: number }>(
    page,
    "/estado-gestion",
    {
      nombre: `Seed Estado ${seedData.testId}`,
      descripcion: "Seeded by global-setup",
    }
  );
  if (egResult.ok && egResult.data?.idEstadoGestion) {
    seedData.seedEstadoGestionId = egResult.data.idEstadoGestion;
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
