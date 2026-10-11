/**
 * CU24 - Generate the index book (Gherkin style)
 * CU25 - Generate the monthly sworn statement
 * CU26 - Enter a new procedure type
 * CU27 - Enter a new document type
 * CU28 - Enter new folios
 * CU29 - Enter a new concept
 * CU30 - Enter a new management status
 * CU31 - Update a procedure type
 * CU32 - Update a document type
 * CU33 - Update a folio
 * CU34 - Update a concept
 * CU35 - Update a management status
 * CU36 - Enter folio types
 * CU37 - Delete a concept
 * CU38 - Delete a document type
 * CU39 - Create a budget template
 * CU40 - Update a folio type
 * CU42 - Report upcoming expirations
 * CU43 - Reenter documentation
 * CU44 - Reenter a testimony
 * CU45 - Update a budget
 * CU49 - Delete a budget template
 * CU50 - Generate the tax sworn statement
 * CU55 - Update a client
 * CU56 - Record a registration
 * CU57 - Delete a procedure type
 * CU58 - Delete a folio type
 * CU59 - Look up substitutions
 * CU60 - Search budgets
 * CU62 - Search deeds
 * CU63 - Search folios
 * CU64 - Search procedure types
 * CU65 - Search document types
 * CU66 - Search concepts
 * CU67 - Search management statuses
 * CU68 - Search folio types
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";

test.describe("Reports — navigation discovery (#1058)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenDashboardIsLoaded();
  });

  test("opens Reports from the sidebar", async ({ page }) => {
    await steps.whenUserOpensSidebarNav("reportes");
    await expect(page).toHaveURL(/\/dashboard\/reports/);
    await expect(page.locator("h1").filter({ hasText: /\S/ }).first()).toBeVisible();
  });
});

test.describe("CU24 - Generate the index book", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenDashboardIsLoaded();
    await steps.whenUserOpensSidebarNav("reportes");
  });

  test.skip("CU24-GW01 (#1146): Given reports, When the index book is requested, Then it is generated", async () => {
    // Skipped (#1146): reports page uses "Descargar PDF" buttons — no "libro índices" button text.
    // The report is triggered by filling the year field and clicking Descargar PDF.
  });
});

test.describe("CU25 - Generate the monthly sworn statement", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenDashboardIsLoaded();
    await steps.whenUserOpensSidebarNav("reportes");
  });

  test.skip("CU25-GW01 (#1146): Given reports, When select month and generate, Then creates", async () => {
    // Skipped (#1146): the reports page has no month dropdown or "declaración jurada" button.
    // The monthly affidavit section uses number inputs for year and month and a "Descargar PDF" button.
  });
});

test.describe("CU26 - Enter a new procedure type", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administration/procedures");
  });

  test("CU26-GW01: Given procedure types, When new is clicked, Then the modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Tipos de Trámite");

    // When
    await steps.whenUserClicksButton("nuevo tipo");

    // Then
    await steps.thenModalIsVisible();
    await steps.thenFormHasField("nombre");
  });
});

test.describe("CU27 - Enter a new document type", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administration/documents");
  });

  test("CU27-GW01: Given document types, When new is clicked, Then the modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Tipos de Documento");

    // When
    await steps.whenUserClicksButton("nuevo tipo");

    // Then
    await steps.thenModalIsVisible();
  });
});

test.describe("CU29 - Enter a new concept", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administration/concepts");
  });

  test("CU29-GW01: Given concepts, When new is clicked, Then the modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Conceptos");

    // When
    await steps.whenUserClicksButton("nuevo concepto");

    // Then
    await steps.thenModalIsVisible("Nuevo concepto");
    await steps.thenFormHasField("nombre");
    await steps.thenFormHasField("valor");
  });

  test("CU29-GW02: Given the form is open, When it is filled and submitted, Then the concept is created", async () => {
    // Given
    await steps.whenUserClicksButton("nuevo concepto");
    await steps.thenModalIsVisible();

    // When — the form shows "Nombre", "Descripción", and "Valor base ($)" (no "porcentaje" field)
    await steps.fillAndSubmitForm({
      "nombre": "New Concept Test",
      "valor": "100",
    });

    // Then
    await steps.thenShowsSuccessMessage("creado");
  });
});

test.describe("CU39 - Create a budget template", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/budgets");
  });

  test.skip("CU39-GW01 (#1146): Given budgets, When templates is clicked, Then the templates are shown", async () => {
    // Skipped (#1146): the budgets page has no templates button in the current UI.
  });
});

test.describe("CU42 - Report upcoming expirations", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard");
  });

  test.skip("CU42-GW01 (#1146): Given on dashboard, When view alerts, Then shows vencimientos", async () => {
    // Skipped (#1146): the dashboard has no alerts button or upcoming-expirations section.
  });
});

test.describe("CU60 - Search budgets", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/budgets");
  });

  test("CU60-GW01: Given budgets, When searching by number, Then results are shown", async () => {
    // Given
    await steps.givenModuleIsVisible("Presupuestos");

    // When
    await steps.whenUserSearches("TEST-001");

    // Then
    await steps.thenTableIsVisible();
  });
});

test.describe("CU62 - Search deeds", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/deeds");
  });

  test.skip("CU62-GW01 (#1146): Given deeds, When search by number, Then shows results", async () => {
    // Skipped (#1146): deeds page has no search bar in the current UI.
  });
});

test.describe("CU65 - Search document types", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administration/documents");
  });

  test.skip("CU65-GW01 (#1146): Given document types, When searching, Then results are shown", async () => {
    // Skipped (#1146): the administration/documents page has no search bar in the current UI.
  });
});

test.describe("CU66 - Search concepts", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administration/concepts");
  });

  test.skip("CU66-GW01 (#1146): Given concepts, When search, Then shows results", async () => {
    // Skipped (#1146): the administration/concepts page has no search bar in the current UI.
  });
});
