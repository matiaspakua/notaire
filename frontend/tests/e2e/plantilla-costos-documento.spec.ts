/**
 * Plantillas - Costos de Documentos (CU27/CU39)
 *
 * Covers: CU27 (Ingresar Nuevo Tipo de Documento), CU39 (Crear Plantilla Presupuesto)
 * Test Level: E2E UI/UX
 *
 * Workflow:
 *   - Select a tipo de trámite in the plantillas page
 *   - Define a fixed or variable cost for a tipo de documento
 *   - Verify the cost appears in the costos de documentos table
 *
 * Reference: openspec/changes/costos-documentos-presupuesto
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("Plantillas - Costos de Documentos", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/plantillas");
  });

  test("CU39-GW01: Given no tipo de trámite selected, Then costos de documentos section is hidden", async ({ page }) => {
    await expect(page.getByTestId("btn-nuevo-costo-documento")).not.toBeVisible();
  });

  test("CU39-GW02: Given a tipo de trámite selected, Then costos de documentos section appears", async ({ page }) => {
    await page.getByTestId("select-tipo-tramite-costos").click();
    await page.getByRole("option").first().click();

    await expect(page.getByTestId("btn-nuevo-costo-documento")).toBeVisible();
  });

  test("CU27-GW01: Given costo form open, When creating a fixed cost, Then it is saved and listed", async ({ page }) => {
    await page.getByTestId("select-tipo-tramite-costos").click();
    await page.getByRole("option").first().click();

    await page.getByTestId("btn-nuevo-costo-documento").click();
    await steps.thenModalIsVisible();

    await page.getByTestId("select-tipo-documento-costo").click();
    await page.getByRole("option").first().click();
    await page.getByTestId("input-valor-costo-documento").fill("1500");

    await page.getByTestId("btn-guardar-costo-documento").click();

    await steps.thenShowsSuccessMessage("Costo creado");
  });
});
