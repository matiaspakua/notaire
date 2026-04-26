/**
 * Playwright E2E tests — Presupuestos module.
 * CU01, CU39, CU45, CU49, CU55, CU60
 *
 * Requires backend running on http://localhost:8080.
 * CI runs with continue-on-error; local run needs full stack up.
 */
import { test, expect } from "@playwright/test";

test.describe("Presupuestos module (CU01, CU39)", () => {
  test.beforeEach(async ({ page }) => {
    await page.context().addCookies([
      {
        name: "notaire-auth-status",
        value: "authenticated",
        domain: "localhost",
        path: "/",
      },
    ]);

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

    await page.goto("/dashboard/presupuestos");
  });

  test("CU01 — página de presupuestos carga", async ({ page }) => {
    await expect(page.getByRole("heading", { name: /presupuestos/i })).toBeVisible();
  });

  test("CU01 — botón nuevo presupuesto está visible", async ({ page }) => {
    await expect(
      page.getByRole("button", { name: /nuevo presupuesto/i })
    ).toBeVisible();
  });

  test("CU01 — modal de nuevo presupuesto se abre", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo presupuesto/i }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
  });

  test("CU01 — modal contiene campos requeridos", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo presupuesto/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByLabel(/fecha/i)).toBeVisible();
    await expect(dialog.getByLabel(/monto/i)).toBeVisible();
  });

  test("CU01 — tabla de presupuestos está presente", async ({ page }) => {
    const tableOrEmpty = page
      .getByRole("table")
      .or(page.getByText(/no hay presupuestos/i));
    await expect(tableOrEmpty).toBeVisible({ timeout: 10000 });
  });
});

test.describe("Pagos module (CU15, CU47)", () => {
  test.beforeEach(async ({ page }) => {
    await page.context().addCookies([
      {
        name: "notaire-auth-status",
        value: "authenticated",
        domain: "localhost",
        path: "/",
      },
    ]);

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

    await page.goto("/dashboard/pagos");
  });

  test("CU15 — página de pagos carga correctamente (#321 fix)", async ({
    page,
  }) => {
    // Issue #321: pagos module was showing 'under construction' — now has full CRUD
    await expect(page.getByRole("heading", { name: /pagos/i })).toBeVisible();
    // Must NOT show placeholder text
    await expect(page.getByText(/under construction|en construcción/i)).not.toBeVisible();
  });

  test("CU15 — botón registrar pago está visible", async ({ page }) => {
    await expect(
      page.getByRole("button", { name: /registrar pago/i })
    ).toBeVisible();
  });

  test("CU15 — modal de nuevo pago tiene campos de fecha, monto y método", async ({
    page,
  }) => {
    await page.getByRole("button", { name: /registrar pago/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByLabel(/fecha/i)).toBeVisible();
    await expect(dialog.getByLabel(/monto/i)).toBeVisible();
    await expect(dialog.getByLabel(/método/i)).toBeVisible();
  });

  test("CU47 — tabla de pagos o mensaje de vacío está presente", async ({
    page,
  }) => {
    const tableOrEmpty = page
      .getByRole("table")
      .or(page.getByText(/no hay pagos/i));
    await expect(tableOrEmpty).toBeVisible({ timeout: 10000 });
  });
});
