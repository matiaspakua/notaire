/**
 * CU24 - Generar libro de índices (Gherkin style)
 * CU25 - Generar Declaración Jurada del mes
 * CU26 - Ingresar nuevo tipo de trámite
 * CU27 - Ingresar nuevo tipo de documento
 * CU28 - Ingresar nuevos folios
 * CU29 - Ingresar nuevo concepto
 * CU30 - Ingresar nuevo estado de Gestión
 * CU31 - Modificar tipo de trámite
 * CU32 - Modificar tipo de documento
 * CU33 - Modificar folio
 * CU34 - Modificar concepto
 * CU35 - Modificar estado de Gestión
 * CU36 - Ingresar tipos de folio
 * CU37 - Eliminar concepto
 * CU38 - Eliminar tipo de documento
 * CU39 - Crear Plantilla Presupuesto
 * CU40 - Modificar tipo de folio
 * CU42 - Informar próximos vencimientos
 * CU43 - Reingresar documentación
 * CU44 - Reingresar testimonio
 * CU45 - Modificar presupuesto
 * CU49 - Eliminar Plantilla Presupuesto
 * CU50 - Generar Declaración Jurada de Rentas
 * CU55 - Modificar Cliente
 * CU56 - Registrar inscripcion
 * CU57 - Eliminar tipo de trámite
 * CU58 - Eliminar tipo de folio
 * CU59 - Consultar Suplencias
 * CU60 - Buscar Presupuesto
 * CU62 - Buscar Escritura
 * CU63 - Buscar Folios
 * CU64 - Buscar Tipo de tramite
 * CU65 - Buscar Tipos de documentos
 * CU66 - Buscar Conceptos
 * CU67 - Buscar Estados de Gestión
 * CU68 - Buscar tipos de folios
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps, TestData } from "./gherkin-helpers";

test.describe("CU24 - Generar libro de índices", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/reportes");
  });

  test("CU24-GW01: Given on reportes, When click libro índices, Then generates", async () => {
    // Given
    await steps.givenModuleIsVisible("Reportes");

    // When
    await steps.whenUserClicksButton("libro índices");

    // Then
    await steps.thenShowsSuccessMessage("generado");
  });
});

test.describe("CU25 - Generar Declaración Jurada del mes", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/reportes");
  });

  test("CU25-GW01: Given on reportes, When select month and generate, Then creates", async () => {
    // Given
    await steps.givenModuleIsVisible("Reportes");

    // When
    await steps.whenUserSelectsFromDropdown("mes", "Enero");
    await steps.whenUserClicksButton("declaración jurada");

    // Then
    await steps.thenShowsSuccessMessage("generada");
  });
});

test.describe("CU26 - Ingresar nuevo tipo de trámite", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/tramites");
  });

  test("CU26-GW01: Given on tipos tramite, When click nuevo, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Tipos de Trámite");

    // When
    await steps.whenUserClicksButton("nuevo tipo");

    // Then
    await steps.thenModalIsVisible();
    await steps.thenFormHasField("nombre");
  });
});

test.describe("CU27 - Ingresar nuevo tipo de documento", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/documentos");
  });

  test("CU27-GW01: Given on tipos documento, When click nuevo, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Tipos de Documento");

    // When
    await steps.whenUserClicksButton("nuevo tipo");

    // Then
    await steps.thenModalIsVisible();
  });
});

test.describe("CU29 - Ingresar nuevo concepto", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/conceptos");
  });

  test("CU29-GW01: Given on conceptos, When click nuevo, Then modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Conceptos");

    // When
    await steps.whenUserClicksButton("nuevo concepto");

    // Then
    await steps.thenModalIsVisible("Nuevo Concepto");
    await steps.thenFormHasField("nombre");
    await steps.thenFormHasField("valor");
  });

  test("CU29-GW02: Given form open, When fill and submit, Then concepto created", async () => {
    // Given
    await steps.whenUserClicksButton("nuevo concepto");
    await steps.thenModalIsVisible();

    // When
    await steps.fillAndSubmitForm({
      "nombre": "Nuevo Concepto Test",
      "valor": "100",
      "porcentaje": "10",
    });

    // Then
    await steps.thenShowsSuccessMessage("creado");
  });
});

test.describe("CU39 - Crear Plantilla Presupuesto", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
  });

  test("CU39-GW01: Given on presupuestos, When click plantillas, Then shows plantillas", async () => {
    // Given
    await steps.givenModuleIsVisible("Presupuestos");

    // When
    await steps.whenUserClicksButton("plantillas");

    // Then
    await steps.thenPageHasHeading("Plantillas de Presupuesto");
  });
});

test.describe("CU42 - Informar próximos vencimientos", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard");
  });

  test("CU42-GW01: Given on dashboard, When view alerts, Then shows vencimientos", async () => {
    // Given
    await steps.givenModuleIsVisible("Dashboard");

    // When
    await steps.whenUserClicksButton("alertas");

    // Then
    await steps.thenElementIsVisible("próximos vencimientos");
  });
});

test.describe("CU60 - Buscar Presupuesto", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
  });

  test("CU60-GW01: Given on presupuestos, When search by number, Then shows results", async () => {
    // Given
    await steps.givenModuleIsVisible("Presupuestos");

    // When
    await steps.whenUserSearches("TEST-001");

    // Then
    await steps.thenTableIsVisible();
  });
});

test.describe("CU62 - Buscar Escritura", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/escrituras");
  });

  test("CU62-GW01: Given on escrituras, When search by number, Then shows results", async () => {
    // Given
    await steps.givenModuleIsVisible("Escrituras");

    // When
    await steps.whenUserSearches("ESC");

    // Then
    await steps.thenTableIsVisible();
  });
});

test.describe("CU65 - Buscar Tipos de documentos", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/documentos");
  });

  test("CU65-GW01: Given on tipos documento, When search, Then shows results", async () => {
    // Given
    await steps.givenModuleIsVisible("Tipos de Documento");

    // When
    await steps.whenUserSearches("Escritura");

    // Then
    await steps.thenTableIsVisible();
  });
});

test.describe("CU66 - Buscar Conceptos", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administracion/conceptos");
  });

  test("CU66-GW01: Given on conceptos, When search, Then shows results", async () => {
    // Given
    await steps.givenModuleIsVisible("Conceptos");

    // When
    await steps.whenUserSearches("Arancel");

    // Then
    await steps.thenTableIsVisible();
  });
});
