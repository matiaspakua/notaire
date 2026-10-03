# Cancel superseded CI runs on the same PR branch

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1148 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/ci-cancel-in-progress-69d3` |
| Gate 1 status | drafted (implement after #1147 merges) |

## Objetivo

Rapid fix pushes leave superseded `ci.yml` / `playwright-e2e.yml` runs holding
runners because `cancel-in-progress: false`. Agents cannot cancel Actions
(HTTP 403). The latest PR head stays `pending` (observed on #1145 and #1147).

## What Changes

- Set `cancel-in-progress: true` on `.github/workflows/ci.yml` and
  `.github/workflows/playwright-e2e.yml` same-ref concurrency groups.
- Keep `.github/workflows/deploy-github-page.yml` at `cancel-in-progress: false`.
- Document the rule in `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md`.
- Add a static unit assert that the three YAML concurrency flags stay correct.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No product business rule — CI reliability for CU76 QA infrastructure. Same-ref cancel only; must not cancel unrelated PRs. | CU76 | New (workflow policy) |

## Capabilities

### New Capabilities

- `ci-workflow-concurrency` — same-ref cancel-in-progress for CI + Playwright.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — (unit test only under frontend if placed there; prefer scripts/tests) |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `cancel-in-progress` flips on ci + playwright |
| `docs/300-development/304-ai-sdlc-cloud/` | yes | CI-MERGE-GATE concurrency note |
| `openspec/changes/` | yes | This Gate 1 folder |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No application-architecture change. No ADR. Workflow concurrency policy only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | Note that ci.yml + playwright-e2e cancel superseded same-ref runs; pages deploy does not |
| `CHANGELOG.md` | n/a — not user-visible product change |

## Out of Scope

- Changing runner counts or Dependabot draft policy
- Cancelling runs across different branches/PRs
- Altering `deploy-github-page.yml` cancel behavior
