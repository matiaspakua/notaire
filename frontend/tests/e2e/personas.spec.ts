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
    await page.goto("/dashboard/personas");
  });

  test("CU17 — página de personas carga correctamente", async ({ page }) => {
    await expect(page).toHaveTitle(/Notaire/i);
    await expect(page.getByRole("heading", { name: /personas/i })).toBeVisible();
  });

  test("CU18 — botón nueva persona es visible", async ({ page }) => {
    await expect(page.getByTestId("btn-nueva-persona")).toBeVisible({ timeout: 10000 });
  });

  test("CU18 — modal de nueva persona se abre al hacer click", async ({ page }) => {
    await page.getByTestId("btn-nueva-persona").click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await expect(
      page.getByRole("heading", { name: /nueva persona|person/i })
    ).toBeVisible();
  });

  test("CU18 — modal contiene campos de nombre, apellido y DNI", async ({ page }) => {
    await page.getByTestId("btn-nueva-persona").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByTestId("input-nombre")).toBeVisible();
    await expect(dialog.getByTestId("input-apellido")).toBeVisible();
    await expect(dialog.getByLabel(/dni/i)).toBeVisible();
  });

  test("CU18 — modal se cierra al presionar cancelar", async ({ page }) => {
    await page.getByTestId("btn-nueva-persona").click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByRole("button", { name: /cancelar|cancel/i }).click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
  });

  test("CU17 — tabla de personas está presente (puede estar vacía con mock)", async ({ page }) => {
    const tableOrEmpty = page
      .getByRole("table")
      .or(page.getByText(/no hay personas|no data/i));
    await expect(tableOrEmpty).toBeVisible({ timeout: 10000 });
  });

  test("CU61 — escribir en búsqueda por nombre llama a GET /personas/buscar", async ({ page }) => {
    const searchRequest = page.waitForRequest((req) =>
      req.url().includes("/api/v1/personas/buscar") && req.method() === "GET"
    );
    await page.getByTestId("input-search-nombre").fill("Juan");
    await searchRequest;
  });
});
