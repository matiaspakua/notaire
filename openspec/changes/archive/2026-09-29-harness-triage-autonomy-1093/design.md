# Design — triage autonomy fixes found in #1064

## Context

`with_retries` calls `gate_scope && "$gate"`. `gate_scope` moves `scope.out`
(written by `scope_guard` and `ref_guard` after they revert something) into
`gate.out` and fails. `gate_triage` derives the change's surfaces from the
Files to Edit paths (`adapter.py surfaces`) and rejects `KIND=code` with none.

## Goals / Non-Goals

**Goals:** neither #1064 triage failure cause can recur; the worker never pays
an attempt for something the harness already fixed.

**Non-Goals:** a `scripts/` surface (the red gate is JUnit-specific); better
triage reasoning by the model.

## Decisions

- **Reverted means handled.** Both guards revert before the gate runs, so the
  gate judges the tree as the phase allows it. The violation is logged as
  `scope-reverted REVIEW` for Gate 4. When the gate fails anyway, the violation
  heads `gate.out`: the revert may be why (lost work), and the retry must know.
- **Fallback, not override.** `TEST_SURFACE` is used only when no Files to Edit
  path is under a surface root; a path-derived surface always wins. It is
  pre-filled `backend` by the template so the model need not decide it in the
  common case, and validated against the adapter's surfaces.
- **Precedent-based prompt example.** #1063's `JacocoCoverageConfigConsistencyTest`
  reads `CONSTITUTION.md` from the repo root; the prompt names this pattern.

## Riesgos / Trade-offs

- A worker that keeps violating scope is no longer stopped by retries alone.
  Its work outside scope never survives, and Gate 4 sees every `scope-reverted` entry.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fallback used; Path surface wins; Unknown fallback ignored | unit | `local-ai/sdlc/tests/test_adapter.py` |

`gate_scope` and `gate_triage` are shell glue with no test runner: checked by
`bash -n` and by the #1064 rerun.

## Regression Strategy

`python3 -m unittest discover -s local-ai/sdlc/tests`, `bash -n foreman.sh`,
then the autonomous #1064 run.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

Merged through the PR. The next `foreman.sh` run uses it.

## Rollback Strategy

`git revert` of the merge commit.

## Migration Plan

None.

## Open Questions

None.
