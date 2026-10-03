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
| Issue | #1067 | open (Gate 1 draft only; implement after queue ahead) |
| Use Case | CU76 – QA; CU78 – Security; CU75 – Database Management | exists |
| Related | #256 (backups — OPEN, Phase C gate); #281 (ZAP guide — OPEN); ADR-006 | referenced |
| Specification | `openspec/changes/test-1067-dast-contract-backup/` (draft: `internal/openspec-1067/`) | Gate 1 draft ready |
| Branch | `cursor/test-1067-dast-contract-backup-69d3` | pending (do not create/push yet) |
| Tasks | `tasks.md` | Gate 1 planning complete; implement pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| ZAP baseline job executes successfully | CI DAST workflow + artifact | pending |
| Trivy SCA is not removed | static/CI inspect `ci.yml` | pending |
| OpenAPI artifact is present in the repo | committed file + export script | pending |
| PR OpenAPI diff job runs | CI openapi-diff job | pending |
| Restore smoke runs after #256 exists | backup-restore workflow | pending |
| Restore smoke does not false-green without #256 | workflow skip assertion | pending |
| Docs describe landed gates | DevSecOps + TEST-PLAN review | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/208-devsecops/README.md` | pending | — |
| `docs/300-development/303-testing/TEST-PLAN.md` | pending | — |
| `docs/200-architecture/209-deployment/README.md` | pending | — |
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

None for Gate 1. Phase C execution depends on #256 (OPEN) — design requires
explicit skip (no false green) until backups exist. Gate 1 stockpile only —
no implementation/PR/push.
