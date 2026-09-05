/**
 * TS-0090 - Demo seed — two full comparable cases
 *
 * Creates 2 complete, comparable "gestiones" through the real Notaire UI
 * (no direct API/SQL), each with every dependent entity: cliente,
 * presupuesto, tipo de trámite, folio, gestión, inmueble, escritura (firmada),
 * testimonio (verificado), documento presentado, pago and copia.
 *
 * Intended for live-demo prep: run once against a dev stack to populate the
 * database with two side-by-side cases before showing the app. Reference
 * suite (on demand), same category as TS-0070/TS-0071.
 *
 *   HEADED=1 SLOW_MO=400 npx playwright test TS-0090-demo-two-full-cases --project=chromium
 */
import { expect, test, type Page } from "@playwright/test";

const adminUser = process.env.E2E_TEST_ADMIN_USER ?? "admin";
const adminPassword = process.env.E2E_TEST_ADMIN_PASS ?? "admin";
const pauseMs = Number(process.env.TUTORIAL_PAUSE_MS ?? 400);
const runId = Date.now().toString().slice(-6);

async function pause(page: Page, multiplier = 1): Promise<void> {
  await page.waitForTimeout(pauseMs * multiplier);
}

async function go(page: Page, path: string): Promise<void> {
  await page.goto(path);
  await expect(page.locator("h1").filter({ hasText: /\S/ }).first()).toBeVisible();
  await pause(page);
}

async function choose(page: Page, triggerTestId: string, option: RegExp): Promise<void> {
  await page.getByTestId(triggerTestId).click();
  await pause(page, 0.5);
  const choice = page.getByRole("option", { name: option });
  await choice.evaluate((element) => (element as HTMLElement).click());
  await pause(page);
}

async function chooseFirst(page: Page, triggerTestId: string): Promise<void> {
  await page.getByTestId(triggerTestId).click();
  await pause(page, 0.5);
  await page.getByRole("option").first().evaluate((element) => (element as HTMLElement).click());
  await pause(page);
}

async function chooseInDialogCombobox(page: Page, option: RegExp): Promise<void> {
  await page.getByRole("dialog").getByRole("combobox").click();
  await pause(page, 0.5);
  const choice = page.getByRole("option", { name: option });
  await choice.evaluate((element) => (element as HTMLElement).click());
  await pause(page);
}

async function saveDialog(page: Page): Promise<void> {
  await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
  await expect(page.getByRole("dialog")).toBeHidden();
  await pause(page);
}

interface CaseDefinition {
  label: string;
  clienteNombre: string;
  clienteApellido: string;
  tipoTramite: string;
  tipoFolio: string;
  tipoDocumento: string;
  inmuebleDomicilio: string;
  inmuebleNomenclatura: string;
  presupuestoMonto: string;
  gestionNumero: string;
  escrituraNumero: string;
  folioNumero: string;
  pagoMonto: string;
  copiaNumero: string;
}

function buildCaseDefinition(label: "A" | "B", suffix: string): CaseDefinition {
  return {
    label,
    clienteNombre: label === "A" ? "María" : "Carlos",
    clienteApellido: `Demo${label} ${suffix}`,
    tipoTramite: `Compraventa Demo ${label} ${suffix}`,
    tipoFolio: `Protocolo Demo ${label} ${suffix}`,
    tipoDocumento: `DNI Comprador Demo ${label} ${suffix}`,
    inmuebleDomicilio: `Av. Demo ${label} 1234, CABA ${suffix}`,
    inmuebleNomenclatura: `0${label === "A" ? "1" : "2"}-0${label === "A" ? "2" : "3"}-${suffix}`,
    presupuestoMonto: label === "A" ? "150000" : "220000",
    gestionNumero: `${label === "A" ? "10" : "20"}${suffix}`,
    escrituraNumero: `${label === "A" ? "30" : "40"}${suffix}`,
    folioNumero: `${label === "A" ? "50" : "60"}${suffix}`,
    pagoMonto: label === "A" ? "75000" : "110000",
    copiaNumero: `${label === "A" ? "70" : "80"}${suffix}`,
  };
}

