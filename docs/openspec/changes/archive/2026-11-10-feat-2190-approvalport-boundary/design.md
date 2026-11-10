> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Gap audit rank 5; ranks 1–4 shipped (#2182–#2189). Isolator design shipped (#2173 / ADR-004).
Wilson autonomy on Notaire: `status=no-evaluable`, `trials=0`, empty `stages` — no Owner-chosen
multi-stage evidence volume for L1+.

## Goals / Non-Goals

**Goals:** Honest ApprovalPort boundary design + unevaluable stub; refuse Wilson auto-promotion
in this PR; document why Wilson waits.
**Non-Goals:** Implementing SSH/DSSE mint/verify, inventing vanity stages, Isolator OS, ForgePort
write, portal, flipping `required=true`.

## Decisions

ADR-005 + ApprovalPort stub `status()`; doctor INFO line; authority = token + channel; agent never
self-approves; stub never mints or accepts fake approvals. Wilson promotion deferred until
operational multi-stage volume exists (honesty already in #2171).

## Riesgos / Trade-offs

Readers confuse stub with live approval — mitigated by `approvals_live=false`,
`agent_can_approve=false`, and ADR language parallel to Isolator.

## Testing Strategy

ApprovalPortTest + doctor output assertion (TDD red→green).

## Regression Strategy

framework/ai-sdlc doctor/isolator tests remain green.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
