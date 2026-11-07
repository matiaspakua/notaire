# Coverage mandate docs + JaCoCo ratchet + AI SDLC lessons (#2046)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2046 |
| Use Case | CU76 |
| Branch | `chore/2046_coverage-mandate-and-sdlc-lessons` |
| Gate 1 status | passed |

## Objetivo

Owner mandates **>90%** coverage across the test pyramid. Enforced floors today are lower. Document target vs floor, ratchet JaCoCo where headroom exists, and capture AI SDLC self-improvement lessons from recent tips.

## Why

P12 self-improve while delivering; quality cannot claim 90% while gates enforce 80%/65%. Measured JaCoCo ~86% line / ~71% branch allows a safe floor raise.

## What Changes

- Document Owner 90% **target** vs enforced **floors** in TEST-PLAN + Constitution coverage table
- Ratchet JaCoCo minimums to **85% line / 70% branch**
- Note #1246 stale; link #2046
- Append fleet lessons: worktree `.env`, wire `@ControllerAdvice` into standalone MockMvc, regen INVENTORY, PrefLight Skip after green `--full`, multi_pr Refs-not-Closes

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Coverage target 90%; floors ratchet raise-only toward it | Owner mandate; #2046; CU76 | New / Made explicit |

## Capabilities

### New Capabilities

- `coverage-mandate-90`: Owner target and ratchet policy

### Modified Capabilities

- `coverage`: JaCoCo floor scenario raised to 85% line / 70% branch
- `testing-process-architecture`: floors match tools; Owner >90% labeled separately

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | JaCoCo floors |
| `docs` | yes | TEST-PLAN, Constitution table, fleet lessons |

### Surface area

- Gate only (higher floor)

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/303-testing/TEST-PLAN.md` | Target 90% + current floors |
| `CONSTITUTION.md` | Coverage table / DoD wording |
| `FLEET-LESSONS-HEX-1693.md` | Agent friction lessons |
| `CHANGELOG.md` | Unreleased |

## Out of Scope

["Jumping all pyramid gates to 90% in this PR", "Closing #577"]
