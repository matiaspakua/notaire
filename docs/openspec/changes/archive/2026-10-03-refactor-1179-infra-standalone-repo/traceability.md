# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```unknown
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1179 (https://github.com/matiaspakua/notaire/issues/1179) | open → in progress |
| Use Case | CU77 – Operations Monitoring and Incident Management | exists (add #1179 to GitHub ID table at implement) |
| Related | #302 IaC documentation (CU77); #1044 prod compose; #901 kustomize; #1047 k6 | referenced |
| Specification | `openspec/changes/refactor-1179-infra-standalone-repo/` | Gate 1 approved by Owner |
| Branch | `refactor/1179_infra_standalone_repo` | created from updated `main` |
| Tasks | `tasks.md` | complete except items annotated NOT MET |
| Commits | squashed into `faa500a5`; branch `refactor/1179_infra_standalone_repo` | merged |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1184 | merged |
| CI run | PR #1184 checks: all required jobs passed (37 pass, 3 skipping) | green |
| Merge commit | `faa500a5` (2026-10-03) | merged |
| Release / tag | none; `cd.yml` ran on the merge commit | n/a |
| Smoke test | start-all.sh then check-infra.sh 15/15 passed on merged main; all 5 Prometheus targets up | passed |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| All infra assets live under infra/ | `scripts/test_infra_standalone.py` | covered |
| Legacy locations no longer exist | same | covered |
| nginx.conf has a single source | `scripts/test_infra_standalone.py`, `scripts/test_staging_kustomize.py` | covered |
| infra/ does not reference paths outside itself | same | covered |
| infra/ ships its own env example without secrets | same | covered |
| Stale E2E suite removed | same | covered |
| Infra documentation set exists and is linked from docs/ | same | covered |
| Consumers point to the new paths | same + existing `scripts/test_*.py` | covered |
| Stack still starts and manifests still validate | `scripts/test_staging_kustomize.py`, `test_prod_compose.py`, `docker compose config` | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `infra/README.md`, `infra/docs/*` | yes | docs commit |
| `README.md`, `CLAUDE.md`, `AGENTS.md` | yes | docs commit |
| SAD, ADR-009/016/017/019, 207, 209, DEPLOYMENT-PLAN, puml | yes | docs commit |
| CU77 | yes | docs commit |
| `CHANGELOG.md` | yes | docs commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh refactor-1179-infra-standalone-repo` |
| 2 | Failing tests written, test cases designed | yes | `96fe9399`: new guard 10 failures + 3 errors, 4 repointed guards red, before any move |
| 3 | Suite green, coverage held, docs updated | partial | guards and suites green; `run_pipeline.sh` NOT green: three failures predating the change (#1185); pushed with PREFLIGHT_SKIP; the pipeline failures that predate the change are tracked in #1185 |
| 4 | CI green, review approved, no conflicts | yes | required CI green; merged by the code owner directly, no formal GitHub review recorded (Constitution step 20) |
| 5 | Deployed, smoke test passed, Issue closed | yes | `cd.yml` green on `faa500a5`; start-all.sh then check-infra.sh 15/15 passed on merged main; all 5 Prometheus targets up; issue #1179 closed with evidence |

## Exceptions

None.
