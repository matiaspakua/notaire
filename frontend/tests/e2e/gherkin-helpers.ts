/**
 * Gherkin-style test helpers for Playwright E2E tests
 * Implements Given-When-Then pattern for all CU use cases
 *
 * Extended with business-specific helpers and Bruno-synced API assertions.
 */

import { type Page, type Locator, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";

/**
 * Gherkin step definitions and helpers
 */
export class GherkinSteps {
  constructor(public page: Page) {}

  // =================== GIVEN steps ===================

  /** Only "admin" is seeded by default; other roles fall back to it. */
  async givenUserIsAuthenticatedAs(role: "admin" | "empleado" = "admin") {
    await authenticateAsAdmin(this.page);
  }

  async givenUserIsOnPage(path: string) {
    await this.page.goto(path);
  }

  async givenUserIsLoggedIn() {
    await this.page.goto("/login");
    await this.page.getByTestId("input-usuario").fill("admin");
    await this.page.getByTestId("input-contrasenia").fill("admin");
    await this.page.getByTestId("btn-ingresar").click();
    await this.page.waitForURL(/\/dashboard/, { timeout: 10000 });
  }

  async givenModuleIsVisible(moduleName: string) {
    await expect(this.page.getByRole("heading", { name: new RegExp(moduleName, "i") })).toBeVisible();
  }

  /** Given: the dashboard navigation sidebar is loaded */
  async givenDashboardIsLoaded() {
    await this.page.waitForSelector('[data-testid="sidebar"]', { timeout: 10000 });
    await expect(this.page.getByTestId("sidebar")).toBeVisible();
  }

  /** Given: a record exists in the system (created via API) */
  async givenRecordExists(entity: string) {
    const envVar = `E2E_SEED_${entity.toUpperCase()}_ID`;
    const id = process.env[envVar];
    expect(id).toBeTruthy();
  }

  // =================== WHEN steps ===================

  async whenUserClicksButton(buttonName: string) {
    await this.page.getByRole("button", { name: new RegExp(buttonName, "i") }).click();
  }

  async whenUserClicksLink(linkName: string) {
    await this.page.getByRole("link", { name: new RegExp(linkName, "i") }).click();
  }

  /**
   * List pages commonly have a "Buscar por {campo}..." search input whose
   * aria-label also matches a modal field's label regex (e.g. /nombre/i
   * matches both "Nombre" and "Buscar por nombre..."). Scoping to the open
   * dialog when one exists avoids that ambiguity for form-field lookups.
   */
  private async formScope(): Promise<Locator | Page> {
    const dialog = this.page.getByRole("dialog");
    return (await dialog.isVisible().catch(() => false)) ? dialog : this.page;
  }

  async whenUserFillsField(fieldLabel: string, value: string) {
    const scope = await this.formScope();
    await scope.getByLabel(new RegExp(fieldLabel, "i")).fill(value);
  }

  async whenUserSelectsFromDropdown(dropdownLabel: string, option: string) {
    await this.page.getByRole("combobox", { name: new RegExp(dropdownLabel, "i") }).click();
    await this.page.getByRole("option", { name: new RegExp(option, "i") }).click();
  }

  async whenUserSubmitsForm() {
    await this.page
      .getByRole("button", { name: /confirmar|guardar|crear|registrar/i })
      .click();
  }

  async whenUserCancelsForm() {
    await this.page.getByRole("button", { name: /cancelar/i }).click();
  }

  async whenUserSearches(searchTerm: string) {
    const searchInput = this.page
      .getByRole("searchbox")
      .or(this.page.getByPlaceholder(/buscar/i));
    await searchInput.fill(searchTerm);
  }

  async whenUserNavigatesTo(path: string) {
    await this.page.goto(path);
  }

  /** When: user selects a sidebar menu item */
  async whenUserOpensSidebarModule(moduleLabel: string) {
    await this.page.getByTestId("sidebar").getByText(moduleLabel).click();
    await this.page.waitForLoadState("networkidle");
  }

  /** When: user clicks a row in a data table */
  async whenUserClicksTableRow(rowIndex: number = 0) {
    const table = this.page.getByRole("table");
    const rows = table.getByRole("row");
    await rows.nth(rowIndex + 1).click(); // +1 to skip header
  }

  /** When: user selects a date in a date field */
  async whenUserPicksDate(fieldLabel: string, date: string) {
    const scope = await this.formScope();
    await scope.getByLabel(new RegExp(fieldLabel, "i")).fill(date);
  }

  /** When: user uploads a file */
  async whenUserUploadsFile(inputLabel: string, filePath: string) {
    const scope = await this.formScope();
    await scope.getByLabel(new RegExp(inputLabel, "i")).setInputFiles(filePath);
  }

  /** When: user confirms a dialog */
  async whenUserConfirmsDialog() {
    await this.page.getByRole("button", { name: /confirmar|si|aceptar/i }).click();
  }

  // =================== THEN steps ===================

  async thenModalIsVisible(modalTitle?: string) {
    const dialog = this.page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    if (modalTitle) {
      await expect(dialog.getByText(modalTitle)).toBeVisible();
    }
  }

  async thenModalIsNotVisible() {
    await expect(this.page.getByRole("dialog")).not.toBeVisible();
  }

  async thenPageHasHeading(heading: string) {
    await expect(
      this.page.getByRole("heading", { name: new RegExp(heading, "i") })
    ).toBeVisible();
  }

  async thenElementIsVisible(elementName: string) {
    await expect(this.page.getByText(new RegExp(elementName, "i"))).toBeVisible();
  }

  async thenElementHasText(elementTestId: string, text: string) {
    await expect(this.page.getByTestId(elementTestId)).toHaveText(
      new RegExp(text, "i")
    );
  }

  async thenTableIsVisible() {
    await expect(this.page.getByRole("table")).toBeVisible();
  }

  async thenTableContainsText(text: string) {
    const table = this.page.getByRole("table");
    await expect(table).toContainText(text);
  }

  async thenTableRowCountIsAtLeast(minCount: number) {
    const rows = await this.page.getByRole("table").getByRole("row").count();
    expect(rows - 1).toBeGreaterThanOrEqual(minCount); // -1 for header
  }

  async thenShowsErrorMessage(message: string) {
    await expect(
      this.page.locator("[data-sonner-toast]").getByText(new RegExp(message, "i"))
    ).toBeVisible({ timeout: 5000 });
  }

  async thenUrlIs(expectedUrl: RegExp) {
    await expect(this.page).toHaveURL(expectedUrl);
  }

  async thenFormHasField(fieldLabel: string) {
    const scope = await this.formScope();
    await expect(scope.getByLabel(new RegExp(fieldLabel, "i"))).toBeVisible();
  }

  async thenShowsSuccessMessage(message: string = "éxito") {
    await expect(
      this.page.locator("[data-sonner-toast]").getByText(new RegExp(message, "i"))
    ).toBeVisible({ timeout: 5000 });
  }

  /** Then: a toast notification is displayed */
  async thenToastIsVisible(message?: string) {
    const toast = this.page.getByRole("alert");
    await expect(toast).toBeVisible({ timeout: 5000 });
    if (message) {
      await expect(toast).toContainText(message);
    }
  }

  /** Then: a loading spinner is visible */
  async thenLoadingIsVisible() {
    await expect(
      this.page.getByRole("progressbar").or(this.page.locator(".loading"))
    ).toBeVisible();
  }

  /** Then: loading completes */
  async thenLoadingIsNotVisible() {
    await expect(
      this.page.getByRole("progressbar").or(this.page.locator(".loading"))
    ).not.toBeVisible({ timeout: 10000 });
  }

  /** Then: the page has no horizontal scroll at the current viewport (ui-ux-design.md — Responsive Design) */
  async thenHasNoHorizontalOverflow() {
    const overflow = await this.page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    );
    expect(overflow).toBe(true);
  }

  /** Then: a dropdown option is selectable */
  async thenDropdownHasOption(dropdownLabel: string, option: string) {
    await this.page
      .getByRole("combobox", { name: new RegExp(dropdownLabel, "i") })
      .click();
    await expect(
      this.page.getByRole("option", { name: new RegExp(option, "i") })
    ).toBeVisible();
  }

  /** Then: the detail view shows a specific value */
  async thenDetailShowsValue(label: string, value: string) {
    const detailRow = this.page.getByText(new RegExp(label, "i")).locator("..");
    await expect(detailRow).toContainText(value);
  }

  // =================== Utility methods ===================

  async openNewRecordModal(buttonName: string = "nuevo|registrar") {
    await this.whenUserClicksButton(buttonName);
    await this.thenModalIsVisible();
  }

  async fillAndSubmitForm(fields: Record<string, string>) {
    for (const [label, value] of Object.entries(fields)) {
      await this.whenUserFillsField(label, value);
    }
    await this.whenUserSubmitsForm();
  }

  /** Navigate to a dashboard sub-module and wait for content */
  async navigateToModule(modulePath: string) {
    await this.givenUserIsOnPage(`/dashboard/${modulePath}`);
    await this.page.waitForLoadState("networkidle");
  }

  /** Wait for page to fully load */
  async waitForPageReady() {
    await this.page.waitForLoadState("networkidle");
    await this.page.waitForLoadState("domcontentloaded");
  }

  /** Pause briefly for animations/transitions */
  async waitForAnimations(ms: number = 500) {
    await this.page.waitForTimeout(ms);
  }
}

