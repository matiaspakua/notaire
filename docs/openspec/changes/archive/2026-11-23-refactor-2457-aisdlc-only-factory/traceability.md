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
| Issue | #2457 (epic #2456) | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/refactor-2457-aisdlc-only-factory/` | passed |
| Branch | `refactor/2457_remove_factory_control_agentic_os` | created |
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
| If-Match and schema fields appear in OpenAPI | `framework/ai-sdlc/tests/test_contract.py` (contract check on the renamed file) | pending |
| OpenAPI Project Run RunRequest schemas present | `framework/ai-sdlc/tests/test_contract.py` | pending |
| OpenAPI declares config file paths | `framework/ai-sdlc/tests/test_contract.py` | pending |
| OpenAPI declares catalog delete path | `framework/ai-sdlc/tests/test_contract.py` | pending |
| no second OpenAPI tree | `framework/ai-sdlc/tests/test_contract.py` | pending |
| aisdlc contract resolves the renamed file | `test_contract.py::test_contract_lives_under_the_kit_name` | pending |
| the wheel ships the renamed contract | `framework/ai-sdlc/tests/test_packaging.py` | pending |
| the app name in both locales | `factory-dashboard/tests/brand.test.ts` | pending |
| (guard) no tracked file names an external factory | `workspace/tests/test_no_external_factory.py` | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | no | pending |
| `framework/ai-sdlc/factory-dashboard/README.md` | no | pending |
| `framework/ai-sdlc/factory-dashboard/DESIGN.md` | no | pending |
| `framework/ai-sdlc/factory-dashboard/MODULE.md` | no | pending |
| `framework/ai-sdlc/docs/adr/ADR-003-standards.md` | no | pending |
| `framework/ai-sdlc/docs/adr/ADR-006-dashboard-contract-client.md` | no | pending |
| `framework/ai-sdlc/CHANGELOG.md` | no | pending |
| `README.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-PRODUCT-ANALYSIS.md` | no | pending |
| `docs/000-archive/` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2457, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
