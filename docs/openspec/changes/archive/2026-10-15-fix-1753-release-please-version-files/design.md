# Design — release-please bumps every version source (#1753)

## Context

Release-please opens the release PR with `GITHUB_TOKEN`. Its `xml` updater
evaluates the configured XPath against the POM; the POMs declare
`xmlns="http://maven.apache.org/POM/4.0.0"`, so `/project/version` selects
nothing and the update is silently skipped. `openapi.yaml` is exported by
springdoc and compared byte-for-byte by the OpenAPI Contract workflow.

## Decisions

1. **POMs:** keep the `xml` updater and use `local-name()` steps
   (`/*[local-name()='project']/*[local-name()='version']`). No marker comments
   in the POMs.
2. **OpenAPI:** not a release-please extra-file. A `generic` marker comment would
   be wiped by the next export, and the `yaml` updater re-serializes the whole
   file. Instead, a workflow step after release-please (only when it reports a
   PR) checks out the release branch, runs `sync-openapi-version.py` with the
   manifest version, and commits only when the line changed. Release-please
   rebuilds its branch on every main push, so the step re-applies each time.
3. **Exception gate:** exempt only `github-actions[bot]` + `release-please--*`.
   The author alone is not enough (any workflow using `GITHUB_TOKEN` authors as
   that bot); the branch alone is not enough (a human could name a branch so).

## Goals / Non-Goals

- Goal: the next release PR is green with no manual commit and no label.
- Non-goal: changing who merges the release PR.

## Riesgos / Trade-offs

- A push with `GITHUB_TOKEN` starts no workflows, as today; the Owner still
  starts checks on the release PR by hand (RELEASE.md).
- Rollback: revert this PR.

## Testing Strategy

Python unit tests: `test_release_please_version_files.py` (POM XPaths vs real
POMs and manifest, sync script behaviour, workflow wiring) and three new
`test_pr_checks.py` cases for the exemption.

## Regression Strategy

Existing release and PR-check suites (`test_product_version_coherence`,
`test_release_runtime_identity`, `test_release_asset_attachment`, `test_pr_checks`).

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Takes effect on the next push to main (release-please run).

## Rollback Strategy

Revert this PR; release PRs then need the manual bumps and label again.

## Migration Plan

None. If #1718 is still open, release-please rebuilds it after this merges.
