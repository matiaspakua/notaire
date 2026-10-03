/**
 * E2E tests — Logout flow
 * CU: Autenticación de usuario (AUTH-001 traceability, gap E-09)
 * Issue #1051 — HttpOnly JWT cookie cleared on logout
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

    const cookiesAfterLogin = await page.context().cookies();
    expect(cookiesAfterLogin.some((c) => c.name === "notaire-auth-token" && c.httpOnly)).toBe(true);

    await page.getByTestId("btn-logout").click();
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });

    // Login form visible again — UI shows logged-out state
    await expect(page.getByTestId("input-usuario")).toBeVisible();
    await expect(page.getByTestId("btn-ingresar")).toBeVisible();

    // JWT must not remain in localStorage; client auth flag cleared
    const authState = await page.evaluate(() =>
      window.localStorage.getItem("notaire-auth"),
    );
    const parsed = authState ? JSON.parse(authState) : null;
    expect(parsed?.state?.token ?? null).toBeNull();
    expect(parsed?.state?.isAuthenticated ?? false).toBe(false);

    // HttpOnly auth cookie cleared (Max-Age=0 / absent)
    const cookiesAfterLogout = await page.context().cookies();
    const authCookie = cookiesAfterLogout.find((c) => c.name === "notaire-auth-token");
    expect(!authCookie || !authCookie.value).toBe(true);

    // Protected page is no longer reachable after logout
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });
  });
});
