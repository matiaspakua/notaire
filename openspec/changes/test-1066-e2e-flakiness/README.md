---
cursor:
  subagentId: "bc-0e8fe2cf-2b59-571d-9a68-c71b0555980d"
---

# OpenSpec draft — test-1066-e2e-flakiness

**Status:** Gate 1 draft only (internal store).  
**Target path when implement starts:** `openspec/changes/test-1066-e2e-flakiness/`  
**Do not** scaffold into the repo, push, or open a PR until PR #1145 (#1054) merges.

Artifacts in this folder mirror the `notaire-sdlc` layout (same relative `CONSTITUTION.md` links as if under `openspec/changes/`).

| File | Role |
|------|------|
| `.openspec.yaml` | schema `notaire-sdlc` |
| `proposal.md` | Gate 1 proposal |
| `design.md` | design + test/deploy/rollback |
| `tasks.md` | 12 mandatory SDLC groups |
| `traceability.md` | Issue → Release ledger |
| `specs/e2e-test-reliability/spec.md` | delta AC scenarios |

Prep context: `internal/next-issue-after-1054.md`. Fleet implements only after #1145 merges.
