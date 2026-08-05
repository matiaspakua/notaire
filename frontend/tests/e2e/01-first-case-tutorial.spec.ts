/**
 * First case tutorial — a recording-ready, beginner-friendly Playwright walk-through.
 *
 * The test deliberately performs every learner-visible action in the browser.  Run it
 * slowly, with a visible browser, when recording a demonstration:
 *
 *   HEADED=1 SLOW_MO=700 TUTORIAL_PAUSE_MS=1400 \
 *     npx playwright test 01-first-case-tutorial --project=chromium
 *
 * It creates uniquely named records, so repeat runs never confuse a learner with a
 * previous example.  Global teardown preserves the normal isolated E2E environment.
 */
import { expect, test, type Page } from "@playwright/test";

const adminUser = process.env.E2E_TEST_ADMIN_USER ?? "admin";
const adminPassword = process.env.E2E_TEST_ADMIN_PASS ?? "admin";
const pauseMs = Number(process.env.TUTORIAL_PAUSE_MS ?? 900);
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
  // Radix Select places options in a portal viewport that can be clipped in a
  // headed recording window. Its native click handler still represents the same
  // user selection and avoids coupling the tutorial to a window size.
  await choice.evaluate((element) => (element as HTMLElement).click());
  await pause(page);
}

async function chooseFirst(page: Page, triggerTestId: string): Promise<void> {
  await page.getByTestId(triggerTestId).click();
  await pause(page, 0.5);
  await page.getByRole("option").first().evaluate((element) => (element as HTMLElement).click());
  await pause(page);
}

async function chooseLast(page: Page, triggerTestId: string): Promise<void> {
  await page.getByTestId(triggerTestId).click();
  await pause(page, 0.5);
  await page.getByRole("option").last().evaluate((element) => (element as HTMLElement).click());
  await pause(page);
}

async function saveDialog(page: Page): Promise<void> {
  await page.getByRole("dialog").getByRole("button", { name: /guardar|crear/i }).click();
  await expect(page.getByRole("dialog")).toBeHidden();
  await pause(page);
}

