# Migration Traceability Matrix

This matrix links analyst use cases to backend API endpoints, Swing forms, and test coverage. Focus on **what remains to migrate** to have all forms in the Swing module and all APIs ready.

## Coverage Legend

- `Implemented`: endpoint/form flow exists and uses REST.
- `Partial`: flow exists but some forms still use ControllerNegocio or missing filtered endpoints.
- `Missing`: no explicit backend flow or form not yet migrated.

## CU → API → Swing → Tests

| Domain | Use Cases | Backend API | Swing Forms | Status | Pendiente |
|---|---|---|---|---|---|
| Presupuestos/Pagos | CU01, CU15, CU45, CU47, CU60 | `/api/v1/presupuestos`, `/persona/{id}`, `/api/v1/items`, `/presupuesto/{id}`, `/api/v1/pagos` | RegistrarPago ✅, ConsultarPagos ✅, BuscarPresupuesto ✅, ModificarPresupuesto ✅, CrearPresupuesto, Lista* | Partial | CrearPresupuesto (eliminar ControllerNegocio), DetalleValoresTramites, BuscarInmueble |
| Gestiones | CU02, CU13, CU16, CU19, CU53, CU67 | `/api/v1/gestiones`, `/cliente/{idPersona}`, `/api/v1/historial`, `/gestion/{idGestion}`, `/api/v1/estado-gestion` | IniciarGestion ✅, ArchivarGestion ✅, ModificarGestion, BuscarGestion, DetalleGestion, VerHistorialGestion, ListaGestionesCliente | Partial | ModificarGestion, BuscarGestion, DetalleGestion, ListaGestionesCliente (obtenerEstadoActualDeGestion) |
| Clientes/Personas | CU17, CU18, CU41, CU46, CU61 | `/api/v1/personas`, `/buscar` (nombre, apellido, numeroIdentificacion, idTipoIdentificacion), `/api/v1/tipo-identificacion` | DarAltaPersona, AdministrarCliente, BuscarCliente, ListarPersonas, BuscarGestionesCliente ✅, ModificarCliente | Partial | Clientes (buscarPersonasClientes), BuscarCliente, DarAltaPersona, AdministrarCliente, ListarPersonas; API: `/personas/buscar?esCliente=true` |
| Documentación | CU03, CU04, CU09, CU10, CU43, CU65 | `/api/v1/documento-presentado`, `/api/v1/tipo-de-documento`, reportes | IngresarDocumento ✅, ReingresarDocumentos ✅, RegistrarEntregaDocumentos ✅, ConsultarDeudasDocumentos ✅, ConsultarVencimientosDocumentos ✅, ListarDocumentos ✅ | Partial | NomenclaturaCatastral (si aplica) |
| Escrituras | CU05, CU06, CU52, CU56, CU62 | `/api/v1/escrituras`, `/api/v1/inmueble`, `/api/v1/copia` | BuscarEscritura, ListaEscrituras, DetalleEscritura | Partial | BuscarEscritura, ListaEscrituras, DetalleEscritura |
| Testimonios | CU07, CU08, CU12, CU44 | `/api/v1/testimonio`, `/api/v1/movimiento-testimonio` | GenerarTestimonio, VerificarTestimonio, RetirarTestimonio | Missing | Los 3 formularios usan ControllerNegocio |
| Inscripciones | CU53, CU67 (rel.) | gestiones, tramites, inmueble | IngresarParaInscripcion, RegistrarInscripcion, RegistrarReingreso | Missing | Los 3 formularios usan ControllerNegocio |
| Administración Catálogos | CU26–40, CU57, CU58, CU64, CU66, CU68 | tipo-tramite, tipo-folio, tipo-de-documento, conceptos, estado-gestion, plantilla-presupuestos | IngresarConcepto ✅, ModificarConcepto, EliminarConcepto, IngresarDocumento ✅, ModificarDocumento, EliminarDocumento, DarAltaEscribano ✅, ConsultarSuplencias ✅, IngresarEstadoGestion ✅, ModificarEstadoGestion ✅, IngresarTipoDeFolio ✅, ModificarEliminarFolio ✅ | Partial | Tramites (3), PlantillasPresupuesto (Crear/Modificar con ControllerNegocio) |
| Usuarios/Auditoría | CU20, CU21, CU23 | `/api/v1/usuarios`, `/login`, `/persona/{idPersona}`, `/api/v1/registro-auditoria/usuario/{idUsuario}` | DarAltaUsuario ✅, ModificarUsuario, VerRegistroActividadesUsuario ✅, ActividadUsuario ✅, ListarPersonasUsuario ✅ | Partial | ModificarUsuario; API: `/usuarios/validate-password` (para cambio contraseña) |
| Reportes/DDJJ | CU24, CU25, CU42, CU50 | `/api/v1/reportes/*` (10 endpoints PDF) | AdministradorReportes, GenerarIndices, GenerarDDJJ, DeclaracionJurada | Partial/Missing | AdministradorReportes debe usar API reportes; DDJJ/folios migrar a REST |
| Protocolo | CU24, CU25, CU42 | `/api/v1/folio`, `/api/v1/tipo-folio`, reportes | Folios, IngresarFolios, ModificarFolio, GenerarIndices, GenerarDDJJ, DeclaracionJurada, DeclaracionJuradaRentas | Partial | Folios, IngresarFolios, ModificarFolio (ControllerNegocio) |

## Formularios con ControllerNegocio (prioridad de migración)

