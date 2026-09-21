# Translate remaining PostgreSQL schema to English

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #973 |
| Use Case | none — purely technical/internal-quality change (naming), no user-facing behavior changes; epic #973 itself documents this same exception rather than a Use Case |
| Branch | `refactor/973_<table-cluster>_schema_rename` (one branch per slice, see tasks.md) |
| Gate 1 status | pending |

## Objetivo

Issue #973 (epic) asked to translate the domain model to English end-to-end.
An audit of the current codebase (see `explore.md` in this change) found the
Java entity/DTO/repository/controller layer, REST contract field names, and
frontend types are already fully translated — done ahead of the epic's own
vertical-slice sequencing, alongside the already-merged `people` slice
(archived change `rename-persona-to-person`). The only layer still in
Spanish is the PostgreSQL schema itself: 24+ tables and their columns. This
change closes that remaining gap so `@Table`/`@Column` annotations finally
match the already-English Java model, removing the last source of
Spanish/English naming drift in the codebase.

## What Changes

- Add one new Flyway migration per table cluster (leaf tables first, per
  `explore.md` sequencing), renaming Spanish table/column/sequence names to
  English, matching names already used by the corresponding Java entity's
  fields.
- Update the corresponding `@Table(name = ...)` / `@Column(name = ...)`
  annotations in `backend-api/src/main/java/com/licensis/notaire/business/`
  to point at the new English names.
- Optional, non-blocking cleanup: rename `@NamedQuery` name strings (e.g.
  `"Escritura.findByFechaEscrituracion"`) to English for consistency; these
  are cosmetic identifiers, not SQL, so they carry no migration risk.
- **Not** changing: Java class/field names (already English), DTOs, REST
  endpoints/JSON field names, frontend code — none of these reference raw
  SQL identifiers today (verified by grep in `explore.md`).
- **BREAKING**: none expected for API clients — this is entirely internal
  to the persistence layer. A slice becomes breaking only if a test or
  Bruno fixture hardcodes a raw SQL table/column name; each slice's own
  regression run catches this before merge.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| FK columns on a table that reference an *other* table being renamed in this pass are left untouched until that other table's own slice, per the precedent set by `V25__rename_personas_to_people.sql` | Epic #973 vertical-slice convention (made explicit in V25's own migration comment) | Made explicit |
| Every renamed migration is additive (`V{n}__rename_x_to_y.sql`); no existing migration is edited | CONSTITUTION.md P6 | Made explicit (not new — restating existing project rule so each slice's task list references it) |

No business behavior changes; this is a pure internal refactor of physical
schema naming with no functional requirement attached.

## Capabilities

`skip_specs: true` is set in this change's `.openspec.yaml` — no
capability's observable behavior changes (pure schema-naming refactor).

### New Capabilities
None.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | New Flyway migrations under `db/migration/`; `@Table`/`@Column` annotation updates in `business/*.java`; no field/type changes |
| `frontend` | no | REST contract field names unaffected (already English, backed by JPA annotation mapping, not raw column pass-through) |
| `frontend-swing` | no | N/A (module removed per CLAUDE.md) |
| `notaire-shared` | no | No shared DTOs reference raw SQL identifiers |
| `infra` / observability | possible | `historial`/`registro_auditoria` renames need a check against any raw-SQL Grafana/Loki panels or `AuditoriaAspect` queries before their slice (flagged in `explore.md` risks) |
| CI/CD (`.github/workflows`) | no | No workflow references raw table names |

### Surface area

- Entities: no Java class/field changes — only `@Table`/`@Column` mapping updates across `business/*.java` (one entity per slice)
- Endpoints: none — REST contracts unaffected
- Database (Flyway `V{n}`): one migration per slice, starting at `V26` (next free version after `V25__rename_personas_to_people.sql`); each slice's task specifies its own version at implementation time to avoid collisions with any concurrently merged migration
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows existing architecture and the precedent already set by the `people`
slice: Flyway is the single source of truth for schema (CONSTITUTION.md P6),
migrations are additive-only, and this repeats a pattern already validated
in production. No ADR required — this is a continuation of an established
convention, not a new architectural decision.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/` schema/data-model docs (if any reference table names by their current Spanish names) | Update table names referenced, if found during implementation |
| `CHANGELOG.md` | n/a — not user-visible; internal schema-naming refactor only |

`docs/` business documents stay in Spanish per the epic's explicit
out-of-scope note — this only affects physical database identifiers, not
business terminology.

## Out of Scope

- Renaming Java classes/fields/DTOs/REST contracts/frontend — already done,
  outside this change.
- `docs/` business documentation (Use Cases, ADRs) — stays in Spanish per
  epic #973's own explicit scope note.
- `@NamedQuery` name-string cleanup is listed as optional in this proposal;
  if not completed within this change's tasks, it should be tracked as a
  small separate follow-up, not left silently undone.
- Any table not currently owned by a `business/*.java` entity's own
  `@Table` annotation (e.g. Flyway-only bookkeeping tables) is out of
  scope — this change only touches tables backing a JPA entity.
