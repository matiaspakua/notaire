/**
 * E2E tests — Workflow editor (interactive)
 * CU70: Definir Workflow de Estados de Gestión
 * CU71: Definir Transiciones entre Estados
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

test.describe("CU70 - Workflow manager (CRUD)", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto("/dashboard/administracion/workflows");
  });

  test("workflows page loads", async ({ page }) => {
    await expect(page.getByTestId("btn-nuevo-workflow")).toBeVisible();
    await expect(page.getByTestId("search-workflows")).toBeVisible();
  });

  test("can create a new workflow", async ({ page }) => {
    await page.getByTestId("btn-nuevo-workflow").click();
    await page.getByTestId("input-nombre-workflow").fill("Test Workflow E2E");
    await page.getByTestId("btn-guardar-workflow").click();
    await expect(page.getByText("Test Workflow E2E")).toBeVisible({ timeout: 5000 });
  });
});

test.describe("CU70 - Workflow editor (graph)", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto("/dashboard/administracion/workflows");
  });

  test("editor page loads for a workflow", async ({ page }) => {
    const editorLinks = page.locator("[data-testid^=btn-editor-]");
    const count = await editorLinks.count();
    if (count === 0) {
      test.skip();
      return;
    }
    await editorLinks.first().click();
    await expect(page.getByTestId("workflow-editor")).toBeVisible({ timeout: 5000 });
    await expect(page.getByTestId("btn-toggle-edit")).toBeVisible();
    await expect(page.getByTestId("btn-validate")).toBeVisible();
  });

  test("validate workflow button works", async ({ page }) => {
    const editorLinks = page.locator("[data-testid^=btn-editor-]");
    const count = await editorLinks.count();
    if (count === 0) {
      test.skip();
      return;
    }
    await editorLinks.first().click();
    await expect(page.getByTestId("workflow-editor")).toBeVisible({ timeout: 5000 });
    await page.getByTestId("btn-validate").click();
    await expect(page.locator("[data-sonner-toast]").or(page.getByTestId("validation-errors")))
      .toBeVisible({ timeout: 5000 });
  });

  test("toggle edit mode shows add node button", async ({ page }) => {
    const editorLinks = page.locator("[data-testid^=btn-editor-]");
    const count = await editorLinks.count();
    if (count === 0) {
      test.skip();
      return;
    }
    await editorLinks.first().click();
    await expect(page.getByTestId("btn-toggle-edit")).toBeVisible({ timeout: 5000 });
    await page.getByTestId("btn-toggle-edit").click();
    await expect(page.getByTestId("btn-add-node")).toBeVisible();
  });
});
