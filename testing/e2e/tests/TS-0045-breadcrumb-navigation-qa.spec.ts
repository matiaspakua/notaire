/**
 * E2E tests — Breadcrumb navigation in admin module
 * Issue #434: Breadcrumb navigation en el módulo de Administración
 */
import { test, expect } from "@playwright/test";

async function loginAsAdmin(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByTestId("input-username").fill("admin");
  await page.getByTestId("input-password").fill("admin");
  await page.getByTestId("btn-sign-in").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
}

test.describe("Breadcrumb navigation — admin module", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test("breadcrumb is visible on admin section pages", async ({ page }) => {
    await page.goto("/dashboard/administration/users");
    await expect(page.getByTestId("breadcrumb")).toBeVisible({ timeout: 5000 });
  });

  test("breadcrumb shows correct segments for usuarios", async ({ page }) => {
    await page.goto("/dashboard/administration/users");
    const breadcrumb = page.getByTestId("breadcrumb");
    await expect(breadcrumb).toContainText("Administración");
    await expect(breadcrumb).toContainText("Usuarios");
  });

  test("breadcrumb 'Administración' segment is a link to admin root", async ({ page }) => {
    await page.goto("/dashboard/administration/users");
    const adminLink = page.getByTestId("breadcrumb").getByRole("link", { name: "Administración" });
    await expect(adminLink).toBeVisible();
    await adminLink.click();
    await expect(page).toHaveURL(/\/dashboard\/administration$/);
  });

  test("breadcrumb shows workflow editor nested route", async ({ page }) => {
    await page.goto("/dashboard/administration/workflows");
    const breadcrumb = page.getByTestId("breadcrumb");
    await expect(breadcrumb).toContainText("Administración");
    await expect(breadcrumb).toContainText("Workflows");
  });
});
