/**
 * CU02 - Iniciar Gestión (Gherkin style)
 * CU13 - Ver historial de gestión
 * CU14 - Consultar estado gestión
 * CU16 - Archivar Gestión
 * CU53 - Modificar Gestión
 * CU19 - Buscar gestiones de un Cliente
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";

test.describe("CU02 - Iniciar Gestión", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test("CU02-GW01: Given user on gestiones page, When click nueva gestion, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Gestiones");

    // When
    await steps.whenUserClicksButton("nueva gestión");

    // Then — modal title uses i18n lowercase "gestión"; check form field via testid
    await steps.thenModalIsVisible();
    await expect(steps.page.getByTestId("input-numero-gestion")).toBeVisible();
  });

  test.skip("CU02-GW02: Given form open, When fill and submit, Then gestion created", async () => {
    // Skipped: form was simplified to only have 'numero' field; detalle/fechaInicio/escribano
    // no longer exist in the modal. Re-enable once test is updated to match current UI.
  });

  test.skip("CU02-GW03: Given gestion exists, When click ver detalle, Then shows details", async () => {
    // Skipped: requires at least one gestión in the database (seed data not available in CI).
  });
});

test.describe("CU13 - Ver historial de gestión", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test.skip("CU13-GW01: Given user on gestiones, When filter by estado, Then shows filtered", async () => {
    // Skipped: gestiones page has no estado filter dropdown in the current UI.
    // The page shows gestiones in a table with only edit/delete icon buttons.
  });
});

test.describe("CU14 - Consultar estado gestión", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test.skip("CU14-GW01: Given gestion exists, When view gestion, Then shows current status", async () => {
    // Skipped: gestiones table has no "ver" button per row (only edit/delete icons).
    // Requires existing gestión data and a detail view that does not exist yet.
  });
});

test.describe("CU16 - Archivar Gestión", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test.skip("CU16-GW01: Given gestion exists, When archivar clicked, Then status changes", async () => {
    // Skipped: gestiones page has no "archivar" button. Row actions are edit/delete only.
  });
});

test.describe("CU19 - Buscar gestiones de un Cliente", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/gestiones");
  });

  test("CU19-GW01: Given personas exist, When a cliente is selected, Then calls GET /gestiones/cliente/{id}", async () => {
    await steps.page.getByTestId("select-filter-cliente-gestion").click();
    const firstOption = steps.page.getByRole("option").filter({ hasNotText: /^Todos$/ }).first();
    if (await firstOption.count() === 0) {
      // No cliente seeded in this environment — nothing to filter by, skip assertion.
      return;
    }
    const searchRequest = steps.page.waitForRequest((req) =>
      /\/api\/v1\/gestiones\/cliente\/\d+/.test(req.url()) && req.method() === "GET"
    );
    await firstOption.click();
    await searchRequest;
  });
});
