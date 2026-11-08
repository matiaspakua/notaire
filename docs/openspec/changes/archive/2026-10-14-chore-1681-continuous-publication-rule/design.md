> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The SessionStart hook already lists local branches that were never pushed (#1647); this rule prevents them instead of reporting them.

## Goals / Non-Goals

**Goals:** GitHub always holds the current branch and a resume note on the issue.
**Non-Goals:** automatic enforcement outside the agent's own discipline.

## Decisions

1. **A helper script, not a hook.** Hooks cannot know the stage; the agent calls the helper after each commit group. 2. **Verify the remote head.** RTK can print ok for a rejected push (see ai-agent-workflow.md), so the helper compares ls-remote to HEAD.

## Riesgos / Trade-offs

- [Pre-push preflight rejects an incomplete stage] → the helper reports the failure and leaves the stage unpublished; PREFLIGHT_SKIP stays an explicit Owner/agent choice.
- [Comment noise] → one comment per stage group.

## Testing Strategy

workspace/tests/test_publish_progress.py uses a bare remote and a fake gh; documentation scenarios are checked by the same file.

## Regression Strategy

python3 -m unittest discover -s workspace/tests; bash workspace/sdlc/check-agent-rules.sh; python3 workspace/ci/agent-context-budget.py --max-tokens 8000

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
