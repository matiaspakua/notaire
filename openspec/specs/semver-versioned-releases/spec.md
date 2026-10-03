# semver-versioned-releases Specification

## Purpose

Introduce an automated Semantic Versioning release process that creates `v*`
tags and GitHub Releases, rolls Keep a Changelog `[Unreleased]` into versioned
sections, and derives Maven and npm versions from the release tag. Source:
\#1043; CU76; Constitution §11.

## Requirements

### Requirement: Semver release process is automated and documented

The repository MUST have a documented, automated path to cut a SemVer release
tagged `vMAJOR.MINOR.PATCH` (e.g. release-please opening a release PR, or a
tag-triggered release workflow). Empty tags/releases on `main` today MUST be
replaced by that process for future cuts. Permanent DevSecOps/development docs
MUST describe how an operator/agent cuts a release.

#### Scenario: Automated release workflow exists

- **WHEN** the repository’s `.github/workflows/` (and any release-please
  config) are inspected after this change
- **THEN** there is an automated mechanism that produces a `v*` tag and a
  GitHub Release (release-please and/or tag-triggered workflow), not only
  manual `gh release create` instructions

#### Scenario: Release process is documented

- **WHEN** an operator reads the DevSecOps (or development) release
  documentation updated by this change
- **THEN** the steps to cut a release, which versions are bumped, how CHANGELOG
  rolls, and how CD publishes both images are described without relying on
  tribal knowledge

### Requirement: CHANGELOG Unreleased rolls into versioned releases

On each release cut, `CHANGELOG.md` MUST move curated `[Unreleased]` entries
into a new `## [X.Y.Z] - YYYY-MM-DD` (or Keep a Changelog–equivalent) section
and leave a fresh `[Unreleased]` heading for subsequent work.

#### Scenario: Unreleased section is rolled on release

- **WHEN** a release `vX.Y.Z` is produced by the automated process
- **THEN** `CHANGELOG.md` contains a versioned section for `X.Y.Z` populated
  from the prior `[Unreleased]` content (or release-please–generated notes
  merged into Keep a Changelog form), and `[Unreleased]` remains for new work

### Requirement: Maven and npm versions derive from the release tag

When a release tag `vX.Y.Z` is cut, the Maven reactor version (root
`pom.xml` / modules as applicable) and `frontend/package.json` `"version"`
MUST become `X.Y.Z` (no leading `v`). Development between releases MAY use
`-SNAPSHOT` or release-please’s interim versioning only if documented; the
tagged release commit MUST carry non-SNAPSHOT Maven and matching npm versions.

#### Scenario: Maven version matches tag without v prefix

- **WHEN** the release commit (or release PR merge) for tag `vX.Y.Z` is
  inspected
- **THEN** the root Maven project version is `X.Y.Z` (not `1.0-SNAPSHOT` and
  not `vX.Y.Z`)

#### Scenario: npm version matches tag without v prefix

- **WHEN** the same release commit for tag `vX.Y.Z` is inspected
- **THEN** `frontend/package.json` `"version"` is `X.Y.Z` (not left at
  unrelated `0.1.0` when a release has been cut)
