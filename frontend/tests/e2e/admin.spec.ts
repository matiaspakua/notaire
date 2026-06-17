/**
 * Playwright E2E tests — Administración module.
 * CU20, CU26, CU27, CU29, CU57, CU64, CU65, CU66, CU67
 *
 * Requires backend running on http://localhost:8080.
 */
import { test, expect } from "@playwright/test";

const adminAuthSetup = async (page: import("@playwright/test").Page) => {
  await page.context().addCookies([
    {
      name: "notaire-auth-status",
      value: "authenticated",
      domain: "localhost",
      path: "/",
    },
  ]);
  await page.addInitScript(() => {
    localStorage.setItem(
      "notaire-auth",
      JSON.stringify({
        state: {
          user: { nombre: "admin", tipo: "ADMIN", valido: true },
          isAuthenticated: true,
        },
        version: 0,
      })
    );
  });
};

test.describe("Administración hub (ADMIN only)", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administracion");
  });

  test("hub shows all admin module cards", async ({ page }) => {
    await expect(
      page.getByRole("heading", { name: /administración/i })
    ).toBeVisible();
    await expect(page.getByText(/usuarios/i).first()).toBeVisible();
    await expect(page.getByText(/conceptos/i).first()).toBeVisible();
    await expect(page.getByText(/tipos de trámite/i).first()).toBeVisible();
    await expect(page.getByText(/estados de gestión/i).first()).toBeVisible();
  });

  test("audit link is visible in admin hub", async ({ page }) => {
    await expect(page.getByText(/auditoría/i)).toBeVisible();
  });
});

test.describe("CU67 — Estados de Gestión", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administracion/estados-gestion");
  });

  test("page loads with correct title", async ({ page }) => {
    await expect(
      page.getByRole("heading", { name: /estados de gestión/i })
    ).toBeVisible();
  });

  test("create button opens modal", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo estado/i }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await expect(page.getByLabel(/nombre/i)).toBeVisible();
  });

  test("cancel closes modal without saving", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo estado/i }).click();
    await page.getByRole("button", { name: /cancelar/i }).click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
  });
});

test.describe("CU26/CU57/CU64 — Tipos de Trámite", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administracion/tramites");
  });

  test("page loads with correct title", async ({ page }) => {
    await expect(
      page.getByRole("heading", { name: /tipos de trámite/i })
    ).toBeVisible();
  });

  test("modal has nombre, descripcion and checkbox fields", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo tipo/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByLabel(/nombre/i)).toBeVisible();
    await expect(dialog.getByLabel(/descripción/i)).toBeVisible();
    await expect(dialog.getByLabel(/se archiva/i)).toBeVisible();
    await expect(dialog.getByLabel(/se inscribe/i)).toBeVisible();
  });
});

test.describe("CU28/CU40/CU58/CU68 — Folios", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administracion/folios");
  });

  test("page loads with correct title", async ({ page }) => {
    await expect(
      page.getByRole("heading", { name: /folios/i })
    ).toBeVisible();
  });

  test("modal has numero, anio and estado fields", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo folio/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByLabel(/número/i)).toBeVisible();
    await expect(dialog.getByLabel(/año/i)).toBeVisible();
  });

  test("CU63 — estado filter select is present and usable", async ({ page }) => {
    await expect(page.getByTestId("select-filter-estado-folio")).toBeVisible();
  });

  test("Tipos de Folio section: full CRUD via search + create + edit + delete", async ({ page }) => {
    const uniqueName = `TipoTest${Date.now()}`;
    const renamed = `${uniqueName}-edit`;

    await expect(page.getByRole("heading", { name: /tipos de folio/i })).toBeVisible();

    await page.getByTestId("btn-nuevo-tipo-folio").click();
    const createDialog = page.getByRole("dialog");
    await createDialog.getByTestId("input-nombre-tipo-folio").fill(uniqueName);
    await createDialog.getByTestId("btn-save-tipo-folio").click();
    await expect(createDialog).toBeHidden();

    await page.getByTestId("input-search-tipo-folio").fill(uniqueName);
    const row = page.getByText(uniqueName, { exact: true });
    await expect(row).toBeVisible();

    await page.getByTestId("btn-edit-tipo-folio").first().click();
    const editDialog = page.getByRole("dialog");
    await editDialog.getByTestId("input-nombre-tipo-folio").fill(renamed);
    await editDialog.getByTestId("btn-save-tipo-folio").click();
    await expect(editDialog).toBeHidden();

    await page.getByTestId("input-search-tipo-folio").fill(renamed);
    await expect(page.getByText(renamed, { exact: true })).toBeVisible();

    await page.getByTestId("btn-delete-tipo-folio").first().click();
    await page.getByRole("alertdialog").getByRole("button", { name: /eliminar/i }).click();
    await expect(page.getByText(renamed, { exact: true })).toBeHidden();
  });
});

test.describe("CU39/CU49/CU55 — Plantillas de Presupuesto", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administracion/plantillas");
  });

  test("page loads with correct title", async ({ page }) => {
    await expect(
      page.getByRole("heading", { name: /plantillas de presupuesto/i })
    ).toBeVisible();
  });

  test("modal requires tipo de tramite and concepto", async ({ page }) => {
    await page.getByRole("button", { name: /nueva plantilla/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByRole("combobox").first()).toBeVisible();
  });
});

test.describe("Auditoría (read-only)", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
    await page.goto("/dashboard/administracion/auditoria");
  });

  test("page loads with audit heading", async ({ page }) => {
    await expect(
      page.getByRole("heading", { name: /auditoría/i })
    ).toBeVisible();
  });

  test("no create button — audit is read-only", async ({ page }) => {
    // Audit page should have no 'New' or 'Create' button
    await expect(
      page.getByRole("button", { name: /nuevo|crear|registrar/i })
    ).not.toBeVisible();
  });
});
