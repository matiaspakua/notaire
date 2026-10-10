# Surface business documentation on GitHub Pages `/docs/business`

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1441 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/docs-1441-pages-business-cf98` |
| Gate 1 status | passed |
| Parent | #1197 (monorepo prep — enterprise business + tech docs on Pages) |

## Objetivo

Pages `/docs/` copy already claims business and engineering docs are rendered, but the curated
site only covers modules, architecture, testing and security. `docs/100-business/` (SRS, CUxx,
actors, traceability, manuals) has no Pages entry — a gap against the #1197 prep goal.

## What Changes

- Add `/docs/business/` curated page with deep-links into `docs/100-business/` sections.
- Add Business to Docs chrome nav and home card grid.
- Add a workspace unit guard so Business nav/card/page cannot regress silently.
- `CHANGELOG.md` Unreleased entry.
- Deploy workflow unchanged (auto after main CI).

`skip_specs: true` — no OpenAPI/product requirement change; acceptance is verified by the unit
guard + Pages build/smoke (same pattern as other docs-only curated Pages slices).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Public Pages Docs surface includes business knowledge entry points | #1441, CU76, #1197 | New |
| Business Markdown trees remain SSOT under `docs/100-business/` | docs/100-business/README.md | Made explicit |

## Capabilities

### New Capabilities

- (none — `skip_specs: true`)

### Modified Capabilities

- (none — `skip_specs: true`)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `github-page` | yes | Business route, DocsChrome nav, home card |
| `workspace` | yes | Unit guard for Business Docs surface |
| `docs` | yes | OpenSpec change artifacts only |
| Others | no | — |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none new

### Architecture review

No ADR. Extends #1414 Pages Docs surface; aligns with #1197 enterprise doc readiness.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased entry for Business Docs page |
| `docs/100-business/*` | No content rewrite — deep-linked only |
| OpenSpec `docs-1441-pages-business/` | This change |

## Out of Scope

- Choosing LICENSE (#1226)
- Owner A/B/C on `deprecated/` (#1438)
- Rendering full Markdown bodies inside Pages (deep-link to GitHub, same as SAD)
- Closing #1197
