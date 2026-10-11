/**
 * CU22 - Register a substitution (Gherkin style)
 * CU59 - Consultar Suplencias
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU22 - Register a substitution", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenDashboardIsLoaded();
    // Discovery via primary navigation (#1058) — not a hard deep link.
    await steps.whenUserOpensSidebarNav("suplencias");
  });

  test("CU22-GW01: Given the substitutions page, When new substitution is clicked, Then the modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Suplencias");

    // When
    await steps.whenUserClicksButton("nueva suplencia");

    // Then — modal uses IDs for notary/suplente (numeric inputs), and date range (Desde/Hasta)
    await steps.thenModalIsVisible("Nueva suplencia");
    await expect(steps.page.getByTestId("input-notary-id")).toBeVisible();
    await expect(steps.page.getByTestId("input-substitute-id")).toBeVisible();
    await expect(steps.page.getByTestId("input-start-date")).toBeVisible();
    await expect(steps.page.getByTestId("input-end-date")).toBeVisible();
  });

  test("CU22-GW02: Given the form is open, When it is filled and submitted, Then the substitution is created", async () => {
    // Given
    await steps.whenUserClicksButton("nueva suplencia");
    await steps.thenModalIsVisible();

    // When — fill notary ID, suplente ID, and date range via testids
    await steps.page.getByTestId("input-notary-id").fill("1");
    await steps.page.getByTestId("input-substitute-id").fill("1");
    await steps.page.getByTestId("input-start-date").fill("2026-05-01");
    await steps.page.getByTestId("input-end-date").fill("2026-05-31");
    await steps.page.getByTestId("btn-save-substitution").click();

    // Then — wait for modal to close, then verify success and table
    await steps.thenShowsSuccessMessage("registrada");
    await expect(steps.page.getByRole("dialog")).not.toBeVisible({ timeout: 5000 });
    await steps.thenTableIsVisible();
  });
});

test.describe("CU59 - Look up substitutions", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenDashboardIsLoaded();
    await steps.whenUserOpensSidebarNav("suplencias");
  });

  test.skip("CU59-GW01 (#1146): Given substitutions, When filtered by notary, Then the filtered list is shown", async () => {
    // Skipped (#1146): substitutions page has no filter dropdown by notary.
  });

  test.skip("CU59-GW02 (#1146): Given a substitution exists, When view details is clicked, Then the details are shown", async () => {
    // Skipped (#1146): substitutions table has no "ver" button — only edit/delete icons.
  });
});
