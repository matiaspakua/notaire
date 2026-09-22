> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

See `proposal.md` — Objetivo. The Java entity layer is already fully
English (`business/*.java`); only the `@Table`/`@Column` mappings and the
underlying PostgreSQL identifiers they point at are still Spanish. The
`people` slice (`V25__rename_personas_to_people.sql`, archived change
`rename-persona-to-person`) is the only prior precedent and the template
this design follows. `explore.md` in this change lists the full current
table inventory and a fan-out-based sequencing.

## Goals / Non-Goals

**Goals:**
- Rename every remaining Spanish table/column/sequence name to English,
  matching the Java entity's already-English field names.
- Each slice stays a single, independently revertible PR, following the
  epic's vertical-slice convention — now applied per table cluster instead
  of per business entity, since entities no longer need code changes.
- Zero behavior change: same data, same constraints, same query results.

**Non-Goals:**
- No Java class/field/DTO/endpoint renames (already done).
- No data migration or transformation — only identifier renames.
- No `@NamedQuery` name-string cleanup is required to complete this design
  (tracked as optional per `proposal.md` — Out of Scope), though a slice
  may include it opportunistically if touching that entity anyway.

## Decisions

- **One migration per table cluster, not per column.** Renaming an entire
  table (its own columns + its own sequence) in one migration keeps each
  slice's blast radius aligned with one JPA entity's `@Table` annotation,
  matching how `V25` was scoped. Alternative considered: one migration per
  individual column, rejected as needless churn — a table's own columns
  have no independent rollback need from each other.
- **FK columns on other tables are renamed in their *owning* table's own
  slice, not the referenced table's slice.** This repeats the explicit
  precedent set in `V25`'s migration comment (`fk_id_persona_cliente` was
  left untouched when `personas` was renamed). Keeps each PR's diff scoped
  to one entity's own file, and avoids a slice needing to touch N other
  entities' Java files just because they hold a foreign key to it.
- **Leaf-to-root sequencing** (see `explore.md` — Sequencing options):
  minimizes the chance that an early slice's migration collides with FK
  constraints still pointing at not-yet-renamed columns elsewhere, and
  re-validates the rename pattern on low-risk tables before the
  high-fan-out core tables (`tramites`, `escrituras`, `presupuestos`).
- **`ALTER TABLE ... RENAME` / `ALTER TABLE ... RENAME COLUMN`, not
  create-new-drop-old.** PostgreSQL's rename statements are atomic, keep
  existing FK constraints, indexes and sequences intact, and require no
  data copy — the same approach `V25` used successfully.

## Riesgos / Trade-offs

- **N small PRs instead of 1 big one** → higher total review/CI overhead,
  but each is independently revertible and low-risk; this is the epic's
  own explicit design choice (vertical slices), not new risk introduced
  here.
- **`historial` / `registro_auditoria` renames could break raw-SQL
  consumers outside JPA** (Grafana/Loki panels, `AuditoriaAspect` if it
  uses native queries) → mitigated by grepping `infra/` and
  `AuditoriaAspect` for raw table names before that specific slice; if
  found, update those queries in the same PR.
- **A slice's migration could collide with another concurrently-merged
  migration's version number** (solo-maintainer repo, so low likelihood,
  but slices span multiple PRs over time) → mitigated by checking
  `ls backend-api/src/main/resources/db/migration/ | sort -V | tail -1`
  immediately before writing each slice's migration, not upfront.
- **Test/Bruno fixtures hardcoding raw SQL column names** → mitigated by a
  grep pass (`grep -r <old_column_name>`) across `src/test/` and
  `backend-api/api-test/` as part of each slice's own task list, not
  assumed absent.

## Testing Strategy

No delta spec exists (`skip_specs: true`) — this is a pure schema rename,
verified structurally rather than via new business-behavior scenarios:

