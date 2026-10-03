# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1121 (Related only — cloud fleet process RNF; this chore does not close it) | open |
| Use Case | RNF / process: Engineering Constitution AI agent SDLC — Cursor Cloud fleet tooling | exists |
| Specification | `openspec/changes/chore-bruno-cli-install-pin/` | Gate 1 writing |
| Branch | `cursor/chore-bruno-cli-install-pin-69d3` | created |
| Tasks | `tasks.md` | in progress |
| Commits | 8999bee2 | done |
| Pull Request | pending after ManagePullRequest | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| install.sh pins @usebruno/cli@4.2.0 | static review / grep | pending |
| bru symlink block present after OpenSpec | static review / grep | pending |
| checklist lists Bruno CLI ≥4.2.0 | static review | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1121 OPEN + this folder + scenarios |
| 2 | Failing tests written, test cases designed | yes | static presence scenarios (no prod code) |
| 3 | Suite green, coverage held, docs updated | pending | checklist + validate-sdlc-plan |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | does not close #1121; env rebuild after merge |

## Exceptions

None. Process Checks satisfied by this `openspec/changes/` folder rather than
`sdlc-exception`. Issue linkage is `Related: #1121` (not `Closes #1121`).
