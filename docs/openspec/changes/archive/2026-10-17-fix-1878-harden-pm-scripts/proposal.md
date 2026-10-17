# Harden milestone, label and board Owner scripts

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1878 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `fix/1878_harden_pm_scripts` |
| Gate 1 status | passed |

## Objetivo

Owner apply runs for milestones, labels and the Delivery Board still abort on
`due_on: null`, leave incomplete `--check` coverage, miss R6 (open PRs), time out
on `--scope all`, and keep Owner shell scripts unversioned. This change hardens
those tools so a whole apply survives every milestone kind, drift is visible, and
Process Checks catch premature `Closes` on multi-step PM work.

## What Changes

- Whole-run fake-GitHub regression that rejects `due_on: null` on POST and finishes
  with zero mismatches after `ensure_milestones` / `assign_issues` / `retire_milestones`.
- Richer `--check`: description/due drift, missing policy milestones, retired
  leftovers, closed issues with `status:*`, per-bucket summary, snapshot freshness.
- Token scope preflight (`repo`, `project`) before the first write; step headers and
  HTTP errors that name the step and object.
- Paginated GraphQL load for board coherence `--scope all`; loaders fill `open_prs`
  so R6 fires on live-shaped data.
- Repo tool (or apply step) that sets Status Done for closed issues.
- `VIEW_CONTRACTS` group/sort/column documentation; UI-only fields listed in
  `docs/github/README.md`; Current train derived or documented as a release step.
- Versioned Owner scripts under `workspace/sdlc/owner/` with `bash -n` / shellcheck CI.
- Process Checks guard: `Closes`/`Fixes` forbidden when OpenSpec declares
  `multi_pr: true` or an Owner step is pending (use `Refs`).
- Absorb #1874 scope already present (`retire_milestones`, stale views) if that
  issue remains open; CHANGELOG and Owner README path updates.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Continuous milestones MUST omit `due_on` on create (never send null). | CU76; #1856 | Made explicit (whole-run) |
| `--check` MUST report every drift class the Owner dry-run shows. | CU76; Gate 5 | New |
| Multi-PR / Owner-pending OpenSpec changes MUST use `Refs`, not `Closes`/`Fixes`, until the final PR. | CONSTITUTION §4; #1847 | New |
| Board R6: `status:in-review` requires an open linked PR when the count is known. | CU76; #1550 | Made explicit (loader) |
| Owner scripts are preview-by-default and versioned in-repo. | CU76 | New |

## Capabilities

### New Capabilities
- `pm-owner-tooling`: Contracts for milestone apply/check, board coherence loaders,
  Owner shell scripts, and the Closes/Refs Process Checks guard.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | Process Checks: Closes guard; shellcheck/`bash -n` for `workspace/sdlc/owner/*.sh` |
| `workspace/sdlc` | yes | apply/check, coherence, Owner scripts, unit tests |

### Surface area

- Entities / endpoints / Flyway: none.
- Configuration / `.env`: none.
- Dependencies: none (uses existing `gh` / GitHub API).
- **BREAKING**: no API break. `--check` becomes stricter (may fail until Owner
  apply + snapshot PR are current).

## Documentation Impact

| Document | Action | Why |
|----------|--------|-----|
| `docs/github/README.md` | update | VIEW group/sort UI-only notes; Current train |
| `docs/300-development/305-project-management/README.md` | update | Owner script paths under `workspace/sdlc/owner/` |
| `workspace/sdlc/owner/README.md` | create | Preview-default Owner runbook |
| `CHANGELOG.md` | update | Unreleased note for #1878 |
| CONSTITUTION §4 | Owner (out of agent scope for wording) | Document Closes vs Refs for multi_pr |

## Out of Scope

- Label taxonomy phase 2 (#1860).
- Live `--apply` re-run (Owner already applied; mismatches: 0).
- Amending CONSTITUTION body text in this PR if the Owner prefers a follow-up.

## Open Questions

None blocking Gate 1. Current train: derive earliest open `vX.Y` when safe;
otherwise document the release retarget step.
