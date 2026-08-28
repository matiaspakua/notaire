# REST API Endpoint Registry

**Generated**: 2026-06-16
**Last Regenerated**: 2026-08-28
**Status**: 191 Total Endpoints | 124 Frontend Used | 67 Unused/Reserved

---

## Usage Summary

| Status | Count | % | Notes |
|--------|-------|---|-------|
| **Used by Frontend** | 124 | 65% | Called via `apiGet`/`apiPost`/`apiPut`/`apiDelete`/`apiGetPaged` in `frontend/src/lib/api-client.ts` wrappers, or `downloadPdf` in `useReportes.ts` |
| **Unused/Reserved** | 67 | 35% | Not called by any frontend code path (internal, future, or superseded by list-endpoint client-side filtering) |
| **TOTAL** | **191** | **100%** | All REST endpoints under `com.licensis.notaire.api` (31 controllers) |

> Classification method: static regex scan of `frontend/src/**/*.{ts,tsx}` (excluding `*.test.*`) for `api(Get|Post|Put|Delete|GetBytes|GetPaged)<...>(path)` and `downloadPdf(path)` call sites, matched against every `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping`/`@PatchMapping` in `backend-api/src/main/java/com/licensis/notaire/api/*.java`. Path variables and query strings are normalized before comparison. This will miss dynamically constructed paths and any client call added outside the `api-client.ts` wrapper/`downloadPdf` conventions.

---

## Frontend-Used Endpoints (121)

These endpoints are actively called by the Next.js frontend:

### ConceptoController
- `GET /api/v1/conceptos`
- `GET /api/v1/conceptos/search`
- `GET /api/v1/conceptos/{id}/in-use`
- `POST /api/v1/conceptos`
- `PUT /api/v1/conceptos/{id}`
- `DELETE /api/v1/conceptos/{id}`

### CopiaController
- `GET /api/v1/copia`
- `POST /api/v1/copia`
- `PUT /api/v1/copia/{id}`
- `DELETE /api/v1/copia/{id}`

### DocumentoPresentadoController
- `GET /api/v1/documento-presentado`
- `POST /api/v1/documento-presentado`
- `PUT /api/v1/documento-presentado/{id}`
- `DELETE /api/v1/documento-presentado/{id}`

### EscrituraController
- `GET /api/v1/escrituras`
- `POST /api/v1/escrituras`
- `PUT /api/v1/escrituras/{id}`
- `DELETE /api/v1/escrituras/{id}`
- `GET /api/v1/escrituras/escribanos-disponibles`
- `GET /api/v1/escrituras/buscar`

### EstadoDeGestionController
- `GET /api/v1/estado-gestion`
- `GET /api/v1/estado-gestion/search`
- `GET /api/v1/estado-gestion/{id}/in-use`
- `POST /api/v1/estado-gestion`
- `PUT /api/v1/estado-gestion/{id}`
- `DELETE /api/v1/estado-gestion/{id}`

### FolioController
- `GET /api/v1/folio`
- `GET /api/v1/folio/search`
- `POST /api/v1/folio`
- `PUT /api/v1/folio/{id}`
- `DELETE /api/v1/folio/{id}`

