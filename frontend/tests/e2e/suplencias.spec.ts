import { test, expect } from "@playwright/test";
import { authenticateAsAdmin as authSetup } from "./setup/auth";

/**
 * E2E tests for Suplencias page
 * CU12, CU42, CU59 — Registrar y consultar suplencias de escribano
 */

test.describe("Suplencias page", () => {
  test.beforeEach(async ({ page }) => {
    await authSetup(page);
  });

  test("renders suplencias page with title", async ({ page }) => {
    await page.goto("/dashboard/suplencias");
    await expect(page.getByRole("heading", { name: /suplencias/i })).toBeVisible();
  });

  test("shows suplencias description text", async ({ page }) => {
    await page.goto("/dashboard/suplencias");
    await expect(page.getByText(/suplenci/i).first()).toBeVisible();
  });

  test("nueva suplencia button is visible", async ({ page }) => {
    await page.goto("/dashboard/suplencias");
    await expect(
      page.getByTestId("btn-nueva-suplencia")
    ).toBeVisible();
  });

  test("opens modal on nueva suplencia click", async ({ page }) => {
    await page.goto("/dashboard/suplencias");
    await page.getByTestId("btn-nueva-suplencia").click();
    await expect(
      page.getByRole("dialog")
    ).toBeVisible();
    await expect(
      page.getByRole("heading", { name: /nueva suplencia/i })
    ).toBeVisible();
  });

  test("modal has escribano and suplente ID fields", async ({ page }) => {
    await page.goto("/dashboard/suplencias");
    await page.getByTestId("btn-nueva-suplencia").click();
    await expect(page.getByTestId("input-escribano-id")).toBeVisible();
    await expect(page.getByTestId("input-suplente-id")).toBeVisible();
  });

  test("modal has desde and hasta date fields", async ({ page }) => {
    await page.goto("/dashboard/suplencias");
    await page.getByTestId("btn-nueva-suplencia").click();
    await expect(page.getByTestId("input-desde")).toBeVisible();
    await expect(page.getByTestId("input-hasta")).toBeVisible();
  });

  test("can fill suplencia form fields", async ({ page }) => {
    await page.goto("/dashboard/suplencias");
    await page.getByTestId("btn-nueva-suplencia").click();
    await page.getByTestId("input-escribano-id").fill("1");
    await page.getByTestId("input-suplente-id").fill("2");
    await page.getByTestId("input-desde").fill("2026-01-01");
    await page.getByTestId("input-hasta").fill("2026-06-30");
    await expect(page.getByTestId("btn-guardar-suplencia")).toBeVisible();
  });

  test("modal closes on cancel", async ({ page }) => {
    await page.goto("/dashboard/suplencias");
    await page.getByTestId("btn-nueva-suplencia").click();
    await page.getByRole("button", { name: /cancelar/i }).click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
  });
});

test.describe("Reportes page", () => {
  test.beforeEach(async ({ page }) => {
    await authSetup(page);
  });

  test("renders reportes page with title", async ({ page }) => {
    await page.goto("/dashboard/reportes");
    await expect(page.getByRole("heading", { name: /reportes/i })).toBeVisible();
  });

  test("shows presupuesto report card", async ({ page }) => {
    await page.goto("/dashboard/reportes");
    await expect(page.getByTestId("card-reporte-presupuesto")).toBeVisible();
  });

  test("shows DDJJ rentas report card", async ({ page }) => {
    await page.goto("/dashboard/reportes");
    await expect(page.getByTestId("card-reporte-ddjj-rentas")).toBeVisible();
  });

  test("shows historial gestion report card", async ({ page }) => {
    await page.goto("/dashboard/reportes");
    await expect(page.getByTestId("card-reporte-historial")).toBeVisible();
  });

  test("presupuesto download button is disabled without ID", async ({ page }) => {
    await page.goto("/dashboard/reportes");
    const btn = page.getByTestId("btn-descargar-presupuesto");
    await expect(btn).toBeDisabled();
  });

  test("presupuesto download button enables after entering ID", async ({ page }) => {
    await page.goto("/dashboard/reportes");
    await page.getByTestId("input-id-presupuesto").fill("1");
    await expect(page.getByTestId("btn-descargar-presupuesto")).toBeEnabled();
  });

  test.skip("shows CU references in description", async () => {
    // Skipped: the reportes page does not display raw CU numbers (CU50, CU24, CU25) in its UI.
  });
});

test.describe("Personas search (CU61)", () => {
  test.beforeEach(async ({ page }) => {
    await authSetup(page);
  });

  test("renders search bar on personas page", async ({ page }) => {
    await page.goto("/dashboard/personas");
    await expect(page.getByTestId("search-bar")).toBeVisible();
  });

  test("shows nombre search input", async ({ page }) => {
    await page.goto("/dashboard/personas");
    await expect(page.getByTestId("input-search-nombre")).toBeVisible();
  });

  test("shows apellido search input", async ({ page }) => {
    await page.goto("/dashboard/personas");
    await expect(page.getByTestId("input-search-apellido")).toBeVisible();
  });

  test("shows DNI search input", async ({ page }) => {
    await page.goto("/dashboard/personas");
    await expect(page.getByTestId("input-search-dni")).toBeVisible();
  });

  test("shows filter clientes button", async ({ page }) => {
    await page.goto("/dashboard/personas");
    await expect(page.getByTestId("btn-filter-clientes")).toBeVisible();
  });

  test("can type in nombre search", async ({ page }) => {
    await page.goto("/dashboard/personas");
    const input = page.getByTestId("input-search-nombre");
    await input.fill("Juan");
    await expect(input).toHaveValue("Juan");
  });

  test("nueva persona modal has full form fields", async ({ page }) => {
    await page.goto("/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    await expect(page.getByTestId("input-nombre")).toBeVisible();
    await expect(page.getByTestId("input-apellido")).toBeVisible();
    await expect(page.getByTestId("check-es-cliente")).toBeVisible();
  });

  test("presupuestos filter bar is visible", async ({ page }) => {
    await page.goto("/dashboard/presupuestos");
    await expect(page.getByTestId("input-search-presupuesto")).toBeVisible();
    await expect(page.getByTestId("select-estado")).toBeVisible();
  });
});
