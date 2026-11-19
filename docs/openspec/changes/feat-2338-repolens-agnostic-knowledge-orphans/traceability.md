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
| Specification | `docs/openspec/changes/feat-2338-repolens-agnostic-knowledge-orphans/` | passed |
| Branch | `feat/2338_repolens-agnostic-orphans` | created |
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
| a language without a hand-written spec | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |
| a grammar without a tags query | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |
| a format with no definitions | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |
| paths in any form | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |
| an ambiguous bare name | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |
| identifiers in configuration | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |
| an isolated file | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |
| related only through history | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |
| every interface | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |
| the index run alerts | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |
| measured | repolens tests (test_agnostic, test_orphans, test_interfaces) | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/repolens/README.md` | no | pending |
| `framework/ai-sdlc/repolens/CHANGELOG.md` | no | pending |
| `framework/ai-sdlc/repolens/bench/results/factory-kpis.json` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2338, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
