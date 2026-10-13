> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The rules live in CI-MERGE-GATE.md, ai-agent-workflow.md, hooks.md and CONSTITUTION §5; none is a single on-demand playbook an agent reads when a PR event arrives.

## Goals / Non-Goals

**Goals:** one short playbook, always in sync with its sources.
**Non-Goals:** new policy.

## Decisions

1. **Restate and link, never redefine.** The skill points at the owning document for each rule.
2. **On-demand, not always-loaded**, to respect the 8,000-token context budget (#1259).
3. **Path guard test** so a renamed script breaks the build instead of the playbook.

## Riesgos / Trade-offs

- [Drift from the sources] → the path guard catches renames; wording drift is reviewed with the sources.

## Testing Strategy

`workspace/tests/test_steward_skill.py`: frontmatter, cited paths, required topics. Written first.

## Regression Strategy

`python3 -m unittest discover -s workspace/tests`; `agent-context-budget.py --max-tokens 8000`.

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
