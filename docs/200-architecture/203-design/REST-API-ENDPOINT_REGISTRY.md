# REST API Endpoint Registry

**Generated**: 2026-06-16
**Last Regenerated**: 2026-08-28
**Status**: 191 Total Endpoints | 124 Frontend Used | 67 Unused/Reserved

---

## Usage Summary

| Status | Count | % | Notes |
|--------|-------|---|-------|
| **Used by Frontend** | 124 | 65% | Called via `apiGet`/`apiPost`/`apiPut`/`apiDelete`/`apiGetPaged` in `frontend/src/lib/api-client.ts` wrappers, or `downloadPdf` in `useReports.ts` |
| **Unused/Reserved** | 67 | 35% | Not called by any frontend code path (internal, future, or superseded by list-endpoint client-side filtering) |
| **TOTAL** | **191** | **100%** | All REST endpoints under `com.licensis.notaire.api` (31 controllers) |

> Classification method: static regex scan of `frontend/src/**/*.{ts,tsx}` (excluding `*.test.*`) for `api(Get|Post|Put|Delete|GetBytes|GetPaged)<...>(path)` and `downloadPdf(path)` call sites, matched against every `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping`/`@PatchMapping` in `backend-api/src/main/java/com/licensis/notaire/api/*.java`. Path variables and query strings are normalized before comparison. This will miss dynamically constructed paths and any client call added outside the `api-client.ts` wrapper/`downloadPdf` conventions.

---

## Frontend-Used Endpoints (121)

These endpoints are actively called by the Next.js frontend:

### ConceptoController
- `GET /api/v1/concepts`
- `GET /api/v1/concepts/search`
- `GET /api/v1/concepts/{id}/in-use`
- `POST /api/v1/concepts`
- `PUT /api/v1/concepts/{id}`
- `DELETE /api/v1/concepts/{id}`

### CopiaController
- `GET /api/v1/copies`
- `POST /api/v1/copies`
- `PUT /api/v1/copies/{id}`
- `DELETE /api/v1/copies/{id}`

### DocumentoPresentadoController
- `GET /api/v1/submitted-documents`
- `POST /api/v1/submitted-documents`
- `PUT /api/v1/submitted-documents/{id}`
- `DELETE /api/v1/submitted-documents/{id}`

### EscrituraController
- `GET /api/v1/deeds`
- `POST /api/v1/deeds`
- `PUT /api/v1/deeds/{id}`
- `DELETE /api/v1/deeds/{id}`
- `GET /api/v1/deeds/available-notaries`
- `GET /api/v1/deeds/search`

### EstadoDeGestionController
- `GET /api/v1/management-statuses`
- `GET /api/v1/management-statuses/search`
- `GET /api/v1/management-statuses/{id}/in-use`
- `POST /api/v1/management-statuses`
- `PUT /api/v1/management-statuses/{id}`
- `DELETE /api/v1/management-statuses/{id}`

