> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The audit that produced Issue #1033 read `CONSTITUTION.md`, `AGENTS.md`,
`CLAUDE.md`, all `.claude/rules/*.md` files, `.claude/skills/README.md`, and
spot-checked several individual `SKILL.md` files against the project's actual
current state (removed modules, merged ADRs). Three concrete drift points were
found; see proposal.md for the full list with file:line references. This is a
pure text-editing change to five Markdown files.

## Goals / Non-Goals

**Goals:**

- Make the coverage floor-vs-target distinction consistent across every file
  that states it.
- Remove a stale reference to a module that no longer exists in the repo.
- Make the skill composition tables reflect a skill actually used in a merged
  change.

**Non-Goals:**

- Re-litigating whether 70%/25% or 80%/80% is the *right* number — only
  making the existing, already-decided distinction (documented authoritatively
  in `.claude/rules/code-quality.md`) consistent everywhere it is restated.
- Auditing or fixing every skill in `.claude/skills/` — only the concrete gaps
  found (see proposal.md "Out of Scope" for what is deliberately deferred).
- Any change to `preflight.sh` / CI workflow gate mapping — tracked separately
  under #1029/#1030.

## Decisions

- **Edit in place, don't restructure.** Each fix is a targeted line/paragraph
  edit in an existing file rather than a reorganization, to keep the diff
  reviewable and avoid introducing new duplication while fixing existing
  duplication.
- **Cite the authoritative file instead of re-deriving the number.** Where
  `AGENTS.md` and `ai-agent-workflow.md` restate the coverage gate, the fix
  points back to `.claude/rules/code-quality.md` as the single source of the
  70%/25% floor and 80%/80% target, rather than just swapping one hardcoded
  number for another — the original drift happened because the number was
  copied instead of referenced.
- **Add `hexagonal-arch` to the composition table under "Architecture
  decisions"** (alongside `architecture-decision-design`, `plantuml`) rather
  than creating a new table row, since it is a specialization of that same
  lifecycle concern, not a new one.

## Riesgos / Trade-offs

- [Risk] Editing `CONSTITUTION.md` §5 touches the document CONSTITUTION.md
  §12 calls "highest authority" → Mitigation: the edit is additive (one
  skill name added to an existing table cell) and does not change any
  process rule, gate, or convention; `git diff` on the PR makes this visible
  for review.
- [Risk] Fixing the coverage wording in two files but not auditing every doc
  that might mention "80%" → Mitigation: `grep -rn "80%"` was run across
  `CONSTITUTION.md`, `CLAUDE.md`, `AGENTS.md`, and `.claude/rules/*.md`
  during the audit (see proposal.md); every hit is either already correct
  (`code-quality.md`, `CLAUDE.md:139`, `CONSTITUTION.md:77/408` which already
  say "80% target") or is fixed by this change. No further occurrences found
  in the governed rule set.

## Testing Strategy

n/a — docs-only change, no application code or behavior to unit/integration/E2E
test. `skip_specs: true` is set accordingly.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| n/a | n/a | n/a |

- New unit tests: none
- New integration tests: none
- Coverage impact: none (no code touched)

## Regression Strategy

- Existing tests affected: none — no code changed.
- Full suite command: not required to change for this PR; run as part of
  `bash scripts/preflight.sh` for the markdown-lint pass.
- HTTP/Bruno API suite: not applicable.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface. This change touches only Markdown files under the repo
root and `.claude/`.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — merges directly, no deploy step changes
  behavior.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): n/a — no runtime surface; "smoke test" for
  this change is `bash scripts/preflight.sh` passing (markdown-lint) plus a
  manual read-through confirming the edited files render correctly and the
  cross-references resolve.

## Rollback Strategy

- Revert safe: yes — a plain `git revert` restores the prior text with no
  side effects, since nothing downstream depends on the corrected wording
  beyond human/agent readers.
- Database rollback: none needed.
- Data written under the new behavior after revert: none.
- Blast radius if rollback is delayed: none — worst case is the governance
  docs remain inconsistent, which is the pre-existing state.

## Migration Plan

n/a — single-step text edit, no staged rollout.
