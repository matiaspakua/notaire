# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1546 | open → in progress |
| Use Case | CU59 – Consultar Suplencias; CU22 | exists |
| Specification | `docs/openspec/changes/feat-1546-substitutions-filter/` | Gate 1 draft |
| Branch | `feat/1546_substitutions_filter` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Overlap | Vitest, TS-0017 GW01 | passing |
| Notary | Vitest | passing |
| Empty | Vitest, TS-0017 | passing |
| Detail | TS-0017 GW02 | passing |
| Mobile | TS-0017 GW03 | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes | branch commit |
| `docs/openspec/specs/e2e-feature-gap-skip-tracking/spec.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1546-substitutions-filter` |
| 2 | Failing tests written, test cases designed | yes | the Vitest file failed before the change (module missing) |
| 3 | Suite green, coverage held, docs updated | yes | frontend verify.sh / quick.sh (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
