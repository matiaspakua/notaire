/**
 * Global teardown for Playwright E2E tests
 *
 * Responsibilities:
 *  1. Delete all seed data created during global-setup
 *  2. Remove any test data left behind by individual test suites
 *  3. Clean up auth state
 *
 * This runs once AFTER all test suites complete.
 */
import { chromium } from "@playwright/test";
import { apiDelete } from "./api-helpers";

// Track IDs to clean up (loaded from env vars set by global-setup)
const idsToCleanup = {
  personaId: process.env.E2E_SEED_PERSONA_ID
    ? Number(process.env.E2E_SEED_PERSONA_ID)
    : null,
  presupuestoId: process.env.E2E_SEED_PRESUPUESTO_ID
    ? Number(process.env.E2E_SEED_PRESUPUESTO_ID)
    : null,
  conceptoId: process.env.E2E_SEED_CONCEPTO_ID
    ? Number(process.env.E2E_SEED_CONCEPTO_ID)
    : null,
  tipoTramiteId: process.env.E2E_SEED_TIPO_TRAMITE_ID
    ? Number(process.env.E2E_SEED_TIPO_TRAMITE_ID)
    : null,
  usuarioId: process.env.E2E_SEED_USUARIO_ID
    ? Number(process.env.E2E_SEED_USUARIO_ID)
    : null,
  folioId: process.env.E2E_SEED_FOLIO_ID
    ? Number(process.env.E2E_SEED_FOLIO_ID)
    : null,
  estadoGestionId: process.env.E2E_SEED_ESTADO_GESTION_ID
    ? Number(process.env.E2E_SEED_ESTADO_GESTION_ID)
    : null,
};

async function globalTeardown(): Promise<void> {
  const cleanupEligible = process.env.E2E_CLEANUP !== "false";
  if (!cleanupEligible) {
    console.log("[global-teardown] Skipping cleanup (E2E_CLEANUP=false)");
    return;
  }

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    baseURL: process.env.BASE_URL || "http://localhost:3000",
  });
  const page = await context.newPage();

  try {
    // Authenticate first (needed for DELETE operations)
    await page.goto("/login");
    await page.waitForLoadState("networkidle");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await page.waitForURL(/\/dashboard/, { timeout: 15000 });

    // Delete in dependency-safe order (children before parents)
    const deletions: { label: string; path: string }[] = [
      ...(idsToCleanup.presupuestoId
        ? [{ label: "presupuesto", path: `/presupuestos/${idsToCleanup.presupuestoId}` }]
        : []),
      ...(idsToCleanup.folioId
        ? [{ label: "folio", path: `/folio/${idsToCleanup.folioId}` }]
        : []),
      ...(idsToCleanup.conceptoId
        ? [{ label: "concepto", path: `/conceptos/${idsToCleanup.conceptoId}` }]
        : []),
      ...(idsToCleanup.personaId
        ? [{ label: "persona", path: `/personas/${idsToCleanup.personaId}` }]
        : []),
      ...(idsToCleanup.usuarioId
        ? [{ label: "usuario", path: `/usuarios/${idsToCleanup.usuarioId}` }]
        : []),
      ...(idsToCleanup.tipoTramiteId
        ? [{ label: "tipo-tramite", path: `/tipo-tramite/${idsToCleanup.tipoTramiteId}` }]
        : []),
      ...(idsToCleanup.estadoGestionId
        ? [{ label: "estado-gestion", path: `/estado-gestion/${idsToCleanup.estadoGestionId}` }]
        : []),
    ];

    for (const del of deletions) {
      try {
        const result = await apiDelete(page, del.path);
        if (result.ok) {
          console.log(`[global-teardown] Deleted ${del.label} (${del.path})`);
        } else if (result.status === 404) {
          console.log(`[global-teardown] ${del.label} already deleted (404)`);
        } else {
          console.warn(`[global-teardown] Failed to delete ${del.label}: ${result.status}`);
        }
      } catch (err) {
        console.warn(`[global-teardown] Error deleting ${del.label}:`, err);
      }
    }

    // Remove auth fixture file
    try {
      const fs = await import("fs");
      fs.unlinkSync("tests/e2e/fixtures/admin-auth.json");
      console.log("[global-teardown] Auth fixture removed");
    } catch {
      // File may not exist, that's fine
    }

  } catch (err) {
    console.error("[global-teardown] FAILED:", err);
  } finally {
    await page.close();
    await context.close();
    await browser.close();
  }

  console.log("[global-teardown] Complete");
}

export default globalTeardown;
