/**
 * CU02 - Iniciar Gestión (Gherkin style)
 * CU13 - Ver historial de gestión
 * CU14 - Consultar estado gestión
 * CU16 - Archivar Gestión
 * CU53 - Modificar Gestión
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";

test.describe("CU02 - Iniciar Gestión", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test("CU02-GW01: Given user on gestiones page, When click nueva gestion, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Gestiones");

    // When
    await steps.whenUserClicksButton("nueva gestión");

    // Then
    await steps.thenModalIsVisible("Nueva Gestión");
    await steps.thenFormHasField("numero");
    await steps.thenFormHasField("fecha inicio");
  });

  test("CU02-GW02: Given form open, When fill and submit, Then gestion created", async () => {
    // Given
    await steps.whenUserClicksButton("nueva gestión");
    await steps.thenModalIsVisible();

    // When
    await steps.whenUserFillsField("numero gestion", TestData.gestion.numeroGestion);
    await steps.whenUserFillsField("detalle", TestData.gestion.detalle);
    await steps.whenUserFillsField("fecha inicio", TestData.gestion.fechaInicio);
    await steps.whenUserSelectsFromDropdown("escribano", "Admin");
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("creada");
    await steps.thenTableIsVisible();
  });

  test("CU02-GW03: Given gestion exists, When click ver detalle, Then shows details", async () => {
    // Given
    await steps.givenModuleIsVisible("Gestiones");

    // When
    await steps.whenUserClicksButton("ver");

    // Then
    await steps.thenPageHasHeading("Detalle Gestión");
  });
});

test.describe("CU13 - Ver historial de gestión", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test("CU13-GW01: Given user on gestiones, When filter by estado, Then shows filtered", async () => {
    // Given
    await steps.givenModuleIsVisible("Gestiones");

    // When
    await steps.whenUserSelectsFromDropdown("estado", "En Proceso");

    // Then
    await steps.thenTableIsVisible();
  });
});

test.describe("CU14 - Consultar estado gestión", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test("CU14-GW01: Given gestion exists, When view gestion, Then shows current status", async () => {
    // Given
    await steps.givenModuleIsVisible("Gestiones");

    // When
    const gestionRow = steps.page.getByRole("row").filter({ hasText: /TEST/ }).first();
    await gestionRow.getByRole("button", { name: /ver/i }).click();

    // Then
    await steps.thenElementIsVisible("Estado");
    await steps.thenElementIsVisible("Fecha Inicio");
  });
});

test.describe("CU16 - Archivar Gestión", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test("CU16-GW01: Given gestion exists, When archivar clicked, Then status changes", async () => {
    // Given
    await steps.givenModuleIsVisible("Gestiones");

    // When
    await steps.whenUserClicksButton("archivar");

    // Then
    await steps.thenModalIsVisible("Confirmar Archivo");
    await steps.whenUserSubmitsForm();
    await steps.thenShowsSuccessMessage("archivada");
  });
});
