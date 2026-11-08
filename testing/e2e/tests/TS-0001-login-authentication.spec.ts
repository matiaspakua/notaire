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
    await expect(page.getByTestId("input-username")).toBeVisible();
    await expect(page.getByTestId("input-password")).toBeVisible();
    await expect(page.getByTestId("btn-sign-in")).toBeVisible();
  });

  test("does not disclose backend URL on the public login page (issue #1055)", async ({
    page,
  }) => {
    const body = await page.locator("body").innerText();
    expect(body).not.toMatch(/Backend\s*:/i);
    expect(body).not.toContain("backend:8080");
    expect(body).not.toContain("NEXT_PUBLIC_API_URL");
    const html = await page.content();
    expect(html).not.toContain("http://backend:8080");
  });

  test("shows error for empty submission", async ({ page }) => {
    await page.getByTestId("btn-sign-in").click();
    await expect(page.getByText("Complete usuario y contraseña")).toBeVisible();
  });

  test("shows error for wrong credentials", async ({ page }) => {
    await page.getByTestId("input-username").fill("invalid_user");
    await page.getByTestId("input-password").fill("invalid_password");
    await page.getByTestId("btn-sign-in").click();
    await expect(
      page.getByText(/incorrectos|servidor/i)
    ).toBeVisible({ timeout: 5000 });
  });

  test("redirects to dashboard on successful login", async ({ page }) => {
    await page.getByTestId("input-username").fill("admin");
    await page.getByTestId("input-password").fill("admin");
    await page.getByTestId("btn-sign-in").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
  });

  test("shows one desk card and a single product heading (#2069)", async ({ page }) => {
    const card = page.getByTestId("login-card");
    await expect(card).toBeVisible();
    await expect(page.getByRole("heading", { level: 1, name: "Notaire" })).toBeVisible();
    await expect(card.getByRole("heading", { level: 2, name: "Iniciar sesión" })).toBeVisible();
    await expect(card.getByTestId("btn-sign-in")).toBeVisible();
    await expect(page.getByTestId("login-mark")).toHaveCSS("transform", "none");
  });

  for (const width of [320, 768, 1024]) {
    test(`desk card fits a ${width}px viewport (#2069)`, async ({ page }) => {
      await page.setViewportSize({ width, height: 800 });
      await page.goto("/login");
      await expect(page.getByTestId("login-card")).toBeVisible();
      const overflows = await page.evaluate(
        () => document.documentElement.scrollWidth > document.documentElement.clientWidth + 1,
      );
      expect(overflows).toBe(false);
    });
  }
});

test.describe("TS-0001 login reduced motion (#2069)", () => {
  test.use({ reducedMotion: "reduce" });

  test("seal and card render without a transform", async ({ page }) => {
    await page.goto("/login");
    await expect(page.getByTestId("login-mark")).toBeVisible();
    await expect(page.getByTestId("login-mark")).toHaveCSS("transform", "none");
    await expect(page.getByTestId("login-card")).toHaveCSS("transform", "none");
  });
});
