/**
 * CU17 — Dar Alta Persona
 * CU18 — Dar Alta Cliente
 *
 * Full-stack E2E: UI → API → DB persistence verification.
 * Personas and Clientes share the same page (personas page).
 * A Cliente is a Persona with esCliente=true.
 *
 * References: CU21, CU41, CU46, CU48, CU51, CU54, CU61
 * GitHub: #170, #171
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";
import { TestData, resetTestDataCounter } from "./helpers/test-data";
import { verifyPersonaCreada, eliminarPersona } from "./helpers/api-helpers";

test.describe("CU17 — Dar Alta Persona", () => {
  let steps: GherkinSteps;
  let data: ReturnType<typeof TestData.persona>;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    resetTestDataCounter();
    data = TestData.persona();
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/personas");
  });

  test("GW01: Modal opens with all required form fields", async () => {
    // Given
    await steps.givenModuleIsVisible("Personas");

    // When
    await steps.openNewRecordModal(/nueva persona/i);

    // Then: All required fields visible
    await steps.thenModalIsVisible("Nueva persona");
    await steps.thenFormHasField("Nombre");
    await steps.thenFormHasField("Apellido");
  });

  test("GW02: Create persona with required fields and verify persistence", async () => {
    // Given
    await steps.openNewRecordModal(/nueva persona/i);

    // When: Fill required fields and submit
    await steps.whenUserFillsField("Nombre", data.nombre);
    await steps.whenUserFillsField("Apellido", data.apellido);
    await steps.whenUserFillsField("DNI", data.dni);
    await steps.whenUserFillsField("Email", data.email);
    await steps.whenUserFillsField("Teléfono", data.telefono);
    await steps.whenUserFillsField("Domicilio", data.domicilio);
    await steps.whenUserSubmitsForm();

    // Then: Success message appears
    await steps.thenShowsSuccessMessage("creada");
    await steps.thenTableIsVisible();

    // And: Verify data persisted via API
    const id = await verifyPersonaCreada(steps.page, data.nombre, data.apellido);
    expect(id).toBeGreaterThan(0);

    // Cleanup
    await eliminarPersona(steps.page, id);
  });

  test("GW03: Cancel button closes the modal", async () => {
    // Given
    await steps.openNewRecordModal(/nueva persona/i);

    // When
    await steps.whenUserCancelsForm();

    // Then
    await steps.thenModalIsNotVisible();
  });

  test("GW04: Table shows existing personas", async () => {
    // Then
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible("Nombre");
    await steps.thenElementIsVisible("Tipo");
  });
});

test.describe("CU18 — Dar Alta Cliente", () => {
  let steps: GherkinSteps;
  let data: ReturnType<typeof TestData.cliente>;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    resetTestDataCounter();
    data = TestData.cliente();
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/personas");
  });

  test("GW01: Create persona as cliente (esCliente=true)", async () => {
    // Given: Open new persona modal
    await steps.openNewRecordModal(/nueva persona/i);

    // When: Fill fields and check "Es cliente"
    await steps.whenUserFillsField("Nombre", data.nombre);
    await steps.whenUserFillsField("Apellido", data.apellido);
    await steps.whenUserFillsField("DNI", data.dni);
    await steps.whenUserFillsField("Email", data.email);
    await steps.whenUserFillsField("Teléfono", data.telefono);
    await steps.whenUserFillsField("Domicilio", data.domicilio);

    // Check "Es cliente" checkbox
    const esClienteCheckbox = steps.page.getByText("Es cliente").or(
      steps.page.locator('label:has-text("Es cliente")')
    ).first();
    await esClienteCheckbox.click();

    await steps.whenUserSubmitsForm();

    // Then: Success message
    await steps.thenShowsSuccessMessage("creada");
    await steps.thenTableIsVisible();

    // And: Verify via API that esCliente=true
    const id = await verifyPersonaCreada(steps.page, data.nombre, data.apellido);
    expect(id).toBeGreaterThan(0);

    // Cleanup
    await eliminarPersona(steps.page, id);
  });

  test("GW02: Table shows Cliente badge for clients", async () => {
    // Then: The table should show Cliente badge for client rows
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible("Cliente");
  });
});
