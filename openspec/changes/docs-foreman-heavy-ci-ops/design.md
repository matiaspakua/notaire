# Design — Harden cloud-foreman heavy-CI merge gate ops

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

#1138 / `docs-ci-merge-learnings` documented the heavy merge gate and
`scripts/check-heavy-ci.sh`. Fleet ops on 2026-10-03 then showed residual gaps
in the foreman agent: (1) CI subscriptions can report “all N checks success”
while heavy jobs are still queued/omitted from check-runs — wake-up only, always
re-run the heavy script; (2) concurrent Playwright-heavy tips starve runners —
serialize and draft Dependabot floods; (3) some cloud workers get `gh` 401 —
coordinator (or same-VM worker with working `gh`) must own ready/merge after
heavy-gate exit 0. The product docs change is already on the branch tip in
`.claude/agents/cloud-foreman.md`; this design covers Gate 1 for Process Checks.

## Goals / Non-Goals

**Goals:**
- Complete Gate 1 (`skip_specs: true`) for the foreman merge-ops hardening so
  Process Checks pass without `sdlc-exception`.
- Keep CU76 / #1153 traceability; cite `CI-MERGE-GATE.md` as the permanent
  heavy-gate authority.

**Non-Goals:**
- No product code, no new workflow YAML, no `local-ai/` edits.
- No duplicate rewrite of `CI-MERGE-GATE.md` (already from #1138).
- No `sdlc-exception` label — this change folder satisfies Process Checks.
- No change to GitHub required-check settings (document agent behavior only).

## Decisions

- **`skip_specs: true`**: documentation / agent-config of process; no product
  requirement deltas.
- **Foreman-only surface**: extend `.claude/agents/cloud-foreman.md`; permanent
  merge-gate prose stays in `CI-MERGE-GATE.md`.
- **Issue #1153**: dedicated AC for residual foreman ops (subscription wake-up,
  Playwright serialize, worker `gh` 401 → coordinator).
- **PR #1151**: existing docs commit stays; OpenSpec commit unblocks Process Checks.

## Riesgos / Trade-offs

- [Agents still trust subscription green] → Explicit wake-up-only bullet +
  mandatory `check-heavy-ci.sh` re-run in foreman.
- [Runner starvation from Dependabot + feature PRs] → Serialize Playwright-heavy
  tips; draft Dependabot floods.
- [Worker `gh` 401 blocks merge] → Coordinator owns ready/merge after heavy gate.
- [Soft overlap with #1138 docs] → Complementary agent bullets only; no new
  parallel merge-gate doc.

## Testing Strategy

No application tests. Verification:

- Grep heavy script / wake-up / Playwright serialize / `gh` 401 coordinator under
  `.claude/agents/cloud-foreman.md`.
- `openspec validate docs-foreman-heavy-ci-ops --strict`
- `bash scripts/validate-sdlc-plan.sh docs-foreman-heavy-ci-ops`
- `PR_LABELS= PR_AUTHOR=cursoragent bash scripts/check-sdlc-exception.sh origin/main`
- Confirm no `local-ai/` runtime dependencies introduced.
- Before merge: `bash scripts/check-heavy-ci.sh 1151` (never light-only).

## Regression Strategy

No application code. Confirm Process Checks / docs CI still pass; no Java/TS surface.

## Playwright Strategy

n/a — no UI surface. Docs-only PR tips may still run Playwright in CI; wait for
heavy gate before merge. Foreman must serialize Playwright-heavy PRs.

## Deployment Strategy

Nothing deployed. Merge makes agent docs available to Cloud agents on next checkout.

## Rollback Strategy

Plain `git revert` of the merge commit.

## Open Questions

None.
