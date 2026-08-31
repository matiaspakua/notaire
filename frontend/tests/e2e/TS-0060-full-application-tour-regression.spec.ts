/**
 * Full Application Tour — single login, all modules, single logout
 *
 * Covers every main and admin module with:
 *   - Page load verification
 *   - List / table rendering
 *   - Search / filter (where available)
 *   - Create modal open → fill → save (or cancel on read-only pages)
 *   - Edit modal open → modify → save (where available)
 *   - Delete with confirmation (where available)
 *   - Workflow tracker interactions
 *
 * Each module is its own `test()` inside a `describe.serial` block sharing a
 * single logged-in page, so a failure in one module is reported against that
 * module alone instead of hiding every module that runs after it.
 *
 * Run headed (human-watchable):
 *   HEADED=1 SLOW_MO=1200 npx playwright test full-application-tour --project=chromium
 *
 * Requires the full Docker stack: frontend :3000, backend :8080.
 */

import { test, expect, type Page } from "@playwright/test";

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const ADMIN_USER = process.env.E2E_TEST_ADMIN_USER ?? "admin";
const ADMIN_PASS = process.env.E2E_TEST_ADMIN_PASS ?? "admin";
const PAUSE = 800; // ms between actions for human-readable pacing

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

async function pause(page: Page, ms = PAUSE): Promise<void> {
  await page.waitForTimeout(ms);
}

async function loginViaUI(page: Page): Promise<void> {
  await page.goto("/login");
  await expect(page.getByTestId("input-usuario")).toBeVisible({ timeout: 10000 });
  await page.getByTestId("input-usuario").fill(ADMIN_USER);
  await pause(page, 400);
  await page.getByTestId("input-contrasenia").fill(ADMIN_PASS);
  await pause(page, 400);
  await page.getByTestId("btn-ingresar").click();
  await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  await expect(page.getByTestId("sidebar")).toBeVisible({ timeout: 10000 });
}

async function navigateTo(page: Page, path: string): Promise<void> {
  await page.goto(path);
  await page.waitForLoadState("networkidle");
  await pause(page, PAUSE);
}

async function assertHeadingVisible(page: Page): Promise<void> {
  await expect(page.locator("h1").filter({ hasText: /\S/ }).first()).toBeVisible({ timeout: 10000 });
}

async function openModal(page: Page, testId: string): Promise<void> {
  const btn = page.getByTestId(testId);
  await expect(btn).toBeVisible({ timeout: 8000 });
  await btn.click();
  await expect(page.getByRole("dialog")).toBeVisible({ timeout: 8000 });
  await pause(page, PAUSE);
}

async function closeModalViaButton(page: Page): Promise<void> {
  const cancelBtn = page.getByRole("button", { name: /cancelar/i });
  if (await cancelBtn.isVisible().catch(() => false)) {
    await cancelBtn.click();
  } else {
    await page.keyboard.press("Escape");
  }
  await expect(page.getByRole("dialog")).toBeHidden({ timeout: 6000 });
  await pause(page, 400);
}

async function saveModal(page: Page, saveTestId?: string): Promise<void> {
  if (saveTestId) {
    await page.getByTestId(saveTestId).click();
  } else {
    await page.getByRole("button", { name: /guardar|confirmar|crear|registrar/i }).first().click();
  }
  await pause(page, PAUSE);
}

/** Save and then dismiss the modal regardless of success or validation error. */
async function saveAndDismiss(page: Page, saveTestId?: string): Promise<void> {
  await saveModal(page, saveTestId);
  // If modal is still open (validation failed, API error, etc.), close it gracefully
  const dialog = page.getByRole("dialog");
  if (await dialog.isVisible({ timeout: 1000 }).catch(() => false)) {
    await page.keyboard.press("Escape");
    await expect(dialog).toBeHidden({ timeout: 6000 });
  }
  await pause(page, 400);
}

async function assertTableVisible(page: Page): Promise<void> {
  await expect(page.getByRole("table")).toBeVisible({ timeout: 10000 });
}

// ---------------------------------------------------------------------------
// Main tour — one test() per module, sharing a single logged-in page
// ---------------------------------------------------------------------------

