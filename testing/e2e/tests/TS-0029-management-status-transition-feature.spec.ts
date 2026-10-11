/**
 * E2E tests — status-change action on the managements screen (CU83)
 * Issues: #833, #804 (server rejects PUT status bypass; UI filtered destinations)
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { createPerson, createBudget, seedManagementWithWorkflow } from "./setup/api-helpers";
import { listRow } from "./setup/list-locators";

async function seedManagement(page: Page) {
  const person = await createPerson(page);
  const budget = await createBudget(page, person.data!.personId);
  return seedManagementWithWorkflow(page, budget.data!.idBudget);
}

test.describe("CU83 - Change a management's status", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: transitioning to a valid destination updates the estado", async ({ page }) => {
    const { idManagement, number, finalStatus } = await seedManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-change-status-${idManagement}`).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-new-status").click();
    await page.getByRole("option", { name: finalStatus }).click();
    await dialog.getByTestId("btn-confirm-transition").click();

    await expect(dialog).not.toBeVisible();
    await expect(row).toContainText(finalStatus);
  });

  test("edge path: only valid workflow destinations are offered", async ({ page }) => {
    const { idManagement, initialStatus, finalStatus } = await seedManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    await page.getByTestId(`btn-change-status-${idManagement}`).click();
    const dialog = page.getByRole("dialog");
    await dialog.getByTestId("select-new-status").click();

    await expect(page.getByRole("option", { name: finalStatus })).toBeVisible();
    await expect(page.getByRole("option", { name: initialStatus })).toHaveCount(0);
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`the change-status action is usable at ${viewport.label}`, async ({ page }) => {
      const { idManagement, number, finalStatus } = await seedManagement(page);

      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto("/dashboard/managements");
      await page.waitForLoadState("domcontentloaded");

      const row = listRow(page, new RegExp(String(number)));
      await expect(row).toBeVisible({ timeout: 10000 });

      await page.getByTestId(`btn-change-status-${idManagement}`).click();
      const dialog = page.getByRole("dialog");
      await expect(dialog).toBeVisible();
      await dialog.getByTestId("select-new-status").click();
      await page.getByRole("option", { name: finalStatus }).click();
      await dialog.getByTestId("btn-confirm-transition").click();
      await expect(dialog).not.toBeVisible();
    });
  }
});
