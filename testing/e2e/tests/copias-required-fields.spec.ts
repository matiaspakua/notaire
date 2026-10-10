/**
 * Copias: required fields (Gherkin style)
 *
 * Covers Issue #655: the copy number and print date are NOT NULL columns, and
 * POST/PUT /api/v1/copia without them answered 500. The API now answers 400,
 * and the dialog keeps Save disabled until both are filled.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("Copias: required number and print date (#655)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/copias");
  });

  test("Given the new copy dialog, When the number or the print date is empty, Then the form cannot be saved", async ({ page }) => {
    await steps.whenUserClicksButton("nueva copia");
    await steps.thenModalIsVisible();

    const save = page.getByRole("dialog").getByRole("button", { name: /^(crear|create)$/i });
    await expect(save).toBeDisabled();

    await steps.whenUserFillsField("Número", "7");
    await expect(save).toBeEnabled();

    await steps.whenUserFillsField("Fecha de Impresión", "");
    await expect(save).toBeDisabled();

    await steps.whenUserFillsField("Fecha de Impresión", "2026-10-09");
    await expect(save).toBeEnabled();
  });
});
