# OpenSpec ↔ Constitution Bridge

`CONSTITUTION.md` is the highest authority for how a change is made in this
repository (§12). This document is the single, canonical record of how
**OpenSpec** (`openspec/`, `openspec` CLI) — Notaire's primary spec-driven
framework — was adapted to enforce that Constitution, and the evidence that
the adaptation holds up in real changes. It supersedes and consolidates
`docs/300-development/OPENSPEC-CONSTITUTION-BRIDGE.md` (archived, see
`docs/archive/`) into one location, per Constitution P7/§8 (never duplicate
permanent documentation).

This document is scoped to OpenSpec only. Notaire also evaluates **SpecKit**
as a second, fully separate spec-driven framework under `speckit/` — see
`speckit/NOTAIRE-ADAPTATIONS.md` for its own adaptation story. The two are
deliberately kept apart: different directories, different validation
scripts, no shared code or cross-references beyond this sentence. Nothing in
either tool's setup depends on the other.

## Analysis

### OpenSpec's native structure

`openspec` is a schema-driven CLI: a **schema** (`openspec/schemas/<name>/
schema.yaml`) declares a set of **artifacts** (proposal, spec deltas, design,
tasks, ...), each with a Markdown template and an `instruction:` block the
CLI injects into agent context via `openspec instructions`. `openspec new
change <name>` scaffolds a change directory under `openspec/changes/<name>/`
from those templates; `openspec archive <name>` moves a completed change to
`openspec/changes/archive/` and syncs its accepted deltas into
`openspec/specs/<capability>/spec.md`.

Notaire forked the packaged `spec-driven` schema into a project-owned one,
`openspec/schemas/notaire-sdlc/schema.yaml` (see the file's own header for
the exact diff against upstream): it keeps every OpenSpec-native parsing
primitive unchanged (`## ADDED/MODIFIED/REMOVED/RENAMED Requirements`,
`### Requirement:`, `#### Scenario:` with exactly four hashtags, `- [ ]`
task checkboxes) and adds CONSTITUTION.md as mandatory context, the sections
the Constitution requires in each artifact, a `traceability` artifact
implementing P4, and makes `design.md` mandatory for every change (upstream
it is conditional).

`openspec/config.yaml` carries that Constitution context to **every** agent
through the CLI's own extension points — `context:` (injected into
`openspec instructions` for every artifact) and `rules:`/`operations:`
(per-artifact and per-command guidance). This reaches Claude Code, OpenCode,
Copilot, Codex, Cursor and any future agent through the same channel,
without a tool-specific hook.

### The gap this closed: Explore → Issue → Propose

`opsx:explore` (`.claude/skills/openspec-explore`) is a thinking stance, not
a workflow — by design it can produce a candidate feature/gap report but
never scaffold a change. `opsx:propose` (`.claude/skills/openspec-propose`)
scaffolds a change and expects a real Issue number in the proposal header.
Both skills are vendor-generated (`metadata.author: openspec`) and get
overwritten by `openspec update`, so they cannot carry project-specific
process without it silently resetting. Nothing stopped an agent from typing
a plausible-looking, non-existent Issue number into a scaffolded
`proposal.md` — the only check that existed (`scripts/validate-sdlc-plan.sh`)
validated the **format** of the Issue reference (`#[0-9]+`), not that the
Issue was **real**.

## The fix: four project-owned pieces, no vendor file assumed stable

1. **`.claude/skills/openspec-triage/SKILL.md`** (new, project-owned skill,
   `metadata.author: notaire-project`, never touched by `openspec update`) —
   the missing middle step. It takes an exploration report and produces an
   estimated, prioritized, Use-Case-linked candidate-issue list (the shape
   already used by `openspec/functional_gaps_issues.md`), then creates real
   GitHub Issues on explicit user confirmation. Only its output — a real
   candidate Issue number — may be handed to `opsx:propose`.

2. **`openspec/config.yaml`** (project-owned, a CLI-native extension point,
   safe across `openspec update`) — `context:` is a pointer to
   `CONSTITUTION.md` plus its non-negotiables (never a copy — P7/§8), and
   documents the mandatory Explore → Issue → Propose sequence itself so an
   agent reading `openspec instructions` sees the rule before writing
   anything. `rules:` and `operations:` pin per-artifact requirements
   (Issue + Use Case in the header, live-verified; every template section
   present; traceability rows filled only as they actually happen; never a
   fabricated Issue number) and per-command guidance for `apply` and
   `archive`.

