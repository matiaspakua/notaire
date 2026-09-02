/**
 * E2E tests — Administrar Cuadernos de Folios (CU80).
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { createPersona, createFolio, uniqueId } from "./setup/api-helpers";

async function seedFoliosConsecutivos(
  page: Page,
  escribanoId: number,
  count: number,
  overrides: { estados?: string[] } = {},
) {
  const base = (uniqueId() % 900000) * 10 + 100000;
  const folios = [];
  for (let i = 0; i < count; i++) {
    const numero = base + i;
    const estado = overrides.estados?.[i] ?? "Nuevo";
    const folio = await createFolio(page, escribanoId, { numero, estado });
    folios.push({ idFolio: folio.data!.idFolio, numero });
  }
  return folios;
}

test.describe("CU80 - Administrar Cuadernos de Folios", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: creating a cuaderno from 10 consecutive folios shows it in the list", async ({ page }) => {
    const persona = await createPersona(page);
    const escribanoId = persona.data!.idPersona;
    const folios = await seedFoliosConsecutivos(page, escribanoId, 10);

    await page.goto("/dashboard/protocolo/cuadernos");
    await page.waitForLoadState("domcontentloaded");
    await page.getByRole("button", { name: /nuevo cuaderno/i }).click();

    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    for (const folio of folios) {
      await dialog.getByTestId(`checkbox-folio-${folio.idFolio}`).click();
    }

    await dialog.getByRole("button", { name: /crear/i }).click();
    await expect(page.getByText(/cuaderno creado/i)).toBeVisible({ timeout: 10000 });
    await expect(dialog).not.toBeVisible();
    await expect(page.getByRole("button", { name: /descargar pdf/i }).first()).toBeVisible({ timeout: 10000 });
  });

  test("checkbox selection toggles independently for each folio (regression for double-toggle bug)", async ({
    page,
  }) => {
    const persona = await createPersona(page);
    const folios = await seedFoliosConsecutivos(page, persona.data!.idPersona, 3);

    await page.goto("/dashboard/protocolo/cuadernos");
    await page.waitForLoadState("domcontentloaded");
    await page.getByRole("button", { name: /nuevo cuaderno/i }).click();

    const dialog = page.getByRole("dialog");
    const checkboxes = folios.map((f) => dialog.getByTestId(`checkbox-folio-${f.idFolio}`).getByRole("checkbox"));

    for (const checkbox of checkboxes) {
      await checkbox.click();
      await expect(checkbox).toBeChecked();
    }

    await checkboxes[1].click();
    await expect(checkboxes[1]).not.toBeChecked();
    await expect(checkboxes[0]).toBeChecked();
    await expect(checkboxes[2]).toBeChecked();
  });

  test("edge path: selecting a count that is not a multiple of ten shows a validation error", async ({ page }) => {
    const persona = await createPersona(page);
    const folios = await seedFoliosConsecutivos(page, persona.data!.idPersona, 3);

    await page.goto("/dashboard/protocolo/cuadernos");
    await page.waitForLoadState("domcontentloaded");
    await page.getByRole("button", { name: /nuevo cuaderno/i }).click();

    const dialog = page.getByRole("dialog");
    for (const folio of folios) {
      await dialog.getByTestId(`checkbox-folio-${folio.idFolio}`).click();
    }
    await dialog.getByRole("button", { name: /crear/i }).click();

    await expect(page.getByText(/múltiplo exacto de 10/i)).toBeVisible({ timeout: 10000 });
    await expect(dialog).toBeVisible();
  });

  test("edge path: including a damaged folio without observaciones shows a validation error", async ({ page }) => {
    const persona = await createPersona(page);
    const folios = await seedFoliosConsecutivos(page, persona.data!.idPersona, 10, {
      estados: ["Errose"],
    });

    await page.goto("/dashboard/protocolo/cuadernos");
    await page.waitForLoadState("domcontentloaded");
    await page.getByRole("button", { name: /nuevo cuaderno/i }).click();

    const dialog = page.getByRole("dialog");
    for (const folio of folios) {
      await dialog.getByTestId(`checkbox-folio-${folio.idFolio}`).click();
    }
    await dialog.getByRole("button", { name: /crear/i }).click();

    await expect(page.getByText(/requiere una justificaci/i)).toBeVisible({ timeout: 10000 });
    await expect(dialog).toBeVisible();

    await dialog.getByLabel(/observaciones/i).fill("Folio dañado durante impresión");
    await dialog.getByRole("button", { name: /crear/i }).click();
    await expect(page.getByText(/cuaderno creado/i)).toBeVisible({ timeout: 10000 });
  });
});
