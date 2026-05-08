/**
 * CU26 — Ingresar nuevo tipo de trámite
 *
 * Full-stack E2E: UI → API → DB persistence verification.
 * Creates a new tipo de trámite through UI and verifies persistence.
 *
 * References: CU57, CU64
 * GitHub: #198
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";
import { TestData, resetTestDataCounter } from "./helpers/test-data";
import { verifyTipoTramiteCreado } from "./helpers/api-helpers";

test.describe("CU26 — Ingresar nuevo tipo de trámite", () => {
  let steps: GherkinSteps;
  let data: ReturnType<typeof TestData.tipoTramite>;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    resetTestDataCounter();
    data = TestData.tipoTramite();
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/tramites");
  });

  test("GW01: Modal opens with all required fields", async () => {
    // Given
    await steps.givenModuleIsVisible("Tipos de Trámite");

    // When
    await steps.openNewRecordModal(/nuevo tipo/i);

    // Then
    await steps.thenModalIsVisible("Nuevo Tipo de Trámite");
    await steps.thenFormHasField("Nombre");
  });

  test("GW02: Create tipo de trámite and verify persistence", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo tipo/i);

    // When: Fill fields and submit
    await steps.whenUserFillsField("Nombre", data.nombre);
    await steps.whenUserFillsField("Descripción", data.descripcion);
    await steps.whenUserSubmitsForm();

    // Then: UI shows success
    await steps.thenShowsSuccessMessage("creado");
    await steps.thenTableIsVisible();

    // And: Verify via API
    const id = await verifyTipoTramiteCreado(steps.page, data.nombre);
    expect(id).toBeGreaterThan(0);
  });

  test("GW03: Cancel button closes modal", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo tipo/i);

    // When
    await steps.whenUserCancelsForm();

    // Then
    await steps.thenModalIsNotVisible();
  });

  test("GW04: Table shows existing tipos de trámite", async () => {
    // Then
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible("Nombre");
  });
});
