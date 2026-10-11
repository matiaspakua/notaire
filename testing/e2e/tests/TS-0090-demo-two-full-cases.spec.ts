/**
 * TS-0090 - Demo seed — two full comparable cases
 *
 * Creates 2 complete, comparable "managements" through the real Notaire UI
 * (no direct API/SQL), each with every dependent entity: client,
 * budget, procedure type, folio, management, property, signed deed,
 * verified testimony, submitted document, payment, and copy.
 *
 * Intended for live-demo prep: run once against a dev stack to populate the
 * database with two side-by-side cases before showing the app. Reference
 * suite (on demand), same category as TS-0070/TS-0071.
 *
 *   HEADED=1 SLOW_MO=400 npx playwright test TS-0090-demo-two-full-cases --project=chromium
 */
import { expect, test, type Page } from "@playwright/test";

test.describe.configure({ timeout: 300_000 });

const adminUser = process.env.E2E_TEST_ADMIN_USER ?? "admin";
const adminPassword = process.env.E2E_TEST_ADMIN_PASS ?? "admin";
const pauseMs = Number(process.env.TUTORIAL_PAUSE_MS ?? 400);
const runId = Date.now().toString().slice(-6);

async function pause(page: Page, multiplier = 1): Promise<void> {
  // Pacing only for headed/SLOW_MO demos — never a CI correctness wait (#1066)
  const allowPace = process.env.HEADED === "1" || Number(process.env.SLOW_MO ?? 0) > 0;
  if (!allowPace) {
    return;
  }
  await page.waitForTimeout(pauseMs * multiplier);
}

async function go(page: Page, path: string): Promise<void> {
  await page.goto(path);
  await expect(page.locator("h1").filter({ hasText: /\S/ }).first()).toBeVisible();
  await pause(page);
}

async function choose(page: Page, triggerTestId: string, option: RegExp): Promise<void> {
  const trigger = page.getByTestId(triggerTestId);
  await trigger.click();
  // Person pickers are server-search comboboxes (#1340): type the name first.
  if ((await trigger.evaluate((el) => el.tagName)) === "INPUT") await trigger.fill(option.source);
  await pause(page, 0.5);
  const choice = page.getByRole("option", { name: option });
  await choice.evaluate((element) => (element as HTMLElement).click());
  await pause(page);
}

async function chooseFirst(page: Page, triggerTestId: string): Promise<void> {
  await page.getByTestId(triggerTestId).click();
  await pause(page, 0.5);
  await page.getByRole("listbox").getByRole("option").first().evaluate((element) => (element as HTMLElement).click());
  await pause(page);
}

async function chooseInDialogCombobox(page: Page, option: RegExp, testId?: string): Promise<void> {
  const dialog = page.getByRole("dialog");
  const trigger = testId ? dialog.getByTestId(testId) : dialog.getByRole("combobox");
  // Read the tag before clicking: an open Radix select hides the dialog from the
  // accessibility tree, so the dialog locator no longer resolves afterwards.
  const isSearchInput = (await trigger.evaluate((el) => el.tagName)) === "INPUT";
  await trigger.click();
  // Management pickers are server-search comboboxes (#1340): type the number first.
  if (isSearchInput) {
    await trigger.fill(option.source.replace(/^\^/, "").trim());
  }
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
  clientName: string;
  clientApellido: string;
  typeProcedure: string;
  tipoFolio: string;
  typeDocument: string;
  propertyAddress: string;
  propertyCadastralDesignation: string;
  budgetAmount: string;
  managementNumber: string;
  deedNumber: string;
  folioNumber: string;
  paymentAmount: string;
  copiaNumber: string;
}

function buildCaseDefinition(label: "A" | "B", suffix: string): CaseDefinition {
  return {
    label,
    clientName: label === "A" ? "María" : "Carlos",
    clientApellido: `Demo${label} ${suffix}`,
    typeProcedure: `Compraventa Demo ${label} ${suffix}`,
    tipoFolio: `Protocolo Demo ${label} ${suffix}`,
    typeDocument: `DNI Comprador Demo ${label} ${suffix}`,
    propertyAddress: `Av. Demo ${label} 1234, CABA ${suffix}`,
    propertyCadastralDesignation: `0${label === "A" ? "1" : "2"}-0${label === "A" ? "2" : "3"}-${suffix}`,
    budgetAmount: label === "A" ? "150000" : "220000",
    managementNumber: `${label === "A" ? "10" : "20"}${suffix}`,
    deedNumber: `${label === "A" ? "30" : "40"}${suffix}`,
    folioNumber: `${label === "A" ? "50" : "60"}${suffix}`,
    paymentAmount: label === "A" ? "75000" : "110000",
    copiaNumber: `${label === "A" ? "70" : "80"}${suffix}`,
  };
}