/**
 * Test data factories for use cases
 */
export const TestData = {
  persona: {
    nombre: "Juan",
    apellido: "Pérez",
    tipoIdentificacion: "DNI",
    numeroIdentificacion: "12345678",
    telefono: "1234567890",
    correo: "juan.perez@test.com",
    nacionalidad: "Argentina",
    fechaNacimiento: "1990-01-01",
    cuit: "20-12345678-9",
  },
  cliente: {
    nacionalidad: "Argentina",
    fechaNacimiento: "1990-01-01",
    cuit: "20-12345678-9",
    estadoCivil: "Soltero",
    sexo: "Masculino",
    ocupacion: "Empleado",
    domicilio: "Calle Falsa 123",
  },
  presupuesto: {
    tipoTramite: "Escritura",
    observaciones: "Presupuesto de prueba",
    fecha: new Date().toISOString().split("T")[0],
    monto: 1000,
  },
  gestion: {
    numeroGestion: "TEST-001",
    detalle: "Gestión de prueba",
    fechaInicio: new Date().toISOString().split("T")[0],
  },
  escritura: {
    numeroEscritura: "ESC-001",
    fecha: new Date().toISOString().split("T")[0],
    cuerpo: "Contenido de la escritura de prueba",
    estado: "Firmada",
  },
  usuario: {
    username: "testuser",
    password: "Test1234!",
    tipo: "EMPLEADO",
    estado: "Habilitado",
  },
  suplencia: {
    fechaInicio: new Date().toISOString().split("T")[0],
    motivo: "Suplencia de prueba",
  },
  pago: {
    monto: 5000,
    formaPago: "Efectivo",
    observaciones: "Pago de prueba E2E",
  },
  testimonio: {
    fecha: new Date().toISOString().split("T")[0],
    estado: "Pendiente",
  },
  documentoPresentado: {
    nombre: "Documento de prueba",
    tieneDeuda: false,
  },
  catalogo: {
    tipoTramite: { nombre: "Test Tramite", descripcion: "Catálogo de prueba" },
    tipoDocumento: { nombre: "Test Documento", descripcion: "Catálogo de prueba" },
    concepto: { nombre: "Test Concepto", descripcion: "Catálogo de prueba", valor: 100 },
    estadoGestion: { nombre: "Test Estado", descripcion: "Catálogo de prueba" },
    folio: { numero: 9999, disponible: true },
  },
};

/**
 * Seed data references — use these in tests to get IDs from global setup
 */
export function getSeedId(entity: string): number | null {
  const map: Record<string, string> = {
    persona: "E2E_SEED_PERSONA_ID",
    presupuesto: "E2E_SEED_PRESUPUESTO_ID",
    concepto: "E2E_SEED_CONCEPTO_ID",
    tipo_tramite: "E2E_SEED_TIPO_TRAMITE_ID",
    usuario: "E2E_SEED_USUARIO_ID",
    folio: "E2E_SEED_FOLIO_ID",
    estado_gestion: "E2E_SEED_ESTADO_GESTION_ID",
  };
  const envKey = map[entity];
  if (!envKey) return null;
  const val = process.env[envKey];
  return val ? Number(val) : null;
}
