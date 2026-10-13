# Archive OpenSpec changes in the implementing PR; validate delta ops; run process checks on main

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1623 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/1623_gate5-archive-in-pr` |
| Gate 1 status | passed |

Also closes #1622 (process checks on `main`), which this design depends on.

## Objetivo

On 2026-10-13, four merges (#1621, #1626, #1620, #1624) each left their OpenSpec
change active after the issue closed. Each time, `validate-sdlc-plan.sh` failed
on **every** open PR until someone archived the change by hand. Archiving was a
post-merge step that no role owned. Twenty-two of 32 deltas also could not
archive, because `openspec validate --strict` accepts `MODIFIED` on specs that
don't exist. And `main` broke silently, because process checks ran only on PRs.

## What Changes

- `check-openspec-archive-hygiene.py`: a touched active change must be archived in the PR unless it sets `multi_pr: true`; archiving no longer trips the task gate; other changes' closed-issue leftovers only warn on PRs (still fail with `--inventory-only`).
- `validate-sdlc-plan.sh`: a closed issue warns when validating all changes (it is still an error for a named change); calls the new `check-delta-ops.py`.
- `sdlc-process.yml`: a new `main-health` job on push to `main`, daily, and on dispatch.
- Constitution, agent card, workflow rule, CI-MERGE-GATE, foreman playbook and the tasks template: archive in the PR.
- Sweep: archive `docs-1624` and 24 merged slice changes; align delta headers; drop one requirement that #1389 superseded.
- `docs/tests`: the shared `openspec_changes.change()` finds a change whether it is active or archived.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The PR that implements a change archives it | CONSTITUTION Gate 5, #1252 | Changed (was after merge) |
| Another PR's leftover never blocks a PR; `main` fails on it | #1622, #1623 | New |
| Delta operations match the main spec at Gate 1 | OpenSpec schema | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities
- `ai-sdlc-enforcement`: four requirements above.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `sdlc-process.yml` main-health job |
| `workspace/sdlc`, `docs/openspec`, `docs/tests` | yes | checkers, sweep, test helper |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Process change, recorded as a Constitution amendment (§5, "Archive in the PR").

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CONSTITUTION.md` | Archive in the PR; §8 note |
| `CONSTITUTION-AGENT-CARD.md` | Rule 11 |
| `.claude/rules/ai-agent-workflow.md` | Archive row |
| `.claude/agents/cloud-foreman.md` | Archive before merge |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | Hygiene bullets |
| `docs/openspec/schemas/notaire-sdlc/templates/tasks.md` | 10.1 archive; 12.4 evidence |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- Atomic claiming, harness hygiene, and bot identity (Phase 1 issues filed separately).
