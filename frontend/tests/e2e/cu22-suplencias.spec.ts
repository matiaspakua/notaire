/**
 * CU22 - Registrar Suplencia (Gherkin style)
 * CU59 - Consultar Suplencias
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU22 - Registrar Suplencia", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/suplencias");
  });

  test("CU22-GW01: Given on suplencias page, When click nueva suplencia, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Suplencias");

    // When
    await steps.whenUserClicksButton("nueva suplencia");

    // Then
    await steps.thenModalIsVisible("Nueva Suplencia");
    await steps.thenFormHasField("escribano titular");
    await steps.thenFormHasField("escribano suplente");
    await steps.thenFormHasField("fecha inicio");
    await steps.thenFormHasField("fecha fin");
  });

  test("CU22-GW02: Given form open, When fill and submit, Then suplencia created", async () => {
    // Given
    await steps.whenUserClicksButton("nueva suplencia");
    await steps.thenModalIsVisible();

    // When
    await steps.whenUserSelectsFromDropdown("escribano titular", "Admin");
    await steps.whenUserSelectsFromDropdown("escribano suplente", "Admin");
    await steps.whenUserFillsField("fecha inicio", "2026-05-01");
    await steps.whenUserFillsField("fecha fin", "2026-05-15");
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("creada");
    await steps.thenTableIsVisible();
  });
});

test.describe("CU59 - Consultar Suplencias", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/suplencias");
  });

  test("CU59-GW01: Given on suplencias, When filter by escribano, Then shows filtered", async () => {
    // Given
    await steps.givenModuleIsVisible("Suplencias");

    // When
    await steps.whenUserSelectsFromDropdown("escribano", "Admin");

    // Then
    await steps.thenTableIsVisible();
  });

  test("CU59-GW02: Given suplencia exists, When click ver detalle, Then shows details", async () => {
    // Given
    await steps.givenModuleIsVisible("Suplencias");

    // When
    await steps.whenUserClicksButton("ver");

    // Then
    await steps.thenPageHasHeading("Detalle Suplencia");
    await steps.thenElementIsVisible("Escribano Titular");
    await steps.thenElementIsVisible("Fecha Inicio");
  });
});
