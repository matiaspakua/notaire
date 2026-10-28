/**
 * Copies: required fields (Gherkin style)
 *
 * Covers Issue #655 / Wave 8 #1961: the copy number and print date are required.
 * Save stays enabled; empty required fields surface FormField errors and block the mutation.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("Copies: required number and print date (#655)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/copies");
  });

  test("Given the new copy dialog, When the number or the print date is empty, Then the form cannot be saved", async ({ page }) => {
    await steps.whenUserClicksButton("nueva copia");
    await steps.thenModalIsVisible();

    const dialog = page.getByRole("dialog");
    const save = dialog.getByTestId("btn-save-copy");
    await expect(save).toBeEnabled();

    await dialog.getByTestId("input-number-copy").fill("");
    await dialog.locator('input[type="date"]').first().fill("");
    await save.click();
    await expect(dialog.getByRole("alert").first()).toBeVisible();

    await dialog.getByTestId("input-number-copy").fill("7");
    await dialog.locator('input[type="date"]').first().fill("2026-10-09");
    await expect(dialog.getByRole("alert")).toHaveCount(0);
  });
});
