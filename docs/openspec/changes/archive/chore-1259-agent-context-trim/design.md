# Design — agent context trim (#1259)

## Context

Measured on 2026-10-10: CONSTITUTION ~8768 tokens; AGENTS ~4147; ui-ux-design rule ~4108;
frontend-design skill ~3527; other alwaysApply rules push total ~30k. Budget ≤8k (#1259).

## Goals / Non-Goals

**Goals:** ≤8k always-loaded by committed meter; keep Constitution authority and Gates.  
**Non-Goals:** Deleting Constitution content; weakening TDD/Playwright; multi-repo split.

## Decisions

| Decision | Choice | Alternative | Why alt lost |
|----------|--------|-------------|--------------|
| Constitution in context | Agent card ≤3KB always; full on demand | Keep full CONSTITUTION always | Alone exceeds 8k |
| AGENTS shape | Slim + on-demand table | Keep `@` bulk imports | Imports explode context |
| Large skills/rules | `alwaysApply: false` | Keep alwaysApply | Dominates budget |
| Meter | bytes/4 heuristic + max-tokens fail | Exact tokenizer | Matches repo-metrics; portable |

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Agents miss a rule | On-demand table in AGENTS; hooks/OpenSpec still fire |
| Card drifts from Constitution | Card says full file prevails; review when Constitution changes |
| check-agent-rules breaks | Keep paths valid; update allowlists |

## Testing Strategy

| Scenario | Level | Verification |
|----------|-------|--------------|
| Budget ≤8k | Guard | `agent-context-budget.py --max-tokens 8000` + unit test |
| Card size | Guard | Card file ≤3072 bytes |
| alwaysApply off on large skills | Guard | Parse SKILL.md / rules frontmatter |
| check-agent-rules green | Script | `bash workspace/sdlc/check-agent-rules.sh` |

## Regression Strategy

`check-agent-rules.sh`, `validate-sdlc-plan.sh` smoke, metrics baseline regenerate.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Docs/agent packing only; no runtime deploy.

## Rollback Strategy

Revert PR; restore previous AGENTS and alwaysApply flags.