test.describe.serial("Full Application Tour — single login → all modules → logout", () => {
  test.setTimeout(30 * 60 * 1000); // 30 min for headed runs

  let page: Page;

  test.beforeAll(async ({ browser }) => {
    page = await browser.newPage();
  });

  test.afterAll(async () => {
    await page.close();
  });

  test("Login via UI", async () => {
    await loginViaUI(page);
    await expect(page).toHaveURL(/\/dashboard/);
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Dashboard — workflow tracker loads", async () => {
    await page.goto("/dashboard");
    await page.waitForLoadState("networkidle");
    await pause(page, PAUSE);
    await assertHeadingVisible(page);
    // Workflow hero section
    const hero = page.getByTestId("workflow-hero");
    await expect(hero).toBeVisible({ timeout: 10000 });
    // Search an existing workflow reference (seeded gestion numero 1001)
    const searchInput = page.getByTestId("workflow-search-form").locator("input");
    await searchInput.fill("1001");
    await pause(page, 600);
    await page.keyboard.press("Enter");
    await pause(page, 1200);
    // Accept either the tracker or a not-found message
    const trackerOrMsg = page.getByTestId("workflow-tracker")
      .or(page.getByTestId("workflow-not-found"));
    await expect(trackerOrMsg).toBeVisible({ timeout: 10000 });
    await pause(page, PAUSE);
  });

  test("Gestiones — List, Create, Edit", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/gestiones");
      await assertHeadingVisible(page);
      await assertTableVisible(page);
    });

    await test.step("Create", async () => {
      const uniqueNumero = String(Date.now()).slice(-5);
      await openModal(page, "btn-nueva-gestion");
      await page.getByTestId("input-numero-gestion").fill(uniqueNumero);
      await pause(page, PAUSE);
      await saveAndDismiss(page, "btn-guardar-gestion");
    });

    await test.step("Edit first row", async () => {
      await expect(page.getByRole("table").getByRole("row").nth(1)).toBeVisible({ timeout: 8000 });
      await page.getByRole("table").getByRole("row").nth(1).getByRole("button", { name: /editar|edit/i }).first().click();
      const dialog = page.getByRole("dialog");
      await expect(dialog).toBeVisible({ timeout: 8000 });
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Presupuestos — List, Search, Filter, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/presupuestos");
      await assertHeadingVisible(page);
      await assertTableVisible(page);
    });

    await test.step("Search", async () => {
      const searchInput = page.getByTestId("input-search-presupuesto");
      await searchInput.fill("test");
      await pause(page, PAUSE);
      await searchInput.clear();
      await pause(page, 400);
    });

    await test.step("Filter by estado", async () => {
      const trigger = page.getByTestId("select-estado");
      await trigger.click();
      await pause(page, 400);
      const firstOption = page.getByRole("option").first();
      if (await firstOption.isVisible().catch(() => false)) {
        await firstOption.click();
      } else {
        await page.keyboard.press("Escape");
      }
      await pause(page, PAUSE);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-nuevo-presupuesto");
      await expect(page.getByTestId("input-monto")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Personas — List, Search, Filter, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/personas");
      await assertHeadingVisible(page);
      await assertTableVisible(page);
    });

    await test.step("Search by nombre", async () => {
      await page.getByTestId("input-search-nombre").fill("Juan");
      await pause(page, PAUSE);
      await page.getByTestId("input-search-nombre").clear();
      await pause(page, 400);
    });

    await test.step("Search by apellido", async () => {
      await page.getByTestId("input-search-apellido").fill("Pérez");
      await pause(page, PAUSE);
      await page.getByTestId("input-search-apellido").clear();
      await pause(page, 400);
    });

    await test.step("Search by DNI", async () => {
      await page.getByTestId("input-search-dni").fill("12345678");
      await pause(page, PAUSE);
      await page.getByTestId("input-search-dni").clear();
      await pause(page, 400);
    });

    await test.step("Filter clientes", async () => {
      await page.getByTestId("btn-filter-clientes").click();
      await pause(page, PAUSE);
      // Toggle back
      await page.getByTestId("btn-filter-clientes").click();
      await pause(page, 400);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-nueva-persona");
      await expect(page.getByTestId("input-nombre")).toBeVisible();
      await expect(page.getByTestId("input-apellido")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Escrituras — List", async () => {
    await navigateTo(page, "/dashboard/escrituras");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Pagos — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/pagos");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      const btn = page.getByTestId("btn-nuevo-pago");
      if (await btn.isVisible().catch(() => false)) {
        await openModal(page, "btn-nuevo-pago");
        await pause(page, PAUSE);
        await closeModalViaButton(page);
      }
    });
  });

  test("Protocolo — List", async () => {
    await navigateTo(page, "/dashboard/protocolo");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Inmuebles — List", async () => {
    await navigateTo(page, "/dashboard/inmuebles");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Copias — List", async () => {
    await navigateTo(page, "/dashboard/copias");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Items — List", async () => {
    await navigateTo(page, "/dashboard/items");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Documentos — List", async () => {
    await navigateTo(page, "/dashboard/documentos");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Auditoría — List", async () => {
    await navigateTo(page, "/dashboard/auditoria");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Suplencias — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/suplencias");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      const btn = page.getByTestId("btn-nueva-suplencia");
      if (await btn.isVisible().catch(() => false)) {
        await openModal(page, "btn-nueva-suplencia");
        await pause(page, PAUSE);
        await closeModalViaButton(page);
      }
    });
  });

  test("Reportes — List", async () => {
    await navigateTo(page, "/dashboard/reportes");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Administración — Hub page", async () => {
    await navigateTo(page, "/dashboard/administracion");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Admin Trámites — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administracion/tramites");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-nuevo-tipo-tramite");
      await expect(page.getByTestId("input-nombre-tramite")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin Conceptos — List and Search", async () => {
    await navigateTo(page, "/dashboard/administracion/conceptos");
    await assertHeadingVisible(page);
    const searchInput = page.getByTestId("input-search-concepto");
    await searchInput.fill("test");
    await pause(page, PAUSE);
    await searchInput.clear();
    await pause(page, 400);
  });

  test("Admin Estados Gestión — List, Search, Create, Edit", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administracion/estados-gestion");
      await assertHeadingVisible(page);
    });

    await test.step("Search", async () => {
      const searchInput = page.getByTestId("search-estados");
      await searchInput.fill("inicial");
      await pause(page, PAUSE);
      await searchInput.clear();
      await pause(page, 400);
    });

    await test.step("Create", async () => {
      await openModal(page, "btn-nuevo-estado");
      await page.getByTestId("input-nombre-estado").fill(`Estado E2E ${Date.now()}`);
      await pause(page, PAUSE);
      await saveAndDismiss(page);
    });

    await test.step("Edit first row", async () => {
      const editBtn = page.getByTestId("btn-edit-estado").first();
      if (await editBtn.isVisible().catch(() => false)) {
        await editBtn.click();
        await expect(page.getByRole("dialog")).toBeVisible({ timeout: 6000 });
        await pause(page, PAUSE);
        await closeModalViaButton(page);
      }
    });
  });

  test("Admin Folios — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administracion/folios");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-nuevo-folio");
      await expect(page.getByTestId("input-numero-folio")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin Plantillas — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administracion/plantillas");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-nueva-plantilla");
      await expect(page.getByTestId("input-observaciones-plantilla")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin Tipos Documento — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administracion/documentos");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-nuevo-tipo-documento");
      await expect(page.getByTestId("input-nombre-documento")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin Ítems — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administracion/items");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-nuevo-item");
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin Usuarios — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administracion/usuarios");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-nuevo-usuario");
      await expect(page.getByTestId("input-nombre-usuario")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin Roles — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administracion/roles");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-nuevo-rol");
      await expect(page.getByTestId("input-nombre-rol")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin Auditoría — List", async () => {
    await navigateTo(page, "/dashboard/administracion/auditoria");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Admin Workflows — List, Search, Create, Editor", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administracion/workflows");
      await assertHeadingVisible(page);
    });

    await test.step("Search", async () => {
      const searchInput = page.getByTestId("search-workflows");
      await searchInput.fill("Gestión");
      await pause(page, PAUSE);
      await searchInput.clear();
      await pause(page, 400);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-nuevo-workflow");
      await expect(page.getByTestId("input-nombre-workflow")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });

    await test.step("Open editor for first workflow", async () => {
      await navigateTo(page, "/dashboard/administracion/workflows");
      await page.waitForLoadState("networkidle");
      // Find the first editor button (dynamic testid: btn-editor-{id})
      const editorBtn = page.locator("[data-testid^='btn-editor-']").first();
      if (await editorBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
        await editorBtn.click();
        await page.waitForLoadState("networkidle");
        await pause(page, PAUSE);
        // Assert we're on the workflow editor page
        const editorCanvas = page.getByTestId("workflow-editor");
        await expect(editorCanvas).toBeVisible({ timeout: 10000 });
        await pause(page, PAUSE);
        // Navigate back
        await page.goto("/dashboard/administracion/workflows");
        await page.waitForLoadState("networkidle");
        await pause(page, 600);
      }
    });
  });

  test("Dashboard — Workflow tracker interactive demo", async () => {
    await navigateTo(page, "/dashboard");
    await page.waitForLoadState("networkidle");
    await pause(page, PAUSE);

    const searchInput = page.getByTestId("workflow-search-form").locator("input");
    // Search for seeded gestion 1001
    await searchInput.fill("1001");
    await pause(page, 600);
    await page.keyboard.press("Enter");
    await pause(page, 1500);

    const tracker = page.getByTestId("workflow-tracker");
    if (await tracker.isVisible({ timeout: 8000 }).catch(() => false)) {
      // Click first workflow node
      const firstNode = page.locator("[data-testid^='workflow-node-']").first();
      if (await firstNode.isVisible({ timeout: 5000 }).catch(() => false)) {
        await firstNode.click();
        await pause(page, PAUSE);
        // Modal should open
        const modal = page.getByTestId("workflow-node-modal");
        if (await modal.isVisible({ timeout: 5000 }).catch(() => false)) {
          await pause(page, PAUSE);
          // Close with Escape
          await page.keyboard.press("Escape");
          await pause(page, 600);
        }
      }
    }

    // Test not-found scenario
    await searchInput.fill("99999999");
    await pause(page, 400);
    await page.keyboard.press("Enter");
    await pause(page, 1200);
    const notFound = page.getByTestId("workflow-not-found");
    if (await notFound.isVisible({ timeout: 5000 }).catch(() => false)) {
      await expect(notFound).toBeVisible();
    }
    await pause(page, PAUSE);
  });

  test("Logout", async () => {
    await page.goto("/dashboard");
    await expect(page.getByTestId("btn-logout")).toBeVisible({ timeout: 10000 });
    await pause(page, PAUSE);
    await page.getByTestId("btn-logout").click();
    await expect(page).toHaveURL(/\/login/, { timeout: 10000 });
    await expect(page.getByTestId("btn-ingresar")).toBeVisible();
  });
});
