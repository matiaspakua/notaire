# Corregir DELETE silencioso en entidades con `version` primitivo

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #957 |
| Use Case | Transversal — CU29, CU34, CU37, CU38, CU40, CU58 (pantallas de administración de tablas base con borrado) |
| Branch | `fix/957_silent-delete-persistable-fix` |
| Gate 1 status | pending |

## Objetivo

`DELETE` sobre cualquiera de 30 entidades del paquete `com.licensis.notaire.negocio`
devuelve `200 OK` sin borrar la fila, porque `SimpleJpaRepository.isNew()` infiere
"entidad nueva" a partir de un campo `private int version` primitivo en `0`,
indistinguible de una fila recién leída que ya tiene `version == 0` real. El patrón
ya fue diagnosticado y corregido para `Historial`, `Item`, `Pago` y `Tramite`
(#952); este change extiende la misma corrección al resto del modelo.

## What Changes

- Implementar `Persistable<Integer>` (25 entidades con ID surrogate) o
  `Persistable<XxxPK>` con flag transitorio `@PostLoad`/`@PostPersist` (5
  entidades con `@EmbeddedId`), con `isNew()` explícito, en las 30
  entidades restantes con `private int version` primitivo (ver design.md,
  Decisiones 1 y 2).
- Agregar, para cada entidad afectada, un test de integración de dos
  transacciones (plantilla: `HistorialDeleteIntegrationTest`) que reproduzca el
  borrado silencioso antes de la corrección y confirme el borrado real después.
- Auditar las colecciones `@OneToMany(cascade = CascadeType.ALL, fetch = EAGER)`
  bidireccionales del modelo por el mismo riesgo (cascade revirtiendo un delete
  silenciosamente) y documentar el resultado de la auditoría en `design.md`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Un `DELETE` sobre cualquier recurso de administración de tablas base debe eliminar la fila o fallar explícitamente — nunca devolver éxito sin efecto | Implícito en cualquier CU con paso "eliminar" (CU29, CU34, CU37, CU38, CU40, CU58) | Made explicit |

## Capabilities

### New Capabilities
- `data-integrity/persistable-delete`: garantiza que `isNew()` de Spring Data JPA se infiera del identificador, no de `version`, para toda entidad con `@Version` primitivo — de forma que `delete`/`deleteById` sobre una entidad existente siempre emita el `DELETE` real.

### Modified Capabilities
(ninguna — no existe spec previa de este comportamiento; es puramente correctivo)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | 30 clases `@Entity` en `negocio/` implementan `Persistable<Integer>` o `Persistable<XxxPK>`; nuevos tests de integración |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities (30, confirmed via `grep -rl "private int version"
  backend-api/src/main/java/com/licensis/notaire/negocio/` minus the 4
  already fixed — see design.md Context for the count correction from the
  initial ~26 estimate):
  - 25 with a surrogate `@GeneratedValue(IDENTITY)` integer key (Decision 1
    in design.md — `Persistable<Integer>`, `isNew() = id == null`):
    `Concepto`, `Copia`, `Cuaderno`, `DocumentoPresentado`, `Escritura`,
    `EstadoDeGestion`, `Folio`, `GestionDeEscritura`, `Inmueble`,
    `MinutaInscripcion`, `MovimientoTestimonio`, `Persona`, `Presupuesto`,
    `RegistroAuditoria`, `Rol`, `Suplencia`, `Testimonio`,
    `TipoDeDocumento`, `TipoDeFolio`, `TipoDeTramite`,
    `TipoIdentificacion`, `Usuario`, `WorkflowDefinition`, `WorkflowNode`,
    `WorkflowTransition`.
  - 5 with an `@EmbeddedId` composite key (Decision 2 in design.md —
    `Persistable<XxxPK>` with a `@PostLoad`/`@PostPersist` transient flag,
    since the ID is client-assigned and never `null`): `FoliosCopias`,
    `PlantillaCostoDocumento`, `PlantillaPresupuesto`, `PlantillaTramite`,
    `TramitesPersonas`.
- Endpoints: no new endpoints; existing `DELETE /api/v1/<resource>/{id}` endpoints for each affected entity now behave correctly (no contract change, only correct behavior)
- Database (Flyway `V{n}`): none — no schema change
- Configuration / `.env`: none
- Dependencies: none — uses `org.springframework.data.domain.Persistable`, already on the classpath

### Architecture review

Follows the existing architecture: reuses the exact `Persistable<Integer>` +
explicit `isNew()` pattern already established in `Historial`/`Item`/`Pago`/
`Tramite`. No ADR required — this is a bug-fix applying an already-adopted
pattern consistently, not a new architectural decision.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `backend-api/api-test/COVERAGE.md` | Add a defect entry (in the style of defecto #8/#9) noting the entities fixed by this change |
| `CHANGELOG.md` | Entry under `[Unreleased]` — silent-delete fix extended to remaining entities |
| `openspec/explore.md` | Move the #957 row from "Pendientes" to "Resueltos" once merged |

## Out of Scope

- Migrating `version` from primitive `int` to boxed `Integer` as an alternative
  fix — `Persistable<Integer>` is preferred because it matches the pattern
  already adopted for `Historial`/`Item`/`Pago`/`Tramite` and avoids touching
  the `@Version` optimistic-locking column type.
- Any cascade-related fix found during the audit that is not a direct
  contributor to a silent `DELETE` is tracked as a new Issue, not fixed inline
  here.
