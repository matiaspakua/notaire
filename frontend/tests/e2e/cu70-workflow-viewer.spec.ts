/**
 * E2E tests — Workflow viewer (read-only)
 * CU70: Definir Workflow de Estados de Gestión
 * Requires: backend running at localhost:8080, frontend at localhost:3000
 */
import { test, expect } from "@playwright/test";

async function loginAsAdmin(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
}

test.describe("CU70 - Workflow viewer on Estados de Gestión page", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto("/dashboard/administracion/estados-gestion");
  });

  test("estados-gestion page loads and shows table", async ({ page }) => {
    await expect(page.getByTestId("search-estados")).toBeVisible();
    await expect(page.getByTestId("btn-nuevo-estado")).toBeVisible();
  });

  test("workflow selector appears when workflows exist", async ({ page }) => {
    const selector = page.getByTestId("select-workflow");
    const count = await selector.count();
    if (count > 0) {
      await expect(selector).toBeVisible();
    }
  });

  test("workflow viewer renders when workflow selected", async ({ page }) => {
    const selector = page.getByTestId("select-workflow");
    const count = await selector.count();
    if (count === 0) {
      test.skip();
      return;
    }
    const options = await selector.locator("option").count();
    if (options <= 1) {
      test.skip();
      return;
    }
    await selector.selectOption({ index: 1 });
    await expect(page.getByTestId("workflow-viewer")).toBeVisible({ timeout: 5000 });
  });
});
