/**
 * E2E tests — Dashboard navigation and module access
 * CU: Principal navigation, role-based access
 */
import { type Page, test, expect } from "@playwright/test";

async function loginAs(page: Page, role: "admin" | "empleado" = "admin") {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill(role === "admin" ? "admin" : "empleado");
  await page.getByTestId("input-contrasenia").fill(role === "admin" ? "admin" : "admin");
  await page.getByTestId("btn-ingresar").click();
  await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  // Wait for the dashboard layout to finish hydrating
  await page.waitForLoadState("domcontentloaded");
}

test.describe("Dashboard navigation", () => {
  test("authenticated admin sees all modules", async ({ page }) => {
    await loginAs(page);

    // Sidebar link accessible names include icon alt text + label text
    // (e.g. "Gestiones Gestiones"), so use exact: false
    for (const name of ["Gestiones", "Presupuestos", "Personas", "Escrituras", "Pagos"]) {
      await expect(page.getByRole("link", { name }).first()).toBeVisible({ timeout: 5000 });
    }
  });

  test("unauthenticated user is redirected to login", async ({ page }) => {
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });
  });

  test("sidebar PNG icons render with correct alt text", async ({ page }) => {
    await loginAs(page);

    const iconAlts = [
      "Gestiones",
      "Presupuestos",
      "Personas",
      "Escrituras",
      "Pagos",
      "Protocolo",
      "Documentos",
      "Administración",
      "Cerrar sesión",
    ];

    for (const alt of iconAlts) {
      const img = page.locator(`aside img[alt="${alt}"]`);
      await expect(img).toBeVisible({ timeout: 5000 });
    }
  });

  test("dashboard stat icons load correctly", async ({ page }) => {
    await loginAs(page);

    // Use .first() because stat cards and dashboard module grid both render Gestiones/Personas/Presupuestos icons
    await expect(page.locator('main img[alt="Gestiones"]').first()).toBeVisible({ timeout: 5000 });
    await expect(page.locator('main img[alt="Personas"]').first()).toBeVisible({ timeout: 5000 });
    await expect(page.locator('main img[alt="Presupuestos"]').first()).toBeVisible({ timeout: 5000 });
  });

  test("all module card icons in dashboard grid are visible", async ({ page }) => {
    await loginAs(page);

    const moduleLabels = [
      "Gestiones",
      "Presupuestos",
      "Personas",
      "Escrituras",
      "Pagos",
      "Protocolo",
      "Inmuebles",
      "Copias",
      "Items",
      "Documentos",
      "Auditoría",
      "Administración",
    ];

    for (const label of moduleLabels) {
      // Module cards use either lucide-react icons or NotaireIcon PNGs
      const card = page.locator(`main a:has(h3:text-is("${label}"))`);
      await expect(card).toBeVisible({ timeout: 5000 });
    }
  });
});

test.describe("Gestiones module (CU02, CU13-CU16)", () => {
  test("shows gestiones page with table", async ({ page }) => {
    await loginAs(page);
    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("domcontentloaded");

    await expect(page.getByRole("heading", { name: "Gestiones" })).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId("btn-nueva-gestion")).toBeVisible({ timeout: 10000 });
  });
});

test.describe("Personas module (CU17, CU18)", () => {
  test("shows personas page", async ({ page }) => {
    await loginAs(page);
    await page.goto("/dashboard/personas");
    await page.waitForLoadState("domcontentloaded");

    await expect(page.getByRole("heading", { name: "Personas y Clientes" })).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId("btn-nueva-persona")).toBeVisible({ timeout: 10000 });
  });
});

test.describe("Presupuestos module (CU01)", () => {
  test("shows presupuestos page", async ({ page }) => {
    await loginAs(page);
    await page.goto("/dashboard/presupuestos");
    await page.waitForLoadState("domcontentloaded");

    await expect(page.getByRole("heading", { name: "Presupuestos" })).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId("btn-nuevo-presupuesto")).toBeVisible({ timeout: 10000 });
  });
});
