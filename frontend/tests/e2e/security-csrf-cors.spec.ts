/**
 * E2E tests — CSRF protection posture and CORS enforcement (issue #691)
 * CU: Autenticación de usuario / Seguridad
 *
 * The API is a stateless JWT bearer-token REST API (SecurityAndCorsConfig
 * disables CSRF on every filter chain). CSRF exploits rely on the browser
 * automatically attaching ambient credentials (session cookies) to a
 * forged cross-site request; here the only credential is a Bearer token
 * that a page must read from localStorage and attach explicitly, which a
 * different origin's script cannot do. These tests document that posture
 * end-to-end and verify CORS actually blocks a disallowed origin from
 * reading API responses, so the "no CSRF" design isn't silently
 * undermined by an overly permissive CORS configuration.
 *
 * Requires: backend running at localhost:8080, frontend at localhost:3000
 */
import { test, expect, request } from "@playwright/test";

const BACKEND_URL = "http://localhost:8080";
const DISALLOWED_ORIGIN = "https://malicious-site.example";
const ALLOWED_ORIGIN = "http://localhost:3000";

test.describe("CSRF posture (issue #691)", () => {
  test("login does not set a session/auth cookie — only a non-sensitive UI marker", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });

    const cookies = await page.context().cookies();
    // The only cookie set is a client-side, non-sensitive marker used by the
    // Next.js middleware for route gating (frontend/src/middleware.ts) — it
    // carries no credential and is not read by the backend for authentication.
    expect(cookies.map((c) => c.name)).toEqual(["notaire-auth-status"]);
  });

  test("a request carrying cookies but no Authorization header is rejected", async ({ page }) => {
    // Simulates what a forged cross-site request could achieve: the browser
    // would attach cookies automatically, but never the Authorization header
    // (it lives in localStorage, unreadable cross-origin). Without it, the
    // backend's JwtAuthenticationFilter rejects the request outright.
    await page.goto("/login");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });

    const response = await page.request.get(`${BACKEND_URL}/api/v1/personas`);
    expect(response.status()).toBe(401);
  });
});

test.describe("CORS enforcement (issue #691)", () => {
  test("preflight from a disallowed origin gets no Access-Control-Allow-Origin header", async () => {
    const apiContext = await request.newContext();
    const response = await apiContext.fetch(`${BACKEND_URL}/api/v1/personas`, {
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
    const response = await apiContext.fetch(`${BACKEND_URL}/api/v1/personas`, {
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
