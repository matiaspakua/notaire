# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```unknown
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1191 (phase 1 of #1190; phase 2 #1192) | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure (CU75 for the database suite) | exists (add #1191 to both ID tables) |
| Related | #1179 (same pattern for infra/), #1185 / PR #1193 (merged; pipeline green), #1186 (lesson: no host-port collisions) | referenced |
| Specification | `openspec/changes/refactor-1191-testing-standalone-qa/` | Gate 1 approved by Owner (k6 stays in infra/) |
| Branch | `refactor/1191_testing_standalone_qa` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Suites live under testing/ | `scripts/test_testing_standalone.py` | pending |
| Dead scripts and stale reports are gone | same | pending |
| Runner lists suites and rejects unknown ones | same | pending |
| test.sh still runs the integration suite | same | pending |
| Migrations apply to an empty database | `testing/database/checks` via `run.sh database` | pending |
| A second migrate is a no-op | same | pending |
| Only the documented rollback script is ignored | same | pending |
| An edited migration is detected | same | pending |
| Server configuration is as required | same | pending |
| The exporter role is least-privilege | same | pending |
| Seed data is present | same | pending |
| Core schema objects exist | same | pending |
| The suite uses pinned images and publishes no host port | `scripts/test_testing_standalone.py` | pending |
| testing/ does not reference paths outside itself | same | pending |
| Environment example is complete and secret-free | same | pending |
| Documentation set exists and docs link to it | same | pending |
| CI and preflight carry the database gate | same | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `testing/README.md`, `testing/docs/*` | pending | — |
| `303-testing` README and TEST-PLAN, CI-PREFLIGHT, TEST-COVERAGE-STRATEGY | pending | — |
| `CLAUDE.md`, `AGENTS.md` | pending | — |
| CU76, CU75 | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh refactor-1191-testing-standalone-qa` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
