/**
 * E2E tests — Logout flow
 * CU: Autenticación de usuario (AUTH-001 traceability, gap E-09)
 * Requires: backend running at localhost:8080, frontend at localhost:3000
 */
import { test, expect } from "@playwright/test";

async function loginAsAdmin(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
}

test.describe("Logout flow", () => {
  test("clears session, redirects to login, and blocks protected pages", async ({ page }) => {
    await loginAsAdmin(page);

    await page.getByTestId("btn-logout").click();
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });

    // Login form visible again — UI shows logged-out state
    await expect(page.getByTestId("input-usuario")).toBeVisible();
    await expect(page.getByTestId("btn-ingresar")).toBeVisible();

    // JWT/auth state cleared from localStorage
    const authState = await page.evaluate(() =>
      window.localStorage.getItem("notaire-auth"),
    );
    const parsed = authState ? JSON.parse(authState) : null;
    expect(parsed?.state?.token ?? null).toBeNull();
    expect(parsed?.state?.isAuthenticated ?? false).toBe(false);

    // Protected page is no longer reachable after logout
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });
  });
});
