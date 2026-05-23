/**
 * E2E tests — Gestiones CRUD
 * CU02 Iniciar Gestión, CU13 Ver historial, CU14 Consultar estado,
 * CU16 Archivar Gestión, CU53 Modificar Gestión
 */
import { test, expect } from "@playwright/test";

test.describe("Gestiones CRUD", () => {
  test.beforeEach(async ({ page }) => {
    await page.context().addCookies([
      { name: "notaire-auth-status", value: "authenticated", domain: "localhost", path: "/" },
    ]);
    await page.addInitScript(() => {
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: { user: { nombre: "admin", tipo: "ADMIN", valido: true }, isAuthenticated: true },
          version: 0,
        })
      );
    });
    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("domcontentloaded");
  });

  test("can open create modal and cancel", async ({ page }) => {
    await page.getByTestId("btn-nueva-gestion").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible({ timeout: 10000 });
    await expect(dialog.getByTestId("input-numero-gestion")).toBeVisible();
    await dialog.getByRole("button", { name: /cancelar|cancel/i }).click();
    await expect(dialog).not.toBeVisible();
  });

  test("create form has number input", async ({ page }) => {
    await page.getByTestId("btn-nueva-gestion").click();
    await expect(page.getByTestId("input-numero-gestion")).toBeVisible();
  });
});
