# Scaffolded OpenSpec changes pass markdownlint

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1689 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1689_openspec-scaffold-markdownlint` |
| Gate 1 status | passed |

## Objetivo

Every change scaffolded with `seed-openspec-change.sh` + `scaffold-openspec-change.py`
fails the repository's `markdownlint-cli2` (MD022, MD032, MD040). Agents then hand-fix
the same three files on every branch. Gate 1 should not invent lint debt.

## What Changes

- Fix `scaffold-openspec-change.py` proposal spacing around capability headings/lists.
- Fix notaire-sdlc templates (`proposal.md`, `spec.md`, `traceability.md`) so seed +
  scaffold output is lint-clean.
- Add a `workspace/tests` guard that scaffolds a change and runs markdownlint-cli2.
- Document that archived main specs stay clean when the delta `spec` JSON is well-formed
  (no separate autofix step).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Scaffolded OpenSpec Markdown SHALL pass `markdownlint-cli2 --no-globs` | CU76 | New |

## Capabilities

### New Capabilities

- `openspec-scaffold-markdownlint`: scaffold/seed emit lint-clean OpenSpec Markdown

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` (harness) | yes | scaffold, templates, test |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows existing SDLC tooling; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | Note that scaffolded changes must lint clean |
| `CHANGELOG.md` | n/a — not user-visible |

## Out of Scope

Does not change markdownlint rule configuration or disable MD022/MD032/MD040.
Does not re-lint historical archived changes.
