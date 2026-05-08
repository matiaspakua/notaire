/**
 * CU28 — Ingresar nuevos folios
 *
 * Full-stack E2E: UI → API → DB persistence verification.
 * Creates a new folio through the UI and verifies persistence.
 *
 * References: CU40, CU58, CU63, CU68
 * GitHub: #197
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";
import { TestData, resetTestDataCounter } from "./helpers/test-data";
import { apiGet } from "./helpers/api-helpers";

test.describe("CU28 — Ingresar nuevos folios", () => {
  let steps: GherkinSteps;
  let data: ReturnType<typeof TestData.folio>;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    resetTestDataCounter();
    data = TestData.folio();
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/folios");
  });

  test("GW01: Modal opens with required fields", async () => {
    // Given
    await steps.givenModuleIsVisible("Folios");

    // When
    await steps.openNewRecordModal(/nuevo folio/i);

    // Then
    await steps.thenModalIsVisible("Nuevo Folio");
    await steps.thenFormHasField("Número");
    await steps.thenFormHasField("Año");
  });

  test("GW02: Create folio and verify persistence", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo folio/i);

    // When: Fill fields and submit
    await steps.whenUserFillsField("Número", String(data.numero));
    await steps.whenUserFillsField("Año", String(data.anio));
    await steps.whenUserSubmitsForm();

    // Then: UI shows success
    await steps.thenShowsSuccessMessage("creado");
    await steps.thenTableIsVisible();

    // And: Verify via API
    const { data: folios } = await apiGet(steps.page, "/folio");
    const found = (folios as Array<{ numero?: number; idFolio?: number }>)?.find(
      (f) => f.numero === data.numero
    );
    expect(found, `Folio #${data.numero} not found in API response`).toBeDefined();
    expect(found!.idFolio).toBeGreaterThan(0);
  });

  test("GW03: Cancel closes modal", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo folio/i);

    // When
    await steps.whenUserCancelsForm();

    // Then
    await steps.thenModalIsNotVisible();
  });

  test("GW04: Table shows column headers correctly", async () => {
    // Then
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible("Número");
    await steps.thenElementIsVisible("Estado");
  });
});
