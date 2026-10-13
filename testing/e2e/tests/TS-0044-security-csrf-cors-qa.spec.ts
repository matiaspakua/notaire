/**
 * E2E tests — CSRF protection posture and CORS enforcement (issues #691 / #1051)
 * CU: User authentication / security
 *
 * Browser sessions use an HttpOnly SameSite=Lax JWT cookie via the same-origin
 * Next proxy. Since #1618/#1619 the backend also enforces CSRF with the
 * double-submit cookie pattern (CU78): every API response issues a
 * non-HttpOnly `XSRF-TOKEN` cookie and the client must echo its value in the
 * `X-XSRF-TOKEN` header on unsafe requests (see frontend/src/lib/api-client.ts).
 * Bearer-token clients are exempt because they are not vulnerable to CSRF
 * (issue #1620).
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
    await page.getByTestId("input-username").fill("admin");
    await page.getByTestId("input-password").fill("admin");
    await page.getByTestId("btn-sign-in").click();
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
    await page.getByTestId("input-username").fill("admin");
    await page.getByTestId("input-password").fill("admin");
    await page.getByTestId("btn-sign-in").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });

    const response = await page.request.get("http://localhost:3000/api/v1/people");
    expect(response.status()).toBe(200);
  });

  test("browser double-submit — the XSRF-TOKEN cookie echoed as X-XSRF-TOKEN is accepted (#1618)", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-username").fill("admin");
    await page.getByTestId("input-password").fill("admin");
    await page.getByTestId("btn-sign-in").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });

    const xsrf = (await page.context().cookies()).find((c) => c.name === "XSRF-TOKEN");
    expect(xsrf?.value).toBeTruthy();
    expect(xsrf?.httpOnly).not.toBe(true);

    const created = await page.request.post("/api/v1/procedure-types", {
      headers: { "X-XSRF-TOKEN": xsrf!.value },
      data: {
        name: `e2e-csrf-${Date.now()}`,
        notes: "e2e csrf probe",
        isArchived: false,
        isRegistered: false,
      },
    });
    expect(created.status()).toBe(201);

    // The nonce is a stateless double-submit value, so it must survive the
    // request: a client that reads document.cookie just before sending must not
    // be raced by its own parallel traffic (issue #1620).
    const after = (await page.context().cookies()).find((c) => c.name === "XSRF-TOKEN");
    expect(after?.value).toBe(xsrf!.value);
  });

  test("state-changing cookie request without X-XSRF-TOKEN is rejected with 403 (#1618)", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-username").fill("admin");
    await page.getByTestId("input-password").fill("admin");
    await page.getByTestId("btn-sign-in").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });

    const rejected = await page.request.post("/api/v1/procedure-types", {
      data: {
        name: `e2e-csrf-blocked-${Date.now()}`,
        notes: "e2e csrf probe",
        isArchived: false,
        isRegistered: false,
      },
    });
    expect(rejected.status()).toBe(403);
  });

  test("Bearer-token client is exempt from CSRF (#1620)", async ({ request: apiRequest }) => {
    const login = await apiRequest.post("/api/v1/users/login", {
      data: { name: "admin", password: "admin" },
    });
    expect(login.ok()).toBeTruthy();
    const { token } = (await login.json()) as { token: string };

    const created = await apiRequest.post("/api/v1/procedure-types", {
      headers: { Authorization: `Bearer ${token}` },
      data: {
        name: `e2e-bearer-${Date.now()}`,
        notes: "e2e bearer probe",
        isArchived: false,
        isRegistered: false,
      },
    });
    expect(created.status()).toBe(201);
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
