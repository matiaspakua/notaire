/**
 * Playwright E2E tests — search inputs must be reachable by accessible label.
 * Issue #608: 9 pages rendered search <Input>s with placeholder as the only
 * label, failing WCAG label-association for screen-reader users.
 */
import { test, expect } from "@playwright/test";
import { authenticateAsAdmin as adminAuthSetup } from "./setup/auth";

test.describe("Search inputs expose an accessible label (#608)", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
  });

  test("personas page search inputs are labeled", async ({ page }) => {
    await page.goto("/dashboard/personas");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
    await expect(page.getByLabel("Buscar por apellido...", { exact: true })).toBeVisible();
    await expect(page.getByLabel("Buscar por DNI...", { exact: true })).toBeVisible();
  });

  test("escrituras page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/escrituras");
    await expect(page.getByLabel("Buscar por número...", { exact: true })).toBeVisible();
  });

  test("presupuestos page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/presupuestos");
    await expect(page.getByLabel("Buscar por ID o cliente...", { exact: true })).toBeVisible();
  });

  test("auditoria page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/auditoria");
    await expect(page.getByLabel("Buscar por usuario u operación...", { exact: true })).toBeVisible();
  });

  test("administracion/workflows page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/administracion/workflows");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
  });

  test("administracion/tramites page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/administracion/tramites");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
  });

  test("administracion/conceptos page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/administracion/conceptos");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
  });

  test("administracion/documentos page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/administracion/documentos");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
  });

  test("administracion/estados-gestion page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/administracion/estados-gestion");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
  });
});
