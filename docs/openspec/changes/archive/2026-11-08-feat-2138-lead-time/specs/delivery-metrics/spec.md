## ADDED Requirements

### Requirement: Lead time uses a fixed event pair
The kit SHALL define lead time as issue.created_at to the earliest linked pull_request.checks_green_at and expose it via aisdlc metrics lead-time from a JSON fixture.

#### Scenario: Notaire sample
- **WHEN** the Notaire sample fixture is evaluated
- **THEN** lead time for issue 2135 via PR 2144 is 6300 seconds

#### Scenario: No evaluable PR
- **WHEN** no linked PR has checks_green_at
- **THEN** status is no-evaluable and seconds is null

### Requirement: Stage durations come from the evidence ledger
The kit SHALL report per-stage median duration from ledger runs in aisdlc report and aisdlc metrics stages.

#### Scenario: Report JSON
- **WHEN** report --json runs with ledger data
- **THEN** the payload includes a stages array

### Requirement: Metrics redact secrets
Metric attributes SHALL NOT include prompts, diffs, or secret-shaped keys.

#### Scenario: Redaction
- **WHEN** emit_lead_time receives a prompt attribute
- **THEN** the emitted event does not contain the prompt