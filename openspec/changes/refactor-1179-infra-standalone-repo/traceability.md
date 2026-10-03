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
| Tasks | `tasks.md` | implementation complete; pipeline, PR and Gates 4-5 pending |
| Commits | `4ff9f4d4`, `f951f984` (spec), `96fe9399` (red tests), `7a128aa2`, `250de798`, `36df4301` (moves), `98dbad29`, `16f15bf9`, `c9724848` (content) + docs commit | pushed: no |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

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
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
