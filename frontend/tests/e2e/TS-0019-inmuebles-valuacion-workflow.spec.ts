/**
 * CU69 - Gestión de Inmuebles: valuación fiscal (Gherkin style)
 *
 * Covers Issue #879: Inmueble.valuacionFiscal was declared String while the
 * Postgres column is real, so every Inmueble creation failed against the
 * live schema regardless of the value sent.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU69 - Gestión de Inmuebles: valuación fiscal", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/inmuebles");
  });

  test("CU69-GW01: Given on inmuebles page, When create with a numeric valuación fiscal, Then it is saved", async () => {
    const nomenclatura = `NC-E2E-${Date.now()}`;

    await steps.whenUserClicksButton("nuevo inmueble");
    await steps.thenModalIsVisible();

    await steps.whenUserFillsField("Nomenclatura Catastral", nomenclatura);
    await steps.whenUserFillsField("Domicilio", "Calle Falsa 123");
    await steps.whenUserFillsField("Valuación Fiscal", "150000.5");
    await steps.whenUserSubmitsForm();

    await steps.thenShowsSuccessMessage("creado");
    await steps.thenModalIsNotVisible();
    await steps.thenTableContainsText(nomenclatura);
    await steps.thenTableContainsText("150000.5");
  });

  test("CU69-GW02: Given on inmuebles page, When create leaving valuación fiscal empty, Then it still succeeds", async () => {
    const nomenclatura = `NC-E2E-${Date.now()}-EMPTY`;

    await steps.whenUserClicksButton("nuevo inmueble");
    await steps.thenModalIsVisible();

    await steps.whenUserFillsField("Nomenclatura Catastral", nomenclatura);
    await steps.whenUserFillsField("Domicilio", "Calle Falsa 456");
    await steps.whenUserSubmitsForm();

    await steps.thenShowsSuccessMessage("creado");
    await steps.thenModalIsNotVisible();
    await steps.thenTableContainsText(nomenclatura);
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`CU69-GW03: the inmueble form is usable at ${viewport.label}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });

      await steps.whenUserClicksButton("nuevo inmueble");
      await steps.thenModalIsVisible();
      await steps.thenFormHasField("Valuación Fiscal");
      await steps.thenHasNoHorizontalOverflow();
    });
  }
});
