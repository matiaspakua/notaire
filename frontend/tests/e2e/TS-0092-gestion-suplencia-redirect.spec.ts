/**
 * TS-0092 - Efecto práctico de las suplencias en la asignación de gestiones
 * CU22 - Registrar Suplencia
 * CU59 - Aplicar suplencia a una gestión (redirección automática)
 * CU48 - Dar alta escribano (registro de escribano)
 * CU51 - Modificar escribano (registro de escribano)
 * Issue #836
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { createPersona, createPresupuesto, createSuplencia, uniqueId } from "./setup/api-helpers";

async function choose(page: Page, triggerTestId: string, option: RegExp): Promise<void> {
  await page.getByTestId(triggerTestId).click();
  const choice = page.getByRole("option", { name: option });
  await expect(choice).toBeVisible({ timeout: 8000 });
  await choice.click();
}

async function chooseFirst(page: Page, triggerTestId: string): Promise<void> {
  await page.getByTestId(triggerTestId).click();
  await page.getByRole("option").first().click();
}

// ---------------------------------------------------------------------------
// CU48/CU51 - Registro de escribano en el formulario de personas
// ---------------------------------------------------------------------------

test.describe("CU48/CU51 - Registro de escribano en Personas", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
    await page.goto("/dashboard/personas");
    await page.waitForLoadState("domcontentloaded");
  });

  test("CU48-GW01: alta de persona con registro de escribano se guarda correctamente", async ({ page }) => {
    const apellido = `Escribano${uniqueId() % 100_000}`;
    const dni = `48${uniqueId() % 10_000_000}`;

    await page.getByTestId("btn-nueva-persona").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await page.getByTestId("input-nombre").fill("Juan");
    await page.getByTestId("input-apellido").fill(apellido);
    await dialog.getByLabel(/dni/i).fill(dni);
    await page.getByTestId("input-registro-escribano").fill("123");

    await dialog.getByRole("button", { name: /crear|guardar/i }).click();
    await expect(
      page.locator("[data-sonner-toast]").getByText(/creada/i),
    ).toBeVisible({ timeout: 8000 });
  });

  test("CU51-GW01: editar persona y modificar su registro de escribano", async ({ page }) => {
    const persona = await createPersona(page, {
      apellido: `Modif${uniqueId() % 100_000}`,
      numeroIdentificacion: `51${uniqueId() % 10_000_000}`,
    });
    const apellido = persona.data!.apellido!;

    await page.reload();
    await page.waitForLoadState("domcontentloaded");
    await page.getByTestId("input-search-apellido").fill(apellido);
    const row = page.getByRole("row", { name: new RegExp(apellido, "i") });
    await expect(row).toBeVisible({ timeout: 10000 });
    await row.getByRole("button").first().click();

    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    await page.getByTestId("input-registro-escribano").fill("456");
    await dialog.getByRole("button", { name: /actualizar|guardar/i }).click();

    await expect(
      page.locator("[data-sonner-toast]").getByText(/actualizada/i),
    ).toBeVisible({ timeout: 8000 });
  });
});

// ---------------------------------------------------------------------------
// CU22/CU59 - Redirección automática de la gestión al escribano suplente
// ---------------------------------------------------------------------------

test.describe("CU22/CU59 - Redirección de gestión por suplencia activa", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("GW01: gestión creada para un escribano con suplencia activa se redirige al suplente", async ({ page }) => {
    const escribano = await createPersona(page, {
      apellido: `Suplantado${uniqueId() % 100_000}`,
    });
    const suplente = await createPersona(page, {
      apellido: `Suplente${uniqueId() % 100_000}`,
    });
    const cliente = await createPersona(page, {
      apellido: `Cliente${uniqueId() % 100_000}`,
    });
    const presupuesto = await createPresupuesto(page, cliente.data!.idPersona);

    const hoy = new Date();
    const manana = new Date(hoy.getTime() + 24 * 60 * 60 * 1000);
    const suplencia = await createSuplencia(page, suplente.data!.idPersona, escribano.data!.idPersona, {
      fechaInicio: hoy.toISOString().split("T")[0],
      fechaFin: manana.toISOString().split("T")[0],
    });
    expect(suplencia.ok).toBe(true);

    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("domcontentloaded");
    await page.getByTestId("btn-nueva-gestion").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await page.getByTestId("input-numero-gestion").fill(String(uniqueId() % 1_000_000));
    await choose(page, "select-presupuesto-gestion", new RegExp(cliente.data!.apellido!, "i"));
    await choose(page, "select-escribano-gestion", new RegExp(escribano.data!.apellido!, "i"));
    await chooseFirst(page, "select-estado-gestion");
    await chooseFirst(page, "select-tipo-tramite-gestion");

    await page.getByTestId("btn-guardar-gestion").click();

    await expect(
      page.locator("[data-sonner-toast]").getByText(/creada/i),
    ).toBeVisible({ timeout: 8000 });
    await expect(
      page.locator("[data-sonner-toast]").getByText(/suplente/i).first(),
    ).toBeVisible({ timeout: 8000 });
  });

  test("EDGE: gestión creada para un escribano sin suplencia activa no dispara aviso de redirección", async ({ page }) => {
    const escribano = await createPersona(page, {
      apellido: `SinSuplencia${uniqueId() % 100_000}`,
    });
    const cliente = await createPersona(page, {
      apellido: `ClienteNorm${uniqueId() % 100_000}`,
    });
    await createPresupuesto(page, cliente.data!.idPersona);

    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("domcontentloaded");
    await page.getByTestId("btn-nueva-gestion").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await page.getByTestId("input-numero-gestion").fill(String(uniqueId() % 1_000_000));
    await choose(page, "select-presupuesto-gestion", new RegExp(cliente.data!.apellido!, "i"));
    await choose(page, "select-escribano-gestion", new RegExp(escribano.data!.apellido!, "i"));
    await chooseFirst(page, "select-estado-gestion");
    await chooseFirst(page, "select-tipo-tramite-gestion");

    await page.getByTestId("btn-guardar-gestion").click();

    await expect(
      page.locator("[data-sonner-toast]").getByText(/creada/i),
    ).toBeVisible({ timeout: 8000 });
    await expect(
      page.locator("[data-sonner-toast]").getByText(/suplente/i),
    ).not.toBeVisible();
  });
});
