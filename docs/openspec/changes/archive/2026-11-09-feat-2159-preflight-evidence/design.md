> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Factory validation showed report/HTML/improve need ledger signal. Preflight already
runs ratchet + skills without recording.

## Goals / Non-Goals

**Goals:** Thin wiring: same two checks, one `gate signal` stage record in local
preflight (`--full` path included).

**Non-Goals:** Kit STAGES expansion; CI evidence writes; portal; changing heavy gates.

## Decisions

1. Custom stage `signal` (not in canonical STAGES) — doctor may warn; acceptable.
2. Gate commands use `aisdlc ratchet --base {base}` and `aisdlc skills` so Context
   expand reuses the same kit binary/profile.
3. Replace the two preflight `run` lines with one `aisdlc gate signal` — no added
   wall-clock work beyond evidence append.
4. Do **not** add `signal` under `pre-push` (preflight is already that stage's
   command — recursion).

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Recursion if signal called from pre-push gate | signal only from shell preflight, not listed as pre-push |
| Custom stage warning on doctor | documented; no STAGES kit change this PR |

## Testing Strategy

- Update `WiringTest.test_preflight_runs_ratchet_skills_and_kit_tests` to require
  `gate signal`.
- Add profile assertion that `signal` stage declares ratchet + skills.
- Smoke: run `aisdlc gate signal` and assert ledger grows / report runs ≥ 1.

## Regression Strategy

Existing doctor/skills/ratchet adoption tests; CI process still calls ratchet/skills
directly.

## Playwright Strategy

n/a — no UI surface

## Deployment Strategy

Ships with the next release; evidence remains local/gitignored.

## Rollback Strategy

Revert the PR (restore separate ratchet/skills lines).

## Migration Plan

None — first successful local preflight starts the chain.

## Open Questions

None.
