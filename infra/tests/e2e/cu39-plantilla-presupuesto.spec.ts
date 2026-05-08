/**
 * CU39 — Crear Plantilla Presupuesto
 *
 * Full-stack E2E: UI → API → DB persistence verification.
 * Creates a new plantilla de presupuesto through the UI.
 * Requires: A TipoDeTramite must exist to link to the plantilla.
 *
 * References: CU49, CU55
 * GitHub: #192
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";
import { TestData, resetTestDataCounter } from "./helpers/test-data";
import { apiPost, apiGet, apiDelete } from "./helpers/api-helpers";

test.describe("CU39 — Crear Plantilla Presupuesto", () => {
  let steps: GherkinSteps;
  let data: ReturnType<typeof TestData.plantilla>;
  let tipoTramiteData: ReturnType<typeof TestData.tipoTramite>;
  let createdTipoTramiteId: number;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    resetTestDataCounter();
    data = TestData.plantilla();
    tipoTramiteData = TestData.tipoTramite();

    await steps.givenUserIsLoggedIn();

    // Ensure a TipoDeTramite exists to link to the plantilla
    const ttResp = await apiPost<{ idTipoDeTramite?: number }>(
      page,
      "/tipo-tramite",
      {
        nombre: tipoTramiteData.nombre,
        descripcion: tipoTramiteData.descripcion,
      }
    );
    expect(ttResp.ok, "Failed to create prerequisite tipo de trámite").toBe(true);
    createdTipoTramiteId = ttResp.data?.idTipoDeTramite ?? 0;
    expect(createdTipoTramiteId).toBeGreaterThan(0);

    await steps.givenUserIsOnPage("/dashboard/administracion/plantillas");
  });

  test("GW01: Modal opens with required fields", async () => {
    // Given
    await steps.givenModuleIsVisible("Plantillas");

    // When
    await steps.openNewRecordModal(/nueva plantilla/i);

    // Then
    await steps.thenModalIsVisible("Nueva Plantilla de Presupuesto");
    await steps.thenFormHasField("Nombre");
    await steps.thenFormHasField("Tipo de Trámite");
  });

  test("GW02: Create plantilla and verify persistence", async () => {
    // Given: Open modal and fill
    await steps.openNewRecordModal(/nueva plantilla/i);

    // When: Fill fields and submit
    await steps.whenUserFillsField("Nombre", data.nombre);
    await steps.whenUserSelectsFromDropdown(
      "Tipo de Trámite",
      tipoTramiteData.nombre
    );
    await steps.whenUserSubmitsForm();

    // Then: UI shows success
    await steps.thenShowsSuccessMessage("creada");
    await steps.thenTableIsVisible();

    // And: Verify via API
    const { data: plantillas } = await apiGet(steps.page, "/plantilla-presupuestos");
    const found = (plantillas as Array<{ nombre?: string; idPlantillaPresupuesto?: number }>)?.find(
      (p) => p.nombre === data.nombre
    );
    expect(found, `Plantilla "${data.nombre}" not found in API response`).toBeDefined();
    expect(found!.idPlantillaPresupuesto).toBeGreaterThan(0);
  });

  test("GW03: Cancel button closes modal", async () => {
    // Given
    await steps.openNewRecordModal(/nueva plantilla/i);

    // When
    await steps.whenUserCancelsForm();

    // Then
    await steps.thenModalIsNotVisible();
  });

  test("GW04: Table shows existing plantillas", async () => {
    // Then
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible("Items");
  });
});
