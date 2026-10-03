# Document light-CI merge-when-green false positive

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1133 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/docs-ci-merge-gate-false-green-69d3` |
| Gate 1 status | passed |

## Objetivo

Light CI (PR Validation + Frontend + SDLC ~12 checks) can report success while
`CI - Build, Test & Security` and Playwright are still pending. Document this
false positive so the Cloud fleet never merges on light-only green.

## What Changes

- Add `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` stating the rule
  and required terminal checks (Unit, Integration, Coverage Gate, Bruno, Playwright).
- Index the rule in `README.md`, `ENVIRONMENT-CHECKLIST.md` §7, and
  `FLEET-ARCHITECTURE.md` §9 / CI-watch / “must never”.
- Harden `.claude/agents/cloud-foreman.md` with the same hard exclusion.
- Add this OpenSpec change folder (`skip_specs: true`).

No product code, CI workflow YAML, or `local-ai/` changes.

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
| `docs/300-development/304-ai-sdlc-cloud/` | yes | New `CI-MERGE-GATE.md` + index updates |
| `.claude/agents/cloud-foreman.md` | yes | Hard rule: never merge on light-CI-only green |
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
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | New short process doc |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Index the new doc + learning |
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | §7 row: light CI ≠ mergeable |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | CI watch + must-never + §9 learning |
| `.claude/agents/cloud-foreman.md` | Hard exclusion mirroring the doc |
| `CHANGELOG.md` | n/a — not user-visible |
