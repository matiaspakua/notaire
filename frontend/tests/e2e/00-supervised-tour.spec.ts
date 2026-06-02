/**
 * E2E — Supervised full-system guided tour
 *
 * A single, human-watchable journey through the whole UI:
 *   1. FIRST action  -> login (via the real login form)
 *   2. MIDDLE         -> visit every module via the sidebar, assert each page
 *                        loads, and exercise the primary "create" button of
 *                        each CRUD module (open the modal, then close it)
 *   3. LAST action   -> logout (via the sidebar button) and land back on /login
 *
 * Designed to be run headed with a slowMo so a human can review every action:
 *   HEADED=1 SLOW_MO=1500 npx playwright test 00-supervised-tour --project=chromium
 *
 * Requires the full system up: frontend :3000, backend :8080.
 */
import { type Page, test, expect } from "@playwright/test";

const ADMIN_USER = process.env.E2E_TEST_ADMIN_USER ?? "admin";
const ADMIN_PASS = process.env.E2E_TEST_ADMIN_PASS ?? "admin";

/** A module reachable from the sidebar. */
interface Module {
  /** Sidebar aria-label (navigation label). */
  navLabel: string;
  /** Expected URL fragment after navigation. */
  urlPart: string;
  /** Primary create-button testid, if the module has a create modal. */
  createTestId?: string;
}

/** Modules exposed directly in the sidebar (in display order). */
const SIDEBAR_MODULES: Module[] = [
  { navLabel: "Gestiones", urlPart: "/dashboard/gestiones", createTestId: "btn-nueva-gestion" },
  { navLabel: "Presupuestos", urlPart: "/dashboard/presupuestos", createTestId: "btn-nuevo-presupuesto" },
  { navLabel: "Personas", urlPart: "/dashboard/personas", createTestId: "btn-nueva-persona" },
  { navLabel: "Escrituras", urlPart: "/dashboard/escrituras" },
  { navLabel: "Pagos", urlPart: "/dashboard/pagos", createTestId: "btn-nuevo-pago" },
  { navLabel: "Protocolo", urlPart: "/dashboard/protocolo" },
  { navLabel: "Inmuebles", urlPart: "/dashboard/inmuebles" },
  { navLabel: "Copias", urlPart: "/dashboard/copias" },
  { navLabel: "Items", urlPart: "/dashboard/items" },
  { navLabel: "Documentos", urlPart: "/dashboard/documentos" },
  { navLabel: "Auditoría", urlPart: "/dashboard/auditoria" },
];

/** Administración sub-pages (reached by direct navigation from the admin hub). */
const ADMIN_MODULES: Module[] = [
  { navLabel: "Trámites", urlPart: "/dashboard/administracion/tramites", createTestId: "btn-nuevo-tipo-tramite" },
  { navLabel: "Conceptos", urlPart: "/dashboard/administracion/conceptos" },
  { navLabel: "Estados de gestión", urlPart: "/dashboard/administracion/estados-gestion", createTestId: "btn-nuevo-estado" },
  { navLabel: "Folios", urlPart: "/dashboard/administracion/folios", createTestId: "btn-nuevo-folio" },
  { navLabel: "Plantillas", urlPart: "/dashboard/administracion/plantillas", createTestId: "btn-nueva-plantilla" },
  { navLabel: "Tipos de documento", urlPart: "/dashboard/administracion/documentos", createTestId: "btn-nuevo-tipo-documento" },
  { navLabel: "Ítems", urlPart: "/dashboard/administracion/items", createTestId: "btn-nuevo-item" },
  { navLabel: "Usuarios", urlPart: "/dashboard/administracion/usuarios", createTestId: "btn-nuevo-usuario" },
  { navLabel: "Auditoría", urlPart: "/dashboard/administracion/auditoria" },
];

/**
 * The dashboard layout renders a global <AppHeader /> with no title, which
 * emits an empty (hidden) <h1> at the top of every page. Target the first
 * heading that actually has text so assertions hit the real page title.
 */
