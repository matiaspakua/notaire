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

    // Then — modal title is "Nuevo usuario" (i18n lowercase); form has Nombre, Contraseña, Tipo
    await steps.thenModalIsVisible("Nuevo usuario");
    await steps.thenFormHasField("nombre");
    await steps.thenFormHasField("contraseña");
    await steps.thenFormHasField("tipo");
  });

  test("CU20-GW02: Given form open, When fill and submit, Then usuario created", async () => {
    // Given
    await steps.whenUserClicksButton("nuevo usuario");
    await steps.thenModalIsVisible();

    // When — fill nombre via testid; tipo is a Select (combobox), activo defaults to true
    await steps.page.getByTestId("input-nombre-usuario").fill(TestData.usuario.username);
    await steps.page.getByLabel(/contraseña/i).fill(TestData.usuario.password);
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
    // Given — seeded usuarios table has at least one row
    await expect(steps.page.getByRole("table")).toBeVisible({ timeout: 15000 });

    // When — icon-only edit is named via aria-label (#1057)
    await steps.page
      .getByRole("table")
      .getByRole("row")
      .nth(1)
      .getByRole("button", { name: /editar/i })
      .click();

    // Then
    await steps.thenModalIsVisible("Editar usuario");
    await expect(steps.page.getByTestId("input-nombre-usuario")).not.toHaveValue("");
  });

  test("CU21-GW02: Given edit modal open, When modify and submit, Then shows success", async () => {
    await expect(steps.page.getByRole("table")).toBeVisible({ timeout: 15000 });
    await steps.page
      .getByRole("table")
      .getByRole("row")
      .nth(1)
      .getByRole("button", { name: /editar/i })
      .click();
    await steps.thenModalIsVisible("Editar usuario");

    const input = steps.page.getByTestId("input-nombre-usuario");
    const current = await input.inputValue();
    await input.fill(`${current}_e2e`);
    await steps.whenUserSubmitsForm();

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

  test.skip("CU23-GW01 (#1146): Given on usuarios page, When click ver actividades, Then shows log", async () => {
    // Skipped (#1146): no "ver actividades" button on the usuarios page.
  });
});

test.describe("CU48 - Dar alta escribano", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/usuarios");
  });

  test.skip("CU48-GW01 (#1146): Given on escribanos page, When click nuevo escribano, Then modal opens", async () => {
    // Skipped (#1146): no /dashboard/administracion/escribanos page exists in the current frontend.
    // Escribanos are managed through the Usuarios module.
  });

  test.skip("CU48-GW02 (#1146): Given form open, When fill and submit, Then escribano created", async () => {
    // Skipped (#1146): same reason as CU48-GW01.
  });
});
