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

Release Please fails on every push to main because GitHub Actions was not allowed to open PRs. The Owner enabled that setting; make the workflow rely on the default token explicitly and stop parsing pre-release history.

## What Changes

- `release-please.yml` passes `token: ${{ github.token }}` and documents the repository setting it depends on.
- `release-please-config.json` sets `bootstrap-sha` to `8ab8a6e` (#1043).
- `RELEASE.md` documents the setting and the manual close/reopen step that starts required checks on the release PR; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Release Please opens its PR with GITHUB_TOKEN while the Actions-PR setting is enabled; required checks are started by the Owner | #1264, #1040 | New |

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
- Configuration: repository setting "Allow GitHub Actions to create and approve pull requests" (enabled by the Owner)

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one Fixed entry |
| `docs/300-development/RELEASE.md` | Release PR permissions section |
