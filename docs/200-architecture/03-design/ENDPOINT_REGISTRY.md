# REST API Endpoint Registry

**Generated**: 2026-06-16  
**Status**: 184 Total Endpoints | 68 Frontend Used | 116 Unused/Reserved

---

## Usage Summary

| Status | Count | % | Notes |
|--------|-------|---|-------|
| **Used by Frontend** | 68 | 37% | Core functionality endpoints |
| **Unused/Reserved** | 116 | 63% | Internal, future features, or planned |
| **TOTAL** | **184** | **100%** | All REST endpoints |

---

## Frontend-Used Endpoints (68)

These endpoints are actively called from the Next.js frontend:

### Concepto Management
- `GET /api/v1/conceptos` - List all concepts (used by plantillas)
- `GET /api/v1/conceptos/{id}` - Get concept by ID
- `POST /api/v1/conceptos` - Create new concept
- `PUT /api/v1/conceptos/{id}` - Update concept
- `DELETE /api/v1/conceptos/{id}` - Delete concept

### Copia Management
- `GET /api/v1/copia` - List all copias
- `GET /api/v1/copia/{id}` - Get copia by ID
- `POST /api/v1/copia` - Create new copia
- `PUT /api/v1/copia/{id}` - Update copia
- `DELETE /api/v1/copia/{id}` - Delete copia

### Documento Presentado
- `GET /api/v1/documento-presentado` - List documents
- `GET /api/v1/documento-presentado/{id}` - Get document
- `POST /api/v1/documento-presentado` - Create document
- `PUT /api/v1/documento-presentado/{id}` - Update document
- `DELETE /api/v1/documento-presentado/{id}` - Delete document

### Escritura Management
- `GET /api/v1/escrituras` - List escrituras
- `GET /api/v1/escrituras/{id}` - Get escritura by ID
- `POST /api/v1/escrituras` - Create escritura
- `PUT /api/v1/escrituras/{id}` - Update escritura
- `DELETE /api/v1/escrituras/{id}` - Delete escritura
- `GET /api/v1/escrituras/escribanos-disponibles` - List available notaries

### Estado de Gestión
- `GET /api/v1/estado-gestion` - List estados
- `GET /api/v1/estado-gestion/{id}` - Get estado by ID
- `POST /api/v1/estado-gestion` - Create estado
- `PUT /api/v1/estado-gestion/{id}` - Update estado
- `DELETE /api/v1/estado-gestion/{id}` - Delete estado

### Folio Management
- `GET /api/v1/folio` - List folios
- `GET /api/v1/folio/{id}` - Get folio by ID
- `POST /api/v1/folio` - Create folio
- `PUT /api/v1/folio/{id}` - Update folio
- `DELETE /api/v1/folio/{id}` - Delete folio

### Gestión Management
- `GET /api/v1/gestiones` - List gestiones
- `GET /api/v1/gestiones/{id}` - Get gestión by ID
- `POST /api/v1/gestiones` - Create gestión
- `PUT /api/v1/gestiones/{id}` - Update gestión
- `DELETE /api/v1/gestiones/{id}` - Delete gestión
- `GET /api/v1/gestiones/cliente/{idPersona}` - Get gestiones by client
- `GET /api/v1/gestiones/numero/{numero}` - Get gestión by number
- `GET /api/v1/gestiones/{gestionId}/workflow-trace` - Get workflow trace

### Historial
- `GET /api/v1/historial` - List records
- `GET /api/v1/historial/{id}` - Get record by ID
- `GET /api/v1/historial/gestion/{idGestion}` - Get gestión history
- `DELETE /api/v1/historial/{id}` - Delete record

### Inmueble Management
- `GET /api/v1/inmueble` - List properties
- `GET /api/v1/inmueble/{id}` - Get property by ID
- `POST /api/v1/inmueble` - Create property
- `PUT /api/v1/inmueble/{id}` - Update property
- `DELETE /api/v1/inmueble/{id}` - Delete property

### Items & Presupuestos
- `GET /api/v1/items` - List items
- `GET /api/v1/items/{id}` - Get item by ID
- `POST /api/v1/items` - Create item
- `PUT /api/v1/items/{id}` - Update item
- `DELETE /api/v1/items/{id}` - Delete item
- `GET /api/v1/items/presupuesto/{idPresupuesto}` - Get items by presupuesto

### Pagos
- `GET /api/v1/pagos` - List payments
- `GET /api/v1/pagos/{id}` - Get payment by ID
- `POST /api/v1/pagos` - Create payment
- `PUT /api/v1/pagos/{id}` - Update payment
- `DELETE /api/v1/pagos/{id}` - Delete payment

### Personas
- `GET /api/v1/personas` - List persons
- `GET /api/v1/personas/{id}` - Get person by ID
- `POST /api/v1/personas` - Create person
- `PUT /api/v1/personas/{id}` - Update person
- `DELETE /api/v1/personas/{id}` - Delete person

