# Plan: Backend Refactoring to Spring Data JPA

## Current Status (as of 2026-03-03)

### Completed Work
1. ✅ Created 27 Spring Data JPA repositories in `backend-api/src/main/java/com/licensis/notaire/repository/`
2. ✅ Created PersonaService with CRUD operations
3. ✅ Refactored PersonaController to use Spring Data + Service layer
4. ✅ Created EscrituraService
5. ✅ Refactored EscrituraController to use Spring Data + Service layer
6. ✅ Fixed Presupuesto entity to match database schema (added numero, encabezado, estado, montoInmueble fields)
7. ✅ Fixed DtoPresupuesto to match entity fields
8. ✅ Added deprecated stub methods (getSaldo, setSaldo, getTotal, setTotal) to Presupuesto and DtoPresupuesto for backwards compatibility with ControllerNegocio

### Issues Found
- Integration tests failing due to repository field name mismatches (e.g., `findByFkIdUsuario` but Escritura has no `fkIdUsuario` field)
- Legacy ControllerNegocio uses non-existent methods (saldo/total)
- Some repositories have methods that don't match entity fields

## Remaining Work Plan

### Phase 1: Fix Repository Field Mismatches (CRITICAL)
**Goal:** Fix all Spring Data repository methods to match actual entity fields

**Actions Required:**
1. Review each repository and remove/query methods that reference non-existent fields
2. Fix EscrituraRepository - remove `findByFkIdUsuario`, `findByFkIdTipoFolio...`
3. Fix other repositories with similar issues

**Files to review:**
- `EscrituraRepository.java`
- `PersonaRepository.java`
- `PresupuestoRepository.java`
- `GestionDeEscrituraRepository.java`
- `TramiteRepository.java`
- All other repositories

### Phase 2: Complete Controller Refactoring
**Goal:** Refactor remaining controllers to use Spring Data + Service layer

| Priority | Controller | Status |
|----------|------------|--------|
| High | PresupuestoController | Not started |
| High | GestionController | Not started |
| High | TramiteController | Not started |
| High | PagoController | Not started |
| Medium | UsuarioController | Not started |
| Medium | TipoIdentificacionController | Not started |
| Medium | InmuebleController | Not started |
| Medium | TipoDeTramiteController | Not started |
| Medium | TipoDeFolioController | Not started |
| Medium | TipoDeDocumentoController | Not started |
| Medium | EstadoDeGestionController | Not started |
| Medium | FolioController | Not started |
| Medium | CopiaController | Not started |
| Medium | SuplenciaController | Not started |
| Medium | TestimonioController | Not started |
| Medium | MovimientoTestimonioController | Not started |
| Medium | DocumentoPresentadoController | Not started |
| Medium | ItemController | Not started |
| Medium | HistorialController | Not started |
| Medium | ConceptoController | Not started |
| Medium | IdentificacionController | Not started |
| Medium | PlantillaTramiteController | Not started |
| Medium | PlantillaPresupuestoController | Not started |
| Medium | TramitesPersonasController | Not started |
| Low | RegistroAuditoriaController | Already has service |
| Low | ReporteController | Already has service |

**For each controller:**
1. Create Service interface (if needed) and implementation
2. Add repository injection via constructor
3. Add @Transactional annotations
4. Update controller to use service
5. Remove legacy JpaController usage

### Phase 3: Delete Legacy Code
**Goal:** Remove legacy JpaControllers after migration

**Actions:**
1. Delete all `JpaController` classes in `backend-api/src/main/java/com/licensis/notaire/jpa/`
2. Delete legacy `ControllerNegocio.java` (or refactor to remove business logic)
3. Remove old DTOs from src.old if still referenced

### Phase 4: Testing & Coverage
**Goal:** Achieve 80% test coverage

**Actions:**
1. Fix all integration tests (H2 database setup)
2. Add unit tests for new services
3. Run coverage analysis
4. Address coverage gaps

## Implementation Notes

### Entity Field Reference
Based on `init-db/01-schema.sql`:

| Entity | Key Fields |
|--------|------------|
| Escritura | numero, fechaEscrituracion, fechaInscripcion, cuerpo, estado, matriculaInscripcion, observaciones |
| Presupuesto | numero, fecha, encabezado, estado, montoInmueble, fkIdPersona, fkIdTramite |
| Persona | nombre, apellido, numeroIdentificacion, esCliente, registroEscribano, fkIdTipoIdentificacion |
| Tramite | numero, nombre, observaciones, fkIdTipoTramite, fkIdGestion, fkIdEscritura, fkIdPresupuesto, fkIdInmueble |
| GestionDeEscritura | numero, fechaInicio, fechaFin, observaciones, fkIdPersonaEscribano, fkIdEstado |
| Pago | monto, fechaPago, estado, fkIdPresupuesto |
| Usuario | nombreUsuario, password, estado, fkIdPersona |
| Folio | numeroFolio, numero, estado, fkIdTipoFolio, fkIdPersonaEscribano |
| Testimonio | numeroTestimonio, estado, fkIdEscritura |

### Repository Method Naming
Spring Data JPA derives queries from method names:
- `findByFieldName` → WHERE field_name = ?
- `findByFieldNameContaining` → WHERE field_name LIKE %?%
- `findByFieldNameIdField` → WHERE field_id = ? (for @ManyToOne)

**Important:** Primitive types (int) vs Objects (Integer) matter for method naming.

## Daily Goals Suggestion

| Day | Goal |
|-----|------|
| Day 1 | Fix repository field mismatches, get tests passing |
| Day 2 | Complete core controllers (Presupuesto, Gestion, Tramite, Pago) |
| Day 3 | Complete medium-priority controllers (10 remaining) |
| Day 4 | Complete remaining controllers, cleanup |
| Day 5 | Testing, coverage analysis, fixes |

## Files Created So Far

### Repositories (27 files)
```
backend-api/src/main/java/com/licensis/notaire/repository/
├── PersonaRepository.java
├── EscrituraRepository.java
├── PresupuestoRepository.java
├── GestionDeEscrituraRepository.java
├── TramiteRepository.java
├── PagoRepository.java
├── UsuarioRepository.java
├── TipoIdentificacionRepository.java
├── InmuebleRepository.java
├── TipoDeTramiteRepository.java
├── TipoDeFolioRepository.java
├── TipoDeDocumentoRepository.java
├── EstadoDeGestionRepository.java
├── FolioRepository.java
├── CopiaRepository.java
├── SuplenciaRepository.java
├── TestimonioRepository.java
├── MovimientoTestimonioRepository.java
├── DocumentoPresentadoRepository.java
├── ItemRepository.java
├── HistorialRepository.java
├── ConceptoRepository.java
├── IdentificacionRepository.java
├── PlantillaTramiteRepository.java
├── PlantillaPresupuestoRepository.java
├── TramitesPersonasRepository.java
└── RegistroAuditoriaRepository.java (was existing)
```

### Services (2 files)
```
backend-api/src/main/java/com/licensis/notaire/service/
├── PersonaService.java
└── EscrituraService.java
```

### Updated Controllers (2 files)
```
backend-api/src/main/java/com/licensis/notaire/api/
├── PersonaController.java (refactored)
└── EscrituraController.java (refactored)
```

### Fixed Entity/DTO
```
notaire-shared/src/main/java/com/licensis/notaire/dto/
└── DtoPresupuesto.java (added getSaldo/setSaldo/getTotal/setTotal stubs)

backend-api/src/main/java/com/licensis/notaire/negocio/
└── Presupuesto.java (added stub methods, fixed field names)
```
