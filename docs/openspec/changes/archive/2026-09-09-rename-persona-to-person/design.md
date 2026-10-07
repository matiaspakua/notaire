# Design: Rename Persona entity to Person

## Context

`Persona` is the smallest-fanout core domain entity (referenced by
`TramitesPersonas`, `Usuario`, `GestionDeEscritura`, `Folio`, `Suplencia`,
`Copia`, `Testimonio`), making it the lowest-risk candidate to validate the
rename pattern (migration + entity + repo + controller + DTO + REST + frontend
+ tests) before tackling higher-fanout entities in the epic (#973).

## Goals / Non-Goals

**Goals**
- Rename `Persona` → `Person` end-to-end with zero behavior change.
- Establish a repeatable, documented pattern for subsequent entity slices.
- Keep the change atomic and independently revertible via a single new
  Flyway migration (no edits to existing migrations, per P6).

**Non-Goals**
- Renaming any other entity in this change (tracked as separate epic slices).
- Translating `docs/` (explicitly out of scope, per epic #973).
- Any behavior or validation change.

## Decisions

- **DB rename via new migration, not edit**: add
  `V{n}__rename_persona_to_person.sql` that `ALTER TABLE ... RENAME TO` /
  `RENAME COLUMN`. Existing migrations (`V1`-`V11+`) stay untouched per P6.
- **REST path**: `/api/v1/personas` → `/api/v1/people`, JSON fields
  translated to English to match the entity/DTO rename (breaking API change,
  acceptable since backend + frontend are versioned/deployed together in this
  monorepo and there are no external API consumers documented).
- **`isNew()`/`Persistable` semantics preserved**: the entity keeps its
  `Persistable<Integer>` override (same fix that prevents `deleteById()`
  silently no-op'ing on unpersisted rows with default `version=0`); only
  names change, not the mechanism.

## Riesgos / Trade-offs

- **Risk**: missed reference during the mechanical rename causes a
  compile-time or runtime break. Mitigated by relying on `mvn compile` /
  `mvn test` to surface every unresolved reference before commit — a rename
  cannot silently half-apply in a statically typed codebase.
- **Risk**: Flyway rename migration run against non-empty `persona` table in
  a live environment. Mitigated by `ALTER TABLE RENAME` (metadata-only,
  no data loss) rather than `CREATE`+copy+`DROP`.
- **Trade-off**: breaking the REST contract (`/personas` → `/people`) instead
  of keeping deprecated aliases — accepted because there are no known
  external consumers, and the epic aims for full translation rather than
  perpetual dual-naming.

## Testing Strategy

- Existing backend unit/integration tests are renamed and adapted in place
  (`PersonaTest` → `PersonTest`, field/method names updated) rather than
  duplicated — this is a rename, not new coverage.
- `FlywaySchemaValidationIntegrationTest` (`mvn test -Ppg-integration`)
  validates the new migration keeps entity↔schema in sync.
- No new business scenarios are introduced, so `skip_specs: true` — no
  `specs/*.md` delta is authored for this change.

## Regression Strategy

Full backend suite (`mvn verify -pl backend-api`) plus full Playwright E2E
suite must stay green, since the rename touches JSON contracts consumed by
every frontend page that renders person data (personas list/detail,
escrituras, folios, tramites, gestiones, suplencias).

## Playwright Strategy

Existing E2E specs referencing `/personas` UI and `Persona` fixtures/helpers
are updated in place to the new routes/labels; no new scenarios needed since
UX and behavior are unchanged.

## Deployment Strategy

Standard: merge to `main`, Flyway applies the new migration on next backend
startup against the target database. Backend and frontend must deploy
together (breaking REST contract).

## Rollback Strategy

Revert the merge commit; a follow-up down-migration
(`V{n+1}__revert_rename_persona_to_person.sql`) restores the original
Spanish table/column names if a rollback after deploy is needed (per P6,
never edit or delete the applied migration).
