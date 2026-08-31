/**
 * CU01 - Preparar Presupuesto: la asociación con el cliente (Gherkin style)
 *
 * Covers Issue #883: PresupuestoController.create/update bound to the raw
 * Presupuesto entity (field fkIdPersona) while the frontend sends "persona",
 * so every Presupuesto created or edited from the UI silently lost its
 * client association.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("CU01 - Preparar Presupuesto: asociación con cliente", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("CU01-GW01: Given a Presupuesto created selecting a cliente, Then it keeps the association", async ({
    page,
  }) => {
    const apellido = `E2E${Date.now()}`;
    const dni = `${Date.now()}`.slice(-8);

    await steps.givenUserIsOnPage("/dashboard/personas");
    await steps.whenUserClicksButton("nueva persona");
    await steps.thenModalIsVisible();
    await steps.whenUserFillsField("Nombre", "Cliente");
    await steps.whenUserFillsField("Apellido", apellido);
    await steps.whenUserFillsField("DNI", dni);
    await steps.whenUserSubmitsForm();
    await steps.thenModalIsNotVisible();

    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    await steps.whenUserClicksButton("nuevo presupuesto");
    await steps.thenModalIsVisible();
    await steps.whenUserSelectsFromDropdown("cliente", apellido);
    await steps.whenUserFillsField("Fecha", "2026-01-01");
    await steps.whenUserFillsField("Monto", "1000");
    await steps.whenUserSubmitsForm();
    await steps.thenModalIsNotVisible();

    await steps.thenTableContainsText(apellido);
  });

  test("CU01-GW02: Given a Presupuesto with cliente, When searching by apellido (CU60), Then it is found", async ({
    page,
  }) => {
    const apellido = `E2ESearch${Date.now()}`;
    const dni = `${Date.now()}`.slice(-8);

    await steps.givenUserIsOnPage("/dashboard/personas");
    await steps.whenUserClicksButton("nueva persona");
    await steps.thenModalIsVisible();
    await steps.whenUserFillsField("Nombre", "Cliente");
    await steps.whenUserFillsField("Apellido", apellido);
    await steps.whenUserFillsField("DNI", dni);
    await steps.whenUserSubmitsForm();
    await steps.thenModalIsNotVisible();

    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    await steps.whenUserClicksButton("nuevo presupuesto");
    await steps.thenModalIsVisible();
    await steps.whenUserSelectsFromDropdown("cliente", apellido);
    await steps.whenUserFillsField("Fecha", "2026-01-01");
    await steps.whenUserFillsField("Monto", "1000");
    await steps.whenUserSubmitsForm();
    await steps.thenModalIsNotVisible();

    await page.getByTestId("input-search-presupuesto").fill(apellido);
    await steps.thenTableContainsText(apellido);
  });
});
