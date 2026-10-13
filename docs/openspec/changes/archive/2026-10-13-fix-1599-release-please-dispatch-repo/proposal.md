# Release-please CD dispatch names the repository without a git checkout

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1599 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-release-please-cd-dispatch-393d` |
| Gate 1 status | passed |
| SURFACE | CI/CD (no UI surface) |

## Objetivo

`v0.2.1` was cut with zero GitHub Release assets. The #1591 dispatch step ran
`gh workflow run cd.yml --ref v0.2.1` after `googleapis/release-please-action@v5`
and failed with `fatal: not a git repository`. `gh` infers the repository from
a working tree unless `GH_REPO` or `--repo` is set, so CD never starts on the
tag. This change makes the dispatch name the repository so the next cut attaches
the five assets.

## What Changes

- `release-please.yml` dispatch step sets `GH_REPO: ${{ github.repository }}`
  (repository token and `actions: write` stay).
- The existing unit test requires that the dispatch names the repository and
  still passes `--ref` to `cd.yml`.
- Permanent docs (RELEASE.md, ADR-028, CU76, CHANGELOG) record the `v0.2.1`
  evidence and the `GH_REPO` requirement.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A release-please tag starts CD with the repository token even when the job has no git checkout | CU76, ADR-028, #1591 | Made explicit — #1599 |
| Every `v*` Release still carries exactly five assets or the attach job fails | CU76, SBOM.md | Unchanged (enforced by #1591) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `release-asset-attachment`: dispatch names the GitHub repository without requiring a git working tree.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `release-please.yml` dispatch env `GH_REPO` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (uses existing `gh` on the runner and `github.token`)

### Architecture review

Follows ADR-028 decision 8 (dispatch with the repository token; no PAT). This is
not a new ADR: it tightens the same decision after the `v0.2.1` smoke failed.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/RELEASE.md` | Dispatch must set `GH_REPO`; step 5 is dispatch-on-tag, not a GITHUB_TOKEN tag push |
| `docs/200-architecture/202-ADR/ADR-028-release-and-version-identity.md` | Record that `gh` needs `GH_REPO` because the action leaves no checkout |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Cite #1599 on the dispatch bullet |
| `CHANGELOG.md` | `[Unreleased]` Fixed: CD dispatch names the repository so tag cuts attach assets |

## Out of Scope

- PDF reports (#178) — owned by another agent.
- OpenSpec archive of `chore-1591-release-foundation-completion` / `chore-1571-product-release`.
- Adding `frontend/package-lock.json` as a release-please extra-file.
- Changing Maven extra-files xpath / `type: pom`.
- Introducing a PAT or GitHub App for release-please.
- Moving or recreating tag `v0.2.1`.
