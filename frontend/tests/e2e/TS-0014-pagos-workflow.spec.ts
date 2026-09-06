/**
 * TS-0014 - Pagos Workflow (CU15 → CU47)
 * CU15 - Procesar Pago
 * CU47 - Consultar Pago
 * Issue #796 (presupuesto picker + saldo pendiente visibility)
 * Issue #821 (estado de pago badge: SIN_PAGOS / PARCIAL / SALDADO)
 * Issue #848 (overpayment rejection message)
 */
import { type Page, test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { createPersona, createPresupuesto, createPago } from "./setup/api-helpers";

// ──────────────────────────────────────────────
// Seed helpers
// ──────────────────────────────────────────────

/** Seed a persona + presupuesto with a given monto, returning both IDs and the apellido used to find it in the UI. */
async function seedPresupuesto(page: Page, monto: number): Promise<{ idPresupuesto: number; apellido: string }> {
  const personaResult = await createPersona(page);
  if (!personaResult.ok || !personaResult.data?.idPersona) {
    throw new Error(`Failed to seed persona: ${personaResult.error ?? JSON.stringify(personaResult.data)}`);
  }
  const presupuestoResult = await createPresupuesto(page, personaResult.data.idPersona, undefined, { monto });
  if (!presupuestoResult.ok || !presupuestoResult.data?.idPresupuesto) {
    throw new Error(`Failed to seed presupuesto: ${presupuestoResult.error ?? JSON.stringify(presupuestoResult.data)}`);
  }
  return { idPresupuesto: presupuestoResult.data.idPresupuesto, apellido: personaResult.data.apellido! };
}

test.describe("CU15 - Procesar Pago", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/pagos");
  });

  test("CU15-GW01: Given user on pagos page, When click nuevo pago, Then modal opens", async () => {
    await steps.givenModuleIsVisible("Pagos");
    await steps.whenUserClicksButton("nuevo pago");

    await steps.thenModalIsVisible("Nuevo pago");
    await steps.thenFormHasField("fecha");
    await steps.thenFormHasField("monto");
    await steps.thenFormHasField("método");
  });

  test("CU15-SALDO-01 (#796): Payment form has presupuesto picker, not a numeric input", async ({ page }) => {
    await steps.whenUserClicksButton("nuevo pago");

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await expect(presupuestoSelector).toBeVisible();
  });

  test("CU15-SALDO-02 (#796): Selecting a presupuesto shows its client name and saldo pendiente", async ({ page }) => {
    const montoPresupuesto = 75000;
    const { apellido } = await seedPresupuesto(page, montoPresupuesto);

    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();

    const presupuestoOption = page.getByRole("option", { name: new RegExp(apellido, "i") });
    await expect(presupuestoOption).toBeVisible();
    await presupuestoOption.click();

    const saldoDisplay = page.getByRole("dialog").getByText(/saldo pendiente/i);
    await expect(saldoDisplay).toBeVisible({ timeout: 5000 });

    const saldoText = await page.getByRole("dialog").getByText(new RegExp(String(montoPresupuesto))).textContent();
    expect(saldoText).toContain(String(montoPresupuesto));
  });

  test("CU15-SALDO-03 (#796): Saldo updates when presupuesto selection changes", async ({ page }) => {
    const { apellido: apellido1 } = await seedPresupuesto(page, 50000);
    const { apellido: apellido2 } = await seedPresupuesto(page, 100000);

    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();
    await page.getByRole("option", { name: new RegExp(apellido1, "i") }).click();

    let saldoDisplay = page.getByRole("dialog").getByText(/saldo pendiente/i);
    await expect(saldoDisplay).toBeVisible();
    const saldoText1 = await saldoDisplay.locator("../..").textContent();

    await presupuestoSelector.click();
    await page.getByRole("option", { name: new RegExp(apellido2, "i") }).click();

    saldoDisplay = page.getByRole("dialog").getByText(/saldo pendiente/i);
    await expect(saldoDisplay).toBeVisible();
    const saldoText2 = await saldoDisplay.locator("../..").textContent();

    expect(saldoText1).not.toBe(saldoText2);
  });

  test("CU15-SALDO-04 (#848): Submitting a monto over saldo pendiente shows a specific rejection message", async ({ page }) => {
    const { apellido } = await seedPresupuesto(page, 50000);

    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByRole("dialog").locator('input[type="number"]').fill("999999");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();

    await expect(page.getByText(/excede el saldo pendiente/i)).toBeVisible({ timeout: 5000 });
    await expect(page.getByRole("dialog")).toBeVisible();
  });

  test("CU15-GW02: Given form open, When fill and submit, Then pago is registered", async ({ page }) => {
    const { apellido } = await seedPresupuesto(page, 50000);

    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByRole("dialog").locator('input[type="number"]').fill("10000");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();

    await expect(page.getByRole("dialog")).toBeHidden();
    await steps.thenToastIsVisible();
  });

  test.skip("CU15-GW03: Given pago exists, When click ver detalle, Then shows details", () => {
    // Skipped: pagos table has no "ver" button per row — only edit/delete icons.
  });

  test("CU15-RECIBO-01 (#23): Given pago exists, When click emitir recibo, Then PDF is downloaded", async ({ page }) => {
    const montoUnico = 345;
    const { idPresupuesto } = await seedPresupuesto(page, montoUnico + 1000);
    const pagoResult = await createPago(page, idPresupuesto, { monto: montoUnico });
    if (!pagoResult.ok || !pagoResult.data?.idPago) {
      throw new Error(`Failed to seed pago: ${pagoResult.error ?? JSON.stringify(pagoResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/pagos");

    const row = page.getByRole("row", { name: new RegExp(`#${pagoResult.data.idPago}\\b`) });
    await expect(row).toBeVisible({ timeout: 5000 });

    const downloadPromise = page.waitForEvent("download");
    await row.getByTitle(/emitir recibo/i).click();
    const download = await downloadPromise;

    expect(download.suggestedFilename()).toMatch(/recibo_pago_.*\.pdf/);
  });
});

test.describe("CU47 - Consultar Pago (Estado de Pago #821)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("ESTADO-01: New presupuesto without payments shows SIN_PAGOS badge", async ({ page }) => {
    const { apellido } = await seedPresupuesto(page, 60000);

    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    const badge = page.getByTestId("estado-pago-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/sin pagos/i);
  });

  test("ESTADO-02: Presupuesto with a partial payment shows PARCIAL badge", async ({ page }) => {
    const { idPresupuesto, apellido } = await seedPresupuesto(page, 100000);
    const pagoResult = await createPago(page, idPresupuesto, { monto: 40000 });
    if (!pagoResult.ok) {
      throw new Error(`Failed to seed pago: ${pagoResult.error ?? JSON.stringify(pagoResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    const badge = page.getByTestId("estado-pago-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/parcial/i);
  });

  test("ESTADO-03: Presupuesto fully paid shows SALDADO badge", async ({ page }) => {
    const { idPresupuesto, apellido } = await seedPresupuesto(page, 80000);
    const pagoResult = await createPago(page, idPresupuesto, { monto: 80000 });
    if (!pagoResult.ok) {
      throw new Error(`Failed to seed pago: ${pagoResult.error ?? JSON.stringify(pagoResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    const badge = page.getByTestId("estado-pago-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/saldado/i);
  });

  test.skip("CU47-GW01: Given on pagos page, When filter by date, Then shows filtered", () => {
    // Skipped: pagos page has no "fecha desde" / "fecha hasta" filter inputs.
  });
});
