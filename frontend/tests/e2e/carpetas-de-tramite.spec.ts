/**
 * E2E tests — CU85 Administrar Carpetas de Trámite.
 *
 * A carpeta is auto-generated (estado "Activa") whenever a gestión's trámite is
 * created, and is cascade-archived when the gestión itself is archived (#839).
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import {
  apiGet,
  createPersona,
  createPresupuesto,
  createEstadoGestion,
  createTipoTramite,
  createWorkflowDefinition,
  createWorkflowNode,
  createWorkflowTransition,
  assignWorkflowToTipoTramite,
  createCompleteCaseGestion,
} from "./setup/api-helpers";

/**
 * "Archivada" is a global, singleton estado (looked up by name in
 * GestionTransitionService) — reuse the existing row instead of creating a
 * duplicate, which would make that lookup ambiguous.
 */
async function findEstadoArchivada(page: Page): Promise<{ idEstadoGestion: number; nombre: string }> {
  const result = await apiGet<{ idEstadoGestion: number; nombre: string }[]>(
    page,
    "/estado-gestion/search?nombre=Archivada",
  );
  const match = result.data!.find((e) => e.nombre === "Archivada");
  if (!match) {
    throw new Error("Estado 'Archivada' not found — expected to be seeded by default data");
  }
  return match;
}

/** Seeds a gestión whose workflow allows a direct transition to "Archivada". */
async function seedArchivableGestion(page: Page) {
  const persona = await createPersona(page);
  const presupuesto = await createPresupuesto(page, persona.data!.idPersona);

  const estadoInicial = await createEstadoGestion(page);
  const estadoArchivada = await findEstadoArchivada(page);
  const workflow = await createWorkflowDefinition(page);
  const workflowId = workflow.data!.id;

  const nodoInicial = await createWorkflowNode(
    page,
    workflowId,
    estadoInicial.data!.idEstadoGestion,
    "INITIAL",
  );
  const nodoArchivada = await createWorkflowNode(
    page,
    workflowId,
    estadoArchivada.idEstadoGestion,
    "FINAL",
  );
  await createWorkflowTransition(page, workflowId, nodoInicial.data!.id, nodoArchivada.data!.id);

  const tipoTramite = await createTipoTramite(page);
  await assignWorkflowToTipoTramite(page, tipoTramite.data!.idTipoDeTramite, workflowId);

  const gestion = await createCompleteCaseGestion(page, {
    presupuestoId: presupuesto.data!.idPresupuesto,
    tipoTramiteId: tipoTramite.data!.idTipoDeTramite,
    estadoGestionId: estadoInicial.data!.idEstadoGestion,
  });

  return { idGestion: gestion.data!.idGestion, numero: gestion.data!.numero };
}

test.describe("CU85 - Administrar Carpetas de Trámite", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: iniciar trámite genera carpeta activa, se pone en espera y se archiva con la gestión", async ({
    page,
  }) => {
    const { idGestion, numero } = await seedArchivableGestion(page);

    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-ver-carpetas-${idGestion}`).click();
    let dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    const item = dialog.getByTestId("carpeta-item").first();
    await expect(item).toContainText("Activa");

    await item.getByTestId(/^btn-poner-en-espera-/).click();
    dialog = page.getByRole("dialog");
    await dialog.getByTestId("input-motivo-espera").fill("Falta documentación del titular");
    await dialog.getByTestId("btn-confirmar-espera").click();
    await expect(dialog).not.toBeVisible();

    await page.getByTestId(`btn-ver-carpetas-${idGestion}`).click();
    dialog = page.getByRole("dialog");
    await expect(dialog.getByTestId("carpeta-item").first()).toContainText("Espera");
    await page.keyboard.press("Escape");

    await page.getByTestId(`btn-archivar-gestion-${idGestion}`).click();
    const confirmDialog = page.getByRole("alertdialog");
    await expect(confirmDialog).toBeVisible();
    await confirmDialog.getByRole("button", { name: /archivar gestión/i }).click();
    await expect(confirmDialog).toContainText(/espera/i);
    await confirmDialog.getByRole("button", { name: /archivar de todos modos/i }).click();
    await expect(confirmDialog).not.toBeVisible();

    await page.getByTestId(`btn-ver-carpetas-${idGestion}`).click();
    dialog = page.getByRole("dialog");
    await expect(dialog.getByTestId("carpeta-item").first()).toContainText("Archivada");
  });

  test("edge path: poner en espera sin motivo muestra un error visible", async ({ page }) => {
    const { idGestion, numero } = await seedArchivableGestion(page);

    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-ver-carpetas-${idGestion}`).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId(/^btn-poner-en-espera-/).click();
    const esperaDialog = page.getByRole("dialog");
    await esperaDialog.getByTestId("btn-confirmar-espera").click();

    await expect(page.getByText(/motivo es obligatorio/i)).toBeVisible({ timeout: 10000 });
    await expect(esperaDialog).toBeVisible();
  });

  test("edge path: archivar una gestión con carpeta en espera exige confirmación explícita", async ({
    page,
  }) => {
    const { idGestion, numero } = await seedArchivableGestion(page);

    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-ver-carpetas-${idGestion}`).click();
    let dialog = page.getByRole("dialog");
    await dialog.getByTestId(/^btn-poner-en-espera-/).click();
    dialog = page.getByRole("dialog");
    await dialog.getByTestId("input-motivo-espera").fill("Falta documentación");
    await dialog.getByTestId("btn-confirmar-espera").click();
    await expect(dialog).not.toBeVisible();
    await page.keyboard.press("Escape");

    await page.getByTestId(`btn-archivar-gestion-${idGestion}`).click();
    const confirmDialog = page.getByRole("alertdialog");
    await expect(confirmDialog).toBeVisible();
    await confirmDialog.getByRole("button", { name: /archivar gestión/i }).click();
    await expect(confirmDialog).toContainText(/espera/i);
    await expect(confirmDialog.getByRole("button", { name: /archivar de todos modos/i })).toBeVisible();
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`carpetas dialog is usable at ${viewport.label}`, async ({ page }) => {
      const { idGestion, numero } = await seedArchivableGestion(page);

      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto("/dashboard/gestiones");
      await page.waitForLoadState("domcontentloaded");

      const row = page.getByRole("row", { name: new RegExp(String(numero)) });
      await expect(row).toBeVisible({ timeout: 10000 });

      await page.getByTestId(`btn-ver-carpetas-${idGestion}`).click();
      const dialog = page.getByRole("dialog");
      await expect(dialog).toBeVisible();
      await expect(dialog.getByTestId("carpeta-item").first()).toContainText("Activa");
    });
  }
});
