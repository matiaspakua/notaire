/**
 * CU03 - Listar documentos y certificados necesarios (Gherkin style)
 * CU04 - Registrar documentación cliente
 * CU07 - Generar testimonio
 * CU08 - Verificar Testimonio
 * CU09 - Registrar deudas documentos de Cliente
 * CU10 - Registrar movimientos documentación de entidades externas
 * CU11 - Ingresar para inscripción
 * CU12 - Retirar testimonio
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU03 - Listar documentos y certificados necesarios", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test("CU03-GW01: Given on escrituras, When select gestión, Then shows documentos", async () => {
    // Given
    await steps.givenModuleIsVisible("Escrituras");

    // When
    await steps.whenUserClicksButton("ver documentos");

    // Then
    await steps.thenModalIsVisible("Documentos Necesarios");
    await steps.thenElementIsVisible("Certificados");
  });
});

test.describe("CU04 - Registrar documentación cliente", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/personas");
  });

  test("CU04-GW01: Given on personas, When click documentación, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When
    await steps.whenUserClicksButton("documentación");

    // Then
    await steps.thenModalIsVisible("Documentación Cliente");
    await steps.thenFormHasField("tipo documento");
  });
});

test.describe("CU07 - Generar testimonio", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test("CU07-GW01: Given escritura exists, When click generar testimonio, Then generates", async () => {
    // Given
    await steps.givenModuleIsVisible("Escrituras");

    // When
    await steps.whenUserClicksButton("generar testimonio");

    // Then
    await steps.thenShowsSuccessMessage("Testimonio generado");
  });
});

test.describe("CU08 - Verificar Testimonio", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test("CU08-GW01: Given testimonio exists, When click ver, Then shows testimonio", async () => {
    // Given
    await steps.givenModuleIsVisible("Escrituras");

    // When
    await steps.whenUserClicksButton("ver testimonio");

    // Then
    await steps.thenPageHasHeading("Testimonio");
    await steps.thenElementIsVisible("Firma");
  });
});

test.describe("CU11 - Ingresar para inscripción", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test("CU11-GW01: Given gestión exists, When click inscripción, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Gestiones");

    // When
    await steps.whenUserClicksButton("inscripción");

    // Then
    await steps.thenModalIsVisible("Inscripción");
    await steps.thenFormHasField("fecha");
  });
});

test.describe("CU12 - Retirar testimonio", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test("CU12-GW01: Given testimonio ready, When click retirar, Then confirms", async () => {
    // Given
    await steps.givenModuleIsVisible("Escrituras");

    // When
    await steps.whenUserClicksButton("retirar testimonio");

    // Then
    await steps.thenModalIsVisible("Confirmar Retiro");
    await steps.whenUserSubmitsForm();
    await steps.thenShowsSuccessMessage("retirado");
  });
});
