/**
 * TS-0121 — Grouped sidebar (#1367, RNF-10 / RNF-06, CU76).
 * Six task-based sections with headings; the current route's section is open;
 * collapsed sections persist across reloads; the dashboard module grid comes
 * from the same config; non-admins get neither Administración nor Auditoría.
 */
import { test, expect, type Page } from "@playwright/test";
import { createUsuario } from "./setup/api-helpers";
import { authenticateAsAdmin, establishAdminBrowserSession } from "./setup/auth";

const SECTIONS = [
  "Clientes",
  "Trámites",
  "Documentación",
  "Escrituras y protocolo",
  "Gestión",
  "Administración",
];

function sidebar(page: Page) {
  return page.locator("aside[data-testid='sidebar']");
}

test.describe("TS-0121 grouped sidebar (#1367)", () => {
  test("admin sees the six sections in order; the current route's section is open", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard/copias");
    const headings = sidebar(page).getByRole("heading", { level: 2 });
    await expect(headings).toHaveText(SECTIONS, { timeout: 10000 });
    await expect(sidebar(page).getByRole("button", { name: "Escrituras y protocolo" })).toHaveAttribute(
      "aria-expanded",
      "true",
    );
    await expect(sidebar(page).getByTestId("nav-copias")).toHaveAttribute("aria-current", "page");
  });

  test("a collapsed section stays collapsed after reload, and opens when its route is visited", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard/gestiones");
    const clientes = sidebar(page).getByRole("button", { name: "Clientes" });
    await clientes.click();
    await expect(clientes).toHaveAttribute("aria-expanded", "false");
    await expect(sidebar(page).getByTestId("nav-personas")).toHaveCount(0);

    await page.reload();
    await expect(sidebar(page).getByRole("button", { name: "Clientes" })).toHaveAttribute("aria-expanded", "false");
    await expect(sidebar(page).getByTestId("nav-personas")).toHaveCount(0);

    await page.goto("/dashboard/personas");
    await expect(sidebar(page).getByRole("button", { name: "Clientes" })).toHaveAttribute("aria-expanded", "true");
    await expect(sidebar(page).getByTestId("nav-personas")).toHaveAttribute("aria-current", "page");
  });

  test("the dashboard module grid follows the sidebar order", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard");
    const grid = page.getByTestId("dashboard-modules");
    await expect(grid.getByRole("link")).toHaveCount(14, { timeout: 10000 });
    const hrefs = await grid.getByRole("link").evaluateAll((els) => els.map((e) => e.getAttribute("href")));
    expect(hrefs.slice(0, 3)).toEqual(["/dashboard/personas", "/dashboard/inmuebles", "/dashboard/gestiones"]);
    expect(hrefs[hrefs.length - 1]).toBe("/dashboard/administracion");
  });

  test("a non-admin sees neither Administración nor Auditoría", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    const username = `emp1367-${Date.now()}`;
    const password = "Test1234!";
    await authenticateAsAdmin(page);
    const created = await createUsuario(page, undefined, { name: username, password, type: "EMPLEADO", active: true });
    expect(created.ok, `createUsuario failed: ${created.status} ${created.error}`).toBe(true);
    await page.context().clearCookies();
    await page.goto("/login");
    await page.evaluate(() => localStorage.clear());
    await page.getByTestId("input-usuario").fill(username);
    await page.getByTestId("input-contrasenia").fill(password);
    await page.getByTestId("btn-ingresar").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });

    await expect(sidebar(page).getByRole("heading", { level: 2 })).toHaveText(SECTIONS.slice(0, 5));
    await expect(sidebar(page).getByTestId("nav-auditoria")).toHaveCount(0);
    await expect(sidebar(page).getByTestId("nav-administracion")).toHaveCount(0);
    await expect(page.getByTestId("dashboard-modules").locator('a[href="/dashboard/auditoria"]')).toHaveCount(0);
  });
});
