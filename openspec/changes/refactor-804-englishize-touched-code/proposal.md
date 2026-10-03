# Englishize #804 touched management/workflow code

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1203 |
| Use Case | CU02, CU53, CU16, CU83 — Management workflow English hygiene |
| Branch | `cursor/refactor-804-englishize-touched-code-69d3` |
| Gate 1 status | passed — CI follow-up for PR #1201 |

## Objetivo

Follow-up to merged #804 / PR #1199: Englishize comments, OpenAPI text, logs,
exception messages, and tests in management/workflow files already touched for
workflow status enforcement, so CI and Matias English-only hygiene pass.

## What Changes

- English developer-facing / API message strings in management transition and
  related controller/use-case code touched by #804.
- Regenerated committed OpenAPI artifact (`Gestiones` → `Managements` tag text,
  English summaries/descriptions).
- Align integration and Playwright assertions with English error messages and
  with the #804 rule that plain PUT cannot change status.
- Spanish **URL path segments** (`/gestiones`, `/historial`, `/archivar`, …) and
  existing API JSON keys remain deferred to the rename ADR.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Touched codebase strings must be English | Matias hard rule; #1203 | Made explicit |
| Status mutations use POST `/{id}/transition` | #804 / CU83 | Unchanged (already enforced) |
| Spanish URL paths / JSON keys deferred | rename ADR | Unchanged |

## Capabilities

### New Capabilities

- (none — pure language hygiene / test alignment; `skip_specs: true`)

### Modified Capabilities

- (none under `openspec/specs/` — observable messages change language only;
  transition/archive requirements are unchanged)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | English strings in management/workflow touched files; OpenAPI yaml; tests |
| `frontend` | yes | Playwright assertions for English transition-rejected message |
| `frontend-swing` | no | Removed module |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: same paths; response **message text** Englishized where touched
- Database (Flyway): none
- Configuration / `.env`: none
- Dependencies: none
- **Not BREAKING** for URL/JSON contracts; clients that match Spanish error
  substrings need updating (Playwright does)

### Architecture review

No architectural change. Continues repository/use-case patterns from #804.
No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `backend-api/openapi/openapi.yaml` | Regenerated committed artifact |
| `CHANGELOG.md` | n/a for this CI follow-up commit set (hygiene) |
| Use Case CU docs | n/a — behavior unchanged |

## Out of Scope

- Renaming Spanish URL path segments or JSON keys (rename ADR).
- Product work on #799 / PR #1202.
- Re-opening closed #804 (follow-up tracked as #1203).
