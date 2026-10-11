/**
 * CU20 - Register a user account (Gherkin style)
 * CU21 - Update a user account
 * CU23 - View a user's activity log
 * CU48 - Register a notary
 * CU51 - Update a notary
 */
import { test, expect, type Page } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";
import { createUser } from "./setup/api-helpers";

/** Capture JWT from HttpOnly cookie (or shared env) for page.request helpers. */
async function syncAdminTokenFromBrowser(page: Page): Promise<void> {
  if (process.env.E2E_ADMIN_TOKEN) {
    return;
  }
  const cookies = await page.context().cookies();
  const authCookie = cookies.find((c) => c.name === "notaire-auth-token");
  if (authCookie?.value) {
    process.env.E2E_ADMIN_TOKEN = authCookie.value;
  }
}

/**
 * Create a disposable EMPLEADO to edit. Never target row nth(1) — that is the
 * seeded admin; renaming it locks out the rest of the suite (login 429).
 */
async function seedEditableUser(page: Page): Promise<string> {
  const username = `cu21-${Date.now()}`;
  const created = await createUser(page, undefined, {
    name: username,
    password: "Test1234!",
    type: "EMPLEADO",
    active: true,
  });
  expect(created.ok, created.error ?? "createUser failed").toBe(true);
  await page.goto("/dashboard/administration/users");
  await page.waitForLoadState("networkidle");
  await expect(page.getByRole("table").getByText(username)).toBeVisible({ timeout: 15000 });
  return username;
}

test.describe("CU20 - Register a user", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administration/users");
  });

  test("CU20-GW01: Given the users page, When new user is clicked, Then the modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Usuarios");

    // When
    await steps.whenUserClicksButton("nuevo usuario");

    // Then — modal title is the catalog string "Nuevo usuario"; form has Name, password, and type
    await steps.thenModalIsVisible("Nuevo usuario");
    await steps.thenFormHasField("nombre");
    await steps.thenFormHasField("contraseña");
    await steps.thenFormHasField("tipo");
  });

  test("CU20-GW02: Given form open, When fill and submit, Then the user is created", async () => {
    // Given
    await steps.whenUserClicksButton("nuevo usuario");
    await steps.thenModalIsVisible();

    // When — fill name via testid; tipo is a Select (combobox), activo defaults to true
    await steps.page.getByTestId("input-name-user").fill(TestData.usuario.username);
    await steps.page.getByLabel(/contraseña/i).fill(TestData.usuario.password);
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("creado");
    await steps.thenTableIsVisible();
  });
});

test.describe("CU21 - Update a user account", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await syncAdminTokenFromBrowser(page);
    await steps.givenUserIsOnPage("/dashboard/administration/users");
  });

  test("CU21-GW01: Given a user exists, When edit is clicked, Then the modal opens with the data", async ({
    page,
  }) => {
    const username = await seedEditableUser(page);

    // When — icon-only edit is named via aria-label (#1057)
    await page
      .getByRole("table")
      .getByRole("row", { name: new RegExp(username) })
      .getByRole("button", { name: /editar/i })
      .click();

    // Then
    await steps.thenModalIsVisible("Editar usuario");
    await expect(page.getByTestId("input-name-user")).toHaveValue(username);
  });

  test("CU21-GW02: Given edit modal open, When modify and submit, Then shows success", async ({
    page,
  }) => {
    const username = await seedEditableUser(page);

    await page
      .getByRole("table")
      .getByRole("row", { name: new RegExp(username) })
      .getByRole("button", { name: /editar/i })
      .click();
    await steps.thenModalIsVisible("Editar usuario");

    const input = page.getByTestId("input-name-user");
    await expect(input).toHaveValue(username);
    // Never rename admin — only mutate the disposable EMPLEADO we just created.
    await input.fill(`${username}_e2e`);
    await steps.whenUserSubmitsForm();

    await steps.thenShowsSuccessMessage("actualizado");
  });
});

test.describe("CU23 - View the activity log", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administration/users");
  });

  test.skip("CU23-GW01 (#1146): Given on usuarios page, When click ver actividades, Then shows log", async () => {
    // Skipped (#1146): no "ver actividades" button on the users page.
  });
});

test.describe("CU48 - Register a notary", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administration/users");
  });

  test.skip("CU48-GW01 (#1146): Given the notaries page, When new notary is clicked, Then the modal opens", async () => {
    // Skipped (#1146): no /dashboard/administration/notaries page exists in the current frontend.
    // Escribanos are managed through the Usuarios module.
  });

  test.skip("CU48-GW02 (#1146): Given the form is open, When it is filled and submitted, Then the notary is created", async () => {
    // Skipped (#1146): same reason as CU48-GW01.
  });
});
