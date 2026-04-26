/**
 * E2E tests — Dashboard navigation and module access
 * CU: Principal navigation, role-based access
 */
import { type Page, test, expect } from "@playwright/test";

// Helper: authenticate before navigating to protected pages
async function loginAs(page: Page, role: "admin" | "empleado" = "admin") {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill(role === "admin" ? "admin" : "empleado");
  await page.getByTestId("input-contrasenia").fill(role === "admin" ? "admin" : "admin");
  await page.getByTestId("btn-ingresar").click();
  await page.waitForURL(/\/dashboard/);
}

test.describe("Dashboard navigation", () => {
  test("authenticated admin sees all modules", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await page.waitForURL(/\/dashboard/, { timeout: 10000 });

    await expect(page.getByText("Gestiones")).toBeVisible();
    await expect(page.getByText("Presupuestos")).toBeVisible();
    await expect(page.getByText("Personas")).toBeVisible();
    await expect(page.getByText("Escrituras")).toBeVisible();
    await expect(page.getByText("Pagos")).toBeVisible();
  });

  test("unauthenticated user is redirected to login", async ({ page }) => {
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });
  });
});

test.describe("Gestiones module (CU02, CU13-CU16)", () => {
  test("shows gestiones page with table", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await page.waitForURL(/\/dashboard/);

    await page.goto("/dashboard/gestiones");
    await expect(page.getByText("Gestiones")).toBeVisible();
    await expect(page.getByTestId("btn-nueva-gestion")).toBeVisible();
  });
});

test.describe("Personas module (CU17, CU18)", () => {
  test("shows personas page", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await page.waitForURL(/\/dashboard/);

    await page.goto("/dashboard/personas");
    await expect(page.getByText("Personas y Clientes")).toBeVisible();
    await expect(page.getByTestId("btn-nueva-persona")).toBeVisible();
  });
});

test.describe("Presupuestos module (CU01)", () => {
  test("shows presupuestos page", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await page.waitForURL(/\/dashboard/);

    await page.goto("/dashboard/presupuestos");
    await expect(page.getByText("Presupuestos")).toBeVisible();
    await expect(page.getByTestId("btn-nuevo-presupuesto")).toBeVisible();
  });
});