| Prioridad | Formulario | ControllerNegocio usado |
|-----------|------------|--------------------------|
| 1 | Clientes | buscarPersonasClientes |
| 2 | BuscarCliente | buscarPersonaNombreApellido, asociarFkTipoIdentificacion, buscarPersonaTipoNumeroIdentificacion, listarTiposIdentificacion |
| ~~3~~ | ~~ActividadUsuario~~ | ✅ Migrado Batch A |
| ~~4~~ | ~~DarAltaUsuario~~ | ✅ Migrado Batch A |
| ~~5~~ | ~~ListarPersonasUsuario~~ | ✅ Migrado Batch A |
| 6 | BuscarGestion | listarTiposIdentificacion, buscarPersonaNombreApellidoConGestion, asociarFkTipoIdentificacion, buscarPersonaTipoNumeroIdentificacionConGestion |
| 7 | ListaGestionesCliente | obtenerEstadoActualDeGestion |
| 8 | DarAltaPersona, AdministrarCliente | listarTiposIdentificacion, asociarFkTipoIdentificacion |
| 9 | ListarPersonas | asociarFkTipoIdentificacion, buscarPersonaTipoNumeroIdentificacion |
| 10 | ModificarGestion, IniciarGestion, DetalleGestion | ControllerNegocio (eliminar) |
| 11 | GenerarTestimonio, VerificarTestimonio, RetirarTestimonio | ControllerNegocio |
| 12 | IngresarParaInscripcion, RegistrarInscripcion, RegistrarReingreso | ControllerNegocio |
| 13 | BuscarEscritura, ListaEscrituras, DetalleEscritura | ControllerNegocio |
| 14 | CrearPresupuesto, DetalleValoresTramites, BuscarInmueble | ControllerNegocio |
| 15 | ModificarFolio, IngresarFolios | ControllerNegocio |
| 16 | CrearPlantillaPresupuesto, ModificarPlantillaPresupuesto | ControllerNegocio |
| 17 | AdministradorValidaciones | isPasswordCorrect |

## APIs disponibles (backend)

| Recurso | Base Path | Notas |
|---------|-----------|-------|
| Conceptos | `/api/v1/conceptos` | CRUD completo |
| Copias | `/api/v1/copia` | CRUD |
| Documento presentado | `/api/v1/documento-presentado` | CRUD |
| Escrituras | `/api/v1/escrituras` | CRUD |
| Estado gestión | `/api/v1/estado-gestion` | CRUD |
| Folio | `/api/v1/folio` | CRUD |
| Gestiones | `/api/v1/gestiones` | GET all/{id}/numero/{n}/cliente/{idPersona}, POST, PUT |
| Historial | `/api/v1/historial` | GET, /gestion/{idGestion} |
| Inmueble | `/api/v1/inmueble` | CRUD |
| Items | `/api/v1/items` | CRUD, /presupuesto/{idPresupuesto} |
| Movimiento testimonio | `/api/v1/movimiento-testimonio` | CRUD |
| Pagos | `/api/v1/pagos` | CRUD, /presupuesto/{idPresupuesto} |
| Personas | `/api/v1/personas` | CRUD, /buscar (nombre, apellido, numeroIdentificacion, idTipoIdentificacion) |
| Plantilla presupuestos | `/api/v1/plantilla-presupuestos` | CRUD, /tipo-tramite/{id} |
| Plantilla trámite | `/api/v1/plantilla-tramite` | GET, /tipo-tramite/{id} |
| Presupuestos | `/api/v1/presupuestos` | CRUD, /persona/{idPersona} |
| Registro auditoría | `/api/v1/registro-auditoria` | GET, /usuario/{idUsuario} |
| Reportes | `/api/v1/reportes` | 10 endpoints PDF (presupuesto, historial, documentos, libro-indice, DDJJ) |
| Suplencia | `/api/v1/suplencia` | CRUD |
| Testimonio | `/api/v1/testimonio` | CRUD |
| Tipo documento | `/api/v1/tipo-de-documento` | CRUD |
| Tipo folio | `/api/v1/tipo-folio` | CRUD |
| Tipo identificación | `/api/v1/tipo-identificacion` | CRUD |
| Tipo trámite | `/api/v1/tipo-tramite` | CRUD |
| Trámites | `/api/v1/tramites` | CRUD |
| Usuarios | `/api/v1/usuarios` | CRUD, /login, /persona/{idPersona} (Batch A) |

## APIs faltantes o por extender

1. `GET /api/v1/personas/buscar?esCliente=true` — filtrar personas con gestiones (clientes)
2. `GET /api/v1/usuarios?nombre=X` — buscar usuario por nombre
3. `POST /api/v1/usuarios/validate-password` — validar contraseña sin exponer hash (para AdministradorValidaciones)
4. `GET /api/v1/gestiones/{id}/estado-actual` o equivalente — para ListaGestionesCliente (alternativa: usar historial/gestion/{id} y último registro)

## Test Coverage Baseline

- `backend-api`: UseCaseDomainsIntegrationTest cubre rutas de dominio
- Shell API tests en `test/http`; alinear con paths y contratos
- No hay tests Testcontainers activos aún

## Referencias

- Plan de migración: `MIGRATION_PLAN.md`
- Guía de migración Swing: `frontend-swing/MIGRATION_GUIDE.md`
- Progreso administración: `frontend-swing/MIGRATION_PROGRESS.md`
