/**
 * TS-0156 — High-traffic create forms show required-field errors (#615 / #1955)
 *
 * People, budgets, deeds, managements: empty save → FormField role="alert"
 * and the dialog stays open (Wave 5 client validation).
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

async function expectRequiredAlerts(page: Page) {
  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole("alert").first()).toBeVisible({ timeout: 5000 });
  await expect(dialog).toBeVisible();
}

test.describe("TS-0156 - required field FormField errors (#615)", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("people: empty create shows field alerts", async ({ page }) => {
    await page.goto("/dashboard/people");
    await page.getByTestId("btn-new-person").click();
    await page.getByRole("dialog").getByRole("button", { name: /crear|create/i }).click();
    await expectRequiredAlerts(page);
  });

  test("budgets: empty create shows field alerts", async ({ page }) => {
    await page.goto("/dashboard/budgets");
    await page.getByTestId("btn-new-budget").click();
    await page.getByTestId("btn-save-budget").click();
    await expectRequiredAlerts(page);
  });

  test("deeds: empty create shows field alerts", async ({ page }) => {
    await page.goto("/dashboard/deeds");
    await page.getByTestId("btn-new-deed").click();
    await page.getByRole("dialog").getByRole("button", { name: /crear|create/i }).click();
    await expectRequiredAlerts(page);
  });

  test("managements: empty create shows field alerts", async ({ page }) => {
    await page.goto("/dashboard/managements");
    await page.getByTestId("btn-new-management").click();
    await page.getByTestId("btn-save-management").click();
    await expectRequiredAlerts(page);
  });
});
