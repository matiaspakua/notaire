/**
 * Items - Descuentos y Recargos (CU45/CU71)
 *
 * Covers: CU45 (Modificar Presupuesto), CU71 (Gestión de Items)
 * Test Level: E2E UI/UX
 *
 * Workflow:
 *   - Open the item form and select tipo DESCUENTO/RECARGO
 *   - Motivo becomes required and is enforced client-side
 *   - Query the descuentos/recargos report for a presupuesto
 *
 * Reference: openspec/changes/descuentos-recargos-presupuesto
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("Items - Descuentos y Recargos", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/items");
  });

  test("CU71-GW01: Given nuevo ítem, When tipo is Descuento, Then motivo field is required", async ({ page }) => {
    await page.getByTestId("btn-nuevo-item").click();
    await steps.thenModalIsVisible();

    await expect(page.getByTestId("input-motivo")).not.toBeVisible();

    await page.getByTestId("select-tipo-item").click();
    await page.getByRole("option", { name: "Descuento" }).click();

    await expect(page.getByTestId("input-motivo")).toBeVisible();
  });

  test("CU71-GW02: Given tipo Descuento, When saving without motivo, Then shows validation error", async ({ page }) => {
    await page.getByTestId("btn-nuevo-item").click();
    await page.getByTestId("select-tipo-item").click();
    await page.getByRole("option", { name: "Recargo" }).click();

    await page.getByTestId("btn-guardar-item").click();

    await steps.thenShowsErrorMessage("motivo es obligatorio");
    await steps.thenModalIsVisible();
  });

  test("CU45-GW01: Given a presupuesto ID, When consultar descuentos y recargos, Then report table renders", async ({ page }) => {
    await page.getByTestId("input-reporte-presupuesto-id").fill("999999");
    await page.getByTestId("btn-consultar-reporte").click();

    await expect(page.getByRole("table").last()).toBeVisible();
  });
});
