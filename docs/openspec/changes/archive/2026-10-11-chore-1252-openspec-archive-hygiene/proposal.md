# OpenSpec archive hygiene and task completion gate

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1252 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/chore-1252-openspec-archive-hygiene-69d3` |
| Gate 1 status | passed |
| SURFACE | none |

## Objetivo

Active OpenSpec changes still list merged work as incomplete (unticked `tasks.md`
checkboxes), so SessionStart / resume signals are noise. Assessment #1242 measured
this drift; #1252 restores a trustworthy lifecycle: archive shipped changes, keep
task checkboxes honest, and fail the process gate when a merge-ready PR leaves its
change unfinished.

## What Changes

- Reconcile every active change under `docs/openspec/changes/` (inventory at apply
  time): archive shipped trees with specs synced; leave true WIP untouched; fix or
  archive orphans (e.g. incomplete `.openspec.yaml` / missing artifacts).
- Extend the SDLC process check so a PR that touches an OpenSpec change fails when
  that change still has unchecked implementation tasks in groups 3–8 (or document
  and automate the post-merge archive step instead — design chooses the mechanical
  check).
- Document the OpenSpec archive + task-completion verification step for Cloud
  agents under `docs/300-development/304-ai-sdlc-cloud/` (alongside the existing
  heavy-CI merge gate).
- Modify capability `ai-sdlc-enforcement` (delta requirements). No product API or UI.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A shipped OpenSpec change MUST leave the active set after merge (archive with specs synced when deltas exist) | CU76, CONSTITUTION Gate 5 | Made explicit |
| Task checkboxes MUST reflect reality: implementation groups are ticked when the work shipped, or the change is archived | CU76, RF #90 / #91 | New |
| CI / preflight MUST reject a merge-ready PR whose linked OpenSpec change still has unchecked implementation tasks (unless `sdlc-exception`) | CU76 | New |
| Cloud merge-gate docs MUST tell agents to verify archive hygiene and task completion, not only heavy CI | CU76, `304-ai-sdlc-cloud/` | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ai-sdlc-enforcement`: add requirements for post-merge archive hygiene, honest
  task checkboxes, and a process-gate failure when a PR's change still has
  unchecked implementation work.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | Process Checks / `sdlc-process` may invoke the new checker |
| `workspace` | yes | New or extended `workspace/sdlc/` check + unit tests under `workspace/tests/` |
| `docs` | yes | OpenSpec archive moves; `304-ai-sdlc-cloud/` merge-gate docs; CU76 pointer |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (stdlib / existing `gh` + bash/`python3` only)
- SURFACE: **none** (docs + process tooling; no product backend or frontend)

### Architecture review

Follows existing AI SDLC enforcement pattern (`validate-sdlc-plan.sh`,
`check-sdlc-exception.sh`, `sdlc-process.yml`). No ADR — process hygiene, not a
new architectural shape. RDUF (INGENIERIA.md): Gate 1 plan first; apply archives
and the mechanical check in a later implement pass.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | Add OpenSpec archive + task-completion verification before merge |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Gate 5 / foreman row: archive hygiene is mechanical |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Index the new verification step if the TOC lists merge-gate duties |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Short pointer to OpenSpec lifecycle / archive rule |
| `docs/openspec/specs/ai-sdlc-enforcement/spec.md` | Sync delta requirements at archive time |
| `docs/openspec/changes/archive/` | Receive reconciled shipped change trees |
| `CHANGELOG.md` | n/a for this Gate 1 pack — Agent 3 holds CHANGELOG via #1518; apply phase adds Unreleased only after that merges |

## Out of Scope

- Owner decisions on #1445 / #1438 / #1226 (never invent A/B/C/LICENSE).
- Product feature implementation for open issues (#773, #774, #579, …).
- Editing `CHANGELOG.md` while #1518 owns it.
- Automating GitHub ruleset admin apply (#1040) or merge-API required-check
  enforcement for the agent account (documented operational gap only).
- `local-ai/` harness changes.
