# Design — Capture CI merge and CodeQL fleet learnings

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

\#1134 added `CI-MERGE-GATE.md` and `scripts/check-heavy-ci.sh`. Cascade work on
2026-10-02 then showed: (1) agents still need an explicit heavy-gate command and
docs-only Playwright note; CI subscriptions can report “all N checks success”
(e.g. 18 on #1137) while `CI - Build, Test & Security` / Playwright are still
pending — never trust subscription success alone; (2) Integration/Playwright
Budget/person / `undefined, undefined` failures on tips behind `main` were fixed
by rebasing onto #1132’s nested `BudgetResponse.person`, not by inventing product
fixes; (3) adding `.github/workflows/codeql.yml` while Code Scanning default
setup is on rejects advanced SARIF and fails Analyze.

## Goals / Non-Goals

**Goals:**

- Extend existing fleet docs (no parallel duplicates) with the three learnings.
- Cross-link DevSecOps CodeQL ops (`enable-gh-secure.sh`, `wait-for-processing`).
- Keep Gate 1 complete with `skip_specs: true` and CU76 / #1133 traceability.

**Non-Goals:**

- No product code, no new workflow YAML in this PR, no `local-ai/` edits.
- No `sdlc-exception` label — this change folder satisfies Process Checks.
- No change to GitHub required-check settings (document agent behavior only).

## Decisions

- **`skip_specs: true`**: documentation of process; no product requirement deltas.
- **Extend `CI-MERGE-GATE.md`**: canonical citation for heavy gate + rebase-first;
  thin rows in checklist / architecture / preflight / foreman.
- **Issue #1133**: same CU76 acceptance theme as #1134 (light-CI false positive /
  required terminal checks); this PR completes lingering AC and closes #1133.
- **CodeQL section in DevSecOps README**: lives next to security/CI pipeline docs;
  complements (does not replace) #1136 when that lands `codeql.yml`.

## Riesgos / Trade-offs

- [Agents still trust light-only green] → Keep `check-heavy-ci.sh` as the merge
  gate command in CI-MERGE-GATE + foreman.
- [Stale tips look like product bugs] → Explicit rebase-first checklist in
  CI-MERGE-GATE before inventing fixes.
- [CodeQL default + advanced conflict] → Document XOR rule + admin script.
- [Soft conflict with #1136 on DevSecOps README] → Complementary ops section;
  rebase whichever lands second.

## Testing Strategy

No application tests. Verification:

- Grep heavy gate / rebase-first / CodeQL XOR under
  `docs/300-development/304-ai-sdlc-cloud/`, `CI-PREFLIGHT.md`,
  `208-devsecops/README.md`, and `.claude/agents/cloud-foreman.md`.
- `openspec validate docs-ci-merge-learnings --strict`
- `bash scripts/validate-sdlc-plan.sh docs-ci-merge-learnings`
- Confirm no `local-ai/` runtime dependencies introduced.
- Before merge: `bash scripts/check-heavy-ci.sh 1138` (never light-only).

## Regression Strategy

No application code. Confirm Process Checks / docs CI still pass; no Java/TS surface.

## Playwright Strategy

n/a — no UI surface. Docs-only PR tips may still run Playwright in CI; wait for
heavy gate before merge.

## Deployment Strategy

Nothing deployed. Merge makes docs available to Cloud agents on next checkout.

## Rollback Strategy

Plain `git revert` of the merge commit.

## Open Questions

None.
