/**
 * CU82 - Generar y hacer seguimiento de la Minuta de Inscripción (issue #839)
 *
 * Golden path: generar registrationDraft desde una deed firmada con property con
 * datos catastrales/registrales completos → presentar ante el Registro →
 * inscribir en forma definitiva. Edge paths: observar (subsanación) tras
 * presentar; generar bloqueado cuando faltan datos catastrales/registrales.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { apiPost, uniqueId } from "./setup/api-helpers";

interface DeedApiResult {
  idDeed: number;
}

interface PropertyApiResult {
  idProperty: number;
}

async function seedSignedDeed(page: import("@playwright/test").Page): Promise<{ idDeed: number; number: number }> {
  const number = uniqueId() % 1_000_000;
  const seeded = await apiPost<DeedApiResult>(page, "/deeds", {
    number: number,
    dateDeedrecording: new Date().toISOString().split("T")[0],
    body: `Contenido E2E ${number}`,
    status: "Firmada",
  });
  return { idDeed: seeded.data!.idDeed, number };
}

async function seedProperty(
  page: import("@playwright/test").Page,
  overrides: Record<string, unknown> = {},
): Promise<number> {
  const seeded = await apiPost<PropertyApiResult>(page, "/properties", {
    cadastralDesignation: `NC-${uniqueId()}`,
    address: "Av. Siempreviva 742",
    fiscalAppraisal: 100000,
    registrationNumber: `MAT-${uniqueId()}`,
    volumeFolioLandRecord: "Tomo 1 Folio 2",
    boundaries: "Norte, Sur, Este, Oeste",
    ...overrides,
  });
  return seeded.data!.idProperty;
}

async function seedProcedure(
  page: import("@playwright/test").Page,
  idDeed: number,
  idProperty: number,
): Promise<void> {
  await apiPost(page, "/procedures", {
    idProcedureType: 1,
    idDeed: idDeed,
    idProperty: idProperty,
  });
}

test.describe("CU82 - Generate and track the registration draft", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("Golden path: generate the draft, submit it, and register it definitively", async ({ page }) => {
    const { idDeed, number } = await seedSignedDeed(page);
    const idProperty = await seedProperty(page);
    await seedProcedure(page, idDeed, idProperty);

    await steps.givenUserIsOnPage("/dashboard/registration-drafts");
    await page.getByTestId("select-deed-registration-draft").click();
    await page.getByRole("option", { name: new RegExp(String(number)) }).click();
    await page.getByTestId("btn-generate-registration-draft").click();
    await steps.thenShowsSuccessMessage("generada");

    await expect(page.getByTestId("registration-draft-status")).toHaveText(/Generada/i);

    await page.getByTestId("btn-submit-registration-draft").click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-submission-date").fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("input-registry-entry-number").fill(`ENT-${uniqueId()}`);
    await page.getByTestId("btn-confirm-submit").click();
    await steps.thenShowsSuccessMessage("Presentación");
    await steps.thenModalIsNotVisible();

    await expect(page.getByTestId("registration-draft-status")).toHaveText(/Presentado para inscripción/i);

    await page.getByTestId("btn-register-registration-draft").click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-receipt-date").fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("input-final-registration-number").fill(`INS-${uniqueId()}`);
    await page.getByTestId("btn-confirm-register").click();
    await steps.thenShowsSuccessMessage("definitiva");
    await steps.thenModalIsNotVisible();

    await expect(page.getByTestId("registration-draft-status")).toHaveText(/Inscripto/i);
  });

  test("Edge: flagging after submission shows the draft as Observado", async ({ page }) => {
    const { idDeed, number } = await seedSignedDeed(page);
    const idProperty = await seedProperty(page);
    await seedProcedure(page, idDeed, idProperty);

    await steps.givenUserIsOnPage("/dashboard/registration-drafts");
    await page.getByTestId("select-deed-registration-draft").click();
    await page.getByRole("option", { name: new RegExp(String(number)) }).click();
    await page.getByTestId("btn-generate-registration-draft").click();
    await steps.thenShowsSuccessMessage("generada");

    await page.getByTestId("btn-submit-registration-draft").click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-submission-date").fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("input-registry-entry-number").fill(`ENT-${uniqueId()}`);
    await page.getByTestId("btn-confirm-submit").click();
    await steps.thenShowsSuccessMessage("Presentación");

    await page.getByTestId("btn-observe-registration-draft").click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-notes-registro").fill("Falta certificado catastral");
    await page.getByTestId("input-correction-date").fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("btn-confirm-observe").click();
    await steps.thenShowsSuccessMessage("Observación");

    await expect(page.getByTestId("registration-draft-status")).toHaveText(/Observado/i);
  });

  test("Edge: generation is blocked when the property lacks complete cadastral and registry data", async ({
    page,
  }) => {
    const { idDeed, number } = await seedSignedDeed(page);
    const idProperty = await seedProperty(page, {
      registrationNumber: null,
      volumeFolioLandRecord: null,
      boundaries: null,
    });
    await seedProcedure(page, idDeed, idProperty);

    await steps.givenUserIsOnPage("/dashboard/registration-drafts");
    await page.getByTestId("select-deed-registration-draft").click();
    await page.getByRole("option", { name: new RegExp(String(number)) }).click();
    await page.getByTestId("btn-generate-registration-draft").click();

    await steps.thenShowsErrorMessage("catastrales");
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`the registration drafts screen has no horizontal overflow at ${viewport.label}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await steps.givenUserIsOnPage("/dashboard/registration-drafts");
      await page.waitForLoadState("networkidle");
      await steps.thenHasNoHorizontalOverflow();
    });
  }
});
