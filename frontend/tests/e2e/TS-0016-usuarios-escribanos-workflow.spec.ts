/**
 * CU20 - Dar Alta Usuario (Gherkin style)
 * CU21 - Modificar Usuario
 * CU23 - Ver registro de actividades de usuario
 * CU48 - Dar alta escribano
 * CU51 - Modificar escribano
 */
import { test, expect, type Page } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";
import { createUsuario } from "./setup/api-helpers";

/** Capture JWT from UI login so page.request helpers authenticate. */
async function syncAdminTokenFromBrowser(page: Page): Promise<void> {
  const token = await page.evaluate(() => {
    try {
      const raw = localStorage.getItem("notaire-auth");
      return raw ? (JSON.parse(raw)?.state?.token as string | undefined) : undefined;
    } catch {
      return undefined;
    }
  });
  if (token) {
    process.env.E2E_ADMIN_TOKEN = token;
  }
}

/**
 * Create a disposable EMPLEADO to edit. Never target row nth(1) — that is the
 * seeded admin; renaming it locks out the rest of the suite (login 429).
 */
async function seedEditableUsuario(page: Page): Promise<string> {
  const username = `cu21-${Date.now()}`;
  const created = await createUsuario(page, undefined, {
    name: username,
    password: "Test1234!",
    type: "EMPLEADO",
    active: true,
  });
  expect(created.ok, created.error ?? "createUsuario failed").toBe(true);
  await page.goto("/dashboard/administracion/usuarios");
  await page.waitForLoadState("networkidle");
  await expect(page.getByRole("table").getByText(username)).toBeVisible({ timeout: 15000 });
  return username;
}

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
    await syncAdminTokenFromBrowser(page);
    await steps.givenUserIsOnPage("/dashboard/administracion/usuarios");
  });

  test("CU21-GW01: Given usuario exists, When click editar, Then modal opens with data", async ({
    page,
  }) => {
    const username = await seedEditableUsuario(page);

    // When — icon-only edit is named via aria-label (#1057)
    await page
      .getByRole("table")
      .getByRole("row", { name: new RegExp(username) })
      .getByRole("button", { name: /editar/i })
      .click();

    // Then
    await steps.thenModalIsVisible("Editar usuario");
    await expect(page.getByTestId("input-nombre-usuario")).toHaveValue(username);
  });

  test("CU21-GW02: Given edit modal open, When modify and submit, Then shows success", async ({
    page,
  }) => {
    const username = await seedEditableUsuario(page);

    await page
      .getByRole("table")
      .getByRole("row", { name: new RegExp(username) })
      .getByRole("button", { name: /editar/i })
      .click();
    await steps.thenModalIsVisible("Editar usuario");

    const input = page.getByTestId("input-nombre-usuario");
    await expect(input).toHaveValue(username);
    // Never rename admin — only mutate the disposable EMPLEADO we just created.
    await input.fill(`${username}_e2e`);
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
