/**
 * TS-0091 - Items - discounts and surcharges (CU45/CU71)
 *
 * Covers: CU45 (update a budget), CU71 (manage items)
 * Test Level: E2E UI/UX
 *
 * Workflow:
 *   - Open the item form and select tipo DESCUENTO/RECARGO
 *   - Motivo becomes required and is enforced client-side
 *   - Query the descuentos/recargos report for a budget
 *
 * Reference: docs/openspec/changes/descuentos-recargos-budget
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("Items - Descuentos y Recargos", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/items");
  });

  test("CU71-GW01: Given a new item, When the type is Descuento, Then the reason field is required", async ({ page }) => {
    await page.getByTestId("btn-new-item").click();
    await steps.thenModalIsVisible();

    await expect(page.getByTestId("input-reason")).not.toBeVisible();

    await page.getByTestId("select-item-type").click();
    await page.getByRole("option", { name: "Descuento" }).click();

    await expect(page.getByTestId("input-reason")).toBeVisible();
  });

  test("CU71-GW02: Given tipo Descuento, When saving without motivo, Then shows validation error", async ({ page }) => {
    await page.getByTestId("btn-new-item").click();
    await page.getByTestId("select-item-type").click();
    await page.getByRole("option", { name: "Recargo" }).click();

    await page.getByTestId("btn-save-item").click();

    await steps.thenShowsErrorMessage("motivo es obligatorio");
    await steps.thenModalIsVisible();
  });

  test("CU45-GW01: Given a budget ID, When discounts and surcharges are queried, Then the report table renders", async ({ page }) => {
    await page.getByTestId("input-report-budget-id").fill("999999");
    await page.getByTestId("btn-query-report").click();

    await expect(page.getByRole("table").last()).toBeVisible();
  });
});
