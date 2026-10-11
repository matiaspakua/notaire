/**
 * CU69 - Property management: tax valuation (Gherkin style)
 *
 * Covers Issue #879: Property.valuacionFiscal was declared String while the
 * Postgres column is real, so every Property creation failed against the
 * live schema regardless of the value sent.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU69 - Property management: fiscal valuation", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/properties");
  });

  test("CU69-GW01: Given the properties page, When a property is created with a numeric fiscal valuation, Then it is saved", async () => {
    const cadastralDesignation = `NC-E2E-${Date.now()}`;

    await steps.whenUserClicksButton("nuevo inmueble");
    await steps.thenModalIsVisible();

    await steps.whenUserFillsField("Nomenclatura Catastral", cadastralDesignation);
    await steps.whenUserFillsField("Domicilio", "Fake Street 123");
    await steps.whenUserFillsField("Valuación Fiscal", "150000.5");
    await steps.whenUserSubmitsForm();

    await steps.thenShowsSuccessMessage("creado");
    await steps.thenModalIsNotVisible();
    await steps.thenTableContainsText(cadastralDesignation);
    await steps.thenTableContainsText("150000.5");
  });

  test("CU69-GW02: Given the properties page, When a property is created with an empty fiscal valuation, Then it still succeeds", async () => {
    const cadastralDesignation = `NC-E2E-${Date.now()}-EMPTY`;

    await steps.whenUserClicksButton("nuevo inmueble");
    await steps.thenModalIsVisible();

    await steps.whenUserFillsField("Nomenclatura Catastral", cadastralDesignation);
    await steps.whenUserFillsField("Domicilio", "Fake Street 456");
    await steps.whenUserSubmitsForm();

    await steps.thenShowsSuccessMessage("creado");
    await steps.thenModalIsNotVisible();
    await steps.thenTableContainsText(cadastralDesignation);
  });

  test("CU69-GW04: Given on the properties page, When the cadastral code is blank, Then the form cannot be saved (#655)", async ({ page }) => {
    await steps.whenUserClicksButton("nuevo inmueble");
    await steps.thenModalIsVisible();

    const save = page.getByRole("dialog").getByRole("button", { name: /^(crear|create)$/i });
    await steps.whenUserFillsField("Domicilio", "Street without designation");
    await expect(save).toBeDisabled();

    await steps.whenUserFillsField("Nomenclatura Catastral", "   ");
    await expect(save).toBeDisabled();

    await steps.whenUserFillsField("Nomenclatura Catastral", `NC-E2E-${Date.now()}-REQ`);
    await expect(save).toBeEnabled();
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`CU69-GW03: the property form is usable at ${viewport.label}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });

      await steps.whenUserClicksButton("nuevo inmueble");
      await steps.thenModalIsVisible();
      await steps.thenFormHasField("Valuación Fiscal");
      await steps.thenHasNoHorizontalOverflow();
    });
  }
});
