> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`SimpleJpaRepository.isNew()` (via `JpaMetamodelEntityInformation`) infers
"new entity" from the `@Version` property when one is present: if the
version getter returns `null`, or (for a primitive) returns `0`, the entity
is treated as unsaved and `delete()`/`deleteById()` silently skips the SQL
`DELETE`, still returning success. Every affected entity declares
`private int version` (primitive, default `0`), so a freshly-loaded row that
has never been updated (`version == 0`, which is the common case) is
misclassified as new. `Historial`, `Item`, `Pago`, `Tramite` were already
fixed (#952) by implementing `Persistable<Integer>` with an explicit
`isNew()` based on the `@Id` field instead of `version`. See proposal.md for
the full motivation and business impact.

The audit for this change (`grep -rl "private int version" negocio/`) found
**30** affected entities, not the ~26 estimated in the proposal — 5 of them
(`FoliosCopias`, `PlantillaCostoDocumento`, `PlantillaPresupuesto`,
`PlantillaTramite`, `TramitesPersonas`) use `@EmbeddedId` composite keys that
are constructed client-side before the entity is ever persisted, not
`@GeneratedValue`. That distinction drives Decision 2 below.

## Goals / Non-Goals

**Goals:**
- Every entity with a primitive `@Version` field reports `isNew()`
  correctly, so `delete()`/`deleteById()` on an existing row always emits a
  real `DELETE`.
- The fix is verified per entity with a reproducing integration test
  (red before the fix, green after), not just asserted by inspection.
- The two structurally different ID shapes in this model (surrogate
  `@GeneratedValue` integer vs. client-assigned `@EmbeddedId`) each get a
  pattern that is actually correct for that shape, not one pattern forced
  onto both.

**Non-Goals:**
- Changing the `version` column from primitive `int` to boxed `Integer`
  (see proposal.md — Out of Scope).
- Fixing every `CascadeType.ALL` collection found during the audit; only
  ones shown to interact with the delete path are addressed here (Decision
  3 / Riesgos).
- Touching `FoliosCopiasPK` or any other `@EmbeddedId` class's own
  `equals`/`hashCode` beyond what `Persistable` requires.

## Decisions

### Decision 1 — `Persistable<Integer>` + `isNew() = (id == null)` for the 25 surrogate-key entities
Applies to: `Concepto`, `Copia`, `Cuaderno`, `DocumentoPresentado`,
`Escritura`, `EstadoDeGestion`, `Folio`, `GestionDeEscritura`, `Inmueble`,
`MinutaInscripcion`, `MovimientoTestimonio`, `Persona`,
`RegistroAuditoria`, `Rol`, `Suplencia`, `Testimonio`, `TipoDeDocumento`,
`TipoDeFolio`, `TipoDeTramite`, `TipoIdentificacion`, `Usuario`,
`WorkflowDefinition`, `WorkflowNode`, `WorkflowTransition` (24) plus
`Presupuesto` (already partially covered by #957's `Pago` fix on its child;
the parent itself still needs it) — all declare `@Id @GeneratedValue(strategy
= GenerationType.IDENTITY) private Integer id...`.

This is the exact pattern already adopted for `Historial`/`Item`/`Pago`/
`Tramite`: the surrogate ID is `null` until the first `INSERT` assigns it,
so `id == null` is a correct and sufficient "is this new" signal.
**Alternative considered**: keep relying on `@Version`, but change the
column to boxed `Integer` so `null` (not `0`) means new — rejected per
proposal.md (touches the optimistic-locking column type across the schema
for no behavioral gain over the `Persistable` approach, which needs no
schema change).

### Decision 2 — `Persistable<XxxPK>` + `@PostLoad`/`@PostPersist` transient flag for the 5 `@EmbeddedId` entities
Applies to: `FoliosCopias`, `PlantillaCostoDocumento`,
`PlantillaPresupuesto`, `PlantillaTramite`, `TramitesPersonas`.

These entities build their `@EmbeddedId` (e.g. `FoliosCopiasPK`) from
already-known foreign keys at construction time — the ID is never `null`,
not even for a brand-new, unsaved instance. `id == null` is therefore
**not** a valid "is new" signal here, unlike Decision 1. The correct,
standard Spring Data pattern for a pre-assigned ID is a transient boolean
flag flipped by JPA lifecycle callbacks:

```java
@Transient
private boolean isNew = true;

@PostPersist
@PostLoad
void markNotNew() {
    this.isNew = false;
}

@Override
public boolean isNew() {
    return isNew;
}
```

**Alternative considered**: apply the same `id == null` check as Decision 1
— rejected because it is factually wrong for these entities (the ID is
always non-null) and would make `isNew()` always return `false`, breaking
`save()` for genuinely new composite-key rows (regression risk, not just a
missed fix). **Alternative considered**: query-before-delete (`existsById`
check in the service layer) — rejected because it treats the symptom at
every call site instead of fixing `isNew()` once at the entity level, and
does not fix `save()` for the same entities.

### Decision 3 — Test structure: one shared parameterized integration test per ID shape, not 30 near-identical classes
Two test classes, mirroring Decision 1 / Decision 2's grouping:
- `PersistableIdentityEntitiesDeleteIntegrationTest` — `@ParameterizedTest`
  over the 25 surrogate-key entities, each parameterized case supplying a
  `Supplier<Object>` (builds & saves a minimal valid instance) and the
  matching `JpaRepository`. Reuses the exact two-transaction shape of
  `HistorialDeleteIntegrationTest` (load in tx 1, delete in tx 2, assert
  absence in tx 3) instead of duplicating that method 25 times.
- `PersistableEmbeddedIdEntitiesDeleteIntegrationTest` — same two-
  transaction shape, parameterized over the 5 composite-key entities.

**Alternative considered**: one integration test class per entity (30
classes) — rejected: violates DRY/KIS (`.claude/rules/general.md` §11/§12),
the test body is identical for every case in a group, and 30 files add
review and maintenance cost with no coverage benefit over parameterization.
**Alternative considered**: a single reflective/generic test driving all 30
entities through one repository-agnostic helper — rejected: the two ID
shapes need genuinely different setup (constructing an `@EmbeddedId` needs
its parent FK rows saved first), so forcing them into one parameterized
source would need per-case conditional branching that is harder to read
than two small classes.

`isNew()` itself is also covered directly by fast unit tests (no DB) per
group, asserting the three scenarios in
`specs/data-integrity/persistable-delete/spec.md` without needing
Spring context.

## Riesgos / Trade-offs

- **[Risk]** `Concepto`'s
  `@OneToMany(cascade = CascadeType.ALL, mappedBy = "concepto", fetch =
  FetchType.EAGER)` and `Presupuesto.pagoList`
  (`@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)`) are the
  only two `EAGER` + `cascade = ALL` bidirectional collections found in the
  audit. `EAGER` cascade-`ALL` collections are the highest-risk shape for
  Hibernate re-inserting/reverting a delete, because the full collection is
  loaded and managed in the same persistence context as the parent at
  delete time. → **Mitigation**: both are covered by the "Deleting a parent
  with cascaded children removes both" scenario with an explicit
  integration test before this change is considered done; if either test
  reveals cascade-driven revert behavior beyond the `isNew()` bug itself,
  that is filed as a new, separate Issue per proposal.md's Out of Scope,
  not patched inline here.
- **[Risk]** The remaining `LAZY` cascade-`ALL` collections found in the
  audit (`Copia`, `Persona` ×6, `Usuario`, `WorkflowDefinition` ×2,
  `Testimonio` ×2, `Escritura`, `GestionDeEscritura`) are lower risk because
  Hibernate does not initialize them unless accessed, but a delete path that
  happens to touch the collection first (e.g. via a `@JsonIgnore` getter
  called during serialization earlier in the same transaction) could still
  reproduce the same class of bug. → **Mitigation**: not fixed proactively
  here (no evidence of an actual defect); flagged in `explore.md` as a
  follow-up audit item if a similar silent-delete report surfaces again.
- **[Risk]** 30 entities touched in one change is a large surface area for
  a single PR to review. → **Mitigation**: `tasks.md` sequences the work in
  small, independently-verifiable batches (by module/domain area) with a
  passing test suite after each batch, so review can proceed batch-by-batch
  even though it lands as one PR/one Issue.
- **[Risk]** The proposal's Impact Analysis said "~26 entities"; the audit
  found 30, 5 of which need a different pattern than the one the proposal
  described. → **Mitigation**: proposal.md's Surface Area section is
  corrected in the same PR as this design (documentation must stay
  accurate per `.claude/rules/general.md` §14); the two-pattern approach
  does not change the Issue, Use Case, or Acceptance Criteria — only the
  entity list and one implementation detail — so it does not reopen Gate 1.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|---|---|---|
| Deleting an existing entity removes the row (surrogate-key entities) | integration | `PersistableIdentityEntitiesDeleteIntegrationTest` (parameterized, 24 cases) |
| Deleting an existing entity removes the row (embedded-id entities) | integration | `PersistableEmbeddedIdEntitiesDeleteIntegrationTest` (parameterized, 5 cases) |
| `isNew()` reflects identity, not version (surrogate-key) | unit | `PersistableIdentityEntitiesIsNewTest` (parameterized) |
| `isNew()` reflects identity, not version (embedded-id) | unit | `PersistableEmbeddedIdEntitiesIsNewTest` (parameterized) |
| `isNew()` true only for a genuinely new, unsaved entity | unit | same two classes above (covered by the same parameterized cases, asserting both the "loaded" and "freshly constructed" branches) |
| Deleting a parent with cascaded children removes both | integration | `ConceptoDeleteCascadeIntegrationTest`, `PresupuestoDeleteCascadeIntegrationTest` (the two `EAGER`+`ALL` cases from Riesgos) |

- New unit tests (`src/test/java/.../unit/`): `PersistableIdentityEntitiesIsNewTest`, `PersistableEmbeddedIdEntitiesIsNewTest`.
- New integration tests (`src/test/java/.../integration/`): `PersistableIdentityEntitiesDeleteIntegrationTest`, `PersistableEmbeddedIdEntitiesDeleteIntegrationTest`, `ConceptoDeleteCascadeIntegrationTest`, `PresupuestoDeleteCascadeIntegrationTest`.
- Coverage impact: net positive — 30 entities gain both a unit and an
  integration assertion they did not have before; no production line loses
  coverage. Expected to keep the suite comfortably above the 70%
  line / 25% branch ratchet floor (currently ~84%/~74%).

## Regression Strategy

- Existing tests affected: none of the 30 entities currently have a
  passing test that asserts `isNew()` behavior, so no existing assertion is
  weakened or removed. `HistorialDeleteIntegrationTest` (the reference
  template) is left untouched.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh` (per this repo's
  actual script location — `backend-api/api-test/*.yml` collections),
  spot-checked for the entities that already have a delete `.yml` case
  (e.g. `folios`, `suplencias`) to confirm the previously-silent 200-without-
  delete no longer reproduces.
- Legacy paths at risk: the `jpa` package's `*JpaController` classes are
  not touched — `Persistable` only affects `JpaRepository`-based
  save/delete, which the legacy controllers do not use.

## Playwright Strategy

n/a — no UI surface. This is a backend persistence-layer correctness fix;
the affected `DELETE /api/v1/<resource>/{id}` endpoints' contracts and
frontend call sites are unchanged (a `DELETE` that returned `200 OK` now
also actually deletes, which is the endpoint's documented contract, not a
new behavior the UI needs to handle differently).

## Deployment Strategy

- Flyway migration required: no — no schema change.
- Deployment order / coupling: none; a plain code deploy.
- Configuration or `.env` keys to add: none.
- Feature flag: no — this is a correctness fix with no safe "old" behavior
  to flag-gate (the old behavior is the bug).
- Smoke test after deploy (Gate 5): for one representative entity per
  group (e.g. `Rol` for Decision 1, `TramitesPersonas` for Decision 2),
  create a row via the API, delete it, then confirm a subsequent `GET`
  returns `404`.

## Rollback Strategy

- Revert safe: yes — reverting restores the previous (buggy) `isNew()`
  inference; no new column, index, or data shape is introduced.
- Database rollback: none needed.
- Data written under the new behavior after revert: rows correctly deleted
  under the fix stay deleted after a revert (a revert does not undelete
  data); the only regression is that `DELETE` calls after the revert can
  again silently no-op, which is the pre-existing bug, not a new one.
- Blast radius if rollback is delayed: none beyond the bug already present
  on `main` today — delaying rollback of this fix cannot make the delete
  path worse than it already is.

## Migration Plan

Batches (see tasks.md for the exact task-level breakdown), each landing as
its own commit with its own green test run before moving to the next:
1. Catálogo/base tables with no incoming cascade risk: `Rol`,
   `TipoDeDocumento`, `TipoDeFolio`, `TipoDeTramite`, `TipoIdentificacion`,
   `EstadoDeGestion`, `WorkflowDefinition`, `WorkflowNode`,
   `WorkflowTransition`.
2. Core domain entities: `Persona`, `Usuario`, `Escritura`,
   `GestionDeEscritura`, `Presupuesto`, `Testimonio`, `Cuaderno`, `Folio`,
   `Inmueble`, `MinutaInscripcion`, `MovimientoTestimonio`,
   `DocumentoPresentado`, `RegistroAuditoria`, `Suplencia`, `Concepto`,
   `Copia`.
3. `@EmbeddedId` composite-key entities: `FoliosCopias`,
   `PlantillaCostoDocumento`, `PlantillaPresupuesto`, `PlantillaTramite`,
   `TramitesPersonas`.
4. Cascade-specific regression tests for the two `EAGER`+`ALL` risk cases
   (`Concepto`, `Presupuesto`).
5. Documentation: `proposal.md` surface-area correction, `COVERAGE.md`,
   `CHANGELOG.md`, `openspec/explore.md`.

## Open Questions

None — the ID-shape split (Decision 2) was the only open unknown from the
proposal stage and is resolved above.
