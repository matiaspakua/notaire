<!-- Governed by CONSTITUTION.md. CU76, issue #1599. Evidence: v0.2.1 Release has no assets; run 38366521356 failed `gh workflow run` without a git checkout. -->

## Purpose

The release-please job must start CD on the tag it creates even when that job
has no git working tree.

## ADDED Requirements

### Requirement: A release-please tag starts the delivery pipeline

The release workflow SHALL start the delivery pipeline on the tag it creates,
because a tag created with the repository token does not start a tag-triggered
workflow by itself. The dispatch SHALL name the GitHub repository explicitly so
`gh` does not need a git checkout. The delivery pipeline SHALL still start on a
manually pushed `v*` or legacy `notaire-v*` tag.

#### Scenario: Dispatch names the repository without a git checkout

- **WHEN** the dispatch step on the release-please job is read
- **THEN** it sets `GH_REPO` to `${{ github.repository }}` or passes `--repo ${{ github.repository }}` to `gh workflow run`, and it still passes the created tag as `--ref` to `cd.yml`

#### Scenario: Release creation dispatches delivery on the tag

- **WHEN** release-please reports that it created a release with a tag name
- **THEN** the release workflow dispatches the delivery workflow with that tag as the ref, using only the repository token and an `actions: write` permission

#### Scenario: No release created, no dispatch

- **WHEN** release-please only opens or updates its pull request
- **THEN** no delivery dispatch happens

#### Scenario: Manually pushed tags still publish

- **WHEN** the delivery workflow triggers are read
- **THEN** both the `v*` and the `notaire-v*` tag filters remain
