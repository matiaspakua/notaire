# Design — Document light-CI merge-when-green false positive

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

On 2026-10-02, subscriptions / `gh pr checks` reported “all green” for #1126
after ~12 light checks while backend `CI - Build, Test & Security` and Playwright
were still pending. Premature merge was avoided; the rule must live in permanent
fleet docs.

## Goals / Non-Goals

**Goals:**

- Short, findable process doc under `304-ai-sdlc-cloud/` with the false positive
  and required terminal checks.
- Cross-link from README, checklist, fleet architecture, and `cloud-foreman`.
- Keep Gate 1 complete with `skip_specs: true` and CU76 / #1133 traceability.

**Non-Goals:**

- No product code, workflow YAML, or `local-ai/` edits.
- No change to GitHub required-check settings (document agent behavior only).

## Decisions

- **`skip_specs: true`**: documentation of process; no product requirement deltas.
- **Dedicated `CI-MERGE-GATE.md`**: short enough to be the canonical citation;
  fold a one-line summary into checklist / architecture so either entry point works.
- **Verify via `gh run list`**: agents must confirm heavy workflows completed for
  the PR head SHA, not merely that finished light checks are green.

## Riesgos / Trade-offs

- [Agents still trust `gh pr checks` blindly] → Explicit verify command + list of
  required job names in `CI-MERGE-GATE.md` and foreman hard exclusions.
- [Docs-only PR itself tempt light-merge] → Apply the same rule before merging
  this PR (wait for heavy CI / Playwright terminal success).

## Testing Strategy

No application tests. Verification:

- Grep merge-gate rule in `docs/300-development/304-ai-sdlc-cloud/` and
  `.claude/agents/cloud-foreman.md`.
- `openspec validate docs-ci-merge-gate-false-green-1133 --strict`
- `bash scripts/validate-sdlc-plan.sh docs-ci-merge-gate-false-green-1133`
- Confirm no `local-ai/` runtime dependencies introduced.

## Regression Strategy

No application code. Confirm Process Checks / docs CI still pass; no Java/TS surface.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Nothing deployed. Merge makes docs available to Cloud agents on next checkout.

## Rollback Strategy

Plain `git revert` of the merge commit.

## Open Questions

None.