| Verification | Test level | Test class / file |
|-----------------|------------|-------------------|
| Renamed table/columns match JPA mapping | integration | `FlywaySchemaValidationIntegrationTest` (`mvn test -Ppg-integration`) |
| Existing entity CRUD still works post-rename | integration | existing `*IntegrationTest` for the renamed entity (unchanged assertions — same behavior, different physical names) |
| Existing unit tests for the entity | unit | existing `*Test` under `unit/` (should require zero changes, since Java field names don't move) |

- New unit tests: none expected — no Java behavior changes.
- New integration tests: none expected beyond re-running
  `FlywaySchemaValidationIntegrationTest`, which already asserts
  schema/entity alignment and will fail if a rename is incomplete or
  mismatched (this is this change's "failing test first" signal per slice
  — verify it fails against the *old* schema/new entity mapping mismatch
  before the migration lands, per TDD).
- Coverage impact: none expected (no new code paths).

## Regression Strategy

- Existing tests affected: any test that hardcodes a raw SQL table/column
  name for the entity in that slice (to be identified per slice via grep,
  not assumed upfront).
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: `jpa` package controllers for the same entity
  (e.g. `DeedJpaController`) — these already use the English entity/field
  names via JPA, so no source change expected, but included in each
  slice's regression run.

## Playwright Strategy

n/a — no UI surface. This is a persistence-layer-only rename; the REST
contract and frontend already use English field names and are unaffected.

## Deployment Strategy

- Flyway migration required: yes — one `V{n}__rename_<table>_to_<name>.sql`
  per slice, next available version checked at implementation time for
  each slice (starting no earlier than `V26`).
- Deployment order / coupling: migration and the corresponding
  `@Table`/`@Column` annotation update ship in the same PR/commit — they
  must land together, since the entity would otherwise fail to map against
  either the old or the new schema alone.
- Configuration or `.env` keys: none.
- Feature flag: no — schema rename is all-or-nothing per slice, not
  toggleable.
- Smoke test after deploy (Gate 5): verify the renamed entity's own CRUD
  endpoint (e.g. `GET/POST /api/v1/<resource>`) against the deployed
  environment.

## Rollback Strategy

- Revert safe: yes, if reverted before any subsequent slice or app code
  depends on the new name — a straight `git revert` of the PR restores the
  old `@Table`/`@Column` mapping, but the Flyway migration itself is
  additive-only (Constitution P6: never edit/delete an applied migration),
  so rollback requires a new down-migration (`V{n+1}__revert_<table>_rename.sql`),
  not deleting the applied one.
- Database rollback: forward-fix only, via a new reverse-rename migration —
  consistent with Flyway's additive-only model.
- Data written under the new behavior after revert: none at risk — column
  renames carry existing data forward unchanged; no data loss on either
  direction.
- Blast radius if rollback is delayed: none beyond the single entity in
  that slice, since slices are independent and FK-column renames are
  deferred to the owning table's own slice.

## Migration Plan

Ordered by `explore.md`'s fan-out sequencing — leaf tables, then mid
fan-out, then high fan-out/central tables. Each step is its own PR/branch;
see `tasks.md` for the per-slice checklist template and the concrete first
slice.

1. Leaf: `identificaciones`, `tipos_identificacion`, `tipos_de_documento`,
   `tipos_de_folio`, `tipos_de_tramite`, `roles`, `estados_de_gestion`,
   `registro_auditoria`, `cuadernos`
2. Mid fan-out: `inmuebles`, `conceptos`, `items`, `pagos`, `copias`,
   `folios_copias`, `movimientos_testimonio`, `documentos_presentados`,
   `plantilla_tramites`, `plantilla_presupuestos`,
   `plantilla_costos_documento`, `carpetas_tramite`, `minutas_inscripcion`
3. High fan-out / central: `tramites`, `escrituras`,
   `gestiones_de_escrituras`, `presupuestos`, `testimonios`, `suplencias`,
   `historial`, `tramites_personas`, `folios` (folio's own table may
   already be partly aligned — verify at that slice's implementation time)

## Open Questions

None — the pattern, sequencing and per-slice checklist are fully
determined by the already-merged `people` precedent; nothing here would
change the specs, approach, or task breakdown if answered differently.
