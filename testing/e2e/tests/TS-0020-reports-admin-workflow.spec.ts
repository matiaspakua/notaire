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
import { apiPost, createDocumentType, createProcedureType, seedProtocolReportPeriod, uniqueId } from "./setup/api-helpers";
import { assertPdfDownload } from "./setup/pdf-helpers";

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

  test("CU24-GW01: Given a recorded period, When the index book is downloaded, Then the PDF lists the person", async ({
    page,
  }) => {
    const seeded = await seedProtocolReportPeriod(page);
    await page.getByTestId("input-report-index-year").fill(String(seeded.year));
    const downloadPromise = page.waitForEvent("download");
    await page.getByTestId("btn-download-index-book").click();
    const download = await downloadPromise;
    await assertPdfDownload(download, /libro_indice_.*\.pdf/, [seeded.lastName, seeded.procedureName, "Index book"]);
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

  // #1556: the home dashboard alerts about documents due within the default 30-day window.
  for (const vp of [{ name: "desktop", width: 1440, height: 900 }, { name: "390px", width: 390, height: 844 }]) {
    test(`CU42-GW01 (#1556, ${vp.name}): Given a document due soon, When on the dashboard, Then an expirations alert links to the list`, async ({ page }) => {
      await page.setViewportSize({ width: vp.width, height: vp.height });
      // Given a document due in 2 days (seeded as in TS-0097).
      const documentType = await createDocumentType(page, { expires: true, dueDays: 2 });
      expect(documentType.ok, documentType.error ?? "createDocumentType failed").toBe(true);
      const procedureType = await createProcedureType(page);
      expect(procedureType.ok, procedureType.error ?? "createProcedureType failed").toBe(true);
      const procedure = await apiPost<{ idProcedure: number }>(page, "/procedures", {
        idProcedureType: procedureType.data!.idProcedureType,
      });
      expect(procedure.ok, procedure.error ?? "create procedure failed").toBe(true);
      const name = `Alert E2E ${uniqueId()}`;
      const seeded = await apiPost<{ idSubmittedDocument: number }>(page, "/submitted-documents", {
        typeId: documentType.data!.idDocumentType,
        procedureId: procedure.data!.idProcedure,
        date: new Date().toISOString().split("T")[0],
        delivered: false,
        name,
      });
      expect(seeded.ok, seeded.error ?? "submitted document failed").toBe(true);

      // When the dashboard home loads
      await steps.givenUserIsOnPage("/dashboard");
      const alert = page.getByTestId("expirations-alert");
      // Then the alert shows a count and up to three items, without horizontal overflow
      await expect(alert).toBeVisible({ timeout: 15000 });
      await expect(alert).toContainText(/vencen? en los próximos 30 días/i);
      const items = alert.getByTestId("expirations-alert-item");
      expect(await items.count()).toBeGreaterThan(0);
      expect(await items.count()).toBeLessThanOrEqual(3);
      const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
      expect(overflow).toBeLessThanOrEqual(1);

      // And its link opens the full list, where the seeded document is listed
      await alert.getByTestId("expirations-alert-link").click();
      await expect(page).toHaveURL(/\/dashboard\/upcoming-expirations$/);
      await expect(page.getByText(name).first()).toBeVisible({ timeout: 15000 });
    });
  }
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
