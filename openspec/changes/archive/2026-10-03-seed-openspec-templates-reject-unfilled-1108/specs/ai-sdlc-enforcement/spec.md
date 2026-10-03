## ADDED Requirements

### Requirement: OpenSpec changes are seeded from notaire-sdlc templates

After `openspec new change`, or when invoked on an existing change folder,
`scripts/seed-openspec-change.sh` SHALL copy
`openspec/schemas/notaire-sdlc/templates/{proposal,design,tasks,traceability}.md`
into the change directory when those files are absent, and SHALL fill the Issue,
Use Case, Branch, and change-name values it is given. It SHALL NOT overwrite an
existing non-empty artifact file.

#### Scenario: Seed copies four templates when absent

- **WHEN** a change folder has only `.openspec.yaml` and the seed script runs
- **THEN** `proposal.md`, `design.md`, `tasks.md` and `traceability.md` exist and
  contain the mandatory `##` headings from the schema templates

#### Scenario: Seed leaves an existing file alone

- **WHEN** `proposal.md` already exists with filled content and the seed script runs
- **THEN** that file's content is unchanged

#### Scenario: Seed fills known header values

- **WHEN** the seed script is given `--issue 1108`, a Use Case string, and a branch
- **THEN** `proposal.md` and `traceability.md` show `#1108`, the Use Case, and the
  branch instead of the template placeholders

### Requirement: Unfilled template HTML-comment sections fail Gate 1

`scripts/validate-sdlc-plan.sh` SHALL reject a `notaire-sdlc` change when a
mandatory `##` section body in `proposal.md`, `design.md`, `tasks.md`, or
`traceability.md` is still only one or more template `<!-- ... -->` HTML
comments (after stripping comments and whitespace the body is empty). The error
SHALL name the file and the heading.

#### Scenario: Leftover HTML-comment body rejected

- **WHEN** `proposal.md` has `## Objetivo` whose body is only
  `<!-- Why this change is needed -->`
- **THEN** the validator exits non-zero and the message names `proposal.md` and
  `Objetivo`

#### Scenario: Filled section body accepted

- **WHEN** every checked `##` section has non-comment prose or table content
- **THEN** the leftover-comment check does not fail that change

#### Scenario: Rejection names file and heading

- **WHEN** `design.md` `## Decisions` is still only a template HTML comment
- **THEN** the failure text includes `design.md` and `Decisions`
