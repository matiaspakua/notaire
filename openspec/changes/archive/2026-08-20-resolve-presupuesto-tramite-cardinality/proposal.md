# Resolve contradictory Presupuesto↔Tramite cardinality

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #798 |
| Use Case | CU01 – Preparar Presupuesto, CU45 – Modificar presupuesto |
| Branch | `fix/798_resolve-presupuesto-tramite-cardinality` |
| Gate 1 status | pending |

## Objetivo

`Presupuesto` and `Tramite` are connected through two independent, contradictory JPA
relations: `presupuestos.fk_id_tramite` (one Presupuesto → at most one Tramite) and
`tramites.fk_id_presupuesto` (one Presupuesto → many Tramites). Nothing keeps them in
sync, and the entity model cannot answer whether a budget belongs to one transaction or
can span several. Reading the actual write paths resolves the ambiguity: the modern,
live case-creation flow (`GestionController.applyTramiteDependencies`, the code behind
CU02 "Iniciar Gestión" and consumed from the real `gestiones` screen) exclusively
writes `Tramite.fkIdPresupuesto` and never touches `Presupuesto.fkIdTramite`, which is
set only by the deprecated `ControllerNegocio` god-class and its `*JpaController`
helpers. This change removes the unused relation so the domain model matches the
system's actual, live behavior before issue #797 (connecting `PlantillaPresupuesto`/
`Item` to budget creation) starts building on top of it.

## What Changes

- Remove `Presupuesto.fkIdTramite` (`@ManyToOne`) and the backing
  `presupuestos.fk_id_tramite` column/FK constraint.
- Keep `Tramite.fkIdPresupuesto` (`@ManyToOne`, backing `tramites.fk_id_presupuesto`)
  and its inverse `Presupuesto.tramiteList` (`@OneToMany`) as the sole, canonical
  relation: **one Presupuesto can be associated with many Tramites**.
- Remove the legacy read/write usages of `Presupuesto.fkIdTramite` in
  `ControllerNegocio.java` (lines ~740, 820, 977-984) and `PresupuestoJpaController.java`
  (lines ~64-67, 162-207, 345) — these become dead code once the column is gone.
- **BREAKING**: any API consumer relying on `Presupuesto.getFkIdTramite()` /
  `DtoPresupuesto.getTramite()` (single-tramite shape) loses that field. No known
  frontend consumer exists today (`frontend/src/types/index.ts` and
  `presupuestos/page.tsx` were checked — neither reads or writes a single-tramite
  field on Presupuesto), so this is breaking in principle, not in observed practice.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A Presupuesto may be associated with zero or more Tramites; a Tramite belongs to at most one Presupuesto. | CU01 (a presupuesto is generated for a requested tipo de trámite), confirmed as the live behavior of `GestionController`'s CU02 implementation | Made explicit — the code already behaved this way on the modern path; the contradictory reverse relation is removed, not the rule itself |

## Capabilities

### New Capabilities
- `presupuesto-tramite-relation`: the Presupuesto↔Tramite association — cardinality,
  which side owns the foreign key, and that no second, contradictory relation exists
  between the two entities.

### Modified Capabilities
_None — no existing capability spec under `openspec/specs/` covers this relation yet
(`openspec/specs/` is currently empty); this is the first spec for it._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Remove `Presupuesto.fkIdTramite` field/getter/setter, its `DtoPresupuesto` mapping, the `presupuestos.fk_id_tramite` column/FK, `Tramite.presupuestoList` (the field's inverse side), and the legacy `ControllerNegocio`/`PresupuestoJpaController`/`TramiteJpaController` code paths that touch either |
| `frontend` | no | No frontend code reads or writes a single-tramite field on Presupuesto (verified via grep of `frontend/src`) |
| `frontend-swing` | yes (deprecated, out of scope) | Originally assessed as "no" — corrected: `DtoPresupuesto.getTramite()`/`setTramite()` is used in ~6 Swing screens. This change deprecates the module instead of migrating it: removed from the root Maven reactor (`pom.xml`) and CI (`ci.yml`, `scripts/preflight.sh`) rather than adapted to the new DTO shape. See Decisions in `design.md` |
| `notaire-shared` | yes | `DtoPresupuesto` loses its `tramite` field/accessors (the field `frontend-swing` depended on) |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Presupuesto` (`negocio/Presupuesto.java`) loses `fkIdTramite`; `Tramite`
  (`negocio/Tramite.java`) and its `fkIdPresupuesto` field are unchanged.
- Endpoints: none change signature. `PresupuestoController`/`PresupuestoService`
  (modern REST path) never referenced `fkIdTramite`, so no endpoint contract changes.
  `DtoPresupuesto` drops its `tramite` field — a shape change on
  `GET/POST/PUT /api/v1/presupuestos*` responses.
- Database (Flyway `V14`): new migration drops the `fk_presupuestos_tramite`
  constraint and the `presupuestos.fk_id_tramite` column.
- Configuration / `.env`: none.
- Dependencies: none.

### Architecture review

Follows the existing architecture direction: `repository`/modern REST path is kept as
the source of truth, the legacy `jpa`/`ControllerNegocio` usage of the removed field is
deleted rather than preserved (CLAUDE.md: new code uses `repository`, not `jpa`; the
`jpa` package is being superseded). No ADR required — this is a targeted data-model
correction, not a new architectural pattern.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/01-business/00-FUNCTIONAL-BASELINE.md` | If it documents the Presupuesto↔Tramite relation, update it to state the single, resolved cardinality (one Presupuesto → many Tramites) instead of leaving it implicit |
| `CHANGELOG.md` | Add an entry noting the removed `Presupuesto.fkIdTramite`/`tramite` field is no longer part of the Presupuesto API shape |

## Out of Scope

- Wiring `PlantillaPresupuesto`/`Item` into budget creation — tracked separately in
  issue #797, which depends on this change landing first so it builds on the correct
  cardinality.
- Any UI change to the `presupuestos` create/edit form — no tramite-selection UI exists
  today and none is added here; this change only resolves the entity-level model.
- Backfilling or migrating any existing data that might currently populate
  `presupuestos.fk_id_tramite` — the report found this path latent (never written by
  the modern flow), but the migration script will verify the column is empty in
  practice before dropping it, and stop with a clear error if it is not.
