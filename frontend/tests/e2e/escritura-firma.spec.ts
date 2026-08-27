/**
 * CU06 - Firmar escritura (issue #832)
 *
 * The golden path ("firmar una escritura con folio asignado") cannot be
 * exercised end-to-end yet: there is no REST endpoint that links a Folio to
 * an Escritura (design.md — Non-Goals, tracked separately as issue #838 /
 * CU87). `EscrituraFirmaServiceTest` covers that transition at the service
 * layer with a repository-seeded folio. This spec covers what is reachable
 * through the running app: the two edge paths from design.md — Playwright
 * Strategy (sin folio, ya firmada) and the responsive layout.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { apiPost, uniqueId } from "./setup/api-helpers";

test.describe("CU06 - Firmar escritura", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("CU06-GW01: Given escritura sin folio asignado, When firmar, Then shows visible error and stays sin firmar", async ({
    page,
  }) => {
    const numero = uniqueId() % 1_000_000;
    const seeded = await apiPost<{ idEscritura: number }>(page, "/escrituras", {
      numero,
      fechaEscrituracion: new Date().toISOString().split("T")[0],
      cuerpo: `Contenido E2E ${numero}`,
      estado: "Sin Firmar",
    });
    const idEscritura = seeded.data!.idEscritura;

    await steps.givenUserIsOnPage("/dashboard/escrituras");
    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-firmar-escritura-${idEscritura}`).click();
    const dialog = page.getByRole("alertdialog");
    await expect(dialog).toBeVisible();
    await expect(dialog).toContainText(/¿Firmar esta escritura\?/i);

    await dialog.getByRole("button", { name: /firmar escritura/i }).click();
    await expect(dialog).not.toBeVisible();

    await steps.thenShowsErrorMessage("firmar");
    await expect(page.getByTestId(`btn-firmar-escritura-${idEscritura}`)).toBeVisible();
  });

  test("CU06-GW02: Given escritura ya firmada, Then the firmar action is not offered", async ({ page }) => {
    const numero = uniqueId() % 1_000_000;
    const seeded = await apiPost<{ idEscritura: number }>(page, "/escrituras", {
      numero,
      fechaEscrituracion: new Date().toISOString().split("T")[0],
      cuerpo: `Contenido E2E ${numero}`,
      estado: "Firmada",
    });
    const idEscritura = seeded.data!.idEscritura;

    await steps.givenUserIsOnPage("/dashboard/escrituras");
    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId(`btn-firmar-escritura-${idEscritura}`)).toHaveCount(0);
  });

  test.skip("CU06-GW03: Given escritura con folio asignado, When firmar, Then transitions to Firmada", async () => {
    // Blocked on #838 — no REST endpoint links a Folio to an Escritura yet.
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`escrituras screen has no horizontal overflow at ${viewport.label}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await steps.givenUserIsOnPage("/dashboard/escrituras");
      await page.waitForLoadState("networkidle");
      await steps.thenHasNoHorizontalOverflow();
    });
  }
});
