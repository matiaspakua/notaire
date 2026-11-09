> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Post-#2166 challenge: thin sole-committer dry-run, not Isolator.

## Goals / Non-Goals

**Goals:** Profile + CLI that flags agent-looking commits; required gates fail-closed.
**Non-Goals:** Sandbox, rewrite, forge API.

## Decisions

1. Section `[committer]` with `required`, `runtime_names`, `runtime_emails`,
   `agent_names`, `agent_emails`, `agent_trailers` (substring match on body).
2. No section / all empty lists → `no-evaluable`, exit 0.
3. Agent match OR (runtime lists non-empty AND author not in runtime) → flagged.
4. `required=false` → print fail but exit 0; `required=true` → exit 1.
5. Output must never contain the words "sandbox" or "isolated" as a claim.

## Riesgos / Trade-offs

False positives on human bots — tune allow/deny lists; required defaults false.

## Testing Strategy

Kit unittest with commits under spoofed author env; no-evaluable / pass / fail / required.

## Regression Strategy

Existing diffchecks untouched.

## Playwright Strategy

n/a

## Deployment Strategy

Kit + optional Notaire profile.

## Rollback Strategy

Revert PR.

## Migration Plan

None — opt-in via profile.

## Open Questions

None.