3. **`scripts/validate-sdlc-plan.sh`** (project-owned, agent-agnostic plain
   bash — works for a human via `scripts/preflight.sh` and
   `.github/workflows/pr-validation.yml` regardless of which agent or tool
   authored the change) — the actual mechanical gate. The Issue-reference
   check became two checks: format (`#[0-9]+` present — as before) and
   **live** (`gh issue view <number>` resolves and is `OPEN` — new). When
   the `gh` CLI is installed and authenticated, a fabricated or already-
   closed Issue number fails Gate 1 mechanically; without `gh` available the
   check degrades to a visible `note`, never a silent pass. It also enforces
   the `design.md` Testing/Regression/Playwright/Deployment/Rollback
   sections, the twelve mandatory `tasks.md` SDLC groups plus Definition of
   Done, and that `traceability.md` was not pre-filled.

4. **`openspec/changes/archive/`** (via the vendor `openspec-archive-change`
   skill, `metadata.author: openspec`, used as-is — the archive mechanism
   itself needed no adaptation) — Gate 5: a change only archives once
   deployed, smoke-tested and its Issue closed, `traceability.md` completed
   Issue-through-Release with real evidence, and its accepted deltas synced
   into `openspec/specs/`. `openspec/config.yaml`'s `operations.archive`
   guidance pins this sequence so an agent cannot archive early.

This keeps the adaptation entirely on the project side, per Constitution P10
("Adapt, don't replace"): OpenSpec's vendor skills, schema-parsing engine and
slash commands stay installable/upgradable as-is; the Constitution's
requirements are layered on through project-owned files plus one
already-wired mechanical gate.

## Sequence

```text
        opsx:explore              openspec-triage             opsx:propose
(thinking, produces   ──▶  (report → estimated,   ──▶  (scaffolds change,
report; never                prioritized candidates;      proposal.md cites
scaffolds a change)          creates real GitHub          REAL Issue)
                              Issues on confirmation)
                                        │
                                        ▼
                          scripts/validate-sdlc-plan.sh
                          resolves the Issue live via
                          `gh issue view` — a fabricated
                          or closed Issue fails Gate 1;
                          missing design/tasks sections
                          fail too
                                        │
                                        ▼
                          openspec-archive-change (Gate 5)
                          only after: deployed, smoke test
                          passed, Issue closed, traceability
                          complete → openspec/changes/archive/
```

## Evidence: changes carried through the adapted flow

As of this writing, `openspec/changes/archive/` holds 6 changes that went
through the full Explore → Issue → Propose → Apply → Archive cycle, each
with a `traceability.md` recording real commit SHAs, a merged PR and a
passing CI run — not pre-filled placeholders:

| Change | Issue | PR | Notes |
|---|---|---|---|
| `persist-metodo-pago-on-pago` | #792 | #828 | Bug fix carried through the full schema |
| `resolve-presupuesto-tramite-cardinality` | — | — | Data-model correction |
| `verify-debt-on-gestion-archive` | #819 | #826 | Business rule gate on archiving |
| `payment-financial-tracking` | #820 | #845 | Larger feature, multi-capability spec deltas |
| `escritura-post-firma-legal-cycle` | #832 | #852 | Legal workflow after signature |
| `gestion-workflow-y-bitacora` | #833 | #855 | Workflow engine + audit trail, 8 commits, full E2E/CI evidence (see the sample `traceability.md` for this change for the exact Chain/Gate-log shape every other change follows) |

Several more changes are active under `openspec/changes/` at any given time
(costs, discounts, folio linkage, protocol numbering, ...) following the
same schema — `openspec/explore.md` is the single, permanent triage record
that every business finding must resolve into a real Issue against, per
`openspec/config.yaml`'s own `context:` rule.

## Verification (2026-08-13)

Tested directly against `scripts/validate-sdlc-plan.sh` using a scratch
fixture copied momentarily into `openspec/changes/` (never committed):

| Case | Issue reference | Result |
|------|------------------|--------|
| Negative | `#999999` (does not exist) | ✗ `proposal: Issue #999999 was not found on GitHub` — Gate 1 fails |
| Positive | `#780` (real, open) | ✓ `proposal: Issue #780 verified live on GitHub (OPEN)` |
| Degraded | any number, `gh` removed from `PATH` | `- proposal: gh CLI not available/authenticated — Issue not live-verified` (advisory, does not fail the gate) |

## References

`CONSTITUTION.md` §5 (SDLC, step 0.5 applies to OpenSpec), §10 (Specification
mechanism), §12 (highest authority), P4 (Traceability), P7/§8 (never
duplicate permanent documentation), P10 (Adapt, don't replace).
`openspec/functional_gaps_issues.md` for the candidate-issue-list shape
`openspec-triage` reuses. This bridge is process/tooling, Use Case `N/A` per
Constitution §1/§12, tracked under #783 (original OpenSpec adoption) and #870
(this consolidation).
