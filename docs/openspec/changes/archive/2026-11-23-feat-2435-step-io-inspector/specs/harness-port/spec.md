## ADDED Requirements

### Requirement: Step inputs and outputs

A `step.started` or `step.finished` harness event MAY carry `inputs` and `outputs`. Both SHALL use one
shape: `request`, `prompt`, `summary` (text), `files` (repository-relative paths), `commits` (hex SHAs)
and `links` (`label` plus an http or https `url`). The kit SHALL refuse an unknown key, a non-http
link, an absolute path or a path containing `..`, a non-hex commit, or an oversized text or list with
`invalid_body`, appending nothing. The run's step SHALL expose the recorded values, merged across its
events.

#### Scenario: a step records what it received and produced

- **GIVEN** an approved workflow and a started run
- **WHEN** `step.started` is reported with inputs (request, prompt, files) and `step.finished` with outputs (commits, files, links)
- **THEN** `run show` returns that step with both `inputs` and `outputs`

#### Scenario: an unsafe link is refused

- **GIVEN** a started run
- **WHEN** a step event carries a link with a `javascript:` URL
- **THEN** the report fails with `invalid_body` and the ledger gains no event

### Requirement: Station inputs and outputs view

The station detail SHALL list, for each step of the current run mapped to that stage, the step's inputs
and outputs. Files and commits SHALL link to the project repository when its URL is http(s); the prompt
SHALL open in an expandable block; a step with nothing recorded SHALL say so.

#### Scenario: inspecting a stage

- **GIVEN** the current run's `green` step (stage `pre-push`) recorded a prompt and two commits
- **WHEN** the user selects the `pre-push` station
- **THEN** the detail shows the prompt in an expandable block and the commits as links to the repository
