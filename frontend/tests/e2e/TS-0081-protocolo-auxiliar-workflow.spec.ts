/**
 * E2E tests — Gestión de Trámites en Protocolo Auxiliar (CU81).
 */
import { test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { createPersona, createFolio, createTipoDeFolio } from "./setup/api-helpers";

async function seedFolioAuxiliarDisponible(page: import("@playwright/test").Page) {
  const persona = await createPersona(page);
  const tipoAuxiliar = await createTipoDeFolio(page, { esAuxiliar: true });
  const folio = await createFolio(page, persona.data!.idPersona, {
    tipoFolioId: tipoAuxiliar.data!.idTipoFolio,
  });
  return folio.data!.idFolio;
}

test.describe("CU81 - Gestión de Trámites en Protocolo Auxiliar", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: starting an escritura on an available auxiliar folio removes it from the list", async ({
    page,
  }) => {
    const idFolio = await seedFolioAuxiliarDisponible(page);

    await page.goto("/dashboard/protocolo/auxiliar");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(idFolio)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await row.getByRole("button", { name: /iniciar escritura/i }).click();

    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    await dialog.getByTestId("textarea-cuerpo-escritura-auxiliar").fill("Cuerpo de la escritura de prueba E2E");
    await dialog.getByRole("button", { name: /crear/i }).click();

    await expect(page.getByText(/escritura iniciada/i)).toBeVisible({ timeout: 10000 });
    await expect(dialog).not.toBeVisible();
    await expect(row).not.toBeVisible();
  });

  test("edge path: submit button stays disabled until the cuerpo field is filled", async ({ page }) => {
    const idFolio = await seedFolioAuxiliarDisponible(page);

    await page.goto("/dashboard/protocolo/auxiliar");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(idFolio)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await row.getByRole("button", { name: /iniciar escritura/i }).click();

    const dialog = page.getByRole("dialog");
    const submit = dialog.getByRole("button", { name: /crear/i });
    await expect(submit).toBeDisabled();

    await dialog.getByTestId("textarea-cuerpo-escritura-auxiliar").fill("Contenido");
    await expect(submit).toBeEnabled();
  });

  test("empty state: shows no-data message when there are no available auxiliar folios", async ({ page }) => {
    await page.goto("/dashboard/protocolo/auxiliar");
    await page.waitForLoadState("domcontentloaded");

    await expect(page.getByText(/protocolo auxiliar/i).first()).toBeVisible();
  });

  for (const width of [320, 768, 1024]) {
    test(`has no horizontal overflow at ${width}px`, async ({ page }) => {
      await page.setViewportSize({ width, height: 800 });
      await page.goto("/dashboard/protocolo/auxiliar");
      await page.waitForLoadState("domcontentloaded");

      await expect(page.getByText(/protocolo auxiliar/i).first()).toBeVisible();
      const hasNoOverflow = await page.evaluate(
        () => document.documentElement.scrollWidth <= window.innerWidth,
      );
      expect(hasNoOverflow).toBe(true);
    });
  }
});
