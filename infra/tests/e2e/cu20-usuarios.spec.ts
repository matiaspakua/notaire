/**
 * CU20 — Dar alta usuario
 *
 * Full-stack E2E: UI → API → DB persistence verification.
 * Creates a new system user through UI and verifies persistence.
 * Note: Requires a Persona to link to the user.
 *
 * References: CU21, CU23
 * GitHub: #173
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";
import { TestData, resetTestDataCounter } from "./helpers/test-data";
import { apiPost, apiGet, apiDelete } from "./helpers/api-helpers";

test.describe("CU20 — Dar alta usuario", () => {
  let steps: GherkinSteps;
  let data: ReturnType<typeof TestData.usuario>;
  let personaData: ReturnType<typeof TestData.persona>;
  let createdPersonaId: number;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    resetTestDataCounter();
    data = TestData.usuario();
    personaData = TestData.persona();

    // Ensure a Persona exists to link to the user
    await steps.givenUserIsLoggedIn();
    const personaResp = await apiPost<{ idPersona?: number }>(
      page,
      "/personas",
      {
        nombre: personaData.nombre,
        apellido: personaData.apellido,
        dni: personaData.dni,
        email: personaData.email,
        telefono: personaData.telefono,
      }
    );
    expect(personaResp.ok, "Failed to create prerequisite persona").toBe(true);
    createdPersonaId = personaResp.data?.idPersona ?? 0;
    expect(createdPersonaId).toBeGreaterThan(0);

    await steps.givenUserIsOnPage("/dashboard/administracion/usuarios");
  });

  test.afterEach(async () => {
    // Cleanup created user (API-based, will run in context)
  });

  test("GW01: Page loads with table and new button", async () => {
    // Given
    await steps.givenModuleIsVisible("Usuarios");

    // Then
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible("Nuevo usuario");
  });

  test("GW02: Create usuario and verify persistence", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo usuario/i);
    await steps.thenModalIsVisible("Nuevo usuario");

    // When: Fill fields and submit
    await steps.whenUserFillsField("Nombre de usuario", data.nombre);
    await steps.whenUserFillsField("Contraseña", data.contrasenia);
    // Select role from dropdown
    await steps.whenUserSelectsFromDropdown("Tipo", data.tipo);
    await steps.whenUserSubmitsForm();

    // Then: UI shows success
    await steps.thenShowsSuccessMessage("creado");
    await steps.thenTableIsVisible();

    // And: Verify via API
    const { data: usuarios } = await apiGet(steps.page, "/usuarios");
    const found = (usuarios as Array<{ nombre?: string; idUsuario?: number }>)?.find(
      (u) => u.nombre === data.nombre
    );
    expect(found, `Usuario "${data.nombre}" not found in API response`).toBeDefined();
    expect(found!.idUsuario).toBeGreaterThan(0);

    // Cleanup: Delete the created user
    if (found?.idUsuario) {
      await apiDelete(steps.page, `/usuarios/${found.idUsuario}`);
    }

    // Cleanup: Delete the prerequisite persona
    if (createdPersonaId) {
      await apiDelete(steps.page, `/personas/${createdPersonaId}`);
    }
  });

  test("GW03: Cancel button closes modal", async () => {
    // Given
    await steps.openNewRecordModal(/nuevo usuario/i);

    // When
    await steps.whenUserCancelsForm();

    // Then
    await steps.thenModalIsNotVisible();
  });

  test("GW04: Table shows all existing usuarios", async () => {
    // Then
    await steps.thenTableIsVisible();
    await steps.thenElementIsVisible("Rol");
    await steps.thenElementIsVisible("Estado");
  });
});
