# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #957 | open |
| Use Case | Transversal — CU29, CU34, CU37, CU38, CU40, CU58 | exists |
| Specification | `openspec/changes/silent-delete-persistable-fix/` | proposal + design + tasks drafted |
| Branch | `fix/957_silent-delete-persistable-fix` | pending |
| Tasks | `tasks.md` | Sections 1–6, 8 complete; 7 n/a; 9–12 pending |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| DELETE de una entidad afectada, existente, en su propia transacción, borra la fila realmente | Test de integración de 2 transacciones por entidad afectada (plantilla `HistorialDeleteIntegrationTest`) | passed |
| `isNew()` de cada entidad afectada devuelve `false` para una fila leída de la base con id no nulo | Test unitario por entidad (o parametrizado) sobre `isNew()` (`PersistableIdentityEntitiesIsNewTest`, `PersistableEmbeddedIdEntitiesIsNewTest`) | passed |
| Ninguna colección `cascade = CascadeType.ALL` bidireccional revierte el delete de forma silenciosa | `ConceptoDeleteCascadeIntegrationTest`, `PresupuestoDeleteCascadeIntegrationTest` | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/api-test/COVERAGE.md` | yes | pending (uncommitted) |
| `CHANGELOG.md` | yes | pending (uncommitted) |
| `openspec/explore.md` | yes | pending (uncommitted) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | passed | Issue #957, `proposal.md`/`design.md`/`specs/`/`traceability.md` |
| 2 | Failing tests written, test cases designed | passed | `PersistableIdentityEntitiesIsNewTest`, `PersistableEmbeddedIdEntitiesIsNewTest`, delete integration tests (observed failing before Batches 1–3) |
| 3 | Suite green, coverage held, docs updated | passed | `mvn verify -pl backend-api` exit 0 (944 tests, 0 failures); Bruno suite green; COVERAGE.md/CHANGELOG.md/explore.md updated |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None.
