/**
 * CU05 - Preparar Escritura (Gherkin style)
 * CU06 - Firmar escritura
 * CU52 - Modificar Escritura
 * CU63 - Buscar Escritura
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";

test.describe("CU05 - Preparar Escritura", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test("CU05-GW01: Given user on escrituras page, When click nueva escritura, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Escrituras");

    // When
    await steps.whenUserClicksButton("nueva escritura");

    // Then
    await steps.thenModalIsVisible("Nueva Escritura");
    await steps.thenFormHasField("numero escritura");
    await steps.thenFormHasField("fecha");
  });

  test("CU05-GW02: Given form open, When fill and submit, Then escritura created", async () => {
    // Given
    await steps.whenUserClicksButton("nueva escritura");
    await steps.thenModalIsVisible();

    // When
    await steps.whenUserFillsField("numero escritura", TestData.escritura.numeroEscritura);
    await steps.whenUserFillsField("fecha", TestData.escritura.fecha);
    await steps.whenUserFillsField("cuerpo", TestData.escritura.cuerpo);
    await steps.whenUserSelectsFromDropdown("estado", TestData.escritura.estado);
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("creada");
    await steps.thenTableIsVisible();
  });
});

test.describe("CU06 - Firmar Escritura", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test("CU06-GW01: Given escritura exists, When click firmar, Then status changes", async () => {
    // Given
    await steps.givenModuleIsVisible("Escrituras");

    // When
    await steps.whenUserClicksButton("firmar");

    // Then
    await steps.thenModalIsVisible("Confirmar Firma");
    await steps.whenUserSubmitsForm();
    await steps.thenShowsSuccessMessage("firmada");
  });
});

test.describe("CU52 - Modificar Escritura", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test("CU52-GW01: Given escritura exists, When click edit, Then modal opens with data", async () => {
    // Given
    await steps.givenModuleIsVisible("Escrituras");

    // When
    await steps.whenUserClicksButton("editar");

    // Then
    await steps.thenModalIsVisible("Modificar Escritura");
    await steps.thenFormHasField("cuerpo");
  });

  test("CU52-GW02: Given edit modal open, When modify and submit, Then shows success", async () => {
    // Given
    await steps.whenUserClicksButton("editar");
    await steps.thenModalIsVisible();

    // When
    await steps.whenUserFillsField("cuerpo", "Contenido modificado");
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("actualizada");
  });
});

test.describe("CU63 - Buscar Escritura", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test("CU63-GW01: Given on escrituras page, When search by number, Then shows results", async () => {
    // Given
    await steps.givenModuleIsVisible("Escrituras");

    // When
    await steps.whenUserSearches("ESC");

    // Then
    await steps.thenTableIsVisible();
  });
});
