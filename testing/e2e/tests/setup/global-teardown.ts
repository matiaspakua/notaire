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
  personId: process.env.E2E_SEED_PERSON_ID
    ? Number(process.env.E2E_SEED_PERSON_ID)
    : null,
  budgetId: process.env.E2E_SEED_BUDGET_ID
    ? Number(process.env.E2E_SEED_BUDGET_ID)
    : null,
  conceptId: process.env.E2E_SEED_CONCEPT_ID
    ? Number(process.env.E2E_SEED_CONCEPT_ID)
    : null,
  procedureTypeId: process.env.E2E_SEED_PROCEDURE_TYPE_ID
    ? Number(process.env.E2E_SEED_PROCEDURE_TYPE_ID)
    : null,
  userId: process.env.E2E_SEED_USER_ID
    ? Number(process.env.E2E_SEED_USER_ID)
    : null,
  folioId: process.env.E2E_SEED_FOLIO_ID
    ? Number(process.env.E2E_SEED_FOLIO_ID)
    : null,
  managementStatusId: process.env.E2E_SEED_MANAGEMENT_STATUS_ID
    ? Number(process.env.E2E_SEED_MANAGEMENT_STATUS_ID)
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
    await page.getByTestId("input-username").fill("admin");
    await page.getByTestId("input-password").fill("admin");
    await page.getByTestId("btn-sign-in").click();
    await page.waitForURL(/\/dashboard/, { timeout: 15000 });

    // Delete in dependency-safe order (children before parents)
    const deletions: { label: string; path: string }[] = [
      ...(idsToCleanup.budgetId
        ? [{ label: "budget", path: `/budgets/${idsToCleanup.budgetId}` }]
        : []),
      ...(idsToCleanup.folioId
        ? [{ label: "folio", path: `/folios/${idsToCleanup.folioId}` }]
        : []),
      ...(idsToCleanup.conceptId
        ? [{ label: "concept", path: `/concepts/${idsToCleanup.conceptId}` }]
        : []),
      // Delete the user before the person it references, otherwise the
      // person delete hits a foreign-key constraint.
      ...(idsToCleanup.userId
        ? [{ label: "user", path: `/users/${idsToCleanup.userId}` }]
        : []),
      ...(idsToCleanup.personId
        ? [{ label: "person", path: `/people/${idsToCleanup.personId}` }]
        : []),
      ...(idsToCleanup.procedureTypeId
        ? [{ label: "procedure-type", path: `/procedure-types/${idsToCleanup.procedureTypeId}` }]
        : []),
      ...(idsToCleanup.managementStatusId
        ? [{ label: "management-status", path: `/management-statuses/${idsToCleanup.managementStatusId}` }]
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
          console.warn(
            `[global-teardown] Failed to delete ${del.label}: ${result.status}`,
            result.error ?? JSON.stringify(result.data),
          );
        }
      } catch (err) {
        console.warn(`[global-teardown] Error deleting ${del.label}:`, err);
      }
    }

    // Remove auth fixture file
    try {
      const fs = await import("fs");
      fs.unlinkSync("tests/fixtures/admin-auth.json");
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
