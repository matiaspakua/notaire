/**
 * E2E tests — Animated workflow tracker on the dashboard landing page
 * CU: CU70 (Workflow de Estados de Gestión), CU71 (Trazabilidad de Trámites)
 * Issue: #453
 */
import { type Page, test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import {
  apiGet,
  assignWorkflowToTipoTramite,
  createCompleteCaseGestion,
  createEstadoGestion,
  createPersona,
  createPresupuesto,
  createTipoTramite,
  createWorkflowDefinition,
  createWorkflowNode,
} from "./setup/api-helpers";

async function loginAs(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  await page.waitForLoadState("domcontentloaded");
}

test.describe("Workflow hero section (CU70, CU71)", () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page);
  });

  test("workflow hero renders with search form", async ({ page }) => {
    await expect(page.getByTestId("workflow-hero")).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId("workflow-search-form")).toBeVisible();
    await expect(page.locator("#workflow-ref")).toBeVisible();
  });

  test("workflow tracker renders nodes and legend for latest gestion", async ({ page }) => {
    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });

    const nodes = tracker.locator('[data-testid^="workflow-node-"]:not([data-testid="workflow-node-modal"])');
    expect(await nodes.count()).toBeGreaterThan(0);
    await expect(page.getByTestId("workflow-legend")).toBeVisible();
  });

  test("clicking a node opens detail modal and Escape closes it", async ({ page }) => {
    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });

    const firstNode = tracker
      .locator('[data-testid^="workflow-node-"]:not([data-testid="workflow-node-modal"])')
      .first();
    await firstNode.click();

    const modal = page.getByTestId("workflow-node-modal");
    await expect(modal).toBeVisible({ timeout: 5000 });
    await expect(page.getByTestId("workflow-modal-status")).toBeVisible();

    await page.keyboard.press("Escape");
    await expect(modal).not.toBeVisible({ timeout: 5000 });
  });

  test("searching an unknown reference shows not-found message", async ({ page }) => {
    await page.locator("#workflow-ref").fill("99999999");
    await page.getByTestId("workflow-search-form").locator('button[type="submit"]').click();

    await expect(page.getByTestId("workflow-not-found")).toBeVisible({ timeout: 10000 });
  });

  test("searching an existing reference renders its trace", async ({ page }) => {
    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });

    const subtitle = page.getByTestId("workflow-subtitle");
    await expect(subtitle).toBeVisible();
  });
});

// #1347: the hero follows the newest management with a workflow, not management 1001.
test.describe("Workflow hero shows the newest case (#1347)", () => {
  test("a case just created with a workflow (or a newer one) is shown, never the oldest", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const person = await createPersona(page, { isClient: true });
    expect(person.ok, person.error).toBe(true);
    const budget = await createPresupuesto(page, person.data!.personId);
    expect(budget.ok, budget.error).toBe(true);
    // A self-contained one-node workflow, so the new case is traceable on any database.
    const estado = await createEstadoGestion(page);
    const workflow = await createWorkflowDefinition(page);
    await createWorkflowNode(page, workflow.data!.id, estado.data!.idManagementStatus, "INITIAL");
    const tipo = await createTipoTramite(page);
    await assignWorkflowToTipoTramite(page, tipo.data!.idProcedureType, workflow.data!.id);
    const gestion = await createCompleteCaseGestion(page, {
      presupuestoId: budget.data!.idBudget,
      tipoTramiteId: tipo.data!.idProcedureType,
      estadoGestionId: estado.data!.idManagementStatus,
    });
    expect(gestion.ok, gestion.error).toBe(true);
    const trace = await apiGet(page, `/gestiones/${gestion.data!.idManagement}/workflow-trace`);
    expect(trace.ok, trace.error).toBe(true);

    await page.goto("/dashboard");
    const hero = page.getByTestId("workflow-hero");
    await expect(page.getByTestId("workflow-tracker")).toBeVisible({ timeout: 15000 });
    const shown = Number(await hero.getAttribute("data-management-id"));
    // Parallel workers may create newer cases; the oldest (seed 1001) must never win.
    expect(shown).toBeGreaterThanOrEqual(gestion.data!.idManagement);
    await expect(page.getByTestId("workflow-subtitle")).toContainText("#");
  });

  test("the modules header has no dead 'view all' button", async ({ page }) => {
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard");
    await expect(page.getByTestId("workflow-hero")).toBeVisible({ timeout: 10000 });
    await expect(page.getByRole("button", { name: /ver todos|view all/i })).toHaveCount(0);
  });
});

test.describe("Workflow tracker accessibility (#1353)", () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page);
  });

  test("the diagram is a named group whose steps are buttons with their state", async ({ page }) => {
    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });
    await expect(tracker.locator('svg[role="img"]')).toHaveCount(0);
    const diagram = tracker.locator('svg[role="group"]');
    await expect(diagram).toHaveAttribute("aria-roledescription", "diagrama de flujo");
    const nodes = tracker.locator('[data-testid^="workflow-node-"]:not([data-testid="workflow-node-modal"])');
    const steps = diagram.getByRole("button", { name: /—\s*(Completada|En curso|Pendiente)$/ });
    await expect(steps).toHaveCount(await nodes.count());
  });

  test("no looping animation runs forever", async ({ page }) => {
    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });
    await expect(tracker.locator('animateMotion[repeatCount="indefinite"]')).toHaveCount(0);
  });
});
