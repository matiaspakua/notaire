# Intake Foreman refuse + clean-board report (#2257 remainders)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2257 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2257_intake-foreman-refuse-7ab7` |
| Gate 1 status | passed |

## Objetivo

PR #2261 shipped `aisdlc intake check` (sections, checkbox, placeholders, links,
labels, JSON). The Foreman still has no machine-checkable refuse before dispatch,
and Notaire has no clean-board report that must precede raising enforcement. Without
those, agents can still start from under-specified issues, and flipping
`required=true` would be silent.

## What Changes

- `aisdlc foreman claim <issue> [--json] [--file F]`: claim-protocol gate that runs
  intake lint and returns `dispatch_allowed` / `would_refuse`. When
  `[intake] required = true`, a non-pass refuses dispatch (exit 1). When advisory
  (`required = false`, Notaire default), the check still reports honestly but does
  not refuse.
- `aisdlc intake board [--json] [--file F]`: clean-board report over open issues
  (via `[tracker] list` or a fixture file). `clean=true` only when every listed
  issue passes; documents that enforcement is raise-only after a clean report.
- Profile: `[intake] required` (bool, default false); `[tracker] list` (command
  printing a JSON array of issue objects). Section names remain adopter-configurable
  via `[intake] sections` (already shipped); kit code names no Notaire paths.
- Notaire profile: `[intake] required = false` (advisory) + `[tracker] list`; do
  **not** flip to `required = true` in this PR.
- Foreman template: claim step before stage dispatch.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Foreman refuses to dispatch when intake fails and `[intake] required = true` | #2257 AC; FACTORY-PRODUCT-ANALYSIS §10 | New |
| Advisory mode reports fail without refusing; enforcement is raise-only after a clean board report | #2257 AC | New |
| Section names (and related lint keys) are adopter-configurable; kit code has no Notaire product paths | #2257 AC; MODULE.md independence | Made explicit |
| A check that cannot run is `no-evaluable`, never pass (axiom 3) | FACTORY-ROADMAP axiom 3; #2261 | Preserved |

## Capabilities

### New Capabilities

- _(none — extends `issue-intake`)_

### Modified Capabilities

- `issue-intake`: Foreman claim refuse, board clean report, `[intake] required`,
  `[tracker] list`, advisory Notaire wiring.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` / `framework/ai-sdlc` | yes | intake/foreman/cli/profile/tests/docs |
| `.aisdlc` | yes | advisory `[intake]` + `[tracker] list` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (stdlib)

### Write set

- `framework/ai-sdlc/**`
- `.aisdlc/aisdlc.toml`
- `docs/openspec/changes/feat-2257-intake-foreman-refuse/**`
- `docs/openspec/specs/issue-intake/**`

### Architecture review

Follows existing Tracker-port + Foreman plan patterns. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | claim, board, `required`, `list` |
| `framework/ai-sdlc/CHANGELOG.md` | kit Unreleased note for remainders |
| `docs/openspec/specs/issue-intake/spec.md` | folded on archive |

## Out of Scope

- Rewriting existing issues automatically.
- Silently setting Notaire `[intake] required = true` without a clean board evidence.
- Context Lake / Wilson / fake Isolator / `[committer] required=true`.
- Product Playwright / Notaire UI changes.
