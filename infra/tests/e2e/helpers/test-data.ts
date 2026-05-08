/**
 * Type definitions and test data factories for E2E tests.
 *
 * These types mirror the API DTOs used by the backend, ensuring
 * the verification helpers have correct type information.
 */

// ======================= Type definitions (mirror API DTOs) =======================

export interface Concepto {
  idConcepto?: number;
  nombre?: string;
  descripcion?: string;
  valor?: number;
  [key: string]: unknown;
}

export interface TipoDeDocumento {
  idTipoDeDocumento?: number;
  nombre?: string;
  [key: string]: unknown;
}

export interface EstadoDeGestion {
  idEstadoDeGestion?: number;
  nombre?: string;
  descripcion?: string;
  [key: string]: unknown;
}

export interface Folio {
  idFolio?: number;
  numero?: number;
  anio?: number;
  disponible?: boolean;
  tipoDeFolio?: { nombre?: string };
  [key: string]: unknown;
}

export interface Persona {
  idPersona?: number;
  nombre?: string;
  apellido?: string;
  dni?: string;
  cuil?: string;
  email?: string;
  telefono?: string;
  domicilio?: string;
  esCliente?: boolean;
  [key: string]: unknown;
}

export interface Usuario {
  idUsuario?: number;
  nombre?: string;
  contrasenia?: string;
  tipo?: string;
  activo?: boolean;
  [key: string]: unknown;
}

export interface TipoDeTramite {
  idTipoDeTramite?: number;
  nombre?: string;
  descripcion?: string;
  [key: string]: unknown;
}

export interface PlantillaPresupuesto {
  idPlantillaPresupuesto?: number;
  nombre?: string;
  tipoTramiteId?: number;
  itemList?: Array<unknown>;
  [key: string]: unknown;
}

export interface Presupuesto {
  idPresupuesto?: number;
  fecha?: string;
  monto?: number;
  estado?: string;
  [key: string]: unknown;
}

// ======================= Test data factories =======================

let uniqueCounter = Date.now();

/** Reset the unique counter (call in beforeEach) */
export function resetTestDataCounter() {
  uniqueCounter = Date.now();
}

/** Generate a unique suffix for test data to avoid collisions */
export function uniqueSuffix(): string {
  return `e2e-${uniqueCounter++}`;
}

export const TestData = {
  /** CU29 - Concepto */
  concepto: () => ({
    nombre: `Concepto Test ${uniqueSuffix()}`,
    descripcion: `Concepto de prueba E2E`,
    valor: 1000.50,
  }),

  /** CU27 - Tipo de Documento */
  tipoDocumento: () => ({
    nombre: `Tipo Doc Test ${uniqueSuffix()}`,
  }),

  /** CU30 - Estado de Gestión */
  estadoGestion: () => ({
    nombre: `Estado Test ${uniqueSuffix()}`,
    descripcion: `Estado de gestión de prueba E2E`,
  }),

  /** CU28 - Folio */
  folio: () => ({
    numero: Math.floor(Math.random() * 90000) + 10000,
    anio: new Date().getFullYear(),
  }),

  /** CU17 - Persona */
  persona: () => ({
    nombre: `PersonaE2E`,
    apellido: `Test${uniqueSuffix()}`,
    dni: `${Math.floor(Math.random() * 40000000) + 10000000}`,
    email: `persona${uniqueSuffix()}@test.com`,
    telefono: `11${Math.floor(Math.random() * 10000000)}`,
    domicilio: `Calle Test 123`,
    esCliente: false,
  }),

  /** CU18 - Cliente (extends Persona) */
  cliente: () => ({
    nombre: `ClienteE2E`,
    apellido: `Test${uniqueSuffix()}`,
    dni: `${Math.floor(Math.random() * 40000000) + 10000000}`,
    email: `cliente${uniqueSuffix()}@test.com`,
    telefono: `11${Math.floor(Math.random() * 10000000)}`,
    domicilio: `Av. Cliente 456`,
    esCliente: true,
  }),

  /** CU20 - Usuario */
  usuario: () => ({
    nombre: `usertest${uniqueSuffix()}`,
    contrasenia: `TestPass123!`,
    tipo: "EMPLEADO",
    activo: true,
  }),

  /** CU26 - Tipo de Trámite */
  tipoTramite: () => ({
    nombre: `Tramite Test ${uniqueSuffix()}`,
    descripcion: `Tipo de trámite de prueba E2E`,
  }),

  /** CU39 - Plantilla Presupuesto */
  plantilla: () => ({
    nombre: `Plantilla Test ${uniqueSuffix()}`,
  }),

  /** CU01 - Presupuesto */
  presupuesto: () => ({
    fecha: new Date().toISOString().split("T")[0],
    monto: Math.floor(Math.random() * 100000) + 5000,
    estado: "BORRADOR",
  }),
};

/** Credenciales de login para pruebas */
export const CREDENTIALS = {
  username: "admin",
  password: "admin",
};
