# Pages release record for v0.2.5

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1974 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/1974_pages_v025_release_record` |
| Gate 1 status | passed |

## Why

Pages release identity and CHANGELOG attribution must match the tagged `v0.2.5` cut so visitors and operators see one version.

## Objetivo

`v0.2.5` is tagged on GitHub, but the public Pages site still listed product version
`0.2.4` until `github-page/lib/releases.json` is regenerated, and Wave 9 changelog
bullets remained under `[Unreleased]` instead of under `## [0.2.5]`.

## What Changes

- Regenerate `github-page/lib/releases.json` so `productVersion` / `tag` / history lead with `0.2.5` (Flyway schema `53`).
- Move Wave 9 Security and related Bug Fixes from `[Unreleased]` into `## [0.2.5]`.
- Keep post-cut fixes (#1971 and later) under `[Unreleased]` until the next SemVer cut.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Pages release identity matches the latest SemVer tag | CU76; RELEASE.md; #1974 | Made explicit |
| CHANGELOG section for a cut lists what shipped in that tag | Conventional Commits; release-please | Applied |
| Accepted-breaking list stays empty on main after a prune cut | #1920; #1967 | Already true; documented in 0.2.5 notes |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `github-pages-release-identity` — data file only (`releases.json`)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github`) | no | – |
| `docs` | yes | OpenSpec change; `CHANGELOG.md` |
| `workspace` | yes | `github-page/lib/releases.json` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none (schema number already recorded as 53)
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Static data for the Pages Releases page. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `github-page/lib/releases.json` | Lead with 0.2.5 |
| `CHANGELOG.md` | Attribute Wave 9 to `[0.2.5]`; keep post-cut items Unreleased |

## Out of Scope

- Cutting `v0.2.6` for #1971 / #1970.
- Redesigning Pages UX (done in #1970).
