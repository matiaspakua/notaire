// ──────────────────────────────────────────────
// Core domain types — mirrors backend JPA entities
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

export interface TipoDeTramite {
  idTipoDeTramite?: number;
  nombre?: string;
  descripcion?: string;
  seArchiva?: boolean;
  seInscribe?: boolean;
}

export interface TipoDeDocumento {
  idTipoDocumento?: number;
  nombre?: string;
}

export interface TipoDeFolio {
  idTipoDeFolio?: number;
  nombre?: string;
}

export interface EstadoDeGestion {
  idEstadoGestion?: number;
  nombre?: string;
  observaciones?: string;
  version?: number;
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
  observaciones?: string;
  tipoDeFolio?: TipoDeFolio;
  disponible?: boolean;
  version?: number;
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

export interface DocumentoPresentadoRequest {
  tipoId: number | null;
  fecha: string | null;
  entregado: boolean;
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
  presupuesto?: Presupuesto;
}

export interface PlantillaPresupuestoPK {
  fkIdTipoTramite: number;
  fkIdConcepto: number;
}

export interface PlantillaPresupuesto {
  // Real backend model: a (tipo de trámite × concepto) pair with observaciones.
  plantillaPresupuestoPK?: PlantillaPresupuestoPK;
  tipoDeTramite?: TipoDeTramite;
  concepto?: Concepto;
  observaciones?: string;
  version?: number;
  // Legacy fields kept for backward compatibility with older unit tests.
  idPlantillaPresupuesto?: number;
  nombre?: string;
  descripcion?: string;
  itemList?: Item[];
}

export interface Presupuesto {
  idPresupuesto?: number;
  fecha?: string;
  monto?: number;
  estado?: string;
  persona?: Persona;
  plantilla?: PlantillaPresupuesto;
  itemList?: Item[];
}

export interface Escritura {
  idEscritura?: number;
  numero?: number;
  fechaEscrituracion?: string;
  cuerpo?: string;
  estado?: string;
  folio?: Folio;
  gestion?: GestionDeEscritura;
}

export interface Pago {
  idPago?: number;
  idPresupuesto?: number;
  monto?: number;
  fecha?: string;
  metodoPago?: string;
  observaciones?: string;
  presupuesto?: Presupuesto;
}

export interface Suplencia {
  idSuplencia?: number;
  fkIdSuplantado?: Persona;
  fkIdSuplente?: Persona;
  fechaInicio?: string;
  fechaFin?: string;
}

export interface RegistroAuditoria {
  idRegistroAuditoria?: number;
  usuarios?: { nombre?: string };
  detalleOperacion?: string;
  fecha?: string;
  modulo?: string;
}

// ──────────────────────────────────────────────
// UI / Navigation types
// ──────────────────────────────────────────────

export interface Inmueble {
  idInmueble?: number;
  nomenclaturaCatastral?: string;
  valuacionFiscal?: string;
  domicilio?: string;
  observaciones?: string;
}

export interface Copia {
  idCopia?: number;
  numero?: number;
  fechaImpresion?: string;
  fechaRetiro?: string;
  observaciones?: string;
  testimonio?: { idTestimonio?: number; numero?: number };
  persona?: Persona;
}

export interface NavItem {
  label: string;
  href: string;
  icon?: string;
  adminOnly?: boolean;
}
