---
cursor:
  subagentId: "bc-d5786561-8eb2-5bb0-b579-deb2851750a7"
---

# OpenSpec draft — feat-1044-prod-compose

**Status:** Gate 1 draft only (internal store).  
**Target path when implement starts:** `openspec/changes/feat-1044-prod-compose/`  
**Do not** scaffold into the repo, push, or open a PR until **#1047** merges
(queue: #1057 PR #1150 → #1048 → #1047 → then #1044).

Artifacts in this folder mirror the `notaire-sdlc` layout (same relative
`CONSTITUTION.md` links as if under `openspec/changes/`).

| File | Role |
|------|------|
| `.openspec.yaml` | schema `notaire-sdlc` |
| `proposal.md` | Gate 1 proposal |
| `design.md` | design + test/deploy/rollback |
| `tasks.md` | 12 mandatory SDLC groups |
| `traceability.md` | Issue → Release ledger |
| `specs/prod-docker-compose/spec.md` | delta AC scenarios |

Kickoff: `internal/implement-1044-kickoff.md`  
Issue: https://github.com/matiaspakua/notaire/issues/1044  
Use Cases: **CU78** + **CU75**  
Branch (later): `cursor/feat-1044-prod-compose-69d3`  
Commits/PR: `Closes #1044`