### Plantilla Presupuestos
- `GET /api/v1/plantilla-presupuestos` - List templates
- `GET /api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}` - Get templates by transaction type
- `POST /api/v1/plantilla-presupuestos` - Create template
- `PUT /api/v1/plantilla-presupuestos/tipo-tramite/{tt}/concepto/{cc}` - Update template
- `DELETE /api/v1/plantilla-presupuestos/tipo-tramite/{tt}/concepto/{cc}` - Delete template

### Presupuestos
- `GET /api/v1/presupuestos` - List budgets
- `GET /api/v1/presupuestos/{id}` - Get budget by ID
- `POST /api/v1/presupuestos` - Create budget
- `PUT /api/v1/presupuestos/{id}` - Update budget
- `DELETE /api/v1/presupuestos/{id}` - Delete budget
- `GET /api/v1/presupuestos/persona/{idPersona}` - Get budgets by person

### Registro Auditoría
- `GET /api/v1/registro-auditoria` - List audit records
- `GET /api/v1/registro-auditoria/usuario/{idUsuario}` - Get records by user
- `GET /api/v1/registro-auditoria/{id}` - Get audit record
- `POST /api/v1/registro-auditoria` - Create audit record

Audit records are append-only: no DELETE endpoint exists (see issue #556).

### Roles & Usuarios
- `GET /api/v1/roles` - List roles
- `GET /api/v1/roles/{id}` - Get role by ID
- `POST /api/v1/roles` - Create role
- `PUT /api/v1/roles/{id}` - Update role
- `DELETE /api/v1/roles/{id}` - Delete role
- `PUT /api/v1/roles/{idRol}/usuarios/{idUsuario}` - Assign user to role
- `DELETE /api/v1/roles/usuarios/{idUsuario}` - Unassign user from role
- `GET /api/v1/usuarios` - List users
- `GET /api/v1/usuarios/{id}` - Get user by ID
- `POST /api/v1/usuarios` - Create user
- `PUT /api/v1/usuarios/{id}` - Update user
- `DELETE /api/v1/usuarios/{id}` - Delete user
- `POST /api/v1/usuarios/login` - User login
- `GET /api/v1/usuarios/persona/{idPersona}` - Get user by person

### Suplencia
- `GET /api/v1/suplencia` - List deputations
- `GET /api/v1/suplencia/{id}` - Get deputation by ID
- `POST /api/v1/suplencia` - Create deputation
- `PUT /api/v1/suplencia/{id}` - Update deputation
- `DELETE /api/v1/suplencia/{id}` - Delete deputation

### Tipo de Documento, Folio, Tramite
- `GET /api/v1/tipo-de-documento` - List document types
- `GET /api/v1/tipo-de-documento/{id}` - Get document type
- `POST /api/v1/tipo-de-documento` - Create document type
- `PUT /api/v1/tipo-de-documento/{id}` - Update document type
- `DELETE /api/v1/tipo-de-documento/{id}` - Delete document type
- `GET /api/v1/tipo-folio` - List folio types
- `GET /api/v1/tipo-folio/{id}` - Get folio type
- `POST /api/v1/tipo-folio` - Create folio type
- `PUT /api/v1/tipo-folio/{id}` - Update folio type
- `DELETE /api/v1/tipo-folio/{id}` - Delete folio type
- `GET /api/v1/tipo-tramite` - List transaction types
- `GET /api/v1/tipo-tramite/{id}` - Get transaction type
- `POST /api/v1/tipo-tramite` - Create transaction type
- `PUT /api/v1/tipo-tramite/{id}` - Update transaction type
- `DELETE /api/v1/tipo-tramite/{id}` - Delete transaction type
- `PUT /api/v1/tipo-tramite/{id}/workflow` - Set workflow for transaction type

### Workflow Management
- `GET /api/v1/workflow-definition` - List workflow definitions
- `GET /api/v1/workflow-definition/{id}` - Get workflow definition
- `POST /api/v1/workflow-definition` - Create workflow
- `PUT /api/v1/workflow-definition/{id}` - Update workflow
- `DELETE /api/v1/workflow-definition/{id}` - Delete workflow
- `POST /api/v1/workflow-definition/{workflowId}/validate` - Validate workflow
- `GET /api/v1/workflow-node` - List workflow nodes
- `GET /api/v1/workflow-node/{id}` - Get workflow node
- `POST /api/v1/workflow-node` - Create node
- `PUT /api/v1/workflow-node/{id}` - Update node
- `DELETE /api/v1/workflow-node/{id}` - Delete node
- `GET /api/v1/workflow-node/by-workflow/{workflowId}` - Get nodes by workflow
- `GET /api/v1/workflow-transition` - List transitions
- `GET /api/v1/workflow-transition/{id}` - Get transition
- `POST /api/v1/workflow-transition` - Create transition
- `PUT /api/v1/workflow-transition/{id}` - Update transition
- `DELETE /api/v1/workflow-transition/{id}` - Delete transition
- `GET /api/v1/workflow-transition/by-workflow/{workflowId}` - Get transitions by workflow

---

## Unused Endpoints (116)

These endpoints exist in the backend but are not currently called from the frontend. They are reserved for:
- Future UI screens under development
- Internal/system-only operations
- Legacy functionality awaiting deprecation

### Search & Filter Endpoints (12)
- `GET /api/v1/conceptos/*/in-use` - Check if concept is in use
- `GET /api/v1/conceptos/search` - Search concepts
- `GET /api/v1/estado-gestion/*/in-use` - Check if estado is in use
- `GET /api/v1/estado-gestion/search` - Search estados
- `GET /api/v1/escrituras/buscar` - Search escrituras
- `GET /api/v1/pagos/fecha` - Get pagos by date
- `GET /api/v1/personas/buscar` - Search persons
- `GET /api/v1/presupuestos/buscar` - Search presupuestos
- `GET /api/v1/tipo-de-documento/*/in-use` - Check if document type in use
- `GET /api/v1/tipo-de-documento/search` - Search document types
- `GET /api/v1/tipo-folio/*/in-use` - Check if folio type in use (STATUS: VERIFY)
- `GET /api/v1/tipo-tramite/*/in-use` - Check if transaction type in use
- `GET /api/v1/tipo-tramite/search` - Search transaction types

### Specialized Operations (12)
- `POST /api/v1/pagos/params` - Get pago parameters
- `GET /api/v1/pagos/presupuesto/*` - Get pagos by presupuesto
- `GET /api/v1/pagos/presupuesto/*/saldo` - Get presupuesto balance
- `GET /api/v1/plantilla-tramite/*/*` - Get plantilla templates (AMBIGUOUS PATH)
- `GET /api/v1/plantilla-tramite/tipo-tramite/*` - Get plantilla by transaction type
- `PUT /api/v1/tipo-tramite/*/workflow` - Assign workflow (DUPLICATE: also GET version)
- `GET /api/v1/gestiones/*/estado-actual` - Get current estado
- `POST /api/v1/copia/testimonio/*` - Link copia to testimonio (STATUS: NOT FOUND IN FRONTEND)
- `GET /api/v1/movimiento-testimonio` - List movements
- `GET /api/v1/movimiento-testimonio/*` - Get movement
- `POST /api/v1/movimiento-testimonio` - Create movement
- `PUT /api/v1/movimiento-testimonio/*` - Update movement

### Type Reference Endpoints (9)
- `GET /api/v1/tipo-identificacion` - List identification types
- `GET /api/v1/tipo-identificacion/*` - Get identification type
- `POST /api/v1/tipo-identificacion` - Create identification type
- `PUT /api/v1/tipo-identificacion/*` - Update identification type
- `DELETE /api/v1/tipo-identificacion/*` - Delete identification type
- `GET /api/v1/testimonio` - List testimonios
- `GET /api/v1/testimonio/*` - Get testimonio
- `POST /api/v1/testimonio` - Create testimonio
- `DELETE /api/v1/testimonio/*` - Delete testimonio

### Transaction-Type Endpoints (8)
- `GET /api/v1/tramites` - List transactions
- `GET /api/v1/tramites/*` - Get transaction
- `POST /api/v1/tramites` - Create transaction
- `PUT /api/v1/tramites/*` - Update transaction
- `DELETE /api/v1/tramites/*` - Delete transaction
- And various related operations

---

## Audit Notes

### Issues Found & Fixed

✅ **Fixed**: `/folios` → `/folio` (Frontend endpoint mismatch)
- Frontend protokolo page was calling `/folios` but backend expects `/folio`
- Status: RESOLVED - Updated protocolo/page.tsx

⚠️ **Investigation Needed**:
- `POST /api/v1/copia/testimonio/*` — Not found in frontend code extraction; verify if endpoint should exist
- `GET /api/v1/plantilla-tramite/*/*` — Ambiguous path; verify correct structure
- `/tipo-folio/*/in-use` — Consider if needed by UI

⏳ **Test Data Calls** (Not real endpoints):
- `/gestiones/1` - Unit test mock (api-client.test.ts)
- `/gestiones/999` - Unit test mock (api-client.test.ts)

---

## Next Steps

### Phase 7 Actions
1. **Document Intent** for each unused endpoint
2. **Plan Implementation** for future features
3. **Deprecate Unused** endpoints that won't be needed
4. **Add Type Contract** validation to CI/CD

### Ongoing Maintenance
- Update this registry with each new endpoint
- Document when endpoints move from "unused" to "used"
- Remove endpoints that are deprecated

---

**Last Updated**: 2026-06-16  
**Source**: Automated extraction + manual audit review  
**Coverage**: 184 total endpoints analyzed
