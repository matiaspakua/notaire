/**
 * CU15 - Procesar Pago: Presupuesto Picker & Saldo Visibility
 * Issue #796: Payment form must show saldo pendiente before confirming
 *
 * Test suite for presupuesto selection and saldo display.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU15 - Procesar Pago (Saldo Visibility #796)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/pagos");
  });

  test("CU15-SALDO-01: Payment form has presupuesto picker (not numeric input)", async ({ page }) => {
    // GIVEN
    await steps.givenModuleIsVisible("Pagos");
    await steps.whenUserClicksButton("nuevo pago");

    // WHEN/THEN — presupuesto field should be a select/combobox, not a number input
    const presupuestoSelector = page
      .getByRole("dialog")
      .getByRole("combobox", { name: /presupuesto/i });

    await expect(presupuestoSelector).toBeVisible();
    console.log("✅ Presupuesto picker found (not number input)");
  });

  test("CU15-SALDO-02: Presupuesto picker lists existing presupuestos with client names", async ({ page }) => {
    // Given: a presupuesto exists in the database (seeded by global setup)
    // When: operator opens the picker
    // Then: list shows presupuesto with client name

    // First create a presupuesto so we have something to pick
    const suffix = Date.now().toString().slice(-6);
    const apellido = `PagoTest${suffix}`;

    // Create cliente
    await steps.givenUserIsOnPage("/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    await expect(page.getByRole("dialog")).toBeVisible();

    await page.getByTestId("input-nombre").fill("Cliente");
    await page.getByTestId("input-apellido").fill(apellido);
    await page.getByRole("dialog").getByLabel(/dni/i).fill(`DNI${suffix}`);
    await page.getByTestId("check-es-cliente").click();
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    // Create presupuesto
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    await page.getByTestId("btn-nuevo-presupuesto").click();
    await expect(page.getByRole("dialog")).toBeVisible();

    await page.getByTestId("select-persona").click();
    await page
      .getByRole("option", { name: new RegExp(apellido, "i") })
      .evaluate((el: HTMLElement) => el.click());
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByTestId("input-monto").fill("50000");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    // Now test the payment form presupuesto picker
    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    // Click the presupuesto picker
    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();

    // Verify picker shows option with client name
    const presupuestoOption = page.getByRole("option", { name: new RegExp(apellido, "i") });
    await expect(presupuestoOption).toBeVisible();
    console.log("✅ Presupuesto picker lists presupuestos with client names");
  });

  test("CU15-SALDO-03: Selecting presupuesto displays its saldo pendiente", async ({ page }) => {
    // Given: presupuesto with known balance (e.g. 50000)
    const suffix = Date.now().toString().slice(-6);
    const apellido = `SaldoTest${suffix}`;
    const montoPresupuesto = "75000";

    // Create cliente
    await steps.givenUserIsOnPage("/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    await expect(page.getByRole("dialog")).toBeVisible();

    await page.getByTestId("input-nombre").fill("Cliente");
    await page.getByTestId("input-apellido").fill(apellido);
    await page.getByRole("dialog").getByLabel(/dni/i).fill(`DNI${suffix}`);
    await page.getByTestId("check-es-cliente").click();
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    // Create presupuesto with known monto
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    await page.getByTestId("btn-nuevo-presupuesto").click();
    await expect(page.getByRole("dialog")).toBeVisible();

    await page.getByTestId("select-persona").click();
    await page
      .getByRole("option", { name: new RegExp(apellido, "i") })
      .evaluate((el: HTMLElement) => el.click());
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByTestId("input-monto").fill(montoPresupuesto);
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    // When: operator selects presupuesto in payment form
    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();

    const presupuestoOption = page.getByRole("option", { name: new RegExp(apellido, "i") });
    await presupuestoOption.click();

    // Then: saldo pendiente should be displayed before confirming payment
    const saldoDisplay = page.getByTestId("saldo-pendiente-amount");
    await expect(saldoDisplay).toBeVisible({ timeout: 5000 });

    // Saldo should be approximately the presupuesto amount (since no payments yet),
    // displayed as localized currency (e.g. "75000" -> "75.000")
    const saldoText = await saldoDisplay.textContent();
    expect(saldoText).toContain(Number(montoPresupuesto).toLocaleString("es-AR"));
    console.log(`✅ Saldo pendiente displayed: ${saldoText}`);
  });

  test("CU15-SALDO-04: Saldo updates when presupuesto selection changes", async ({ page }) => {
    // Create two presupuestos with different clients and amounts
    const suffix1 = Date.now().toString().slice(-6);
    const apellido1 = `Saldo1-${suffix1}`;
    const monto1 = "50000";

    const suffix2 = (Date.now() + 1000).toString().slice(-6);
    const apellido2 = `Saldo2-${suffix2}`;
    const monto2 = "100000";

    // Create first presupuesto
    await steps.givenUserIsOnPage("/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    await page.getByTestId("input-nombre").fill("Cliente1");
    await page.getByTestId("input-apellido").fill(apellido1);
    await page.getByRole("dialog").getByLabel(/dni/i).fill(`DNI${suffix1}`);
    await page.getByTestId("check-es-cliente").click();
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    await page.getByTestId("btn-nuevo-presupuesto").click();
    await page.getByTestId("select-persona").click();
    await page.getByRole("option", { name: new RegExp(apellido1, "i") }).evaluate((el: HTMLElement) => el.click());
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByTestId("input-monto").fill(monto1);
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    // Create second presupuesto
    await steps.givenUserIsOnPage("/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    await page.getByTestId("input-nombre").fill("Cliente2");
    await page.getByTestId("input-apellido").fill(apellido2);
    await page.getByRole("dialog").getByLabel(/dni/i).fill(`DNI${suffix2}`);
    await page.getByTestId("check-es-cliente").click();
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    await page.getByTestId("btn-nuevo-presupuesto").click();
    await page.getByTestId("select-persona").click();
    await page.getByRole("option", { name: new RegExp(apellido2, "i") }).evaluate((el: HTMLElement) => el.click());
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByTestId("input-monto").fill(monto2);
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    // Now test: select first presupuesto, check saldo, change to second, check saldo updates
    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();

    // Select first presupuesto
    await page.getByRole("option", { name: new RegExp(apellido1, "i") }).click();
    let saldoDisplay = page.getByTestId("saldo-pendiente-amount");
    await expect(saldoDisplay).toBeVisible();
    const saldoText1 = await saldoDisplay.textContent();

    // Change to second presupuesto
    await presupuestoSelector.click();
    await page.getByRole("option", { name: new RegExp(apellido2, "i") }).click();

    // Saldo should update to second presupuesto's amount
    saldoDisplay = page.getByTestId("saldo-pendiente-amount");
    await expect(saldoDisplay).toBeVisible();
    const saldoText2 = await saldoDisplay.textContent();

    // The saldo values should be different (50k vs 100k)
    expect(saldoText1).not.toBe(saldoText2);
    console.log(`✅ Saldo updated when selection changed: ${saldoText1} → ${saldoText2}`);
  });

  test("CU15-SALDO-05 (#848): Submitting a monto over saldo pendiente shows a specific rejection message", async ({ page }) => {
    const suffix = Date.now().toString().slice(-6);
    const apellido = `Overpay${suffix}`;
    const montoPresupuesto = "50000";

    // Create cliente
    await steps.givenUserIsOnPage("/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByTestId("input-nombre").fill("Cliente");
    await page.getByTestId("input-apellido").fill(apellido);
    await page.getByRole("dialog").getByLabel(/dni/i).fill(`DNI${suffix}`);
    await page.getByTestId("check-es-cliente").click();
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    // Create presupuesto with known monto
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    await page.getByTestId("btn-nuevo-presupuesto").click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByTestId("select-persona").click();
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).evaluate((el: HTMLElement) => el.click());
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByTestId("input-monto").fill(montoPresupuesto);
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    // Submit a pago with a monto greater than the saldo pendiente
    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();

    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();
    await page.getByRole("option", { name: new RegExp(apellido, "i") }).click();

    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByRole("dialog").locator('input[type="number"]').fill("999999");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();

    // Then: a specific message about exceeding the saldo is shown, the dialog stays open,
    // and the payment is not persisted
    await expect(page.getByText(/excede el saldo pendiente/i)).toBeVisible({ timeout: 5000 });
    await expect(page.getByRole("dialog")).toBeVisible();
    console.log("✅ Overpayment rejected with specific message");
  });
});
