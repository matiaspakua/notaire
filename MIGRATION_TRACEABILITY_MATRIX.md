# Migration Traceability Matrix

This matrix links analyst use cases to backend API endpoints, Swing forms, and test coverage. Last updated: 25/02/2026

## Coverage Legend

- `✅ Implemented`: endpoint/form flow exists and uses REST.
- `🔄 Partial`: flow exists but some forms still use ControllerNegocio or missing filtered endpoints.
- `❌ Missing`: no explicit backend flow or form not yet migrated.
- `~~~~`: Already completed/migrated

## CU → API → Swing → Tests

| Domain | Use Cases | Backend API | Swing Forms | Status | Pending |
|--------|-----------|-------------|-------------|--------|---------|
| **Presupuestos/Pagos** | CU01, CU15, CU45, CU47, CU60 | `/api/v1/presupuestos`, `/persona/{id}`, `/api/v1/items`, `/presupuesto/{id}`, `/api/v1/pagos` | ~~RegistrarPago~~ ✅, ~~ConsultarPagos~~ ✅, ~~BuscarPresupuesto~~ ✅, ~~ModificarPresupuesto~~ ✅, CrearPresupuesto ❌, ~~ListaPresupuestosCliente~~ ✅, ~~BuscarInmueble~~ ❌, ~~DetalleValoresTramites~~ ❌ | 🔄 | CrearPresupuesto, BuscarInmueble, DetalleValoresTramites |
| **Gestiones** | CU02, CU13, CU16, CU19, CU53, CU67 | `/api/v1/gestiones`, `/cliente/{idPersona}`, `/api/v1/historial`, `/gestion/{idGestion}`, `/api/v1/estado-gestion` | ~~IniciarGestion~~ 🔄, ~~ArchivarGestion~~ ✅, ModificarGestion ❌, BuscarGestion ❌, DetalleGestion ❌, ~~VerHistorialGestion~~ ✅, ListaGestionesCliente ❌ | 🔄 | ModificarGestion, BuscarGestion, DetalleGestion, ListaGestionesCliente (obtenerEstadoActualDeGestion) |
| **Clientes/Personas** | CU17, CU18, CU41, CU46, CU61 | `/api/v1/personas`, `/buscar` (nombre, apellido, numeroIdentificacion, idTipoIdentificacion), `/api/v1/tipo-identificacion` | ~~DarAltaPersona~~ ✅, ~~AdministrarCliente~~ ✅, ~~BuscarCliente~~ ✅, ~~ListarPersonas~~ ✅, ~~BuscarGestionesCliente~~ ✅, ~~ModificarCliente~~ ✅ | ✅ | None |
| **Documentación** | CU03, CU04, CU09, CU10, CU43, CU65 | `/api/v1/documento-presentado`, `/api/v1/tipo-de-documento`, reportes | ~~IngresarDocumento~~ ✅, ~~ReingresarDocumentos~~ ✅, ~~RegistrarEntregaDocumentos~~ ✅, ~~ConsultarDeudasDocumentos~~ ✅, ~~ConsultarVencimientosDocumentos~~ ✅, ~~ListarDocumentos~~ ✅ | ✅ | None |
| **Escrituras** | CU05, CU06, CU52, CU56, CU62 | `/api/v1/escrituras`, `/api/v1/inmueble`, `/api/v1/copia` | BuscarEscritura ❌, ListaEscrituras ❌, DetalleEscritura ❌, ~~PrepararEscritura~~ 🔄 | ❌ | BuscarEscritura, ListaEscrituras, DetalleEscritura |
| **Testimonios** | CU07, CU08, CU12, CU44 | `/api/v1/testimonio`, `/api/v1/movimiento-testimonio` | GenerarTestimonio ❌, VerificarTestimonio ❌, RetirarTestimonio ❌ | ❌ | Los 3 formularios usan ControllerNegocio |
| **Inscripciones** | CU53, CU67 (rel.) | `/api/v1/gestiones`, `/api/v1/tramites`, `/api/v1/inmueble` | IngresarParaInscripcion ❌, RegistrarInscripcion ❌, RegistrarReingreso ❌ | ❌ | Los 3 formularios usan ControllerNegocio |
| **Administración Catálogos** | CU26–40, CU57, CU58, CU64, CU66, CU68 | tipo-tramite, tipo-folio, tipo-de-documento, conceptos, estado-gestion, plantilla-presupuestos | ~~IngresarConcepto~~ ✅, ~~ModificarConcepto~~ ✅, ~~EliminarConcepto~~ ✅, ~~IngresarDocumento~~ ✅, ~~ModificarDocumento~~ ✅, ~~EliminarDocumento~~ ✅, ~~DarAltaEscribano~~ ✅, ~~ConsultarSuplencias~~ ✅, ~~RegistrarSuplencia~~ ✅, ~~IngresarEstadoGestion~~ ✅, ~~ModificarEstadoGestion~~ ✅, ~~IngresarTipoDeFolio~~ ✅, ~~ModificarEliminarFolio~~ ✅, ~~IngresarTipoTramite~~ ✅, ~~ModificarTipoTramite~~ ✅, ~~EliminarTipoTramite~~ ✅ | ✅ | None |
| **Usuarios/Auditoría** | CU20, CU21, CU23 | `/api/v1/usuarios`, `/login`, `/persona/{idPersona}`, `/api/v1/registro-auditoria/usuario/{idUsuario}` | ~~DarAltaUsuario~~ ✅, ModificarUsuario 🔄, ~~VerRegistroActividadesUsuario~~ ✅, ~~ActividadUsuario~~ ✅, ~~ListarPersonasUsuario~~ ✅ | 🔄 | ModificarUsuario; API: `/usuarios/validate-password` (para cambio contraseña) |
| **Reportes/DDJJ** | CU24, CU25, CU42, CU50 | `/api/v1/reportes/*` (10 endpoints PDF) | ~~AdministradorReportes~~ 🔄, GenerarIndices 🔄, GenerarDDJJ 🔄, DeclaracionJurada 🔄 | 🔄 | Conectar AdministradorReportes a API reportes |
| **Protocolo** | CU24, CU25, CU42 | `/api/v1/folio`, `/api/v1/tipo-folio`, reportes | ~~Folios~~ ✅, IngresarFolios ❌, ModificarFolio ❌, GenerarIndices 🔄, GenerarDDJJ 🔄, DeclaracionJurada 🔄, DeclaracionJuradaRentas 🔄 | 🔄 | IngresarFolios, ModificarFolio (ControllerNegocio) |
| **Plantillas Presupuesto** | CU57, CU58 | `/api/v1/plantilla-presupuestos`, `/api/v1/plantilla-tramite` | ~~PlantillasPresupuesto~~ ✅, CrearPlantillaPresupuesto ❌, ModificarPlantillaPresupuesto ❌ | ❌ | CrearPlantillaPresupuesto, ModificarPlantillaPresupuesto |

