# Cut always-loaded agent context to ≤8k tokens

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1259 |
| Use Case | CU76 |
| Branch | `cursor/chore-1259-agent-context-cf98` |
| Gate 1 status | complete |
| Parent | #1197 Phase 0.4 |

## Objetivo

Always-loaded agent context is ~26–30k tokens (CONSTITUTION alone ~8.8k; AGENTS +
alwaysApply rules/skills add the rest). #1197 P0.4 requires ≤8k without losing Gate
enforcement. Ship a Constitution agent card + slim AGENTS on-demand table; turn off
large `alwaysApply` skills/rules; measure with `agent-context-budget.py --max-tokens 8000`.

## What Changes

- Add `CONSTITUTION-AGENT-CARD.md` (≤3 KB digest); full CONSTITUTION on demand.
- Replace bulk `@` imports in `AGENTS.md` with path→skill table.
- Set `alwaysApply: false` on frontend-design skill and large always-applied rules.
- Add `workspace/ci/agent-context-budget.py` (+ unit test); refresh metrics baseline.
- Note packing in `docs/300-development/304-ai-sdlc-cloud/`.

## Reglas de negocio

| Rule | Source | Change |
|------|--------|--------|
| Always-loaded ≤8k tokens | #1259 / #1197 P0.4 | Made explicit |
| CONSTITUTION remains highest authority | CONSTITUTION | Unchanged (card is digest only) |
| Gate 1/2/3 still enforced | AI SDLC | Unchanged |

## Capabilities

### New Capabilities

- `agent-context-budget`: measurable always-loaded token budget with CI/local guard.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `docs` / agent packing | yes | AGENTS, Constitution card, cloud packing note, baseline |
| `workspace` | yes | `agent-context-budget.py` + unit test; `repo-metrics.py` ALWAYS_LOADED |
| `.claude` | yes | `alwaysApply: false` on large rules/skills |

### Surface area

- Entities / Endpoints / Flyway / Configuration / Dependencies: none
- **BREAKING** for API clients: no

### Architecture review

Agent packing only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `AGENTS.md` | Slim + on-demand table |
| `CONSTITUTION-AGENT-CARD.md` | New |
| `REPO-METRICS-BASELINE.md` | Refresh after trim |
| `docs/300-development/304-ai-sdlc-cloud/` | Packing note |
| `CHANGELOG.md` | Unreleased |

## Out of Scope

- Multi-repo split (#1197 Owner decision).
- Path-scoped CI (#1257 / #1422) and Playwright shards (#1258).
- Weakening TDD / Playwright / OpenSpec gates.
