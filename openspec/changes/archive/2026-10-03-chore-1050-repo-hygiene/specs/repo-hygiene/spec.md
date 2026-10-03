<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Keep the Notaire git repository hygienic for CI and contributors: precise
ignore rules, CODEOWNERS aligned to the live tree, large manuals outside
ordinary blobs, and an explicit ADR before any history rewrite. Source: #1050;
CU76; audit-2026-09.

## ADDED Requirements

### Requirement: Gitignore allows needed text assets and excludes Serena state

The repository `.gitignore` MUST NOT use an unbounded global `*.txt` rule that
prevents tracking legitimate text assets (including Python `requirements.txt`
under testing trees). It MUST ignore `.serena/` so Serena local project state
is not committed. Files currently tracked under `.serena/` MUST be removed from
the index (keep local copies optional).

#### Scenario: Needed requirements.txt is not ignored

- **WHEN** a contributor adds or restores `testing/e2e-swing/requirements.txt`
  (or another documented needed `*.txt` asset under an allowed path)
- **THEN** `git check-ignore -v` does not report a global `*.txt` rule blocking
  that path, and the file can be staged

#### Scenario: .serena is ignored and untracked

- **WHEN** the repository ignore rules and index are inspected after this change
- **THEN** `.serena/` matches an ignore rule and `git ls-files .serena` returns
  no paths

### Requirement: CODEOWNERS matches the current module tree

`.github/CODEOWNERS` MUST list ownership paths that exist in the current tree
and MUST NOT reference the removed `frontend-swing` module.

#### Scenario: CODEOWNERS has no frontend-swing path

- **WHEN** `.github/CODEOWNERS` is inspected after this change
- **THEN** it contains no `/frontend-swing/` (or equivalent) ownership path and
  still covers `/frontend/`, `/backend-api/`, and other live top-level modules
  as appropriate

### Requirement: Large user-manual PDF is not an ordinary git blob

The 13 MB `Manual de Usuario Notaire.doc.pdf` MUST NOT remain as a normal
git-tracked blob. It MUST be relocated to Git LFS and/or published as a GitHub
Release (or equivalent) asset, with permanent docs explaining how to obtain it.

#### Scenario: PDF relocated from ordinary blob storage

- **WHEN** `git ls-files -s` (or equivalent) is inspected for the user-manual PDF
  path after this change
- **THEN** either the path is absent from the tree with a documented Release/LFS
  fetch path, or the git object is an LFS pointer (not a full 13 MB blob)

### Requirement: History rewrite requires an ADR decision

Before any `git filter-repo` (or equivalent) rewrite of shared history, an ADR
under `docs/200-architecture/202-ADR/` MUST record the decision (proceed now,
defer with follow-up issue, or never) including risks to clones/PRs and the
list of historical heavy blobs in scope (related #585 / #682).

#### Scenario: ADR documents filter-repo decision

- **WHEN** Gate 3 documentation for this change is reviewed
- **THEN** a new ADR exists that states the filter-repo decision and points to
  related cleanup issues for historical binaries without silently rewriting
  `main` in this PR unless the ADR explicitly authorizes it in a follow-up
