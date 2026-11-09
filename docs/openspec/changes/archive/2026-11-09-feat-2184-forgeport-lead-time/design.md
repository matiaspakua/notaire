> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2134 closed. #2138 shipped fixture lead-time. Gap audit ASLI-46 owns ForgePort read-only as next slice after #2182 stage knobs merged.

## Goals / Non-Goals

**Goals:** - Live `metrics lead-time --issue N` via read-only ForgePort (`gh`)
- Preserve fixture path and event pair
- Prove on a real Notaire issue→PR pair
**Non-Goals:** - Forge write / check attestation / factory/gate
- Isolator / required=true / portal
- Changing EVENT_PAIR

## Decisions

1. **Reuse sample shape** — ForgePort returns the #2138 JSON sample; `lead_time_from_sample` stays the sole calculator.
2. **Injectable runner** — unit tests never call real `gh`; default runner is subprocess `gh`.
3. **checks_green_at** — from PR `statusCheckRollup`: all completed with success/skip/neutral → max(completedAt); empty checks + merged → mergedAt (Notaire #2001 missing=success); else omit (no-evaluable).
4. **Linked PRs** — GraphQL/timeline cross-refs plus PRs whose closingIssuesReferences include the issue; title `Closes #N` is not required when closing refs exist.
5. **CLI** — `--issue` XOR `--fixture`; no forge write flags.

## Riesgos / Trade-offs

- [Historical check expiry] → empty rollup + merged uses mergedAt; document honesty.
- [gh missing] → exit 2 with clear error; no fake pass.
- [Write creep] → ForgePort module documents read-only; tests assert argv verbs.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| sample from forge runner | unit | test_forge_port.py |
| read-only argv verbs | unit | test_forge_port.py |
| live CLI green | unit | test_forge_port.py / test_metrics.py |
| live CLI no-evaluable | unit | test_forge_port.py |
| fixture path unchanged | unit | test_metrics.py |

- New: `framework/ai-sdlc/tests/test_forge_port.py`
- Coverage: kit suite only

## Regression Strategy

- Existing fixture lead-time tests must stay green
- Full kit: `python3 -m unittest discover -s framework/ai-sdlc/tests`
- Notaire: live `--issue 2182` (or known closed pair)
- HTTP/Bruno/Playwright: n/a

## Playwright Strategy

n/a — no UI surface

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
