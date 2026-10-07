> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Presupuesto` (`negocio/Presupuesto.java`) and `Tramite` (`negocio/Tramite.java`) each
declare a foreign key to the other: `presupuestos.fk_id_tramite`
(`Presupuesto.fkIdTramite`, `@ManyToOne`) and `tramites.fk_id_presupuesto`
(`Tramite.fkIdPresupuesto`, `@ManyToOne`, with `Presupuesto.tramiteList` as its
`@OneToMany` inverse). Both columns exist in the schema (`V1__initial_schema.sql`).
Grepping every write site confirms only `Tramite.fkIdPresupuesto` is touched by the
live, modern path — `GestionController.applyTramiteDependencies` (the code behind CU02
"Iniciar Gestión") sets it on every complete-case creation/update.
`Presupuesto.fkIdTramite` is set only inside the deprecated `ControllerNegocio` god
class (lines ~740, 820, 977-984) and its `PresupuestoJpaController`/`TramiteJpaController`
helpers — the legacy path CLAUDE.md already marks as superseded by `repository`. See
proposal.md - Objetivo for the full motivation.

## Goals / Non-Goals

**Goals:**
- Leave exactly one, consistent Presupuesto↔Tramite relation in the entity model,
  matching what the live modern write path already does.
- Remove the dead legacy code paths that set the field being dropped, rather than
  leaving them to silently no-op.
- Protect any real data in `presupuestos.fk_id_tramite` with a migration that verifies
  before it drops.

**Non-Goals:**
- Building any UI for selecting/associating a Tramite with a Presupuesto — no such UI
  exists today and none is added here (tracked separately if ever needed).
- Wiring `PlantillaPresupuesto`/`Item` into budget creation (issue #797) — this change
  only clears the data-model ambiguity that issue depends on.
- Changing `Tramite`'s side of the relation in any way — `Tramite.fkIdPresupuesto` and
  `Presupuesto.tramiteList` are kept exactly as they are today.

## Decisions

**Decision: Keep `Tramite.fkIdPresupuesto`/`Presupuesto.tramiteList`; remove
`Presupuesto.fkIdTramite`.**
Alternative considered: keep `Presupuesto.fkIdTramite` (one Presupuesto → one Tramite)
and remove the `tramiteList`/`fkIdPresupuesto` pair instead. Rejected because it would
require rewriting `GestionController.applyTramiteDependencies` — the live, currently
working CU02 flow — to fit a cardinality nothing in the running system uses today. The
evidence (grep of every write site) shows the system has already, in practice, chosen
"one Presupuesto, many Tramites"; codifying that is a data-model correction, not a
behavior change.

**Decision: Delete the dead legacy code that wrote the removed field, in the same
change.**
Alternative considered: leave `ControllerNegocio`'s references and let them fail to
compile, forcing a follow-up fix. Rejected — Constitution §6 (KIS/SRP, no dead code)
and `.claude/rules/general.md` rule 11 require removing dead/duplicate code as part of
the same change, and leaving a non-compiling legacy class would block every other
change until someone else cleaned it up.

**Decision: Deprecate and exclude `frontend-swing` from the build rather than
migrate its `DtoPresupuesto.getTramite()`/`setTramite()` call sites.**
Discovered mid-implementation: `proposal.md`'s original Impact Analysis checked
`frontend/src` (Next.js) only and missed that `frontend-swing` (~6 Swing screens)
reads/writes `DtoPresupuesto`'s single-tramite field. Alternative considered:
migrate those call sites to use the surviving `listaTramites`/Tramite-owns-the-key
relation instead. Rejected on explicit user direction — `frontend-swing` is
deprecated project-wide, not just for this change, so investing effort adapting it
would contradict that direction. Instead: removed from the root reactor
(`pom.xml`), from the `ci.yml` unit-test job, and from `scripts/preflight.sh`;
`frontend-swing/README.md` and `CLAUDE.md` now say not to build on it. The module's
source is left in place (uncompiled) rather than deleted.

**Decision: Migration verifies the column is empty before dropping it, rather than
assuming it.**
Alternative considered: drop the column unconditionally, since the report found the
path "latent." Rejected — "latent as of the report's read" is not the same as
"guaranteed empty in every environment," and a migration that silently discards data
if that assumption is wrong is worse than one that fails loudly and asks for review.

## Riesgos / Trade-offs

- [Risk] A production or staging database has non-null `presupuestos.fk_id_tramite`
  rows from historical use of the legacy `ControllerNegocio` path → Mitigation: the
  Flyway migration checks `COUNT(*) WHERE fk_id_tramite IS NOT NULL` and raises before
  dropping the column if it is non-zero; the migration author reviews and resolves the
  conflicting rows by hand rather than the migration deciding automatically.
- [Risk] `DtoPresupuesto` API consumers outside this repository (if any exist) read the
  removed `tramite` field → Mitigation: none possible within this change since such
  consumers are outside the codebase; declared as **BREAKING** in proposal.md and
  called out in the `CHANGELOG.md` entry so any external consumer can react.
- [Risk] Deleting the legacy `ControllerNegocio`/`*JpaController` code paths that
  reference `fkIdTramite` could remove logic something else in that god-class still
  depends on → Mitigation: each removal is scoped to the exact lines referencing the
  removed field (see proposal.md - What Changes); the surrounding methods are read in
  full before editing to confirm no other logic shares those lines.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|--------------------|
| A Tramite is created and associated to a Presupuesto | integration | `GestionControllerIntegrationTest` (extend an existing complete-case test to assert the Tramite's Presupuesto reference persists) |
| A Presupuesto is associated with more than one Tramite | unit | `PresupuestoEntityTest` (new test: two Tramites added to one Presupuesto's `tramiteList`) |
| A Tramite belongs to at most one Presupuesto | unit | `TramiteEntityTest` (new test: reassigning `fkIdPresupuesto` removes the Tramite from the old Presupuesto's collection) |
| Presupuesto no longer exposes a single-Tramite reference | unit | `PresupuestoEntityTest` (replace the existing "Should link presupuesto to tramite" test, which calls the removed `setFkIdTramite`, with an assertion that no such accessor exists / `DtoPresupuesto` has no `tramite` field) |
| Migration refuses to drop the column if data would be lost | integration | `FlywaySchemaValidationIntegrationTest` (`mvn test -Ppg-integration`) — new test seeding a non-null `fk_id_tramite` row against the pre-migration schema and asserting the migration fails |

- New unit tests (`src/test/java/.../unit/`): `PresupuestoEntityTest`,
  `TramiteEntityTest` — written first and run to confirm they fail against the current
  entity model (TDD, Constitution P1) before `fkIdTramite` is removed.
- New integration tests (`src/test/java/.../integration/`): extension to
  `GestionControllerIntegrationTest`; new guard test in
  `FlywaySchemaValidationIntegrationTest`.
- Coverage impact: net code reduction (one field/getter/setter plus ~6 legacy call
  sites removed) with new test coverage added on the surviving relation; expected to
  hold or improve against the JaCoCo ratchet floor (currently ~74% branch / ~84% line
  per `.claude/rules/code-quality.md`), verified with `mvn jacoco:check -pl backend-api`.

## Regression Strategy

- Existing tests affected: `PresupuestoEntityTest` ("Should link presupuesto to
  tramite", currently calling `presupuesto.setFkIdTramite(tramite)`) must be rewritten
  — the old expectation (a Presupuesto directly holding a Tramite reference) is exactly
  the contradiction this change resolves, so it is not weakened, it is replaced by the
  new scenario-mapped test above. Any other reference to `Presupuesto.getFkIdTramite()`/
  `setFkIdTramite()` across `src/test/java` must be located (`grep -rn "FkIdTramite"
  backend-api/src/test`) and updated in the same commit.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh` — re-run the
  presupuestos collection to confirm no request/response shape assumption broke beyond
  the documented, intentional removal of the `tramite` field.
