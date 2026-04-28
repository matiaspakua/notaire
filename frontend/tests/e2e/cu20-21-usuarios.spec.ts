/**
 * CU20 - Dar Alta Usuario (Gherkin style)
 * CU21 - Modificar Usuario
 * CU23 - Ver registro de actividades de usuario
 * CU48 - Dar alta escribano
 * CU51 - Modificar escribano
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";

test.describe("CU20 - Dar Alta Usuario", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/usuarios");
  });

  test("CU20-GW01: Given on usuarios page, When click nuevo usuario, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Usuarios");

    // When
    await steps.whenUserClicksButton("nuevo usuario");

    // Then
    await steps.thenModalIsVisible("Nuevo Usuario");
    await steps.thenFormHasField("nombre usuario");
    await steps.thenFormHasField("contraseña");
    await steps.thenFormHasField("tipo");
  });

  test("CU20-GW02: Given form open, When fill and submit, Then usuario created", async () => {
    // Given
    await steps.whenUserClicksButton("nuevo usuario");
    await steps.thenModalIsVisible();

    // When
    await steps.whenUserFillsField("nombre usuario", TestData.usuario.username);
    await steps.whenUserFillsField("contraseña", TestData.usuario.password);
    await steps.whenUserSelectsFromDropdown("tipo", TestData.usuario.tipo);
    await steps.whenUserSelectsFromDropdown("estado", TestData.usuario.estado);
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("creado");
    await steps.thenTableIsVisible();
  });
});

test.describe("CU21 - Modificar Usuario", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/usuarios");
  });

  test("CU21-GW01: Given usuario exists, When click editar, Then modal opens with data", async () => {
    // Given
    await steps.givenModuleIsVisible("Usuarios");

    // When
    await steps.whenUserClicksButton("editar");

    // Then
    await steps.thenModalIsVisible("Modificar Usuario");
    await steps.thenFormHasField("tipo");
  });

  test("CU21-GW02: Given edit modal open, When modify and submit, Then shows success", async () => {
    // Given
    await steps.whenUserClicksButton("editar");
    await steps.thenModalIsVisible();

    // When
    await steps.whenUserSelectsFromDropdown("tipo", "ADMIN");
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("actualizado");
  });
});

test.describe("CU23 - Ver registro de actividades", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/usuarios");
  });

  test("CU23-GW01: Given on usuarios page, When click ver actividades, Then shows log", async () => {
    // Given
    await steps.givenModuleIsVisible("Usuarios");

    // When
    await steps.whenUserClicksButton("ver actividades");

    // Then
    await steps.thenPageHasHeading("Registro de Actividades");
    await steps.thenTableIsVisible();
  });
});

test.describe("CU48 - Dar alta escribano", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/escribanos");
  });

  test("CU48-GW01: Given on escribanos page, When click nuevo escribano, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Escribanos");

    // When
    await steps.whenUserClicksButton("nuevo escribano");

    // Then
    await steps.thenModalIsVisible("Nuevo Escribano");
  });

  test("CU48-GW02: Given form open, When fill and submit, Then escribano created", async () => {
    // Given
    await steps.whenUserClicksButton("nuevo escribano");
    await steps.thenModalIsVisible();

    // When
    await steps.fillAndSubmitForm({
      "nombre": "Escribano Test",
      "matricula": "MAT-001",
    });

    // Then
    await steps.thenShowsSuccessMessage("creado");
  });
});
