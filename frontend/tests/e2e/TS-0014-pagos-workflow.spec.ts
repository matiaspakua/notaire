/**
 * CU15 - Procesar Pago (Gherkin style)
 * CU47 - Consultar Pago
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU15 - Procesar Pago", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/pagos");
  });

  test("CU15-GW01: Given user on pagos page, When click nuevo pago, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Pagos");

    // When — button text is t("newPago") = "Nuevo pago"
    await steps.whenUserClicksButton("nuevo pago");

    // Then — modal title is "Nuevo pago"; form has Fecha, Monto and Método de Pago fields
    await steps.thenModalIsVisible("Nuevo pago");
    await steps.thenFormHasField("fecha");
    await steps.thenFormHasField("monto");
    await steps.thenFormHasField("método");
  });

  test.skip("CU15-GW02: Given form open, When fill and submit, Then pago registered", async () => {
    // Skipped: requires a valid presupuesto ID in the database. Global setup does not seed a
    // presupuesto (presupuestoId is null after setup). Re-enable when seed data includes presupuesto.
  });

  test.skip("CU15-GW03: Given pago exists, When click ver detalle, Then shows details", async () => {
    // Skipped: pagos table has no "ver" button per row — only edit/delete icons.
  });
});

test.describe("CU47 - Consultar Pago", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/pagos");
  });

  test.skip("CU47-GW01: Given on pagos page, When filter by date, Then shows filtered", async () => {
    // Skipped: pagos page has no "fecha desde" / "fecha hasta" filter inputs.
  });
});
