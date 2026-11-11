<!-- Governed by CONSTITUTION.md. CU76, issue #2257. -->

## ADDED Requirements

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
