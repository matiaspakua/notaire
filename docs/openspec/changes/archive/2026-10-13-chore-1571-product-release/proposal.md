# Product release: backend, frontend, database

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1571 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/release-product-schema-db96` |
| Gate 1 status | passed |
| SURFACE | UI (version label only) |

## Objetivo

A Notaire release is one semver cut of the backend, the frontend, and the
database schema. It lines up with the delivery trains without treating a
milestone title as a git tag, defines the CycloneDX SBOM, and shows the
version in the running system and on GitHub Pages.

## What Changes

- Record the three components in `release/product-release.json` and fail CI
  when Maven, npm, the manifest, or the Flyway id drift.
- Stop prefixing new tags with `notaire-` so CD's `v*` filter matches, and
  keep accepting legacy `notaire-v*` tags.
- Generate the product and database CycloneDX bills and attach them with the
  image bills.
- Show the product version on actuator info, OpenAPI, the sidebar, and a
  Pages version history that does not rewrite the narrative timeline.
- Correct the project-management docs where trains, the board, and releases
  were conflated.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| One product version covers Maven, npm, and the manifest | CU76, #1043 | Changed — Maven was stuck at 1.0-SNAPSHOT |
| The Flyway schema id is part of the release and is not the semver | CU76, ADR-007 | New |
| 0.x and 1.0.x advance the v1.0 train and do not close it | CU76, MILESTONES.md | New |
| A release does not close RF / CU / RNF catalog issues and does not add a board column | CU76, #1550 | Made explicit |
| New tags are `vX.Y.Z`; legacy `notaire-v*` still publish | CU76, #1043 | Changed |
| The product SBOM lists backend, frontend, and database | CU76 | New |

## Capabilities

### New Capabilities

- `product-release`: the semver cut, its three components, the train map, and the CycloneDX bills.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Maven version, filtered `app.version`, actuator info, OpenAPI version |
| `frontend` | yes | Sidebar shows `package.json` version |
| `frontend-swing` | no | — |
| `infra` / observability | no | Actuator info only; no new exporter |
| CI/CD (`.github/workflows`) | yes | CD tag filter and SBOM attach list |
| `workspace` | yes | `workspace/release/product_release.py` and tests |
| `docs` | yes | Release, SBOM, and project-management contract |
| `github-page` | yes | Version history section and docs tab |

### Surface area

- Entities: none
- Endpoints: `GET /actuator/info` gains a `notaire` detail (already public)
- Database (Flyway `V{n}`): none (the existing migrations are recorded, not edited)
- Configuration / `.env`: `app.version` is filtered from the Maven version
- Dependencies: none
- SURFACE: **UI** — a version line in the sidebar. No new route in the product app.

### Architecture review

The schema id stays a Flyway version (ADR-007). The product version stays
semver (release-please). They are joined in one record instead of being
forced equal. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/305-project-management/RELEASES.md` | Train vs tag contract |
| `docs/300-development/SBOM.md` | CycloneDX definition |
| `docs/300-development/RELEASE.md` | Operator path includes database and both tag prefixes |
| `CONSTITUTION.md` §11 | Three components, trains, SBOM, version surfaces |
| `docs/github/README.md`, `MILESTONES.md`, `CICLO-DE-VIDA.md` | Point at RELEASES.md; drop the stale three-bucket-only sentence |
| CU76 | Acceptance bullet for the three-component cut |
| `CHANGELOG.md` | Unreleased note |

## Out of Scope

- Renaming live GitHub milestones (needs a token that can edit them; #1569)
- Closing the v1.0 train or cutting `v1.0.0`
- Rewriting the Pages project-history timeline
- Merging the open release-please pull request from this branch
