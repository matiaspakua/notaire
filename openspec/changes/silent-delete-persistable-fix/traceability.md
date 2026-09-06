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
| Tasks | `tasks.md` | 2/~50 complete (1.1, 1.2) |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| DELETE de una entidad afectada, existente, en su propia transacción, borra la fila realmente | Test de integración de 2 transacciones por entidad afectada (plantilla `HistorialDeleteIntegrationTest`) | pending |
| `isNew()` de cada entidad afectada devuelve `false` para una fila leída de la base con id no nulo | Test unitario por entidad (o parametrizado) sobre `isNew()` | pending |
| Ninguna colección `cascade = CascadeType.ALL` bidireccional revierte el delete de forma silenciosa | Auditoría documentada en `design.md`; test de integración para cualquier caso confirmado de riesgo | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/api-test/COVERAGE.md` | no | pending |
| `CHANGELOG.md` | no | pending |
| `openspec/explore.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None.
