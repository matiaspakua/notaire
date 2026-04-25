/**
 * E2E tests — Gestiones CRUD
 * CU02 Iniciar Gestión, CU13 Ver historial, CU14 Consultar estado,
 * CU16 Archivar Gestión, CU53 Modificar Gestión
 */
import { test, expect } from "@playwright/test";

test.describe("Gestiones CRUD", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await page.waitForURL(/\/dashboard/);
    await page.goto("/dashboard/gestiones");
  });

  test("can open create modal and cancel", async ({ page }) => {
    await page.getByTestId("btn-nueva-gestion").click();
    await expect(page.getByText("Nueva gestión")).toBeVisible();
    await page.getByRole("button", { name: "Cancelar" }).click();
    await expect(page.getByText("Nueva gestión")).not.toBeVisible();
  });

  test("create form has number input", async ({ page }) => {
    await page.getByTestId("btn-nueva-gestion").click();
    await expect(page.getByTestId("input-numero-gestion")).toBeVisible();
  });
});
