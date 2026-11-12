# issue-intake Specification

## Purpose

Issue intake lint and Foreman claim protocol (CU76 / #2257): agents start only from
issues that meet the configured lint, with an advisory-first clean-board report before
raising enforcement.

## Requirements

### Requirement: Issue intake lint

The kit SHALL provide `aisdlc intake check <issue>` that evaluates an issue's title, body and labels and reports each check as `pass`, `fail` or `no-evaluable`. Exit code SHALL be 0 when every check passes, 1 when any check fails, and 3 when the issue cannot be evaluated (no fetch command, or the fetch fails). A check that cannot run SHALL NOT be reported as pass.

#### Scenario: complete issue passes

- **WHEN** the issue has every required section with content, an acceptance checkbox, no placeholder, resolvable links and the required label prefixes
- **THEN** every check passes and the command exits 0

#### Scenario: missing or empty section fails

- **WHEN** the issue lacks the `Test plan` section or the section has no content
- **THEN** the check named after that section fails and the command exits 1

#### Scenario: placeholder or dangling link fails

- **WHEN** the body contains `#TBD` or a `#N` that the tracker cannot resolve
- **THEN** the placeholder or link check fails and the command exits 1

#### Scenario: missing label prefix fails

- **WHEN** the issue carries no label with a configured prefix such as `area:`
- **THEN** the label check fails

#### Scenario: no fetch command is no-evaluable

- **WHEN** the profile has no `[tracker] fetch` and no `--file` is given
- **THEN** the command reports `no-evaluable` and exits 3

### Requirement: Foreman claim refuses on intake fail when enforced

The kit SHALL provide `aisdlc foreman claim <issue> [--json] [--file F]` that runs
the intake lint and reports `intake_status`, `required`, `would_refuse`, and
`dispatch_allowed`. `would_refuse` SHALL be true whenever `intake_status` is not
`pass`. When `[intake] required = true`, `dispatch_allowed` SHALL be false and the
command SHALL exit 1. When `required = false` (advisory), `dispatch_allowed` SHALL
be true even if the lint fails, and the command SHALL exit 0. A no-evaluable lint
SHALL NOT be treated as pass.

#### Scenario: enforced claim refuses a failing issue

- **WHEN** `[intake] required = true` and the issue fails intake
- **THEN** `dispatch_allowed` is false, `would_refuse` is true, and the command exits 1

#### Scenario: advisory claim reports fail without refusing

- **WHEN** `[intake] required = false` and the issue fails intake
- **THEN** `would_refuse` is true, `dispatch_allowed` is true, and the command exits 0

#### Scenario: claim allows a passing issue

- **WHEN** the issue passes intake
- **THEN** `dispatch_allowed` is true, `would_refuse` is false, and the command exits 0

### Requirement: Clean-board intake report

The kit SHALL provide `aisdlc intake board [--json] [--file F]` that evaluates every
issue from `[tracker] list` (a command that prints a JSON array of issue objects) or
from `--file`. The report SHALL include per-issue status, counts, and `clean` which
is true only when every issue status is `pass`. The report SHALL note that
`[intake] required` is raise-only after a clean board. When `required = false`, a
dirty board SHALL exit 0 (advisory). When `required = true` and any issue fails or
is no-evaluable, the command SHALL exit 1. Missing list command and no `--file`
SHALL be `no-evaluable` (exit 3).

#### Scenario: clean board reports clean true

- **WHEN** every listed open issue passes intake
- **THEN** the board report has `clean=true` and overall status `pass`

#### Scenario: dirty board reports clean false

- **WHEN** at least one listed issue fails intake
- **THEN** the board report has `clean=false` and overall status `fail`

#### Scenario: no list command is no-evaluable

- **WHEN** the profile has no `[tracker] list` and no `--file` is given
- **THEN** the board command reports `no-evaluable` and exits 3

### Requirement: Adopter-configurable sections stay kit-independent

`[intake] sections` (and related lint keys) SHALL remain adopter-configurable. Kit
Python and templates under `framework/ai-sdlc` SHALL NOT hard-code Notaire product
paths (`backend-api/`, `docs/openspec/`, `matiaspakua/notaire`) as intake defaults.

#### Scenario: custom section names replace defaults

- **WHEN** the profile sets `[intake] sections = ["Problem"]`
- **THEN** intake accepts an issue that has a `Problem` section and does not require the default section names

#### Scenario: kit intake modules name no Notaire paths

- **WHEN** the intake and foreman claim sources are scanned
- **THEN** they contain no Notaire product path literals
