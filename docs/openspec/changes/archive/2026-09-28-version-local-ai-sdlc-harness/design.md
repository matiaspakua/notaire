# Design — Version the local-AI SDLC harness

## Context

The harness was built over several sessions while driving #1069 through the full
workflow. Each failure of the local model became a deterministic gate or a git
guardrail; these are recorded in `local-ai/sdlc/AI-SDLC.md` (guardrails table).
This change only puts that directory under version control.

## Goals / Non-Goals

- Goal: `local-ai/` reviewed and versioned, without secrets, `.env` or tool caches.
- Non-goal: changing the harness behavior in this PR beyond what #1069 needed.

## Decisions

- **Separate worktree for this PR**: the main checkout holds unrelated uncommitted
  work; the branch is built from `origin/main` in `../notaire-harness`.
- **Exclude `.serena/` and `__pycache__/`**: tool state, not source.
- **`skip_specs: true`**: no product behavior, so no delta spec.

## Riesgos / Trade-offs

- The harness drives `gh pr merge`; it only merges after CI is green and the foreman
  runs `foreman.sh <n> merge` explicitly (Gate 4 stays human/Claude).
- Workers run with full shell access; the git hooks limit what reaches the branch,
  and `ref_guard`/`scope_guard` revert what they miss.

## Testing Strategy

No automated harness exists for these shell/prompt files (documented Gate 2
exception, see traceability.md). Verification:

- `bash -n` on every shell script, `python3 -m py_compile` on `bin/*.py`.
- End-to-end evidence: #1069 run through every phase to merge (PR #1073,
  gates.log in the PR body), Gate 5 smoke test passed on merged main.
- `bash scripts/preflight.sh` passes on this branch.

## Regression Strategy

No application code changes; preflight confirms the repo gates are unaffected.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Nothing is deployed; merge makes the harness available to every clone.

## Rollback Strategy

Plain `git revert` of the merge commit.

## Open Questions

None.
