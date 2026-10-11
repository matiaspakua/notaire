/**
 * Tipo de Documento - Vencimiento y responsable (CU27/CU32/CU42)
 *
 * Covers: CU27 (Ingresar nuevo tipo de document), CU32 (Modificar tipo de
 * document) — loading vence/diasVencimiento/quienEntrega so CU42 (Informar
 * próximos vencimientos) has data to report on.
 *
 * Reference: docs/openspec/changes/tipo-document-vencimiento-config
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("Tipo de Documento - Vencimiento y responsable", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administration/documents");
  });

  test("CU27-GW01: Given nuevo tipo de documento, When vence is unchecked, Then dias vencimiento is hidden", async ({
    page,
  }) => {
    await page.getByTestId("btn-new-document-type").click();
    await steps.thenModalIsVisible();

    await expect(page.getByTestId("input-due-days-document")).not.toBeVisible();

    await page.getByTestId("checkbox-expires-document").click();

    await expect(page.getByTestId("input-due-days-document")).toBeVisible();
  });

  test("CU27-GW02: Given vence checked without dias, When saving, Then shows validation error", async ({ page }) => {
    await page.getByTestId("btn-new-document-type").click();
    await page.getByTestId("input-name-document").fill(`Poder Vence ${Date.now()}`);
    await page.getByTestId("checkbox-expires-document").click();

    await page.getByRole("button", { name: /crear|guardar/i }).click();

    await steps.thenShowsErrorMessage("días");
  });

  test("CU27-GW03: Given a complete form, When creating a tipo de documento with vencimiento, Then it is persisted", async ({
    page,
  }) => {
    const name = `Cedula Vence ${Date.now()}`;
    await page.getByTestId("btn-new-document-type").click();
    await page.getByTestId("input-name-document").fill(name);
    await page.getByTestId("checkbox-expires-document").click();
    await page.getByTestId("input-due-days-document").fill("30");
    await page.getByTestId("input-delivered-by-document").fill("Registro de la Propiedad");

    await page.getByRole("button", { name: /crear|guardar/i }).click();

    await steps.thenShowsSuccessMessage("creado");
    await expect(page.getByRole("cell", { name: name })).toBeVisible({ timeout: 10000 });
  });

  test("CU32-GW01: Given an existing tipo de documento, When editing it, Then vencimiento fields are pre-filled", async ({
    page,
  }) => {
    const name = `Contrato Vence ${Date.now()}`;
    await page.getByTestId("btn-new-document-type").click();
    await page.getByTestId("input-name-document").fill(name);
    await page.getByTestId("checkbox-expires-document").click();
    await page.getByTestId("input-due-days-document").fill("15");
    await page.getByTestId("input-delivered-by-document").fill("Escribano");
    await page.getByRole("button", { name: /crear|guardar/i }).click();
    await steps.thenShowsSuccessMessage("creado");

    const row = page.getByRole("row", { name: new RegExp(name) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await row.getByRole("button").first().click();

    await steps.thenModalIsVisible();
    await expect(page.getByTestId("checkbox-expires-document")).toBeChecked();
    await expect(page.getByTestId("input-due-days-document")).toHaveValue("15");
    await expect(page.getByTestId("input-delivered-by-document")).toHaveValue("Escribano");
  });

  test("CU27-GW04: Given nuevo tipo de documento, When form opens, Then enabled is checked and returned is unchecked", async ({
    page,
  }) => {
    await page.getByTestId("btn-new-document-type").click();
    await steps.thenModalIsVisible();

    await expect(page.getByTestId("checkbox-enabled-document")).toBeChecked();
    await expect(page.getByTestId("checkbox-returned-document")).not.toBeChecked();
  });

  test("CU27-GW05: Given enabled/returned toggled, When creating, Then values persist on edit reopen", async ({
    page,
  }) => {
    const name = `Doc Enabled Returned ${Date.now()}`;
    await page.getByTestId("btn-new-document-type").click();
    await page.getByTestId("input-name-document").fill(name);
    // Defaults: enabled checked, returned unchecked — flip both.
    await page.getByTestId("checkbox-enabled-document").click();
    await page.getByTestId("checkbox-returned-document").click();

    await page.getByRole("button", { name: /crear|guardar|save|create/i }).click();
    await steps.thenShowsSuccessMessage("creado|created");

    const row = page.getByRole("row", { name: new RegExp(name) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await row.getByRole("button").first().click();

    await steps.thenModalIsVisible();
    await expect(page.getByTestId("checkbox-enabled-document")).not.toBeChecked();
    await expect(page.getByTestId("checkbox-returned-document")).toBeChecked();
  });

  test("CU32-GW02: Given an existing tipo with returned, When editing, Then enabled and returned are pre-filled", async ({
    page,
  }) => {
    const name = `Doc Returned Edit ${Date.now()}`;
    await page.getByTestId("btn-new-document-type").click();
    await page.getByTestId("input-name-document").fill(name);
    await page.getByTestId("checkbox-returned-document").click();
    await page.getByRole("button", { name: /crear|guardar|save|create/i }).click();
    await steps.thenShowsSuccessMessage("creado|created");

    const row = page.getByRole("row", { name: new RegExp(name) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await row.getByRole("button").first().click();

    await steps.thenModalIsVisible();
    await expect(page.getByTestId("checkbox-enabled-document")).toBeChecked();
    await expect(page.getByTestId("checkbox-returned-document")).toBeChecked();
  });
});
