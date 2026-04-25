// ──────────────────────────────────────────────
// Core domain types — mirrors backend JPA entities
// Field names match backend JSON serialization (camelCase from getter names)
// ──────────────────────────────────────────────

export interface DtoUsuario {
  idUsuario?: number;
  nombre: string;
  contrasenia?: string;
  tipo?: string;
  valido?: boolean;
  idPersona?: number;
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
}

export interface Usuario {
  idUsuario?: number;
  nombre?: string;
  contrasenia?: string;
  tipo?: string;
  activo?: boolean;
  persona?: Persona;
}

/** Backend field: idTipoTramite (NOT idTipoDeTramite) */
export interface TipoDeTramite {
  idTipoTramite?: number;
  nombre?: string;
  descripcion?: string;
  seArchiva?: boolean;
  seInscribe?: boolean;
}

export interface TipoDeDocumento {
  idTipoDeDocumento?: number;
  nombre?: string;
}

export interface TipoDeFolio {
  idTipoDeFolio?: number;
  nombre?: string;
}

/** Backend field: idEstadoGestion (NOT idEstadoDeGestion) */
export interface EstadoDeGestion {
  idEstadoGestion?: number;
  nombre?: string;
}

export interface Concepto {
  idConcepto?: number;
  nombre?: string;
  descripcion?: string;
  valor?: number;
}

export interface Folio {
  idFolio?: number;
  numero?: number;
  anio?: number;
  estado?: string;
  tipoDeFolio?: TipoDeFolio;
}

export interface Tramite {
  idTramite?: number;
  tipo?: TipoDeTramite;
  personaList?: Persona[];
  documentosPresentados?: DocumentoPresentado[];
}

export interface DocumentoPresentado {
  idDocumentoPresentado?: number;
  tipo?: TipoDeDocumento;
  entregado?: boolean;
  fecha?: string;
}

export interface Historial {
  idHistorial?: number;
  estado?: EstadoDeGestion;
  fecha?: string;
  observaciones?: string;
}

export interface GestionDeEscritura {
  idGestion?: number;
  numero?: number;
  tramiteList?: Tramite[];
  historialList?: Historial[];
}

export interface Item {
  idItem?: number;
  concepto?: Concepto;
  cantidad?: number;
  precio?: number;
  /** Backend: fkIdPresupuesto relationship */
  presupuesto?: { idPresupuesto?: number };
}

/**
 * PlantillaPresupuesto — uses composite PK (fkIdTipoTramite + fkIdConcepto).
 * Backend: plantillaPresupuestoPK embedded object.
 */
export interface PlantillaPresupuestoPK {
  fkIdTipoTramite: number;
  fkIdConcepto: number;
}

export interface PlantillaPresupuesto {
  plantillaPresupuestoPK?: PlantillaPresupuestoPK;
  observaciones?: string;
  tipoDeTramite?: TipoDeTramite;
  concepto?: Concepto;
}

export interface Presupuesto {
  idPresupuesto?: number;
  fecha?: string;
  monto?: number;
  estado?: string;
  persona?: Persona;
  itemList?: Item[];
}

export interface Escritura {
  idEscritura?: number;
  numero?: number;
  fecha?: string;
  folio?: Folio;
  gestion?: GestionDeEscritura;
}

export interface Pago {
  idPago?: number;
  monto?: number;
  fecha?: string;
  metodoPago?: string;
  presupuesto?: Presupuesto;
}

export interface Suplencia {
  idSuplencia?: number;
  escribano?: Persona;
  suplente?: Persona;
  desde?: string;
  hasta?: string;
}

/** Backend: RegistroAuditoria with fields idRegistroAuditoria, detalleOperacion, modulo, fecha */
export interface RegistroAuditoria {
  idRegistroAuditoria?: number;
  detalleOperacion?: string;
  modulo?: string;
  fecha?: string;
  fkIdUsuario?: Usuario;
}

// ──────────────────────────────────────────────
// UI / Navigation types
// ──────────────────────────────────────────────

export interface NavItem {
  label: string;
  href: string;
  icon?: string;
  adminOnly?: boolean;
}
