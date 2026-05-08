/**
 * E2E: Login flow — Prerequisite for all other tests.
 *
 * Verifies the authentication page works correctly:
 * - Form renders with all required elements
 * - Empty submission shows validation error
 * - Invalid credentials show error message
 * - Valid credentials redirect to dashboard
 * - Post-login cookie is set
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";
import { CREDENTIALS } from "./helpers/test-data";

test.describe("Login — Autenticación de usuario", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await page.goto("/login");
    await page.waitForLoadState("networkidle");
  });

  test("GW01: Login form displays all required elements", async () => {
    // Given
    await expect(steps.page.getByTestId("input-usuario")).toBeVisible();
    await expect(steps.page.getByTestId("input-contrasenia")).toBeVisible();
    await expect(steps.page.getByTestId("btn-ingresar")).toBeVisible();

    // Then
    await steps.thenPageHasHeading("Notaire");
    await steps.thenElementIsVisible("Sistema de Gestión");
  });

  test("GW02: Empty submission shows validation error", async () => {
    // When: Click login with empty fields
    await steps.whenUserClicksButton("Ingresar");

    // Then: Shows validation error
    await steps.thenShowsErrorMessage("Complete usuario y contraseña");
  });

  test("GW03: Wrong credentials show error message", async () => {
    // When: Fill with invalid credentials
    await steps.whenUserFillsFieldByTestId("input-usuario", "invalid_user");
    await steps.whenUserFillsFieldByTestId("input-contrasenia", "invalid_pass");
    await steps.whenUserClicksButton("Ingresar");

    // Then: Shows error (either "incorrectos" or connection error)
    await steps.thenShowsErrorMessage(/incorrectos|servidor/);
  });

  test("GW04: Valid credentials redirect to dashboard", async () => {
    // When: Fill with valid credentials and submit
    await steps.whenUserFillsFieldByTestId("input-usuario", CREDENTIALS.username);
    await steps.whenUserFillsFieldByTestId("input-contrasenia", CREDENTIALS.password);
    await steps.whenUserClicksButton("Ingresar");

    // Then: Redirected to dashboard
    await steps.thenUrlIs(/\/dashboard/);

    // Verify auth cookie is set
    const cookies = await steps.page.context().cookies();
    const authCookie = cookies.find((c) => c.name === "notaire-auth-status");
    expect(authCookie).toBeDefined();
    expect(authCookie!.value).toBeTruthy();
  });

  test("GW05: Dashboard shows logged-in user elements", async () => {
    // When: Login successfully
    await steps.whenUserFillsFieldByTestId("input-usuario", CREDENTIALS.username);
    await steps.whenUserFillsFieldByTestId("input-contrasenia", CREDENTIALS.password);
    await steps.whenUserClicksButton("Ingresar");
    await steps.thenUrlIs(/\/dashboard/);

    // Then: Dashboard shows expected modules
    await steps.thenElementIsVisible("Gestiones");
    await steps.thenElementIsVisible("Personas");
    await steps.thenElementIsVisible("Presupuestos");
  });
});
