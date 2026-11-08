# Release-please bumps every version source and its PR needs no exception label

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1753 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `1753/fix/release-please-version-files` |
| Gate 1 status | passed |
| SURFACE | CI/CD (no UI surface) |

## Objetivo

The 0.2.1 (#1597) and 0.2.2 (#1718) release PRs both arrived red and needed
manual commits plus the Owner-only `sdlc-exception` label. Three causes:

1. The POM `xml` extra-files use `/project/version`, but the POMs declare the
   Maven default namespace, so the XPath matches nothing.
2. `openapi.yaml` `info.version` is never bumped. The file is regenerated
   byte-for-byte by springdoc, so it cannot carry a release-please marker comment.
3. `check-sdlc-exception.sh` rejects the bot PR, which never carries an OpenSpec change.

## What Changes

- `release-please-config.json`: namespace-agnostic `local-name()` XPaths for both POMs.
- `release-please.yml`: when a release PR is opened or updated, check out its branch,
  run `workspace/release/sync-openapi-version.py` with the manifest version, and
  commit the change if any.
- `check-sdlc-exception.sh`: exempt a PR only when the author is
  `github-actions[bot]` **and** the head branch starts with `release-please--`;
  `sdlc-process.yml` passes `PR_HEAD_REF`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A release PR bumps every product version source, POMs and OpenAPI included | ADR-028, release-version-coherence | Made explicit — #1753 |
| The release-please PR needs no human `sdlc-exception` label | CONSTITUTION §12 (bots exempt) | Changed — #1753 |

## Capabilities

### Modified Capabilities

- `release-version-coherence`: the release PR bumps POMs and OpenAPI `info.version`.
- `ai-sdlc-enforcement`: the exception gate exempts the release-please PR.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`, `workspace/`) | yes | release-please config + workflow, exception gate |

### Surface area

- Entities / endpoints / database / dependencies: none
- Configuration: `release-please-config.json`

### Architecture review

Follows ADR-028 (one product version). No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/RELEASE.md` | POM XPaths, OpenAPI sync step, no exception label on the release PR |
| `CHANGELOG.md` | `[Unreleased]` Bug Fixes entry |

## Out of Scope

- Changing who may merge; the Owner still merges the release PR.
- Exempting any other `github-actions[bot]` PR.
