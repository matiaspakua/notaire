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
| Issue | #<!-- number --> | <!-- open / in-progress / closed --> |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2298-repolens-factory-integration/` | passed |
| Branch | `feat/2297_repolens-code-knowledge-graph` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| setup registers every harness once | repolens factory tests (test_factory.py, test_repolens_integration.py) | pending |
| the committed wiring has not drifted | repolens factory tests (test_factory.py, test_repolens_integration.py) | pending |
| an agent's edit is visible to the next query | repolens factory tests (test_factory.py, test_repolens_integration.py) | pending |
| switching branches re-indexes only the difference | repolens factory tests (test_factory.py, test_repolens_integration.py) | pending |
| a new worktree reuses the shared vectors | repolens factory tests (test_factory.py, test_repolens_integration.py) | pending |
| a second indexer steps aside | repolens factory tests (test_factory.py, test_repolens_integration.py) | pending |
| the server builds on first start and logs each call | repolens factory tests (test_factory.py, test_repolens_integration.py) | pending |
| the report measures real sessions | repolens factory tests (test_factory.py, test_repolens_integration.py) | pending |
| stages, catalog and gate | repolens factory tests (test_factory.py, test_repolens_integration.py) | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md` | no | pending |
| `AGENTS.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2298, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
