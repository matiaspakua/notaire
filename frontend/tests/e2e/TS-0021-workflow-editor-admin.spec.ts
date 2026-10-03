/**
 * E2E tests — Workflow editor (interactive)
 * CU70: Definir Workflow de Estados de Gestión
 * CU71: Definir Transiciones entre Estados
 * Requires: backend running at localhost:8080, frontend at localhost:3000
 *
 * Arranges its own workflow via API helpers (#1066) — never skips when the table is empty.
 */
import { test, expect, type Page } from "@playwright/test";
import { createWorkflowDefinition } from "./setup/api-helpers";

async function loginAsAdmin(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
}

/** Create a workflow definition; fail the test if arrange/seed fails (do not skip). */
async function arrangeWorkflow(page: Page): Promise<number> {
  const created = await createWorkflowDefinition(page, {
    name: `Editor E2E ${Date.now()}`,
  });
  expect(
    created.ok && created.data?.id != null,
    `arrange workflow failed: ${created.status} ${created.error ?? JSON.stringify(created.data)}`,
  ).toBe(true);
  return created.data!.id;
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
    const nombre = `Test Workflow E2E ${Date.now()}`;
    await page.getByTestId("btn-nuevo-workflow").click();
    await page.getByTestId("input-nombre-workflow").fill(nombre);
    await page.getByTestId("btn-guardar-workflow").click();
    await expect(page.getByText(nombre)).toBeVisible({ timeout: 5000 });
  });
});

test.describe("CU70 - Workflow editor (graph)", () => {
  let workflowId: number;

  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    workflowId = await arrangeWorkflow(page);
    await page.goto("/dashboard/administracion/workflows");
    await expect(page.getByTestId(`btn-editor-${workflowId}`)).toBeVisible({ timeout: 10000 });
  });

  test("editor page loads for a workflow", async ({ page }) => {
    await page.getByTestId(`btn-editor-${workflowId}`).click();
    await expect(page.getByTestId("workflow-editor")).toBeVisible({ timeout: 5000 });
    await expect(page.getByTestId("btn-toggle-edit")).toBeVisible();
    await expect(page.getByTestId("btn-validate")).toBeVisible();
  });

  test("validate workflow button works", async ({ page }) => {
    await page.getByTestId(`btn-editor-${workflowId}`).click();
    await expect(page.getByTestId("workflow-editor")).toBeVisible({ timeout: 5000 });
    await page.getByTestId("btn-validate").click();
    // Assert the inline panel only — toast can appear too; a toast|panel union trips strict mode (#1147).
    await expect(page.getByTestId("validation-errors")).toBeVisible({ timeout: 5000 });
  });

  test("toggle edit mode shows add node button", async ({ page }) => {
    await page.getByTestId(`btn-editor-${workflowId}`).click();
    await expect(page.getByTestId("btn-toggle-edit")).toBeVisible({ timeout: 5000 });
    await page.getByTestId("btn-toggle-edit").click();
    await expect(page.getByTestId("btn-add-node")).toBeVisible();
  });
});
