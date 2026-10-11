/**
 * E2E tests — "Cambiar estado" action on the gestiones screen (CU83)
 * Issues: #833, #804 (server rejects PUT status bypass; UI filtered destinations)
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { createPerson, createBudget, seedManagementWithWorkflow } from "./setup/api-helpers";
import { listRow } from "./setup/list-locators";

async function seedManagement(page: Page) {
  const persona = await createPerson(page);
  const presupuesto = await createBudget(page, persona.data!.personId);
  return seedManagementWithWorkflow(page, presupuesto.data!.idBudget);
}

test.describe("CU83 - Cambiar estado de una gestión", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: transitioning to a valid destination updates the estado", async ({ page }) => {
    const { idGestion, numero, estadoFinal } = await seedManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-cambiar-estado-${idGestion}`).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-nuevo-estado").click();
    await page.getByRole("option", { name: estadoFinal }).click();
    await dialog.getByTestId("btn-confirmar-transicion").click();

    await expect(dialog).not.toBeVisible();
    await expect(row).toContainText(estadoFinal);
  });

  test("edge path: only valid workflow destinations are offered", async ({ page }) => {
    const { idGestion, estadoInicial, estadoFinal } = await seedManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    await page.getByTestId(`btn-cambiar-estado-${idGestion}`).click();
    const dialog = page.getByRole("dialog");
    await dialog.getByTestId("select-nuevo-estado").click();

    await expect(page.getByRole("option", { name: estadoFinal })).toBeVisible();
    await expect(page.getByRole("option", { name: estadoInicial })).toHaveCount(0);
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`cambiar estado action is usable at ${viewport.label}`, async ({ page }) => {
      const { idGestion, numero, estadoFinal } = await seedManagement(page);

      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto("/dashboard/managements");
      await page.waitForLoadState("domcontentloaded");

      const row = listRow(page, new RegExp(String(numero)));
      await expect(row).toBeVisible({ timeout: 10000 });

      await page.getByTestId(`btn-cambiar-estado-${idGestion}`).click();
      const dialog = page.getByRole("dialog");
      await expect(dialog).toBeVisible();
      await dialog.getByTestId("select-nuevo-estado").click();
      await page.getByRole("option", { name: estadoFinal }).click();
      await dialog.getByTestId("btn-confirmar-transicion").click();
      await expect(dialog).not.toBeVisible();
    });
  }
});