test.describe("First case tutorial — from setup to a notarial case", () => {
  test.setTimeout(20 * 60 * 1000);

  test("CU02 management form requires the case dependencies", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("input-usuario").fill(adminUser);
    await page.getByTestId("input-contrasenia").fill(adminPassword);
    await page.getByTestId("btn-ingresar").click();
    await expect(page).toHaveURL(/\/dashboard/);
    await page.goto("/dashboard/gestiones");
    await page.getByTestId("btn-nueva-gestion").click();

    await expect(page.getByTestId("select-presupuesto-gestion")).toBeVisible();
    await expect(page.getByTestId("select-escribano-gestion")).toBeVisible();
    await expect(page.getByTestId("select-estado-gestion")).toBeVisible();
    await expect(page.getByTestId("select-tipo-tramite-gestion")).toBeVisible();
  });

  test("a new user can set up and follow a complete first case", async ({ page }) => {
    const tipoTramite = `Compraventa tutorial ${runId}`;
    const concepto = `Honorarios tutorial ${runId}`;
    const tipoFolio = `Protocolo tutorial ${runId}`;
    const cliente = `Ana Tutorial ${runId}`;
    const gestion = `900${runId}`;

    await test.step("1. Sign in — the dashboard is the starting point", async () => {
      await page.goto("/login");
      await page.getByTestId("input-usuario").fill(adminUser);
      await pause(page, 0.5);
      await page.getByTestId("input-contrasenia").fill(adminPassword);
      await pause(page, 0.5);
      await page.getByTestId("btn-ingresar").click();
      await expect(page).toHaveURL(/\/dashboard/);
      await expect(page.getByTestId("sidebar")).toBeVisible();
      await pause(page, 1.5);
    });

    await test.step("2. Define the task type — what work the office will perform", async () => {
      await go(page, "/dashboard/administracion/tramites");
      await page.getByTestId("btn-nuevo-tipo-tramite").click();
      await page.getByTestId("input-nombre-tramite").fill(tipoTramite);
      await pause(page);
      await saveDialog(page);
      await page.getByTestId("input-search-tramite").fill(tipoTramite);
      await expect(page.getByRole("table")).toContainText(tipoTramite);
      await pause(page);
    });

    await test.step("3. Add a charge concept — the building block for quotes", async () => {
      await go(page, "/dashboard/administracion/conceptos");
      await page.getByRole("button", { name: /nuevo concepto/i }).click();
      await page.getByTestId("input-nombre-concepto").fill(concepto);
      await page.getByRole("dialog").getByRole("spinbutton").fill("25000");
      await pause(page);
      await saveDialog(page);
      await page.getByTestId("input-search-concepto").fill(concepto);
      await expect(page.getByRole("table")).toContainText(concepto);
      await pause(page);
    });

    await test.step("4. Prepare the protocol — create a folio type and a folio", async () => {
      await go(page, "/dashboard/administracion/folios");
      await page.getByTestId("btn-nuevo-tipo-folio").click();
      await page.getByTestId("input-nombre-tipo-folio").fill(tipoFolio);
      await pause(page);
      await page.getByTestId("btn-save-tipo-folio").click();
      await expect(page.getByRole("dialog")).toBeHidden();
      await pause(page);

      await page.getByTestId("btn-nuevo-folio").click();
      await page.getByTestId("input-numero-folio").fill(`8${runId}`);
      await choose(page, "select-estado-folio", /nuevo/i);
      await choose(page, "select-tipo-folio", new RegExp(tipoFolio, "i"));
      // The office's existing notary is selected from the live list; this makes the
      // tutorial usable even when no tutorial user has an Escribano role.
      await chooseFirst(page, "select-escribano-folio");
      await pause(page);
      await saveDialog(page);
    });

    await test.step("5. Register the client — the person behind the case", async () => {
      await go(page, "/dashboard/personas");
      await page.getByTestId("btn-nueva-persona").click();
      await page.getByTestId("input-nombre").fill("Ana");
      await page.getByTestId("input-apellido").fill(`Tutorial ${runId}`);
      await page.getByLabel(/email/i).fill(`ana.tutorial.${runId}@notaire.test`);
      await page.getByTestId("check-es-cliente").click();
      await pause(page);
      await saveDialog(page);
      await page.getByTestId("input-search-nombre").fill("Ana");
      await expect(page.getByRole("table")).toContainText(cliente);
      await pause(page);
    });

    await test.step("6. Create the quote — link the proposed amount to the client", async () => {
      await go(page, "/dashboard/presupuestos");
      await page.getByTestId("btn-nuevo-presupuesto").click();
      await choose(page, "select-persona", new RegExp(cliente, "i"));
      await page.getByRole("dialog").getByLabel(/fecha/i).fill("2026-08-04");
      await page.getByTestId("input-monto").fill("25000");
      await pause(page);
      await saveDialog(page);
      await page.getByTestId("input-search-presupuesto").fill("Ana");
      await pause(page);
    });

    await test.step("7. Open the case — link it to the quote, notary, state and task type", async () => {
      await go(page, "/dashboard/gestiones");
      await page.getByTestId("btn-nueva-gestion").click();
      await page.getByTestId("input-numero-gestion").fill(gestion);
      await pause(page);
      // The quote just created is the newest entry offered by the select.
      await chooseLast(page, "select-presupuesto-gestion");
      await chooseFirst(page, "select-escribano-gestion");
      await chooseFirst(page, "select-estado-gestion");
      await choose(page, "select-tipo-tramite-gestion", new RegExp(tipoTramite, "i"));
      await saveDialog(page);
      await expect(page.getByRole("table")).toContainText(gestion);
      await pause(page);
    });

    await test.step("8. Tour the remaining work areas — records, documents, payments and reporting", async () => {
      for (const path of [
        "/dashboard/escrituras", "/dashboard/documentos", "/dashboard/pagos",
        "/dashboard/items", "/dashboard/inmuebles", "/dashboard/copias",
        "/dashboard/protocolo", "/dashboard/suplencias", "/dashboard/reportes",
        "/dashboard/auditoria", "/dashboard/administracion/usuarios",
        "/dashboard/administracion/roles", "/dashboard/administracion/estados-gestion",
        "/dashboard/administracion/plantillas", "/dashboard/administracion/documentos",
        "/dashboard/administracion/workflows",
      ]) {
        await go(page, path);
      }
    });

    await test.step("9. Finish safely — log out", async () => {
      await go(page, "/dashboard");
      await page.getByTestId("btn-logout").click();
      await expect(page).toHaveURL(/\/login/);
      await pause(page);
    });
  });
});
