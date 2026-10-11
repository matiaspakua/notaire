/**
 * E2E tests — history view on the managements screen (CU13)
 * Also covers the archive-rejected-by-invalid-transition edge path (CU16, issue #833).
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

test.describe("CU13 - View a management's history", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: transitioning a management adds a new history entry", async ({ page }) => {
    const { idManagement, number, initialStatus, finalStatus } = await seedManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-change-status-${idManagement}`).click();
    let dialog = page.getByRole("dialog");
    await dialog.getByTestId("select-new-status").click();
    await page.getByRole("option", { name: finalStatus }).click();
    await dialog.getByTestId("btn-confirm-transition").click();
    await expect(dialog).not.toBeVisible();

    await page.getByTestId(`btn-view-history-${idManagement}`).click();
    dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    const items = dialog.getByTestId("history-item");
    await expect(items).toHaveCount(2);
    await expect(items.nth(0)).toContainText(initialStatus);
    await expect(items.nth(1)).toContainText(finalStatus);
  });

  test("edge path: a management with only its initial status shows a single history entry", async ({ page }) => {
    const { idManagement, number, initialStatus } = await seedManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-view-history-${idManagement}`).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    const items = dialog.getByTestId("history-item");
    await expect(items).toHaveCount(1);
    await expect(items.first()).toContainText(initialStatus);
  });

  test("edge path: archiving a management whose status cannot transition to Archivada shows a visible error", async ({
    page,
  }) => {
    const { idManagement, number } = await seedManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-archive-management-${idManagement}`).click();
    const confirmDialog = page.getByRole("alertdialog");
    await expect(confirmDialog).toBeVisible();
    await confirmDialog.getByRole("button", { name: /archive gestión/i }).click();

    await expect(page.getByText(/is not allowed/i)).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId(`btn-archive-management-${idManagement}`)).toBeVisible();
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`the history view is usable at ${viewport.label}`, async ({ page }) => {
      const { idManagement, number, initialStatus } = await seedManagement(page);

      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto("/dashboard/managements");
      await page.waitForLoadState("domcontentloaded");

      const row = listRow(page, new RegExp(String(number)));
      await expect(row).toBeVisible({ timeout: 10000 });

      await page.getByTestId(`btn-view-history-${idManagement}`).click();
      const dialog = page.getByRole("dialog");
      await expect(dialog).toBeVisible();
      await expect(dialog.getByTestId("history-item").first()).toContainText(initialStatus);
    });
  }
});
