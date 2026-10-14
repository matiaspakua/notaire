> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Measured baseline on main (epic #1693): 6 of 38 controllers clean, 81 of 108 application files import JPA entities, frontend has no ports. ADR-021 allowed Spring Service and Transactional in use cases; the Owner kept that on #1693.

## Goals / Non-Goals

**Goals:** an agent can pick a slice and know exactly what to build, what to delete and when it is done.
**Non-Goals:** choosing slice order for the whole backlog.

## Decisions

1. **Strict hexagon over a pragmatic one.** Persistence entities move inside the adapter and the domain is pure. 2. **Mermaid for new diagrams** (ADR-027). 3. **Appendix, not a rewrite, for the skill** so the generic skill stays reusable.

## Riesgos / Trade-offs

- [Docs drift from the code again] → the ArchUnit and lint baselines are the executable truth; the docs cite them.
- [Context budget] → the new rule is on-demand; only one AGENTS.md row is always loaded.

## Testing Strategy

docs/tests, markdown lint on changed files, agent-context-budget.py and check-agent-rules.sh.

## Regression Strategy

python3 -m unittest discover -s docs/tests; python3 workspace/ci/agent-context-budget.py --max-tokens 8000

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
