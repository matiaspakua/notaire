/**
 * Tipo de Documento - Vencimiento y responsable (CU27/CU32/CU42)
 *
 * Covers: CU27 (Ingresar nuevo tipo de documento), CU32 (Modificar tipo de
 * documento) — loading vence/diasVencimiento/quienEntrega so CU42 (Informar
 * próximos vencimientos) has data to report on.
 *
 * Reference: openspec/changes/tipo-documento-vencimiento-config
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("Tipo de Documento - Vencimiento y responsable", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/documentos");
  });

  test("CU27-GW01: Given nuevo tipo de documento, When vence is unchecked, Then dias vencimiento is hidden", async ({
    page,
  }) => {
    await page.getByTestId("btn-nuevo-tipo-documento").click();
    await steps.thenModalIsVisible();

    await expect(page.getByTestId("input-dias-vencimiento-documento")).not.toBeVisible();

    await page.getByTestId("checkbox-vence-documento").getByRole("checkbox").click();

    await expect(page.getByTestId("input-dias-vencimiento-documento")).toBeVisible();
  });

  test("CU27-GW02: Given vence checked without dias, When saving, Then shows validation error", async ({ page }) => {
    await page.getByTestId("btn-nuevo-tipo-documento").click();
    await page.getByTestId("input-nombre-documento").fill(`Poder Vence ${Date.now()}`);
    await page.getByTestId("checkbox-vence-documento").getByRole("checkbox").click();

    await page.getByRole("button", { name: /crear|guardar/i }).click();

    await steps.thenShowsErrorMessage("días");
  });

  test("CU27-GW03: Given a complete form, When creating a tipo de documento with vencimiento, Then it is persisted", async ({
    page,
  }) => {
    const nombre = `Cedula Vence ${Date.now()}`;
    await page.getByTestId("btn-nuevo-tipo-documento").click();
    await page.getByTestId("input-nombre-documento").fill(nombre);
    await page.getByTestId("checkbox-vence-documento").getByRole("checkbox").click();
    await page.getByTestId("input-dias-vencimiento-documento").fill("30");
    await page.getByTestId("input-quien-entrega-documento").fill("Registro de la Propiedad");

    await page.getByRole("button", { name: /crear|guardar/i }).click();

    await steps.thenShowsSuccessMessage("creado");
    await expect(page.getByRole("cell", { name: nombre })).toBeVisible({ timeout: 10000 });
  });

  test("CU32-GW01: Given an existing tipo de documento, When editing it, Then vencimiento fields are pre-filled", async ({
    page,
  }) => {
    const nombre = `Contrato Vence ${Date.now()}`;
    await page.getByTestId("btn-nuevo-tipo-documento").click();
    await page.getByTestId("input-nombre-documento").fill(nombre);
    await page.getByTestId("checkbox-vence-documento").getByRole("checkbox").click();
    await page.getByTestId("input-dias-vencimiento-documento").fill("15");
    await page.getByTestId("input-quien-entrega-documento").fill("Escribano");
    await page.getByRole("button", { name: /crear|guardar/i }).click();
    await steps.thenShowsSuccessMessage("creado");

    const row = page.getByRole("row", { name: new RegExp(nombre) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await row.getByRole("button").first().click();

    await steps.thenModalIsVisible();
    await expect(page.getByTestId("checkbox-vence-documento").getByRole("checkbox")).toBeChecked();
    await expect(page.getByTestId("input-dias-vencimiento-documento")).toHaveValue("15");
    await expect(page.getByTestId("input-quien-entrega-documento")).toHaveValue("Escribano");
  });
});
