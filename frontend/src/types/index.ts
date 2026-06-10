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

export interface Rol {
  idRol?: number;
  nombre?: string;
  descripcion?: string;
  activo?: boolean;
  modulos?: string[];
}

export interface Usuario {
  idUsuario?: number;
  nombre?: string;
  contrasenia?: string;
  tipo?: string;
  activo?: boolean;
  persona?: Persona;
  rol?: { idRol: number; nombre: string } | null;
}

export interface TipoDeTramite {
  idTipoDeTramite?: number;
  nombre?: string;
  descripcion?: string;
  seArchiva?: boolean;
  seInscribe?: boolean;
  workflowDefinitionId?: number | null;
  workflowDefinitionNombre?: string | null;
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
  tiposDeFolio?: { idTipoFolio?: number; nombre?: string };
  personaEscribano?: { idPersona?: number; registroEscribano?: number };
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
// Workflow types (CU70, CU71, CU72, CU73)
// ──────────────────────────────────────────────
export type WorkflowNodeType = "INITIAL" | "INTERMEDIATE" | "FINAL";

export interface WorkflowDefinition {
  id?: number;
  nombre?: string;
  descripcion?: string;
  activo?: boolean;
  version?: number;
}

export interface WorkflowNode {
  id?: number;
  workflowDefinitionId?: number;
  estadoGestionId?: number;
  estadoGestionNombre?: string;
  tipo?: WorkflowNodeType;
  posicionX?: number;
  posicionY?: number;
  version?: number;
}

export interface WorkflowTransition {
  id?: number;
  workflowDefinitionId?: number;
  nodoOrigenId?: number;
  nodoDestinoId?: number;
  condicion?: string;
  descripcion?: string;
  version?: number;
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

// ──────────────────────────────────────────────
// Workflow Trace (dashboard)
// ──────────────────────────────────────────────

/** A single historial entry for the workflow trace. */
export interface HistorialEntry {
  idHistorial?: number;
  estadoGestionId?: number;
  estadoGestionNombre?: string;
  fecha?: string;
  observaciones?: string;
}

/** Aggregated response from GET /gestiones/{id}/workflow-trace. */
export interface GestionWorkflowTrace {
  gestionId: number;
  numero?: number;
  encabezado?: string;
  fechaInicio?: string;
  estadoActual?: string;
  workflowDefinition?: WorkflowDefinition;
  nodes: WorkflowNode[];
  transitions: WorkflowTransition[];
  historial: HistorialEntry[];
  /** nodeId → "completed" | "in_progress" | "pending" */
  nodeStatuses: Record<number, string>;
}
