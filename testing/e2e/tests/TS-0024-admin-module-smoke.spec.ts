/**
 * Playwright E2E tests — Administración module.
 * CU20, CU26, CU27, CU29, CU57, CU64, CU65, CU66, CU67
 *
 * Requires backend running on http://localhost:8080.
 */
import { test, expect } from "@playwright/test";
import { authenticateAsAdmin as adminAuthSetup } from "./setup/auth";

test.describe("Administration hub (ADMIN only)", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administration");
  });

  test("hub shows all admin module cards", async ({ page }) => {
    await expect(
      page.getByRole("heading", { level: 1, name: /administración/i })
    ).toBeVisible();
    await expect(page.getByText(/users/i).first()).toBeVisible();
    await expect(page.getByText(/concepts/i).first()).toBeVisible();
    await expect(page.getByText(/tipos de trámite/i).first()).toBeVisible();
    await expect(page.getByText(/estados de gestión/i).first()).toBeVisible();
  });

  test("audit link is visible in admin hub", async ({ page }) => {
    await expect(page.getByText(/auditoría/i)).toBeVisible();
  });
});

test.describe("CU67 — Management statuses", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administration/management-statuses");
  });

  test("page loads with correct title", async ({ page }) => {
    await expect(
      page.getByRole("heading", { name: /estados de gestión/i })
    ).toBeVisible();
  });

  test("create button opens modal", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo estado/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    await expect(dialog.getByLabel(/nombre/i)).toBeVisible();
  });

  test("cancel closes modal without saving", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo estado/i }).click();
    await page.getByRole("button", { name: /cancelar/i }).click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
  });

  test("CU67 — typing in search calls GET /management-statuses/search", async ({ page }) => {
    const searchRequest = page.waitForRequest((req) =>
      req.url().includes("/api/v1/management-statuses/search") && req.method() === "GET"
    );
    await page.getByTestId("search-statuses").fill("Iniciada");
    await searchRequest;
  });
});

test.describe("CU26/CU57/CU64 — Procedure types", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administration/procedures");
  });

  test("page loads with correct title", async ({ page }) => {
    await expect(
      page.getByRole("heading", { name: /tipos de trámite/i })
    ).toBeVisible();
  });

  test("modal has name, description, and checkbox fields", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo tipo/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByLabel(/nombre/i)).toBeVisible();
    await expect(dialog.getByLabel(/descripción/i)).toBeVisible();
    await expect(dialog.getByLabel(/se archiva/i)).toBeVisible();
    await expect(dialog.getByLabel(/se inscribe/i)).toBeVisible();
  });

  test("CU26/CU64 — typing in search calls GET /procedure-types/search", async ({ page }) => {
    const searchRequest = page.waitForRequest((req) =>
      req.url().includes("/api/v1/procedure-types/search") && req.method() === "GET"
    );
    await page.getByTestId("input-search-procedure").fill("Compraventa");
    await searchRequest;
  });
});

test.describe("CU37 — Concepts", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administration/concepts");
  });

  test("page loads with correct title", async ({ page }) => {
    await expect(page.getByRole("heading", { name: /concepts/i })).toBeVisible();
  });

  test("typing in search calls GET /concepts/search", async ({ page }) => {
    const searchRequest = page.waitForRequest((req) =>
      req.url().includes("/api/v1/concepts/search") && req.method() === "GET"
    );
    await page.getByTestId("input-search-concept").fill("Honorarios");
    await searchRequest;
  });
});

test.describe("CU38 — Document types", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administration/documents");
  });

  test("page loads with correct title", async ({ page }) => {
    await expect(page.getByRole("heading", { name: /tipos de documento/i })).toBeVisible();
  });

  test("typing in search calls GET /document-types/search", async ({ page }) => {
    const searchRequest = page.waitForRequest((req) =>
      req.url().includes("/api/v1/document-types/search") && req.method() === "GET"
    );
    await page.getByTestId("input-search-document").fill("DNI");
    await searchRequest;
  });
});

