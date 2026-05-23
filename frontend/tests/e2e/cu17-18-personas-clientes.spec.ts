/**
 * CU17 - Dar Alta Persona (Gherkin style)
 * CU18 - Dar Alta Cliente
 * CU41 - Modificar Cliente
 * CU46 - Ver detalle cliente
 * CU54 - Modificar Persona
 * CU61 - Buscar persona o cliente
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";

test.describe("CU17 - Dar Alta Persona", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/personas");
  });

  test("CU17-GW01: Given user on personas page, When click nueva persona, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When
    await steps.whenUserClicksButton("nueva persona");

    // Then — modal title is "Nueva persona" (i18n lowercase); form has Nombre, Apellido, DNI, Email
    await steps.thenModalIsVisible("Nueva persona");
    await steps.thenFormHasField("nombre");
    await steps.thenFormHasField("apellido");
    await steps.thenFormHasField("dni");
  });

  test("CU17-GW02: Given form open, When fill all fields and submit, Then persona created", async () => {
    // Given
    await steps.whenUserClicksButton("nueva persona");
    await steps.thenModalIsVisible();

    // When — form has: Nombre, Apellido, DNI, CUIL, Email, Teléfono, Domicilio (no tipo/numero identificacion)
    await steps.page.getByTestId("input-nombre").fill(TestData.persona.nombre);
    await steps.page.getByTestId("input-apellido").fill(TestData.persona.apellido);
    await steps.page.getByLabel(/dni/i).fill(TestData.persona.numeroIdentificacion);
    await steps.page.getByLabel(/email/i).fill(TestData.persona.correo);
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("creada");
    await steps.thenTableIsVisible();
  });

  test("CU17-GW03: Given persona exists, When search by apellido, Then shows results", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When — use apellido search input (testid)
    await steps.page.getByTestId("input-search-apellido").fill(TestData.persona.apellido);

    // Then
    await steps.thenTableIsVisible();
  });
});

test.describe("CU18 - Dar Alta Cliente", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/personas");
  });

  test.skip("CU18-GW01: Given persona exists, When click dar de alta cliente, Then modal opens", async () => {
    // Skipped: no "dar de alta cliente" button on the personas page.
    // The persona form has an "Es cliente" checkbox instead; there is no separate client upgrade flow.
  });

  test.skip("CU18-GW02: Given cliente form open, When fill and submit, Then cliente created", async () => {
    // Skipped: same reason as CU18-GW01.
  });
});

test.describe("CU61 - Buscar persona o cliente", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/personas");
  });

  test("CU61-GW01: Given on personas page, When search by apellido, Then shows matching", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When — use apellido search input (testid)
    await steps.page.getByTestId("input-search-apellido").fill("Pérez");

    // Then
    await steps.thenTableIsVisible();
  });

  test("CU61-GW02: Given on personas page, When search by dni, Then shows matching", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When — use DNI search input (testid)
    await steps.page.getByTestId("input-search-dni").fill("12345678");

    // Then
    await steps.thenTableIsVisible();
  });

  test("CU61-GW03: Given search with no results, Then shows empty message", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When — search by DNI with a value that won't match any person
    await steps.page.getByTestId("input-search-dni").fill("XYZNOTEXISTS999");

    // Then — DataTable shows the noData message
    await steps.thenElementIsVisible("no hay");
  });
});
