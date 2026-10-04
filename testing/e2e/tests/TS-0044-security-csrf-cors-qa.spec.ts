/**
 * E2E tests — CSRF protection posture and CORS enforcement (issue #691 / #1051)
 * CU: Autenticación de usuario / Seguridad
 *
 * Browser sessions now use an HttpOnly SameSite=Lax JWT cookie via the
 * same-origin Next proxy. CSRF remains disabled on the API; ambient cookie
 * risk is mitigated by SameSite=Lax + same-origin proxy + strict CORS
 * allowlist. These tests document that cookie-era posture.
 *
 * Requires: backend running at localhost:8080, frontend at localhost:3000
 */
import { test, expect, request } from "@playwright/test";

const BACKEND_URL = "http://localhost:8080";
const DISALLOWED_ORIGIN = "https://malicious-site.example";
const ALLOWED_ORIGIN = "http://localhost:3000";

test.describe("CSRF / cookie posture (issues #691 / #1051)", () => {
  test("login sets HttpOnly auth cookie plus non-credential UX markers", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });

    const cookies = await page.context().cookies();
    const names = cookies.map((c) => c.name).sort();
    expect(names).toEqual(
      expect.arrayContaining([
        "notaire-auth-token",
        "notaire-auth-role",
        "notaire-auth-status",
      ]),
    );

    const authToken = cookies.find((c) => c.name === "notaire-auth-token");
    expect(authToken?.httpOnly).toBe(true);
    expect(authToken?.sameSite?.toLowerCase()).toBe("lax");

    const uxCookies = cookies.filter((c) =>
      c.name === "notaire-auth-role" || c.name === "notaire-auth-status",
    );
    expect(uxCookies.every((c) => c.httpOnly !== true)).toBe(true);

    // JWT must not be script-readable in localStorage
    const authState = await page.evaluate(() => window.localStorage.getItem("notaire-auth"));
    const parsed = authState ? JSON.parse(authState) : null;
    expect(parsed?.state?.token ?? null).toBeNull();
  });

  test("same-origin proxied API call authenticates via cookie without Bearer", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });

    const response = await page.request.get("http://localhost:3000/api/v1/people");
    expect(response.status()).toBe(200);
  });
});

test.describe("CORS enforcement (issue #691)", () => {
  test("preflight from a disallowed origin gets no Access-Control-Allow-Origin header", async () => {
    const apiContext = await request.newContext();
    const response = await apiContext.fetch(`${BACKEND_URL}/api/v1/people`, {
      method: "OPTIONS",
      headers: {
        Origin: DISALLOWED_ORIGIN,
        "Access-Control-Request-Method": "GET",
        "Access-Control-Request-Headers": "Authorization",
      },
    });
    expect(response.headers()["access-control-allow-origin"]).toBeUndefined();
    await apiContext.dispose();
  });

  test("preflight from an allowed origin gets an exact (non-wildcard) Access-Control-Allow-Origin", async () => {
    const apiContext = await request.newContext();
    const response = await apiContext.fetch(`${BACKEND_URL}/api/v1/people`, {
      method: "OPTIONS",
      headers: {
        Origin: ALLOWED_ORIGIN,
        "Access-Control-Request-Method": "GET",
        "Access-Control-Request-Headers": "Authorization",
      },
    });
    expect(response.headers()["access-control-allow-origin"]).toBe(ALLOWED_ORIGIN);
    await apiContext.dispose();
  });
});