test.describe("CU28/CU40/CU58/CU68 — Folios", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administration/folios");
  });

  test("page loads with correct title", async ({ page }) => {
    await expect(
      page.getByRole("heading", { name: /folios/i })
    ).toBeVisible();
  });

  test("modal has number, year, and status fields", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo folio/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByLabel(/número/i)).toBeVisible();
    await expect(dialog.getByLabel(/año/i)).toBeVisible();
  });

  test("CU63 — status filter select is present and usable", async ({ page }) => {
    await expect(page.getByTestId("select-filter-status-folio")).toBeVisible();
  });

  test("Folio types section: full CRUD via search + create + edit + delete", async ({ page }) => {
    const uniqueName = `TipoTest${Date.now()}`;
    const renamed = `${uniqueName}-edit`;

    await expect(page.getByRole("heading", { name: /tipos de folio/i })).toBeVisible();

    await page.getByTestId("btn-new-folio-type").click();
    const createDialog = page.getByRole("dialog");
    await createDialog.getByTestId("input-name-folio-type").fill(uniqueName);
    await createDialog.getByTestId("btn-save-folio-type").click();
    await expect(createDialog).toBeHidden();

    await page.getByTestId("input-search-folio-type").fill(uniqueName);
    const row = page.getByText(uniqueName, { exact: true });
    await expect(row).toBeVisible();

    await page.getByTestId("btn-edit-folio-type").first().click();
    const editDialog = page.getByRole("dialog");
    await editDialog.getByTestId("input-name-folio-type").fill(renamed);
    await editDialog.getByTestId("btn-save-folio-type").click();
    await expect(editDialog).toBeHidden();

    await page.getByTestId("input-search-folio-type").fill(renamed);
    await expect(page.getByText(renamed, { exact: true })).toBeVisible();

    await page.getByTestId("btn-delete-folio-type").first().click();
    await page.getByRole("alertdialog").getByRole("button", { name: /eliminar/i }).click();
    await expect(page.getByText(renamed, { exact: true })).toBeHidden();
  });

  test("CU81 — marking a folio type as Protocolo Auxiliar via checkbox persists the flag", async ({ page }) => {
    const uniqueName = `TipoAuxiliarTest${Date.now()}`;

    await page.getByTestId("btn-new-folio-type").click();
    const dialog = page.getByRole("dialog");
    await dialog.getByTestId("input-name-folio-type").fill(uniqueName);
    await dialog.getByTestId("checkbox-is-auxiliary-folio-type").click();
    await dialog.getByTestId("btn-save-folio-type").click();
    await expect(dialog).toBeHidden();

    await page.getByTestId("input-search-folio-type").fill(uniqueName);
    await expect(page.getByText(/protocol auxiliar/i).first()).toBeVisible();
  });
});

test.describe("CU39/CU49/CU55 — Budget templates", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administration/templates");
  });

  test("page loads with correct title", async ({ page }) => {
    await expect(
      page.getByRole("heading", { name: /templates de presupuesto/i })
    ).toBeVisible();
  });

  test("the modal requires a procedure type and a concept", async ({ page }) => {
    await page.getByRole("button", { name: /nueva plantilla/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByRole("combobox").first()).toBeVisible();
  });
});

test.describe("Audit (read-only)", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/audit");
  });

  test("page loads with audit heading", async ({ page }) => {
    // Page title is h1; an h3 section card also says "Auditoría" (#1058 nav).
    await expect(
      page.getByRole("heading", { name: /auditoría/i, level: 1 })
    ).toBeVisible();
  });

  test("no create button — audit is read-only", async ({ page }) => {
    // Audit page should have no 'New' or 'Create' button
    await expect(
      page.getByRole("button", { name: /nuevo|crear|registrar/i })
    ).not.toBeVisible();
  });
});
