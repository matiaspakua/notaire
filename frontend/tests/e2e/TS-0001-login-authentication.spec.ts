/**
 * TS-0001 - Login Authentication Workflow
 *
 * Covers: AUTH-001 (User Authentication)
 * Test Level: E2E UI/UX (foundation - login form submission)
 * Fixtures: Direct page navigation, form fill, JWT token validation
 *
 * Golden Path:
 *   - Display login form
 *   - Submit with valid credentials
 *   - Redirect to /dashboard
 *
 * Edge Cases:
 *   - Empty submission (validation error)
 *   - Invalid credentials (error message)
 *
 * Requires: backend running at localhost:8080, frontend at localhost:3000
 * Reference: docs/300-development/303-testing/TEST-PLAN.md (TS-0001 row)
 */
import { test, expect } from "@playwright/test";

test.describe("TS-0001 - Login Authentication", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/login");
  });

  test("displays login form", async ({ page }) => {
    await expect(page.getByTestId("input-usuario")).toBeVisible();
    await expect(page.getByTestId("input-contrasenia")).toBeVisible();
    await expect(page.getByTestId("btn-ingresar")).toBeVisible();
  });

  test("shows error for empty submission", async ({ page }) => {
    await page.getByTestId("btn-ingresar").click();
    await expect(page.getByText("Complete usuario y contraseña")).toBeVisible();
  });

  test("shows error for wrong credentials", async ({ page }) => {
    await page.getByTestId("input-usuario").fill("usuario_invalido");
    await page.getByTestId("input-contrasenia").fill("contrasenia_invalida");
    await page.getByTestId("btn-ingresar").click();
    await expect(
      page.getByText(/incorrectos|servidor/i)
    ).toBeVisible({ timeout: 5000 });
  });

  test("redirects to dashboard on successful login", async ({ page }) => {
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
  });
});
