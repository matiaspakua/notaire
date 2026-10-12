/**
 * CU20 - Register a user account (Gherkin style)
 * CU21 - Update a user account
 * CU23 - View a user's activity log (#1545: user + date-range filter on /dashboard/audit)
 * CU48 - Register a notary
 * CU51 - Update a notary
 */
import { test, expect, type Page } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";
import { apiGet, createManagementStatus, createUser } from "./setup/api-helpers";

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

    // When — fill name via testid; type is a Select (combobox), active defaults to true
    await steps.page.getByTestId("input-name-user").fill(TestData.user.username);
    await steps.page.getByLabel(/contraseña/i).fill(TestData.user.password);
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
  });

  test("CU23-GW01 (#1545): Given admin made a change today, When the audit log is filtered by admin and today, Then only admin's records of today are shown", async ({ page }) => {
    await syncAdminTokenFromBrowser(page);
    const status = await createManagementStatus(page);
    expect(status.ok, status.error).toBe(true);
    const users = await apiGet<Array<{ idUser: number; name: string }>>(page, "/users");
    const admin = users.data!.find((u) => u.name === "admin")!;
    const now = new Date();
    const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;

    await steps.givenUserIsOnPage("/dashboard/audit");
    // Each filter lands in the URL before the next one is set.
    await page.getByTestId("select-audit-user").selectOption(String(admin.idUser));
    await expect(page).toHaveURL(new RegExp(`user=${admin.idUser}`));
    await page.getByTestId("input-audit-from").fill(today);
    await expect(page).toHaveURL(new RegExp(`from=${today}`));
    await page.getByTestId("input-audit-to").fill(today);
    await expect(page).toHaveURL(new RegExp(`user=${admin.idUser}&from=${today}&to=${today}`));

    const rows = page.locator("table tbody tr");
    await expect(rows.first()).toBeVisible();
    await expect(rows.filter({ hasText: "ManagementStatuses" }).first()).toBeVisible();
    const userCells = await rows.locator("td:nth-child(3)").allTextContents();
    expect(userCells.length).toBeGreaterThan(0);
    expect(userCells.every((c) => c.trim() === "admin")).toBe(true);

    // An inverted range is an inline error.
    await page.getByTestId("input-audit-from").fill("2031-05-15");
    await expect(page).toHaveURL(/from=2031-05-15/);
    await page.getByTestId("input-audit-to").fill("2031-05-14");
    await expect(page.getByTestId("audit-range-error")).toBeVisible();
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
    // Notaries are managed through the users module. The stored role name Escribano stays.
  });

  test.skip("CU48-GW02 (#1146): Given the form is open, When it is filled and submitted, Then the notary is created", async () => {
    // Skipped (#1146): same reason as CU48-GW01.
  });
});
