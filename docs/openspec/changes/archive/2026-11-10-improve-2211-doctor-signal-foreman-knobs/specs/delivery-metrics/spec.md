## ADDED Requirements

### Requirement: Lead-time operators pass Issue number not PR number
`aisdlc metrics lead-time --issue N` is Issue-keyed (forge issue open → linked PR
green). The CLI help for `--issue` and `aisdlc doctor` SHALL state that operators
must pass the **Issue** number, not a pull-request number. Passing a PR number
that is not an Issue remains `no-evaluable` (honest; not a pass).

#### Scenario: lead-time issue help says Issue not PR
- **WHEN** an operator runs `aisdlc metrics lead-time --help`
- **THEN** the `--issue` help text states that N is a forge Issue number (not a
  pull-request number)

#### Scenario: doctor INFO mentions Issue not PR for lead-time
- **WHEN** `aisdlc doctor` runs on a valid profile
- **THEN** output includes an INFO (or metrics/lead-time note) stating that
  `metrics lead-time --issue` takes an Issue number, not a PR number
