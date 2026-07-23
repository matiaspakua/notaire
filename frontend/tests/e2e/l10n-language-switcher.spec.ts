/**
 * E2E tests — Localization (l10n) and Language Switcher
 * Phase 3: Verify ES/EN language switching via LanguageSwitcher component
 *
 * Requires: frontend at localhost:3000, backend at localhost:8080
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";

async function setupAuthAndGo(page: Page, path = "/dashboard") {
  await authenticateAsAdmin(page);
  await page.goto(path);
  await page.waitForLoadState("domcontentloaded");
}

async function clearLocale(page: Page) {
  await page.context().clearCookies();
}

test.describe("Language Switcher — l10n feature", () => {
  test.beforeEach(async ({ page }) => {
    await clearLocale(page);
  });

  test("language switcher is visible in the sidebar", async ({ page }) => {
    await setupAuthAndGo(page);
    const switcher = page.getByTestId("language-switcher");
    await expect(switcher).toBeVisible({ timeout: 10000 });
  });

  test("default locale is Spanish — sidebar shows ES label pressed", async ({ page }) => {
    await setupAuthAndGo(page);
    const switcher = page.getByTestId("language-switcher");
    await expect(switcher).toBeVisible({ timeout: 10000 });
    const esBtn = switcher.locator("button", { hasText: "ES" });
    await expect(esBtn).toHaveAttribute("aria-pressed", "true");
  });

  test("sidebar navigation labels are in Spanish by default", async ({ page }) => {
    await setupAuthAndGo(page);
    // Sidebar shows Spanish nav links
    await expect(page.getByRole("link", { name: /gestiones/i }).first()).toBeVisible({ timeout: 10000 });
    await expect(page.getByRole("link", { name: /personas/i }).first()).toBeVisible({ timeout: 10000 });
  });

  test("clicking EN button switches UI to English", async ({ page }) => {
    await setupAuthAndGo(page);
    const switcher = page.getByTestId("language-switcher");
    await expect(switcher).toBeVisible({ timeout: 10000 });

    const enBtn = switcher.locator("button", { hasText: "EN" });
    await enBtn.click();

    // Wait for page reload (language switch triggers window.location.reload)
    await page.waitForLoadState("domcontentloaded");
    await page.waitForTimeout(2000);

    // After switching, EN button should be active
    const switcherAfter = page.getByTestId("language-switcher");
    await expect(switcherAfter).toBeVisible({ timeout: 10000 });
    const enBtnAfter = switcherAfter.locator("button", { hasText: "EN" });
    await expect(enBtnAfter).toHaveAttribute("aria-pressed", "true");
  });

  test("English locale shows English navigation labels", async ({ page }) => {
    await authenticateAsAdmin(page);
    // Pre-set English locale cookie
    await page.context().addCookies([
      { name: "NEXT_LOCALE", value: "en", domain: "localhost", path: "/" },
    ]);
    await page.goto("/dashboard");
    await page.waitForLoadState("domcontentloaded");

    // Sidebar should show English labels
    await expect(page.getByRole("link", { name: /cases/i }).first()).toBeVisible({ timeout: 10000 });
  });

  test("switching to ES from EN shows Spanish navigation labels", async ({ page }) => {
    // Start in English
    await page.context().addCookies([
      { name: "NEXT_LOCALE", value: "en", domain: "localhost", path: "/" },
      { name: "notaire-auth-status", value: "authenticated", domain: "localhost", path: "/" },
    ]);
    await page.addInitScript(() => {
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: { user: { nombre: "Admin Test", tipo: "ADMIN", valido: true }, isAuthenticated: true },
          version: 0,
        })
      );
    });
    await page.goto("/dashboard");
    await page.waitForLoadState("domcontentloaded");

    // Switch to Spanish
    const switcher = page.getByTestId("language-switcher");
    await expect(switcher).toBeVisible({ timeout: 10000 });
    const esBtn = switcher.locator("button", { hasText: "ES" });
    await esBtn.click();

    await page.waitForLoadState("domcontentloaded");
    await page.waitForTimeout(2000);

    // Should show Spanish nav
    await expect(page.getByRole("link", { name: /gestiones/i }).first()).toBeVisible({ timeout: 10000 });
  });
});

test.describe("l10n — Dashboard page translations", () => {
  test("dashboard page shows Spanish text by default", async ({ page }) => {
    await setupAuthAndGo(page);
    await expect(page.getByText(/módulos disponibles/i)).toBeVisible({ timeout: 10000 });
  });

  test("dashboard shows English text when locale is EN", async ({ page }) => {
    await page.context().addCookies([
      { name: "NEXT_LOCALE", value: "en", domain: "localhost", path: "/" },
      { name: "notaire-auth-status", value: "authenticated", domain: "localhost", path: "/" },
    ]);
    await page.addInitScript(() => {
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: { user: { nombre: "Admin Test", tipo: "ADMIN", valido: true }, isAuthenticated: true },
          version: 0,
        })
      );
    });
    await page.goto("/dashboard");
    await page.waitForLoadState("domcontentloaded");
    await expect(page.getByText(/available modules/i)).toBeVisible({ timeout: 10000 });
  });
});

test.describe("l10n — Login page translations", () => {
  test("login page shows Spanish by default", async ({ page }) => {
    await page.goto("/login");
    await expect(page.getByText(/iniciar sesión/i)).toBeVisible({ timeout: 10000 });
  });

  test("login page shows English when locale is EN", async ({ page }) => {
    await page.context().addCookies([
      { name: "NEXT_LOCALE", value: "en", domain: "localhost", path: "/" },
    ]);
    await page.goto("/login");
    await expect(page.getByText(/sign in/i).first()).toBeVisible({ timeout: 10000 });
  });
});

test.describe("l10n — Admin pages translations", () => {
  test("usuarios page shows Spanish title by default", async ({ page }) => {
    await setupAuthAndGo(page, "/dashboard/administracion/usuarios");
    await expect(page.getByRole("heading", { name: /usuarios/i })).toBeVisible({ timeout: 10000 });
  });

  test("usuarios page shows English title when locale is EN", async ({ page }) => {
    await page.context().addCookies([
      { name: "NEXT_LOCALE", value: "en", domain: "localhost", path: "/" },
      { name: "notaire-auth-status", value: "authenticated", domain: "localhost", path: "/" },
    ]);
    await page.addInitScript(() => {
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: { user: { nombre: "Admin Test", tipo: "ADMIN", valido: true }, isAuthenticated: true },
          version: 0,
        })
      );
    });
    await page.goto("/dashboard/administracion/usuarios");
    await page.waitForLoadState("domcontentloaded");
    await expect(page.getByRole("heading", { name: /users/i })).toBeVisible({ timeout: 10000 });
  });
});
