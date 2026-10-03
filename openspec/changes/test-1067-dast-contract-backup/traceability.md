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
| Issue | #1067 | open (implement in progress; label may need human — agent `gh` 403 on labels) |
| Use Case | CU76 – QA; CU78 – Security; CU75 – Database Management | exists |
| Related | #256 (backups — OPEN, Phase C gate); #281 (ZAP guide — OPEN); ADR-006 | referenced |
| Specification | `openspec/changes/test-1067-dast-contract-backup/` | Gate 1 complete |
| Branch | `cursor/test-1067-dast-contract-backup-69d3` | pushed |
| Tasks | `tasks.md` | A/B + C-skip implemented; merge pending |
| Commits | `f971c760` (red guards); implement commit(s) on branch | in progress |
| Pull Request | [#1174](https://github.com/matiaspakua/notaire/pull/1174) | draft — keep until heavy green |
| CI run | Process Checks / OpenAPI Contract / heavy suite | pending green |
| Merge commit | — | pending (coordinator owns heavy-CI merge) |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| ZAP baseline job executes successfully | `.github/workflows/dast-zap.yml` + artifact; guard suite | implemented (warn-first) |
| Trivy SCA is not removed | `TrivyRetainedTest` + `ci.yml` | green |
| OpenAPI artifact is present in the repo | `backend-api/openapi/openapi.yaml` + export script | green |
| PR OpenAPI diff job runs | `openapi-contract.yml` (`oasdiff` ERR) | implemented |
| Restore smoke runs after #256 exists | `backup-restore-smoke.yml` enable path | gated (tooling absent) |
| Restore smoke does not false-green without #256 | explicit skip / blocked-on-#256 | green |
| Docs describe landed gates | DevSecOps + TEST-PLAN + deployment | updated |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/208-devsecops/README.md` | yes | on branch |
| `docs/300-development/303-testing/TEST-PLAN.md` | yes | on branch |
| `docs/200-architecture/209-deployment/README.md` | yes | on branch |
| `CHANGELOG.md` | yes | on branch |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | openspec change + validate-sdlc-plan |
| 2 | Failing tests written, test cases designed | yes | red commit `f971c760`; then green asset suite |
| 3 | Suite green, coverage held, docs updated | pending | preflight / CI |
| 4 | CI green, review approved, no conflicts | pending | PR #1174 |
| 5 | Deployed, smoke test passed, Issue closed | pending | coordinator merge |

## Exceptions

Phase C execution depends on #256 (OPEN) — workflow skips with explicit
blocked-on-#256 signal until `scripts/backup-postgres.sh` exists. No parallel
backup product invented in this change.