async function buildFullCase(page: Page, def: CaseDefinition): Promise<void> {
  await test.step(`[Caso ${def.label}] Tipo de trámite`, async () => {
    await go(page, "/dashboard/administracion/tramites");
    await page.getByTestId("btn-nuevo-tipo-tramite").click();
    await page.getByTestId("input-nombre-tramite").fill(def.tipoTramite);
    await saveDialog(page);
    await page.getByTestId("input-search-tramite").fill(def.tipoTramite);
    await expect(page.getByRole("table")).toContainText(def.tipoTramite);
  });

  await test.step(`[Caso ${def.label}] Tipo de documento`, async () => {
    await go(page, "/dashboard/administracion/documentos");
    await page.getByTestId("btn-nuevo-tipo-documento").click();
    await page.getByTestId("input-nombre-documento").fill(def.tipoDocumento);
    await saveDialog(page);
    await page.getByTestId("input-search-documento").fill(def.tipoDocumento);
    await expect(page.getByRole("table")).toContainText(def.tipoDocumento);
  });

  await test.step(`[Caso ${def.label}] Tipo de folio y folio`, async () => {
    await go(page, "/dashboard/administracion/folios");
    await page.getByTestId("btn-nuevo-tipo-folio").click();
    await page.getByTestId("input-nombre-tipo-folio").fill(def.tipoFolio);
    await page.getByTestId("btn-save-tipo-folio").click();
    await expect(page.getByRole("dialog")).toBeHidden();
    await pause(page);

    await page.getByTestId("btn-nuevo-folio").click();
    await page.getByTestId("input-numero-folio").fill(def.folioNumero);
    await choose(page, "select-estado-folio", /nuevo/i);
    await choose(page, "select-tipo-folio", new RegExp(def.tipoFolio, "i"));
    await chooseFirst(page, "select-escribano-folio");
    await saveDialog(page);
  });

  await test.step(`[Caso ${def.label}] Cliente`, async () => {
    await go(page, "/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    await page.getByTestId("input-nombre").fill(def.clienteNombre);
    await page.getByTestId("input-apellido").fill(def.clienteApellido);
    await page.getByRole("dialog").getByLabel(/dni/i).fill(`DNI${runId}${def.label}`);
    await page.getByLabel(/email/i).fill(`${def.label.toLowerCase()}.demo.${runId}@notaire.test`);
    await page.getByTestId("check-es-cliente").click();
    await saveDialog(page);
    await page.getByTestId("input-search-apellido").fill(def.clienteApellido);
    await expect(page.getByRole("table")).toContainText(def.clienteApellido);
  });

  await test.step(`[Caso ${def.label}] Inmueble`, async () => {
    await go(page, "/dashboard/inmuebles");
    await page.getByRole("button", { name: /nuevo inmueble/i }).click();
    await page.getByRole("dialog").getByLabel(/nomenclatura catastral/i).fill(def.inmuebleNomenclatura);
    await page.getByRole("dialog").getByLabel(/domicilio/i).fill(def.inmuebleDomicilio);
    await page.getByRole("dialog").getByLabel(/valuación fiscal/i).fill(def.label === "A" ? "180000" : "260000");
    await saveDialog(page);
  });

  await test.step(`[Caso ${def.label}] Presupuesto`, async () => {
    await go(page, "/dashboard/presupuestos");
    await page.getByTestId("btn-nuevo-presupuesto").click();
    await choose(page, "select-persona", new RegExp(def.clienteApellido, "i"));
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-08-04");
    await page.getByTestId("input-monto").fill(def.presupuestoMonto);
    await saveDialog(page);
    await page.getByTestId("input-search-presupuesto").fill(def.clienteApellido);
    await expect(page.getByRole("table")).toContainText(def.clienteApellido);
  });

  await test.step(`[Caso ${def.label}] Gestión`, async () => {
    await go(page, "/dashboard/gestiones");
    await page.getByTestId("btn-nueva-gestion").click();
    await page.getByTestId("input-numero-gestion").fill(def.gestionNumero);
    await choose(page, "select-presupuesto-gestion", new RegExp(def.clienteApellido, "i"));
    await chooseFirst(page, "select-escribano-gestion");
    await chooseFirst(page, "select-estado-gestion");
    await choose(page, "select-tipo-tramite-gestion", new RegExp(def.tipoTramite, "i"));
    await choose(page, "select-inmueble-gestion", new RegExp(def.inmuebleDomicilio, "i"));
    await saveDialog(page);
    await expect(page.getByRole("table")).toContainText(def.gestionNumero);
  });

  let escrituraId = "";
  await test.step(`[Caso ${def.label}] Escritura y firma`, async () => {
    await go(page, "/dashboard/escrituras");
    await page.getByRole("button", { name: /nueva escritura/i }).click();
    await page.getByRole("dialog").getByLabel(/número/i).fill(def.escrituraNumero);
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-08-05");
    await choose(page, "select-folio-escritura", new RegExp(def.tipoFolio, "i"));
    await page
      .getByRole("dialog")
      .getByLabel(/observaciones/i)
      .fill("Numeración no correlativa: seed de datos E2E aislado (CU86)");
    await saveDialog(page);

    await page.getByTestId("input-search-escritura").fill(def.escrituraNumero);
    const row = page.getByRole("row").filter({ hasText: def.escrituraNumero });
    escrituraId = ((await row.locator("td").first().innerText()) ?? "").trim();
    await row.getByTestId(`btn-firmar-escritura-${escrituraId}`).click();
    await page.getByRole("button", { name: /firmar escritura/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();
    await pause(page);
  });

  await test.step(`[Caso ${def.label}] Testimonio, verificación y copia`, async () => {
    await go(page, "/dashboard/testimonios");
    await page.getByTestId("btn-generar-testimonio").click();
    await choose(page, "select-escritura-testimonio", new RegExp(def.escrituraNumero, "i"));
    await page.getByTestId("btn-guardar-testimonio").click();
    await expect(page.getByRole("dialog")).toBeHidden();
    await pause(page);

    const row = page.getByRole("row").filter({ hasText: def.escrituraNumero });
    const testimonioId = ((await row.locator("td").first().innerText()) ?? "").trim();
    await row.getByTestId(`btn-verificar-testimonio-${testimonioId}`).click();
    await page.getByTestId("btn-confirmar-verificar-testimonio").click();
    await pause(page);

    await go(page, "/dashboard/copias");
    await page.getByRole("button", { name: /nueva copia/i }).click();
    await page.getByRole("dialog").getByLabel(/número/i).fill(def.copiaNumero);
    await saveDialog(page);
  });

  await test.step(`[Caso ${def.label}] Documento presentado`, async () => {
    await go(page, "/dashboard/documentos");
    await page.getByRole("button", { name: /nuevo documento/i }).click();
    await chooseInDialogCombobox(page, new RegExp(def.tipoDocumento, "i"));
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-08-06");
    await page.getByRole("dialog").getByLabel(/documento entregado/i).click();
    await saveDialog(page);
  });

  await test.step(`[Caso ${def.label}] Pago`, async () => {
    await go(page, "/dashboard/pagos");
    await page.getByTestId("btn-nuevo-pago").click();
    await choose(page, "select-presupuesto-pago", new RegExp(def.clienteApellido, "i"));
    await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-08-07");
    await page.getByRole("dialog").getByLabel(/monto/i).fill(def.pagoMonto);
    await saveDialog(page);
  });
}

test.describe("Demo seed — two full comparable cases", () => {
  test.setTimeout(15 * 60 * 1000);

  test("seeds Caso A and Caso B end to end through the UI", async ({ page }) => {
    await test.step("Sign in", async () => {
      await page.goto("/login");
      await page.getByTestId("input-usuario").fill(adminUser);
      await page.getByTestId("input-contrasenia").fill(adminPassword);
      await page.getByTestId("btn-ingresar").click();
      await expect(page).toHaveURL(/\/dashboard/);
    });

    const caseA = buildCaseDefinition("A", runId);
    const caseB = buildCaseDefinition("B", runId);

    await buildFullCase(page, caseA);
    await buildFullCase(page, caseB);

    await test.step("Both cases visible on dashboard", async () => {
      await go(page, "/dashboard/gestiones");
      await expect(page.getByRole("table")).toContainText(caseA.gestionNumero);
      await expect(page.getByRole("table")).toContainText(caseB.gestionNumero);
    });
  });
});
