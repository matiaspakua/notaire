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

  test("people page search inputs are labeled", async ({ page }) => {
    await page.goto("/dashboard/people");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
    await expect(page.getByLabel("Buscar por apellido...", { exact: true })).toBeVisible();
    await expect(page.getByLabel("Buscar por DNI...", { exact: true })).toBeVisible();
  });

  test("deeds page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/deeds");
    await expect(page.getByLabel("Buscar por número...", { exact: true })).toBeVisible();
  });

  test("the budgets page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/budgets");
    await expect(page.getByLabel("Buscar por ID o cliente...", { exact: true })).toBeVisible();
  });

  test("auditoria page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/audit");
    await expect(page.getByLabel("Buscar en esta página por usuario u operación...", { exact: true })).toBeVisible();
  });

  test("administracion/workflows page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/administration/workflows");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
  });

  test("administracion/procedures page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/administration/procedures");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
  });

  test("administracion/concepts page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/administration/concepts");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
  });

  test("administracion/documents page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/administration/documents");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
  });

  test("administracion/management-statuses page search input is labeled", async ({ page }) => {
    await page.goto("/dashboard/administration/management-statuses");
    await expect(page.getByLabel("Buscar por nombre...", { exact: true })).toBeVisible();
  });
});

/**
 * Issue #1343: filter selects had no accessible name (axe button-name /
 * select-name, critical) on managements, budgets, folios, auditoria and
 * estados-management.
 */
test.describe("Filter selects expose an accessible name (#1343)", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
  });

  test("the managements client filter is a named combobox", async ({ page }) => {
    await page.goto("/dashboard/managements");
    await expect(page.getByRole("combobox", { name: "Filtrar por cliente", exact: true })).toBeVisible();
  });

  test("the budgets status filter is a named combobox", async ({ page }) => {
    await page.goto("/dashboard/budgets");
    await expect(page.getByRole("combobox", { name: "Filtrar por estado", exact: true })).toBeVisible();
  });

  test("folios status filter is a named combobox", async ({ page }) => {
    await page.goto("/dashboard/administration/folios");
    await expect(page.getByRole("combobox", { name: "Filtrar por estado", exact: true })).toBeVisible();
  });

  test("auditoria module filter, when shown, is a named combobox", async ({ page }) => {
    await page.goto("/dashboard/audit");
    await expect(page.getByRole("heading", { level: 1 })).toBeVisible();
    const selects = page.locator("select");
    if ((await selects.count()) > 0) {
      await expect(page.getByRole("combobox", { name: "Módulo", exact: true })).toBeVisible();
    }
  });

  test("management-statuses workflow selector, when shown, is a named combobox", async ({ page }) => {
    await page.goto("/dashboard/administration/management-statuses");
    await expect(page.getByRole("heading", { level: 1 })).toBeVisible();
    if ((await page.getByTestId("select-workflow").count()) > 0) {
      await expect(page.getByRole("combobox", { name: "Workflow a visualizar", exact: true })).toBeVisible();
    }
  });
});
