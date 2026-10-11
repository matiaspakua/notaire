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

test.describe.configure({ timeout: 300_000 });

const adminUser = process.env.E2E_TEST_ADMIN_USER ?? "admin";
const adminPassword = process.env.E2E_TEST_ADMIN_PASS ?? "admin";
const pauseMs = Number(process.env.TUTORIAL_PAUSE_MS ?? 900);
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
  // Radix Select places options in a portal viewport that can be clipped in a
  // headed recording window. Its native click handler still represents the same
  // user selection and avoids coupling the tutorial to a window size.
  await choice.evaluate((element) => (element as HTMLElement).click());
  await pause(page);
}

async function chooseFirst(page: Page, triggerTestId: string): Promise<void> {
  await page.getByTestId(triggerTestId).click();
  await pause(page, 0.5);
  await page.getByRole("listbox").getByRole("option").first().evaluate((element) => (element as HTMLElement).click());
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
    await page.getByTestId("input-username").fill(adminUser);
    await page.getByTestId("input-password").fill(adminPassword);
    await page.getByTestId("btn-sign-in").click();
    await expect(page).toHaveURL(/\/dashboard/);
    await page.goto("/dashboard/managements");
    await page.getByTestId("btn-new-management").click();

    await expect(page.getByTestId("select-budget-management")).toBeVisible();
    await expect(page.getByTestId("select-notary-management")).toBeVisible();
    await expect(page.getByTestId("select-status-management")).toBeVisible();
    await expect(page.getByTestId("select-procedure-type-management")).toBeVisible();
  });

  test("a new user can set up and follow a complete first case", async ({ page }) => {
    const typeProcedure = `Compraventa tutorial ${runId}`;
    const concept = `Honorarios tutorial ${runId}`;
    const tipoFolio = `Protocolo tutorial ${runId}`;
    const client = `Ana Tutorial ${runId}`;
    const management = `900${runId}`;

    await test.step("1. Sign in — the dashboard is the starting point", async () => {
      await page.goto("/login");
      await page.getByTestId("input-username").fill(adminUser);
      await pause(page, 0.5);
      await page.getByTestId("input-password").fill(adminPassword);
      await pause(page, 0.5);
      await page.getByTestId("btn-sign-in").click();
      await expect(page).toHaveURL(/\/dashboard/);
      await expect(page.getByTestId("sidebar")).toBeVisible();
      await pause(page, 1.5);
    });

    await test.step("2. Define the task type — what work the office will perform", async () => {
      await go(page, "/dashboard/administration/procedures");
      await page.getByTestId("btn-new-procedure-type").click();
      await page.getByTestId("input-name-procedure").fill(typeProcedure);
      await pause(page);
      await saveDialog(page);
      await page.getByTestId("input-search-procedure").fill(typeProcedure);
      await expect(page.getByRole("table")).toContainText(typeProcedure);
      await pause(page);
    });

    await test.step("3. Add a charge concept — the building block for quotes", async () => {
      await go(page, "/dashboard/administration/concepts");
      await page.getByRole("button", { name: /nuevo concepto/i }).click();
      await page.getByTestId("input-name-concept").fill(concept);
      await page.getByRole("dialog").getByRole("spinbutton").fill("25000");
      await pause(page);
      await saveDialog(page);
      await page.getByTestId("input-search-concept").fill(concept);
      await expect(page.getByRole("table")).toContainText(concept);
      await pause(page);
    });

    await test.step("4. Prepare the protocol — create a folio type and a folio", async () => {
      await go(page, "/dashboard/administration/folios");
      await page.getByTestId("btn-new-folio-type").click();
      await page.getByTestId("input-name-folio-type").fill(tipoFolio);
      await pause(page);
      await page.getByTestId("btn-save-folio-type").click();
      await expect(page.getByRole("dialog")).toBeHidden();
      await pause(page);

      await page.getByTestId("btn-new-folio").click();
      await page.getByTestId("input-folio-number").fill(`8${runId}`);
      await choose(page, "select-estado-folio", /nuevo/i);
      await choose(page, "select-tipo-folio", new RegExp(tipoFolio, "i"));
      // The office's existing notary is selected from the live list; this makes the
      // tutorial usable even when no tutorial user has an Escribano role.
      await chooseFirst(page, "select-escribano-folio");
      await pause(page);
      await saveDialog(page);
    });

    await test.step("5. Register the client — the person behind the case", async () => {
      await go(page, "/dashboard/people");
      await page.getByTestId("btn-new-person").click();
      await page.getByTestId("input-firstName").fill("Ana");
      await page.getByTestId("input-lastName").fill(`Tutorial ${runId}`);
      await page.getByRole("dialog").getByTestId("input-dni").fill(`DNI${runId}`);
      await page.getByLabel(/email/i).fill(`ana.tutorial.${runId}@notaire.test`);
      await page.getByTestId("check-es-client").click();
      await pause(page);
      await saveDialog(page);
      await page.getByTestId("input-search-firstName").fill("Ana");
      await expect(page.getByRole("table")).toContainText(client);
      await pause(page);
    });

    await test.step("6. Create the quote — link the proposed amount to the client", async () => {
      await go(page, "/dashboard/budgets");
      await page.getByTestId("btn-new-budget").click();
      await choose(page, "select-persona", new RegExp(client, "i"));
      await page.getByRole("dialog").getByLabel(/date/i).fill("2026-08-04");
      await page.getByTestId("input-amount").fill("25000");
      await pause(page);
      await saveDialog(page);
      await page.getByTestId("input-search-budget").fill("Ana");
      await pause(page);
    });

    await test.step("7. Open the case — link it to the quote, notary, state and task type", async () => {
      await go(page, "/dashboard/managements");
      await page.getByTestId("btn-new-management").click();
      await page.getByTestId("input-management-number").fill(management);
      await pause(page);
      // Budget options are labeled with the client's name, so the tutorial links the
      // case to its own quote rather than relying on list order.
      await choose(page, "select-presupuesto-gestion", new RegExp(client, "i"));
      await chooseFirst(page, "select-escribano-gestion");
      await chooseFirst(page, "select-estado-gestion");
      await choose(page, "select-tipo-tramite-gestion", new RegExp(typeProcedure, "i"));
      await saveDialog(page);
      await expect(page.getByRole("table")).toContainText(management);
      await pause(page);
    });

    await test.step("8. Tour the remaining work areas — records, documents, payments and reporting", async () => {
      for (const path of [
        "/dashboard/deeds", "/dashboard/documents", "/dashboard/payments",
        "/dashboard/items", "/dashboard/properties", "/dashboard/copies",
        "/dashboard/protocol", "/dashboard/substitutions", "/dashboard/reports",
        "/dashboard/audit", "/dashboard/administration/users",
        "/dashboard/administration/roles", "/dashboard/administration/management-statuses",
        "/dashboard/administration/templates", "/dashboard/administration/documents",
        "/dashboard/administration/workflows",
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
