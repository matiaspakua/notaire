# Migration Traceability Matrix

This matrix links analyst use cases to current backend API endpoints, Swing forms, and test coverage status.

## Coverage Legend

- `Implemented`: endpoint/form flow exists.
- `Partial`: flow exists but missing filtered endpoints or business validations.
- `Missing`: no explicit backend flow yet.

## CU -> API -> Swing -> Tests

| Domain | Use Cases | Backend API | Swing Forms | Status | Tests |
|---|---|---|---|---|---|
| Presupuestos/Pagos | CU01, CU15, CU45, CU47, CU60 | `/api/v1/presupuestos`, `/api/v1/presupuestos/persona/{idPersona}`, `/api/v1/items`, `/api/v1/pagos` | `RegistrarPago`, `ConsultarPagos`, `BuscarPresupuesto`, `ModificarPresupuesto` | Partial | Shell partial, JUnit domain tests |
| Gestiones | CU02, CU13, CU16, CU19, CU53, CU67 | `/api/v1/tramites`, `/api/v1/gestiones/cliente/{idPersona}`, `/api/v1/historial`, `/api/v1/historial/gestion/{idGestion}`, `/api/v1/estado-gestion` | `IniciarGestion`, `BuscarGestion`, `DetalleGestion`, `VerHistorialGestion`, `ArchivarGestion`, `ModificarGestion` | Partial | Shell partial, JUnit domain tests |
| Clientes/Personas | CU17, CU18, CU41, CU46, CU61 | `/api/v1/personas`, `/api/v1/personas/buscar` (incl. `idTipoIdentificacion`), `/api/v1/tipo-identificacion` | `DarAltaPersona`, `AdministrarCliente`, `BuscarCliente`, `ListarPersonas`, `BuscarGestionesCliente` | Partial | Shell partial, JUnit domain tests |
| Documentacion | CU03, CU04, CU09, CU10, CU43, CU65 | `/api/v1/documento-presentado`, `/api/v1/tipo-de-documento`, reportes API | `IngresarDocumento`, `ReingresarDocumentos`, `RegistrarEntregaDocumentos`, `ConsultarDeudasDocumentos`, `ConsultarVencimientosDocumentos` | Partial | Shell partial, JUnit missing |
| Escrituras | CU05, CU06, CU52, CU56, CU62 | `/api/v1/escrituras`, `/api/v1/inmueble`, `/api/v1/copia` | `BuscarEscritura`, `ListaEscrituras`, `DetalleEscritura` | Partial | Shell partial, JUnit missing |
| Testimonios | CU07, CU08, CU12, CU44 | `/api/v1/testimonio`, `/api/v1/movimiento-testimonio` | `GenerarTestimonio`, `VerificarTestimonio`, `RetirarTestimonio` | Partial | Shell partial, JUnit missing |
| Administracion Catalogos | CU26, CU27, CU28, CU29, CU30, CU31, CU32, CU33, CU34, CU35, CU36, CU37, CU38, CU40, CU57, CU58, CU64, CU66, CU68 | `/api/v1/tipo-tramite`, `/api/v1/tipo-folio`, `/api/v1/tipo-de-documento`, `/api/v1/conceptos`, `/api/v1/estado-gestion` | Multiple `gui/administracion/**` forms | Implemented/Partial | Shell partial, JUnit missing |
| Usuarios/Auditoria | CU20, CU21, CU23 | `/api/v1/usuarios`, `/api/v1/usuarios/login`, `/api/v1/registro-auditoria/usuario/{idUsuario}` | `DarAltaUsuario`, `ModificarUsuario`, `VerRegistroActividadesUsuario` (REST-migrated), `ActividadUsuario` | Partial | Shell partial, JUnit domain tests |
| Reportes/DDJJ | CU24, CU25, CU42, CU50 | `/api/v1/reportes/*` (6 endpoints currently) | `AdministradorReportes` and dependent forms | Partial/Missing | Shell missing, JUnit missing |

## Identified Endpoint Gaps (updated)

1. ~~Persona search by document/type (CU61 expansion)~~ — `GET /api/v1/personas/buscar` supports optional `idTipoIdentificacion`.
2. ~~Gestiones aggregate query endpoints~~ — `GET /api/v1/gestiones/cliente/{idPersona}` added.
3. ~~Historial por gestión~~ — `GET /api/v1/historial/gestion/{idGestion}` added.
4. ~~Presupuestos por persona~~ — `GET /api/v1/presupuestos/persona/{idPersona}` added.
5. Functional API tests validating business flows end-to-end per CU (beyond route coverage).

## New APIs added for CU coverage

- `GET /api/v1/plantilla-presupuestos`
- `GET /api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}`
- `POST /api/v1/plantilla-presupuestos`
- `PUT /api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}`
- `DELETE /api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}`
- `GET /api/v1/registro-auditoria`
- `GET /api/v1/registro-auditoria/{id}`
- `GET /api/v1/registro-auditoria/usuario/{idUsuario}`
- `GET /api/v1/plantilla-tramite/tipo-tramite/{idTipoTramite}` (for ListarDocumentos)
- `POST /api/v1/registro-auditoria`
- `DELETE /api/v1/registro-auditoria/{id}`
- `GET /api/v1/reportes/libro-indice` (CU24)
- `GET /api/v1/reportes/declaracion-jurada-mensual` (CU25)
- `GET /api/v1/reportes/declaracion-jurada-rentas` (CU50)

## Swing Migration Backlog (current)

- Total Swing GUI classes: `97`
- Still coupled to legacy (`ControllerNegocio` / legacy JPA usage): reduced after batch 5.
- **Batch 5 (20/02/2026)**:
  - `ArchivarGestion`: `obtenerGestionesEnTramite`, `archivarGestion` → REST via `AdministradorJpa.getGestionJpa()`, `getEstadoDeGestionJpa()`
  - `IniciarGestion`: `obtenerListaEscribanosDisponibles`, `obtenerProximaGestionDeEscritura`, `obtenerListaEstadosDeGestionDisponibles` → REST via `AdministradorJpa` + `RestMapper`
  - `ModificarGestion`: `buscarTramite`, `buscarPresupuestoPorNumero` → REST via `AdministradorJpa` + `RestMapper`
  - **Documentación (Batch 5b)**:
    - `DocumentacionRestHelper`: helper con `buscarDtoGestion`, `obtenerDocNecesarioEntregadosNoEntregadosDeGestion`, `modificarDocumentacion`, `documentacionCompletaCliente`, `documentacionCompletaExterna`, `iscompletaDocumentacion`
    - `RegistrarEntregaDocumentos`, `IngresarDocumento`, `ReingresarDocumentos`, `ConsultarDeudasDocumentos` → usan `DocumentacionRestHelper` en lugar de `ControllerNegocio`
- Priority migration batches:
  1. `documentacion` (RegistrarEntregaDocumentos, IngresarDocumento, etc.)
  2. `presupuestos` rest + `documentacion`
  3. `inscripciones` + `testimonios` + `escrituras` + `administracion/usuarios` (VerRegistroActividadesUsuario done) + `protocolo/folios` + `gui/Principal`

## Test Coverage Baseline

- `backend-api/src/test`: **UseCaseDomainsIntegrationTest** covers domain routes (presupuestos/persona, items/presupuesto, gestiones/cliente, historial/gestion, personas/buscar, registro-auditoria/usuario, etc.) with MockMvc GET checks.
- Shell API tests exist in `test/http`; align with endpoint paths and payload contracts as needed.
- No Testcontainers-based integration tests yet.
