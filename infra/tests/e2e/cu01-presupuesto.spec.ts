/**
 * CU01 — Preparar Presupuesto
 *
 * Full-stack E2E: UI → API → DB persistence verification.
 * Creates a new presupuesto through the UI and verifies persistence.
 *
 * References: CU39, CU45, CU49, CU55, CU60
 * GitHub: #154
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";
import { TestData, resetTestDataCounter } from "./helpers/test-data";
import { verifyPresupuestoCreado, eliminarPresupuesto } from "./helpers/api-helpers";

test.describe("CU01 — Preparar Presupuesto", () => {
  let steps: GherkinSteps;
  let data: ReturnType<typeof TestData.presupuesto>;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    resetTestDataCounter();
    data = TestData.presupuesto();
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
  });

  test("GW01: Modal opens with all required form fields", async () => {
    // Given
    await steps.givenModuleIsVisible("Presupuestos");

    // When: Click "Nuevo presupuesto"
    await steps.openNewRecordModal(/nuevo presupuesto/i);

    // Then: Modal shows required fields
    await steps.thenModalIsVisible("Nuevo presupuesto");
    await steps.thenFormHasField("Fecha");
    await steps.thenFormHasField("Monto");
  });

  test("GW02: Create presupuesto and verify persistence", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo presupuesto/i);

    // When: Fill required fields and submit
    await steps.whenUserFillsField("Fecha", data.fecha);
    await steps.whenUserFillsField("Monto", String(data.monto));
    await steps.whenUserSubmitsForm();

    // Then: Success message appears
    await steps.thenShowsSuccessMessage("creado");
    await steps.thenTableIsVisible();

    // And: Verify data persisted via API
    const id = await verifyPresupuestoCreado(steps.page, data.monto);
    expect(id).toBeGreaterThan(0);

    // Cleanup: Remove test data
    await eliminarPresupuesto(steps.page, id);
  });

  test("GW03: Cancel button closes modal", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo presupuesto/i);

    // When
    await steps.whenUserCancelsForm();

    // Then
    await steps.thenModalIsNotVisible();
  });

  test("GW04: Table shows existing presupuestos", async () => {
    // Then
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible("Fecha");
    await steps.thenElementIsVisible("Monto");
    await steps.thenElementIsVisible("Estado");
  });
});
