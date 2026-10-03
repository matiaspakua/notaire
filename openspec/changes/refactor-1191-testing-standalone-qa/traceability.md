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
| Tasks | `tasks.md` | implementation and docs complete; pipeline, PR and Gates 4-5 pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Suites live under testing/ | `scripts/test_testing_standalone.py` | covered |
| Dead scripts and stale reports are gone | same | covered |
| Runner lists suites and rejects unknown ones | same | covered |
| test.sh still runs the integration suite | same | covered |
| Migrations apply to an empty database | `testing/database/checks` via `run.sh database` | covered |
| A second migrate is a no-op | same | covered |
| Only the documented rollback script is ignored | same | covered |
| An edited migration is detected | same | covered |
| Server configuration is as required | same | covered |
| The exporter role is least-privilege | same | covered |
| Seed data is present | same | covered |
| Core schema objects exist | same | covered |
| The suite uses pinned images and publishes no host port | `scripts/test_testing_standalone.py` | covered |
| testing/ does not reference paths outside itself | same | covered |
| Environment example is complete and secret-free | same | covered |
| Documentation set exists and docs link to it | same | covered |
| CI and preflight carry the database gate | same | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `testing/README.md`, `testing/docs/*` | yes | docs commit |
| `303-testing` README and api-test README, CI-PREFLIGHT | yes | docs commit |
| TEST-PLAN, TEST-COVERAGE-STRATEGY | reviewed, no stale path (its script path is kept) | n/a |
| `CLAUDE.md`, `AGENTS.md` | yes | docs commit |
| CU76, CU75 | yes | docs commit |
| `CHANGELOG.md` | yes | docs commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh refactor-1191-testing-standalone-qa` |
| 2 | Failing tests written, test cases designed | yes | `b27c8875`: guard 16 of 18 failed, image-pin test failed, `run.sh` did not exist, before any production file changed |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
