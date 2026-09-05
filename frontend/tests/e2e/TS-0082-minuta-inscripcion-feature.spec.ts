/**
 * CU82 - Generar y hacer seguimiento de la Minuta de Inscripción (issue #839)
 *
 * Golden path: generar minuta desde una escritura firmada con inmueble con
 * datos catastrales/registrales completos → presentar ante el Registro →
 * inscribir en forma definitiva. Edge paths: observar (subsanación) tras
 * presentar; generar bloqueado cuando faltan datos catastrales/registrales.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { apiPost, uniqueId } from "./setup/api-helpers";

interface EscrituraApiResult {
  idEscritura: number;
}

interface InmuebleApiResult {
  idInmueble: number;
}

async function seedEscrituraFirmada(page: import("@playwright/test").Page): Promise<{ idEscritura: number; numero: number }> {
  const numero = uniqueId() % 1_000_000;
  const seeded = await apiPost<EscrituraApiResult>(page, "/escrituras", {
    numero,
    fechaEscrituracion: new Date().toISOString().split("T")[0],
    cuerpo: `Contenido E2E ${numero}`,
    estado: "Firmada",
  });
  return { idEscritura: seeded.data!.idEscritura, numero };
}

async function seedInmueble(
  page: import("@playwright/test").Page,
  overrides: Record<string, unknown> = {},
): Promise<number> {
  const seeded = await apiPost<InmuebleApiResult>(page, "/inmueble", {
    nomenclaturaCatastral: `NC-${uniqueId()}`,
    domicilio: "Av. Siempreviva 742",
    valuacionFiscal: 100000,
    matricula: `MAT-${uniqueId()}`,
    tomoFolioFinca: "Tomo 1 Folio 2",
    linderos: "Norte, Sur, Este, Oeste",
    ...overrides,
  });
  return seeded.data!.idInmueble;
}

async function seedTramite(
  page: import("@playwright/test").Page,
  idEscritura: number,
  idInmueble: number,
): Promise<void> {
  await apiPost(page, "/tramites", {
    fkIdTipoTramite: { idTipoTramite: 1 },
    fkIdEscritura: { idEscritura },
    fkIdInmueble: { idInmueble },
  });
}

test.describe("CU82 - Generar y hacer seguimiento de la minuta de inscripción", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("Golden path: generar minuta, presentar e inscribir en forma definitiva", async ({ page }) => {
    const { idEscritura, numero } = await seedEscrituraFirmada(page);
    const idInmueble = await seedInmueble(page);
    await seedTramite(page, idEscritura, idInmueble);

    await steps.givenUserIsOnPage("/dashboard/minutas-inscripcion");
    await page.getByTestId("select-escritura-minuta").click();
    await page.getByRole("option", { name: new RegExp(String(numero)) }).click();
    await page.getByTestId("btn-generar-minuta").click();
    await steps.thenShowsSuccessMessage("generada");

    await expect(page.getByTestId("minuta-estado")).toHaveText(/Generada/i);

    await page.getByTestId("btn-presentar-minuta").click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-fecha-presentacion").fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("input-numero-entrada-registral").fill(`ENT-${uniqueId()}`);
    await page.getByTestId("btn-confirmar-presentar").click();
    await steps.thenShowsSuccessMessage("Presentación");
    await steps.thenModalIsNotVisible();

    await expect(page.getByTestId("minuta-estado")).toHaveText(/Presentado para inscripción/i);

    await page.getByTestId("btn-inscribir-minuta").click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-fecha-recepcion").fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("input-numero-inscripcion-definitivo").fill(`INS-${uniqueId()}`);
    await page.getByTestId("btn-confirmar-inscribir").click();
    await steps.thenShowsSuccessMessage("definitiva");
    await steps.thenModalIsNotVisible();

    await expect(page.getByTestId("minuta-estado")).toHaveText(/Inscripto/i);
  });

  test("Edge: observar tras presentar muestra la minuta como Observado", async ({ page }) => {
    const { idEscritura, numero } = await seedEscrituraFirmada(page);
    const idInmueble = await seedInmueble(page);
    await seedTramite(page, idEscritura, idInmueble);

    await steps.givenUserIsOnPage("/dashboard/minutas-inscripcion");
    await page.getByTestId("select-escritura-minuta").click();
    await page.getByRole("option", { name: new RegExp(String(numero)) }).click();
    await page.getByTestId("btn-generar-minuta").click();
    await steps.thenShowsSuccessMessage("generada");

    await page.getByTestId("btn-presentar-minuta").click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-fecha-presentacion").fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("input-numero-entrada-registral").fill(`ENT-${uniqueId()}`);
    await page.getByTestId("btn-confirmar-presentar").click();
    await steps.thenShowsSuccessMessage("Presentación");

    await page.getByTestId("btn-observar-minuta").click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-observaciones-registro").fill("Falta certificado catastral");
    await page.getByTestId("input-fecha-subsanacion").fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("btn-confirmar-observar").click();
    await steps.thenShowsSuccessMessage("Observación");

    await expect(page.getByTestId("minuta-estado")).toHaveText(/Observado/i);
  });

  test("Edge: generar está bloqueado cuando el inmueble no tiene datos catastrales/registrales completos", async ({
    page,
  }) => {
    const { idEscritura, numero } = await seedEscrituraFirmada(page);
    const idInmueble = await seedInmueble(page, { matricula: null, tomoFolioFinca: null, linderos: null });
    await seedTramite(page, idEscritura, idInmueble);

    await steps.givenUserIsOnPage("/dashboard/minutas-inscripcion");
    await page.getByTestId("select-escritura-minuta").click();
    await page.getByRole("option", { name: new RegExp(String(numero)) }).click();
    await page.getByTestId("btn-generar-minuta").click();

    await steps.thenShowsErrorMessage("catastrales");
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`minutas-inscripcion screen has no horizontal overflow at ${viewport.label}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await steps.givenUserIsOnPage("/dashboard/minutas-inscripcion");
      await page.waitForLoadState("networkidle");
      await steps.thenHasNoHorizontalOverflow();
    });
  }
});
