/**
 * Playwright E2E — calendar dates show the stored day in any browser zone (#1339).
 * RNF-08, RF-16/RF-17: a payment stored on 2026-08-07 shows 7/8/2026 whether
 * the browser runs in Buenos Aires, Madrid or UTC. The backend serializes
 * date-only fields as midnight in its own JVM zone (e.g. 2026-08-06T22:00Z).
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { createPayment, createPerson, createBudget } from "./setup/api-helpers";

async function seedPayment(page: Page): Promise<number> {
  const persona = await createPerson(page);
  expect(persona.ok, persona.error ?? "createPerson failed").toBe(true);
  const budget = await createBudget(page, persona.data!.personId, undefined, {
    date: "2026-08-07",
    propertyAmount: 10000, // a payment may not exceed the budget balance
  });
  expect(budget.ok, budget.error ?? "createBudget failed").toBe(true);
  const pago = await createPayment(page, budget.data!.idBudget, { date: "2026-08-07" });
  expect(pago.ok, pago.error ?? "createPayment failed").toBe(true);
  return pago.data!.idPayment;
}

for (const timezoneId of ["America/Argentina/Buenos_Aires", "Europe/Madrid", "UTC"]) {
  test.describe(`Calendar dates in ${timezoneId} (#1339)`, () => {
    test.use({ timezoneId });

    test("the payments list shows the stored payment day", async ({ page }) => {
      await establishAdminBrowserSession(page);
      const id = await seedPayment(page);

      await page.goto("/dashboard/payments");
      await page.waitForLoadState("networkidle");
      const row = page
        .getByRole("row")
        .filter({ has: page.getByRole("cell", { name: String(id), exact: true }) });
      await expect(row).toHaveCount(1, { timeout: 15000 });
      await expect(row).toContainText("7/8/2026");
    });
  });
}
