/**
 * TS-0014 - Payments workflow (CU15 → CU47)
 * CU15 - Process a payment
 * CU47 - Look up a payment
 * Issue #796 (budget picker and pending-balance visibility)
 * Issue #821 (payment-status badge: SIN_PAGOS / PARCIAL / SALDADO)
 * Issue #848 (overpayment rejection message)
 * Issue #1544 (CU47: consult one budget's payments, summary and balance)
 * Issue #1557 (CU47: inclusive date-range filter, URL-persisted)
 */
import { type Page, test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { createPerson, createBudget, createPayment } from "./setup/api-helpers";

// ──────────────────────────────────────────────
// Seed helpers
// ──────────────────────────────────────────────

/** Seed a person + budget with a given amount, returning both IDs and the lastName used to find it in the UI. */
async function seedBudget(page: Page, amount: number): Promise<{ idBudget: number; lastName: string }> {
  const personResult = await createPerson(page);
  if (!personResult.ok || !personResult.data?.personId) {
    throw new Error(`Failed to seed persona: ${personResult.error ?? JSON.stringify(personResult.data)}`);
  }
  const budgetResult = await createBudget(page, personResult.data.personId, undefined, { propertyAmount: amount });
  if (!budgetResult.ok || !budgetResult.data?.idBudget) {
    throw new Error(`Failed to seed budget: ${budgetResult.error ?? JSON.stringify(budgetResult.data)}`);
  }
  return { idBudget: budgetResult.data.idBudget, lastName: personResult.data.lastName! };
}

test.describe("CU15 - Process a payment", () => {
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

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await expect(budgetSelector).toBeVisible();
  });

  test("CU15-SALDO-02 (#796): selecting a budget shows its client name and outstanding balance", async ({ page }) => {
    const amountBudget = 75000;
    const { lastName } = await seedBudget(page, amountBudget);

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(lastName);

    const budgetOption = page.getByRole("option", { name: new RegExp(lastName, "i") });
    await expect(budgetOption).toBeVisible();
    await budgetOption.click();

    const balanceDisplay = page.getByRole("dialog").getByText(/saldo pendiente/i);
    await expect(balanceDisplay).toBeVisible({ timeout: 5000 });

    // The picker shows "#<id> — <client>"; the budget amount is in the balance panel (#1340).
    await expect(budgetSelector).toHaveValue(new RegExp(lastName, "i"));
    await expect(page.getByRole("dialog").getByText(/Presupuestado: \$\s?75\.000/)).toBeVisible();
  });

  test("CU15-SALDO-03 (#796): the balance updates when the budget selection changes", async ({ page }) => {
    const { lastName: lastName1 } = await seedBudget(page, 50000);
    const { lastName: lastName2 } = await seedBudget(page, 100000);

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(lastName1);
    await page.getByRole("option", { name: new RegExp(lastName1, "i") }).click();

    let balanceDisplay = page.getByRole("dialog").getByText(/saldo pendiente/i);
    await expect(balanceDisplay).toBeVisible();
    const balanceText1 = await balanceDisplay.locator("../..").textContent();

    await budgetSelector.click();

    // Budget pickers search the server (#1340): type the client first.

    await budgetSelector.fill(lastName2);
    await page.getByRole("option", { name: new RegExp(lastName2, "i") }).click();

    balanceDisplay = page.getByRole("dialog").getByText(/saldo pendiente/i);
    await expect(balanceDisplay).toBeVisible();
    const balanceText2 = await balanceDisplay.locator("../..").textContent();

    expect(balanceText1).not.toBe(balanceText2);
  });

  test("CU15-SALDO-04 (#848): submitting an amount over the outstanding balance shows a specific rejection message", async ({ page }) => {
    const { lastName } = await seedBudget(page, 50000);

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(lastName);
    await page.getByRole("option", { name: new RegExp(lastName, "i") }).click();

    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByRole("dialog").locator('input[type="number"]').fill("999999");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();

    await expect(page.getByText(/excede el saldo pendiente/i)).toBeVisible({ timeout: 5000 });
    await expect(page.getByRole("dialog")).toBeVisible();
  });

  test("CU15-GW02: Given the form is open, When it is filled and submitted, Then the payment is recorded", async ({ page }) => {
    const { lastName } = await seedBudget(page, 50000);

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(lastName);
    await page.getByRole("option", { name: new RegExp(lastName, "i") }).click();

    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByRole("dialog").locator('input[type="number"]').fill("10000");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();

    await expect(page.getByRole("dialog")).toBeHidden();
    await steps.thenToastIsVisible();
  });

  test.skip("CU15-GW03 (#1146): Given a payment exists, When view details is clicked, Then the details are shown", () => {
    // Skipped (#1146): the payments table has no view button per row — only edit and delete icons.
  });

  test("CU15-RECIBO-01 (#23): Given a payment exists, When issue receipt is clicked, Then a PDF is downloaded", async ({ page }) => {
    const amountUnico = 345;
    const { idBudget } = await seedBudget(page, amountUnico + 1000);
    const paymentResult = await createPayment(page, idBudget, { amount: amountUnico });
    if (!paymentResult.ok || !paymentResult.data?.idPayment) {
      throw new Error(`Failed to seed payment: ${paymentResult.error ?? JSON.stringify(paymentResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");

    // Row's accessible name starts with the bare id (no "#" prefix) — see TABLE-01/02/03.
    const row = page.getByRole("row", { name: new RegExp(`^${paymentResult.data.idPayment}\\s`) });
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
    const { lastName } = await seedBudget(page, 60000);

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(lastName);
    await page.getByRole("option", { name: new RegExp(lastName, "i") }).click();

    const badge = page.getByTestId("status-payment-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/sin pagos/i);
  });

  test("ESTADO-02: a budget with a partial payment shows the PARCIAL badge", async ({ page }) => {
    const { idBudget, lastName } = await seedBudget(page, 100000);
    const paymentResult = await createPayment(page, idBudget, { amount: 40000 });
    if (!paymentResult.ok) {
      throw new Error(`Failed to seed payment: ${paymentResult.error ?? JSON.stringify(paymentResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(lastName);
    await page.getByRole("option", { name: new RegExp(lastName, "i") }).click();

    const badge = page.getByTestId("status-payment-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/parcial/i);
  });

  test("ESTADO-03: a fully paid budget shows the SALDADO badge", async ({ page }) => {
    const { idBudget, lastName } = await seedBudget(page, 80000);
    const paymentResult = await createPayment(page, idBudget, { amount: 80000 });
    if (!paymentResult.ok) {
      throw new Error(`Failed to seed payment: ${paymentResult.error ?? JSON.stringify(paymentResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const budgetSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await budgetSelector.click();
    // Budget pickers search the server (#1340): type the client first.
    await budgetSelector.fill(lastName);
    await page.getByRole("option", { name: new RegExp(lastName, "i") }).click();

    const badge = page.getByTestId("status-payment-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/saldado/i);
  });

  test("CU47-GW01 (#1557): Given payments on several days, When filtered by date range, Then only the inclusive range is shown", async ({ page }) => {
    const { idBudget } = await seedBudget(page, 100000);
    for (const [date, amount] of [["2031-02-10", 1111], ["2031-02-15", 2222], ["2031-02-20", 3333]] as const) {
      const r = await createPayment(page, idBudget, { amount, date });
      if (!r.ok) throw new Error(`Failed to seed payment: ${r.error ?? JSON.stringify(r.data)}`);
    }

    // Given the budget is consulted and the range ends on the middle payment's day.
    await steps.givenUserIsOnPage(`/dashboard/payments?budget=${idBudget}&from=2031-02-10&to=2031-02-15`);
    const rows = page.locator("tbody tr");
    // Then both boundary days are included and the later one is not.
    await expect(rows).toHaveCount(2, { timeout: 10000 });

    // When the "to" day moves back past "from", Then an inline error is shown.
    await page.getByTestId("input-payments-to").fill("2031-02-01");
    await expect(page.getByTestId("payments-range-error")).toBeVisible();
    await expect(page).toHaveURL(/to=2031-02-01/);

    // When the range is cleared, Then all three payments come back.
    await page.getByTestId("input-payments-to").fill("");
    await page.getByTestId("input-payments-from").fill("");
    await expect(rows).toHaveCount(3, { timeout: 10000 });
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
    const paymentResult = await createPayment(page, idBudget, { amount: 12345, paymentMethod: "Transferencia" });
    if (!paymentResult.ok || !paymentResult.data?.idPayment) {
      throw new Error(`Failed to seed payment: ${paymentResult.error ?? JSON.stringify(paymentResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");

    const row = page.getByRole("row", { name: new RegExp(`^${paymentResult.data.idPayment}\\s`) });
    await expect(row).toBeVisible({ timeout: 5000 });
    await expect(row).toContainText("Transferencia");
    await expect(row).toContainText(`#${idBudget}`);
  });

  test("TABLE-02: Given a payment exists, When its amount is edited, Then the table shows the update", async ({ page }) => {
    const { idBudget } = await seedBudget(page, 30000);
    const paymentResult = await createPayment(page, idBudget, { amount: 1000, paymentMethod: "Efectivo" });
    if (!paymentResult.ok || !paymentResult.data?.idPayment) {
      throw new Error(`Failed to seed payment: ${paymentResult.error ?? JSON.stringify(paymentResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");

    const row = page.getByRole("row", { name: new RegExp(`^${paymentResult.data.idPayment}\\s`) });
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
    const paymentResult = await createPayment(page, idBudget, { amount: 500 });
    if (!paymentResult.ok || !paymentResult.data?.idPayment) {
      throw new Error(`Failed to seed payment: ${paymentResult.error ?? JSON.stringify(paymentResult.data)}`);
    }

    await steps.givenUserIsOnPage("/dashboard/payments");

    const row = page.getByRole("row", { name: new RegExp(`^${paymentResult.data.idPayment}\\s`) });
    await expect(row).toBeVisible({ timeout: 5000 });
    await row.getByRole("button").nth(2).click();

    await page.getByRole("button", { name: /confirmar|eliminar|s[ií]/i }).last().click();

    await expect(row).toBeHidden({ timeout: 5000 });
  });
});

test.describe("CU47 - Consult payments by budget (#1544)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  async function seedTwoPayments(page: Page) {
    const seeded = await seedBudget(page, 50000);
    for (const [amount, paymentMethod] of [[1000, "Efectivo"], [2000, "Transferencia"]] as const) {
      const r = await createPayment(page, seeded.idBudget, { amount, paymentMethod });
      if (!r.ok) throw new Error(`Failed to seed payment: ${r.error ?? JSON.stringify(r.data)}`);
    }
    return seeded;
  }

  test("CU47-BUDGET-01: Given a budget with 2 payments, When it is consulted, Then its summary and only its payments are shown", async ({ page }) => {
    const { idBudget, lastName } = await seedTwoPayments(page);
    await steps.givenUserIsOnPage("/dashboard/payments");

    const picker = page.getByRole("combobox", { name: /consultar por presupuesto/i });
    await picker.click();
    await picker.fill(lastName);
    await page.getByRole("option", { name: new RegExp(lastName, "i") }).click();

    await expect(page).toHaveURL(new RegExp(`[?&]budget=${idBudget}`));
    const summary = page.getByTestId("payment-budget-summary");
    await expect(summary).toBeVisible();
    await expect(page.getByTestId("payment-budget-status")).toHaveText(/parcial/i);
    await expect(page.getByTestId("payment-budget-paid")).toContainText(/3[.,]?000/);
    const rows = page.locator("table tbody tr");
    await expect(rows).toHaveCount(2);

    // Reload keeps the selection; clearing returns to the full list.
    await page.reload();
    await expect(page.getByTestId("payment-budget-summary")).toBeVisible();
    await steps.givenUserIsOnPage("/dashboard/payments");
    await expect(page.getByTestId("payment-budget-summary")).toHaveCount(0);
  });

  test("CU47-BUDGET-02: the consult works at 390px", async ({ page }) => {
    const { idBudget } = await seedTwoPayments(page);
    await page.setViewportSize({ width: 390, height: 844 });
    await steps.givenUserIsOnPage(`/dashboard/payments?budget=${idBudget}`);
    await expect(page.getByTestId("payment-budget-summary")).toBeVisible();
    await expect(page.getByTestId("payment-budget-status")).toHaveText(/parcial/i);
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(overflow).toBeLessThanOrEqual(1);
  });
});
