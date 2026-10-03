/**
 * E2E tests — Workflow assignment to TipoDeTramite
 * CU73: Asignar Workflow a Tipo de Trámite
 * Requires: backend running at localhost:8080, frontend at localhost:3000
 *
 * Arranges its own tipo-tramite via API helpers (#1066) — never skips when the table is empty.
 */
import { test, expect, type Page } from "@playwright/test";
import { createTipoTramite } from "./setup/api-helpers";

async function loginAsAdmin(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
}

/** Create a tipo de trámite; fail the test if arrange/seed fails (do not skip). */
async function arrangeTipoTramite(page: Page): Promise<number> {
  const created = await createTipoTramite(page, {
    name: `Asignación E2E ${Date.now()}`,
  });
  expect(
    created.ok && created.data?.idProcedureType != null,
    `arrange tipo-tramite failed: ${created.status} ${created.error ?? JSON.stringify(created.data)}`,
  ).toBe(true);
  return created.data!.idProcedureType;
}

test.describe("CU73 - Workflow assignment to TipoDeTramite", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await arrangeTipoTramite(page);
    await page.goto("/dashboard/administracion/tramites");
    await expect(page.locator("table tbody tr").first()).toBeVisible({ timeout: 10000 });
  });

  test("tramites page shows workflow column", async ({ page }) => {
    await expect(page.getByRole("columnheader", { name: "Workflow" })).toBeVisible();
    await expect(page.getByTestId("btn-nuevo-tipo-tramite")).toBeVisible();
  });

  test("edit modal shows workflow selector", async ({ page }) => {
    await page.locator("table tbody tr").first().locator("button").first().click();
    await expect(page.getByTestId("select-workflow-tramite")).toBeVisible({ timeout: 5000 });
  });
});
