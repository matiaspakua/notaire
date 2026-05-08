/**
 * CU27 — Ingresar nuevo tipo de documento
 *
 * Full-stack E2E: UI → API → DB persistence verification.
 * Note: The "Tipos de Documento" admin page is currently read-only (no create button).
 * This test verifies the page loads, the table displays data, and the API
 * endpoint works correctly for CRUD operations.
 *
 * References: RF 2.1, CU65
 * GitHub: #194
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";
import { TestData, resetTestDataCounter } from "./helpers/test-data";
import { apiPost, apiGet } from "./helpers/api-helpers";

test.describe("CU27 — Ingresar nuevo tipo de documento", () => {
  let steps: GherkinSteps;
  let data: ReturnType<typeof TestData.tipoDocumento>;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    resetTestDataCounter();
    data = TestData.tipoDocumento();
    await steps.givenUserIsLoggedIn();
  });

  test("GW01: Page loads with table of existing tipos de documento", async () => {
    // Given
    await steps.givenUserIsOnPage("/dashboard/administracion/documentos");

    // Then
    await steps.thenPageHasHeading("Tipos de Documento");
    await steps.thenTableIsVisible();
  });

  test("GW02: Create tipo de documento via API and verify it appears in UI", async () => {
    // Given: Create via API
    const { ok, data: created } = await apiPost<{ idTipoDeDocumento: number }>(
      steps.page,
      "/tipo-de-documento",
      { nombre: data.nombre }
    );
    expect(ok, "POST /api/v1/tipo-de-documento failed").toBe(true);

    // Then: Verify via API it was persisted
    const { data: allDocs } = await apiGet(steps.page, "/tipo-de-documento");
    const found = (allDocs as Array<{ nombre?: string }>)?.find(
      (d) => d.nombre === data.nombre
    );
    expect(found, "Created tipo de documento not found in GET response").toBeDefined();

    // And: Navigate to UI and verify it appears in the table
    await steps.givenUserIsOnPage("/dashboard/administracion/documentos");
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible(data.nombre);
  });

  test("GW03: Admin hub links to documentos page", async () => {
    // Given
    await steps.givenUserIsOnPage("/dashboard/administracion");

    // Then: Documentos link is visible
    await steps.thenElementIsVisible("Documentos");
  });
});
