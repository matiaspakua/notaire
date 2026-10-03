# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #953 | in progress (implement) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | #952 (Bruno audit — closed); #1035 (idempotent suite) | referenced |
| Specification | `openspec/changes/test-953-bruno-zero-coverage/` (draft: `internal/openspec-953/`) | Gate 1 draft ready |
| Branch | `cursor/test-953-bruno-zero-coverage-69d3` | created |
| Tasks | `tasks.md` | Gate 1 planning complete; implement pending |
| Commits | `cfc76da6` (+ `964bafe9`) | pushed |
| Pull Request | #1173 | draft open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Sixteen folders exist for previously uncovered controllers | Bruno folders under `api-test/` | pending |
| New requests assert status (and key body fields) | chai blocks in each YAML | pending |
| Full Bruno run passes with new folders | `bru run . -r --env Development` | pending |
| Second consecutive Bruno run stays green | double `bru run` | pending |
| COVERAGE TODO no longer lists the sixteen | `COVERAGE.md` review | pending |
| Matrix and TEST-PLAN reflect Bruno status | `CU-API-MATRIX.csv` + TEST-PLAN §7 | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/api-test/COVERAGE.md` | pending | — |
| `docs/300-development/303-testing/TEST-PLAN.md` | pending | — |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes (draft) | artifacts in this folder |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Gate 1 stockpile only — no implementation/PR/push. Prefer implementing
after Playwright-heavy queue items to free runners for Bruno CI.