- Legacy paths at risk: `ControllerNegocio.java` (~lines 740, 820, 977-984) and
  `PresupuestoJpaController.java`/`TramiteJpaController.java` — these are edited to
  remove the now-invalid field access; `unit/jpa/RemainingControllersJpaTest.java` and
  `unit/jpa/AdministradorJpaTest.java` are checked for any test that exercises the
  removed code paths and updated accordingly.

## Playwright Strategy

n/a - no UI surface. No frontend page reads or writes a single-tramite field on
Presupuesto today (verified via grep of `frontend/src`); this change is entity/schema
only and has no user-observable UI behavior to cover.

## Deployment Strategy

- Flyway migration required: yes (`V14__drop_presupuestos_fk_id_tramite.sql`).
- Deployment order / coupling: standard — Flyway migrations run on backend-api
  startup before the application accepts traffic (existing `start.sh` behavior), so
  code and schema change deploy together as one artifact; no special ordering needed
  since nothing else depends on the dropped column.
- Configuration or `.env` keys to add: none.
- Feature flag: no — this is a data-model correction with no user-facing toggle.
- Smoke test after deploy (Gate 5): create a gestión end-to-end through
  `POST /api/v1/gestiones/complete-case` (the CU02 flow) and confirm the created
  Tramite's Presupuesto association is retrievable via
  `GET /api/v1/presupuestos/{id}`'s associated-Tramite data — proves the surviving
  relation still works post-deploy.

