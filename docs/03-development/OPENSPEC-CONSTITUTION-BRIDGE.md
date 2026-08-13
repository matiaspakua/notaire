# OpenSpec ↔ Constitution Bridge: Explore → Issue → Propose

`CONSTITUTION.md` is the highest authority for how a change is made in this
repository (§12). OpenSpec is its Specification mechanism (§10, §5 step 3). This
document describes how the two stay aligned for the part of the flow that sits
*before* a change is scaffolded: turning open-ended exploration into a real,
governed GitHub Issue.

## The problem this closes

`opsx:explore` (`.claude/skills/openspec-explore`) is a thinking stance, not a
workflow — by design it can produce a candidate feature/gap report but must
never scaffold a change. `opsx:propose` (`.claude/skills/openspec-propose`)
scaffolds a change and expects a real Issue number in the proposal header. Both
skills are vendor-generated (`metadata.author: openspec`) and are overwritten by
`openspec update`, so they cannot carry project-specific process without being
silently reset. Nothing stopped an agent from typing a plausible-looking but
non-existent Issue number into a scaffolded `proposal.md` — the only check
(`scripts/validate-sdlc-plan.sh`) validated the **format** of the Issue
reference (`#[0-9]+`), not that the Issue was **real**.

## The fix: three project-owned pieces, no vendor file touched

1. **`.claude/skills/openspec-triage/SKILL.md`** (new, project-owned skill,
   `metadata.author: notaire-project`, not touched by `openspec update`) — the
   missing middle step. It takes an exploration report and produces an
   estimated, prioritized, Use-Case-linked candidate-issue list (the same shape
   already used by `openspec/functional_gaps_issues.md`), then creates real
   GitHub Issues on explicit user confirmation. Only after this step does a
   candidate have a real Issue number `opsx:propose` may reference.

2. **`openspec/config.yaml`** (project-owned, read by the CLI into
   `openspec instructions` for every agent) — `context` now states the mandatory
   Explore → Triage → Issue → Propose sequence and points at this document;
   `rules.proposal` now requires the Issue to be live-verified, not just typed
   in, and requires a candidate that came from a report to have gone through
   `openspec-triage` first.

3. **`scripts/validate-sdlc-plan.sh`** (project-owned, agent-agnostic bash,
   already the one enforcement point that reaches every agent and human via
   `scripts/preflight.sh` and `pr-validation.yml` regardless of tool) — the
   Issue-reference check is now two checks:
   - format (`#[0-9]+` present) — as before;
   - **live** (`gh issue view <number>` resolves to state `OPEN`) — new. When
     the `gh` CLI is installed and authenticated, a fabricated or closed Issue
     number fails Gate 1 mechanically. Without `gh` available, the check
     degrades to a visible `note`, never a silent pass.

This keeps the adaptation on the project side, per Constitution P10 ("Adapt,
don't replace"): OpenSpec's vendor skills and slash commands are unmodified and
safe from being overwritten; the Constitution's requirements are layered on
through the CLI-native extension points (`config.yaml`) and the one
already-wired mechanical gate (`validate-sdlc-plan.sh`).

## Sequence

```
opsx:explore                 openspec-triage                  opsx:propose
(thinking, produces   ──▶    (report → estimated,      ──▶    (scaffolds change,
 a report; never              prioritized candidates;          proposal.md cites
 scaffolds a change)          creates real GitHub               a REAL Issue)
                               Issues on confirmation)
                                                                       │
                                                                       ▼
                                                   scripts/validate-sdlc-plan.sh
                                                   resolves the Issue live via
                                                   `gh issue view` — fabricated
                                                   or closed Issue fails Gate 1
```

## Verification (2026-08-13)

Tested directly against `scripts/validate-sdlc-plan.sh` using a scratch
fixture copied momentarily into `openspec/changes/` (never committed):

| Case | Issue reference | Result |
|------|------------------|--------|
| Negative | `#999999` (does not exist) | ✗ `proposal: Issue #999999 was not found on GitHub` — Gate 1 fails |
| Positive | `#780` (real, open) | ✓ `proposal: Issue #780 verified live on GitHub (OPEN)` |
| Degraded | any number, `gh` unavailable (restricted `PATH`) | `- proposal: gh CLI not available/authenticated — Issue #<n> not live-verified` (note, not a false pass) |

## Related

- `CONSTITUTION.md` §5 step 3 (Specification via OpenSpec), §10 (Spec-Driven
  Development is the mechanism), §13 (Tooling Map).
- `openspec/functional_gaps_issues.md` — the candidate-issue-list shape
  `openspec-triage` follows.
- Issue #783 (closed) — precedent for framing a process/tooling change with
  Use Case `N/A` per Constitution §1/§12.
