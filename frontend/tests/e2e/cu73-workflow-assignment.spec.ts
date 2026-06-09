/**
 * E2E tests — Workflow assignment to TipoDeTramite
 * CU73: Asignar Workflow a Tipo de Trámite
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

test.describe("CU73 - Workflow assignment to TipoDeTramite", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto("/dashboard/administracion/tramites");
  });

  test("tramites page shows workflow column", async ({ page }) => {
    await expect(page.getByText("Workflow")).toBeVisible();
    await expect(page.getByTestId("btn-nuevo-tipo-tramite")).toBeVisible();
  });

  test("edit modal shows workflow selector", async ({ page }) => {
    const editButtons = page.locator("button").filter({ hasText: /\S/ }).first();
    const count = await page.locator("table tbody tr").count();
    if (count === 0) {
      test.skip();
      return;
    }
    await page.locator("table tbody tr").first().locator("button").first().click();
    await expect(page.getByTestId("select-workflow-tramite")).toBeVisible({ timeout: 5000 });
    void editButtons;
  });
});
