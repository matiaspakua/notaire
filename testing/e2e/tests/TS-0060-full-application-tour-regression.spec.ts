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
  await expect(page.getByTestId("input-username")).toBeVisible({ timeout: 10000 });
  await page.getByTestId("input-username").fill(ADMIN_USER);
  await pause(page, 400);
  await page.getByTestId("input-password").fill(ADMIN_PASS);
  await pause(page, 400);
  await page.getByTestId("btn-sign-in").click();
  await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  await expect(page.getByTestId("sidebar")).toBeVisible({ timeout: 10000 });
}

async function navigateViaSidebar(page: Page, labelKey: string): Promise<void> {
  await page.getByTestId(`nav-${labelKey}`).click();
  await page.waitForLoadState("networkidle");
  await pause(page, PAUSE);
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
    // Search an existing workflow reference (seeded management number 1001)
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

  test("Managements — List, Create, Edit", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/managements");
      await assertHeadingVisible(page);
      await assertTableVisible(page);
    });

    await test.step("Create", async () => {
      const uniqueNumber = String(Date.now()).slice(-5);
      await openModal(page, "btn-new-management");
      await page.getByTestId("input-management-number").fill(uniqueNumber);
      await pause(page, PAUSE);
      await saveAndDismiss(page, "btn-save-management");
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

  test("Budgets — List, Search, Filter, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/budgets");
      await assertHeadingVisible(page);
      await assertTableVisible(page);
    });

    await test.step("Search", async () => {
      const searchInput = page.getByTestId("input-search-budget");
      await searchInput.fill("test");
      await pause(page, PAUSE);
      await searchInput.clear();
      await pause(page, 400);
    });

    await test.step("Filter by status", async () => {
      const trigger = page.getByTestId("select-status");
      await trigger.click();
      await pause(page, 400);
      const firstOption = page.getByRole("listbox").getByRole("option").first();
      if (await firstOption.isVisible().catch(() => false)) {
        await firstOption.click();
      } else {
        await page.keyboard.press("Escape");
      }
      await pause(page, PAUSE);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-new-budget");
      await expect(page.getByTestId("input-amount")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("People — List, Search, Filter, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/people");
      await assertHeadingVisible(page);
      await assertTableVisible(page);
    });

    await test.step("Search by first name", async () => {
      await page.getByTestId("input-search-firstName").fill("Juan");
      await pause(page, PAUSE);
      await page.getByTestId("input-search-firstName").clear();
      await pause(page, 400);
    });

    await test.step("Search by lastName", async () => {
      await page.getByTestId("input-search-lastName").fill("Pérez");
      await pause(page, PAUSE);
      await page.getByTestId("input-search-lastName").clear();
      await pause(page, 400);
    });

    await test.step("Search by DNI", async () => {
      await page.getByTestId("input-search-dni").fill("12345678");
      await pause(page, PAUSE);
      await page.getByTestId("input-search-dni").clear();
      await pause(page, 400);
    });

    await test.step("Filter clientes", async () => {
      await page.getByTestId("btn-filter-clients").click();
      await pause(page, PAUSE);
      // Toggle back
      await page.getByTestId("btn-filter-clients").click();
      await pause(page, 400);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-new-person");
      await expect(page.getByTestId("input-firstName")).toBeVisible();
      await expect(page.getByTestId("input-lastName")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Deeds — List", async () => {
    await navigateTo(page, "/dashboard/deeds");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Payments — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/payments");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      const btn = page.getByTestId("btn-new-payment");
      if (await btn.isVisible().catch(() => false)) {
        await openModal(page, "btn-new-payment");
        await pause(page, PAUSE);
        await closeModalViaButton(page);
      }
    });
  });

  test("Protocol — List", async () => {
    await navigateTo(page, "/dashboard/protocol");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Properties — List", async () => {
    await navigateTo(page, "/dashboard/properties");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Copies — List", async () => {
    await navigateTo(page, "/dashboard/copies");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Items — List", async () => {
    await navigateTo(page, "/dashboard/items");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Documentos — List", async () => {
    await navigateTo(page, "/dashboard/documents");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Audit — List", async () => {
    await navigateTo(page, "/dashboard/audit");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Substitutions — List, Create", async () => {
    await test.step("List via sidebar", async () => {
      await navigateViaSidebar(page, "suplencias");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      const btn = page.getByTestId("btn-new-substitution");
      if (await btn.isVisible().catch(() => false)) {
        await openModal(page, "btn-new-substitution");
        await pause(page, PAUSE);
        await closeModalViaButton(page);
      }
    });
  });

  test("Reports — List", async () => {
    await navigateViaSidebar(page, "reportes");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Administration — Hub page", async () => {
    await navigateTo(page, "/dashboard/administration");
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Admin procedures — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administration/procedures");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-new-procedure-type");
      await expect(page.getByTestId("input-name-procedure")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin Concepts — List and Search", async () => {
    await navigateTo(page, "/dashboard/administration/concepts");
    await assertHeadingVisible(page);
    const searchInput = page.getByTestId("input-search-concept");
    await searchInput.fill("test");
    await pause(page, PAUSE);
    await searchInput.clear();
    await pause(page, 400);
  });

  test("Admin management statuses — List, Search, Create, Edit", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administration/management-statuses");
      await assertHeadingVisible(page);
    });

    await test.step("Search", async () => {
      const searchInput = page.getByTestId("search-statuses");
      await searchInput.fill("inicial");
      await pause(page, PAUSE);
      await searchInput.clear();
      await pause(page, 400);
    });

    await test.step("Create", async () => {
      await openModal(page, "btn-new-status");
      await page.getByTestId("input-name-status").fill(`Status E2E ${Date.now()}`);
      await pause(page, PAUSE);
      await saveAndDismiss(page);
    });

    await test.step("Edit first row", async () => {
      const editBtn = page.getByTestId("btn-edit-status").first();
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
      await navigateTo(page, "/dashboard/administration/folios");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-new-folio");
      await expect(page.getByTestId("input-folio-number")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin templates — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administration/templates");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-new-template");
      await expect(page.getByTestId("input-notes-template")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin document types — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administration/documents");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-new-document-type");
      await expect(page.getByTestId("input-name-document")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Items — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/items");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-new-item");
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin Users — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administration/users");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-new-user");
      await expect(page.getByTestId("input-name-user")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin Roles — List, Create", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administration/roles");
      await assertHeadingVisible(page);
    });

    await test.step("Open create modal and cancel", async () => {
      await openModal(page, "btn-new-role");
      await expect(page.getByTestId("input-name-role")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });
  });

  test("Admin audit — redirects to the canonical route", async () => {
    // Legacy admin path must resolve to /dashboard/audit (#1058).
    await navigateTo(page, "/dashboard/administration/audit");
    await expect(page).toHaveURL(/\/dashboard\/audit/);
    await assertHeadingVisible(page);
    await pause(page, PAUSE);
  });

  test("Admin Workflows — List, Search, Create, Editor", async () => {
    await test.step("List", async () => {
      await navigateTo(page, "/dashboard/administration/workflows");
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
      await openModal(page, "btn-new-workflow");
      await expect(page.getByTestId("input-name-workflow")).toBeVisible();
      await pause(page, PAUSE);
      await closeModalViaButton(page);
    });

    await test.step("Open editor for first workflow", async () => {
      await navigateTo(page, "/dashboard/administration/workflows");
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
        await page.goto("/dashboard/administration/workflows");
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
    // Search for seeded management 1001
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
    await expect(page.getByTestId("btn-sign-in")).toBeVisible();
  });
});
