# openspec-scaffold-markdownlint Specification

## Purpose

Keep OpenSpec Gate 1 scaffolds free of markdownlint debt so the local pipeline's
markdown-lint phase does not fail on generated files.

## Requirements

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

### Requirement: Archived main specs get a documented autofix

`openspec archive` MAY strip blank lines around headings when writing
`docs/openspec/specs/<cap>/spec.md`. After archive, agents SHALL run
`frontend/node_modules/.bin/markdownlint-cli2 --fix --no-globs` on the touched
main spec(s) so the result is lint-clean.

#### Scenario: Archive is followed by markdownlint autofix

- **WHEN** a change is archived with `openspec archive <name> --yes`
- **THEN** the agent runs markdownlint-cli2 `--fix` on each updated
  `docs/openspec/specs/<cap>/spec.md` and the files report zero lint issues
