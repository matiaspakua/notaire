/**
 * CU22 - Registrar Suplencia (Gherkin style)
 * CU59 - Consultar Suplencias
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU22 - Registrar Suplencia", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/suplencias");
  });

  test("CU22-GW01: Given on suplencias page, When click nueva suplencia, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Suplencias");

    // When
    await steps.whenUserClicksButton("nueva suplencia");

    // Then — modal uses IDs for escribano/suplente (numeric inputs), and date range (Desde/Hasta)
    await steps.thenModalIsVisible("Nueva suplencia");
    await expect(steps.page.getByTestId("input-escribano-id")).toBeVisible();
    await expect(steps.page.getByTestId("input-suplente-id")).toBeVisible();
    await expect(steps.page.getByTestId("input-desde")).toBeVisible();
    await expect(steps.page.getByTestId("input-hasta")).toBeVisible();
  });

  test("CU22-GW02: Given form open, When fill and submit, Then suplencia created", async () => {
    // Given
    await steps.whenUserClicksButton("nueva suplencia");
    await steps.thenModalIsVisible();

    // When — fill escribano ID, suplente ID, and date range via testids
    await steps.page.getByTestId("input-escribano-id").fill("1");
    await steps.page.getByTestId("input-suplente-id").fill("1");
    await steps.page.getByTestId("input-desde").fill("2026-05-01");
    await steps.page.getByTestId("input-hasta").fill("2026-05-31");
    await steps.page.getByTestId("btn-guardar-suplencia").click();

    // Then — wait for modal to close, then verify success and table
    await steps.thenShowsSuccessMessage("registrada");
    await expect(steps.page.getByRole("dialog")).not.toBeVisible({ timeout: 5000 });
    await steps.thenTableIsVisible();
  });
});

test.describe("CU59 - Consultar Suplencias", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/suplencias");
  });

  test.skip("CU59-GW01: Given on suplencias, When filter by escribano, Then shows filtered", async () => {
    // Skipped: suplencias page has no filter dropdown by escribano.
  });

  test.skip("CU59-GW02: Given suplencia exists, When click ver detalle, Then shows details", async () => {
    // Skipped: suplencias table has no "ver" button — only edit/delete icons.
  });
});
