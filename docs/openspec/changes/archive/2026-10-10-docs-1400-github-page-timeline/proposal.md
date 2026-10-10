# Append Sept–Oct 2026 progress to the GitHub Pages timeline

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1400 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure (docs / public delivery site) |
| Branch | `cursor/docs-1400-github-page-timeline-c19f` |
| Gate 1 status | draft |

## Objetivo

The public site at https://matiaspakua.github.io/notaire/ still ends its Project History timeline in May–August 2026. Sept–Oct delivered the Cursor Cloud AI SDLC fleet, OpenSpec Gate 1, heavy-CI merge gating, and large API/UI hardening slices. Visitors should see that progress **appended** without rewriting earlier narrative.

## What Changes

- Append one or more timeline events (and related AI era / AI tools copy) in `github-page/` for Sept–Oct 2026.
- Do **not** delete or rewrite prior timeline entries (2014 → Aug 2026).
- Leave `deploy-github-page.yml` unchanged.
- English-only strings in touched copy.
- CHANGELOG entry under Unreleased.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Public project history is append-only — past eras stay visible | Issue #1400 AC | Made explicit |
| Living documentation stays in English for touched surfaces | Owner Englishize preference | Respected |

## Capabilities

### New Capabilities

- `github-pages-timeline`: the public Pages timeline preserves prior eras and appends new delivery milestones.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `github-page` | yes | Timeline (+ light AIEra / AITools copy) |
| Docs | yes | OpenSpec change + CHANGELOG |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Docs/marketing site only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one Unreleased line |
| `github-page` components | append timeline / update AI tooling narrative |