---

## Formularios con ControllerNegocio (prioridad de migración)

| Priority | Module | Form | ControllerNegocio methods |
|----------|--------|------|--------------------------|
| ~~1~~ | ~~Clientes~~ | ~~Clientes~~ | ✅ Migrado Batch B |
| ~~2~~ | ~~BuscarCliente~~ | ✅ Migrado Batch B |
| ~~3~~ | ~~ActividadUsuario~~ | ✅ Migrado Batch A |
| ~~4~~ | ~~DarAltaUsuario~~ | ✅ Migrado Batch A |
| ~~5~~ | ~~ListarPersonasUsuario~~ | ✅ Migrado Batch A |
| 1 | Gestiones | BuscarGestion | listarTiposIdentificacion, buscarPersonaNombreApellidoConGestion, asociarFkTipoIdentificacion, buscarPersonaTipoNumeroIdentificacionConGestion |
| 2 | Gestiones | ListaGestionesCliente | obtenerEstadoActualDeGestion |
| 3 | Gestiones | ModificarGestion | ControllerNegocio |
| 4 | Gestiones | DetalleGestion | ControllerNegocio |
| 5 | Gestiones | IniciarGestion | ControllerNegocio |
| 6 | Escrituras | BuscarEscritura | ControllerNegocio |
| 7 | Escrituras | ListaEscrituras | ControllerNegocio |
| 8 | Escrituras | DetalleEscritura | ControllerNegocio |
| 9 | Testimonios | GenerarTestimonio | ControllerNegocio |
| 10 | Testimonios | VerificarTestimonio | ControllerNegocio |
| 11 | Testimonios | RetirarTestimonio | ControllerNegocio |
| 12 | Inscripciones | IngresarParaInscripcion | ControllerNegocio |
| 13 | Inscripciones | RegistrarInscripcion | ControllerNegocio |
| 14 | Inscripciones | RegistrarReingreso | ControllerNegocio |
| 15 | Presupuestos | CrearPresupuesto | ControllerNegocio |
| 16 | Presupuestos | DetalleValoresTramites | ControllerNegocio |
| 17 | Presupuestos | BuscarInmueble | ControllerNegocio |
| 18 | Protocolo | IngresarFolios | ControllerNegocio |
| 19 | Protocolo | ModificarFolio | ControllerNegocio |
| 20 | Plantillas | CrearPlantillaPresupuesto | ControllerNegocio |
| 21 | Plantillas | ModificarPlantillaPresupuesto | ControllerNegocio |
| 22 | Servicios | AdministradorValidaciones | isPasswordCorrect |

