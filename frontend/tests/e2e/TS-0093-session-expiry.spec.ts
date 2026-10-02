/**
 * TS-0093 - Session expiry / authenticated 401 handling
 *
 * Covers: CU84 – Login al sistema (expired-session alternate flow)
 * Issues: #1053 (global 401 handling), #690 (E2E gap)
 *
 * Golden path:
 *   authenticated user → API 401 (invalid JWT) → /login?expired=1 + message
 *   → successful re-login → /dashboard (no expiry banner)
 *
 * Approach: corrupt the persisted JWT after login so the backend returns a
 * real 401 (avoids waiting for the 24 h JWT TTL).
 */
import { test, expect, type Page } from "@playwright/test";

async function loginAsAdmin(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
}

async function corruptJwt(page: Page) {
  await page.evaluate(() => {
    const raw = window.localStorage.getItem("notaire-auth");
    if (!raw) {
      throw new Error("notaire-auth missing after login");
    }
    const parsed = JSON.parse(raw) as {
      state?: { token?: string; isAuthenticated?: boolean };
    };
    if (!parsed.state) {
      parsed.state = {};
    }
    parsed.state.token = "invalid-expired-jwt-for-e2e";
    parsed.state.isAuthenticated = true;
    window.localStorage.setItem("notaire-auth", JSON.stringify(parsed));
  });
}

test.describe("TS-0093 - Session expiry (CU84 / #1053)", () => {
  test("redirects to login with expired message and allows re-login", async ({ page }) => {
    await loginAsAdmin(page);
    await corruptJwt(page);

    // Any authenticated API call should receive 401 and trigger session expiry.
    await page.goto("/dashboard/gestiones");

    await expect(page).toHaveURL(/\/login\?expired=1/, { timeout: 15000 });
    await expect(page.getByTestId("session-expired-message")).toBeVisible();
    await expect(page.getByTestId("session-expired-message")).toContainText(/sesión ha expirado|session has expired/i);

    const authState = await page.evaluate(() => window.localStorage.getItem("notaire-auth"));
    const parsed = authState ? JSON.parse(authState) : null;
    expect(parsed?.state?.token ?? null).toBeNull();
    expect(parsed?.state?.isAuthenticated ?? false).toBe(false);

    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
    await expect(page.getByTestId("session-expired-message")).toHaveCount(0);
  });

  test("login page shows expired message at mobile, tablet, and desktop widths", async ({
    page,
  }) => {
    for (const width of [320, 768, 1024] as const) {
      await page.setViewportSize({ width, height: 800 });
      await page.goto("/login?expired=1");
      await expect(page.getByTestId("session-expired-message")).toBeVisible();
    }
  });
});
