/**
 * E2E — Workflow tracker reingreso loop (strategy b)
 * CU: CU83, CU44 — Issue #841
 *
 * Stubs the workflow-trace API so the secondary TestimonyMovement timeline
 * and reingreso badge can be asserted without depending on live seed data.
 */
import { type Page, test, expect } from "@playwright/test";

async function loginAs(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  await page.waitForLoadState("domcontentloaded");
}

function baseTrace(overrides: Record<string, unknown> = {}) {
  return {
    managementId: 1,
    number: 1001,
    encabezado: "Reingreso loop fixture",
    statusActual: "Testimonio Ingresado a Inscripcion",
    workflowDefinition: {
      id: 1,
      name: "Standard management workflow",
      active: true,
    },
    nodes: [
      {
        id: 1,
        statusManagementId: 1,
        statusManagementName: "Iniciada",
        type: "INITIAL",
      },
      {
        id: 5,
        statusManagementId: 6,
        statusManagementName: "Gestion con Escritura Firmada",
        type: "INTERMEDIATE",
      },
      {
        id: 8,
        statusManagementId: 11,
        statusManagementName: "Testimonio Generado",
        type: "INTERMEDIATE",
      },
      {
        id: 9,
        statusManagementId: 12,
        statusManagementName: "Testimonio Ingresado a Inscripcion",
        type: "INTERMEDIATE",
      },
      {
        id: 10,
        statusManagementId: 13,
        statusManagementName: "Testimonio Retirado",
        type: "FINAL",
      },
    ],
    transitions: [
      { id: 1, originNodeId: 1, destinationNodeId: 5 },
      { id: 2, originNodeId: 5, destinationNodeId: 8 },
      { id: 3, originNodeId: 8, destinationNodeId: 9 },
      { id: 4, originNodeId: 9, destinationNodeId: 10 },
    ],
    history: [
      { idHistory: 1, statusManagementId: 1, date: "2026-05-01" },
      { idHistory: 2, statusManagementId: 6, date: "2026-05-10" },
      { idHistory: 3, statusManagementId: 11, date: "2026-05-12" },
      { idHistory: 4, statusManagementId: 12, date: "2026-05-15" },
    ],
    nodeStatuses: {
      "1": "completed",
      "5": "completed",
      "8": "completed",
      "9": "in_progress",
      "10": "pending",
    },
    testimonyMovements: [],
    ...overrides,
  };
}

async function stubTrace(page: Page, body: Record<string, unknown>) {
  await page.route("**/api/v1/gestiones/*/workflow-trace", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify(body),
    });
  });
}

test.describe("Workflow tracker reingreso loop (#841)", () => {
  test("shows reingreso badge and movement timeline when returnedObserved=2", async ({
    page,
  }) => {
    await stubTrace(
      page,
      baseTrace({
        testimonyMovements: [
          {
            dateEntry: "2026-05-15",
            dateExit: "2026-05-20",
            returnedObserved: true,
          },
          {
            dateEntry: "2026-05-22",
            dateExit: "2026-05-28",
            returnedObserved: true,
          },
          {
            dateEntry: "2026-06-01",
            dateExit: null,
            returnedObserved: false,
          },
        ],
      }),
    );
    await loginAs(page);

    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });
    await expect(page.getByTestId("workflow-reingreso-badge")).toBeVisible();
    await expect(page.getByTestId("workflow-reingreso-badge")).toContainText("2");

    await page.getByTestId("workflow-node-9").click();
    await expect(page.getByTestId("workflow-node-modal")).toBeVisible();
    await expect(page.getByTestId("workflow-movement-timeline")).toBeVisible();
    await expect(page.getByTestId("workflow-movement-entry")).toHaveCount(3);
  });

  test("hides reingreso badge when no returned-observed movements", async ({
    page,
  }) => {
    await stubTrace(
      page,
      baseTrace({
        testimonyMovements: [
          {
            dateEntry: "2026-05-15",
            dateExit: null,
            returnedObserved: false,
          },
        ],
      }),
    );
    await loginAs(page);

    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });
    await expect(page.getByTestId("workflow-reingreso-badge")).toHaveCount(0);
  });

  test("degrades without post-firma nodes and without movements", async ({
    page,
  }) => {
    await stubTrace(
      page,
      baseTrace({
        nodes: [
          {
            id: 1,
            statusManagementId: 1,
            statusManagementName: "Iniciada",
            type: "INITIAL",
          },
          {
            id: 2,
            statusManagementId: 2,
            statusManagementName: "En Tramite",
            type: "FINAL",
          },
        ],
        transitions: [{ id: 1, originNodeId: 1, destinationNodeId: 2 }],
        history: [{ idHistory: 1, statusManagementId: 1, date: "2026-05-01" }],
        nodeStatuses: { "1": "in_progress", "2": "pending" },
        testimonyMovements: [],
      }),
    );
    await loginAs(page);

    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });
    await expect(page.getByTestId("workflow-reingreso-badge")).toHaveCount(0);
    await expect(page.getByTestId("workflow-node-1")).toBeVisible();
  });

  for (const width of [320, 768, 1024] as const) {
    test(`renders tracker at ${width}px with reingreso badge`, async ({ page }) => {
      await page.setViewportSize({ width, height: 800 });
      await stubTrace(
        page,
        baseTrace({
          testimonyMovements: [
            {
              dateEntry: "2026-05-15",
              dateExit: "2026-05-20",
              returnedObserved: true,
            },
            {
              dateEntry: "2026-05-22",
              dateExit: "2026-05-28",
              returnedObserved: true,
            },
          ],
        }),
      );
      await loginAs(page);
      await expect(page.getByTestId("workflow-tracker")).toBeVisible({
        timeout: 15000,
      });
      await expect(page.getByTestId("workflow-reingreso-badge")).toBeVisible();
    });
  }
});
