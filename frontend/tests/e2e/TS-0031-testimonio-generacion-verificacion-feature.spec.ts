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
  idTestimonio: number;
  escritura?: { numero?: number };
}

async function seedEscrituraFirmada(page: import("@playwright/test").Page): Promise<{ idEscritura: number; numero: number }> {
  const numero = uniqueId() % 1_000_000;
  const seeded = await apiPost<{ idEscritura: number }>(page, "/escrituras", {
    numero,
    fechaEscrituracion: new Date().toISOString().split("T")[0],
    cuerpo: `Contenido E2E ${numero}`,
    estado: "Firmada",
  });
  return { idEscritura: seeded.data!.idEscritura, numero };
}

test.describe("CU07/CU08 - Generar y verificar testimonio", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("Golden path: generar testimonio, verificar sin observaciones y emitir copia", async ({ page }) => {
    const { numero } = await seedEscrituraFirmada(page);

    await steps.givenUserIsOnPage("/dashboard/testimonios");
    await page.getByTestId("btn-generar-testimonio").click();
    await steps.thenModalIsVisible();

    await page.getByTestId("select-escritura-testimonio").click();
    await page.getByRole("option", { name: new RegExp(String(numero)) }).click();
    await page.getByTestId("btn-guardar-testimonio").click();
    await steps.thenShowsSuccessMessage("generado");
    await steps.thenModalIsNotVisible();

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    const list = await apiGet<TestimonioApiResult[]>(page, "/testimonio");
    const idTestimonio = list.data!.find((te) => te.escritura?.numero === numero)!.idTestimonio;

    await page.getByTestId(`btn-verificar-testimonio-${idTestimonio}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("btn-confirmar-verificar-testimonio").click();
    await steps.thenShowsSuccessMessage("verificado");

    await expect(row).toContainText(/verificado/i);
    const downloadPromise = page.waitForEvent("download");
    await page.getByTestId(`btn-emitir-copia-${idTestimonio}`).click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/^testimonio_\d+_copia\.pdf$/);
  });

  test("Edge: verificar con observaciones muestra el testimonio como Observado", async ({ page }) => {
    const { idEscritura, numero } = await seedEscrituraFirmada(page);
    const generated = await apiPost<{ idTestimonio: number }>(page, `/testimonio/${idEscritura}/generar`, {});
    const idTestimonio = generated.data!.idTestimonio;

    await steps.givenUserIsOnPage("/dashboard/testimonios");
    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-verificar-testimonio-${idTestimonio}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("checkbox-observado-testimonio").click();
    await page.getByTestId("input-observaciones-testimonio").fill("Falta firma del otorgante");
    await page.getByTestId("btn-confirmar-verificar-testimonio").click();
    await steps.thenShowsSuccessMessage("verificado");

    await expect(row).toContainText(/observado/i);
  });

  test("Edge: emitir copia está bloqueado hasta que el testimonio esté verificado", async ({ page }) => {
    const { idEscritura, numero } = await seedEscrituraFirmada(page);
    const generated = await apiPost<{ idTestimonio: number }>(page, `/testimonio/${idEscritura}/generar`, {});
    const idTestimonio = generated.data!.idTestimonio;

    await steps.givenUserIsOnPage("/dashboard/testimonios");
    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await expect(page.getByTestId(`btn-emitir-copia-${idTestimonio}`)).toHaveCount(0);
    await expect(page.getByTestId(`btn-verificar-testimonio-${idTestimonio}`)).toBeVisible();
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`testimonios screen has no horizontal overflow at ${viewport.label}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await steps.givenUserIsOnPage("/dashboard/testimonios");
      await page.waitForLoadState("networkidle");
      await steps.thenHasNoHorizontalOverflow();
    });
  }
});
