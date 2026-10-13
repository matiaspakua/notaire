# Portable opencode.json, unpushed-work warning, push verification

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1647 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/1647_harness-hygiene` |
| Gate 1 status | passed |

## Objetivo

Three harness defects surfaced on 2026-10-13. `opencode.json` only works on the
Owner's Mac and loads every skill. #1620's finished fix sat unpushed and
invisible. A pre-push rejection was reported as `ok`.

## What Changes

- `opencode.json`: the filesystem MCP root is `.`; instructions are `AGENTS.md` + `CONSTITUTION-AGENT-CARD.md`.
- SessionStart hook: lists never-pushed branches (no upstream, commits on no remote).
- `ai-agent-workflow.md`: verify each push with `git ls-remote`. `hooks.md` documents the hook.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Always-loaded context stays within the budget for every runtime | #1259 | Made explicit for OpenCode |
| Finished work is pushed, and the push is verified | #1647 | New |

## Capabilities

### New Capabilities

### Modified Capabilities
- `ai-sdlc-enforcement`: three requirements above.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| Agent harness | yes | `opencode.json`, SessionStart hook, workflow rule |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: `opencode.json`
- Dependencies: none

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `.claude/rules/ai-agent-workflow.md` | Push verification |
| `.claude/rules/hooks.md` | Hook lists unpushed branches |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- RTK configuration on the Owner's machine (user-level, not in the repo).
