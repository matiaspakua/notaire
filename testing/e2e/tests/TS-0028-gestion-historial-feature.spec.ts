/**
 * E2E tests — Bitácora (ManagementHistory) view on the gestiones screen (CU13)
 * Also covers the archive-rejected-by-invalid-transition edge path (CU16, issue #833).
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

test.describe("CU13 - Ver bitácora de una gestión", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: transitioning a gestión adds a new bitácora entry", async ({ page }) => {
    const { idGestion, numero, estadoInicial, estadoFinal } = await seedManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-change-status-${idGestion}`).click();
    let dialog = page.getByRole("dialog");
    await dialog.getByTestId("select-new-status").click();
    await page.getByRole("option", { name: estadoFinal }).click();
    await dialog.getByTestId("btn-confirm-transicion").click();
    await expect(dialog).not.toBeVisible();

    await page.getByTestId(`btn-view-history-${idGestion}`).click();
    dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    const items = dialog.getByTestId("history-item");
    await expect(items).toHaveCount(2);
    await expect(items.nth(0)).toContainText(estadoInicial);
    await expect(items.nth(1)).toContainText(estadoFinal);
  });

  test("edge path: a gestión with only its initial estado shows a single bitácora entry", async ({ page }) => {
    const { idGestion, numero, estadoInicial } = await seedManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-view-history-${idGestion}`).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    const items = dialog.getByTestId("history-item");
    await expect(items).toHaveCount(1);
    await expect(items.first()).toContainText(estadoInicial);
  });

  test("edge path: archiving a gestión whose estado cannot transition to Archivada shows a visible error", async ({
    page,
  }) => {
    const { idGestion, numero } = await seedManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-archive-management-${idGestion}`).click();
    const confirmDialog = page.getByRole("alertdialog");
    await expect(confirmDialog).toBeVisible();
    await confirmDialog.getByRole("button", { name: /archive gestión/i }).click();

    await expect(page.getByText(/is not allowed/i)).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId(`btn-archive-management-${idGestion}`)).toBeVisible();
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`bitácora view is usable at ${viewport.label}`, async ({ page }) => {
      const { idGestion, numero, estadoInicial } = await seedManagement(page);

      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto("/dashboard/managements");
      await page.waitForLoadState("domcontentloaded");

      const row = listRow(page, new RegExp(String(numero)));
      await expect(row).toBeVisible({ timeout: 10000 });

      await page.getByTestId(`btn-view-history-${idGestion}`).click();
      const dialog = page.getByRole("dialog");
      await expect(dialog).toBeVisible();
      await expect(dialog.getByTestId("history-item").first()).toContainText(estadoInicial);
    });
  }
});
