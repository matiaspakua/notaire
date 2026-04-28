/**
 * CU15 - Procesar Pago (Gherkin style)
 * CU47 - Consultar Pago
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";

test.describe("CU15 - Procesar Pago", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/pagos");
  });

  test("CU15-GW01: Given user on pagos page, When click registrar pago, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Pagos");

    // When
    await steps.whenUserClicksButton("registrar pago");

    // Then
    await steps.thenModalIsVisible("Registrar Pago");
    await steps.thenFormHasField("fecha");
    await steps.thenFormHasField("monto");
    await steps.thenFormHasField("metodo");
  });

  test("CU15-GW02: Given form open, When fill and submit, Then pago registered", async () => {
    // Given
    await steps.whenUserClicksButton("registrar pago");
    await steps.thenModalIsVisible();

    // When
    await steps.whenUserFillsField("fecha", new Date().toISOString().split("T")[0]);
    await steps.whenUserFillsField("monto", "1000");
    await steps.whenUserSelectsFromDropdown("metodo", "Efectivo");
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("registrado");
    await steps.thenTableIsVisible();
  });

  test("CU15-GW03: Given pago exists, When click ver detalle, Then shows details", async () => {
    // Given
    await steps.givenModuleIsVisible("Pagos");

    // When
    await steps.whenUserClicksButton("ver");

    // Then
    await steps.thenPageHasHeading("Detalle Pago");
    await steps.thenElementIsVisible("Fecha");
    await steps.thenElementIsVisible("Monto");
  });
});

test.describe("CU47 - Consultar Pago", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/pagos");
  });

  test("CU47-GW01: Given on pagos page, When filter by date, Then shows filtered", async () => {
    // Given
    await steps.givenModuleIsVisible("Pagos");

    // When
    await steps.whenUserFillsField("fecha desde", "2024-01-01");
    await steps.whenUserFillsField("fecha hasta", "2024-12-31");

    // Then
    await steps.thenTableIsVisible();
  });
});
