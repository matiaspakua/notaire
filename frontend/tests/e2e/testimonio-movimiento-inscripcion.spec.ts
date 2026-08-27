/**
 * CU11/CU12/CU44 - Circuito de inscripción de testimonio (issue #832)
 *
 * Golden path: ingresar para inscripción → registrar inscripción → retirar.
 * Edge path: reingresar sin retiro previo (bloqueado, per design.md —
 * Playwright Strategy). Testimonios are seeded already "Firmada"/generado/
 * verificado directly via the generic CRUD endpoints (design.md — Non-Goals:
 * these endpoints stay open) so each test starts from the exact movimiento
 * state it exercises.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { apiPost, uniqueId } from "./setup/api-helpers";

async function seedTestimonioVerificado(page: import("@playwright/test").Page): Promise<{ idTestimonio: number; numero: number }> {
  const escrituraNumero = uniqueId() % 1_000_000;
  const escritura = await apiPost<{ idEscritura: number }>(page, "/escrituras", {
    numero: escrituraNumero,
    fechaEscrituracion: new Date().toISOString().split("T")[0],
    cuerpo: `Contenido E2E ${escrituraNumero}`,
    estado: "Firmada",
  });
  const numero = uniqueId() % 1_000_000;
  // DtoEscritura.numero is primitive int — must be included in nested object to avoid 400
  // (see TestimonioControllerIntegrationTest#testimonioBody).
  const testimonio = await apiPost<{ idTestimonio: number }>(page, "/testimonio", {
    escritura: { idEscritura: escritura.data!.idEscritura, numero: escrituraNumero },
    numero,
    observado: false,
    verificado: true,
  });
  return { idTestimonio: testimonio.data!.idTestimonio, numero };
}

test.describe("CU11/CU12/CU44 - Movimientos de testimonio", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("Golden path: ingresar, registrar inscripción y retirar", async ({ page }) => {
    const { idTestimonio, numero } = await seedTestimonioVerificado(page);

    await steps.givenUserIsOnPage("/dashboard/movimientos-testimonio");
    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-ingresar-testimonio-${idTestimonio}`).click();
    await steps.thenShowsSuccessMessage("ingresado");
    await expect(row).toContainText(/ingresado/i);

    await page.getByTestId(`btn-registrar-inscripcion-${idTestimonio}`).click();
    await steps.thenShowsSuccessMessage("inscripción registrada");
    await expect(row).toContainText(/inscripto/i);

    await page.getByTestId(`btn-retirar-testimonio-${idTestimonio}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-numero-carton").fill("123");
    await page.getByTestId("btn-confirmar-retiro").click();
    await steps.thenShowsSuccessMessage("retirado");
    await expect(row).toContainText(/retirado/i);
  });

  test("Edge: reingresar solo está disponible después de un retiro previo", async ({ page }) => {
    const { idTestimonio, numero } = await seedTestimonioVerificado(page);

    await steps.givenUserIsOnPage("/dashboard/movimientos-testimonio");
    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await expect(page.getByTestId(`btn-reingresar-testimonio-${idTestimonio}`)).toHaveCount(0);
    await expect(page.getByTestId(`btn-ingresar-testimonio-${idTestimonio}`)).toBeVisible();

    await page.getByTestId(`btn-ingresar-testimonio-${idTestimonio}`).click();
    await steps.thenShowsSuccessMessage("ingresado");
    await page.getByTestId(`btn-registrar-inscripcion-${idTestimonio}`).click();
    await steps.thenShowsSuccessMessage("inscripción registrada");
    await page.getByTestId(`btn-retirar-testimonio-${idTestimonio}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-numero-carton").fill("456");
    await page.getByTestId("btn-confirmar-retiro").click();
    await steps.thenShowsSuccessMessage("retirado");

    await expect(page.getByTestId(`btn-reingresar-testimonio-${idTestimonio}`)).toBeVisible();
    await page.getByTestId(`btn-reingresar-testimonio-${idTestimonio}`).click();
    await steps.thenShowsSuccessMessage("reingresado");
    await expect(row).toContainText(/ingresado/i);
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`movimientos-testimonio screen has no horizontal overflow at ${viewport.label}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await steps.givenUserIsOnPage("/dashboard/movimientos-testimonio");
      await page.waitForLoadState("networkidle");
      await steps.thenHasNoHorizontalOverflow();
    });
  }
});
