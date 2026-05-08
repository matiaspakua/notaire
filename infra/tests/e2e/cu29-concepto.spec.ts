/**
 * CU29 — Ingresar nuevo concepto
 *
 * Full-stack E2E: UI → API → DB persistence verification.
 * Requires: Login, Admin hub access
 *
 * References: RF 2.1, CU66
 * GitHub: #195
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";
import { TestData, resetTestDataCounter } from "./helpers/test-data";
import { verifyConceptoCreado, eliminarConcepto } from "./helpers/api-helpers";

test.describe("CU29 — Ingresar nuevo concepto", () => {
  let steps: GherkinSteps;
  let data: ReturnType<typeof TestData.concepto>;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    resetTestDataCounter();
    data = TestData.concepto();
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/conceptos");
  });

  test("GW01: Modal opens with all required form fields", async () => {
    // Given
    await steps.givenModuleIsVisible("Conceptos");

    // When: Click "Nuevo concepto" button
    await steps.openNewRecordModal(/nuevo concepto/i);

    // Then: Modal shows required fields
    await steps.thenModalIsVisible("Nuevo concepto");
    await steps.thenFormHasField("Nombre");
    await steps.thenFormHasField("Valor base");
  });

  test("GW02: Create concepto with all fields and verify persistence", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo concepto/i);

    // When: Fill all form fields and submit
    await steps.whenUserFillsField("Nombre", data.nombre);
    await steps.whenUserFillsField("Descripción", data.descripcion);
    await steps.whenUserFillsField("Valor base", String(data.valor));
    await steps.whenUserSubmitsForm();

    // Then: Success message appears
    await steps.thenShowsSuccessMessage("creado");
    await steps.thenTableIsVisible();

    // And: Verify data persisted via API
    const id = await verifyConceptoCreado(steps.page, data.nombre);
    expect(id).toBeGreaterThan(0);

    // Cleanup: Remove test data
    await eliminarConcepto(steps.page, id);
  });

  test("GW03: Cancel form closes modal without creating", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo concepto/i);

    // When: Cancel
    await steps.whenUserCancelsForm();

    // Then: Modal closes
    await steps.thenModalIsNotVisible();
  });

  test("GW04: Table shows existing conceptos", async () => {
    // Then
    await steps.thenTableIsVisible();
  });
});
