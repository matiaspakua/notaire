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
| Specification | `docs/openspec/changes/chore-2052-aisdlc-diff-checks-evidence-chain/` | passed |
| Branch | `chore/2052_aisdlc-diff-checks-evidence-chain` | created |
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
| a policy file changed | test_diffchecks / test_evidence_chain | pending |
| an ordinary change | test_diffchecks / test_evidence_chain | pending |
| over the budget | test_diffchecks / test_evidence_chain | pending |
| excluded files do not count | test_diffchecks / test_evidence_chain | pending |
| a path outside the write set | test_diffchecks / test_evidence_chain | pending |
| no write set declared | test_diffchecks / test_evidence_chain | pending |
| an edited record | test_diffchecks / test_evidence_chain | pending |
| a truncated tail | test_diffchecks / test_evidence_chain | pending |
| an empty ledger | test_diffchecks / test_evidence_chain | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | no | pending |
| `framework/ai-sdlc/templates/project/CONSTITUTION.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2052, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