### FolioController
- `GET /api/v1/folios`
- `GET /api/v1/folios/search`
- `POST /api/v1/folios` — `201 Created` + `Location: /api/v1/folios/{id}` (ADR-023 / #1065)
- `PUT /api/v1/folios/{id}`
- `DELETE /api/v1/folios/{id}`

### GestionController
- `POST /api/v1/managements/complete-case` — CU02; initial status must be a workflow start node when a definition exists (#804)
- `GET /api/v1/managements`
- `GET /api/v1/managements/number/{numero}`
- `GET /api/v1/managements/client/{idPersona}`
- `POST /api/v1/managements`
- `PUT /api/v1/managements/{id}` — CU53; **BREAKING (#804):** changing `managementStatusId` → 400; use `POST /{id}/transition`
- `PUT /api/v1/managements/{id}/complete-case` — **BREAKING (#804):** changing `statusManagementId` → 400; use `POST /{id}/transition`
- `POST /api/v1/managements/{id}/transition` — CU83 validated status mutation
- `DELETE /api/v1/managements/{id}`
- `GET /api/v1/managements/{id}/workflow-trace` — CU83 legal-next source: filter transitions from the current node
- `GET /api/v1/managements/{id}/external-entity-documents` — `useExternalEntityDocuments.ts`, CU10 `external-entity-documents` screen (#863)
- `PUT /api/v1/managements/{id}/external-entity-documents/{idSubmittedDocument}` — `useExternalEntityDocuments.ts`, CU10 `external-entity-documents` screen (#863)
- `GET /api/v1/managements/{id}/document-reentry` — `useDocumentReentry.ts`, CU43 `document-reentry` screen (#865)
- `POST /api/v1/managements/{id}/document-reentry` — `useDocumentReentry.ts`, CU43 `document-reentry` screen (#865)

### InmuebleController
- `GET /api/v1/properties`
- `POST /api/v1/properties`
- `PUT /api/v1/properties/{id}`
- `DELETE /api/v1/properties/{id}`

### ItemController
- `GET /api/v1/items`
- `POST /api/v1/items`
- `PUT /api/v1/items/{id}`
- `DELETE /api/v1/items/{id}`

### RegistrationDraftController (Minutas de Inscripción)
- `GET /api/v1/registration-drafts/{id}`
- `POST /api/v1/registration-drafts` — `201 Created` + `Location: /api/v1/registration-drafts/{id}` (ADR-023 / #1065)
- `PUT /api/v1/registration-drafts/{id}/submit`
- `PUT /api/v1/registration-drafts/{id}/observe`
- `PUT /api/v1/registration-drafts/{id}/register`

### PagoController / PaymentController
- `GET /api/v1/payments`
- `POST /api/v1/payments` — `201 Created` + `Location` (ADR-023 / #1065); `/payments/params` removed
- `PUT /api/v1/payments/{id}`
- `DELETE /api/v1/payments/{id}`

### PersonaController
- `GET /api/v1/people`
- `POST /api/v1/people`
- `PUT /api/v1/people/{id}`
- `DELETE /api/v1/people/{id}`
- `GET /api/v1/people/search`

### PlantillaPresupuestoController
- `GET /api/v1/budget-templates`
- `POST /api/v1/budget-templates`
- `PUT /api/v1/budget-templates/procedure-types/{idTipoTramite}/concept/{idConcepto}`
- `DELETE /api/v1/budget-templates/procedure-types/{idTipoTramite}/concept/{idConcepto}`

### PlantillaTramiteController
- `GET /api/v1/procedure-templates/procedure-types/{idTipoTramite}` — `useProcedureTemplate.ts`, CU03 `documentos-necesarios` screen (#860)

### PresupuestoController
- `GET /api/v1/budgets`
- `GET /api/v1/budgets/search`
- `POST /api/v1/budgets`
- `PUT /api/v1/budgets/{id}`
- `DELETE /api/v1/budgets/{id}`

### RegistroAuditoriaController
- `GET /api/v1/registro-auditoria`
- `GET /api/v1/registro-auditoria/user/{idUsuario}`

### ReporteController
- `GET /api/v1/reports/budget/{idPresupuesto}`
- `GET /api/v1/reports/budget-properties/{idPresupuesto}`
- `GET /api/v1/reports/procedure-document-list`
- `GET /api/v1/reports/management-history/{idGestion}`
- `GET /api/v1/reports/document-debt`
- `GET /api/v1/reports/index-book`
- `GET /api/v1/reports/monthly-sworn-statement`
- `GET /api/v1/reports/tax-sworn-statement`

### RolController
- `GET /api/v1/roles`
- `POST /api/v1/roles`
- `PUT /api/v1/roles/{id}`
- `DELETE /api/v1/roles/{id}`
- `PUT /api/v1/roles/{idRol}/users/{idUsuario}`
- `DELETE /api/v1/roles/users/{idUsuario}`

### SuplenciaController
- `GET /api/v1/substitutions`
- `POST /api/v1/substitutions`
- `PUT /api/v1/substitutions/{id}`
- `DELETE /api/v1/substitutions/{id}`

### TipoDeDocumentoController
- `GET /api/v1/document-types`
- `GET /api/v1/document-types/search`
- `GET /api/v1/document-types/{id}/in-use`
- `POST /api/v1/document-types`
- `PUT /api/v1/document-types/{id}`
- `DELETE /api/v1/document-types/{id}`

### TipoDeFolioController
- `GET /api/v1/folio-types`
- `GET /api/v1/folio-types/search`
- `GET /api/v1/folio-types/{id}/in-use`
- `POST /api/v1/folio-types`
- `PUT /api/v1/folio-types/{id}`
- `DELETE /api/v1/folio-types/{id}`

### TipoDeTramiteController
- `GET /api/v1/procedure-types`
- `GET /api/v1/procedure-types/search`
- `GET /api/v1/procedure-types/{id}/in-use`
- `POST /api/v1/procedure-types`
- `PUT /api/v1/procedure-types/{id}`
- `DELETE /api/v1/procedure-types/{id}`
- `PUT /api/v1/procedure-types/{id}/workflow`

### UsuarioController
- `GET /api/v1/users`
- `POST /api/v1/users`
- `PUT /api/v1/users/{id}`
- `DELETE /api/v1/users/{id}`
- `POST /api/v1/users/login`

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
- `GET /api/v1/concepts/{id}`

### CopiaController
- `GET /api/v1/copies/{id}`

### DocumentoPresentadoController
- `GET /api/v1/submitted-documents/{id}`

### EscrituraController
- `GET /api/v1/deeds/{id}`

### EstadoDeGestionController
- `GET /api/v1/management-statuses/{id}`

### FolioController
- `GET /api/v1/folios/{id}/in-use`
- `GET /api/v1/folios/{id}`

### GestionController
- `PUT /api/v1/managements/{id}/complete-case` — see primary GestionController list; status id changes rejected (#804)
- `GET /api/v1/managements/{id}`
- `GET /api/v1/managements/{id}/current-status`

### HistorialController
- `GET /api/v1/history`
- `GET /api/v1/history/{id}`
- `GET /api/v1/history/management/{idGestion}`
- `POST /api/v1/history`
- `PUT /api/v1/history/{id}`
- `DELETE /api/v1/history/{id}`

### InmuebleController
- `GET /api/v1/properties/{id}`

### ItemController
- `GET /api/v1/items/{id}`
- `GET /api/v1/items/budget/{idPresupuesto}`

### MovimientoTestimonioController
- `GET /api/v1/testimony-movements`
- `GET /api/v1/testimony-movements/{id}`
- `POST /api/v1/testimony-movements`
- `PUT /api/v1/testimony-movements/{id}`
- `DELETE /api/v1/testimony-movements/{id}`

### PagoController / PaymentController
- `GET /api/v1/payments/{id}`
- `GET /api/v1/payments/budget/{idPresupuesto}`
- `GET /api/v1/payments/budget/{idPresupuesto}/balance`
- `GET /api/v1/payments/date`
- `POST /api/v1/payments` — `201 Created` + `Location: /api/v1/payments/{id}` (ADR-023 / #1065)
- ~~`POST /api/v1/payments/params`~~ — **removed** (unused; #1065)

### PersonaController
- `GET /api/v1/people/{id}`

### PlantillaPresupuestoController
- `GET /api/v1/budget-templates/procedure-types/{idTipoTramite}`

### PlantillaTramiteController
- `GET /api/v1/procedure-templates`
- `GET /api/v1/procedure-templates/{idTipoTramite}/{idTipoDocumento}`
- `POST /api/v1/procedure-templates`
- `PUT /api/v1/procedure-templates/{idTipoTramite}/{idTipoDocumento}`
- `DELETE /api/v1/procedure-templates/{idTipoTramite}/{idTipoDocumento}`

### PresupuestoController
- `GET /api/v1/budgets/{id}`
- `GET /api/v1/budgets/person/{idPersona}`

### RegistroAuditoriaController
- `GET /api/v1/registro-auditoria/{id}`
- `POST /api/v1/registro-auditoria`

### ReporteController
- `GET /api/v1/reports/documents-due/{idDocumentoPresentado}`

### RolController
- `GET /api/v1/roles/{id}`

### SuplenciaController
- `GET /api/v1/substitutions/{id}`

### TestimonioController
- `GET /api/v1/testimonies`
- `GET /api/v1/testimonies/{id}`
- `POST /api/v1/testimonies`
- `PUT /api/v1/testimonies/{id}`
- `DELETE /api/v1/testimonies/{id}`

### TipoDeDocumentoController
- `GET /api/v1/document-types/{id}`

### TipoDeFolioController
- `GET /api/v1/folio-types/{id}`

### TipoDeTramiteController
- `GET /api/v1/procedure-types/{id}`

### TipoIdentificacionController
- `GET /api/v1/identification-types`
- `GET /api/v1/identification-types/{id}`
- `POST /api/v1/identification-types`
- `PUT /api/v1/identification-types/{id}`
- `DELETE /api/v1/identification-types/{id}`

### TramiteController
- `GET /api/v1/procedures`
- `GET /api/v1/procedures/{id}`
- `POST /api/v1/procedures`
- `PUT /api/v1/procedures/{id}`
- `DELETE /api/v1/procedures/{id}`

### UsuarioController
- `GET /api/v1/users/person/{idPersona}`
- `GET /api/v1/users/{id}`

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
- `ReporteController` endpoints are consumed via `downloadPdf()` in `frontend/src/hooks/useReports.ts`, not the `apiGet`/`apiPost` wrapper family, since they return `application/pdf` rather than JSON.
- `GET /api/v1/reports/documents-due/{idDocumentoPresentado}` is the one `ReporteController` endpoint with no frontend caller found.

### Ongoing Maintenance
- Update registry when a new endpoint is added.
- Document when endpoints move from "unused" to "used".
- Remove endpoints once deprecated.

---

**Last Updated**: 2026-08-18
**Source**: Regenerated by static analysis of `backend-api/src/main/java/com/licensis/notaire/api/*.java` cross-referenced with `frontend/src/**/*.{ts,tsx}`
**Coverage**: 189 total endpoints analyzed
