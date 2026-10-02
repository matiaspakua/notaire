# Document Cursor Cloud AI SDLC fleet process learnings

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1120 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/docs-ai-sdlc-cloud-learnings-69d3` |
| Gate 1 status | passed |

## Objetivo

After merging PRs #1111 / #1112 / #1113 / #1116, several process failures recurred
(issues left OPEN without `Closes #`, `[skip ci]` wiki commits wiping PR checks,
nested Docker/bc gaps, unsaved environment cards, unfilled OpenSpec templates).
This change records those learnings in `docs/300-development/304-ai-sdlc-cloud/`
so the Cloud fleet does not repeat them.

## What Changes

- Update `ENVIRONMENT-CHECKLIST.md` with Saved environment-card rules, host-network
  compose + `bc`, and process pitfalls.
- Update `FLEET-ARCHITECTURE.md` with foreman hard rules for `Closes #`, PR Validation
  wiki commits, and `scripts/seed-openspec-change.sh`.
- Update `README.md` to index the learnings briefly.
- Add this OpenSpec change folder (`skip_specs: true`).

No product code, CI workflows, or `local-ai/` changes.

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
| CI/CD (`.github/workflows`) | no | — |
| `docs/300-development/304-ai-sdlc-cloud/` | yes | Learnings in checklist, architecture, README |
| `openspec/changes/` | yes | This Gate 1 folder |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No application-architecture change. No ADR. Documentation only; Cloud path still
excludes `local-ai/`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | Process learnings + Saved card + nested Docker/`bc` |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Foreman hard rules for Closes / skip-ci / seed script |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Index learnings |
| `CHANGELOG.md` | n/a — not user-visible |
