/**
 * CU07/CU08 - Generación y verificación de testimonio (issue #832)
 *
 * Golden path: generar testimonio desde una escritura firmada → verificar
 * sin observaciones → emitir copia. Edge paths: verificar con observaciones;
 * emitir copia antes de verificar (bloqueado, per design.md — Playwright
 * Strategy).
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { apiGet, apiPost, uniqueId } from "./setup/api-helpers";

interface TestimonioApiResult {
  idTestimony: number;
  deed?: { number?: number };
}

async function seedSignedDeed(page: import("@playwright/test").Page): Promise<{ idEscritura: number; numero: number }> {
  const numero = uniqueId() % 1_000_000;
  const seeded = await apiPost<{ idDeed: number }>(page, "/deeds", {
    number: numero,
    dateDeedrecording: new Date().toISOString().split("T")[0],
    body: `Contenido E2E ${numero}`,
    status: "Firmada",
  });
  return { idEscritura: seeded.data!.idDeed, numero };
}

test.describe("CU07/CU08 - Generar y verificar testimonio", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("Golden path: generar testimonio, verificar sin observaciones y emitir copia", async ({ page }) => {
    const { numero } = await seedSignedDeed(page);

    await steps.givenUserIsOnPage("/dashboard/testimonies");
    await page.getByTestId("btn-generate-testimony").click();
    await steps.thenModalIsVisible();

    await page.getByTestId("select-deed-testimony").click();
    await page.getByRole("option", { name: new RegExp(String(numero)) }).click();
    await page.getByTestId("btn-save-testimony").click();
    await steps.thenShowsSuccessMessage("generado");
    await steps.thenModalIsNotVisible();

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    const list = await apiGet<TestimonioApiResult[]>(page, "/testimonies");
    const idTestimonio = list.data!.find((te) => te.deed?.number === numero)!.idTestimony;

    await page.getByTestId(`btn-verify-testimony-${idTestimonio}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("btn-confirm-verify-testimony").click();
    await steps.thenShowsSuccessMessage("verificado");

    await expect(row).toContainText(/verificado/i);
    const downloadPromise = page.waitForEvent("download");
    await page.getByTestId(`btn-issue-copy-${idTestimonio}`).click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/^testimonio_\d+_copia\.pdf$/);
  });

  test("Edge: verificar con observaciones muestra el testimonio como Observado", async ({ page }) => {
    const { idEscritura, numero } = await seedSignedDeed(page);
    const generated = await apiPost<{ idTestimony: number }>(page, `/testimonies/${idEscritura}/generate`, {});
    const idTestimonio = generated.data!.idTestimony;

    await steps.givenUserIsOnPage("/dashboard/testimonies");
    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-verify-testimony-${idTestimonio}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("checkbox-observed-testimony").click();
    await page.getByTestId("input-notes-testimony").fill("Falta firma del otorgante");
    await page.getByTestId("btn-confirm-verify-testimony").click();
    await steps.thenShowsSuccessMessage("verificado");

    await expect(row).toContainText(/observado/i);
  });

  test("Edge: emitir copia está bloqueado hasta que el testimonio esté verificado", async ({ page }) => {
    const { idEscritura, numero } = await seedSignedDeed(page);
    const generated = await apiPost<{ idTestimony: number }>(page, `/testimonies/${idEscritura}/generate`, {});
    const idTestimonio = generated.data!.idTestimony;

    await steps.givenUserIsOnPage("/dashboard/testimonies");
    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await expect(page.getByTestId(`btn-issue-copy-${idTestimonio}`)).toHaveCount(0);
    await expect(page.getByTestId(`btn-verify-testimony-${idTestimonio}`)).toBeVisible();
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`testimonios screen has no horizontal overflow at ${viewport.label}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await steps.givenUserIsOnPage("/dashboard/testimonies");
      await page.waitForLoadState("networkidle");
      await steps.thenHasNoHorizontalOverflow();
    });
  }
});
