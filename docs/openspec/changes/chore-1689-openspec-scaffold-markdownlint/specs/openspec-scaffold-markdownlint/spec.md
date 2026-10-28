<!-- Governed by CONSTITUTION.md. CU76, issue #1689. -->

## Purpose

Keep OpenSpec Gate 1 scaffolds free of markdownlint debt so the local pipeline's
markdown-lint phase does not fail on generated files.

## ADDED Requirements

### Requirement: Scaffolded change lints clean

The OpenSpec scaffold and notaire-sdlc templates SHALL emit Markdown that passes
`frontend/node_modules/.bin/markdownlint-cli2 --no-globs` with zero findings.

#### Scenario: Fresh scaffold has zero lint findings

- **WHEN** an agent scaffolds a change with a valid spec.json via
  `scaffold-openspec-change.py`
- **THEN** markdownlint-cli2 on that change's Markdown files reports zero issues

#### Scenario: Schema templates lint clean

- **WHEN** markdownlint-cli2 lints `docs/openspec/schemas/notaire-sdlc/templates/*.md`
- **THEN** it reports zero issues

### Requirement: Archived main specs stay lint-clean without autofix

A well-formed delta `spec.md` (blank lines around headings and lists, fenced blocks with
languages) SHALL remain lint-clean after `openspec archive` merges it into
`docs/openspec/specs/<cap>/spec.md`. No separate archive autofix step is required.

#### Scenario: Lint-clean delta archives without introducing lint debt

- **WHEN** a change whose delta spec already passes markdownlint is archived
- **THEN** the resulting main spec under `docs/openspec/specs/` also passes markdownlint
  without an autofix pass
