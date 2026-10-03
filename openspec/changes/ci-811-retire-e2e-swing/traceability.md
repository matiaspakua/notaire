# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step
> actually happens — never pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #811 | open (in-progress label may 403) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/ci-811-retire-e2e-swing/` | drafted |
| Branch | `cursor/ci-811-retire-e2e-swing-69d3` | local |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | TBD | pending |
| CI run | pending — `check-heavy-ci.sh` | pending |
| Merge commit | pending | pending |
| Release / tag | n/a — CI/docs hygiene | n/a |
| Smoke test | hygiene green + no e2e-swing.yml on main | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| e2e-swing workflow remains absent | `DependabotHygieneTest.test_e2e_swing_workflow_absent` | pending |
| workflows do not build Swing modules | `DependabotHygieneTest.test_workflows_do_not_build_swing_modules` | pending |
| synthetic fixture fails hygiene | same tests with temp workflow | pending |
| tip of branch passes hygiene | run on repo root | pending |
| e2e-swing suite hard-deprecated | README + run script early exit | pending |
| OpenSpec Gate 1 complete | `validate-sdlc-plan.sh` | pending |
| PR commits include `Closes #811` | Commit message inspection | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | pending | pending |
| `docs/300-development/303-testing/README.md` | pending | pending |
| `docs/300-development/301-setup/README.md` | pending | pending |
| `docs/300-development/DEVELOPMENT-PLAN.md` | pending | pending |
| `testing/e2e-swing/README.md` | pending | pending |
| `docs/200-architecture/202-ADR/ADR-012-ci-cd-pipeline.md` | pending | pending |
| `CHANGELOG.md` | pending | pending |
| `openspec/changes/ci-811-retire-e2e-swing/` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | Issue #811; this change folder |
| 2 | Failing tests written, test cases designed | pending | hygiene synthetic fixture |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | `check-heavy-ci.sh` |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- **Branch naming**: Cloud Agent requires `cursor/ci-811-retire-e2e-swing-69d3`;
  Constitution form via Issue #811.
- **in-progress label**: `gh` may 403 for label edits.
- **Playwright**: n/a — no UI surface.
- **local-ai/**: must not be used (cloud agent VM only).
