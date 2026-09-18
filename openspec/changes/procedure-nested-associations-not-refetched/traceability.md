# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #981 | open |
| Use Case | CU82 – Generar y hacer seguimiento de la minuta de inscripción | exists |
| Specification | `proposal.md` + `specs/procedure-nested-association-integrity/spec.md` + `design.md` | done |
| Branch | `fix/981_procedure-nested-associations-not-refetched` | created |
| Tasks | `tasks.md` | pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Acceptance criterion (issue #981) | Test / evidence | Status |
|---|---|---|
| `POST /api/v1/tramites` with a nested `fkIdDeed`/`fkIdProperty`/`fkIdProcedureType` reference re-fetches those associations from the DB | `ProcedureNestedAssociationIntegrationTest` | pending |
| `GET /api/v1/tramites/{id}` after creation reflects the real, previously-persisted data | `ProcedureNestedAssociationIntegrationTest` | pending |
| `TS-0082-minuta-inscripcion-feature.spec.ts` golden path + 2 edge cases pass consistently | out of scope for this change (backend fix only); to be re-verified separately per proposal.md "Out of Scope" | pending |

## Permanent documentation updated

- None required — see `proposal.md` "Documentation Impact" (no documented
  behavior changes; this fixes a bug against an already-implicit contract).

## Gate log

| Gate | Status | Evidence |
|------|--------|----------|
| Gate 1 — Prerequisites | pending | `tasks.md` §1 |
| Gate 2 — TDD (failing tests first) | pending | `tasks.md` §3 |
| Gate 3 — Documentation | pending | `tasks.md` §8 |
| Gate 4 — CI / PR mergeable | pending | `tasks.md` §10 |
| Gate 5 — Smoke test & close | pending | `tasks.md` §12 |

## Exceptions

- None.

## Notes

- Merged by code owner directly via PR; no separate formal GitHub review recorded (Constitución §5 paso 20, repositorio solo-mantenedor).
