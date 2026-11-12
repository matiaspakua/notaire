<!-- Governed by CONSTITUTION.md. CU76, issue #2257 remainders. -->

## ADDED Requirements

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
