# Let Release Please open its release PR (#1264)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1264 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `fix/1264_release_please_token` |
| Gate 1 status | draft |

## Objetivo

Release Please fails on every push to main because GITHUB_TOKEN cannot open PRs; make the workflow use a dedicated token and stop parsing pre-release history.

## What Changes

- `release-please.yml` passes `token: ${{ secrets.RELEASE_PLEASE_TOKEN || github.token }}`.
- `release-please-config.json` sets `bootstrap-sha` to `8ab8a6e` (#1043).
- `RELEASE.md` documents the Owner-created secret; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The release PR is opened by an identity that triggers required checks | #1264, #1040 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `semver-versioned-releases`: Automated semver releases via release-please.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | workflow, release config, RELEASE.md, guard test |

### Surface area

- Entities / Endpoints / Flyway: none
- Configuration: new repository secret `RELEASE_PLEASE_TOKEN` (Owner)

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one Fixed entry |
| `docs/300-development/RELEASE.md` | Release token section |
