/**
 * CU03 - Listar documentos y certificados necesarios (Gherkin style)
 * CU04 - Registrar documentación cliente
 * CU07 - Generar testimonio
 * CU08 - Verificar Testimonio
 * CU09 - Registrar deudas documentos de Cliente
 * CU10 - Registrar movimientos documentación de entidades externas
 * CU11 - Ingresar para inscripción
 * CU12 - Retirar testimonio
 *
 * NOTE: CU04, CU07-CU12 tests below are skipped because the corresponding UI
 * buttons and pages have not been implemented yet. The backend endpoints for
 * documentos/testimonios exist but the frontend flow is pending.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU03 - Listar documentos y certificados necesarios", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/documentos-necesarios");
  });

  test("CU03-GW01: Given on documentos necesarios, When select trámite, Then shows results section", async ({
    page,
  }) => {
    await steps.thenPageHasHeading("Documentos Necesarios");

    await page.getByTestId("select-tramite").click();
    await page.getByRole("option").first().click();

    await expect(page.getByText(/Documentos necesarios para/i)).toBeVisible();
    const emptyState = page.getByTestId("empty-state");
    const table = page.getByRole("table");
    await expect(emptyState.or(table)).toBeVisible();
  });
});

test.describe("CU04 - Registrar documentación cliente", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/personas");
  });

  test.skip("CU04-GW01: Given on personas, When click documentación, Then modal opens", async () => {
    // Skipped: no "documentación" button on the personas page.
  });
});

test.describe("CU07 - Generar testimonio", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test.skip("CU07-GW01: Given escritura exists, When click generar testimonio, Then generates", async () => {
    // Skipped: no "generar testimonio" button on the escrituras page.
  });
});

test.describe("CU08 - Verificar Testimonio", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test.skip("CU08-GW01: Given testimonio exists, When click ver, Then shows testimonio", async () => {
    // Skipped: no "ver testimonio" button on the escrituras page.
  });
});

test.describe("CU11 - Ingresar para inscripción", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test.skip("CU11-GW01: Given gestión exists, When click inscripción, Then modal opens", async () => {
    // Skipped: no "inscripción" button on the gestiones page.
  });
});

test.describe("CU12 - Retirar testimonio", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test.skip("CU12-GW01: Given testimonio ready, When click retirar, Then confirms", async () => {
    // Skipped: no "retirar testimonio" button on the escrituras page.
  });
});