function visibleHeading(page: Page) {
  return page.locator("h1").filter({ hasText: /\S/ }).first();
}

async function loginViaUI(page: Page): Promise<void> {
  await page.goto("/login");
  await expect(page.getByTestId("input-usuario")).toBeVisible();
  await page.getByTestId("input-usuario").fill(ADMIN_USER);
  await page.getByTestId("input-contrasenia").fill(ADMIN_PASS);
  await page.getByTestId("btn-ingresar").click();
  await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  await expect(page.getByTestId("sidebar")).toBeVisible({ timeout: 10000 });
}

/** Open the create modal (if present), confirm a dialog appears, then close it. */
async function exerciseCreateModal(page: Page, createTestId: string): Promise<void> {
  const btn = page.getByTestId(createTestId);
  if (!(await btn.isVisible().catch(() => false))) {
    return;
  }
  await btn.click();
  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible({ timeout: 5000 });
  // Close the modal without persisting anything.
  await page.keyboard.press("Escape");
  await expect(dialog).toBeHidden({ timeout: 5000 });
}

async function assertPageLoaded(page: Page, mod: Module): Promise<void> {
  await expect(page).toHaveURL(new RegExp(mod.urlPart.replace(/\//g, "\\/")), { timeout: 10000 });
  // Every dashboard page renders a single <h1> header.
  await expect(visibleHeading(page)).toBeVisible({ timeout: 10000 });
}

test.describe("Supervised full-system tour (login → all modules → logout)", () => {
  // This journey is long when run headed/slowMo; give it room.
  test.setTimeout(15 * 60 * 1000);

  test("walks every module and logs out", async ({ page }) => {
    await test.step("FIRST ACTION: login", async () => {
      await loginViaUI(page);
      await expect(page).toHaveURL(/\/dashboard/);
    });

    await test.step("Dashboard landing renders", async () => {
      await expect(visibleHeading(page)).toBeVisible({ timeout: 10000 });
    });

    for (const mod of SIDEBAR_MODULES) {
      await test.step(`Module: ${mod.navLabel}`, async () => {
        // Navigate using the real sidebar link so the action is visible.
        await page.getByTestId("sidebar").getByRole("link", { name: mod.navLabel }).first().click();
        await assertPageLoaded(page, mod);
        if (mod.createTestId) {
          await exerciseCreateModal(page, mod.createTestId);
        }
      });
    }

    await test.step("Open Administración hub", async () => {
      await page.getByTestId("sidebar").getByRole("link", { name: "Administración" }).first().click();
      await expect(page).toHaveURL(/\/dashboard\/administracion/, { timeout: 10000 });
      await expect(visibleHeading(page)).toBeVisible({ timeout: 10000 });
    });

    for (const mod of ADMIN_MODULES) {
      await test.step(`Admin: ${mod.navLabel}`, async () => {
        await page.goto(mod.urlPart);
        await assertPageLoaded(page, mod);
        if (mod.createTestId) {
          await exerciseCreateModal(page, mod.createTestId);
        }
      });
    }

    await test.step("Visit Reportes & Suplencias", async () => {
      await page.goto("/dashboard/reportes");
      await assertPageLoaded(page, { navLabel: "Reportes", urlPart: "/dashboard/reportes" });
      await page.goto("/dashboard/suplencias");
      await assertPageLoaded(page, { navLabel: "Suplencias", urlPart: "/dashboard/suplencias" });
      await exerciseCreateModal(page, "btn-nueva-suplencia");
    });

    await test.step("LAST ACTION: logout", async () => {
      // Return to a page that always shows the sidebar, then log out.
      await page.goto("/dashboard");
      await expect(page.getByTestId("btn-logout")).toBeVisible({ timeout: 10000 });
      await page.getByTestId("btn-logout").click();
      await expect(page).toHaveURL(/\/login/, { timeout: 10000 });
      await expect(page.getByTestId("btn-ingresar")).toBeVisible();
    });
  });
});
