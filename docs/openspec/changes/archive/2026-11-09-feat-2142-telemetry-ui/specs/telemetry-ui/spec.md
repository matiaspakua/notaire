## ADDED Requirements

### Requirement: Static HTML monitor from evidence
The system SHALL support `aisdlc report --html <path>` that writes a self-contained HTML page summarizing stage/gate pass rates, stage durations, cost totals, and optional lead-time from a fixture. The page SHALL be read-only presentation of evidence JSON; it SHALL NOT provide controls that mutate policy, catalogs, or gates.

#### Scenario: HTML contains stage and cost summaries
- **WHEN** an evidence ledger has stage runs and cost entries and the operator runs `aisdlc report --html out.html`
- **THEN** the file exists and includes pass-rate and cost summary text without prompts, diffs, or secret values

#### Scenario: Lead-time fixture embeds without secrets
- **WHEN** `--lead-time-fixture` points at a forge sample JSON
- **THEN** the HTML includes lead-time seconds (or no-evaluable) and never includes a `prompt` field
