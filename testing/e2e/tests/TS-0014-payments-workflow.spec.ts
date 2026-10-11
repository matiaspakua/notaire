/**
 * TS-0014 - Pagos Workflow (CU15 → CU47)
 * CU15 - Procesar Payment
 * CU47 - Look up a payment
 * Issue #796 (budget picker and pending-balance visibility)
 * Issue #821 (payment-status badge: SIN_PAGOS / PARCIAL / SALDADO)
 * Issue #848 (overpayment rejection message)
 */
import { type Page, test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { createPerson, createBudget, createPayment } from "./setup/api-helpers";

// ──────────────────────────────────────────────
// Seed helpers
// ──────────────────────────────────────────────

/** Seed a person + budget with a given amount, returning both IDs and the apellido used to find it in the UI. */
async function seedBudget(page: Page, amount: number): Promise<{ idBudget: number; apellido: string }> {
  const personResult = await createPerson(page);
  if (!personResult.ok || !personResult.data?.personId) {
    throw new Error(`Failed to seed persona: ${personResult.error ?? JSON.stringify(personResult.data)}`);
  }
  const budgetResult = await createBudget(page, personResult.data.personId, undefined, { propertyAmount: amount });
  if (!budgetResult.ok || !budgetResult.data?.idBudget) {
    throw new Error(`Failed to seed presupuesto: ${budgetResult.error ?? JSON.stringify(budgetResult.data)}`);
  }
  return { idBudget: budgetResult.data.idBudget, apellido: personResult.data.lastName! };
}

test.describe("CU15 - Procesar Payment", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/payments");
  });

  test("CU15-GW01: Given the user is on the payments page, When they click new payment, Then the modal opens", async () => {
    await steps.givenModuleIsVisible("Pagos");
    await steps.whenUserClicksButton("nuevo pago");

    await steps.thenModalIsVisible("Nuevo pago");
    await steps.thenFormHasField("fecha");
    await steps.thenFormHasField("monto");
    await steps.thenFormHasField("método");
  });

  test("CU15-SALDO-01 (#796): the payment form has a budget picker, not a numeric input", async ({ page }) => {
    await steps.whenUserClicksButton("nuevo pago");

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /budget/i });
    await expect(budgetSelector).toBeVisible();
  });

  test("CU15-SALDO-02 (#796): selecting a budget shows its client name and outstanding balance", async ({ page }) => {
    const amountBudget = 75000;
    const { apellido } = await seedBudget(page, amountBudget);

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /budget/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(apellido);

    const budgetOption = page.getByRole("option", { name: new RegExp(apellido, "i") });
    await expect(budgetOption).toBeVisible();
    await budgetOption.click();

    const saldoDisplay = page.getByRole("dialog").getByText(/balance pendiente/i);
    await expect(saldoDisplay).toBeVisible({ timeout: 5000 });

    // The picker shows "#<id> — <client>"; the budget amount is in the balance panel (#1340).
    await expect(budgetSelector).toHaveValue(new RegExp(apellido, "i"));
    await expect(page.getByRole("dialog").getByText(/Presupuestado: \$\s?75\.000/)).toBeVisible();
  });

  test("CU15-SALDO-03 (#796): the balance updates when the budget selection changes", async ({ page }) => {
    const { apellido: apellido1 } = await seedBudget(page, 50000);
    const { apellido: apellido2 } = await seedBudget(page, 100000);

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /budget/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(apellido1);
    await page.getByRole("option", { name: new RegExp(apellido1, "i") }).click();

    let saldoDisplay = page.getByRole("dialog").getByText(/balance pendiente/i);
    await expect(saldoDisplay).toBeVisible();
    const saldoText1 = await saldoDisplay.locator("../..").textContent();

    await budgetSelector.click();

    // Budget pickers search the server (#1340): type the client first.

    await budgetSelector.fill(apellido2);
    await page.getByRole("option", { name: new RegExp(apellido2, "i") }).click();

    saldoDisplay = page.getByRole("dialog").getByText(/balance pendiente/i);
    await expect(saldoDisplay).toBeVisible();
    const saldoText2 = await saldoDisplay.locator("../..").textContent();

    expect(saldoText1).not.toBe(saldoText2);
  });

  test("CU15-SALDO-04 (#848): submitting an amount over the outstanding balance shows a specific rejection message", async ({ page }) => {
    const { apellido } = await seedBudget(page, 50000);

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /budget/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(apellido);
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    await page.getByRole("dialog").getByLabel(/date/i).fill("2026-09-01");
    await page.getByRole("dialog").locator('input[type="number"]').fill("999999");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();

    await expect(page.getByText(/excede el saldo pendiente/i)).toBeVisible({ timeout: 5000 });
    await expect(page.getByRole("dialog")).toBeVisible();
  });

  test("CU15-GW02: Given the form is open, When it is filled and submitted, Then the payment is recorded", async ({ page }) => {
    const { apellido } = await seedBudget(page, 50000);

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /budget/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(apellido);
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    await page.getByRole("dialog").getByLabel(/date/i).fill("2026-09-01");
    await page.getByRole("dialog").locator('input[type="number"]').fill("10000");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();

    await expect(page.getByRole("dialog")).toBeHidden();
    await steps.thenToastIsVisible();
  });

  test.skip("CU15-GW03 (#1146): Given a payment exists, When view details is clicked, Then the details are shown", () => {
    // Skipped (#1146): pagos table has no "ver" button per row — only edit/delete icons.
  });

  test("CU15-RECIBO-01 (#23): Given a payment exists, When issue receipt is clicked, Then a PDF is downloaded", async ({ page }) => {
    const amountUnico = 345;
    const { idBudget } = await seedBudget(page, amountUnico + 1000);
    const pagoResult = await createPayment(page, idBudget, { amount: amountUnico });
    if (!pagoResult.ok || !pagoResult.data?.idPayment) {
      throw new Error(`Failed to seed pago: ${pagoResult.error ?? JSON.stringify(pagoResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");

    // Row's accessible name starts with the bare id (no "#" prefix) — see TABLE-01/02/03.
    const row = page.getByRole("row", { name: new RegExp(`^${pagoResult.data.idPayment}\\s`) });
    await expect(row).toBeVisible({ timeout: 5000 });

    const downloadPromise = page.waitForEvent("download");
    await row.getByTitle(/emitir recibo/i).click();
    const download = await downloadPromise;

    expect(download.suggestedFilename()).toMatch(/recibo_pago_.*\.pdf/);
  });
});

test.describe("CU47 - Look up a payment (payment status #821)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("ESTADO-01: a new budget without payments shows the SIN_PAGOS badge", async ({ page }) => {
    const { apellido } = await seedBudget(page, 60000);

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /budget/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(apellido);
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    const badge = page.getByTestId("status-payment-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/sin pagos/i);
  });

  test("ESTADO-02: a budget with a partial payment shows the PARCIAL badge", async ({ page }) => {
    const { idBudget, apellido } = await seedBudget(page, 100000);
    const pagoResult = await createPayment(page, idBudget, { amount: 40000 });
    if (!pagoResult.ok) {
      throw new Error(`Failed to seed pago: ${pagoResult.error ?? JSON.stringify(pagoResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /budget/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(apellido);
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    const badge = page.getByTestId("status-payment-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/parcial/i);
  });

  test("ESTADO-03: a fully paid budget shows the SALDADO badge", async ({ page }) => {
    const { idBudget, apellido } = await seedBudget(page, 80000);
    const pagoResult = await createPayment(page, idBudget, { amount: 80000 });
    if (!pagoResult.ok) {
      throw new Error(`Failed to seed pago: ${pagoResult.error ?? JSON.stringify(pagoResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /budget/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(apellido);
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    const badge = page.getByTestId("status-payment-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/saldado/i);
  });

  test.skip("CU47-GW01 (#1146): Given the payments page, When filtered by date, Then the filtered list is shown", () => {
    // Skipped (#1146): pagos page has no "fecha desde" / "fecha hasta" filter inputs.
  });
});

test.describe("CU47 - Payments table CRUD (list, edit, delete)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("TABLE-01: Given a payment exists, When the payments page is opened, Then it renders in the table with the correct data", async ({ page }) => {
    const { idBudget } = await seedBudget(page, 20000);
    const pagoResult = await createPayment(page, idBudget, { amount: 12345, paymentMethod: "Transferencia" });
    if (!pagoResult.ok || !pagoResult.data?.idPayment) {
      throw new Error(`Failed to seed pago: ${pagoResult.error ?? JSON.stringify(pagoResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");

    const row = page.getByRole("row", { name: new RegExp(`^${pagoResult.data.idPayment}\\s`) });
    await expect(row).toBeVisible({ timeout: 5000 });
    await expect(row).toContainText("Transferencia");
    await expect(row).toContainText(`#${idBudget}`);
  });

  test("TABLE-02: Given a payment exists, When its amount is edited, Then the table shows the update", async ({ page }) => {
    const { idBudget } = await seedBudget(page, 30000);
    const pagoResult = await createPayment(page, idBudget, { amount: 1000, paymentMethod: "Efectivo" });
    if (!pagoResult.ok || !pagoResult.data?.idPayment) {
      throw new Error(`Failed to seed pago: ${pagoResult.error ?? JSON.stringify(pagoResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");

    const row = page.getByRole("row", { name: new RegExp(`^${pagoResult.data.idPayment}\\s`) });
    await expect(row).toBeVisible({ timeout: 5000 });
    await row.getByRole("button").nth(1).click();

    await expect(page.getByRole("dialog")).toBeVisible();
    const amountInput = page.getByRole("dialog").locator('input[type="number"]');
    await amountInput.fill("1500");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|actualizar/i }).click();

    await expect(page.getByRole("dialog")).toBeHidden();
    await expect(row).toContainText("1.500");
  });

  test("TABLE-03: Given a payment exists, When it is deleted, Then it is removed from the table", async ({ page }) => {
    const { idBudget } = await seedBudget(page, 15000);
    const pagoResult = await createPayment(page, idBudget, { amount: 500 });
    if (!pagoResult.ok || !pagoResult.data?.idPayment) {
      throw new Error(`Failed to seed pago: ${pagoResult.error ?? JSON.stringify(pagoResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");

    const row = page.getByRole("row", { name: new RegExp(`^${pagoResult.data.idPayment}\\s`) });
    await expect(row).toBeVisible({ timeout: 5000 });
    await row.getByRole("button").nth(2).click();

    await page.getByRole("button", { name: /confirmar|eliminar|s[ií]/i }).last().click();

    await expect(row).toBeHidden({ timeout: 5000 });
  });
});
