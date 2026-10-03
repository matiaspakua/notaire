---
cursor:
  subagentId: "bc-0f44218f-11d4-5da9-a3a0-b4bf7760a383"
---

# OpenSpec draft — fix-1051-httponly-jwt-csp

**Status:** Gate 1 draft only (internal store).  
**Target path when implement starts:** `openspec/changes/fix-1051-httponly-jwt-csp/`  
**Do not** scaffold into the repo, push, or open a PR until the
`#1048 → #1047 → #1044` queue clears **and** no Playwright-heavy PR is in flight
(currently waiting on PR #1150 / #1057 CI, then that queue).

Artifacts in this folder mirror the `notaire-sdlc` layout (same relative
`CONSTITUTION.md` links as if under `openspec/changes/`).

| File | Role |
|------|------|
| `.openspec.yaml` | schema `notaire-sdlc` |
| `proposal.md` | Gate 1 proposal |
| `design.md` | design + test/deploy/rollback |
| `tasks.md` | 12 mandatory SDLC groups |
| `traceability.md` | Issue → Release ledger |
| `specs/httponly-jwt-cookie/spec.md` | cookie session AC scenarios |
| `specs/frontend-csp-hardening/spec.md` | CSP nonce / no `unsafe-eval` AC |

Kickoff: `internal/implement-1051-kickoff.md`  
Dispatch: `internal/dispatch-1051-ready.md`  
Issue: https://github.com/matiaspakua/notaire/issues/1051  
Branch (later): `cursor/fix-1051-httponly-jwt-csp-a383`  
Commits/PR: `Closes #1051`
