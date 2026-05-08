/**
 * E2E: Dashboard navigation — Prerequisite for all module tests.
 *
 * Verifies the main dashboard and navigation work correctly:
 * - Module grid renders all expected modules
 * - Sidebar navigation links work
 * - Admin hub page renders correctly
 * - All main pages load without errors
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./helpers/gherkin-steps";

test.describe("Dashboard — Navegación principal", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("GW01: Dashboard shows module grid with expected modules", async () => {
    // Then
    await steps.thenPageHasHeading("Notaire");
    await steps.thenElementIsVisible("Gestiones");
    await steps.thenElementIsVisible("Personas");
    await steps.thenElementIsVisible("Presupuestos");
    await steps.thenElementIsVisible("Escrituras");
  });

  test("GW02: Click personas link navigates to personas page", async () => {
    // When
    await steps.whenUserNavigatesToSidebar("Personas");

    // Then
    await steps.thenUrlIs(/\/personas/);
    await steps.thenPageHasHeading("Personas");
  });

  test("GW03: Click admin link navigates to admin hub", async () => {
    // When
    await steps.whenUserNavigatesToSidebar("Administración");

    // Then
    await steps.thenUrlIs(/\/administracion/);
    await steps.thenElementIsVisible("Conceptos");
  });

  test("GW04: Admin hub shows all admin sub-modules", async () => {
    // Given
    await steps.whenUserNavigatesToSidebar("Administración");

    // Then: All admin module links are visible
    await steps.thenElementIsVisible("Usuarios");
    await steps.thenElementIsVisible("Conceptos");
    await steps.thenElementIsVisible("Tipos de Trámite");
    await steps.thenElementIsVisible("Folios");
    await steps.thenElementIsVisible("Plantillas");
  });

  test("GW05: Presupuestos page loads correctly", async () => {
    // When
    await steps.whenUserNavigatesToSidebar("Presupuestos");

    // Then
    await steps.thenUrlIs(/\/presupuestos/);
    await steps.thenPageHasHeading("Presupuestos");
    await steps.thenPageHasNoCriticalErrors();
  });

  test("GW06: All main pages load without crashes", async () => {
    const pages = [
      { path: "/dashboard", name: "Dashboard" },
      { path: "/dashboard/personas", name: "Personas" },
      { path: "/dashboard/presupuestos", name: "Presupuestos" },
      { path: "/dashboard/administracion", name: "Administración" },
      { path: "/dashboard/administracion/conceptos", name: "Conceptos" },
      { path: "/dashboard/administracion/usuarios", name: "Usuarios" },
      { path: "/dashboard/administracion/tramites", name: "Tipos Trámite" },
      { path: "/dashboard/administracion/folios", name: "Folios" },
      { path: "/dashboard/administracion/plantillas", name: "Plantillas" },
      { path: "/dashboard/administracion/documentos", name: "Documentos" },
      { path: "/dashboard/administracion/estados-gestion", name: "Estados Gestión" },
    ];

    for (const { path, name } of pages) {
      await steps.givenUserIsOnPage(path);
      await steps.thenPageHasNoCriticalErrors();
      console.log(`  ✓ Page loads: ${name} (${path})`);
    }
  });
});
