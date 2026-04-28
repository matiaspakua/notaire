/**
 * CU01 - Preparar Presupuesto (Gherkin style)
 * Given-When-Then pattern
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";

test.describe("CU01 - Preparar Presupuesto", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
  });

  test("CU01-GW01: Given user on presupuestos page, When click nuevo presupuesto, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Presupuestos");

    // When
    await steps.whenUserClicksButton("nuevo presupuesto");

    // Then
    await steps.thenModalIsVisible("Nuevo Presupuesto");
    await steps.thenFormHasField("fecha");
    await steps.thenFormHasField("monto");
  });

  test("CU01-GW02: Given modal open, When fill required fields and submit, Then presupuesto created", async () => {
    // Given
    await steps.whenUserClicksButton("nuevo presupuesto");
    await steps.thenModalIsVisible();

    // When
    await steps.whenUserFillsField("fecha", TestData.presupuesto.observaciones);
    await steps.whenUserSelectsFromDropdown("tipo tramite", TestData.presupuesto.tipoTramite);
    await steps.whenUserFillsField("observaciones", TestData.presupuesto.observaciones);
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("creado");
    await steps.thenTableIsVisible();
  });

  test("CU01-GW03: Given modal open, When cancel clicked, Then modal closes", async () => {
    // Given
    await steps.whenUserClicksButton("nuevo presupuesto");
    await steps.thenModalIsVisible();

    // When
    await steps.whenUserCancelsForm();

    // Then
    await steps.thenModalIsNotVisible();
  });

  test("CU01-GW04: Given user on page, When search presupuesto, Then table shows results", async () => {
    // Given
    await steps.givenModuleIsVisible("Presupuestos");

    // When
    await steps.whenUserSearches("TEST");

    // Then
    await steps.thenTableIsVisible();
  });
});