### GestionController
- `POST /api/v1/gestiones/complete-case`
- `GET /api/v1/gestiones`
- `GET /api/v1/gestiones/numero/{numero}`
- `GET /api/v1/gestiones/cliente/{idPersona}`
- `POST /api/v1/gestiones`
- `PUT /api/v1/gestiones/{id}`
- `DELETE /api/v1/gestiones/{id}`
- `GET /api/v1/gestiones/{id}/workflow-trace`
- `GET /api/v1/gestiones/{id}/documentos-entidades-externas` — `useDocumentosEntidadExterna.ts`, CU10 `documentos-entidades-externas` screen (#863)
- `PUT /api/v1/gestiones/{id}/documentos-entidades-externas/{idDocumentoPresentado}` — `useDocumentosEntidadExterna.ts`, CU10 `documentos-entidades-externas` screen (#863)
- `GET /api/v1/gestiones/{id}/reingreso-documentacion` — `useReingresoDocumentacion.ts`, CU43 `reingreso-documentacion` screen (#865)
- `POST /api/v1/gestiones/{id}/reingreso-documentacion` — `useReingresoDocumentacion.ts`, CU43 `reingreso-documentacion` screen (#865)

### InmuebleController
- `GET /api/v1/inmueble`
- `POST /api/v1/inmueble`
- `PUT /api/v1/inmueble/{id}`
- `DELETE /api/v1/inmueble/{id}`

### ItemController
- `GET /api/v1/items`
- `POST /api/v1/items`
- `PUT /api/v1/items/{id}`
- `DELETE /api/v1/items/{id}`

### PagoController
- `GET /api/v1/pagos`
- `POST /api/v1/pagos`
- `PUT /api/v1/pagos/{id}`
- `DELETE /api/v1/pagos/{id}`

### PersonaController
- `GET /api/v1/personas`
- `POST /api/v1/personas`
- `PUT /api/v1/personas/{id}`
- `DELETE /api/v1/personas/{id}`
- `GET /api/v1/personas/buscar`

### PlantillaPresupuestoController
- `GET /api/v1/plantilla-presupuestos`
- `POST /api/v1/plantilla-presupuestos`
- `PUT /api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}`
- `DELETE /api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}`

### PlantillaTramiteController
- `GET /api/v1/plantilla-tramite/tipo-tramite/{idTipoTramite}` — `usePlantillaTramite.ts`, CU03 `documentos-necesarios` screen (#860)

### PresupuestoController
- `GET /api/v1/presupuestos`
- `GET /api/v1/presupuestos/buscar`
- `POST /api/v1/presupuestos`
- `PUT /api/v1/presupuestos/{id}`
- `DELETE /api/v1/presupuestos/{id}`

### RegistroAuditoriaController
- `GET /api/v1/registro-auditoria`
- `GET /api/v1/registro-auditoria/usuario/{idUsuario}`

### ReporteController
- `GET /api/v1/reportes/presupuesto/{idPresupuesto}`
- `GET /api/v1/reportes/presupuesto-inmuebles/{idPresupuesto}`
- `GET /api/v1/reportes/lista-documentos-tramite`
- `GET /api/v1/reportes/historial-gestion/{idGestion}`
- `GET /api/v1/reportes/consultar-deuda-documentos`
- `GET /api/v1/reportes/libro-indice`
- `GET /api/v1/reportes/declaracion-jurada-mensual`
- `GET /api/v1/reportes/declaracion-jurada-rentas`

### RolController
- `GET /api/v1/roles`
- `POST /api/v1/roles`
- `PUT /api/v1/roles/{id}`
- `DELETE /api/v1/roles/{id}`
- `PUT /api/v1/roles/{idRol}/usuarios/{idUsuario}`
- `DELETE /api/v1/roles/usuarios/{idUsuario}`

### SuplenciaController
- `GET /api/v1/suplencia`
- `POST /api/v1/suplencia`
- `PUT /api/v1/suplencia/{id}`
- `DELETE /api/v1/suplencia/{id}`

### TipoDeDocumentoController
- `GET /api/v1/tipo-de-documento`
- `GET /api/v1/tipo-de-documento/search`
- `GET /api/v1/tipo-de-documento/{id}/in-use`
- `POST /api/v1/tipo-de-documento`
- `PUT /api/v1/tipo-de-documento/{id}`
- `DELETE /api/v1/tipo-de-documento/{id}`

### TipoDeFolioController
- `GET /api/v1/tipo-folio`
- `GET /api/v1/tipo-folio/search`
- `GET /api/v1/tipo-folio/{id}/in-use`
- `POST /api/v1/tipo-folio`
- `PUT /api/v1/tipo-folio/{id}`
- `DELETE /api/v1/tipo-folio/{id}`

### TipoDeTramiteController
- `GET /api/v1/tipo-tramite`
- `GET /api/v1/tipo-tramite/search`
- `GET /api/v1/tipo-tramite/{id}/in-use`
- `POST /api/v1/tipo-tramite`
- `PUT /api/v1/tipo-tramite/{id}`
- `DELETE /api/v1/tipo-tramite/{id}`
- `PUT /api/v1/tipo-tramite/{id}/workflow`

### UsuarioController
- `GET /api/v1/usuarios`
- `POST /api/v1/usuarios`
- `PUT /api/v1/usuarios/{id}`
- `DELETE /api/v1/usuarios/{id}`
- `POST /api/v1/usuarios/login`

### WorkflowDefinitionController
- `GET /api/v1/workflow-definition`
- `POST /api/v1/workflow-definition`
- `PUT /api/v1/workflow-definition/{id}`
- `DELETE /api/v1/workflow-definition/{id}`

### WorkflowNodeController
- `GET /api/v1/workflow-node/by-workflow/{workflowId}`
- `POST /api/v1/workflow-node`
- `PUT /api/v1/workflow-node/{id}`
- `DELETE /api/v1/workflow-node/{id}`

### WorkflowTransitionController
- `GET /api/v1/workflow-transition/by-workflow/{workflowId}`
- `POST /api/v1/workflow-transition`
- `DELETE /api/v1/workflow-transition/{id}`

### WorkflowValidationController
- `POST /api/v1/workflow-definition/{id}/validate`

---

## Unused/Reserved Endpoints (68)

Not reached by any current frontend call site. May be used by reports, background jobs, other internal callers, tests, or reserved for future UI work.

### ConceptoController
- `GET /api/v1/conceptos/{id}`

### CopiaController
- `GET /api/v1/copia/{id}`

### DocumentoPresentadoController
- `GET /api/v1/documento-presentado/{id}`

### EscrituraController
- `GET /api/v1/escrituras/{id}`

### EstadoDeGestionController
- `GET /api/v1/estado-gestion/{id}`

### FolioController
- `GET /api/v1/folio/{id}/in-use`
- `GET /api/v1/folio/{id}`

### GestionController
- `PUT /api/v1/gestiones/{id}/complete-case`
- `GET /api/v1/gestiones/{id}`
- `GET /api/v1/gestiones/{id}/estado-actual`

### HistorialController
- `GET /api/v1/historial`
- `GET /api/v1/historial/{id}`
- `GET /api/v1/historial/gestion/{idGestion}`
- `POST /api/v1/historial`
- `PUT /api/v1/historial/{id}`
- `DELETE /api/v1/historial/{id}`

### InmuebleController
- `GET /api/v1/inmueble/{id}`

### ItemController
- `GET /api/v1/items/{id}`
- `GET /api/v1/items/presupuesto/{idPresupuesto}`

### MovimientoTestimonioController
- `GET /api/v1/movimiento-testimonio`
- `GET /api/v1/movimiento-testimonio/{id}`
- `POST /api/v1/movimiento-testimonio`
- `PUT /api/v1/movimiento-testimonio/{id}`
- `DELETE /api/v1/movimiento-testimonio/{id}`

### PagoController
- `GET /api/v1/pagos/{id}`
- `GET /api/v1/pagos/presupuesto/{idPresupuesto}`
- `GET /api/v1/pagos/presupuesto/{idPresupuesto}/saldo`
- `GET /api/v1/pagos/fecha`
- `POST /api/v1/pagos/params`

### PersonaController
- `GET /api/v1/personas/{id}`

### PlantillaPresupuestoController
- `GET /api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}`

### PlantillaTramiteController
- `GET /api/v1/plantilla-tramite`
- `GET /api/v1/plantilla-tramite/{idTipoTramite}/{idTipoDocumento}`
- `POST /api/v1/plantilla-tramite`
- `PUT /api/v1/plantilla-tramite/{idTipoTramite}/{idTipoDocumento}`
- `DELETE /api/v1/plantilla-tramite/{idTipoTramite}/{idTipoDocumento}`

### PresupuestoController
- `GET /api/v1/presupuestos/{id}`
- `GET /api/v1/presupuestos/persona/{idPersona}`

### RegistroAuditoriaController
- `GET /api/v1/registro-auditoria/{id}`
- `POST /api/v1/registro-auditoria`

### ReporteController
- `GET /api/v1/reportes/documentos-por-vencer/{idDocumentoPresentado}`

### RolController
- `GET /api/v1/roles/{id}`

### SuplenciaController
- `GET /api/v1/suplencia/{id}`

### TestimonioController
- `GET /api/v1/testimonio`
- `GET /api/v1/testimonio/{id}`
- `POST /api/v1/testimonio`
- `PUT /api/v1/testimonio/{id}`
- `DELETE /api/v1/testimonio/{id}`

### TipoDeDocumentoController
- `GET /api/v1/tipo-de-documento/{id}`

### TipoDeFolioController
- `GET /api/v1/tipo-folio/{id}`

### TipoDeTramiteController
- `GET /api/v1/tipo-tramite/{id}`

### TipoIdentificacionController
- `GET /api/v1/tipo-identificacion`
- `GET /api/v1/tipo-identificacion/{id}`
- `POST /api/v1/tipo-identificacion`
- `PUT /api/v1/tipo-identificacion/{id}`
- `DELETE /api/v1/tipo-identificacion/{id}`

### TramiteController
- `GET /api/v1/tramites`
- `GET /api/v1/tramites/{id}`
- `POST /api/v1/tramites`
- `PUT /api/v1/tramites/{id}`
- `DELETE /api/v1/tramites/{id}`

### UsuarioController
- `GET /api/v1/usuarios/persona/{idPersona}`
- `GET /api/v1/usuarios/{id}`

### WorkflowDefinitionController
- `GET /api/v1/workflow-definition/{id}`

### WorkflowNodeController
- `GET /api/v1/workflow-node/{id}`

### WorkflowTransitionController
- `GET /api/v1/workflow-transition/{id}`
- `PUT /api/v1/workflow-transition/{id}`

---

## Notes

- `RegistroAuditoriaController`: audit records are append-only, no DELETE endpoint exists by design (see issue #556).
- `ReporteController` endpoints are consumed via `downloadPdf()` in `frontend/src/hooks/useReportes.ts`, not the `apiGet`/`apiPost` wrapper family, since they return `application/pdf` rather than JSON.
- `GET /api/v1/reportes/documentos-por-vencer/{idDocumentoPresentado}` is the one `ReporteController` endpoint with no frontend caller found.

### Ongoing Maintenance
- Update registry when a new endpoint is added.
- Document when endpoints move from "unused" to "used".
- Remove endpoints once deprecated.

---

**Last Updated**: 2026-08-18
**Source**: Regenerated by static analysis of `backend-api/src/main/java/com/licensis/notaire/api/*.java` cross-referenced with `frontend/src/**/*.{ts,tsx}`
**Coverage**: 189 total endpoints analyzed
