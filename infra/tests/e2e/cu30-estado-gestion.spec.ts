/**
 * CU30 — Ingresar nuevo estado de Gestión
 *
 * Full-stack E2E: UI → API → DB persistence verification.
 * Creates a new estado de gestión through the UI and verifies
 * it persists via the API.
 *
 * References: CU67
 * GitHub: #196
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";
import { TestData, resetTestDataCounter } from "./helpers/test-data";
import { verifyEstadoGestionCreado } from "./helpers/api-helpers";

test.describe("CU30 — Ingresar nuevo estado de Gestión", () => {
  let steps: GherkinSteps;
  let data: ReturnType<typeof TestData.estadoGestion>;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    resetTestDataCounter();
    data = TestData.estadoGestion();
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/estados-gestion");
  });

  test("GW01: Modal opens with required fields", async () => {
    // Given
    await steps.givenModuleIsVisible("Estados de Gestión");

    // When
    await steps.openNewRecordModal(/nuevo estado/i);

    // Then
    await steps.thenModalIsVisible("Nuevo Estado de Gestión");
    await steps.thenFormHasField("Nombre");
  });

  test("GW02: Create estado de gestión and verify persistence", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo estado/i);

    // When: Fill fields and submit
    await steps.whenUserFillsField("Nombre", data.nombre);
    await steps.whenUserFillsField("Descripción", data.descripcion);
    await steps.whenUserSubmitsForm();

    // Then: UI shows success
    await steps.thenShowsSuccessMessage("creado");
    await steps.thenTableIsVisible();

    // And: Verify via API
    const id = await verifyEstadoGestionCreado(steps.page, data.nombre);
    expect(id).toBeGreaterThan(0);
  });

  test("GW03: Cancel button closes modal", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo estado/i);

    // When
    await steps.whenUserCancelsForm();

    // Then
    await steps.thenModalIsNotVisible();
  });

  test("GW04: Table shows list of estados de gestión", async () => {
    // Then
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible("Nombre");
  });
});