---

## APIs disponibles (backend)

| Resource | Base Path | Status | Notes |
|---------|-----------|--------|-------|
| Conceptos | `/api/v1/conceptos` | ✅ | CRUD completo |
| Copias | `/api/v1/copia` | ✅ | CRUD |
| Documento presentado | `/api/v1/documento-presentado` | ✅ | CRUD |
| Escrituras | `/api/v1/escrituras` | ✅ | CRUD |
| Estado gestión | `/api/v1/estado-gestion` | ✅ | CRUD |
| Folio | `/api/v1/folio` | ✅ | CRUD |
| Gestiones | `/api/v1/gestiones` | ✅ | CRUD + búsquedas |
| Historial | `/api/v1/historial` | ✅ | GET, /gestion/{idGestion} |
| Inmueble | `/api/v1/inmueble` | ✅ | CRUD |
| Items | `/api/v1/items` | ✅ | CRUD, /presupuesto/{idPresupuesto} |
| Movimiento testimonio | `/api/v1/movimiento-testimonio` | ✅ | CRUD |
| Pagos | `/api/v1/pagos` | ✅ | CRUD, /presupuesto/{idPresupuesto} |
| Personas | `/api/v1/personas` | ✅ | CRUD + /buscar |
| Plantilla presupuestos | `/api/v1/plantilla-presupuestos` | ✅ | CRUD, /tipo-tramite/{id} |
| Plantilla trámite | `/api/v1/plantilla-tramite` | ✅ | GET, /tipo-tramite/{id} |
| Presupuestos | `/api/v1/presupuestos` | ✅ | CRUD, /persona/{idPersona} |
| Registro auditoría | `/api/v1/registro-auditoria` | ✅ | GET, /usuario/{idUsuario} |
| Reportes | `/api/v1/reportes` | ✅ | 10 endpoints PDF |
| Suplencia | `/api/v1/suplencia` | ✅ | CRUD |
| Testimonio | `/api/v1/testimonio` | ✅ | CRUD |
| Tipo documento | `/api/v1/tipo-de-documento` | ✅ | CRUD |
| Tipo folio | `/api/v1/tipo-folio` | ✅ | CRUD |
| Tipo identificación | `/api/v1/tipo-identificacion` | ✅ | CRUD |
| Tipo trámite | `/api/v1/tipo-tramite` | ✅ | CRUD |
| Trámites | `/api/v1/tramites` | ✅ | CRUD |
| Usuarios | `/api/v1/usuarios` | ✅ | CRUD + /login |

---

## APIs faltantes o por extender

| API | Purpose | Priority |
|-----|---------|----------|
| `GET /api/v1/personas/buscar?esCliente=true` | Filtrar personas con gestiones (clientes) | ✅ Implemented |
| `GET /api/v1/usuarios?nombre=X` | Buscar usuario por nombre | 🔄 Pending |
| `POST /api/v1/usuarios/validate-password` | Validar contraseña sin exponer hash | 🔄 Pending |
| `GET /api/v1/gestiones/{id}/estado-actual` | Obtener estado actual de gestión | 🔄 Pending |

---

## Migration Progress Summary

| Category | Total | Completed | Pending | Progress |
|----------|-------|-----------|---------|----------|
| Formularios Swing | ~50 | ~25 | ~25 | 50% |
| APIs REST | 25+ | 25 | 0 | 100% |
| Administración Catálogos | 16 | 16 | 0 | 100% |
| Pagos | 2 | 2 | 0 | 100% |
| Usuarios | 5 | 5 | 0 | 100% |
| Clientes | 7 | 7 | 0 | 100% |
| Gestiones | 7 | 2 | 5 | 28% |
| Escrituras | 4 | 0 | 4 | 0% |
| Testimonios | 3 | 0 | 3 | 0% |
| Inscripciones | 3 | 0 | 3 | 0% |
| Presupuestos | 8 | 5 | 3 | 62% |
| Protocolo | 7 | 1 | 6 | 14% |
| Plantillas | 3 | 1 | 2 | 33% |

---

## Test Coverage Baseline

- `backend-api`: UseCaseDomainsIntegrationTest cubre rutas de dominio
- Shell API tests en `test/http`
- Tests unitarios para servicios migrados
- **Pendiente**: Tests con Testcontainers

---

## Referencias

- Plan de migración: `MIGRATION_PLAN.md`
- Guía de migración Swing: `frontend-swing/MIGRATION_GUIDE.md`
- Progreso administración: `frontend-swing/MIGRATION_PROGRESS.md`

---

*Última actualización: 25 de Febrero de 2026*
