/**
 * Playwright E2E tests — Presupuestos module.
 * CU01, CU39, CU45, CU49, CU55, CU60
 *
 * Requires backend running on http://localhost:8080 and the full stack up.
 */
import { test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import {
  createPersona,
  createPresupuesto,
  createCompleteCaseGestion,
  createPago,
} from "./setup/api-helpers";

test.describe("Presupuestos module (CU01, CU39)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
    await page.goto("/dashboard/presupuestos");
  });

  test("CU01 — página de presupuestos carga", async ({ page }) => {
    await expect(page.getByRole("heading", { name: /presupuestos/i })).toBeVisible();
  });

  test("CU01 — botón nuevo presupuesto está visible", async ({ page }) => {
    await expect(
      page.getByRole("button", { name: /nuevo presupuesto/i })
    ).toBeVisible();
  });

  test("CU01 — modal de nuevo presupuesto se abre", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo presupuesto/i }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
  });

  test("CU01 — modal contiene campos requeridos", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo presupuesto/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByLabel(/fecha/i)).toBeVisible();
    await expect(dialog.getByLabel(/monto/i)).toBeVisible();
  });

  test("CU01 — modal contiene selector de cliente (persona)", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo presupuesto/i }).click();
    const dialog = page.getByRole("dialog");
    // The persona selector must be present as the primary workflow dependency
    await expect(dialog.getByTestId("select-persona")).toBeVisible();
  });

  test("CU01 — tabla de presupuestos está presente", async ({ page }) => {
    const tableOrEmpty = page
      .getByRole("table")
      .or(page.getByText(/no hay presupuestos/i));
    await expect(tableOrEmpty).toBeVisible({ timeout: 10000 });
  });

  test("CU60 — seleccionar un estado llama a GET /presupuestos/buscar", async ({ page }) => {
    const searchRequest = page.waitForRequest((req) =>
      req.url().includes("/api/v1/presupuestos/buscar") && req.method() === "GET"
    );
    await page.getByTestId("select-estado").click();
    await page.getByRole("option", { name: /borrador/i }).click();
    await searchRequest;
  });

  test("#607 — selector de estado se opera solo con teclado", async ({ page }) => {
    const searchRequest = page.waitForRequest((req) =>
      req.url().includes("/api/v1/presupuestos/buscar") && req.method() === "GET"
    );
    await page.getByTestId("select-estado").focus();
    await page.keyboard.press("Enter");
    const listbox = page.getByRole("listbox");
    await expect(listbox).toBeVisible();
    // Radix highlights the currently-selected item ("Todos") on open; ArrowDown moves
    // the roving-tabindex highlight to the next option ("Borrador").
    await page.keyboard.press("ArrowDown");
    await expect(listbox.getByRole("option", { name: /borrador/i })).toHaveAttribute(
      "data-highlighted",
      ""
    );
    await page.keyboard.press("Enter");
    await searchRequest;
    await expect(listbox).not.toBeVisible();
  });
});

test.describe("Pagos module (CU15, CU47)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
    await page.goto("/dashboard/pagos");
  });

  test("CU15 — página de pagos carga correctamente (#321 fix)", async ({
    page,
  }) => {
    // Issue #321: pagos module was showing 'under construction' — now has full CRUD
    await expect(page.getByRole("heading", { name: /pagos/i })).toBeVisible();
    // Must NOT show placeholder text
    await expect(page.getByText(/under construction|en construcción/i)).not.toBeVisible();
  });

  test("CU15 — botón nuevo pago está visible", async ({ page }) => {
    await expect(page.getByTestId("btn-nuevo-pago")).toBeVisible({ timeout: 10000 });
  });

  test("CU15 — modal de nuevo pago tiene campos de fecha, monto y método", async ({
    page,
  }) => {
    await page.getByTestId("btn-nuevo-pago").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByLabel(/fecha/i)).toBeVisible();
    await expect(dialog.getByLabel(/monto/i)).toBeVisible();
    await expect(dialog.getByLabel(/método/i)).toBeVisible();
  });

  test("CU47 — tabla de pagos o mensaje de vacío está presente", async ({
    page,
  }) => {
    const tableOrEmpty = page
      .getByRole("table")
      .or(page.getByText(/no hay pagos/i));
    await expect(tableOrEmpty.first()).toBeVisible({ timeout: 10000 });
  });
});

test.describe("CU47 — Resumen financiero de presupuesto (#820)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("muestra total, saldo pendiente y gestión asociada sin pagos", async ({ page }) => {
    const persona = await createPersona(page);
    const idPersona = persona.data!.idPersona;
    const presupuesto = await createPresupuesto(page, idPersona, undefined, { monto: 1000 });
    const idPresupuesto = presupuesto.data!.idPresupuesto;
    const gestion = await createCompleteCaseGestion(page, { presupuestoId: idPresupuesto });

    await page.goto("/dashboard/presupuestos");
    await page.waitForLoadState("domcontentloaded");

    await page.getByTestId(`btn-resumen-presupuesto-${idPresupuesto}`).click();
    const dialog = page.getByTestId("dialog-resumen-presupuesto");
    await expect(dialog).toBeVisible({ timeout: 10000 });

    await expect(dialog).toContainText(String(gestion.data!.numero));
    await expect(dialog.getByTestId("resumen-sin-pagos")).toBeVisible();
  });

  test("incluye los pagos aplicados y refleja el saldo pendiente restante", async ({ page }) => {
    const persona = await createPersona(page);
    const idPersona = persona.data!.idPersona;
    const presupuesto = await createPresupuesto(page, idPersona, undefined, { monto: 1000 });
    const idPresupuesto = presupuesto.data!.idPresupuesto;
    await createCompleteCaseGestion(page, { presupuestoId: idPresupuesto });
    const pago = await createPago(page, idPresupuesto, { monto: 400 });
    expect(pago.ok).toBeTruthy();

    await page.goto("/dashboard/presupuestos");
    await page.waitForLoadState("domcontentloaded");

    await page.getByTestId(`btn-resumen-presupuesto-${idPresupuesto}`).click();
    const dialog = page.getByTestId("dialog-resumen-presupuesto");
    await expect(dialog).toBeVisible({ timeout: 10000 });

    await expect(dialog.getByTestId("resumen-sin-pagos")).not.toBeVisible();
    await expect(dialog.getByRole("table")).toBeVisible();
    await expect(dialog).toContainText("400");
  });

  test("muestra mensaje de no encontrado cuando el resumen responde 404", async ({ page }) => {
    const persona = await createPersona(page);
    const idPersona = persona.data!.idPersona;
    const presupuesto = await createPresupuesto(page, idPersona, undefined, { monto: 1000 });
    const idPresupuesto = presupuesto.data!.idPresupuesto;

    await page.route(`**/api/v1/presupuestos/${idPresupuesto}/resumen`, (route) =>
      route.fulfill({ status: 404, contentType: "application/json", body: "{}" })
    );

    await page.goto("/dashboard/presupuestos");
    await page.waitForLoadState("domcontentloaded");

    await page.getByTestId(`btn-resumen-presupuesto-${idPresupuesto}`).click();
    const dialog = page.getByTestId("dialog-resumen-presupuesto");
    await expect(dialog).toBeVisible({ timeout: 10000 });
    await expect(dialog.getByTestId("resumen-not-found")).toBeVisible();
  });
});
