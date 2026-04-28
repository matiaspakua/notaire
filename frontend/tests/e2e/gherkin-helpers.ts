/**
 * Gherkin-style test helpers for Playwright E2E tests
 * Implements Given-When-Then pattern for all CU use cases
 */

import { type Page, type Locator, expect } from "@playwright/test";

/**
 * Gherkin step definitions and helpers
 */
export class GherkinSteps {
  constructor(public page: Page) {}

  // =================== GIVEN steps ===================

  async givenUserIsAuthenticatedAs(role: "admin" | "empleado" = "admin") {
    await this.page.context().addCookies([
      {
        name: "notaire-auth-status",
        value: "authenticated",
        domain: "localhost",
        path: "/",
      },
    ]);

    await this.page.addInitScript(() => {
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: {
            user: { nombre: role, tipo: role.toUpperCase(), valido: true },
            isAuthenticated: true,
          },
          version: 0,
        })
      );
    });
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
    await expect(this.page.getByText(moduleName)).toBeVisible();
  }

  // =================== WHEN steps ===================

  async whenUserClicksButton(buttonName: string) {
    await this.page.getByRole("button", { name: new RegExp(buttonName, "i") }).click();
  }

  async whenUserFillsField(fieldLabel: string, value: string) {
    await this.page.getByLabel(new RegExp(fieldLabel, "i")).fill(value);
  }

  async whenUserSelectsFromDropdown(dropdownLabel: string, option: string) {
    await this.page.getByRole("combobox", { name: new RegExp(dropdownLabel, "i") }).click();
    await this.page.getByRole("option", { name: new RegExp(option, "i") }).click();
  }

  async whenUserSubmitsForm() {
    await this.page.getByRole("button", { name: /confirmar|guardar|crear|registrar/i }).click();
  }

  async whenUserCancelsForm() {
    await this.page.getByRole("button", { name: /cancelar/i }).click();
  }

  async whenUserSearches(searchTerm: string) {
    const searchInput = this.page.getByRole("searchbox").or(this.page.getByPlaceholder(/buscar/i));
    await searchInput.fill(searchTerm);
  }

  async whenUserNavigatesTo(path: string) {
    await this.page.goto(path);
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
    await expect(this.page.getByRole("heading", { name: new RegExp(heading, "i") })).toBeVisible();
  }

  async thenElementIsVisible(elementName: string) {
    await expect(this.page.getByText(new RegExp(elementName, "i"))).toBeVisible();
  }

  async thenElementHasText(elementTestId: string, text: string) {
    await expect(this.page.getByTestId(elementTestId)).toHaveText(new RegExp(text, "i"));
  }

  async thenTableIsVisible() {
    await expect(this.page.getByRole("table")).toBeVisible();
  }

  async thenShowsErrorMessage(message: string) {
    await expect(this.page.getByText(new RegExp(message, "i"))).toBeVisible({ timeout: 5000 });
  }

  async thenUrlIs(expectedUrl: RegExp) {
    await expect(this.page).toHaveURL(expectedUrl);
  }

  async thenFormHasField(fieldLabel: string) {
    await expect(this.page.getByLabel(new RegExp(fieldLabel, "i"))).toBeVisible();
  }

  async thenShowsSuccessMessage(message: string = "éxito") {
    await expect(this.page.getByText(new RegExp(message, "i"))).toBeVisible({ timeout: 5000 });
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
};
