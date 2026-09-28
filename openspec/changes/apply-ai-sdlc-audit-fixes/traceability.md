# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step
> actually happens.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1083 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/apply-ai-sdlc-audit-fixes/` | written |
| Branch | `chore/1083_apply_ai_sdlc_audit_fixes` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Harness env files accept quoted values (3 scenarios) | `local-ai/sdlc/tests/test_envfile.py` | pending |
| Static review gates on test changes (4 scenarios) | `local-ai/sdlc/tests/test_static_checks.py` | pending |
| Foreman review notes are machine-checked (3 scenarios) | `local-ai/sdlc/tests/test_review_check.py` | pending |
| Per-gate metrics (1 scenario) | `local-ai/sdlc/tests/test_metrics.py` | pending |
| PR commits are conventional (2 scenarios) | `scripts/tests/test_pr_checks.py` | pending |
| TDD evidence on production-code PRs (3 scenarios) | `scripts/tests/test_pr_checks.py` | pending |
| SDLC exceptions are labelled (2 scenarios) | `scripts/tests/test_pr_checks.py` | pending |
| Agent rule files stay valid (2 scenarios) | `scripts/tests/test_pr_checks.py` | pending |
| Changes must declare their schema (1 scenario) | `scripts/tests/test_pr_checks.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CONSTITUTION.md` | pending | pending |
| `.claude/rules/ai-agent-workflow.md` | pending | pending |
| `docs/300-development/CI-PREFLIGHT.md` | pending | pending |
| `local-ai/sdlc/AI-SDLC.md` | pending | pending |
| `local-ai/AUDIT.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1083, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only process scripts, CI
  YAML and docs. The tests are Python `unittest` suites for the scripts.
