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

    // Then
    await steps.thenModalIsVisible("Nueva Persona");
    await steps.thenFormHasField("nombre");
    await steps.thenFormHasField("apellido");
    await steps.thenFormHasField("tipo identificacion");
    await steps.thenFormHasField("numero identificacion");
  });

  test("CU17-GW02: Given form open, When fill all fields and submit, Then persona created", async () => {
    // Given
    await steps.whenUserClicksButton("nueva persona");
    await steps.thenModalIsVisible();

    // When
    await steps.whenUserFillsField("nombre", TestData.persona.nombre);
    await steps.whenUserFillsField("apellido", TestData.persona.apellido);
    await steps.whenUserSelectsFromDropdown("tipo identificacion", TestData.persona.tipoIdentificacion);
    await steps.whenUserFillsField("numero identificacion", TestData.persona.numeroIdentificacion);
    await steps.whenUserFillsField("telefono", TestData.persona.telefono);
    await steps.whenUserFillsField("correo", TestData.persona.correo);
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("creada");
    await steps.thenTableIsVisible();
  });

  test("CU17-GW03: Given persona exists, When search by name, Then shows results", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When
    await steps.whenUserSearches(TestData.persona.apellido);

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

  test("CU18-GW01: Given persona exists, When click dar de alta cliente, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When
    await steps.whenUserClicksButton("dar de alta cliente");

    // Then
    await steps.thenModalIsVisible("Nuevo Cliente");
    await steps.thenFormHasField("nacionalidad");
    await steps.thenFormHasField("fecha nacimiento");
    await steps.thenFormHasField("cuit");
  });

  test("CU18-GW02: Given cliente form open, When fill and submit, Then cliente created", async () => {
    // Given
    await steps.whenUserClicksButton("dar de alta cliente");
    await steps.thenModalIsVisible();

    // When
    await steps.fillAndSubmitForm({
      "nacionalidad": TestData.cliente.nacionalidad,
      "fecha nacimiento": TestData.cliente.fechaNacimiento,
      "cuit": TestData.cliente.cuit,
      "estado civil": TestData.cliente.estadoCivil,
      "sexo": TestData.cliente.sexo,
      "ocupacion": TestData.cliente.ocupacion,
      "domicilio": TestData.cliente.domicilio,
    });

    // Then
    await steps.thenShowsSuccessMessage("cliente creado");
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

    // When
    await steps.whenUserSearches("Pérez");

    // Then
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible("Pérez");
  });

  test("CU61-GW02: Given on personas page, When search by dni, Then shows matching", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When
    await steps.whenUserSearches("12345678");

    // Then
    await steps.thenTableIsVisible();
  });

  test("CU61-GW03: Given search with no results, Then shows empty message", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When
    await steps.whenUserSearches("XYZ123NOTEXISTS");

    // Then
    await steps.thenElementIsVisible("no hay");
  });
});
