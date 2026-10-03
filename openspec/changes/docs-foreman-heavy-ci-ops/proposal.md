# Harden cloud-foreman heavy-CI merge gate ops

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1153 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/docs-foreman-heavy-ci-69d3` |
| Gate 1 status | passed |

## Objetivo

After #1138 landed CI-MERGE-GATE learnings and `scripts/check-heavy-ci.sh`, residual
fleet merge ops still needed hardening in `.claude/agents/cloud-foreman.md`:
treat CI subscription “all checks success” as wake-up only, serialize
Playwright-heavy PRs (draft Dependabot floods when runners starve), and have the
coordinator (or a same-VM worker with working `gh`) own merge when a cloud
worker’s `gh` returns 401. Heavy-script authority on the current head remains
the merge gate.

## What Changes

- Harden `.claude/agents/cloud-foreman.md` PR/merge section:
  - Merge only after `bash scripts/check-heavy-ci.sh <pr>` exits 0 on the
    **current head** (Integration + Coverage + Bruno + Playwright).
  - Never merge on light-only `gh pr checks` green (`CI-MERGE-GATE.md`).
  - A `github:ci:branch` “all checks success” event is wake-up only — always
    re-run the heavy script before merge.
  - Serialize Playwright-heavy PRs; draft Dependabot floods when runners starve.
  - If a cloud worker’s `gh` returns 401, the **coordinator** owns
    `gh pr ready` / squash-merge after heavy-gate exit 0.
- Add this OpenSpec change folder (`skip_specs: true`) so Process Checks pass
  without an `sdlc-exception` label.

No product code, workflow YAML, or `local-ai/` changes. Canonical merge-gate
prose remains `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md`
(already present from #1138); this change only hardens the foreman agent.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No business rule — engineering process documentation for Cloud agents (CU76 QA infrastructure). | CU76 | Made explicit |

## Capabilities

### New Capabilities

None — docs-only. `skip_specs: true` is set in `.openspec.yaml`.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — (ops docs / agent only) |
| `docs/300-development/304-ai-sdlc-cloud/` | no | Canonical `CI-MERGE-GATE.md` already covers heavy gate |
| `.claude/agents/cloud-foreman.md` | yes | Residual merge-ops bullets |
| `openspec/changes/` | yes | This Gate 1 folder |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No application-architecture change. No ADR. Documentation / agent-config only;
Cloud path still excludes `local-ai/`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `.claude/agents/cloud-foreman.md` | Residual heavy-CI merge ops bullets |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | n/a — already canonical from #1138 |
| `CHANGELOG.md` | n/a — not user-visible |
