/**
 * CU15/CU47 - Estado de Pago: SIN_PAGOS / PARCIAL / SALDADO
 * Issue #821: Payment form must surface aggregate estado de pago
 *
 * Test suite for the estado-pago-badge shown next to saldo pendiente.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU15/CU47 - Estado de Pago (#821)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("ESTADO-01: New presupuesto without payments shows SIN_PAGOS badge", async ({ page }) => {
    const suffix = Date.now().toString().slice(-6);
    const apellido = `EstadoSin${suffix}`;

    await steps.givenUserIsOnPage("/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByTestId("input-nombre").fill("Cliente");
    await page.getByTestId("input-apellido").fill(apellido);
    await page.getByRole("dialog").getByLabel(/dni/i).fill(`DNI${suffix}`);
    await page.getByTestId("check-es-cliente").click();
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    await page.getByTestId("btn-nuevo-presupuesto").click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByTestId("select-persona").click();
    await page
      .getByRole("option", { name: new RegExp(apellido, "i") })
      .evaluate((el: HTMLElement) => el.click());
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByTestId("input-monto").fill("60000");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();
    const presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();
    await page
      .getByRole("option", { name: new RegExp(apellido, "i") })
      .evaluate((el: HTMLElement) => el.click());

    const badge = page.getByTestId("estado-pago-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/sin pagos/i);
    console.log("✅ SIN_PAGOS badge shown for presupuesto without payments");
  });

  test("ESTADO-02: Presupuesto with partial payment shows PARCIAL badge", async ({ page }) => {
    const suffix = Date.now().toString().slice(-6);
    const apellido = `EstadoParcial${suffix}`;

    await steps.givenUserIsOnPage("/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByTestId("input-nombre").fill("Cliente");
    await page.getByTestId("input-apellido").fill(apellido);
    await page.getByRole("dialog").getByLabel(/dni/i).fill(`DNI${suffix}`);
    await page.getByTestId("check-es-cliente").click();
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    await page.getByTestId("btn-nuevo-presupuesto").click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByTestId("select-persona").click();
    await page
      .getByRole("option", { name: new RegExp(apellido, "i") })
      .evaluate((el: HTMLElement) => el.click());
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByTestId("input-monto").fill("100000");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    // Register a partial payment
    await steps.givenUserIsOnPage("/dashboard/pagos");
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();
    let presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();
    await page
      .getByRole("option", { name: new RegExp(apellido, "i") })
      .evaluate((el: HTMLElement) => el.click());
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-09-01");
    await page.getByRole("dialog").getByLabel(/monto/i).fill("40000");
    await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();

    // Reopen the form for the same presupuesto and check the badge
    await steps.whenUserClicksButton("nuevo pago");
    await expect(page.getByRole("dialog")).toBeVisible();
    presupuestoSelector = page.getByRole("dialog").getByRole("combobox", { name: /presupuesto/i });
    await presupuestoSelector.click();
    await page
      .getByRole("option", { name: new RegExp(apellido, "i") })
      .evaluate((el: HTMLElement) => el.click());

    const badge = page.getByTestId("estado-pago-badge");
    await expect(badge).toBeVisible({ timeout: 5000 });
    await expect(badge).toHaveText(/parcial/i);
    console.log("✅ PARCIAL badge shown after a partial payment");
  });
});
