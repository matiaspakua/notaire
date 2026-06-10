import { test, expect } from "@playwright/test";

test.describe("CU431 — Gestión de Roles y Permisos", () => {
  test("roles page is accessible from administracion", async ({ page }) => {
    await page.goto("/dashboard/administracion/roles");
    await expect(page.getByText("Roles y Permisos")).toBeVisible({ timeout: 10000 });
  });

  test("new role button opens modal", async ({ page }) => {
    await page.goto("/dashboard/administracion/roles");
    await page.getByTestId("btn-nuevo-rol").click();
    await expect(page.getByTestId("input-nombre-rol")).toBeVisible();
  });

  test("usuarios page shows rol column", async ({ page }) => {
    await page.goto("/dashboard/administracion/usuarios");
    await expect(page.getByText("Rol")).toBeVisible({ timeout: 10000 });
  });

  test("usuario edit modal shows rol selector", async ({ page }) => {
    await page.goto("/dashboard/administracion/usuarios");
    await page.getByTestId("btn-nuevo-usuario").click();
    await expect(page.getByTestId("select-rol-usuario")).toBeVisible();
  });
});
