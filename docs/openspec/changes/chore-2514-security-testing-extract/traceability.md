# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2514 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-2514-security-testing-extract/` | passed |
| Branch | `chore/2514_security_testing_extract` | created |
| Rollback tags | `pre-extract-security`, `pre-extract-testing` | created |
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
| gitmodules points at notaire-security | `workspace.tests.test_security_submodule` | pending |
| module verify still runs from security path | `security/verify.sh` + submodule test | pending |
| threat-model docs remain in product | `workspace.tests.test_security_docs_seam` | pending |
| program docs moved or stubbed to security repo | `workspace.tests.test_security_docs_seam` | pending |
| e2e/database workflows pin notaire-testing | `workspace.tests.test_testing_workflow_pin` | pending |
| local testing tree is stub-only | `workspace.tests.test_testing_workflow_pin` | pending |
| NOTAIRE_APP_ROOT / NOTAIRE_TESTING_ROOT resolve | `workspace.tests.test_testing_roots_seam` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `README.md` | no | pending |
| `AGENTS.md` | no | pending |
| `docs/300-development/REPO-SPLIT-PLAN.md` | no | pending |
| `docs/300-development/MULTI-TEAM-CONTRACT.md` | no | pending |
| `docs/200-architecture/206-security/README.md` | no | pending |
| GitHub Pages modules/architecture | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2514, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