## Rollback Strategy

- Revert safe: no, not as a pure code revert once the migration has run — reverting the
  application code without reversing the migration would reintroduce
  `Presupuesto.fkIdTramite` in code while the column no longer exists, breaking startup
  entity mapping. A rollback requires reverting the code AND running an `R14` rollback
  script that re-adds the column, or rolling forward with a fix instead.
- Database rollback: `R14__restore_presupuestos_fk_id_tramite.sql` (adds the column
  back, nullable, no data — since the removed column was verified empty before drop,
  there is nothing to restore) — created alongside `V14` per
  `.claude/rules/database-migrations.md`'s rollback-script guidance for structural
  changes.
- Data written under the new behavior after revert: none is lost, because the dropped
  column was empty at the time it was dropped (guaranteed by the migration's own
  pre-drop check) and no new data is ever written to the removed relation between
  deploy and a hypothetical revert.
- Blast radius if rollback is delayed: low — the removed relation had no live
  consumer (frontend or modern backend) before this change, so a delayed rollback
  leaves the system in the same functional state as immediately after deploy.

## Migration Plan

1. Add failing unit tests (`PresupuestoEntityTest`, `TramiteEntityTest`) that assert
   the new, single-relation behavior — confirm they fail against today's entity model.
2. Remove `Presupuesto.fkIdTramite` field, getter/setter, and its `DtoPresupuesto`
   mapping in `Presupuesto.getDto()`/`setAtributos()`.
3. Remove the legacy call sites in `ControllerNegocio.java` and
   `PresupuestoJpaController.java` that read/write `fkIdTramite`.
4. Add `V14__drop_presupuestos_fk_id_tramite.sql` (pre-drop empty-column check, drop
   the `fk_presupuestos_tramite` constraint, drop the column) and its `R14` rollback
   script.
5. Run the new unit/integration tests, confirm green; run
   `mvn test -Ppg-integration` to validate the Flyway migration against a real
   PostgreSQL instance (`FlywaySchemaValidationIntegrationTest`).
6. Run `mvn verify -pl backend-api` and `bash scripts/preflight.sh` before pushing.

## Open Questions

None — the cardinality decision, the migration's data-safety behavior, and the scope
of the legacy-code removal are all resolved above.