async function buildFullCase(page: Page, def: CaseDefinition): Promise<void> {
  await test.step(`[Caso ${def.label}] Tipo de trámite`, async () => {
    await go(page, "/dashboard/administration/procedures");
    await page.getByTestId("btn-new-procedure-type").click();
    await page.getByTestId("input-name-procedure").fill(def.typeProcedure);
    await saveDialog(page);
    await page.getByTestId("input-search-procedure").fill(def.typeProcedure);
    await expect(page.getByRole("table")).toContainText(def.typeProcedure);
  });

  await test.step(`[Caso ${def.label}] Tipo de documento`, async () => {
    await go(page, "/dashboard/administration/documents");
    await page.getByTestId("btn-new-document-type").click();
    await page.getByTestId("input-name-document").fill(def.typeDocument);
    await saveDialog(page);
    await page.getByTestId("input-search-document").fill(def.typeDocument);
    await expect(page.getByRole("table")).toContainText(def.typeDocument);
  });

  await test.step(`[Caso ${def.label}] Tipo de folio y folio`, async () => {
    await go(page, "/dashboard/administration/folios");
    await page.getByTestId("btn-new-folio-type").click();
    await page.getByTestId("input-name-folio-type").fill(def.tipoFolio);
    await page.getByTestId("btn-save-folio-type").click();
    await expect(page.getByRole("dialog")).toBeHidden();
    await pause(page);

    await page.getByTestId("btn-new-folio").click();
    await page.getByTestId("input-folio-number").fill(def.folioNumber);
    await choose(page, "select-estado-folio", /nuevo/i);
    await choose(page, "select-tipo-folio", new RegExp(def.tipoFolio, "i"));
    await chooseFirst(page, "select-escribano-folio");
    await saveDialog(page);
  });

  await test.step(`[Caso ${def.label}] Cliente`, async () => {
    await go(page, "/dashboard/people");
    await page.getByTestId("btn-new-person").click();
    await page.getByTestId("input-firstName").fill(def.clientName);
    await page.getByTestId("input-lastName").fill(def.clientApellido);
    await page.getByRole("dialog").getByTestId("input-dni").fill(`DNI${runId}${def.label}`);
    await page.getByLabel(/email/i).fill(`${def.label.toLowerCase()}.demo.${runId}@notaire.test`);
    await page.getByTestId("check-es-client").click();
    await saveDialog(page);
    await page.getByTestId("input-search-lastName").fill(def.clientApellido);
    await expect(page.getByRole("table")).toContainText(def.clientApellido);
  });

  await test.step(`[Caso ${def.label}] Property`, async () => {
    await go(page, "/dashboard/properties");
    await page.getByRole("button", { name: /nuevo inmueble/i }).click();
    await page.getByRole("dialog").getByLabel(/nomenclatura catastral/i).fill(def.propertyCadastralDesignation);
    await page.getByRole("dialog").getByLabel(/domicilio/i).fill(def.propertyAddress);
    await page.getByRole("dialog").getByLabel(/valuación fiscal/i).fill(def.label === "A" ? "180000" : "260000");
    await saveDialog(page);
  });

  await test.step(`[Caso ${def.label}] Presupuesto`, async () => {
    await go(page, "/dashboard/budgets");
    await page.getByTestId("btn-new-budget").click();
    await choose(page, "select-persona", new RegExp(def.clientApellido, "i"));
    await page.getByRole("dialog").getByLabel(/date/i).fill("2026-08-04");
    await page.getByTestId("input-amount").fill(def.budgetAmount);
    await saveDialog(page);
    await page.getByTestId("input-search-budget").fill(def.clientApellido);
    await expect(page.getByRole("table")).toContainText(def.clientApellido);
  });

  await test.step(`[Caso ${def.label}] Gestión`, async () => {
    await go(page, "/dashboard/managements");
    await page.getByTestId("btn-new-management").click();
    await page.getByTestId("input-management-number").fill(def.managementNumber);
    await choose(page, "select-presupuesto-gestion", new RegExp(def.clientApellido, "i"));
    await chooseFirst(page, "select-escribano-gestion");
    await chooseFirst(page, "select-estado-gestion");
    await choose(page, "select-tipo-tramite-gestion", new RegExp(def.typeProcedure, "i"));
    await choose(page, "select-inmueble-gestion", new RegExp(def.propertyAddress, "i"));
    await saveDialog(page);
    await expect(page.getByRole("table")).toContainText(def.managementNumber);
  });

  let deedId = "";
  await test.step(`[Caso ${def.label}] Deed y firma`, async () => {
    await go(page, "/dashboard/deeds");
    await page.getByRole("button", { name: /nueva escritura/i }).click();
    await page.getByRole("dialog").getByLabel(/número/i).fill(def.deedNumber);
    await page.getByRole("dialog").getByLabel(/date/i).fill("2026-08-05");
    await choose(page, "select-folio-escritura", new RegExp(def.tipoFolio, "i"));
    await page
      .getByRole("dialog")
      .getByLabel(/observaciones/i)
      .fill("Numeración no correlativa: seed de datos E2E aislado (CU86)");
    await saveDialog(page);

    await page.getByTestId("input-search-deed").fill(def.deedNumber);
    const row = page.getByRole("row").filter({ hasText: def.deedNumber });
    deedId = ((await row.locator("td").first().innerText()) ?? "").trim();
    await row.getByTestId(`btn-sign-deed-${deedId}`).click();
    await page.getByRole("button", { name: /sign escritura/i }).click();
    await expect(page.getByRole("dialog")).toBeHidden();
    await pause(page);
  });

  await test.step(`[Caso ${def.label}] Testimony, verificación y copia`, async () => {
    await go(page, "/dashboard/testimonies");
    await page.getByTestId("btn-generate-testimony").click();
    await choose(page, "select-escritura-testimonio", new RegExp(def.deedNumber, "i"));
    await page.getByTestId("btn-save-testimony").click();
    await expect(page.getByRole("dialog")).toBeHidden();
    await pause(page);

    const row = page.getByRole("row").filter({ hasText: def.deedNumber });
    const testimonyId = ((await row.locator("td").first().innerText()) ?? "").trim();
    await row.getByTestId(`btn-verify-testimony-${testimonyId}`).click();
    await page.getByTestId("btn-confirm-verify-testimony").click();
    await pause(page);

    await go(page, "/dashboard/copies");
    await page.getByRole("button", { name: /nueva copia/i }).click();
    await page.getByRole("dialog").getByLabel(/número/i).fill(def.copiaNumber);
    await saveDialog(page);
  });

  await test.step(`[Caso ${def.label}] Documento presentado`, async () => {
    await go(page, "/dashboard/documents");
    await page.getByRole("button", { name: /nuevo documento/i }).click();
    await chooseInDialogCombobox(page, new RegExp(def.typeDocument, "i"), "select-tipo-documento");
    // The document is presented for the case's procedure (CU04, #655).
    await chooseInDialogCombobox(page, new RegExp(`^${def.managementNumber} `), "select-gestion-documento");
    await chooseFirst(page, "select-tramite-documento");
    await page.getByRole("dialog").getByLabel(/date/i).fill("2026-08-06");
    await page.getByRole("dialog").getByLabel(/document entregado/i).click();
    await saveDialog(page);
  });

  await test.step(`[Caso ${def.label}] Payment`, async () => {
    await go(page, "/dashboard/payments");
    await page.getByTestId("btn-new-payment").click();
    await choose(page, "select-presupuesto-pago", new RegExp(def.clientApellido, "i"));
    await page.getByRole("dialog").getByLabel(/date/i).fill("2026-08-07");
    await page.getByRole("dialog").getByLabel(/monto/i).fill(def.paymentAmount);
    await saveDialog(page);
  });
}

test.describe("Demo seed — two full comparable cases", () => {
  test.setTimeout(15 * 60 * 1000);

  test("seeds Caso A and Caso B end to end through the UI", async ({ page }) => {
    await test.step("Sign in", async () => {
      await page.goto("/login");
      await page.getByTestId("input-username").fill(adminUser);
      await page.getByTestId("input-password").fill(adminPassword);
      await page.getByTestId("btn-sign-in").click();
      await expect(page).toHaveURL(/\/dashboard/);
    });

    const caseA = buildCaseDefinition("A", runId);
    const caseB = buildCaseDefinition("B", runId);

    await buildFullCase(page, caseA);
    await buildFullCase(page, caseB);

    await test.step("Both cases visible on dashboard", async () => {
      await go(page, "/dashboard/managements");
      await expect(page.getByRole("table")).toContainText(caseA.managementNumber);
      await expect(page.getByRole("table")).toContainText(caseB.managementNumber);
    });
  });
});
