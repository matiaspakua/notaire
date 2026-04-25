/**
 * Playwright E2E tests — Personas module.
 * CU17, CU18, CU21, CU41, CU46, CU48, CU51, CU54, CU61
 *
 * Requires backend running on http://localhost:8080.
 * CI runs with continue-on-error; local run needs full stack up.
 */
import { test, expect } from "@playwright/test";

test.describe("Personas module (CU17, CU18)", () => {
  test.beforeEach(async ({ page }) => {
    // Simulate auth cookie so middleware doesn't redirect
    await page.context().addCookies([
      {
        name: "notaire-auth-status",
        value: "authenticated",
        domain: "localhost",
        path: "/",
      },
    ]);

    // Seed localStorage with auth user (ADMIN role)
    await page.addInitScript(() => {
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: {
            user: { nombre: "admin", tipo: "ADMIN", valido: true },
            isAuthenticated: true,
          },
          version: 0,
        })
      );
    });

    await page.goto("/dashboard/personas");
  });

  test("CU17 — página de personas carga correctamente", async ({ page }) => {
    await expect(page).toHaveTitle(/Notaire/i);
    await expect(page.getByRole("heading", { name: /personas/i })).toBeVisible();
  });

  test("CU18 — botón registrar persona es visible", async ({ page }) => {
    const btn = page.getByRole("button", { name: /registrar persona/i });
    await expect(btn).toBeVisible();
  });

  test("CU18 — modal de nueva persona se abre al hacer click", async ({ page }) => {
    const btn = page.getByRole("button", { name: /registrar persona/i });
    await btn.click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await expect(
      page.getByRole("heading", { name: /nueva persona|registrar persona/i })
    ).toBeVisible();
  });

  test("CU18 — modal contiene campos de nombre, apellido y DNI", async ({
    page,
  }) => {
    await page.getByRole("button", { name: /registrar persona/i }).click();
    const dialog = page.getByRole("dialog");

    await expect(dialog.getByLabel(/nombre/i)).toBeVisible();
    await expect(dialog.getByLabel(/apellido/i)).toBeVisible();
    await expect(dialog.getByLabel(/dni/i)).toBeVisible();
  });

  test("CU18 — modal se cierra al presionar cancelar", async ({ page }) => {
    await page.getByRole("button", { name: /registrar persona/i }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByRole("button", { name: /cancelar/i }).click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
  });

  test("CU17 — tabla de personas está presente (puede estar vacía con mock)", async ({
    page,
  }) => {
    // Table or empty state message should be visible
    const tableOrEmpty = page
      .getByRole("table")
      .or(page.getByText(/no hay personas/i));
    await expect(tableOrEmpty).toBeVisible({ timeout: 10000 });
  });
});
