> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Stage knobs (#2182/#2183) validate and list via `aisdlc stage list`. Remaining gaps call for Foreman ops habit; this slice kit-backs the **routing surface** only (print/resolve), not Isolator OS or agent execution.

## Goals / Non-Goals

**Goals:** `aisdlc foreman plan|route [--stage] [--json]`; honesty fields; Notaire ≥1 mapped stage; cloud-foreman + kit template cite CLI; thin plan / remaining-gaps update
**Non-Goals:** executing agents/MCP, Isolator OS, Approval/Forge write runtimes, Wilson vanity stages, flipping committer.required

## Decisions

1. Reuse `stages.stage_dicts` for knob serialization; enrich with gate names per stage for dispatch context.
2. `foreman plan` and `foreman route` are aliases (same handler).
3. `--stage <id>` filters; unknown gate stage → exit 2; known gate without knobs → empty mapped list (exit 0).
4. No new doctor ERROR; optional INFO that foreman plan is read-only when stages are mapped.
5. Docs: cloud-foreman Dispatch section + kit `templates/project/.aisdlc/agents/foreman.md`.

## Riesgos / Trade-offs

Readers treat plan as live orchestration → mitigated by JSON `executes=false` / `sandbox=false` / note + docs language.

## Testing Strategy

| Scenario | Test |
|----------|------|
| omitted knobs → empty plan | ForemanPlanTest |
| mapped stage appears in JSON | ForemanPlanTest |
| `--stage` filter | ForemanPlanTest |
| unknown stage exit 2 | ForemanPlanTest |
| never executes / honesty fields | ForemanPlanTest |
| route alias | ForemanPlanTest |

## Regression Strategy

Existing stage-profiles validate/list tests; Notaire doctor / config validate.

## Playwright Strategy

n/a — no UI surface (kit CLI only)

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
